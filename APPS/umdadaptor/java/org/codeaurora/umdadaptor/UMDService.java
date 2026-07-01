/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package org.codeaurora.umdadaptor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static android.content.pm.ServiceInfo.*;
import android.os.ServiceManager;

public class UMDService extends Service {
    private static final String TAG = "UMDService";
    private AudioCapture mAudioCapture = null;
    private AudioPlayback mAudioPlayback = null;
    private boolean mThreadActive = true;
    private static String mUSBConfig;
    private static String mMode;
    private static final String USBCONFIG_PROP = "sys.usb.config";
    private static final String USBCONFIG_DEFAULT = "diag,uvc,adb";
    private static final String UVC = "uvc";
    private static final String UAC = "uac2";
    private static final String UVC_UAC = "uvc,uac";
    private static final String NOTIFICATION_CHANNEL_ID = "Foreground service";
    private static final int NOTIFICATION_ID = 1;
    private UMDInterface umdServer = null;

    private void HandleUEvent(int status) {
        UMDInterface.AudioState state = umdServer.setAudioState(status);
        switch (state) {
            case _invalid:
                break;
            case _playback:
                mAudioCapture.stop();
                mAudioPlayback.start();
                break;
            case _capture:
                mAudioCapture.start();
                mAudioPlayback.stop();
                break;
            case _playback_capture:
                mAudioCapture.start();
                mAudioPlayback.start();
                break;
            case _paused:
                mAudioCapture.stop();
                mAudioPlayback.stop();
                break;
        }
    }

    private String getUSBMode(String usbConfig) {
        String mode = null;
        Set<String> configs = new HashSet<>(Arrays.asList(usbConfig.split(",")));
        boolean supportsUAC = configs.contains(UAC);
        boolean supportsUVC = configs.contains(UVC) ||
            configs.stream().anyMatch(config -> config.matches(".*\\d+(?=xuvc).*"));

        if (supportsUAC && supportsUVC)
            mode = UVC_UAC;
        else if (supportsUAC)
            mode = UAC;
        else if (supportsUVC)
            mode = UVC;
        Log.i(TAG, String.format("mode is %s", mode));
        return mode;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG,"onStartCommand");
        umdServer = UMDProvider.createUMDImpl();
        mUSBConfig = SystemProperties.get(USBCONFIG_PROP, USBCONFIG_DEFAULT);
        mMode = getUSBMode(mUSBConfig);

        if(mMode.equals(UAC) || mMode.equals(UVC_UAC)) {

            umdServer.initUAC();
            mAudioCapture = new AudioCapture(getApplicationContext(), umdServer);
            mAudioPlayback = new AudioPlayback(getApplicationContext(), umdServer);

            umdServer.setAudioCapture(mAudioCapture);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mThreadActive) {
                        try {
                            umdServer.getConditionQueue().take();
                            HandleUEvent(umdServer.getEventQueue().take());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        if(mMode.equals(UVC) || mMode.equals(UVC_UAC)) {
            umdServer.initUVC();
        }

        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background);
        startForeground(NOTIFICATION_ID, notification.build(), FOREGROUND_SERVICE_TYPE_MICROPHONE);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
            if (mMode.equals(UAC) || mMode.equals(UVC_UAC)) {
                mThreadActive = false;
                umdServer.deinitUAC();
            }
            if (mMode.equals(UVC) || mMode.equals(UVC_UAC))
                umdServer.deinitUVC();
        super.onDestroy();
    }
}

