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
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
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
import vendor.qti.hardware.umdservice.IUMDAdaptor;
import vendor.qti.hardware.umd.V1_0.*;

public class UMDService extends Service {
    private static final String TAG = "UMDService";
    ArrayBlockingQueue<Integer> mEventQueue = new ArrayBlockingQueue<>(8);
    LinkedBlockingQueue<Boolean> mConditionQueue = new LinkedBlockingQueue<Boolean>();
    private vendor.qti.hardware.umd.V1_0.IUMDAdaptor mUMDAdaptorHidl = null;
    private vendor.qti.hardware.umdservice.IUMDAdaptor mUMDAdaptorAidl = null;
    //private IUMDAdaptor mServer = null;
    private AudioCapture mAudioCapture = null;
    private AudioPlayback mAudioPlayback = null;
    private boolean mThreadActive = true;
    private static String mUSBConfig;
    private static String mMode;
    private boolean isAidl = false;
    private boolean isHidl = false;
    private static final String USBCONFIG_PROP = "sys.usb.config";
    private static final String USBCONFIG_DEFAULT = "diag,uvc,adb";
    private static final String UVC = "uvc";
    private static final String UAC = "uac2";
    private static final String UVC_UAC = "uvc,uac";
    private static final String NOTIFICATION_CHANNEL_ID = "Foreground service";
    private static final int NOTIFICATION_ID = 1;

    private vendor.qti.hardware.umd.V1_0.IUMDAdaptorCallback.Stub mHalHidlCallback = new vendor.qti.hardware.umd.V1_0.IUMDAdaptorCallback.Stub() {
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

    private vendor.qti.hardware.umdservice.IUMDAdaptorCallback.Stub mHalAidlCallback = new vendor.qti.hardware.umdservice.IUMDAdaptorCallback.Stub() {
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
        public int getInterfaceVersion() {
            return 0;
        }
        @Override
        public String getInterfaceHash() {
            return "";
        }
        @Override
        public int onAudioBufferReceive(byte[] data) {
            int offset = 0;
            ArrayList<Byte> byteArray = new ArrayList<Byte>(data.length);
            for (int i = 0; i < data.length; i++) {
                byteArray.add(Byte.valueOf(data[offset + i]));
            }
            mAudioCapture.audioCapture(byteArray);
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
            String ISERVICE_INTERFACE = vendor.qti.hardware.umdservice.IUMDAdaptor.DESCRIPTOR + "/default";
            if (ServiceManager.isDeclared(ISERVICE_INTERFACE)){
                IBinder binder = Binder.allowBlocking(ServiceManager.waitForDeclaredService(ISERVICE_INTERFACE));
                mUMDAdaptorAidl = vendor.qti.hardware.umdservice.IUMDAdaptor.Stub.asInterface(binder);
                isAidl=true;
            } else {
                mUMDAdaptorHidl = vendor.qti.hardware.umd.V1_0.IUMDAdaptor.getService(true /* retry */);
                isHidl=true;
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception");
        }
        mUSBConfig = SystemProperties.get(USBCONFIG_PROP, USBCONFIG_DEFAULT);
        mMode = getUSBMode(mUSBConfig);

        if(mMode.equals(UAC) || mMode.equals(UVC_UAC)) {
            try {
                if (isAidl && mUMDAdaptorAidl != null ) {
                    mUMDAdaptorAidl.initUAC(mHalAidlCallback);
                    mAudioCapture = new AudioCapture(getApplicationContext(), mUMDAdaptorAidl);
                    mAudioPlayback = new AudioPlayback(getApplicationContext(), mUMDAdaptorAidl);
                } else if (isHidl && mUMDAdaptorHidl != null ) {
                    mUMDAdaptorHidl.initUAC(mHalHidlCallback);
                    mAudioCapture = new AudioCapture(getApplicationContext(), mUMDAdaptorHidl);
                    mAudioPlayback = new AudioPlayback(getApplicationContext(), mUMDAdaptorHidl);
                } else {
                    Log.e(TAG, "Failed to obtain UMDAdaptorService");
                }
            } catch (RemoteException e) {
                Log.i(TAG, "Remote Exception");
            }
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
                if (isAidl && mUMDAdaptorAidl != null ) {
                    mUMDAdaptorAidl.initUVC();
                } else if (isHidl && mUMDAdaptorHidl != null ) {
                    mUMDAdaptorHidl.initUVC();
                } else {
                    Log.e(TAG, "Failed to obtain UMDAdaptorService");
                }
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
                if (isAidl && mUMDAdaptorAidl != null ) {
                    mUMDAdaptorAidl.deInitUAC();
                } else if (isHidl && mUMDAdaptorHidl != null ) {
                    mUMDAdaptorHidl.deInitUAC();
                } else {
                    Log.e(TAG, "Failed to obtain UMDAdaptorService");
                }
            }
            if (mMode.equals(UVC) || mMode.equals(UVC_UAC)) {
                if (isAidl && mUMDAdaptorAidl != null ) {
                    mUMDAdaptorAidl.deInitUVC();
                } else if (isHidl && mUMDAdaptorHidl != null ) {
                    mUMDAdaptorHidl.deInitUVC();
                } else {
                    Log.e(TAG, "Failed to obtain UMDAdaptorService");
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception");
        }
        super.onDestroy();
    }
}

