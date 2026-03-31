/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package org.codeaurora.qmedia.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import org.codeaurora.qmedia.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CameraConcurrencyFragment extends Fragment {

    private static final String TAG = "CameraConcurrencyFrag";
    private static final int MAX_STREAMS = 4;
    private Button mStartStopButton;
    private final SurfaceView[] mSurfaces =
            new SurfaceView[MAX_STREAMS];
    private final ViewGroup[] mSurfaceContainers =
            new ViewGroup[MAX_STREAMS];
    private CameraManager mCameraManager;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    // Physical camera mode
    private final List<String> mSelectedCameraIds = new ArrayList<>();
    private final CameraDevice[] mCameraDevices =
            new CameraDevice[MAX_STREAMS];
    private final CameraCaptureSession[] mCaptureSessions =
            new CameraCaptureSession[MAX_STREAMS];
    private final Size[] mConfiguredSizes = new Size[MAX_STREAMS];

    // Logical camera mode
    private String mLogicalCameraId;
    private final List<String> mSelectedPhysicalIds = new ArrayList<>();
    private CameraDevice mLogicalCameraDevice;
    private CameraCaptureSession mLogicalCaptureSession;

    private Context mContext;
    private boolean mIsRunning = false;
    private boolean mIsLogicalMode = true;
    private int mActualStreamCount = 0;
    private int mReadySurfaceCount = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        mContext = requireContext();
        return inflater.inflate(R.layout.camera_concurrency,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.v(TAG, "onViewCreated");

        initializeViews(view);
        initializeCameraManager();
        loadUserConfiguration();
        setupSurfaceViews();
        updateStreamCount();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        if (!mIsRunning) {
            loadUserConfiguration();
            updateStreamCount();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        if (mIsRunning) stopCameraStreams();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    private void initializeViews(View view) {
        // Find surface containers and surfaces
        View cam1 = view.findViewById(R.id.surface_camera1);
        mSurfaceContainers[0] = cam1.getParent() instanceof ViewGroup
                ? (ViewGroup) cam1.getParent() : null;

        View cam2 = view.findViewById(R.id.surface_camera2);
        mSurfaceContainers[1] = cam2.getParent() instanceof ViewGroup
                ? (ViewGroup) cam2.getParent() : null;

        View cam3 = view.findViewById(R.id.surface_camera3);
        mSurfaceContainers[2] = cam3.getParent() instanceof ViewGroup
                ? (ViewGroup) cam3.getParent() : null;

        View cam4 = view.findViewById(R.id.surface_camera4);
        mSurfaceContainers[3] = cam4.getParent() instanceof ViewGroup
                ? (ViewGroup) cam4.getParent() : null;

        mSurfaces[0] = view.findViewById(R.id.surface_camera1);
        mSurfaces[1] = view.findViewById(R.id.surface_camera2);
        mSurfaces[2] = view.findViewById(R.id.surface_camera3);
        mSurfaces[3] = view.findViewById(R.id.surface_camera4);

        mStartStopButton = view.findViewById(R.id.start_stop_button);
        mStartStopButton.setEnabled(false);
        mStartStopButton.setOnClickListener(
                v -> toggleCameraStreams());
    }

    private void initializeCameraManager() {
        mCameraManager = (CameraManager)
                mContext.getSystemService(Context.CAMERA_SERVICE);
    }

    private void loadUserConfiguration() {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        String mode = prefs.getString("camera_concurrency_mode",
                "logical");
        mIsLogicalMode = "logical".equals(mode);

        mSelectedCameraIds.clear();
        mSelectedPhysicalIds.clear();
        Arrays.fill(mConfiguredSizes, null);

        if (mIsLogicalMode) {
            // Logical camera mode
            mLogicalCameraId = prefs.getString(
                    "cc_logical_camera_id", "0");
            Set<String> physicalIds = prefs.getStringSet(
                    "cc_physical_camera_selection", new HashSet<>());
            mSelectedPhysicalIds.addAll(physicalIds);

            Collections.sort(mSelectedPhysicalIds);

            if (mSelectedPhysicalIds.size() > MAX_STREAMS) {
                mSelectedPhysicalIds.subList(MAX_STREAMS,
                        mSelectedPhysicalIds.size()).clear();
            }

            Log.i(TAG, "Logical mode: camera=" + mLogicalCameraId
                    + ", physical cameras=" + mSelectedPhysicalIds);

        } else {
            // Physical camera mode
            Set<String> cameraIds = prefs.getStringSet(
                    "cc_independent_camera_selection",
                    new HashSet<>());
            mSelectedCameraIds.addAll(cameraIds);

            Collections.sort(mSelectedCameraIds);

            if (mSelectedCameraIds.size() > MAX_STREAMS) {
                mSelectedCameraIds.subList(MAX_STREAMS,
                        mSelectedCameraIds.size()).clear();
            }

            Log.i(TAG, "Physical mode: cameras="
                    + mSelectedCameraIds);
        }

        // Load resolution configurations
        for (int i = 0; i < MAX_STREAMS; i++) {
            String resKey = "cc_camera" + (i + 1) + "_resolution";
            String resStr = prefs.getString(resKey, "1920x1080");
            mConfiguredSizes[i] = parseResolution(resStr);
            Log.d(TAG, "Camera " + (i + 1) + " resolution: "
                    + mConfiguredSizes[i]);
        }
    }

    private Size parseResolution(String resStr) {
        try {
            String[] parts = resStr.split("x");
            if (parts.length == 2) {
                return new Size(Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing resolution: " + resStr, e);
        }
        return new Size(1920, 1080); // Default
    }

    private void updateStreamCount() {
        mActualStreamCount = mIsLogicalMode
                ? mSelectedPhysicalIds.size()
                : mSelectedCameraIds.size();
        mActualStreamCount = Math.min(mActualStreamCount, MAX_STREAMS);

        Log.i(TAG, "Stream count: " + mActualStreamCount);

        mReadySurfaceCount = 0;
        mStartStopButton.setEnabled(false);

        // Show/hide surfaces based on stream count
        for (int i = 0; i < MAX_STREAMS; i++) {
            if (mSurfaces[i] != null) {
                int visibility = i < mActualStreamCount
                        ? View.VISIBLE : View.INVISIBLE;
                mSurfaces[i].setVisibility(visibility);
            }
        }

        // Count already valid surfaces
        for (int i = 0; i < mActualStreamCount; i++) {
            if (mSurfaces[i] != null
                    && mSurfaces[i].getHolder().getSurface() != null
                    && mSurfaces[i].getHolder().getSurface()
                    .isValid()) {
                mReadySurfaceCount++;
            }
        }

        if (mActualStreamCount > 0
                && mReadySurfaceCount >= mActualStreamCount) {
            Log.d(TAG, "Surfaces already valid – enabling Start");
            mStartStopButton.setEnabled(true);
        }
    }

    private void setupSurfaceViews() {
        for (int i = 0; i < MAX_STREAMS; i++) {
            setupSurfaceView(mSurfaces[i], i);
        }
    }

    private void setupSurfaceView(SurfaceView surfaceView,
                                  final int index) {
        surfaceView.getHolder().addCallback(
                new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(@NonNull SurfaceHolder holder) {
                        if (index >= mActualStreamCount) return;
                        Log.d(TAG, "Surface " + index + " created");
                        mReadySurfaceCount++;
                        if (mReadySurfaceCount >= mActualStreamCount
                                && mActualStreamCount > 0) {
                            Log.d(TAG, "All " + mActualStreamCount
                                    + " surfaces ready");
                            mStartStopButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void surfaceChanged(@NonNull SurfaceHolder holder,
                                               int format, int width,
                                               int height) {
                        Log.d(TAG, "Surface " + index + " changed: "
                                + width + "x" + height);
                    }

                    @Override
                    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                        Log.d(TAG, "Surface " + index + " destroyed");
                        if (index < mActualStreamCount) {
                            mReadySurfaceCount = Math.max(0,
                                    mReadySurfaceCount - 1);
                        }
                    }
                });
    }

    private void toggleCameraStreams() {
        if (mIsRunning) stopCameraStreams();
        else startCameraStreams();
    }

    private void startCameraStreams() {
        String mode = mIsLogicalMode ? "LOGICAL" : "PHYSICAL";
        Log.v(TAG, "startCameraStreams – mode=" + mode
                + ", streams=" + mActualStreamCount);

        if (mActualStreamCount == 0) {
            Toast.makeText(mContext,
                    "No cameras selected in settings",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (mReadySurfaceCount < mActualStreamCount) {
            Toast.makeText(mContext, "Surfaces not ready yet",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        startBackgroundThread();

        if (mIsLogicalMode) startLogicalCameraMode();
        else startPhysicalCamerasMode();
    }

    private void stopCameraStreams() {
        Log.v(TAG, "stopCameraStreams");

        if (mIsLogicalMode) stopLogicalCameraMode();
        else stopPhysicalCamerasMode();

        stopBackgroundThread();
        mIsRunning = false;
        mStartStopButton.setText("Start");
    }

    private void startPhysicalCamerasMode() {
        for (int i = 0; i < MAX_STREAMS; i++) {
            mCameraDevices[i] = null;
            mCaptureSessions[i] = null;
        }
        for (int i = 0; i < mActualStreamCount; i++) {
            openPhysicalCamera(i);
        }
    }

    private void openPhysicalCamera(final int index) {
        String cameraId = mSelectedCameraIds.get(index);
        try {
            Log.d(TAG, "Opening camera " + index
                    + " (id=\"" + cameraId + "\")");
            mCameraManager.openCamera(cameraId,
                    new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(@NonNull CameraDevice camera) {
                            Log.d(TAG, "Camera " + index + " opened");
                            mCameraDevices[index] = camera;
                            createPhysicalCameraSession(index);
                        }

                        @Override
                        public void onDisconnected(
                                @NonNull CameraDevice camera) {
                            Log.w(TAG, "Camera " + index + " disconnected");
                            camera.close();
                            mCameraDevices[index] = null;
                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera,
                                            int error) {
                            Log.e(TAG, "Camera " + index + " error: "
                                    + error);
                            camera.close();
                            mCameraDevices[index] = null;
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(mContext,
                                            "Camera " + (index + 1)
                                                    + " error: " + error,
                                            Toast.LENGTH_SHORT).show());
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException | SecurityException e) {
            Log.e(TAG, "Error opening camera " + index, e);
        }
    }

    private void createPhysicalCameraSession(final int index) {
        CameraDevice camera = mCameraDevices[index];
        if (camera == null || mSurfaces[index] == null) return;

        try {
            Surface surface = mSurfaces[index].getHolder()
                    .getSurface();

            // Use configured resolution (validated in settings)
            final Size finalSize = mConfiguredSizes[index];
            Log.d(TAG, "Camera " + index + " using size: "
                    + finalSize);

            // Set size and adjust layout to preserve aspect ratio
            requireActivity().runOnUiThread(() -> {
                setAspectRatioForSurface(index, finalSize);
                mSurfaces[index].getHolder().setFixedSize(
                        finalSize.getWidth(),
                        finalSize.getHeight());
            });

            OutputConfiguration outConfig =
                    new OutputConfiguration(surface);

            camera.createCaptureSession(new SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    List.of(outConfig),
                    cmd -> mBackgroundHandler.post(cmd),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(
                                @NonNull CameraCaptureSession session) {
                            Log.d(TAG, "Session configured for camera "
                                    + index);
                            mCaptureSessions[index] = session;
                            startSingleCameraPreview(index, surface);
                            checkAllPhysicalSessionsReady();
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession session) {
                            Log.e(TAG, "Session config failed for "
                                    + "camera " + index);
                        }
                    }));
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error creating session for camera "
                    + index, e);
        }
    }

    private void startSingleCameraPreview(int index, Surface surface) {
        try {
            CameraDevice camera = mCameraDevices[index];
            CameraCaptureSession session = mCaptureSessions[index];
            if (camera == null || session == null) return;

            CaptureRequest.Builder builder =
                    camera.createCaptureRequest(
                            CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            session.setRepeatingRequest(builder.build(), null,
                    mBackgroundHandler);
            Log.d(TAG, "Preview started for camera " + index);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error starting preview for camera "
                    + index, e);
        }
    }

    private void checkAllPhysicalSessionsReady() {
        for (int i = 0; i < mActualStreamCount; i++) {
            if (mCaptureSessions[i] == null) return;
        }
        requireActivity().runOnUiThread(() -> {
            mIsRunning = true;
            mStartStopButton.setText("Stop");
            Log.d(TAG, "All " + mActualStreamCount
                    + " physical camera sessions active");
        });
    }

    private void stopPhysicalCamerasMode() {
        for (int i = 0; i < MAX_STREAMS; i++) {
            if (mCaptureSessions[i] != null) {
                mCaptureSessions[i].close();
                mCaptureSessions[i] = null;
            }
            if (mCameraDevices[i] != null) {
                mCameraDevices[i].close();
                mCameraDevices[i] = null;
            }
        }
    }

    private void startLogicalCameraMode() {
        if (mSelectedPhysicalIds.isEmpty()) {
            Toast.makeText(mContext,
                    "No physical cameras selected in settings",
                    Toast.LENGTH_LONG).show();
            stopBackgroundThread();
            return;
        }
        openLogicalCamera();
    }

    private void openLogicalCamera() {
        try {
            Log.d(TAG, "Opening logical camera \""
                    + mLogicalCameraId + "\"");
            mCameraManager.openCamera(mLogicalCameraId,
                    new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(@NonNull CameraDevice camera) {
                            Log.d(TAG, "Logical camera opened");
                            mLogicalCameraDevice = camera;
                            createLogicalCaptureSession();
                        }

                        @Override
                        public void onDisconnected(
                                @NonNull CameraDevice camera) {
                            Log.w(TAG, "Logical camera disconnected");
                            camera.close();
                            mLogicalCameraDevice = null;
                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera,
                                            int error) {
                            Log.e(TAG, "Logical camera error: " + error);
                            camera.close();
                            mLogicalCameraDevice = null;
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(mContext,
                                            "Camera error: " + error,
                                            Toast.LENGTH_SHORT).show());
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException | SecurityException e) {
            Log.e(TAG, "Error opening logical camera", e);
            Toast.makeText(mContext, "Error opening camera",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void createLogicalCaptureSession() {
        if (mLogicalCameraDevice == null) return;

        try {
            // Check if logical camera has physical sub-cameras
            CameraCharacteristics characteristics =
                    mCameraManager.getCameraCharacteristics(
                            mLogicalCameraId);
            Set<String> availablePhysicalIds =
                    characteristics.getPhysicalCameraIds();

            boolean usePhysicalCameraIds =
                    !availablePhysicalIds.isEmpty()
                            && availablePhysicalIds.size() > 1;

            Log.i(TAG, "Logical camera " + mLogicalCameraId
                    + " has " + availablePhysicalIds.size()
                    + " physical cameras. "
                    + "Using physical camera IDs: "
                    + usePhysicalCameraIds);

            List<OutputConfiguration> outputConfigs =
                    new ArrayList<>();

            for (int i = 0; i < mActualStreamCount; i++) {
                String physicalId = mSelectedPhysicalIds.get(i);
                Surface surface = mSurfaces[i].getHolder()
                        .getSurface();

                // Validate physical camera ID
                if (usePhysicalCameraIds
                        && !availablePhysicalIds.contains(
                        physicalId)) {
                    Log.e(TAG, "Physical camera " + physicalId
                            + " not available in logical camera "
                            + mLogicalCameraId + ". Available: "
                            + availablePhysicalIds);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(mContext,
                                    "Physical camera " + physicalId
                                            + " not available",
                                    Toast.LENGTH_SHORT).show());
                    return;
                }

                // Use configured resolution (validated in settings)
                final int surfaceIndex = i;
                final Size finalSize = mConfiguredSizes[i];
                Log.d(TAG, "Physical camera " + physicalId
                        + " using size: " + finalSize);

                // Set size and adjust layout to preserve aspect
                requireActivity().runOnUiThread(() -> {
                    setAspectRatioForSurface(surfaceIndex,
                            finalSize);
                    mSurfaces[surfaceIndex].getHolder().setFixedSize(
                            finalSize.getWidth(),
                            finalSize.getHeight());
                });

                OutputConfiguration config =
                        new OutputConfiguration(surface);

                // Set physical camera ID if supported
                if (usePhysicalCameraIds) {
                    config.setPhysicalCameraId(physicalId);
                    Log.d(TAG, "Stream " + i
                            + " → physical camera \""
                            + physicalId + "\" @ "
                            + mConfiguredSizes[i]);
                } else {
                    Log.d(TAG, "Stream " + i
                            + " → logical camera (no physical ID) @ "
                            + mConfiguredSizes[i]);
                }

                outputConfigs.add(config);
            }

            final SurfaceView[] activeSurfaces =
                    new SurfaceView[mActualStreamCount];
            System.arraycopy(mSurfaces, 0, activeSurfaces, 0,
                    mActualStreamCount);

            mLogicalCameraDevice.createCaptureSession(
                    new SessionConfiguration(
                            SessionConfiguration.SESSION_REGULAR,
                            outputConfigs,
                            cmd -> mBackgroundHandler.post(cmd),
                            new CameraCaptureSession.StateCallback() {
                                @Override
                                public void onConfigured(
                                        @NonNull CameraCaptureSession session) {
                                    Log.d(TAG,
                                            "Logical capture session configured");
                                    mLogicalCaptureSession = session;
                                    startLogicalPreview(activeSurfaces);
                                    requireActivity().runOnUiThread(() -> {
                                        mIsRunning = true;
                                        mStartStopButton.setText("Stop");
                                    });
                                }

                                @Override
                                public void onConfigureFailed(
                                        @NonNull CameraCaptureSession session) {
                                    Log.e(TAG,
                                            "Logical capture session config failed");
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(mContext,
                                                    "Session configuration failed",
                                                    Toast.LENGTH_SHORT).show());
                                }
                            }));
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error creating logical capture session", e);
        }
    }

    private void startLogicalPreview(SurfaceView[] activeSurfaces) {
        try {
            if (mLogicalCameraDevice == null
                    || mLogicalCaptureSession == null) return;

            CaptureRequest.Builder builder =
                    mLogicalCameraDevice.createCaptureRequest(
                            CameraDevice.TEMPLATE_PREVIEW);

            for (SurfaceView sv : activeSurfaces) {
                builder.addTarget(sv.getHolder().getSurface());
            }

            mLogicalCaptureSession.setRepeatingRequest(
                    builder.build(), null, mBackgroundHandler);

            Log.d(TAG, "Logical preview started on "
                    + activeSurfaces.length + " stream(s)");
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error starting logical preview", e);
        }
    }

    private void stopLogicalCameraMode() {
        if (mLogicalCaptureSession != null) {
            mLogicalCaptureSession.close();
            mLogicalCaptureSession = null;
        }
        if (mLogicalCameraDevice != null) {
            mLogicalCameraDevice.close();
            mLogicalCameraDevice = null;
        }
    }

    private void setAspectRatioForSurface(int index, Size streamSize) {
        if (mSurfaces[index] == null
                || mSurfaceContainers[index] == null
                || streamSize == null) {
            return;
        }

        // Get the container's dimensions
        int containerWidth = mSurfaceContainers[index].getWidth();
        int containerHeight = mSurfaceContainers[index].getHeight();

        if (containerWidth == 0 || containerHeight == 0) {
            Log.w(TAG, "Container dimensions not available yet "
                    + "for surface " + index);
            return;
        }

        // Calculate aspect ratios
        float streamAspect = (float) streamSize.getWidth()
                / streamSize.getHeight();
        float containerAspect = (float) containerWidth
                / containerHeight;

        int surfaceWidth, surfaceHeight;

        if (containerAspect > streamAspect) {
            // Container is wider - fit to height
            surfaceHeight = containerHeight;
            surfaceWidth = (int) (containerHeight * streamAspect);
        } else {
            // Container is taller - fit to width
            surfaceWidth = containerWidth;
            surfaceHeight = (int) (containerWidth / streamAspect);
        }

        // Update the SurfaceView's layout params
        ViewGroup.LayoutParams params =
                mSurfaces[index].getLayoutParams();
        params.width = surfaceWidth;
        params.height = surfaceHeight;
        mSurfaces[index].setLayoutParams(params);

        Log.d(TAG, "Set aspect ratio for surface " + index + ": "
                + surfaceWidth + "x" + surfaceHeight
                + " (stream: " + streamSize + ", container: "
                + containerWidth + "x" + containerHeight + ")");
    }

    private void startBackgroundThread() {
        mBackgroundThread =
                new HandlerThread("CameraConcurrencyBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(
                mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping background thread", e);
            }
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
    }
}
