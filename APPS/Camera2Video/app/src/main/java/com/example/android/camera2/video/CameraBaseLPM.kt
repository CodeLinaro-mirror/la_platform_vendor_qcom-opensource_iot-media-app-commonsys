/*
# Copyright (c) 2020 - 2022, 2024-2025 Qualcomm Innovation Center, Inc.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted (subject to the limitations in the
# disclaimer below) provided that the following conditions are met:
#
#    * Redistributions of source code must retain the above copyright
#      notice, this list of conditions and the following disclaimer.
#
#    * Redistributions in binary form must reproduce the above
#      copyright notice, this list of conditions and the following
#      disclaimer in the documentation and/or other materials provided
#      with the distribution.
#
#    * Neither the name Qualcomm Innovation Center nor the names of its
#      contributors may be used to endorse or promote products derived
#      from this software without specific prior written permission.
#
# NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE
# GRANTED BY THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
# HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
# IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
# GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
# IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
# OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
# IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.example.android.camera2.video

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.util.Range
import android.view.Surface
import android.widget.Toast
import com.example.android.camera.utils.OrientationLiveData.Companion.getOrientationValueForRotation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.*
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates


class CameraBaseLPM(val context: Context): CameraModuleLPM {

    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val cameraThread = HandlerThread("CameraThread").apply { start() }

    private val cameraHandler = Handler(cameraThread.looper)

    private lateinit var characteristics: CameraCharacteristics

    private lateinit var camera: CameraDevice

    lateinit var session: CameraCaptureSession

    lateinit var previewRequest: CaptureRequest.Builder

    lateinit var captureRequest: CaptureRequest.Builder

    private val streamSurfaceList = mutableListOf<Surface>()

    private val snapshotSurfaceList = mutableListOf<Surface>()

    private val sharedStreamSurfaceList = mutableListOf<List<Surface>>()

    private val recorderList = mutableListOf<VideoRecorder>()

    private var previewFps = 30

    private var streamConfigOpMode: Int = 0x00

    private var enableZSL = true

    private lateinit var imageReader: ImageReader

    private val imageReaderThread = HandlerThread("imageReaderThread").apply { start() }

    private val imageReaderHandler = Handler(imageReaderThread.looper)

    var currentSnapshotFilePath: String? = null

    val closeSync = Object()

    val takeSnapshotSemaphore  = Semaphore(1)
    private val takeMJPEGSemaphore  = Semaphore(1)

    var listeners = mutableListOf<CameraReadyListener>()

    var isCameraReady: Boolean by Delegates.observable(false) { _, old, new ->
        listeners.forEach { it.onIsCameraReadyUpdated(old, new) }
    }

    private lateinit var mjpegContentValues: ContentValues
    private var mjpegUri: Uri? = null
    private var mjpegBufferStream: BufferedOutputStream? = null
    var mjpegRecording = false

    private val initializationLatch = CountDownLatch(1)

    override fun getAvailableCameras(): Array<String> = cameraManager.cameraIdList

    override fun getSensorOrientation(): Int {
        return characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
    }

    @SuppressLint("MissingPermission")
    override suspend fun openCamera(cameraId: String) {
        Log.i(TAG, "openCamera")
        camera = suspendCancellableCoroutine { cont ->
            val callback = object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.i(TAG, "openCamera onOpened")
                    cont.resume(camera)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.w(TAG, "Camera $cameraId has been disconnected")
                    CameraActivity().finish()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    val msg = when(error) {
                        ERROR_CAMERA_DEVICE -> "Fatal (device)"
                        ERROR_CAMERA_DISABLED -> "Device policy"
                        ERROR_CAMERA_IN_USE -> "Camera in use"
                        ERROR_CAMERA_SERVICE -> "Fatal (service)"
                        ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                        else -> "Unknown"
                    }
                    val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                    Log.e(TAG, exc.message, exc)
                    if (cont.isActive) cont.resumeWithException(exc)
                }

                override fun onClosed(camera: CameraDevice) {
                    Log.i(TAG, "openCamera onClosed")
                    super.onClosed(camera)
                    clearStreams()
                    synchronized(closeSync) {
                        closeSync.notifyAll()
                    }
                }
            }
            cameraManager.openCamera(cameraId, callback, cameraHandler)
        }
        previewRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        characteristics = cameraManager.getCameraCharacteristics(cameraId)
        Log.i(TAG, "openCamera done")
    }

    private fun createAndConfigureSession(targets: List<Surface>) {
        Log.i(TAG, "createAndConfigureSession enter")

        val outConfigurations = mutableListOf<OutputConfiguration>()
        for (surface in targets) {
            outConfigurations.add(OutputConfiguration(surface))
            previewRequest.addTarget(surface)
            captureRequest.addTarget(surface)
        }
        for (surface in snapshotSurfaceList) {
            outConfigurations.add(OutputConfiguration(surface))
            captureRequest.addTarget(surface)
        }

        for (sharedSurface in sharedStreamSurfaceList) {
            val sharedOutputConfig = OutputConfiguration(sharedSurface[0])
            sharedOutputConfig.enableSurfaceSharing()
            previewRequest.addTarget(sharedSurface[0])
            captureRequest.addTarget(sharedSurface[0])
            for (surface in sharedSurface.takeLast(sharedSurface.size - 1)) {
                sharedOutputConfig.addSurface(surface)
                previewRequest.addTarget(surface)
                captureRequest.addTarget(surface)
            }
            outConfigurations.add(sharedOutputConfig)
        }

        previewRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(previewFps, previewFps))
        captureRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(previewFps, previewFps))

        // Set ZSL Mode
        captureRequest.set(CaptureRequest.CONTROL_ENABLE_ZSL, enableZSL)

        Log.d(TAG, "Operation Mode: $streamConfigOpMode")

        val config = SessionConfiguration(
                streamConfigOpMode, outConfigurations, HandlerExecutorLPM(cameraHandler),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(s: CameraCaptureSession) {
                        session = s
                        initializationLatch.countDown()
                    }

                    override fun onConfigureFailed(s: CameraCaptureSession) =
                            s.device.close()
                })
        config.sessionParameters = previewRequest.build()

        camera.createCaptureSession(config)

        Log.i(TAG, "createAndConfigureSession exit")
    }

    private fun setRepeatingRequests(session: CameraCaptureSession) {
        Log.i(TAG, "setRepeatingRequests enter")
        Log.i(TAG, "onConfigured session")
        this.session = session
        // if there is no active surface, do not set setRepeatingRequest.
        if (streamSurfaceList.isNotEmpty()) {
            session.setRepeatingRequest(previewRequest.build(), null, cameraHandler)
            Log.i(TAG, "setRepeatingRequest done")
        }
        isCameraReady = true
        Log.i(TAG, "isCameraReady true")
        Log.i(TAG, "setRepeatingRequests exit")
    }

    override fun startRepeatingRequests() {
        Log.i(TAG, "startRepeatingRequests enter")
        initializationLatch.await()
            session?.let {
                setRepeatingRequests(session)
            } ?: Log.e(TAG, "Session is not initialized")
        Log.i(TAG, "startRepeatingRequests exit")
    }

    private fun getJsonString(fileName: String): String? {
        val file = File(context.filesDir, fileName)
        var jsonString: String? = null
        try {
            jsonString = File(file.absolutePath).bufferedReader().use { it.readText() }
            Log.i(TAG, "Json String: $jsonString")
        } catch (ioException: IOException) {
            Log.e(TAG, "Not able to fetch Json String $ioException")
        }
        return jsonString
    }

    override fun addPreviewStream(surface: Surface) {
        streamSurfaceList.add(surface)
    }

    override fun addStream(surface: Surface)  {
        streamSurfaceList.add(surface)
    }

    override fun addRecorderStream(stream: StreamInfo)  {
        val recorder = MediaCodecRecorder(context, stream)
        streamSurfaceList.add(recorder.getRecorderSurface())
        recorderList.add(recorder)
    }

    override fun addSharedStream(surfaceList: List<Surface>) {
        sharedStreamSurfaceList.add(surfaceList)
    }

    override fun addVideoRecorder(recorder: VideoRecorder) {
        recorderList.add(recorder)
    }

    @SuppressLint("Range")
    override fun addSnapshotStream(stream: StreamInfo) {
        val format = when (stream.encoding) {
            "JPEG" -> ImageFormat.JPEG
            "RAW" -> ImageFormat.RAW10
            else -> {
                throw Exception("Unsupported image format: ${stream.encoding}")
            }
        }
        if (format == ImageFormat.JPEG) {
            imageReader = ImageReader.newInstance(
                    stream.width, stream.height, format, IMAGE_BUFFER_SIZE)
            // Set JPEG Quality
            captureRequest.set(CaptureRequest.JPEG_QUALITY, IMAGE_JPEG_QUALITY)
        } else if (format == ImageFormat.RAW10) {
            val size = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                    .getOutputSizes(format).maxByOrNull { it.height * it.width }!!
            imageReader = ImageReader.newInstance(
                    size.width, size.height, format, IMAGE_BUFFER_SIZE)
        } else {
            throw Exception("Unsupported image format: ${stream.encoding}")
        }
        snapshotSurfaceList.add(imageReader.surface)
    }

    override fun startLPMSession() {
        Log.i(TAG, "startLPMSession enter")
        createAndConfigureSession(streamSurfaceList)
        Log.i(TAG, "startLPMSession exit")
    }

    override fun stopLPMSession() {
        Log.i(TAG, "stopLPMSession enter")
        if (::session.isInitialized) {
            session.stopRepeating()
            session.abortCaptures()
            Log.i(TAG, "abortCaptures done")
        }
        Log.i(TAG, "stopLPMSession exit")
    }

    private fun clearStreams() {
        Log.i(TAG, "clearStreams")
        streamSurfaceList.clear()
        recorderList.clear()
        snapshotSurfaceList.clear()
        sharedStreamSurfaceList.clear()
    }

    override fun startRecording(orientation: Int?) {
        for (recorder in recorderList) {
            recorder.start(orientation)
        }
    }
    override fun isRecording() = false

    override fun stopRecording()  {
        for (recorder in recorderList) {
            recorder.stop()
        }
    }

    override fun takeMJPEG(start: Boolean) {
        Log.i(TAG, "takeMJPEG start:$start")
        if (start) {
            takeMJPEGSemaphore.acquire()
            mjpegRecording = true

            val mjpegOutputFile = "${createFileName()}.mjpeg"
            mjpegContentValues = ContentValues()
            mjpegContentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, mjpegOutputFile)
            mjpegContentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            mjpegContentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
            mjpegUri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), mjpegContentValues)
            val imageOutStream = mjpegUri?.let { context.contentResolver.openOutputStream(it) }
            mjpegBufferStream = BufferedOutputStream(imageOutStream)

            // Clear imageReader
            var image: Image? = null
            do {
                image?.close()
                image = imageReader.acquireNextImage()
            } while (image != null)
            session.setRepeatingRequest(captureRequest.build(), null, cameraHandler)
            var count = 0
            var initialTime: Long = 0
            imageReader.setOnImageAvailableListener({ reader ->
                image = reader.acquireNextImage()
                if (count == 0) {
                    initialTime = image!!.timestamp
                }
                count++
                val buffer = image?.planes?.get(0)?.buffer
                val bytes = ByteArray(buffer!!.remaining()).apply { buffer.get(this) }
                mjpegBufferStream?.write(bytes)
                if (!mjpegRecording) {
                    imageReader.setOnImageAvailableListener(null, null)
                    session.setRepeatingRequest(previewRequest.build(), null, cameraHandler)
                    // Save the mjpeg file
                    mjpegBufferStream?.flush()
                    mjpegBufferStream?.close()

                    mjpegContentValues.clear()
                    mjpegContentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    mjpegUri?.let {context.contentResolver.update(it, mjpegContentValues, null, null)}

                    val finalTime = image!!.timestamp
                    Log.i(TAG, "saved mjpeg file with fps = ${count / ((finalTime - initialTime) / 1000000000.0)}")
                    takeMJPEGSemaphore.release()
                }
                image?.close()
            }, imageReaderHandler)
        } else {
            mjpegRecording = false
            takeMJPEGSemaphore.acquire()
            takeMJPEGSemaphore.release()
        }
    }
    override fun takeSnapshot(orientation: Int?): CombinedCaptureResult {
        @Suppress("ControlFlowWithEmptyBody")
        Log.i(TAG, "takeSnapshot")
        while (imageReader.acquireNextImage() != null) {
        }

        val imageQueue = ArrayBlockingQueue<Image>(IMAGE_BUFFER_SIZE)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage()
            Log.d(TAG, "Image available in queue: ${image.timestamp}")
            imageQueue.add(image)
        }, imageReaderHandler)
        takeSnapshotSemaphore.acquire()
        lateinit var combinedCaptureResult: CombinedCaptureResult
        session.capture(captureRequest.build(), object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult) {
                super.onCaptureCompleted(session, request, result)
                val resultTimestamp = result.get(CaptureResult.SENSOR_TIMESTAMP)
                Log.d(TAG, "Capture result received: $resultTimestamp")

                var image: Image
                do {
                    image = imageQueue.take()
                } while (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                        image.format != ImageFormat.DEPTH_JPEG &&
                        image.timestamp != resultTimestamp)
                Log.d(TAG, "Matching image dequeued: ${image.timestamp}")

                imageReader.setOnImageAvailableListener(null, null)
                while (imageQueue.size > 0) {
                    imageQueue.take().close()
                }
                // Compute EXIF orientation metadata
                val exifOrientation = getOrientationValueForRotation(orientation ?: 0)
                combinedCaptureResult = CombinedCaptureResult(image, result, exifOrientation, imageReader.imageFormat)
                takeSnapshotSemaphore.release()
            }
        }, cameraHandler)
        takeSnapshotSemaphore.acquire()
        takeSnapshotSemaphore.release()
        return combinedCaptureResult
    }


    suspend fun saveResult(result: CombinedCaptureResult): String? = suspendCoroutine { cont ->
        Log.i(TAG, "saveResult")
        when (result.format) {
            ImageFormat.JPEG, ImageFormat.DEPTH_JPEG -> {
                val buffer = result.image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining()).apply { buffer.get(this) }
                try {
                    val filename = "${createFileName()}.jpg"
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/Camera")
                    values.put(MediaStore.Images.Media.IS_PENDING, 1)
                    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    currentSnapshotFilePath = "/storage/emulated/0/DCIM/Camera/$filename"
                    val imageOutStream = uri?.let { context.contentResolver.openOutputStream(it) };
                    imageOutStream?.write(bytes)

                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    uri?.let {context.contentResolver.update(it, values, null, null)}
                    cont.resume(currentSnapshotFilePath)
                } catch (exc: IOException) {
                    Log.e(TAG, "Unable to write JPEG image to file", exc)
                    cont.resumeWithException(exc)
                }
            }

            ImageFormat.RAW10 -> {
                val buffer = result.image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining()).apply { buffer.get(this) }
                try {
                    val filename = "${createFileName()}.raw"
                    val values = ContentValues()
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                    values.put(MediaStore.MediaColumns.IS_PENDING, 1)
                    val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
                    val imageOutStream = uri?.let { context.contentResolver.openOutputStream(it) };
                    imageOutStream?.write(bytes)

                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    uri?.let {context.contentResolver.update(it, values, null, null)}
                    cont.resume(currentSnapshotFilePath)
                } catch (exc: IOException) {
                    Log.e(TAG, "Unable to write raw image to file", exc)
                    cont.resumeWithException(exc)
                }
            }

            else -> {
                val exc = RuntimeException("Unknown image format: ${result.image.format}")
                Log.e(TAG, exc.message, exc)
                cont.resumeWithException(exc)
            }
        }
    }

    fun getCurrentVideoFilePathList():ArrayList<String> {
        val currentVideoFilePathList:ArrayList<String> = ArrayList()
        for (recorder in recorderList) {
            if(recorder.getCurrentVideoFilePath()!=null) {
                currentVideoFilePathList.add(recorder.getCurrentVideoFilePath()!!)
            }
        }
        return currentVideoFilePathList
    }

    override fun closeCamera() {
        Log.i(TAG, "closeCamera enter")
        if (::session.isInitialized) {
            session.stopRepeating()
            session.abortCaptures()
        }

        if (::camera.isInitialized) {
            camera.close()
            synchronized(closeSync) {
                closeSync.wait(CLOSESYNC_TIMEOUT)
            }
        }
        streamConfigOpMode = 0x00
        Log.i(TAG, "closeCamera exit")
    }

    override fun stopRepeatingRequests() {
        Log.i(TAG, "stopRepeatingRequests enter")
        if (::session.isInitialized) {
            session.stopRepeating()
            Log.i(TAG, "stopRepeating done")
        }
        Log.i(TAG, "stopRepeatingRequests exit")
    }

    override fun setZSL(value: Boolean) {
        Log.d(TAG, "ZSL Value: $value")
        enableZSL = value
    }

    private fun updateRepeatingRequest() {
        if (::session.isInitialized) {
            session.setRepeatingRequest(previewRequest.build(), null, cameraHandler)
            Log.i(TAG, "update setRepeatingRequest done")
        }
    }

    private fun showTextDialog(filename: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(CameraActivity.mActivity?.get())
        try {
            builder.setMessage(readFile(context.filesDir.absolutePath + filename))
                    .setCancelable(true)
                    .setPositiveButton("Okay") { dialog, _ -> dialog.cancel() }.show()
        } catch (e: IOException) {
            Toast.makeText(context, "Error in reading file", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    private fun readFile(path: String): String? {
        val stream = FileInputStream(File(path))
        return stream.use { stream ->
            val fc: FileChannel = stream.channel
            val bb: MappedByteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size())
            Charset.defaultCharset().decode(bb).toString()
        }
    }

    override fun setLPMTimeout(value: Int) {
        Log.d(TAG, "LPM Timeout Level: $value")
        VendorTagUtil.setLPMTimeout(previewRequest, value)
        if (::captureRequest.isInitialized) {
            VendorTagUtil.setLPMTimeout(captureRequest, value)
        }
        updateRepeatingRequest()
    }

    companion object {
        private val TAG = CameraBaseLPM::class.simpleName

        data class CombinedCaptureResult(
                val image: Image,
                val metadata: CaptureResult,
                val orientation: Int,
                val format: Int
        ) : Closeable {
            override fun close() = image.close()
        }

        private const val CLOSESYNC_TIMEOUT = 1000L
        private const val IMAGE_BUFFER_SIZE: Int = 8
        private const val IMAGE_CAPTURE_TIMEOUT_MILLIS: Long = 5000
        private const val IMAGE_JPEG_QUALITY: Byte = 85

        private fun createFile(context: Context, extension: String): File {
            val dir = File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), "Camera")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return File.createTempFile(createFileName(), ".$extension", dir)
        }
        private fun createFileName(): String {
            val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
            return "IMG_${sdf.format(Date())}"
        }
    }
}

private class HandlerExecutorLPM(private val handler: Handler?) : Executor {
    override fun execute(command: Runnable) {
        handler?.post(command)
    }
}
