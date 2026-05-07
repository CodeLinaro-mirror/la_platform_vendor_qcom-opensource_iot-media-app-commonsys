/*
# Copyright (c) 2021 The Linux Foundation. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above
#       copyright notice, this list of conditions and the following
#       disclaimer in the documentation and/or other materials provided
#       with the distribution.
#     * Neither the name of The Linux Foundation nor the names of its
#       contributors may be used to endorse or promote products derived
#       from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
# ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
# BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
# BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
# WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
# OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
# IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# ​​​​​Changes from Qualcomm Technologies, Inc. are provided under the following license: 
# Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause-Clear
*/

package org.codeaurora.qmedia.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import org.codeaurora.qmedia.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";
    private PreferenceScreen mPrefScreen;
    private static final CameraCharacteristics.Key<String> CAMERA_TYPE_CHARACTERISTIC_KEY =
            new CameraCharacteristics.Key<>("camera.type", String.class);
    private ArrayList<String> externalCameras = new ArrayList<>();
    private Context mContext;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mContext = requireContext();
        CameraManager manager =
                (CameraManager) requireContext().getSystemService(Context.CAMERA_SERVICE);
        manager.registerAvailabilityCallback(mAvailabilityCallback, new Handler());

        setPreferencesFromResource(R.xml.setting_preference, rootKey);
        mPrefScreen = this.getPreferenceScreen();

        try {
            EditTextPreference version_info = mPrefScreen.findPreference("version_info");
            version_info.setSummary(getActivity().getApplicationContext().getPackageManager().
                    getPackageInfo(getActivity().getApplicationContext().getPackageName(),
                            0).versionName);
            version_info.setEnabled(false);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // Populate camera IDs
        populateCameraIDs();

        setupCameraConcurrencyListeners();
        initializeCameraConcurrencyDefaults();

        // Update Preference
        updatePreference();
    }

    private void initializeCameraConcurrencyDefaults() {
        SharedPreferences prefs = mPrefScreen.getSharedPreferences();

        if (!prefs.contains("camera_concurrency_mode")) {
            prefs.edit().putString("camera_concurrency_mode", "logical").apply();
        }

        if (!prefs.contains("cc_logical_camera_id")) {
            prefs.edit().putString("cc_logical_camera_id", "0").apply();
        }

        updateCameraConcurrencyPreferences();

        CameraManager cameraManager =
                (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        updateResolutionOptionsForSelectedCameras(cameraManager);
    }

    private void setupCameraConcurrencyListeners() {
        SwitchPreference ccEnable = mPrefScreen.findPreference("camera_concurrency_enable");
        if (ccEnable != null) {
            ccEnable.setOnPreferenceChangeListener((preference, newValue) -> {
                SharedPreferences prefs = mPrefScreen.getSharedPreferences();
                prefs.edit().putBoolean("camera_concurrency_enable", (Boolean) newValue).apply();

                updateCameraConcurrencyPreferences();

                if ((Boolean) newValue) {
                    CameraManager cameraManager =
                            (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                    updateResolutionOptionsForSelectedCameras(cameraManager);
                }

                return true;
            });
        }

        ListPreference ccMode = mPrefScreen.findPreference("camera_concurrency_mode");
        if (ccMode != null) {
            ccMode.setOnPreferenceChangeListener((preference, newValue) -> {
                SharedPreferences prefs = mPrefScreen.getSharedPreferences();
                prefs.edit().putString("camera_concurrency_mode", (String) newValue).apply();

                updateCameraConcurrencyPreferences();

                CameraManager cameraManager =
                        (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                updateResolutionOptionsForSelectedCameras(cameraManager);

                return true;
            });
        }

        ListPreference logicalCameraId = mPrefScreen.findPreference("cc_logical_camera_id");
        if (logicalCameraId != null) {
            logicalCameraId.setOnPreferenceChangeListener((preference, newValue) -> {
                SharedPreferences prefs = mPrefScreen.getSharedPreferences();
                prefs.edit().putString("cc_logical_camera_id", (String) newValue).apply();

                prefs.edit().putStringSet("cc_physical_camera_selection", new HashSet<>()).apply();

                MultiSelectListPreference physicalCameraSelection =
                        mPrefScreen.findPreference("cc_physical_camera_selection");
                if (physicalCameraSelection != null) {
                    physicalCameraSelection.setValues(new HashSet<>());
                }

                prefs.edit().remove("cc_camera1_resolution").apply();
                prefs.edit().remove("cc_camera2_resolution").apply();
                prefs.edit().remove("cc_camera3_resolution").apply();
                prefs.edit().remove("cc_camera4_resolution").apply();

                ListPreference cam1Res = mPrefScreen.findPreference("cc_camera1_resolution");
                ListPreference cam2Res = mPrefScreen.findPreference("cc_camera2_resolution");
                ListPreference cam3Res = mPrefScreen.findPreference("cc_camera3_resolution");
                ListPreference cam4Res = mPrefScreen.findPreference("cc_camera4_resolution");
                if (cam1Res != null) cam1Res.setTitle("Camera 1 Resolution");
                if (cam2Res != null) cam2Res.setTitle("Camera 2 Resolution");
                if (cam3Res != null) cam3Res.setTitle("Camera 3 Resolution");
                if (cam4Res != null) cam4Res.setTitle("Camera 4 Resolution");

                updateCameraConcurrencyPreferences();

                CameraManager cameraManager =
                        (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                updatePhysicalCameraListForLogicalCamera(cameraManager, (String) newValue);

                Log.i(TAG, "Logical camera changed to: " + newValue +
                        ", cleared selections and updated UI");
                return true;
            });
        }

        MultiSelectListPreference physicalCameraSelection =
                mPrefScreen.findPreference("cc_physical_camera_selection");
        if (physicalCameraSelection != null) {
            physicalCameraSelection.setOnPreferenceChangeListener((preference, newValue) -> {
                SharedPreferences prefs = mPrefScreen.getSharedPreferences();
                prefs.edit().putStringSet("cc_physical_camera_selection",
                        (Set<String>) newValue).apply();

                CameraManager cameraManager =
                        (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                updateResolutionOptionsForSelectedCameras(cameraManager);

                updateCameraConcurrencyPreferences();
                return true;
            });
        }

        MultiSelectListPreference independentCameraSelection =
                mPrefScreen.findPreference("cc_independent_camera_selection");
        if (independentCameraSelection != null) {
            independentCameraSelection.setOnPreferenceChangeListener((preference, newValue) -> {
                SharedPreferences prefs = mPrefScreen.getSharedPreferences();
                prefs.edit().putStringSet("cc_independent_camera_selection",
                        (Set<String>) newValue).apply();

                CameraManager cameraManager =
                        (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                updateResolutionOptionsForSelectedCameras(cameraManager);

                updateCameraConcurrencyPreferences();
                return true;
            });
        }
    }


    private final CameraManager.AvailabilityCallback mAvailabilityCallback = new CameraManager.AvailabilityCallback() {
        @Override
        public void onCameraAvailable(@NonNull String cameraId) {
            super.onCameraAvailable(cameraId);
            CameraManager manager =
                    (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            try {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                String cameraType = characteristics.get(CAMERA_TYPE_CHARACTERISTIC_KEY);
                if (cameraType != null && cameraType.equals("screen_share_internal")) {
                    Log.i(TAG, "External Camera available is " + cameraId);
                    externalCameras.add(cameraId);
                    populateCameraIDs();
                    updatePreference();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int navHeight = getResources().getDimensionPixelSize(getResources().
                getIdentifier("navigation_bar_height", "dimen", "android"));
        if (navHeight > 0) {
            view.setPadding(0, 0, 0, navHeight);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPrefScreen.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPrefScreen.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference();
    }

    void updatePreference() {
        ListPreference hdmi_source = mPrefScreen.findPreference("hdmi_1_source");
        ListPreference decoder_instance = mPrefScreen.findPreference("hdmi_1_decoder_instance");
        ListPreference compose_view = mPrefScreen.findPreference("hdmi_1_compose_view");
        ListPreference camera_id = mPrefScreen.findPreference("hdmi_1_camera_id");
        ListPreference camera_size = mPrefScreen.findPreference("hdmi_1_camera_size");
        ListPreference snpe_runtime = mPrefScreen.findPreference("hdmi_1_snpe_runtime");
        SwitchPreference hdmiin_audio_enable = mPrefScreen.findPreference("hdmi_1_hdmi_in_audio_enable");
        SwitchPreference hdmiin_video_enable = mPrefScreen.findPreference("hdmi_1_hdmi_in_video_enable");
        SwitchPreference reproc_enable = mPrefScreen.findPreference("hdmi_1_reproc_enable");
        ListPreference reproc_size = mPrefScreen.findPreference("hdmi_1_reproc_size");
        SwitchPreference recorder_enable = mPrefScreen.findPreference("hdmi_1_recorder_enable");
        SwitchPreference tunneling_enable = mPrefScreen.findPreference("hdmi_1_tunneling_enable");

        if (hdmi_source.getValue().equals("MP4")) {
            if (!decoder_instance.isVisible()) {
                decoder_instance.setVisible(true);
            }
            if (!compose_view.isVisible()) {
                compose_view.setVisible(true);
            }
            if (camera_id.isVisible()) {
                camera_id.setVisible(false);
            }
            if (camera_size.isVisible()) {
                camera_size.setVisible(false);
            }
            if (snpe_runtime.isVisible()) {
                snpe_runtime.setVisible(false);
            }
            if (hdmiin_audio_enable.isVisible()) {
                hdmiin_audio_enable.setVisible(false);
            }
            if (hdmiin_video_enable.isVisible()) {
                hdmiin_video_enable.setVisible(false);
            }
            if (reproc_enable.isVisible()) {
                reproc_enable.setVisible(false);
            }
            if (reproc_size.isVisible()) {
                reproc_size.setVisible(false);
            }
            if (recorder_enable.isVisible()) {
                recorder_enable.setVisible(false);
            }
            if (tunneling_enable.isVisible()) {
                tunneling_enable.setVisible(false);
            }
        } else if (hdmi_source.getValue().equals("Camera")) {
            if (decoder_instance.isVisible()) {
                decoder_instance.setVisible(false);
            }
            if (compose_view.isVisible()) {
                compose_view.setVisible(false);
            }
            if (!camera_id.isVisible()) {
                camera_id.setVisible(true);
            }
            if (snpe_runtime.isVisible()) {
                snpe_runtime.setVisible(false);
            }
            if (!tunneling_enable.isVisible()) {
                tunneling_enable.setVisible(true);
            }
            if (externalCameras.contains(camera_id.getValue())) {
                hdmiin_audio_enable.setVisible(true);
                hdmiin_video_enable.setVisible(true);
                reproc_enable.setVisible(false);
                reproc_size.setVisible(false);
                camera_size.setVisible(false);
                recorder_enable.setVisible(false);
            } else {
                hdmiin_audio_enable.setVisible(false);
                hdmiin_video_enable.setVisible(false);
                reproc_enable.setVisible(true);
                reproc_size.setVisible(true);
                camera_size.setVisible(true);
                recorder_enable.setVisible(true);
            }
        } else if (hdmi_source.getValue().equals("SNPE")) {
            if (decoder_instance.isVisible()) {
                decoder_instance.setVisible(false);
            }
            if (compose_view.isVisible()) {
                compose_view.setVisible(false);
            }
            if (!camera_id.isVisible()) {
                camera_id.setVisible(true);
            }
            if (!camera_size.isVisible()) {
                camera_size.setVisible(true);
            }
            if (!snpe_runtime.isVisible()) {
                snpe_runtime.setVisible(true);
            }
            if (hdmiin_audio_enable.isVisible()) {
                hdmiin_audio_enable.setVisible(false);
            }
            if (hdmiin_video_enable.isVisible()) {
                hdmiin_video_enable.setVisible(false);
            }
            if (reproc_enable.isVisible()) {
                reproc_enable.setVisible(false);
            }
            if (reproc_size.isVisible()) {
                reproc_size.setVisible(false);
            }
            if (tunneling_enable.isVisible()) {
                tunneling_enable.setVisible(false);
            }
        } else {
            decoder_instance.setVisible(false);
            compose_view.setVisible(false);
            camera_id.setVisible(false);
            camera_size.setVisible(false);
            hdmiin_audio_enable.setVisible(false);
            hdmiin_video_enable.setVisible(false);
            reproc_enable.setVisible(false);
            reproc_size.setVisible(false);
            snpe_runtime.setVisible(false);
            recorder_enable.setVisible(false);
            tunneling_enable.setVisible(false);
        }

        hdmi_source = mPrefScreen.findPreference("hdmi_2_source");
        decoder_instance = mPrefScreen.findPreference("hdmi_2_decoder_instance");
        compose_view = mPrefScreen.findPreference("hdmi_2_compose_view");
        camera_id = mPrefScreen.findPreference("hdmi_2_camera_id");
        camera_size = mPrefScreen.findPreference("hdmi_2_camera_size");
        snpe_runtime = mPrefScreen.findPreference("hdmi_2_snpe_runtime");
        hdmiin_audio_enable = mPrefScreen.findPreference("hdmi_2_hdmi_in_audio_enable");
        hdmiin_video_enable = mPrefScreen.findPreference("hdmi_2_hdmi_in_video_enable");
        reproc_enable = mPrefScreen.findPreference("hdmi_2_reproc_enable");
        reproc_size = mPrefScreen.findPreference("hdmi_2_reproc_size");
        recorder_enable = mPrefScreen.findPreference("hdmi_2_recorder_enable");
        tunneling_enable = mPrefScreen.findPreference("hdmi_2_tunneling_enable");

        if (hdmi_source.getValue().equals("MP4")) {
            if (!decoder_instance.isVisible()) {
                decoder_instance.setVisible(true);
            }
            if (!compose_view.isVisible()) {
                compose_view.setVisible(true);
            }
            if (camera_id.isVisible()) {
                camera_id.setVisible(false);
            }
            if (camera_size.isVisible()) {
                camera_size.setVisible(false);
            }
            if (snpe_runtime.isVisible()) {
                snpe_runtime.setVisible(false);
            }
            if (hdmiin_audio_enable.isVisible()) {
                hdmiin_audio_enable.setVisible(false);
            }
            if (hdmiin_video_enable.isVisible()) {
                hdmiin_video_enable.setVisible(false);
            }
            if (reproc_enable.isVisible()) {
                reproc_enable.setVisible(false);
            }
            if (reproc_size.isVisible()) {
                reproc_size.setVisible(false);
            }
            if (recorder_enable.isVisible()) {
                recorder_enable.setVisible(false);
            }
            if (tunneling_enable.isVisible()) {
                tunneling_enable.setVisible(false);
            }
        } else if (hdmi_source.getValue().equals("Camera")) {
            if (decoder_instance.isVisible()) {
                decoder_instance.setVisible(false);
            }
            if (compose_view.isVisible()) {
                compose_view.setVisible(false);
            }
            if (!camera_id.isVisible()) {
                camera_id.setVisible(true);
            }
            if (snpe_runtime.isVisible()) {
                snpe_runtime.setVisible(false);
            }
            if (!tunneling_enable.isVisible()) {
                tunneling_enable.setVisible(true);
            }
            if (externalCameras.contains(camera_id.getValue())) {
                hdmiin_audio_enable.setVisible(true);
                hdmiin_video_enable.setVisible(true);
                reproc_enable.setVisible(false);
                reproc_size.setVisible(false);
                camera_size.setVisible(false);
                recorder_enable.setVisible(false);
            } else {
                hdmiin_audio_enable.setVisible(false);
                hdmiin_video_enable.setVisible(false);
                reproc_enable.setVisible(true);
                reproc_size.setVisible(true);
                camera_size.setVisible(true);
                recorder_enable.setVisible(true);
            }
        } else if (hdmi_source.getValue().equals("SNPE")) {
            if (decoder_instance.isVisible()) {
                decoder_instance.setVisible(false);
            }
            if (compose_view.isVisible()) {
                compose_view.setVisible(false);
            }
            if (!camera_id.isVisible()) {
                camera_id.setVisible(true);
            }
            if (!camera_size.isVisible()) {
                camera_size.setVisible(true);
            }
            if (!snpe_runtime.isVisible()) {
                snpe_runtime.setVisible(true);
            }
            if (hdmiin_audio_enable.isVisible()) {
                hdmiin_audio_enable.setVisible(false);
            }
            if (hdmiin_video_enable.isVisible()) {
                hdmiin_video_enable.setVisible(false);
            }
            if (reproc_enable.isVisible()) {
                reproc_enable.setVisible(false);
            }
            if (reproc_size.isVisible()) {
                reproc_size.setVisible(false);
            }
            if (tunneling_enable.isVisible()) {
                tunneling_enable.setVisible(false);
            }
        } else {
            decoder_instance.setVisible(false);
            compose_view.setVisible(false);
            camera_id.setVisible(false);
            hdmiin_audio_enable.setVisible(false);
            hdmiin_video_enable.setVisible(false);
            reproc_enable.setVisible(false);
            reproc_size.setVisible(false);
            camera_size.setVisible(false);
            snpe_runtime.setVisible(false);
            recorder_enable.setVisible(false);
            tunneling_enable.setVisible(false);
        }

        hdmi_source = mPrefScreen.findPreference("hdmi_3_source");
        decoder_instance = mPrefScreen.findPreference("hdmi_3_decoder_instance");
        compose_view = mPrefScreen.findPreference("hdmi_3_compose_view");
        camera_id = mPrefScreen.findPreference("hdmi_3_camera_id");
        camera_size = mPrefScreen.findPreference("hdmi_3_camera_size");
        snpe_runtime = mPrefScreen.findPreference("hdmi_3_snpe_runtime");
        hdmiin_audio_enable = mPrefScreen.findPreference("hdmi_3_hdmi_in_audio_enable");
        hdmiin_video_enable = mPrefScreen.findPreference("hdmi_3_hdmi_in_video_enable");
        reproc_enable = mPrefScreen.findPreference("hdmi_3_reproc_enable");
        reproc_size = mPrefScreen.findPreference("hdmi_3_reproc_size");
        recorder_enable = mPrefScreen.findPreference("hdmi_3_recorder_enable");
        tunneling_enable = mPrefScreen.findPreference("hdmi_3_tunneling_enable");

        if (hdmi_source.getValue().equals("MP4")) {
            if (!decoder_instance.isVisible()) {
                decoder_instance.setVisible(true);
            }
            if (!compose_view.isVisible()) {
                compose_view.setVisible(true);
            }
            if (camera_id.isVisible()) {
                camera_id.setVisible(false);
            }
            if (camera_size.isVisible()) {
                camera_size.setVisible(false);
            }
            if (snpe_runtime.isVisible()) {
                snpe_runtime.setVisible(false);
            }
            if (hdmiin_audio_enable.isVisible()) {
                hdmiin_audio_enable.setVisible(false);
            }
            if (hdmiin_video_enable.isVisible()) {
                hdmiin_video_enable.setVisible(false);
            }
            if (reproc_enable.isVisible()) {
                reproc_enable.setVisible(false);
            }
            if (reproc_size.isVisible()) {
                reproc_size.setVisible(false);
            }
            if (recorder_enable.isVisible()) {
                recorder_enable.setVisible(false);
            }
            if (tunneling_enable.isVisible()) {
                tunneling_enable.setVisible(false);
            }
        } else if (hdmi_source.getValue().equals("Camera")) {
            if (decoder_instance.isVisible()) {
                decoder_instance.setVisible(false);
            }
            if (compose_view.isVisible()) {
                compose_view.setVisible(false);
            }
            if (!camera_id.isVisible()) {
                camera_id.setVisible(true);
            }
            if (snpe_runtime.isVisible()) {
                snpe_runtime.setVisible(false);
            }
            if (!tunneling_enable.isVisible()) {
                tunneling_enable.setVisible(true);
            }
            if (externalCameras.contains(camera_id.getValue())) {
                hdmiin_audio_enable.setVisible(true);
                hdmiin_video_enable.setVisible(true);
                reproc_enable.setVisible(false);
                reproc_size.setVisible(false);
                camera_size.setVisible(false);
                recorder_enable.setVisible(false);
            } else {
                hdmiin_audio_enable.setVisible(false);
                hdmiin_video_enable.setVisible(false);
                reproc_enable.setVisible(true);
                reproc_size.setVisible(true);
                camera_size.setVisible(true);
                recorder_enable.setVisible(true);
            }
        } else if (hdmi_source.getValue().equals("SNPE")) {
            if (decoder_instance.isVisible()) {
                decoder_instance.setVisible(false);
            }
            if (compose_view.isVisible()) {
                compose_view.setVisible(false);
            }
            if (!camera_id.isVisible()) {
                camera_id.setVisible(true);
            }
            if (!camera_size.isVisible()) {
                camera_size.setVisible(true);
            }
            if (!snpe_runtime.isVisible()) {
                snpe_runtime.setVisible(true);
            }
            if (hdmiin_audio_enable.isVisible()) {
                hdmiin_audio_enable.setVisible(false);
            }
            if (hdmiin_video_enable.isVisible()) {
                hdmiin_video_enable.setVisible(false);
            }
            if (reproc_enable.isVisible()) {
                reproc_enable.setVisible(false);
            }
            if (reproc_size.isVisible()) {
                reproc_size.setVisible(false);
            }
            if (tunneling_enable.isVisible()) {
                tunneling_enable.setVisible(false);
            }
        } else {
            decoder_instance.setVisible(false);
            compose_view.setVisible(false);
            camera_id.setVisible(false);
            hdmiin_audio_enable.setVisible(false);
            hdmiin_video_enable.setVisible(false);
            reproc_enable.setVisible(false);
            camera_size.setVisible(false);
            reproc_size.setVisible(false);
            snpe_runtime.setVisible(false);
            recorder_enable.setVisible(false);
            tunneling_enable.setVisible(false);
        }

        // Handle Reset App Preference
        Preference button = mPrefScreen.findPreference("reset");
        button.setOnPreferenceClickListener(preference -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        restoreAppPreference();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Restore App Preference");
            builder.setMessage("Do you wish to continue.").
                    setPositiveButton("Yes", dialogClickListener).
                    setNegativeButton("No", dialogClickListener).
                    show();
            return true;
        });

    }

    String getLensOrientationString(int facing) {
        String out = "Unknown";
        switch (facing) {
            case CameraCharacteristics.LENS_FACING_BACK:
                out = "Back";
                break;
            case CameraCharacteristics.LENS_FACING_FRONT:
                out = "Front";
                break;
            case CameraCharacteristics.LENS_FACING_EXTERNAL:
                out = "External";
                break;
        }
        return out;
    }

    void populateCameraIDs() {
        CameraManager cameraManager =
                (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        ArrayList<String> detectedCameras = new ArrayList<>();
        ArrayList<String> cameraIDs = new ArrayList<>();
        try {
            for (String camID : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(camID);
                String cameraType = null;
                try {
                    cameraType = characteristics.get(CAMERA_TYPE_CHARACTERISTIC_KEY);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                if (cameraType != null && cameraType.equals("screen_share_internal")) {
                    detectedCameras.add("Content_Share(" + camID + ")");
                } else {
                    detectedCameras.add(getLensOrientationString(
                            cameraManager.getCameraCharacteristics(camID)
                                    .get(CameraCharacteristics.LENS_FACING)) + "(" + camID + ")");
                }
                cameraIDs.add(camID);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        ListPreference camera_id = mPrefScreen.findPreference("hdmi_1_camera_id");
        CharSequence[] cameras = detectedCameras.toArray(new CharSequence[detectedCameras.size()]);
        CharSequence[] cameraIds =
                cameraIDs.toArray(new CharSequence[cameraIDs.size()]);
        camera_id.setEntries(cameras);
        camera_id.setEntryValues(cameraIds);

        camera_id = mPrefScreen.findPreference("hdmi_2_camera_id");
        camera_id.setEntries(cameras);
        camera_id.setEntryValues(cameraIds);

        camera_id = mPrefScreen.findPreference("hdmi_3_camera_id");
        camera_id.setEntries(cameras);
        camera_id.setEntryValues(cameraIds);

        populateCameraConcurrencySettings(cameraManager, detectedCameras, cameraIDs);
    }

    private void populateCameraConcurrencySettings(CameraManager cameraManager,
                                                   ArrayList<String> detectedCameras,
                                                   ArrayList<String> cameraIDs) {
        try {
            ListPreference logicalCameraId = mPrefScreen.findPreference("cc_logical_camera_id");
            if (logicalCameraId != null) {
                ArrayList<String> logicalCameras = new ArrayList<>();
                ArrayList<String> logicalCameraIds = new ArrayList<>();

                for (String camID : cameraIDs) {
                    CameraCharacteristics characteristics =
                            cameraManager.getCameraCharacteristics(camID);
                    Set<String> physicalIds = characteristics.getPhysicalCameraIds();
                    int physicalCount = physicalIds.isEmpty() ? 1 : physicalIds.size();
                    logicalCameras.add("Logical Camera " + camID +
                            " (" + physicalCount + " physical)");
                    logicalCameraIds.add(camID);
                }

                if (!logicalCameras.isEmpty()) {
                    logicalCameraId.setEntries(logicalCameras.toArray(new CharSequence[0]));
                    logicalCameraId.setEntryValues(logicalCameraIds.toArray(new CharSequence[0]));
                }
            }

            MultiSelectListPreference physicalCameraSelection =
                    mPrefScreen.findPreference("cc_physical_camera_selection");
            if (physicalCameraSelection != null) {
                updatePhysicalCameraList(cameraManager);
            }

            MultiSelectListPreference independentCameraSelection =
                    mPrefScreen.findPreference("cc_independent_camera_selection");
            if (independentCameraSelection != null) {
                independentCameraSelection.setEntries(detectedCameras.toArray(new CharSequence[0]));
                independentCameraSelection.setEntryValues(cameraIDs.toArray(new CharSequence[0]));
            }

            updateCameraConcurrencyPreferences();

        } catch (CameraAccessException e) {
            Log.e(TAG, "Error populating camera concurrency settings", e);
        }
    }

    private void updatePhysicalCameraList(CameraManager cameraManager) {
        try {
            SharedPreferences prefs = mPrefScreen.getSharedPreferences();
            String logicalCameraId = prefs.getString("cc_logical_camera_id", "0");
            updatePhysicalCameraListForLogicalCamera(cameraManager, logicalCameraId);
        } catch (Exception e) {
            Log.e(TAG, "Error updating physical camera list", e);
        }
    }

    private void updatePhysicalCameraListForLogicalCamera(
            CameraManager cameraManager, String logicalCameraId) {
        try {
            CameraCharacteristics characteristics =
                    cameraManager.getCameraCharacteristics(logicalCameraId);
            Set<String> physicalIds = characteristics.getPhysicalCameraIds();

            MultiSelectListPreference physicalCameraSelection =
                    mPrefScreen.findPreference("cc_physical_camera_selection");

            if (physicalCameraSelection != null) {
                if (!physicalIds.isEmpty()) {
                    ArrayList<String> physicalCameraLabels = new ArrayList<>();
                    ArrayList<String> physicalCameraValues = new ArrayList<>();

                    for (String physId : physicalIds) {
                        physicalCameraLabels.add("Physical Camera " + physId);
                        physicalCameraValues.add(physId);
                    }

                    physicalCameraSelection.setEntries(
                            physicalCameraLabels.toArray(new CharSequence[0]));
                    physicalCameraSelection.setEntryValues(
                            physicalCameraValues.toArray(new CharSequence[0]));

                    Log.i(TAG, "Updated physical camera list for logical camera " +
                            logicalCameraId + ": " + physicalIds.size() + " physical cameras");
                } else {
                    ArrayList<String> physicalCameraLabels = new ArrayList<>();
                    ArrayList<String> physicalCameraValues = new ArrayList<>();

                    physicalCameraLabels.add("Physical Camera " + logicalCameraId);
                    physicalCameraValues.add(logicalCameraId);

                    physicalCameraSelection.setEntries(
                            physicalCameraLabels.toArray(new CharSequence[0]));
                    physicalCameraSelection.setEntryValues(
                            physicalCameraValues.toArray(new CharSequence[0]));

                    Log.i(TAG, "Updated physical camera list for logical camera " +
                            logicalCameraId + ": 1 physical camera (same as logical)");
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error updating physical camera list for logical camera " +
                    logicalCameraId, e);
        }
    }

    private void updateResolutionOptionsForSelectedCameras(CameraManager cameraManager) {
        try {
            SharedPreferences prefs = mPrefScreen.getSharedPreferences();
            String ccMode = prefs.getString("camera_concurrency_mode", "logical");

            ArrayList<String> selectedCameraIds = new ArrayList<>();

            if ("logical".equals(ccMode)) {
                Set<String> physicalIds = prefs.getStringSet("cc_physical_camera_selection",
                        new HashSet<>());
                selectedCameraIds.addAll(physicalIds);

                java.util.Collections.sort(selectedCameraIds);

                for (int i = 0; i < Math.min(selectedCameraIds.size(), 4); i++) {
                    String physicalCameraId = selectedCameraIds.get(i);
                    String resKey = "cc_camera" + (i + 1) + "_resolution";
                    updateResolutionPreferenceForPhysicalCamera(cameraManager,
                            physicalCameraId, resKey, physicalCameraId);
                }
            } else {
                Set<String> cameraIds = prefs.getStringSet("cc_independent_camera_selection",
                        new HashSet<>());
                selectedCameraIds.addAll(cameraIds);

                java.util.Collections.sort(selectedCameraIds);

                for (int i = 0; i < Math.min(selectedCameraIds.size(), 4); i++) {
                    String cameraId = selectedCameraIds.get(i);
                    String resKey = "cc_camera" + (i + 1) + "_resolution";
                    updateResolutionPreference(cameraManager, cameraId, resKey, i);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating resolution options", e);
        }
    }

    private void updateResolutionPreferenceForPhysicalCamera(CameraManager cameraManager,
                                                             String physicalCameraId, String prefKey,
                                                             String actualPhysicalCameraId) {
        try {
            ListPreference resPref = mPrefScreen.findPreference(prefKey);
            if (resPref == null) return;

            resPref.setTitle("Physical Camera " + physicalCameraId + " Resolution");

            SharedPreferences prefs = mPrefScreen.getSharedPreferences();
            String logicalCameraId = prefs.getString("cc_logical_camera_id", "0");

            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(
                    logicalCameraId);
            android.hardware.camera2.params.StreamConfigurationMap map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            if (map != null) {
                android.util.Size[] sizes = map.getOutputSizes(android.view.SurfaceHolder.class);
                if (sizes != null && sizes.length > 0) {
                    java.util.Arrays.sort(sizes, (s1, s2) -> {
                        long area1 = (long) s1.getWidth() * s1.getHeight();
                        long area2 = (long) s2.getWidth() * s2.getHeight();
                        return Long.compare(area2, area1);
                    });

                    java.util.LinkedHashSet<String> resolutionSet = new java.util.LinkedHashSet<>();
                    for (android.util.Size size : sizes) {
                        String resolution = size.getWidth() + "x" + size.getHeight();
                        resolutionSet.add(resolution);
                    }

                    String[] resolutions = resolutionSet.toArray(new String[0]);
                    resPref.setEntries(resolutions);
                    resPref.setEntryValues(resolutions);

                    String currentValue = resPref.getValue();
                    if (currentValue == null || !resolutionSet.contains(currentValue)) {
                        if (resolutions.length > 0) {
                            resPref.setValue(resolutions[0]);
                            Log.i(TAG, "Set default resolution to sensor max: " + resolutions[0] +
                                    " for " + prefKey);
                        }
                    } else {
                        Log.i(TAG, "Preserving existing resolution: " + currentValue + " for "
                                + prefKey);
                    }

                    Log.i(TAG, "Updated resolution options for " + prefKey + " (physical camera " +
                            physicalCameraId + "): " + resolutions.length + " resolutions available");
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error updating resolution preference for physical camera " +
                    physicalCameraId, e);
        }
    }

    private void updateResolutionPreference(CameraManager cameraManager,
                                            String cameraId, String prefKey, int cameraIndex) {
        try {
            ListPreference resPref = mPrefScreen.findPreference(prefKey);
            if (resPref == null) return;

            resPref.setTitle("Camera " + cameraId + " Resolution");

            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            android.hardware.camera2.params.StreamConfigurationMap map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            if (map != null) {
                android.util.Size[] sizes = map.getOutputSizes(android.view.SurfaceHolder.class);
                if (sizes != null && sizes.length > 0) {
                    java.util.Arrays.sort(sizes, (s1, s2) -> {
                        long area1 = (long) s1.getWidth() * s1.getHeight();
                        long area2 = (long) s2.getWidth() * s2.getHeight();
                        return Long.compare(area2, area1);
                    });

                    java.util.LinkedHashSet<String> resolutionSet = new java.util.LinkedHashSet<>();
                    for (android.util.Size size : sizes) {
                        String resolution = size.getWidth() + "x" + size.getHeight();
                        resolutionSet.add(resolution);
                    }

                    String[] resolutions = resolutionSet.toArray(new String[0]);
                    resPref.setEntries(resolutions);
                    resPref.setEntryValues(resolutions);

                    String currentValue = resPref.getValue();
                    if (currentValue == null || !resolutionSet.contains(currentValue)) {
                        if (resolutions.length > 0) {
                            resPref.setValue(resolutions[0]);
                        }
                    }

                    Log.i(TAG, "Updated resolution options for " + prefKey + " (camera " +
                            cameraId + "): " + resolutions.length + " resolutions available");
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error updating resolution preference for camera " + cameraId, e);
        }
    }

    private void updateCameraConcurrencyPreferences() {
        SharedPreferences prefs = mPrefScreen.getSharedPreferences();
        boolean ccEnabled = prefs.getBoolean("camera_concurrency_enable", false);
        String ccMode = prefs.getString("camera_concurrency_mode", "logical");

        ListPreference logicalCameraId = mPrefScreen.findPreference("cc_logical_camera_id");
        MultiSelectListPreference physicalCameraSelection =
                mPrefScreen.findPreference("cc_physical_camera_selection");
        MultiSelectListPreference independentCameraSelection =
                mPrefScreen.findPreference("cc_independent_camera_selection");

        ListPreference cam1Res = mPrefScreen.findPreference("cc_camera1_resolution");
        ListPreference cam2Res = mPrefScreen.findPreference("cc_camera2_resolution");
        ListPreference cam3Res = mPrefScreen.findPreference("cc_camera3_resolution");
        ListPreference cam4Res = mPrefScreen.findPreference("cc_camera4_resolution");

        if (!ccEnabled) {
            if (logicalCameraId != null) logicalCameraId.setVisible(false);
            if (physicalCameraSelection != null) physicalCameraSelection.setVisible(false);
            if (independentCameraSelection != null) independentCameraSelection.setVisible(false);
            if (cam1Res != null) cam1Res.setVisible(false);
            if (cam2Res != null) cam2Res.setVisible(false);
            if (cam3Res != null) cam3Res.setVisible(false);
            if (cam4Res != null) cam4Res.setVisible(false);
            return;
        }

        if ("logical".equals(ccMode)) {
            if (logicalCameraId != null) logicalCameraId.setVisible(true);
            if (physicalCameraSelection != null) physicalCameraSelection.setVisible(true);
            if (independentCameraSelection != null) independentCameraSelection.setVisible(false);

            Set<String> selectedPhysicalCameras =
                    prefs.getStringSet("cc_physical_camera_selection", new HashSet<>());
            int selectedCount = selectedPhysicalCameras.size();

            if (cam1Res != null) cam1Res.setVisible(selectedCount >= 1);
            if (cam2Res != null) cam2Res.setVisible(selectedCount >= 2);
            if (cam3Res != null) cam3Res.setVisible(selectedCount >= 3);
            if (cam4Res != null) cam4Res.setVisible(selectedCount >= 4);

        } else {
            if (logicalCameraId != null) logicalCameraId.setVisible(false);
            if (physicalCameraSelection != null) physicalCameraSelection.setVisible(false);
            if (independentCameraSelection != null) independentCameraSelection.setVisible(true);

            Set<String> selectedIndependentCameras =
                    prefs.getStringSet("cc_independent_camera_selection", new HashSet<>());
            int selectedCount = selectedIndependentCameras.size();

            if (cam1Res != null) cam1Res.setVisible(selectedCount >= 1);
            if (cam2Res != null) cam2Res.setVisible(selectedCount >= 2);
            if (cam3Res != null) cam3Res.setVisible(selectedCount >= 3);
            if (cam4Res != null) cam4Res.setVisible(selectedCount >= 4);
        }
    }

    private void restoreAppPreference() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        PreferenceManager.setDefaultValues(getActivity(), R.xml.setting_preference, true);
        getPreferenceScreen().removeAll();
        onCreatePreferences(null, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CameraManager manager =
                (CameraManager) requireContext().getSystemService(Context.CAMERA_SERVICE);
        manager.unregisterAvailabilityCallback(mAvailabilityCallback);
    }
}
