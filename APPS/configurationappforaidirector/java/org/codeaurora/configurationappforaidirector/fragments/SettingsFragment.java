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
        ListPreference doa_dump_csv = mPrefScreen.findPreference("doa_dump_csv");
        ListPreference beamforming_adoa_logging = mPrefScreen.findPreference("beamforming_adoa_logging");
        EditTextPreference autoframing_filter_size = mPrefScreen.findPreference("autoframing_filter_size");
        EditTextPreference autoframing_pos_threshold = mPrefScreen.findPreference("autoframing_pos_threshold");
        EditTextPreference autoframing_size_threshold = mPrefScreen.findPreference("autoframing_size_threshold");
        EditTextPreference autoframing_speed_movement = mPrefScreen.findPreference("autoframing_speed_movement");
        EditTextPreference autoframing_max_move_step = mPrefScreen.findPreference("autoframing_max_move_step");
        EditTextPreference autoframing_max_crop_ratio = mPrefScreen.findPreference("autoframing_max_crop_ratio");
        EditTextPreference postprocess_yolov5_conf_threshold = mPrefScreen.findPreference("postprocess_yolov5_conf_threshold");
        EditTextPreference postprocess_yolov5_conf_threshold_person = mPrefScreen.findPreference("postprocess_yolov5_conf_threshold_person");
        EditTextPreference postprocess_yolov5_max_num_objects = mPrefScreen.findPreference("postprocess_yolov5_max_num_objects");
        EditTextPreference postprocess_yolov5_margins = mPrefScreen.findPreference("postprocess_yolov5_margins");
        ListPreference dsp_aip_mode = mPrefScreen.findPreference("dsp_aip_mode");
        EditTextPreference filter_box_tracking_outside_percent = mPrefScreen.findPreference("filter_box_tracking_outside_percent");
        EditTextPreference filter_default_detection_audio_doa_list_size = mPrefScreen.findPreference("filter_default_detection_audio_doa_list_size");
        EditTextPreference filter_default_history_filter_depth = mPrefScreen.findPreference("filter_default_history_filter_depth");
        EditTextPreference filter_default_signal_pos_list_size = mPrefScreen.findPreference("filter_default_signal_pos_list_size");
        EditTextPreference filter_detection_audio_doa_threshold_high = mPrefScreen.findPreference("filter_detection_audio_doa_threshold_high");
        EditTextPreference filter_detection_audio_doa_threshold_low = mPrefScreen.findPreference("filter_detection_audio_doa_threshold_low");
        EditTextPreference filter_doa_detected_delay = mPrefScreen.findPreference("filter_doa_detected_delay");
        EditTextPreference filter_doa_change_delay = mPrefScreen.findPreference("filter_doa_change_delay");
        EditTextPreference doa_histogram_res = mPrefScreen.findPreference("doa_histogram_res");
        EditTextPreference doa_histogram_thres = mPrefScreen.findPreference("doa_histogram_thres");
        EditTextPreference sample_interval = mPrefScreen.findPreference("sample_interval");
        ListPreference filter_type = mPrefScreen.findPreference("filter_type");
        ListPreference average_fov_filter_en = mPrefScreen.findPreference("average_fov_filter_en");
        EditTextPreference speech_probability_threshold = mPrefScreen.findPreference("speech_probability_threshold");
        EditTextPreference tracking_roi_release_cnt = mPrefScreen.findPreference("tracking_roi_release_cnt");
        EditTextPreference autoframing_roi_release_cnt = mPrefScreen.findPreference("autoframing_roi_release_cnt");
        EditTextPreference tracking_intersect_coef = mPrefScreen.findPreference("tracking_intersect_coef");

        String framing_mode_value = pref.getString("framing_mode", "SpeakerFraming");
        String framing_mode_value_final = "speaker";
        if (framing_mode_value.equals("SpeakerFraming")) {
            framing_mode_value_final = "speaker";
        }
        if (framing_mode_value.equals("GroupFraming")) {
            framing_mode_value_final = "group";
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
        String doa_dump_csv_value = pref.getString("doa_dump_csv", "Disable");
        String doa_dump_csv_value_final = "0";
        if (doa_dump_csv_value.equals("Enable")) {
            doa_dump_csv_value_final = "1";
        } else {
            doa_dump_csv_value_final = "0";
        }
        String beamforming_adoa_logging_value = pref.getString("beamforming_adoa_logging", "Disable");
        String beamforming_adoa_logging_value_final = "0";
        if (beamforming_adoa_logging_value.equals("Enable")) {
            beamforming_adoa_logging_value_final = "1";
        } else {
            beamforming_adoa_logging_value_final = "0";
        }

        String dsp_aip_mode_value = pref.getString("dsp_aip_mode", "aip");
        String filter_type_value = pref.getString("filter_type", "0");
        String average_fov_filter_en_value = pref.getString("average_fov_filter_en", "1");
        String autoframing_filter_size_value = pref.getString("autoframing_filter_size", "25");
        if (autoframing_filter_size_value.equals("")) {
            autoframing_filter_size.setText(getResources().getString(R.string.autoframing_filter_size_hint));
        }
        String autoframing_pos_threshold_value = pref.getString("autoframing_pos_threshold", "6");
        if (autoframing_pos_threshold_value.equals("")) {
            autoframing_pos_threshold.setText(getResources().getString(R.string.autoframing_pos_threshold_hint));
        }
        String autoframing_size_threshold_value = pref.getString("autoframing_size_threshold", "6");
        if (autoframing_size_threshold_value.equals("")) {
            autoframing_size_threshold.setText(getResources().getString(R.string.autoframing_size_threshold_hint));
        }
        String autoframing_speed_movement_value = pref.getString("autoframing_speed_movement", "40");
        if (autoframing_speed_movement_value.equals("")) {
            autoframing_speed_movement.setText(getResources().getString(R.string.autoframing_speed_movement_hint));
        }
        String autoframing_max_move_step_value = pref.getString("autoframing_max_move_step", "20");
        if (autoframing_max_move_step_value.equals("")) {
            autoframing_max_move_step.setText(getResources().getString(R.string.autoframing_max_move_step_hint));
        }
        String autoframing_max_crop_ratio_value = pref.getString("autoframing_max_crop_ratio", "20");
        if (autoframing_max_crop_ratio_value.equals("")) {
            autoframing_max_crop_ratio.setText(getResources().getString(R.string.autoframing_max_crop_ratio_hint));
        }
        String postprocess_yolov5_conf_threshold_value = pref.getString("postprocess_yolov5_conf_threshold", "0.7");
        if (postprocess_yolov5_conf_threshold_value.equals("")) {
            postprocess_yolov5_conf_threshold.setText(getResources().getString(R.string.postprocess_yolov5_conf_threshold_hint));
        }
        String postprocess_yolov5_conf_threshold_person_value = pref.getString("postprocess_yolov5_conf_threshold_person", "0.4");
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
        String filter_box_tracking_outside_percent_value = pref.getString("filter_box_tracking_outside_percent", "0");
        if (filter_box_tracking_outside_percent_value.equals("")) {
            filter_box_tracking_outside_percent.setText(getResources().getString(R.string.filter_box_tracking_outside_percent_hint));
        }
        String filter_default_detection_audio_doa_list_size_value = pref.getString("filter_default_detection_audio_doa_list_size", "60");
        if (filter_default_detection_audio_doa_list_size_value.equals("")) {
            filter_default_detection_audio_doa_list_size.setText(getResources().getString(R.string.filter_default_detection_audio_doa_list_size_hint));
        }
        String filter_default_history_filter_depth_value = pref.getString("filter_default_history_filter_depth", "60");
        if (filter_default_history_filter_depth_value.equals("")) {
            filter_default_history_filter_depth.setText(getResources().getString(R.string.filter_default_history_filter_depth_hint));
        }
        String filter_default_signal_pos_list_size_value = pref.getString("filter_default_signal_pos_list_size", "60");
        if (filter_default_signal_pos_list_size_value.equals("")) {
            filter_default_signal_pos_list_size.setText(getResources().getString(R.string.filter_default_signal_pos_list_size_hint));
        }
        String filter_detection_audio_doa_threshold_high_value = pref.getString("filter_detection_audio_doa_threshold_high", "28.0");
        if (filter_detection_audio_doa_threshold_high_value.equals("")) {
            filter_detection_audio_doa_threshold_high.setText(getResources().getString(R.string.filter_detection_audio_doa_threshold_high_hint));
        }
        String filter_detection_audio_doa_threshold_low_value = pref.getString("filter_detection_audio_doa_threshold_low", "15.0");
        if (filter_detection_audio_doa_threshold_low_value.equals("")) {
            filter_detection_audio_doa_threshold_low.setText(getResources().getString(R.string.filter_detection_audio_doa_threshold_low_hint));
        }
        String filter_doa_detected_delay_value = pref.getString("filter_doa_detected_delay", "100");
        if (filter_doa_detected_delay_value.equals("")) {
            filter_doa_detected_delay.setText(getResources().getString(R.string.filter_doa_detected_delay_hint));
        }
        String filter_doa_change_delay_value = pref.getString("filter_doa_change_delay", "50");
        if (filter_doa_change_delay_value.equals("")) {
            filter_doa_change_delay.setText(getResources().getString(R.string.filter_doa_change_delay_hint));
        }
        String doa_histogram_res_value = pref.getString("doa_histogram_res", "20");
        if (doa_histogram_res_value.equals("")) {
            doa_histogram_res.setText(getResources().getString(R.string.doa_histogram_res_hint));
        }
        String doa_histogram_thres_value = pref.getString("doa_histogram_thres", "15");
        if (doa_histogram_thres_value.equals("")) {
            doa_histogram_thres.setText(getResources().getString(R.string.doa_histogram_thres_hint));
        }
        String sample_interval_value = pref.getString("sample_interval", "20");
        if (sample_interval_value.equals("")) {
            sample_interval.setText(getResources().getString(R.string.sample_interval_hint));
        }
        String speech_probability_threshold_value = pref.getString("speech_probability_threshold", "0.7");
        if (speech_probability_threshold_value.equals("")) {
            speech_probability_threshold.setText(getResources().getString(R.string.speech_probability_threshold_hint));
        }
        String tracking_roi_release_cnt_value = pref.getString("tracking_roi_release_cnt", "120");
        if (tracking_roi_release_cnt_value.equals("")) {
            tracking_roi_release_cnt.setText(getResources().getString(R.string.tracking_roi_release_cnt_hint));
        }
        String autoframing_roi_release_cnt_value = pref.getString("autoframing_roi_release_cnt", "120");
        if (autoframing_roi_release_cnt_value.equals("")) {
            autoframing_roi_release_cnt.setText(getResources().getString(R.string.autoframing_roi_release_cnt_hint));
        }
        String tracking_intersect_coef_value = pref.getString("tracking_intersect_coef", "0.2");
        if (tracking_intersect_coef_value.equals("")) {
            tracking_intersect_coef.setText(getResources().getString(R.string.tracking_intersect_coef_hint));
        }

        if (!gpu_transform_draw_debug.isVisible()) {
            gpu_transform_draw_debug.setVisible(true);
        }
        if (!gpu_transform_enable_crop.isVisible()) {
            gpu_transform_enable_crop.setVisible(true);
        }
        if (!doa_dump_csv.isVisible()) {
            doa_dump_csv.setVisible(true);
        }
        if (!beamforming_adoa_logging.isVisible()) {
            beamforming_adoa_logging.setVisible(true);
        }
        if (!autoframing_filter_size.isVisible()) {
            autoframing_filter_size.setVisible(true);
        }
        if (!autoframing_pos_threshold.isVisible()) {
            autoframing_pos_threshold.setVisible(true);
        }
        if (!autoframing_size_threshold.isVisible()) {
            autoframing_size_threshold.setVisible(true);
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
        if (!dsp_aip_mode.isVisible()) {
            dsp_aip_mode.setVisible(true);
        }
        if (filter_box_tracking_outside_percent.isVisible()) {
            filter_box_tracking_outside_percent.setVisible(false);
        }
        if (filter_default_detection_audio_doa_list_size.isVisible()) {
            filter_default_detection_audio_doa_list_size.setVisible(false);
        }
        if (filter_default_history_filter_depth.isVisible()) {
            filter_default_history_filter_depth.setVisible(false);
        }
        if (filter_default_signal_pos_list_size.isVisible()) {
            filter_default_signal_pos_list_size.setVisible(false);
        }
        if (filter_detection_audio_doa_threshold_high.isVisible()) {
            filter_detection_audio_doa_threshold_high.setVisible(false);
        }
        if (filter_detection_audio_doa_threshold_low.isVisible()) {
            filter_detection_audio_doa_threshold_low.setVisible(false);
        }
        if (filter_doa_detected_delay.isVisible()) {
            filter_doa_detected_delay.setVisible(false);
        }
        if (filter_doa_change_delay.isVisible()) {
            filter_doa_change_delay.setVisible(false);
        }
        if (doa_histogram_res.isVisible()) {
            doa_histogram_res.setVisible(false);
        }
        if (doa_histogram_thres.isVisible()) {
            doa_histogram_thres.setVisible(false);
        }
        if (sample_interval.isVisible()) {
            sample_interval.setVisible(false);
        }
        if (filter_type.isVisible()) {
            filter_type.setVisible(false);
        }
        if (average_fov_filter_en.isVisible()) {
            average_fov_filter_en.setVisible(false);
        }
        if (speech_probability_threshold.isVisible()) {
            speech_probability_threshold.setVisible(false);
        }
        if (tracking_roi_release_cnt.isVisible()) {
            tracking_roi_release_cnt.setVisible(false);
        }
        if (autoframing_roi_release_cnt.isVisible()) {
            autoframing_roi_release_cnt.setVisible(false);
        }
        if (tracking_intersect_coef.isVisible()) {
            tracking_intersect_coef.setVisible(false);
        }

        try {
            @SuppressWarnings("rawtypes")
            Class SystemProperties = Class.forName("android.os.SystemProperties");
            Method set = SystemProperties.getMethod("set", String.class, String.class);
            set.invoke(SystemProperties, "persist.vendor.ai-director.framing_mode", framing_mode_value_final);
            set.invoke(SystemProperties, "persist.vendor.ai-director.gpu_transform.draw_debug", gpu_transform_draw_debug_value_final);
            set.invoke(SystemProperties, "persist.vendor.ai-director.gpu_transform.enable_crop", gpu_transform_enable_crop_value_final);
            set.invoke(SystemProperties, "persist.vendor.ai-director.doa_dump_csv", doa_dump_csv_value_final);
            set.invoke(SystemProperties, "persist.vendor.ai-director.beamforming.adoa.logging", beamforming_adoa_logging_value_final);
            if (isInteger(autoframing_filter_size_value)) {
                set.invoke(SystemProperties, "persist.vendor.ai-director.autoframing.filter_size", autoframing_filter_size_value);
            }
            if (isInteger(autoframing_pos_threshold_value)) {
                set.invoke(SystemProperties, "persist.vendor.ai-director.autoframing.pos_threshold", autoframing_pos_threshold_value);
            }
            if (isInteger(autoframing_size_threshold_value)) {
                set.invoke(SystemProperties, "persist.vendor.ai-director.autoframing.size_threshold", autoframing_size_threshold_value);
            }
            if (isInteger(autoframing_speed_movement_value)) {
                set.invoke(SystemProperties, "persist.vendor.ai-director.autoframing.speed_movement", autoframing_speed_movement_value);
            }
            if (isInteger(autoframing_max_move_step_value)) {
                set.invoke(SystemProperties, "persist.vendor.ai-director.autoframing.max_move_step", autoframing_max_move_step_value);
            }
            if (isInteger(autoframing_max_crop_ratio_value)) {
                set.invoke(SystemProperties, "persist.vendor.ai-director.autoframing.max_crop_ratio", autoframing_max_crop_ratio_value);
            }
            if (isFloat(postprocess_yolov5_conf_threshold_value)) {
                set.invoke(SystemProperties, "persist.vendor.ai-director.postprocess_yolov5.conf_threshold", postprocess_yolov5_conf_threshold_value);
            }
            if (isFloat(postprocess_yolov5_conf_threshold_person_value)) {
                set.invoke(SystemProperties, "persist.vendor.ai-director.postprocess_yolov5.conf_threshold_person", postprocess_yolov5_conf_threshold_person_value);
            }
            if (isInteger(postprocess_yolov5_max_num_objects_value)) {
                set.invoke(SystemProperties, "persist.vendor.ai-director.postprocess_yolov5.max_num_objects", postprocess_yolov5_max_num_objects_value);
            }
            if (isInteger(postprocess_yolov5_margins_value)) {
                set.invoke(SystemProperties, "persist.vendor.ai-director.postprocess_yolov5.margins", postprocess_yolov5_margins_value);
            }
            set.invoke(SystemProperties, "persist.vendor.ai-director.dsp-aip-mode", dsp_aip_mode_value);
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (framing_mode.getValue().equals("SpeakerFraming")) {
            if (!filter_box_tracking_outside_percent.isVisible()) {
                filter_box_tracking_outside_percent.setVisible(true);
            }
            if (!filter_default_detection_audio_doa_list_size.isVisible()) {
                filter_default_detection_audio_doa_list_size.setVisible(true);
            }
            if (!filter_default_history_filter_depth.isVisible()) {
                filter_default_history_filter_depth.setVisible(true);
            }
            if (!filter_default_signal_pos_list_size.isVisible()) {
                filter_default_signal_pos_list_size.setVisible(true);
            }
            if (!filter_detection_audio_doa_threshold_high.isVisible()) {
                filter_detection_audio_doa_threshold_high.setVisible(true);
            }
            if (!filter_detection_audio_doa_threshold_low.isVisible()) {
                filter_detection_audio_doa_threshold_low.setVisible(true);
            }
            if (!filter_doa_detected_delay.isVisible()) {
                filter_doa_detected_delay.setVisible(true);
            }
            if (!filter_doa_change_delay.isVisible()) {
                filter_doa_change_delay.setVisible(true);
            }
            if (!doa_histogram_res.isVisible()) {
                doa_histogram_res.setVisible(true);
            }
            if (!doa_histogram_thres.isVisible()) {
                doa_histogram_thres.setVisible(true);
            }
            if (!sample_interval.isVisible()) {
                sample_interval.setVisible(true);
            }
            if (!filter_type.isVisible()) {
                filter_type.setVisible(true);
            }
            if (!average_fov_filter_en.isVisible()) {
                average_fov_filter_en.setVisible(true);
            }
            if (!speech_probability_threshold.isVisible()) {
                speech_probability_threshold.setVisible(true);
            }
            if (tracking_roi_release_cnt.isVisible()) {
                tracking_roi_release_cnt.setVisible(false);
            }
            if (autoframing_roi_release_cnt.isVisible()) {
                autoframing_roi_release_cnt.setVisible(false);
            }
            if (tracking_intersect_coef.isVisible()) {
                tracking_intersect_coef.setVisible(false);
            }

            try {
                @SuppressWarnings("rawtypes")
                Class SystemProperties = Class.forName("android.os.SystemProperties");
                Method set = SystemProperties.getMethod("set", String.class, String.class);
                if (isInteger(filter_box_tracking_outside_percent_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.filter_box_tracking_outside_percent", filter_box_tracking_outside_percent_value);
                }
                if (isInteger(filter_default_detection_audio_doa_list_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.filter_default_detection_audio_doa_list_size", filter_default_detection_audio_doa_list_size_value);
                }
                if (isInteger(filter_default_history_filter_depth_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.filter_default_history_filter_depth", filter_default_history_filter_depth_value);
                }
                if (isInteger(filter_default_signal_pos_list_size_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.filter_default_signal_pos_list_size", filter_default_signal_pos_list_size_value);
                }
                if (isFloat(filter_detection_audio_doa_threshold_high_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.filter_detection_audio_doa_threshold_high", filter_detection_audio_doa_threshold_high_value);
                }
                if (isFloat(filter_detection_audio_doa_threshold_low_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.filter_detection_audio_doa_threshold_low", filter_detection_audio_doa_threshold_low_value);
                }
                if (isInteger(filter_doa_detected_delay_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.filter_doa_detected_delay", filter_doa_detected_delay_value);
                }
                if (isInteger(filter_doa_change_delay_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.filter_doa_change_delay_value", filter_doa_change_delay_value);
                }
                if (isInteger(doa_histogram_res_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.doa_histogram_res", doa_histogram_res_value);
                }
                if (isInteger(doa_histogram_thres_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.doa_histogram_thres", doa_histogram_thres_value);
                }
                if (isInteger(sample_interval_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.beamforming.adoa.sample_interval", sample_interval_value);
                }
                set.invoke(SystemProperties, "persist.vendor.ai-director.filter_type", filter_type_value);
                set.invoke(SystemProperties, "persist.vendor.ai-director.average_fov_filter_en", average_fov_filter_en_value);
                if (isFloat(speech_probability_threshold_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.speech_probability_threshold", speech_probability_threshold_value);
                }
            } catch (IllegalArgumentException iAE) {
                throw iAE;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (framing_mode.getValue().equals("PeopleFraming")) {
            if (filter_box_tracking_outside_percent.isVisible()) {
                filter_box_tracking_outside_percent.setVisible(false);
            }
            if (filter_default_detection_audio_doa_list_size.isVisible()) {
                filter_default_detection_audio_doa_list_size.setVisible(false);
            }
            if (filter_default_history_filter_depth.isVisible()) {
                filter_default_history_filter_depth.setVisible(false);
            }
            if (filter_default_signal_pos_list_size.isVisible()) {
                filter_default_signal_pos_list_size.setVisible(false);
            }
            if (filter_detection_audio_doa_threshold_high.isVisible()) {
                filter_detection_audio_doa_threshold_high.setVisible(false);
            }
            if (filter_detection_audio_doa_threshold_low.isVisible()) {
                filter_detection_audio_doa_threshold_low.setVisible(false);
            }
            if (filter_doa_detected_delay.isVisible()) {
                filter_doa_detected_delay.setVisible(false);
            }
            if (filter_doa_change_delay.isVisible()) {
                filter_doa_change_delay.setVisible(false);
            }
            if (doa_histogram_res.isVisible()) {
                doa_histogram_res.setVisible(false);
            }
            if (doa_histogram_thres.isVisible()) {
                doa_histogram_thres.setVisible(false);
            }
            if (sample_interval.isVisible()) {
                sample_interval.setVisible(false);
            }
            if (filter_type.isVisible()) {
                filter_type.setVisible(false);
            }
            if (average_fov_filter_en.isVisible()) {
                average_fov_filter_en.setVisible(false);
            }
            if (speech_probability_threshold.isVisible()) {
                speech_probability_threshold.setVisible(false);
            }
            if (!tracking_roi_release_cnt.isVisible()) {
                tracking_roi_release_cnt.setVisible(true);
            }
            if (!autoframing_roi_release_cnt.isVisible()) {
                autoframing_roi_release_cnt.setVisible(true);
            }
            if (!tracking_intersect_coef.isVisible()) {
                tracking_intersect_coef.setVisible(true);
            }

            try {
                @SuppressWarnings("rawtypes")
                Class SystemProperties = Class.forName("android.os.SystemProperties");
                Method set = SystemProperties.getMethod("set", String.class, String.class);
                if (isInteger(tracking_roi_release_cnt_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.tracking.roi_release_cnt", tracking_roi_release_cnt_value);
                }
                if (isInteger(autoframing_roi_release_cnt_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.autoframing.roi_release_cnt", autoframing_roi_release_cnt_value);
                }
                if (isFloat(tracking_intersect_coef_value)) {
                    set.invoke(SystemProperties, "persist.vendor.ai-director.tracking.intersect_coef", tracking_intersect_coef_value);
                }
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
        // only got here if we didn't return false
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
        // only got here if we didn't return false
        return true;
    }


}