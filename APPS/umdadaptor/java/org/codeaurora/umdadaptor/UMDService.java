/*
 * Copyright (c) 2023-2024 Qualcomm Innovation Center, Inc. All rights reserved.
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
import vendor.qti.hardware.umd.V1_0.*;

public class UMDService extends Service {
    private static final String TAG = "UMDService";
    ArrayBlockingQueue<Integer> mEventQueue = new ArrayBlockingQueue<>(8);
    LinkedBlockingQueue<Boolean> mConditionQueue = new LinkedBlockingQueue<Boolean>();
    private IUMDAdaptor mServer = null;
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
    private IUMDAdaptorCallback.Stub mHalCallback = new IUMDAdaptorCallback.Stub() {
        @Override
        public void onAudioUevent(int status) {
            try {
                mEventQueue.put(status);
                mConditionQueue.put(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int onAudioBufferReceive(ArrayList<Byte> data) {
            mAudioCapture.audioCapture(data);
            return 0;
        }
    };

    private void HandleUEvent(int status) {
        switch (status) {
            case AudioStatus.AUDIO_STATE_INVALID:
                break;
            case AudioStatus.AUDIO_STATE_PLAYBACK:
                mAudioCapture.stop();
                mAudioPlayback.start();
                break;
            case AudioStatus.AUDIO_STATE_CAPTURE:
                mAudioCapture.start();
                mAudioPlayback.stop();
                break;
            case AudioStatus.AUDIO_STATE_PLAYBACK_CAPTURE:
                mAudioCapture.start();
                mAudioPlayback.start();
                break;
            case AudioStatus.AUDIO_STATE_PAUSED:
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
        try {
            mServer = IUMDAdaptor.getService(true /* retry */);
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception");
        }
        mUSBConfig = SystemProperties.get(USBCONFIG_PROP, USBCONFIG_DEFAULT);
        mMode = getUSBMode(mUSBConfig);

        if(mMode.equals(UAC) || mMode.equals(UVC_UAC)) {
            try {
                mServer.initUAC(mHalCallback);
            } catch (RemoteException e) {
                Log.i(TAG, "Remote Exception");
            }
            mAudioCapture = new AudioCapture(getApplicationContext(), mServer);
            mAudioPlayback = new AudioPlayback(getApplicationContext(), mServer);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mThreadActive) {
                        try {
                            mConditionQueue.take();
                            HandleUEvent(mEventQueue.take());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        if (mMode.equals(UVC) || mMode.equals(UVC_UAC)) {
            try {
                mServer.initUVC();
            } catch (RemoteException e) {
                Log.i(TAG, "Remote Exception");
            }
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
        try {
            if (mMode.equals(UAC) || mMode.equals(UVC_UAC)) {
                mThreadActive = false;
                mServer.deInitUAC();
            }
            if (mMode.equals(UVC) || mMode.equals(UVC_UAC))
                mServer.deInitUVC();
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception");
        }
        super.onDestroy();
    }
}

