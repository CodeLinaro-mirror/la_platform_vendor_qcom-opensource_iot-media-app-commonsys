/*
# Copyright (c) 2020-2022, 2024-2025 Qualcomm Innovation Center, Inc.
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

package com.example.android.camera2.video.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ExifInterface
import android.media.MediaActionSound
import android.media.ThumbnailUtils
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.android.camera.utils.AutoFitSurfaceView
import com.example.android.camera.utils.OrientationLiveData
import com.example.android.camera.utils.getPreviewOutputSize
import com.example.android.camera2.video.*
import com.example.android.camera2.video.CameraActivity.Companion.printAppVersion
import com.example.android.camera2.video.CameraSettingsUtil.getCameraSettings
import com.example.android.camera2.video.MediaCodecRecorder.Companion.MIN_REQUIRED_RECORDING_TIME_MILLIS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraFragmentLPM : Fragment(), CameraReadyListener {
    private val cameraManager: CameraManager by lazy {
        val context = requireContext().applicationContext
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    private lateinit var cameraBaseLPM: CameraBaseLPM
    private lateinit var characteristics: CameraCharacteristics
    private lateinit var viewFinder: AutoFitSurfaceView
    private lateinit var settings: CameraSettings
    private lateinit var relativeOrientation: OrientationLiveData
    private lateinit var previewSize: Size
    private val captureButton by lazy { (CameraActivity.mActivity?.get() as CameraActivity).findViewById<ImageButton>(R.id.capture_button) }
    private val recorderButton by lazy { (CameraActivity.mActivity?.get() as CameraActivity).findViewById<ImageButton>(R.id.recorder_button) }
    private val chronometer by lazy { (CameraActivity.mActivity?.get() as CameraActivity).findViewById<Chronometer>(R.id.chronometer) }
    private val thumbnailButton by lazy { (CameraActivity.mActivity?.get() as CameraActivity).findViewById<ImageView>(R.id.thumbnailButton) }
    private val openCameraButton by lazy { (CameraActivity.mActivity?.get() as CameraActivity).findViewById<Button>(R.id.open_camera_button) }
    private val startSessionButton by lazy { (CameraActivity.mActivity?.get() as CameraActivity).findViewById<Button>(R.id.start_session_button) }
    private val startPreviewButton by lazy { (CameraActivity.mActivity?.get() as CameraActivity).findViewById<Button>(R.id.start_preview_button) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera_lpm, container, false)

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        printAppVersion(requireContext().applicationContext)
        cameraBaseLPM = CameraBaseLPM(requireContext().applicationContext)
        cameraBaseLPM.listeners.add(this)
        settings = getCameraSettings(requireContext().applicationContext)
        captureButton.visibility = View.INVISIBLE
        recorderButton.visibility = View.INVISIBLE
        characteristics = cameraManager.getCameraCharacteristics(settings.cameraId)
        viewFinder = view.findViewById(R.id.view_finder)
        viewFinder.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) = Unit
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit
            override fun surfaceCreated(holder: SurfaceHolder) {
                previewSize = if (settings.previewInfo.fps > 30 && settings.cameraId != "4") {
                    Size(1280, 720)
                } else if (settings.cameraId == "4") {
                    Size(1440, 720)
                } else {
                    getPreviewOutputSize(viewFinder.display, characteristics, SurfaceHolder::class.java)
                }
                Log.d(TAG, "View finder size: ${viewFinder.width} x ${viewFinder.height}")
                Log.d(TAG, "Selected preview size: $previewSize")
                viewFinder.setAspectRatio(previewSize.width, previewSize.height)
            }
        })

        val cameraMenu = CameraMenu(this.context, view)
        startSessionButton.visibility = View.GONE
        startPreviewButton.visibility = View.GONE

        openCameraButton.setOnClickListener {
            if (openCameraButton.text.toString() == "Open Camera") {
                openCameraButton.text = "Close Camera"
                startSessionButton.text = "Start Session"
                startSessionButton.visibility = View.VISIBLE
                initializeCamera()
            } else {
                openCameraButton.text = "Open Camera"
                startSessionButton.visibility = View.GONE
                startPreviewButton.visibility = View.GONE
                cameraBaseLPM.closeCamera()
            }
        }

        startSessionButton.setOnClickListener {
            if (startSessionButton.text.toString() == "Start Session") {
                startSessionButton.text = "Stop Session"
                startPreviewButton.text = "Start Preview"
                startPreviewButton.visibility = View.VISIBLE
            } else {
                startSessionButton.text = "Start Session"
                startPreviewButton.visibility = View.GONE
                cameraBaseLPM.stopLPMSession()
            }
        }
        startPreviewButton.setOnClickListener {
            if (startPreviewButton.text.toString() == "Start Preview") {
                startPreviewButton.text = "Stop Preview"
                cameraMenu.setOnLPMTimeoutListener(object : CameraMenu.OnLPMTimeoutListener {
                    override fun onLPMTimeoutValue(value: Int) {
                        cameraBaseLPM.setLPMTimeout(value)
                        Log.d(TAG, "Timeout value: $value")
                    }
                })
                view.setOnClickListener {
                cameraMenu.show()
                }
                if (settings.snapshotOn) captureButton.visibility = View.VISIBLE
                if (!settings.recorderInfo.isEmpty()) recorderButton.visibility = View.VISIBLE
                cameraBaseLPM.startRepeatingRequests()
            } else {
                startPreviewButton.text = "Start Preview"
                cameraMenu.clearTimeoutInput()
                captureButton.visibility = View.INVISIBLE
                recorderButton.visibility = View.INVISIBLE
                view.setOnClickListener {
                    cameraMenu.hide()
                }
                cameraBaseLPM.stopRepeatingRequests()
            }
        }

        relativeOrientation = OrientationLiveData(requireContext(), characteristics).apply {
            observe(viewLifecycleOwner, Observer { orientation ->
                Log.d(TAG, "Orientation changed: $orientation")
                val sensorOrientationDegrees = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
                val sign = if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) 1 else -1
                val requiredOrientation = ((orientation - sensorOrientationDegrees) * sign).toFloat()
                recorderButton.rotation = requiredOrientation
                chronometer.rotation = requiredOrientation
                thumbnailButton.rotation = requiredOrientation
            })
        }
    }

    private fun startChronometer() {
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.visibility = View.VISIBLE
        chronometer.start()
    }

    private fun stopChronometer() {
        chronometer.visibility = View.INVISIBLE
        chronometer.stop()
    }

    private fun createThumb(path: String?, type: Int): Bitmap? {
        return if (type == THUMBNAIL_TYPE_IMAGE) {
            path?.let { ThumbnailUtils.createImageThumbnail(it, MediaStore.Images.Thumbnails.MICRO_KIND) }
        } else {
            path?.let { ThumbnailUtils.createVideoThumbnail(it, MediaStore.Video.Thumbnails.MICRO_KIND) }
        }
    }

    private fun createRoundThumb(path: String?, type: Int): RoundedBitmapDrawable? {
        Log.i(TAG, "createRoundThumb path=$path type=$type")
        CameraActivity.saveThumbnailData(path, type)
        return createThumb(path, type)?.let {
            RoundedBitmapDrawableFactory.create(resources, it).apply { this.isCircular = true }
        }
    }

    private fun addCameraStreams(camBase: CameraBaseLPM, settings: CameraSettings) {
        Log.i(TAG, "addCameraStreams start")
        var availableCameraStreams = MAX_CAMERA_STREAMS
        if (settings.displayOn) {
            camBase.addPreviewStream(viewFinder.holder.surface)
            Log.i(TAG, "addCameraStreams preview ${settings.previewInfo}")
            availableCameraStreams--
        }
        if (settings.snapshotOn) {
            camBase.addSnapshotStream(settings.snapshotInfo)
            availableCameraStreams--
        }
        val sharedStreamSurfaces = mutableListOf<Surface>()
        var sharedStreamsSize: Size = Size(0, 0)
        for ((streamCount, stream) in settings.recorderInfo.withIndex()) {
            if (availableCameraStreams - streamCount <= 1) {
                if (sharedStreamsSize.width < stream.width) {
                    sharedStreamsSize = Size(stream.width, stream.height)
                }
            }
        }
        for ((streamCount, stream) in settings.recorderInfo.withIndex()) {
            val recorder = VideoRecorderFactory(requireContext().applicationContext, stream, stream.videoRecorderType)
            camBase.addVideoRecorder(recorder)
            if (availableCameraStreams > 1) {
                camBase.addStream(recorder.getRecorderSurface())
                Log.i(TAG, "addCameraStreams encoded stream$streamCount $stream")
                availableCameraStreams--
            } else {
                sharedStreamSurfaces.add(recorder.getRecorderSurface())
                Log.i(TAG, "addCameraStreams encoded stream$streamCount $stream")
            }
        }
        if (sharedStreamSurfaces.isNotEmpty()) {
            camBase.addSharedStream(sharedStreamSurfaces)
            availableCameraStreams--
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeCamera() = lifecycleScope.launch(Dispatchers.Main) {
        Log.i(TAG, "initializeCamera")
        cameraBaseLPM.openCamera(settings.cameraId)
        addCameraStreams(cameraBaseLPM, settings)
        cameraBaseLPM.setZSL(settings.cameraParams.hal_zsl_enable)
        cameraBaseLPM.startLPMSession()
        val sound = MediaActionSound()
        if (settings.snapshotOn) {
            captureButton.setOnClickListener {
                Log.i(TAG, "capture_button pressed")
                if (settings.mjpegOn) {
                    if (recordingMJPEG) {
                        if (SystemClock.elapsedRealtime() - chronometer.base > MIN_REQUIRED_RECORDING_TIME_MILLIS) {
                            cameraBaseLPM.takeMJPEG(false)
                            captureButton.background = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_shutter)
                            stopChronometer()
                            sound.play(MediaActionSound.SHUTTER_CLICK)
                            recordingMJPEG = false
                            Log.i(TAG, "recordingMJPEG stopped")
                        } else {
                            Log.d(TAG, "Cannot record mjpeg less than $MIN_REQUIRED_RECORDING_TIME_MILLIS ms")
                        }
                    } else if (CameraActivity.enoughStorageAvailable()) {
                        Log.i(TAG, "recordingMJPEG started")
                        recordingMJPEG = true
                        sound.play(MediaActionSound.SHUTTER_CLICK)
                        captureButton.background = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_shutter_mjpeg)
                        cameraBaseLPM.takeMJPEG(true)
                        startChronometer()
                    }
                } else if (CameraActivity.enoughStorageAvailable()) {
                    sound.play(MediaActionSound.SHUTTER_CLICK)
                    it.isEnabled = false
                    Log.i(TAG, "capture_button disabled")
                    lifecycleScope.launch(Dispatchers.IO) {
                        cameraBaseLPM.takeSnapshot(relativeOrientation.value).use { result ->
                            Log.d(TAG, "Result received: $result")
                            val outputFilePath = cameraBaseLPM.saveResult(result)
                            if (outputFilePath?.substring(outputFilePath.lastIndexOf(".")) == ".jpg") {
                                val exif = ExifInterface(outputFilePath)
                                exif.setAttribute(ExifInterface.TAG_ORIENTATION, result.orientation.toString())
                                exif.saveAttributes()
                                Log.d(TAG, "EXIF metadata saved: $outputFilePath")
                            }
                        }
                        it.post {
                            if (settings.snapshotInfo.encoding == "JPEG") {
                                thumbnailButton.setImageDrawable(createRoundThumb(cameraBaseLPM.currentSnapshotFilePath, THUMBNAIL_TYPE_IMAGE))
                            }
                            it.isEnabled = true
                            Log.i(TAG, "capture_button enabled")
                        }
                    }
                }
            }
        }
        if (settings.recorderInfo.isNotEmpty()) {
            recorderButton.setOnClickListener {
                if (recording) {
                    if (SystemClock.elapsedRealtime() - chronometer.base > MIN_REQUIRED_RECORDING_TIME_MILLIS) {
                        Log.i(TAG, "stopRecording enter")
                        cameraBaseLPM.stopRecording()
                        sound.play(MediaActionSound.STOP_VIDEO_RECORDING)
                        recorderButton.setBackgroundResource(android.R.drawable.presence_video_online)
                        if (settings.recorderInfo[0].storageEnable) thumbnailButton.setImageDrawable(createRoundThumb(cameraBaseLPM.getCurrentVideoFilePathList()[0], THUMBNAIL_TYPE_VIDEO))
                        recording = false
                        stopChronometer()
                        Log.i(TAG, "stopRecording exit")
                    } else {
                        Log.d(TAG, "Cannot record a video less than $MIN_REQUIRED_RECORDING_TIME_MILLIS ms")
                    }
                } else {
                    if (CameraActivity.enoughStorageAvailable()) {
                        Log.i(TAG, "startRecording enter")
                        sound.play(MediaActionSound.START_VIDEO_RECORDING)
                        cameraBaseLPM.startRecording(relativeOrientation.value)
                        recorderButton.setBackgroundResource(android.R.drawable.presence_video_busy)
                        startChronometer()
                        recording = true
                        Log.i(TAG, "startRecording exit")
                    }
                }
            }
        }
        thumbnailButton.setOnClickListener {
            Log.d(TAG, "Thumbnail icon pressed")
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                type = "image/* video/*"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        Log.i(TAG, "onResume")
        super.onResume()
        createRoundThumb(CameraActivity.thumbnailPath, CameraActivity.thumbnailType)?.let {
            thumbnailButton.setImageDrawable(it)
        } ?: run {
            thumbnailButton.setImageDrawable(resources.getDrawable(R.drawable.ic_camera_thumbnail))
        }
    }

    override fun onPause() {
        Log.i(TAG, "onPause")
        if (recordingMJPEG) {
            cameraBaseLPM.takeMJPEG(false)
            captureButton.background = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_shutter)
            stopChronometer()
            recordingMJPEG = false
            Log.i(TAG, "recordingMJPEG stopped")
        }
        if (recording) {
            Log.i(TAG, "stopRecording enter")
            cameraBaseLPM.stopRecording()
            recorderButton.setBackgroundResource(android.R.drawable.presence_video_online)
            if (settings.recorderInfo[0].storageEnable) thumbnailButton.setImageDrawable(createRoundThumb(cameraBaseLPM.getCurrentVideoFilePathList()[0], THUMBNAIL_TYPE_VIDEO))
            recording = false
            stopChronometer()
            Log.i(TAG, "stopRecording exit")
        }
        try {
            openCameraButton.text = "Open Camera"
            startSessionButton.visibility = View.GONE
            startPreviewButton.visibility = View.GONE
            cameraBaseLPM.closeCamera()
        } catch (exc: Throwable) {
            Log.e(TAG, "Error closing camera", exc)
        }
        super.onPause()
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onIsCameraReadyUpdated(oldIsCameraReady: Boolean, newIsCameraReady: Boolean) {
        Log.i(TAG, "onIsCameraReadyUpdated $oldIsCameraReady to $newIsCameraReady")
        (CameraActivity.mActivity?.get() as CameraActivity).enableTabs()
    }

    companion object {
        const val THUMBNAIL_TYPE_IMAGE = 1
        const val THUMBNAIL_TYPE_VIDEO = 2
        private val TAG = CameraFragmentLPM::class.java.simpleName
        var recording = false
        var recordingMJPEG = false
        const val MAX_CAMERA_STREAMS = 3
    }
}
