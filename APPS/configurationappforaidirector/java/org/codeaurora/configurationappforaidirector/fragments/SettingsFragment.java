/*
# Copyright (c) 2023 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: BSD-3-Clause-Clear
*/

package org.codeaurora.configurationappforaidirector.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import org.codeaurora.configurationappforaidirector.R;

import java.lang.reflect.Method;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private PreferenceScreen mPrefScreen;
    private Context mContext;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mContext = requireContext();
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        mPrefScreen = this.getPreferenceScreen();
        updatePreference();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        updatePreference();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void updatePreference() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        ListPreference framing_mode = mPrefScreen.findPreference("framing_mode");
        ListPreference gpu_transform_draw_debug = mPrefScreen.findPreference("gpu_transform_draw_debug");
        ListPreference gpu_transform_enable_crop = mPrefScreen.findPreference("gpu_transform_enable_crop");
        ListPreference csv_logging_enable = mPrefScreen.findPreference("csv_logging_enable");
        EditTextPreference beamform_log_filename = mPrefScreen.findPreference("beamform_log_filename");
        EditTextPreference filter_log_filename = mPrefScreen.findPreference("filter_log_filename");
        EditTextPreference histogram_log_filename = mPrefScreen.findPreference("histogram_log_filename");
        EditTextPreference device_log_filename = mPrefScreen.findPreference("device_log_filename");
        ListPreference audio_plot = mPrefScreen.findPreference("audio_plot");
        EditTextPreference autoframing_filter_size = mPrefScreen.findPreference("autoframing_filter_size");
        EditTextPreference autoframing_filter_average_size = mPrefScreen.findPreference("autoframing_filter_average_size");
        EditTextPreference autoframing_pos_static_threshold = mPrefScreen.findPreference("autoframing_pos_static_threshold");
        EditTextPreference autoframing_size_static_threshold = mPrefScreen.findPreference("autoframing_size_static_threshold");
        EditTextPreference autoframing_pos_moving_threshold = mPrefScreen.findPreference("autoframing_pos_moving_threshold");
        EditTextPreference autoframing_size_moving_threshold = mPrefScreen.findPreference("autoframing_size_moving_threshold");
        EditTextPreference autoframing_speed_movement = mPrefScreen.findPreference("autoframing_speed_movement");
        EditTextPreference autoframing_max_move_step = mPrefScreen.findPreference("autoframing_max_move_step");
        EditTextPreference autoframing_max_crop_ratio = mPrefScreen.findPreference("autoframing_max_crop_ratio");
        EditTextPreference autoframing_roi_release_cnt = mPrefScreen.findPreference("autoframing_roi_release_cnt");
        EditTextPreference adoa_sound_card = mPrefScreen.findPreference("adoa_sound_card");
        EditTextPreference adoa_backend_interface = mPrefScreen.findPreference("adoa_backend_interface");
        EditTextPreference adoa_mixer_control_stt = mPrefScreen.findPreference("adoa_mixer_control_stt");
        EditTextPreference adoa_mixer_control_fnn = mPrefScreen.findPreference("adoa_mixer_control_fnn");
        EditTextPreference adoa_mixer_open_retry_delay = mPrefScreen.findPreference("adoa_mixer_open_retry_delay");
        EditTextPreference adoa_mixer_open_max_retries = mPrefScreen.findPreference("adoa_mixer_open_max_retries");
        ListPreference filter_select = mPrefScreen.findPreference("filter_select");
        EditTextPreference frame_rate = mPrefScreen.findPreference("frame_rate");
        EditTextPreference camera_fov = mPrefScreen.findPreference("camera_fov");
        EditTextPreference device_sampling_period = mPrefScreen.findPreference("device_sampling_period");
        EditTextPreference device_sample_size = mPrefScreen.findPreference("device_sample_size");
        EditTextPreference device_sample_threshold = mPrefScreen.findPreference("device_sample_threshold");
        EditTextPreference device_thread_timeout = mPrefScreen.findPreference("device_thread_timeout");
        EditTextPreference device_mic_orientation = mPrefScreen.findPreference("device_mic_orientation");
        ListPreference device_mic_flip = mPrefScreen.findPreference("device_mic_flip");
        EditTextPreference device_mic_resolution = mPrefScreen.findPreference("device_mic_resolution");
        EditTextPreference filter_speech_probability_threshold = mPrefScreen.findPreference("filter_speech_probability_threshold");
        EditTextPreference filter_single_activation_threshold = mPrefScreen.findPreference("filter_single_activation_threshold");
        EditTextPreference filter_single_activation_deadline = mPrefScreen.findPreference("filter_single_activation_deadline");
        EditTextPreference filter_single_deactivation_delay = mPrefScreen.findPreference("filter_single_deactivation_delay");
        EditTextPreference filter_multi_activation_threshold = mPrefScreen.findPreference("filter_multi_activation_threshold");
        EditTextPreference filter_multi_activation_deadline = mPrefScreen.findPreference("filter_multi_activation_deadline");
        EditTextPreference filter_multi_deactivation_delay = mPrefScreen.findPreference("filter_multi_deactivation_delay");
        EditTextPreference filter_multi_max_speakers = mPrefScreen.findPreference("filter_multi_max_speakers");
        ListPreference filter_multi_enable = mPrefScreen.findPreference("filter_multi_enable");
        EditTextPreference box_tracking_outside = mPrefScreen.findPreference("box_tracking_outside");
        EditTextPreference detection_audio_doa_list_size = mPrefScreen.findPreference("detection_audio_doa_list_size");
        EditTextPreference history_filter_depth = mPrefScreen.findPreference("history_filter_depth");
        EditTextPreference signal_pos_list_size = mPrefScreen.findPreference("signal_pos_list_size");
        EditTextPreference detection_audio_doa_threshold_high = mPrefScreen.findPreference("detection_audio_doa_threshold_high");
        EditTextPreference detection_audio_doa_threshold_low = mPrefScreen.findPreference("detection_audio_doa_threshold_low");
        EditTextPreference doa_detected_delay = mPrefScreen.findPreference("doa_detected_delay");
        EditTextPreference doa_change_delay = mPrefScreen.findPreference("doa_change_delay");
        ListPreference filter_average_fov_filter_enabled = mPrefScreen.findPreference("filter_average_fov_filter_enabled");
        ListPreference dsp_aip_mode = mPrefScreen.findPreference("dsp_aip_mode");
        EditTextPreference postprocess_yolov5_conf_threshold = mPrefScreen.findPreference("postprocess_yolov5_conf_threshold");
        EditTextPreference postprocess_yolov5_conf_threshold_person = mPrefScreen.findPreference("postprocess_yolov5_conf_threshold_person");
        EditTextPreference postprocess_yolov5_max_num_objects = mPrefScreen.findPreference("postprocess_yolov5_max_num_objects");
        EditTextPreference postprocess_yolov5_margins = mPrefScreen.findPreference("postprocess_yolov5_margins");
        ListPreference muxer_priority = mPrefScreen.findPreference("muxer_priority");
        EditTextPreference postprocess_segmentation_labels_file = mPrefScreen.findPreference("postprocess_segmentation_labels_file");
        EditTextPreference postprocess_segmentation_model_file = mPrefScreen.findPreference("postprocess_segmentation_model_file");
        EditTextPreference tracking_roi_release_cnt = mPrefScreen.findPreference("tracking_roi_release_cnt");
        EditTextPreference tracking_intersect_coef = mPrefScreen.findPreference("tracking_intersect_coef");
        EditTextPreference tracking_new_entry_list_depth = mPrefScreen.findPreference("tracking_new_entry_list_depth");
        EditTextPreference tracking_new_entry_timeout_ms = mPrefScreen.findPreference("tracking_new_entry_timeout_ms");
        ListPreference horizontal_flip = mPrefScreen.findPreference("horizontal_flip");
        ListPreference vertical_flip = mPrefScreen.findPreference("vertical_flip");
        EditTextPreference nms_threshold = mPrefScreen.findPreference("nms_threshold");
        EditTextPreference output_layers = mPrefScreen.findPreference("output_layers");
        EditTextPreference roi_release_count = mPrefScreen.findPreference("roi_release_count");
        EditTextPreference layout_file = mPrefScreen.findPreference("layout_file");

        String framing_mode_value = pref.getString("framing_mode", "GroupFraming");
        String framing_mode_value_final = "speaker";
        if (framing_mode_value.equals("SpeakerFraming")) {
            framing_mode_value_final = "speaker";
        }
        if (framing_mode_value.equals("GroupFraming")) {
            framing_mode_value_final = "group";
        }
        if (framing_mode_value.equals("PresenterFraming")) {
            framing_mode_value_final = "presenter";
        }
        if (framing_mode_value.equals("PeopleFraming")) {
            framing_mode_value_final = "people";
        }
        String gpu_transform_draw_debug_value = pref.getString("gpu_transform_draw_debug", "Disable");
        String gpu_transform_draw_debug_value_final = "0";
        if (gpu_transform_draw_debug_value.equals("Enable")) {
            gpu_transform_draw_debug_value_final = "1";
        } else {
            gpu_transform_draw_debug_value_final = "0";
        }
        String gpu_transform_enable_crop_value = pref.getString("gpu_transform_enable_crop", "Enable");
        String gpu_transform_enable_crop_value_final = "1";
        if (gpu_transform_enable_crop_value.equals("Disable")) {
            gpu_transform_enable_crop_value_final = "0";
        } else {
            gpu_transform_enable_crop_value_final = "1";
        }
        String autoframing_pos_moving_threshold_value = pref.getString("autoframing_pos_moving_threshold", "5");
        if (autoframing_pos_moving_threshold_value.equals("")) {
            autoframing_max_move_step.setText(getResources().getString(R.string.autoframing_pos_moving_threshold_hint));
        }
        String autoframing_size_moving_threshold_value = pref.getString("autoframing_size_moving_threshold", "5");
        if (autoframing_size_moving_threshold_value.equals("")) {
            autoframing_max_move_step.setText(getResources().getString(R.string.autoframing_size_moving_threshold_hint));
        }
        String autoframing_speed_movement_value = pref.getString("autoframing_speed_movement", "30");
        if (autoframing_speed_movement_value.equals("")) {
            autoframing_speed_movement.setText(getResources().getString(R.string.autoframing_speed_movement_hint));
        }
        String autoframing_max_move_step_value = pref.getString("autoframing_max_move_step", "20");
        if (autoframing_max_move_step_value.equals("")) {
            autoframing_max_move_step.setText(getResources().getString(R.string.autoframing_max_move_step_hint));
        }
        String autoframing_max_crop_ratio_value = pref.getString("autoframing_max_crop_ratio", "10");
        if (autoframing_max_crop_ratio_value.equals("")) {
            autoframing_max_crop_ratio.setText(getResources().getString(R.string.autoframing_max_crop_ratio_hint));
        }
        String dsp_aip_mode_value = pref.getString("dsp_aip_mode", "aip");
        String postprocess_yolov5_conf_threshold_value = pref.getString("postprocess_yolov5_conf_threshold", "0.25");
        if (postprocess_yolov5_conf_threshold_value.equals("")) {
            postprocess_yolov5_conf_threshold.setText(getResources().getString(R.string.postprocess_yolov5_conf_threshold_hint));
        }
        String postprocess_yolov5_conf_threshold_person_value = pref.getString("postprocess_yolov5_conf_threshold_person", "0.45");
        if (postprocess_yolov5_conf_threshold_person_value.equals("")) {
            postprocess_yolov5_conf_threshold_person.setText(getResources().getString(R.string.postprocess_yolov5_conf_threshold_person_hint));
        }
        String postprocess_yolov5_max_num_objects_value = pref.getString("postprocess_yolov5_max_num_objects", "10");
        if (postprocess_yolov5_max_num_objects_value.equals("")) {
            postprocess_yolov5_max_num_objects.setText(getResources().getString(R.string.postprocess_yolov5_max_num_objects_hint));
        }
        String postprocess_yolov5_margins_value = pref.getString("postprocess_yolov5_margins", "32");
        if (postprocess_yolov5_margins_value.equals("")) {
            postprocess_yolov5_margins.setText(getResources().getString(R.string.postprocess_yolov5_margins_hint));
        }
        String horizontal_flip_value = pref.getString("horizontal_flip", "false");
        String vertical_flip_value = pref.getString("vertical_flip", "true");

        if (!gpu_transform_draw_debug.isVisible()) {
            gpu_transform_draw_debug.setVisible(true);
        }
        if (!gpu_transform_enable_crop.isVisible()) {
            gpu_transform_enable_crop.setVisible(true);
        }
        if (!autoframing_pos_moving_threshold.isVisible()) {
            autoframing_pos_moving_threshold.setVisible(true);
        }
        if (!autoframing_size_moving_threshold.isVisible()) {
            autoframing_size_moving_threshold.setVisible(true);
        }
        if (!autoframing_speed_movement.isVisible()) {
            autoframing_speed_movement.setVisible(true);
        }
        if (!autoframing_max_move_step.isVisible()) {
            autoframing_max_move_step.setVisible(true);
        }
        if (!autoframing_max_crop_ratio.isVisible()) {
            autoframing_max_crop_ratio.setVisible(true);
        }
        if (!dsp_aip_mode.isVisible()) {
            dsp_aip_mode.setVisible(true);
        }
        if (!postprocess_yolov5_conf_threshold.isVisible()) {
            postprocess_yolov5_conf_threshold.setVisible(true);
        }
        if (!postprocess_yolov5_conf_threshold_person.isVisible()) {
            postprocess_yolov5_conf_threshold_person.setVisible(true);
        }
        if (!postprocess_yolov5_max_num_objects.isVisible()) {
            postprocess_yolov5_max_num_objects.setVisible(true);
        }
        if (!postprocess_yolov5_margins.isVisible()) {
            postprocess_yolov5_margins.setVisible(true);
        }
        if (!autoframing_filter_size.isVisible()) {
            autoframing_filter_size.setVisible(true);
        }
        if (!autoframing_filter_average_size.isVisible()) {
            autoframing_filter_average_size.setVisible(true);
        }
        if (!autoframing_pos_static_threshold.isVisible()) {
            autoframing_pos_static_threshold.setVisible(true);
        }
        if (!autoframing_size_static_threshold.isVisible()) {
            autoframing_size_static_threshold.setVisible(true);
        }
        if (!muxer_priority.isVisible()) {
            muxer_priority.setVisible(true);
        }
        if (!output_layers.isVisible()) {
            output_layers.setVisible(true);
        }
        if (!horizontal_flip.isVisible()) {
            horizontal_flip.setVisible(true);
        }
        if (!vertical_flip.isVisible()) {
            vertical_flip.setVisible(true);
        }
        if (csv_logging_enable.isVisible()) {
            csv_logging_enable.setVisible(false);
        }
        if (beamform_log_filename.isVisible()) {
            beamform_log_filename.setVisible(false);
        }
        if (filter_log_filename.isVisible()) {
            filter_log_filename.setVisible(false);
        }
        if (histogram_log_filename.isVisible()) {
            histogram_log_filename.setVisible(false);
        }
        if (device_log_filename.isVisible()) {
            device_log_filename.setVisible(false);
        }
        if (audio_plot.isVisible()) {
            audio_plot.setVisible(false);
        }
        if (autoframing_roi_release_cnt.isVisible()) {
            autoframing_roi_release_cnt.setVisible(false);
        }
        if (adoa_sound_card.isVisible()) {
            adoa_sound_card.setVisible(false);
        }
        if (adoa_backend_interface.isVisible()) {
            adoa_backend_interface.setVisible(false);
        }
        if (adoa_mixer_control_stt.isVisible()) {
            adoa_mixer_control_stt.setVisible(false);
        }
        if (adoa_mixer_control_fnn.isVisible()) {
            adoa_mixer_control_fnn.setVisible(false);
        }
        if (adoa_mixer_open_retry_delay.isVisible()) {
            adoa_mixer_open_retry_delay.setVisible(false);
        }
        if (adoa_mixer_open_max_retries.isVisible()) {
            adoa_mixer_open_max_retries.setVisible(false);
        }
        if (filter_select.isVisible()) {
            filter_select.setVisible(false);
        }
        if (frame_rate.isVisible()) {
            frame_rate.setVisible(false);
        }
        if (camera_fov.isVisible()) {
            camera_fov.setVisible(false);
        }
        if (device_sampling_period.isVisible()) {
            device_sampling_period.setVisible(false);
        }
        if (device_sample_size.isVisible()) {
            device_sample_size.setVisible(false);
        }
        if (device_sample_threshold.isVisible()) {
            device_sample_threshold.setVisible(false);
        }
        if (device_thread_timeout.isVisible()) {
            device_thread_timeout.setVisible(false);
        }
        if (device_mic_orientation.isVisible()) {
            device_mic_orientation.setVisible(false);
        }
        if (device_mic_flip.isVisible()) {
            device_mic_flip.setVisible(false);
        }
        if (device_mic_resolution.isVisible()) {
            device_mic_resolution.setVisible(false);
        }
        if (filter_speech_probability_threshold.isVisible()) {
            filter_speech_probability_threshold.setVisible(false);
        }
        if (filter_single_activation_threshold.isVisible()) {
            filter_single_activation_threshold.setVisible(false);
        }
        if (filter_single_activation_deadline.isVisible()) {
            filter_single_activation_deadline.setVisible(false);
        }
        if (filter_single_deactivation_delay.isVisible()) {
            filter_single_deactivation_delay.setVisible(false);
        }
        if (filter_multi_activation_threshold.isVisible()) {
            filter_multi_activation_threshold.setVisible(false);
        }
        if (filter_multi_activation_deadline.isVisible()) {
            filter_multi_activation_deadline.setVisible(false);
        }
        if (filter_multi_deactivation_delay.isVisible()) {
            filter_multi_deactivation_delay.setVisible(false);
        }
        if (filter_multi_max_speakers.isVisible()) {
            filter_multi_max_speakers.setVisible(false);
        }
        if (filter_multi_enable.isVisible()) {
            filter_multi_enable.setVisible(false);
        }
        if (box_tracking_outside.isVisible()) {
            box_tracking_outside.setVisible(false);
        }
        if (detection_audio_doa_list_size.isVisible()) {
            detection_audio_doa_list_size.setVisible(false);
        }
        if (history_filter_depth.isVisible()) {
            history_filter_depth.setVisible(false);
        }
        if (signal_pos_list_size.isVisible()) {
            signal_pos_list_size.setVisible(false);
        }
        if (detection_audio_doa_threshold_high.isVisible()) {
            detection_audio_doa_threshold_high.setVisible(false);
        }
        if (detection_audio_doa_threshold_low.isVisible()) {
            detection_audio_doa_threshold_low.setVisible(false);
        }
        if (doa_detected_delay.isVisible()) {
            doa_detected_delay.setVisible(false);
        }
        if (doa_change_delay.isVisible()) {
            doa_change_delay.setVisible(false);
        }
        if (filter_average_fov_filter_enabled.isVisible()) {
            filter_average_fov_filter_enabled.setVisible(false);
        }
        if (postprocess_segmentation_labels_file.isVisible()) {
            postprocess_segmentation_labels_file.setVisible(false);
        }
        if (postprocess_segmentation_model_file.isVisible()) {
            postprocess_segmentation_model_file.setVisible(false);
        }
        if (tracking_roi_release_cnt.isVisible()) {
            tracking_roi_release_cnt.setVisible(false);
        }
        if (tracking_intersect_coef.isVisible()) {
            tracking_intersect_coef.setVisible(false);
        }
        if (tracking_new_entry_list_depth.isVisible()) {
            tracking_new_entry_list_depth.setVisible(false);
        }
        if (tracking_new_entry_timeout_ms.isVisible()) {
            tracking_new_entry_timeout_ms.setVisible(false);
        }
        if (roi_release_count.isVisible()) {
            roi_release_count.setVisible(false);
        }
        if (layout_file.isVisible()) {
            layout_file.setVisible(false);
        }
        if (nms_threshold.isVisible()) {
            nms_threshold.setVisible(false);
        }

        try {
            @SuppressWarnings("rawtypes")
            Class SystemProperties = Class.forName("android.os.SystemProperties");
            Method set = SystemProperties.getMethod("set", String.class, String.class);
            set.invoke(SystemProperties, "persist.vendor.ai-director.framing_mode", framing_mode_value_final);
            set.invoke(SystemProperties, "persist.vendor.ai-director.gpu_transform.draw_debug", gpu_transform_draw_debug_value_final);
            set.invoke(SystemProperties, "persist.vendor.ai-director.gpu_transform.enable_crop", gpu_transform_enable_crop_value_final);
            set.invoke(SystemProperties, "persist.vendor.ai-director.dsp-aip-mode", dsp_aip_mode_value);
            set.invoke(SystemProperties, "persist.vendor.ai-director.horizontal_flip", horizontal_flip_value);
            set.invoke(SystemProperties, "persist.vendor.ai-director.vertical_flip", vertical_flip_value);
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (framing_mode.getValue().equals("GroupFraming")) {
            if (!postprocess_segmentation_labels_file.isVisible()) {
                postprocess_segmentation_labels_file.setVisible(true);
            }
            if (!postprocess_segmentation_model_file.isVisible()) {
                postprocess_segmentation_model_file.setVisible(true);
            }
            if (!nms_threshold.isVisible()) {
                nms_threshold.setVisible(true);
            }
            autoframing_filter_size.setDefaultValue("240");
            autoframing_filter_size.setSummary("240");
            String autoframing_group_filter_size_value = pref.getString("autoframing_filter_size", "240");
            autoframing_filter_size.setSummary(autoframing_group_filter_size_value);
            if (autoframing_group_filter_size_value.equals("")) {
                autoframing_filter_size.setText(getResources().getString(R.string.autoframing_filter_size_hint));
            }
            autoframing_filter_average_size.setDefaultValue("48");
            autoframing_filter_average_size.setSummary("48");
            String autoframing_group_filter_average_size_value = pref.getString("autoframing_filter_average_size", "48");
            autoframing_filter_average_size.setSummary(autoframing_group_filter_average_size_value);
            if (autoframing_group_filter_average_size_value.equals("")) {
                autoframing_filter_average_size.setText(getResources().getString(R.string.autoframing_filter_average_size_hint));
            }
            autoframing_pos_static_threshold.setDefaultValue("6");
            autoframing_pos_static_threshold.setSummary("6");
            String autoframing_group_pos_static_threshold_value = pref.getString("autoframing_pos_static_threshold", "6");
            autoframing_pos_static_threshold.setSummary(autoframing_group_pos_static_threshold_value);
            if (autoframing_group_pos_static_threshold_value.equals("")) {
                autoframing_pos_static_threshold.setText(getResources().getString(R.string.autoframing_pos_static_threshold_hint));
            }
            autoframing_size_static_threshold.setDefaultValue("6");
            autoframing_size_static_threshold.setSummary("6");
            String autoframing_group_size_static_threshold_value = pref.getString("autoframing_size_static_threshold", "6");
            autoframing_size_static_threshold.setSummary(autoframing_group_size_static_threshold_value);
            if (autoframing_group_size_static_threshold_value.equals("")) {
                autoframing_size_static_threshold.setText(getResources().getString(R.string.autoframing_size_static_threshold_hint));
            }
            muxer_priority.setDefaultValue("face,person");
            muxer_priority.setSummary("face,person");
            String group_muxer_priority_value = pref.getString("muxer_priority", "face,person");
            muxer_priority.setSummary(group_muxer_priority_value);
            String postprocess_segmentation_labels_file_value = pref.getString("postprocess_segmentation_labels_file", "/vendor/etc/camera/labels_seg.txt");
            if (postprocess_segmentation_labels_file_value.equals("")) {
                postprocess_segmentation_labels_file.setText(getResources().getString(R.string.postprocess_segmentation_labels_file_hint));
            }
            String postprocess_segmentation_model_file_value = pref.getString("postprocess_segmentation_model_file", "/vendor/etc/camera/deeplabv3_quantized.dlc");
            if (postprocess_segmentation_model_file_value.equals("")) {
                postprocess_segmentation_model_file.setText(getResources().getString(R.string.postprocess_segmentation_model_file_hint));
            }
            String output_layers_value = pref.getString("output_layers", "Conv_139,Conv_140,Conv_141");
            if (output_layers_value.equals("")) {
                output_layers.setText(getResources().getString(R.string.output_layers_hint));
            }
            String nms_threshold_value = pref.getString("nms_threshold", "0.1");
            if (nms_threshold_value.equals("")) {
                nms_threshold.setText(getResources().getString(R.string.nms_threshold_hint));
            }

            try {
                @SuppressWarnings("rawtypes")
                Class SystemProperties = Class.forName("android.os.SystemProperties");
                Method set = SystemProperties.getMethod("set", String.class, String.class);
                if (isInteger(autoframing_group_filter_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.autoframing.filter_size", autoframing_group_filter_size_value);
                }
                if (isInteger(autoframing_group_filter_average_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.autoframing.filter_average_size", autoframing_group_filter_average_size_value);
                }
                if (isInteger(autoframing_group_pos_static_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.autoframing.pos_static_threshold", autoframing_group_pos_static_threshold_value);
                }
                if (isInteger(autoframing_group_size_static_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.autoframing.size_static_threshold", autoframing_group_size_static_threshold_value);
                }
                if (isInteger(autoframing_pos_moving_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.autoframing.pos_moving_threshold", autoframing_pos_moving_threshold_value);
                }
                if (isInteger(autoframing_size_moving_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.autoframing.size_moving_threshold", autoframing_size_moving_threshold_value);
                }
                if (isInteger(autoframing_speed_movement_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.autoframing.speed_movement", autoframing_speed_movement_value);
                }
                if (isInteger(autoframing_max_move_step_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.autoframing.max_move_step", autoframing_max_move_step_value);
                }
                if (isInteger(autoframing_max_crop_ratio_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.autoframing.max_crop_ratio", autoframing_max_crop_ratio_value);
                }
                if (isFloat(postprocess_yolov5_conf_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.postprocess_yolov5.conf_threshold", postprocess_yolov5_conf_threshold_value);
                }
                if (isFloat(postprocess_yolov5_conf_threshold_person_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.postprocess_yolov5.conf_threshold_person", postprocess_yolov5_conf_threshold_person_value);
                }
                if (isInteger(postprocess_yolov5_max_num_objects_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.postprocess_yolov5.max_num_objects", postprocess_yolov5_max_num_objects_value);
                }
                if (isInteger(postprocess_yolov5_margins_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.postprocess_yolov5.margins", postprocess_yolov5_margins_value);
                }
                if (isFloat(nms_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.group.stabilization.nms_threshold", nms_threshold_value);
                }
                set.invoke(SystemProperties, "persist.vendor.ai-director.group.postprocess_yolov5.output_layers", output_layers_value);
                set.invoke(SystemProperties, "persist.vendor.ai-director.group.muxer.priority", group_muxer_priority_value);
                set.invoke(SystemProperties, "persist.vendor.ai-director.group.postprocess_segmentation.labels_file", postprocess_segmentation_labels_file_value);
                set.invoke(SystemProperties, "persist.vendor.ai-director.group.postprocess_segmentation.model_file", postprocess_segmentation_model_file_value);

            } catch (IllegalArgumentException iAE) {
                throw iAE;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (framing_mode.getValue().equals("SpeakerFraming")) {
            if (!adoa_sound_card.isVisible()) {
                adoa_sound_card.setVisible(true);
            }
            if (!adoa_backend_interface.isVisible()) {
                adoa_backend_interface.setVisible(true);
            }
            if (!adoa_mixer_control_stt.isVisible()) {
                adoa_mixer_control_stt.setVisible(true);
            }
            if (!adoa_mixer_control_fnn.isVisible()) {
                adoa_mixer_control_fnn.setVisible(true);
            }
            if (!adoa_mixer_open_retry_delay.isVisible()) {
                adoa_mixer_open_retry_delay.setVisible(true);
            }
            if (!adoa_mixer_open_max_retries.isVisible()) {
                adoa_mixer_open_max_retries.setVisible(true);
            }
            if (!filter_select.isVisible()) {
                filter_select.setVisible(true);
            }
            if (!frame_rate.isVisible()) {
                frame_rate.setVisible(true);
            }
            if (!camera_fov.isVisible()) {
                camera_fov.setVisible(true);
            }
            if (!device_sampling_period.isVisible()) {
                device_sampling_period.setVisible(true);
            }
            if (!device_sample_size.isVisible()) {
                device_sample_size.setVisible(true);
            }
            if (!device_sample_threshold.isVisible()) {
                device_sample_threshold.setVisible(true);
            }
            if (!device_thread_timeout.isVisible()) {
                device_thread_timeout.setVisible(true);
            }
            if (!device_mic_orientation.isVisible()) {
                device_mic_orientation.setVisible(true);
            }
            if (!device_mic_flip.isVisible()) {
                device_mic_flip.setVisible(true);
            }
            if (!device_mic_resolution.isVisible()) {
                device_mic_resolution.setVisible(true);
            }
            if (!filter_speech_probability_threshold.isVisible()) {
                filter_speech_probability_threshold.setVisible(true);
            }
            if (!filter_single_activation_threshold.isVisible()) {
                filter_single_activation_threshold.setVisible(true);
            }
            if (!filter_single_activation_deadline.isVisible()) {
                filter_single_activation_deadline.setVisible(true);
            }
            if (!filter_single_deactivation_delay.isVisible()) {
                filter_single_deactivation_delay.setVisible(true);
            }
            if (!filter_multi_activation_threshold.isVisible()) {
                filter_multi_activation_threshold.setVisible(true);
            }
            if (!filter_multi_activation_deadline.isVisible()) {
                filter_multi_activation_deadline.setVisible(true);
            }
            if (!filter_multi_deactivation_delay.isVisible()) {
                filter_multi_deactivation_delay.setVisible(true);
            }
            if (!filter_multi_max_speakers.isVisible()) {
                filter_multi_max_speakers.setVisible(true);
            }
            if (!filter_multi_enable.isVisible()) {
                filter_multi_enable.setVisible(true);
            }
            if (!box_tracking_outside.isVisible()) {
                box_tracking_outside.setVisible(true);
            }
            if (!detection_audio_doa_list_size.isVisible()) {
                detection_audio_doa_list_size.setVisible(true);
            }
            if (!history_filter_depth.isVisible()) {
                history_filter_depth.setVisible(true);
            }
            if (!signal_pos_list_size.isVisible()) {
                signal_pos_list_size.setVisible(true);
            }
            if (!detection_audio_doa_threshold_high.isVisible()) {
                detection_audio_doa_threshold_high.setVisible(true);
            }
            if (!detection_audio_doa_threshold_low.isVisible()) {
                detection_audio_doa_threshold_low.setVisible(true);
            }
            if (!doa_detected_delay.isVisible()) {
                doa_detected_delay.setVisible(true);
            }
            if (!doa_change_delay.isVisible()) {
                doa_change_delay.setVisible(true);
            }
            if (!filter_average_fov_filter_enabled.isVisible()) {
                filter_average_fov_filter_enabled.setVisible(true);
            }
            if (!csv_logging_enable.isVisible()) {
                csv_logging_enable.setVisible(true);
            }
            if (!beamform_log_filename.isVisible()) {
                beamform_log_filename.setVisible(true);
            }
            if (!filter_log_filename.isVisible()) {
                filter_log_filename.setVisible(true);
            }
            if (!histogram_log_filename.isVisible()) {
                histogram_log_filename.setVisible(true);
            }
            if (!device_log_filename.isVisible()) {
                device_log_filename.setVisible(true);
            }
            if (!audio_plot.isVisible()) {
                audio_plot.setVisible(true);
            }
            if (!nms_threshold.isVisible()) {
                nms_threshold.setVisible(true);
            }

            autoframing_filter_size.setDefaultValue("120");
            autoframing_filter_size.setSummary("120");
            String autoframing_speaker_filter_size_value = pref.getString("autoframing_filter_size", "120");
            autoframing_filter_size.setSummary(autoframing_speaker_filter_size_value);
            if (autoframing_speaker_filter_size_value.equals("")) {
                autoframing_filter_size.setText(getResources().getString(R.string.autoframing_filter_size_hint));
            }
            autoframing_filter_average_size.setDefaultValue("60");
            autoframing_filter_average_size.setSummary("60");
            String autoframing_speaker_filter_average_size_value = pref.getString("autoframing_filter_average_size", "60");
            autoframing_filter_average_size.setSummary(autoframing_speaker_filter_average_size_value);
            if (autoframing_speaker_filter_average_size_value.equals("")) {
                autoframing_filter_average_size.setText(getResources().getString(R.string.autoframing_filter_average_size_hint));
            }
            autoframing_pos_static_threshold.setDefaultValue("15");
            autoframing_pos_static_threshold.setSummary("15");
            String autoframing_speaker_pos_static_threshold_value = pref.getString("autoframing_pos_static_threshold", "15");
            autoframing_pos_static_threshold.setSummary(autoframing_speaker_pos_static_threshold_value);
            if (autoframing_speaker_pos_static_threshold_value.equals("")) {
                autoframing_pos_static_threshold.setText(getResources().getString(R.string.autoframing_pos_static_threshold_hint));
            }
            autoframing_size_static_threshold.setDefaultValue("15");
            autoframing_size_static_threshold.setSummary("15");
            String autoframing_speaker_size_static_threshold_value = pref.getString("autoframing_size_static_threshold", "15");
            autoframing_size_static_threshold.setSummary(autoframing_speaker_size_static_threshold_value);
            if (autoframing_speaker_size_static_threshold_value.equals("")) {
                autoframing_size_static_threshold.setText(getResources().getString(R.string.autoframing_size_static_threshold_hint));
            }
            muxer_priority.setDefaultValue("face");
            muxer_priority.setSummary("face");
            String speaker_muxer_priority_value = pref.getString("muxer_priority", "face");
            muxer_priority.setSummary(speaker_muxer_priority_value);
            String adoa_sound_card_value = pref.getString("adoa_sound_card", "0");
            if (adoa_sound_card_value.equals("")) {
                autoframing_filter_size.setText(getResources().getString(R.string.adoa_sound_card_hint));
            }
            String adoa_backend_interface_value = pref.getString("adoa_backend_interface", "TX_CDC_DMA_TX_3");
            if (adoa_backend_interface_value.equals("")) {
                adoa_backend_interface.setText(getResources().getString(R.string.adoa_backend_interface_hint));
            }
            String adoa_mixer_control_stt_value = pref.getString("adoa_mixer_control_stt", "Source Tracking Audio Tx");
            if (adoa_mixer_control_stt_value.equals("")) {
                adoa_mixer_control_stt.setText(getResources().getString(R.string.adoa_mixer_control_stt_hint));
            }
            String adoa_mixer_control_fnn_value = pref.getString("adoa_mixer_control_fnn", "Fnn Audio Tx");
            if (adoa_mixer_control_fnn_value.equals("")) {
                adoa_mixer_control_fnn.setText(getResources().getString(R.string.adoa_mixer_control_fnn_hint));
            }
            String adoa_mixer_open_retry_delay_value = pref.getString("adoa_mixer_open_retry_delay", "1000");
            if (adoa_mixer_open_retry_delay_value.equals("")) {
                adoa_mixer_open_retry_delay.setText(getResources().getString(R.string.adoa_mixer_open_retry_delay_hint));
            }
            String adoa_mixer_open_max_retries_value = pref.getString("adoa_mixer_open_max_retries", "10");
            if (adoa_mixer_open_max_retries_value.equals("")) {
                adoa_mixer_open_max_retries.setText(getResources().getString(R.string.adoa_mixer_open_max_retries_hint));
            }
            String filter_select_value = pref.getString("filter_select", "0");
            String frame_rate_value = pref.getString("frame_rate", "30.0");
            if (frame_rate_value.equals("")) {
                frame_rate.setText(getResources().getString(R.string.frame_rate_hint));
            }
            String camera_fov_value = pref.getString("camera_fov", "110");
            if (camera_fov_value.equals("")) {
                camera_fov.setText(getResources().getString(R.string.camera_fov_hint));
            }
            String device_sampling_period_value = pref.getString("device_sampling_period", "40");
            if (device_sampling_period_value.equals("")) {
                device_sampling_period.setText(getResources().getString(R.string.device_sampling_period_hint));
            }
            String device_sample_size_value = pref.getString("device_sample_size", "10");
            if (device_sample_size_value.equals("")) {
                device_sample_size.setText(getResources().getString(R.string.device_sample_size_hint));
            }
            String device_sample_threshold_value = pref.getString("device_sample_threshold", "35");
            if (device_sample_threshold_value.equals("")) {
                device_sample_threshold.setText(getResources().getString(R.string.device_sample_threshold_hint));
            }
            String device_thread_timeout_value = pref.getString("device_thread_timeout", "500");
            if (device_thread_timeout_value.equals("")) {
                device_thread_timeout.setText(getResources().getString(R.string.device_thread_timeout_hint));
            }
            String device_mic_orientation_value = pref.getString("device_mic_orientation", "180");
            if (device_mic_orientation_value.equals("")) {
                device_mic_orientation.setText(getResources().getString(R.string.device_mic_orientation_hint));
            }
            String device_mic_flip_value = pref.getString("device_mic_flip", "Disable");
            String device_mic_flip_value_final = "1";
            if (device_mic_flip_value.equals("Disable")) {
                device_mic_flip_value_final = "0";
            } else {
                device_mic_flip_value_final = "1";
            }
            String device_mic_resolution_value = pref.getString("device_mic_resolution", "8");
            if (device_mic_resolution_value.equals("")) {
                device_mic_resolution.setText(getResources().getString(R.string.device_mic_resolution_hint));
            }
            String filter_speech_probability_threshold_value = pref.getString("filter_speech_probability_threshold", "95.0");
            if (filter_speech_probability_threshold_value.equals("")) {
                filter_speech_probability_threshold.setText(getResources().getString(R.string.filter_speech_probability_threshold_hint));
            }
            String filter_single_activation_threshold_value = pref.getString("filter_single_activation_threshold", "100");
            if (filter_single_activation_threshold_value.equals("")) {
                filter_single_activation_threshold.setText(getResources().getString(R.string.filter_single_activation_threshold_hint));
            }
            String filter_single_activation_deadline_value = pref.getString("filter_single_activation_deadline", "1000");
            if (filter_single_activation_deadline_value.equals("")) {
                filter_single_activation_deadline.setText(getResources().getString(R.string.filter_single_activation_deadline_hint));
            }
            String filter_single_deactivation_delay_value = pref.getString("filter_single_deactivation_delay", "3000");
            if (filter_single_deactivation_delay_value.equals("")) {
                filter_single_deactivation_delay.setText(getResources().getString(R.string.filter_single_deactivation_delay_hint));
            }
            String filter_multi_activation_threshold_value = pref.getString("filter_multi_activation_threshold", "5000");
            if (filter_multi_activation_threshold_value.equals("")) {
                filter_multi_activation_threshold.setText(getResources().getString(R.string.filter_multi_activation_threshold_hint));
            }
            String filter_multi_activation_deadline_value = pref.getString("filter_multi_activation_deadline", "8000");
            if (filter_multi_activation_deadline_value.equals("")) {
                filter_multi_activation_deadline.setText(getResources().getString(R.string.filter_multi_activation_deadline_hint));
            }
            String filter_multi_deactivation_delay_value = pref.getString("filter_multi_deactivation_delay", "5000");
            if (filter_multi_deactivation_delay_value.equals("")) {
                filter_multi_deactivation_delay.setText(getResources().getString(R.string.filter_multi_deactivation_delay_hint));
            }
            String filter_multi_max_speakers_value = pref.getString("filter_multi_max_speakers", "5");
            if (filter_multi_max_speakers_value.equals("")) {
                filter_multi_max_speakers.setText(getResources().getString(R.string.filter_multi_max_speakers_hint));
            }
            String filter_multi_enable_value = pref.getString("filter_multi_enable", "Enable");
            String filter_multi_enable_value_final = "1";
            if (filter_multi_enable_value.equals("Disable")) {
                filter_multi_enable_value_final = "0";
            } else {
                filter_multi_enable_value_final = "1";
            }
            String box_tracking_outside_value = pref.getString("box_tracking_outside", "30");
            if (box_tracking_outside_value.equals("")) {
                box_tracking_outside.setText(getResources().getString(R.string.box_tracking_outside_hint));
            }
            String detection_audio_doa_list_size_value = pref.getString("detection_audio_doa_list_size", "250");
            if (detection_audio_doa_list_size_value.equals("")) {
                detection_audio_doa_list_size.setText(getResources().getString(R.string.detection_audio_doa_list_size_hint));
            }
            String history_filter_depth_value = pref.getString("history_filter_depth", "60");
            if (history_filter_depth_value.equals("")) {
                history_filter_depth.setText(getResources().getString(R.string.history_filter_depth_hint));
            }
            String signal_pos_list_size_value = pref.getString("signal_pos_list_size", "60");
            if (signal_pos_list_size_value.equals("")) {
                signal_pos_list_size.setText(getResources().getString(R.string.signal_pos_list_size_hint));
            }
            String detection_audio_doa_threshold_high_value = pref.getString("detection_audio_doa_threshold_high", "28.0");
            if (detection_audio_doa_threshold_high_value.equals("")) {
                detection_audio_doa_threshold_high.setText(getResources().getString(R.string.detection_audio_doa_threshold_high_hint));
            }
            String detection_audio_doa_threshold_low_value = pref.getString("detection_audio_doa_threshold_low", "15.0");
            if (detection_audio_doa_threshold_low_value.equals("")) {
                detection_audio_doa_threshold_low.setText(getResources().getString(R.string.detection_audio_doa_threshold_low_hint));
            }
            String doa_detected_delay_value = pref.getString("doa_detected_delay", "100");
            if (doa_detected_delay_value.equals("")) {
                doa_detected_delay.setText(getResources().getString(R.string.doa_detected_delay_hint));
            }
            String doa_change_delay_value = pref.getString("doa_change_delay", "50");
            if (doa_change_delay_value.equals("")) {
                doa_change_delay.setText(getResources().getString(R.string.doa_change_delay_hint));
            }
            String filter_average_fov_filter_enabled_value = pref.getString("filter_average_fov_filter_enabled", "Enable");
            String filter_average_fov_filter_enabled_value_final = "1";
            if (filter_average_fov_filter_enabled_value.equals("Disable")) {
                filter_average_fov_filter_enabled_value_final = "0";
            } else {
                filter_average_fov_filter_enabled_value_final = "1";
            }
            String csv_logging_enable_value = pref.getString("csv_logging_enable", "Disable");
            String csv_logging_enable_value_final = "1";
            if (csv_logging_enable_value.equals("Disable")) {
                csv_logging_enable_value_final = "0";
            } else {
                csv_logging_enable_value_final = "1";
            }
            String beamform_log_filename_value = pref.getString("beamform_log_filename", "/vendor/etc/camera/adoa_beamform.csv");
            if (beamform_log_filename_value.equals("")) {
                beamform_log_filename.setText(getResources().getString(R.string.beamform_log_filename_hint));
            }
            String filter_log_filename_value = pref.getString("filter_log_filename", "/vendor/etc/camera/adoa_filter.csv");
            if (filter_log_filename_value.equals("")) {
                filter_log_filename.setText(getResources().getString(R.string.filter_log_filename_hint));
            }
            String histogram_log_filename_value = pref.getString("histogram_log_filename", "/vendor/etc/camera/adoa_histogram.csv");
            if (histogram_log_filename_value.equals("")) {
                histogram_log_filename.setText(getResources().getString(R.string.histogram_log_filename_hint));
            }
            String device_log_filename_value = pref.getString("device_log_filename", "/vendor/etc/camera/adoa_device.csv");
            if (device_log_filename_value.equals("")) {
                beamform_log_filename.setText(getResources().getString(R.string.device_log_filename_hint));
            }
            String audio_plot_value = pref.getString("audio_plot", "Disable");
            String audio_plot_value_final = "1";
            if (audio_plot_value.equals("Disable")) {
                audio_plot_value_final = "0";
            } else {
                audio_plot_value_final = "1";
            }
            String output_layers_value = pref.getString("output_layers", "Conv_139,Conv_140,Conv_141");
            if (output_layers_value.equals("")) {
                output_layers.setText(getResources().getString(R.string.output_layers_hint));
            }
            String nms_threshold_value = pref.getString("nms_threshold", "0.1");
            if (nms_threshold_value.equals("")) {
                nms_threshold.setText(getResources().getString(R.string.nms_threshold_hint));
            }

            try {
                @SuppressWarnings("rawtypes")
                Class SystemProperties = Class.forName("android.os.SystemProperties");
                Method set = SystemProperties.getMethod("set", String.class, String.class);
                if (isInteger(autoframing_pos_moving_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.autoframing.pos_moving_threshold", autoframing_pos_moving_threshold_value);
                }
                if (isInteger(autoframing_size_moving_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.autoframing.size_moving_threshold", autoframing_size_moving_threshold_value);
                }
                if (isInteger(autoframing_speed_movement_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.autoframing.speed_movement", autoframing_speed_movement_value);
                }
                if (isInteger(autoframing_max_move_step_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.autoframing.max_move_step", autoframing_max_move_step_value);
                }
                if (isInteger(autoframing_max_crop_ratio_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.autoframing.max_crop_ratio", autoframing_max_crop_ratio_value);
                }
                if (isFloat(postprocess_yolov5_conf_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.postprocess_yolov5.conf_threshold", postprocess_yolov5_conf_threshold_value);
                }
                if (isFloat(postprocess_yolov5_conf_threshold_person_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.postprocess_yolov5.conf_threshold_person", postprocess_yolov5_conf_threshold_person_value);
                }
                if (isInteger(postprocess_yolov5_max_num_objects_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.postprocess_yolov5.max_num_objects", postprocess_yolov5_max_num_objects_value);
                }
                if (isInteger(postprocess_yolov5_margins_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.postprocess_yolov5.margins", postprocess_yolov5_margins_value);
                }
                if (isInteger(autoframing_speaker_filter_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.autoframing.filter_size", autoframing_speaker_filter_size_value);
                }
                if (isInteger(autoframing_speaker_filter_average_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.autoframing.filter_average_size", autoframing_speaker_filter_average_size_value);
                }
                if (isInteger(autoframing_speaker_pos_static_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.autoframing.pos_static_threshold", autoframing_speaker_pos_static_threshold_value);
                }
                if (isInteger(autoframing_speaker_size_static_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.autoframing.size_static_threshold", autoframing_speaker_size_static_threshold_value);
                }
                set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.muxer.priority", speaker_muxer_priority_value);
                if (isInteger(adoa_sound_card_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.adoa.soundcard", adoa_sound_card_value);
                }
                set.invoke(SystemProperties, "persist.vendor.ai-director.adoa.backend_interface", adoa_backend_interface_value);
                set.invoke(SystemProperties, "persist.vendor.ai-director.adoa.mixer_control_stt", adoa_mixer_control_stt_value);
                set.invoke(SystemProperties, "persist.vendor.ai-director.adoa.mixer_control_fnn", adoa_mixer_control_fnn_value);
                if (isInteger(adoa_mixer_open_retry_delay_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.adoa.mixer_open_retry_delay", adoa_mixer_open_retry_delay_value);
                }
                if (isInteger(adoa_mixer_open_max_retries_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.adoa.mixer_open_max_retries", adoa_mixer_open_max_retries_value);
                }
                set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.filter_select", filter_select_value);
                if (isFloat(frame_rate_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.frame_rate", frame_rate_value);
                }
                if (isInteger(camera_fov_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.camera_fov", camera_fov_value);
                }
                if (isInteger(device_sampling_period_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.device.sampling_period", device_sampling_period_value);
                }
                if (isInteger(device_sample_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.device.sample_size", device_sample_size_value);
                }
                if (isInteger(device_sample_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.device.sample_threshold", device_sample_threshold_value);
                }
                if (isInteger(device_thread_timeout_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.device.thread_timeout", device_thread_timeout_value);
                }
                if (isInteger(device_mic_orientation_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.device.mic_orientation", device_mic_orientation_value);
                }
                set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.device.mic_flip", device_mic_flip_value_final);
                if (isInteger(device_mic_resolution_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.device.mic_resolution", device_mic_resolution_value);
                }
                if (isFloat(filter_speech_probability_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.filter.speech_probability_threshold", filter_speech_probability_threshold_value);
                }
                if (isInteger(filter_single_activation_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.filter.single_activation_threshold", filter_single_activation_threshold_value);
                }
                if (isInteger(filter_single_activation_deadline_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.filter.single_activation_deadline", filter_single_activation_deadline_value);
                }
                if (isInteger(filter_single_deactivation_delay_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.filter.single_deactivation_delay", filter_single_deactivation_delay_value);
                }
                if (isInteger(filter_multi_activation_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.filter.multi_activation_threshold", filter_multi_activation_threshold_value);
                }
                if (isInteger(filter_multi_activation_deadline_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.filter.multi_activation_deadline", filter_multi_activation_deadline_value);
                }
                if (isInteger(filter_multi_deactivation_delay_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.filter.multi_deactivation_delay", filter_multi_deactivation_delay_value);
                }
                if (isInteger(filter_multi_max_speakers_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.filter.multi_max_speakers", filter_multi_max_speakers_value);
                }
                set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.filter.multi_enable", filter_multi_enable_value_final);
                if (isInteger(box_tracking_outside_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.box_tracking_outside", box_tracking_outside_value);
                }
                if (isInteger(detection_audio_doa_list_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.detection_audio_doa_list_size", detection_audio_doa_list_size_value);
                }
                if (isInteger(history_filter_depth_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.history_filter_depth", history_filter_depth_value);
                }
                if (isInteger(signal_pos_list_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.signal_pos_list_size", signal_pos_list_size_value);
                }
                if (isFloat(detection_audio_doa_threshold_high_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.detection_audio_doa_threshold_high", detection_audio_doa_threshold_high_value);
                }
                if (isFloat(detection_audio_doa_threshold_low_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.detection_audio_doa_threshold_low", detection_audio_doa_threshold_low_value);
                }
                if (isInteger(doa_detected_delay_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.doa_detected_delay", doa_detected_delay_value);
                }
                if (isInteger(doa_change_delay_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.doa_change_delay", doa_change_delay_value);
                }
                if (isFloat(nms_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.stabilization.nms_threshold", nms_threshold_value);
                }
                set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.postprocess_yolov5.output_layers", output_layers_value);
                set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.filter.average_fov_filter_enabled", filter_average_fov_filter_enabled_value_final);
                set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.csv_logging_enable", csv_logging_enable_value_final);
                set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.beamform_log_filename", beamform_log_filename_value);
                set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.filter_log_filename", filter_log_filename_value);
                set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.histogram_log_filename", histogram_log_filename_value);
                set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.device_log_filename", device_log_filename_value);
                set.invoke(SystemProperties, "persist.vendor.ai-director.speaker.audio_plot", audio_plot_value_final);

            } catch (IllegalArgumentException iAE) {
                throw iAE;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (framing_mode.getValue().equals("PeopleFraming")) {
            if (!autoframing_roi_release_cnt.isVisible()) {
                autoframing_roi_release_cnt.setVisible(true);
            }
            if (!tracking_roi_release_cnt.isVisible()) {
                tracking_roi_release_cnt.setVisible(true);
            }
            if (!tracking_intersect_coef.isVisible()) {
                tracking_intersect_coef.setVisible(true);
            }
            if (!tracking_new_entry_list_depth.isVisible()) {
                tracking_new_entry_list_depth.setVisible(true);
            }
            if (!tracking_new_entry_timeout_ms.isVisible()) {
                tracking_new_entry_timeout_ms.setVisible(true);
            }
            if (!roi_release_count.isVisible()) {
                roi_release_count.setVisible(true);
            }
            if (!layout_file.isVisible()) {
                layout_file.setVisible(true);
            }
            if (nms_threshold.isVisible()) {
                nms_threshold.setVisible(false);
            }

            autoframing_filter_size.setDefaultValue("120");
            autoframing_filter_size.setSummary("120");
            String autoframing_people_filter_size_value = pref.getString("autoframing_filter_size", "120");
            autoframing_filter_size.setSummary(autoframing_people_filter_size_value);
            if (autoframing_people_filter_size_value.equals("")) {
                autoframing_filter_size.setText(getResources().getString(R.string.autoframing_filter_size_hint));
            }
            autoframing_filter_average_size.setDefaultValue("60");
            autoframing_filter_average_size.setSummary("60");
            String autoframing_people_filter_average_size_value = pref.getString("autoframing_filter_average_size", "60");
            autoframing_filter_average_size.setSummary(autoframing_people_filter_average_size_value);
            if (autoframing_people_filter_average_size_value.equals("")) {
                autoframing_filter_average_size.setText(getResources().getString(R.string.autoframing_filter_average_size_hint));
            }
            autoframing_pos_static_threshold.setDefaultValue("20");
            autoframing_pos_static_threshold.setSummary("20");
            String autoframing_people_pos_static_threshold_value = pref.getString("autoframing_pos_static_threshold", "20");
            autoframing_pos_static_threshold.setSummary(autoframing_people_pos_static_threshold_value);
            if (autoframing_people_pos_static_threshold_value.equals("")) {
                autoframing_pos_static_threshold.setText(getResources().getString(R.string.autoframing_pos_static_threshold_hint));
            }
            autoframing_size_static_threshold.setDefaultValue("15");
            autoframing_size_static_threshold.setSummary("15");
            String autoframing_people_size_static_threshold_value = pref.getString("autoframing_size_static_threshold", "15");
            autoframing_size_static_threshold.setSummary(autoframing_people_size_static_threshold_value);
            if (autoframing_people_size_static_threshold_value.equals("")) {
                autoframing_size_static_threshold.setText(getResources().getString(R.string.autoframing_size_static_threshold_hint));
            }
            muxer_priority.setDefaultValue("face");
            muxer_priority.setSummary("face");
            String people_muxer_priority_value = pref.getString("muxer_priority", "face");
            muxer_priority.setSummary(people_muxer_priority_value);
            String autoframing_roi_release_cnt_value = pref.getString("autoframing_roi_release_cnt", "120");
            if (autoframing_roi_release_cnt_value.equals("")) {
                autoframing_roi_release_cnt.setText(getResources().getString(R.string.autoframing_roi_release_cnt_hint));
            }
            String tracking_roi_release_cnt_value = pref.getString("tracking_roi_release_cnt", "120");
            if (tracking_roi_release_cnt_value.equals("")) {
                tracking_roi_release_cnt.setText(getResources().getString(R.string.tracking_roi_release_cnt_hint));
            }
            String tracking_intersect_coef_value = pref.getString("tracking_intersect_coef", "0.4");
            if (tracking_intersect_coef_value.equals("")) {
                tracking_intersect_coef.setText(getResources().getString(R.string.tracking_intersect_coef_hint));
            }
            String tracking_new_entry_list_depth_value = pref.getString("tracking_new_entry_list_depth", "60");
            if (tracking_new_entry_list_depth_value.equals("")) {
                tracking_new_entry_list_depth.setText(getResources().getString(R.string.tracking_new_entry_list_depth_hint));
            }
            String tracking_new_entry_timeout_ms_value = pref.getString("tracking_new_entry_timeout_ms", "4000");
            if (tracking_new_entry_timeout_ms_value.equals("")) {
                tracking_new_entry_timeout_ms.setText(getResources().getString(R.string.tracking_new_entry_timeout_ms_hint));
            }
            String roi_release_count_value = pref.getString("roi_release_count", "5");
            if (roi_release_count_value.equals("")) {
                roi_release_count.setText(getResources().getString(R.string.roi_release_count_hint));
            }
            String layout_file_value = pref.getString("layout_file", "/vendor/etc/camera/grid_layout.json");
            if (layout_file_value.equals("")) {
                layout_file.setText(getResources().getString(R.string.layout_file_hint));
            }
            String output_layers_value = pref.getString("output_layers", "Conv_139,Conv_140,Conv_141");
            if (output_layers_value.equals("")) {
                output_layers.setText(getResources().getString(R.string.output_layers_hint));
            }

            try {
                @SuppressWarnings("rawtypes")
                Class SystemProperties = Class.forName("android.os.SystemProperties");
                Method set = SystemProperties.getMethod("set", String.class, String.class);
                if (isInteger(autoframing_pos_moving_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.autoframing.pos_moving_threshold", autoframing_pos_moving_threshold_value);
                }
                if (isInteger(autoframing_size_moving_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.autoframing.size_moving_threshold", autoframing_size_moving_threshold_value);
                }
                if (isInteger(autoframing_speed_movement_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.autoframing.speed_movement", autoframing_speed_movement_value);
                }
                if (isInteger(autoframing_max_move_step_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.autoframing.max_move_step", autoframing_max_move_step_value);
                }
                if (isInteger(autoframing_max_crop_ratio_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.autoframing.max_crop_ratio", autoframing_max_crop_ratio_value);
                }
                if (isFloat(postprocess_yolov5_conf_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.postprocess_yolov5.conf_threshold", postprocess_yolov5_conf_threshold_value);
                }
                if (isFloat(postprocess_yolov5_conf_threshold_person_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.postprocess_yolov5.conf_threshold_person", postprocess_yolov5_conf_threshold_person_value);
                }
                if (isInteger(postprocess_yolov5_max_num_objects_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.postprocess_yolov5.max_num_objects", postprocess_yolov5_max_num_objects_value);
                }
                if (isInteger(postprocess_yolov5_margins_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.postprocess_yolov5.margins", postprocess_yolov5_margins_value);
                }
                if (isInteger(autoframing_people_filter_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.autoframing.filter_size", autoframing_people_filter_size_value);
                }
                if (isInteger(autoframing_people_filter_average_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.autoframing.filter_average_size", autoframing_people_filter_average_size_value);
                }
                if (isInteger(autoframing_people_pos_static_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.autoframing.pos_static_threshold", autoframing_people_pos_static_threshold_value);
                }
                if (isInteger(autoframing_people_size_static_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.autoframing.size_static_threshold", autoframing_people_size_static_threshold_value);
                }
                set.invoke(SystemProperties, "persist.vendor.ai-director.people.muxer.priority", people_muxer_priority_value);
                if (isInteger(autoframing_roi_release_cnt_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.autoframing.roi_release_cnt", autoframing_roi_release_cnt_value);
                }
                if (isInteger(tracking_roi_release_cnt_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.tracking.roi_release_cnt", tracking_roi_release_cnt_value);
                }
                if (isFloat(tracking_intersect_coef_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.tracking.intersect_coef", tracking_intersect_coef_value);
                }
                if (isInteger(tracking_new_entry_list_depth_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.tracking.new_entry_list_depth", tracking_new_entry_list_depth_value);
                }
                if (isInteger(tracking_new_entry_timeout_ms_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.people.tracking.new_entry_timeout_ms", tracking_new_entry_timeout_ms_value);
                }
                if (isInteger(roi_release_count_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.grid_manager.roi_release_count", roi_release_count_value);
                }
                set.invoke(SystemProperties, "persist.vendor.ai-director.grid_manager.layout_file", layout_file_value);
                set.invoke(SystemProperties, "persist.vendor.ai-director.people.postprocess_yolov5.output_layers", output_layers_value);

            } catch (IllegalArgumentException iAE) {
                throw iAE;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (framing_mode.getValue().equals("PresenterFraming")) {
            if (!tracking_roi_release_cnt.isVisible()) {
                tracking_roi_release_cnt.setVisible(true);
            }
            if (!tracking_intersect_coef.isVisible()) {
                tracking_intersect_coef.setVisible(true);
            }
            if (!tracking_new_entry_list_depth.isVisible()) {
                tracking_new_entry_list_depth.setVisible(true);
            }
            if (!tracking_new_entry_timeout_ms.isVisible()) {
                tracking_new_entry_timeout_ms.setVisible(true);
            }
            if (!nms_threshold.isVisible()) {
                nms_threshold.setVisible(true);
            }

            autoframing_filter_size.setDefaultValue("240");
            autoframing_filter_size.setSummary("240");
            String autoframing_presenter_filter_size_value = pref.getString("autoframing_filter_size", "240");
            autoframing_filter_size.setSummary(autoframing_presenter_filter_size_value);
            if (autoframing_presenter_filter_size_value.equals("")) {
                autoframing_filter_size.setText(getResources().getString(R.string.autoframing_filter_size_hint));
            }
            autoframing_filter_average_size.setDefaultValue("48");
            autoframing_filter_average_size.setSummary("48");
            String autoframing_presenter_filter_average_size_value = pref.getString("autoframing_filter_average_size", "48");
            autoframing_filter_average_size.setSummary(autoframing_presenter_filter_average_size_value);
            if (autoframing_presenter_filter_average_size_value.equals("")) {
                autoframing_filter_average_size.setText(getResources().getString(R.string.autoframing_filter_average_size_hint));
            }
            autoframing_pos_static_threshold.setDefaultValue("6");
            autoframing_pos_static_threshold.setSummary("6");
            String autoframing_presenter_pos_static_threshold_value = pref.getString("autoframing_pos_static_threshold", "6");
            autoframing_pos_static_threshold.setSummary(autoframing_presenter_pos_static_threshold_value);
            if (autoframing_presenter_pos_static_threshold_value.equals("")) {
                autoframing_pos_static_threshold.setText(getResources().getString(R.string.autoframing_pos_static_threshold_hint));
            }
            autoframing_size_static_threshold.setDefaultValue("6");
            autoframing_size_static_threshold.setSummary("6");
            String autoframing_presenter_size_static_threshold_value = pref.getString("autoframing_size_static_threshold", "6");
            autoframing_size_static_threshold.setSummary(autoframing_presenter_size_static_threshold_value);
            if (autoframing_presenter_size_static_threshold_value.equals("")) {
                autoframing_size_static_threshold.setText(getResources().getString(R.string.autoframing_size_static_threshold_hint));
            }
            muxer_priority.setDefaultValue("person");
            muxer_priority.setSummary("person");
            String presenter_muxer_priority_value = pref.getString("muxer_priority", "person");
            muxer_priority.setSummary(presenter_muxer_priority_value);
            String tracking_roi_release_cnt_value = pref.getString("tracking_roi_release_cnt", "120");
            if (tracking_roi_release_cnt_value.equals("")) {
                tracking_roi_release_cnt.setText(getResources().getString(R.string.tracking_roi_release_cnt_hint));
            }
            String tracking_intersect_coef_value = pref.getString("tracking_intersect_coef", "0.4");
            if (tracking_intersect_coef_value.equals("")) {
                tracking_intersect_coef.setText(getResources().getString(R.string.tracking_intersect_coef_hint));
            }
            String tracking_new_entry_list_depth_value = pref.getString("tracking_new_entry_list_depth", "60");
            if (tracking_new_entry_list_depth_value.equals("")) {
                tracking_new_entry_list_depth.setText(getResources().getString(R.string.tracking_new_entry_list_depth_hint));
            }
            String tracking_new_entry_timeout_ms_value = pref.getString("tracking_new_entry_timeout_ms", "4000");
            if (tracking_new_entry_timeout_ms_value.equals("")) {
                tracking_new_entry_timeout_ms.setText(getResources().getString(R.string.tracking_new_entry_timeout_ms_hint));
            }
            String output_layers_value = pref.getString("output_layers", "Conv_139,Conv_140,Conv_141");
            if (output_layers_value.equals("")) {
                output_layers.setText(getResources().getString(R.string.output_layers_hint));
            }
            String nms_threshold_value = pref.getString("nms_threshold", "0.1");
            if (nms_threshold_value.equals("")) {
                nms_threshold.setText(getResources().getString(R.string.nms_threshold_hint));
            }
            try {
                @SuppressWarnings("rawtypes")
                Class SystemProperties = Class.forName("android.os.SystemProperties");
                Method set = SystemProperties.getMethod("set", String.class, String.class);
                if (isInteger(autoframing_pos_moving_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.autoframing.pos_moving_threshold", autoframing_pos_moving_threshold_value);
                }
                if (isInteger(autoframing_size_moving_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.autoframing.size_moving_threshold", autoframing_size_moving_threshold_value);
                }
                if (isInteger(autoframing_speed_movement_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.autoframing.speed_movement", autoframing_speed_movement_value);
                }
                if (isInteger(autoframing_max_move_step_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.autoframing.max_move_step", autoframing_max_move_step_value);
                }
                if (isInteger(autoframing_max_crop_ratio_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.autoframing.max_crop_ratio", autoframing_max_crop_ratio_value);
                }
                if (isFloat(postprocess_yolov5_conf_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.postprocess_yolov5.conf_threshold", postprocess_yolov5_conf_threshold_value);
                }
                if (isFloat(postprocess_yolov5_conf_threshold_person_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.postprocess_yolov5.conf_threshold_person", postprocess_yolov5_conf_threshold_person_value);
                }
                if (isInteger(postprocess_yolov5_max_num_objects_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.postprocess_yolov5.max_num_objects", postprocess_yolov5_max_num_objects_value);
                }
                if (isInteger(postprocess_yolov5_margins_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.postprocess_yolov5.margins", postprocess_yolov5_margins_value);
                }
                if (isInteger(autoframing_presenter_filter_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.autoframing.filter_size", autoframing_presenter_filter_size_value);
                }
                if (isInteger(autoframing_presenter_filter_average_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.autoframing.filter_average_size", autoframing_presenter_filter_average_size_value);
                }
                if (isInteger(autoframing_presenter_pos_static_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.autoframing.pos_static_threshold", autoframing_presenter_pos_static_threshold_value);
                }
                if (isInteger(autoframing_presenter_size_static_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.autoframing.size_static_threshold", autoframing_presenter_size_static_threshold_value);
                }
                set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.muxer.priority", presenter_muxer_priority_value);
                if (isInteger(tracking_roi_release_cnt_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.tracking.roi_release_cnt", tracking_roi_release_cnt_value);
                }
                if (isFloat(tracking_intersect_coef_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.tracking.intersect_coef", tracking_intersect_coef_value);
                }
                if (isInteger(tracking_new_entry_list_depth_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.tracking.new_entry_list_depth", tracking_new_entry_list_depth_value);
                }
                if (isInteger(tracking_new_entry_timeout_ms_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.tracking.new_entry_timeout_ms", tracking_new_entry_timeout_ms_value);
                }
                if (isFloat(nms_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.stabilization.nms_threshold", nms_threshold_value);
                }
                set.invoke(SystemProperties, "persist.vendor.ai-director.presenter.postprocess_yolov5.output_layers", output_layers_value);
            } catch (IllegalArgumentException iAE) {
                throw iAE;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private void restoreAppPreference() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        PreferenceManager.setDefaultValues(getActivity(), R.xml.root_preferences, true);
        getPreferenceScreen().removeAll();
        onCreatePreferences(null, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean isInteger(String s) {
        try {
            Integer.parseUnsignedInt(s);
        } catch (NumberFormatException e) {
            Toast.makeText(requireActivity().getApplicationContext(),
                    "Please enter an integer value", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean isFloat(String s) {
        try {
            Float.parseFloat(s);
        } catch (NumberFormatException e) {
            Toast.makeText(requireActivity().getApplicationContext(),
                    "Please enter a Float value", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}