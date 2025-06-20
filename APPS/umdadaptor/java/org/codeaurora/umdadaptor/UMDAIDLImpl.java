/*
 *Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 *SPDX-License-Identifier: BSD-3-Clause-Clear
 */
package org.codeaurora.umdadaptor;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import vendor.qti.hardware.umd_aidl.IUMDAdaptor;
import vendor.qti.hardware.umd_aidl.IUMDAdaptorCallback;

public class UMDAIDLImpl implements UMDInterface {
    private static final String TAG = "UMDAIDLImpl";

    private final IUMDAdaptor mAudio;
    private final IUMDAdaptor mCamera;

    private final ArrayBlockingQueue<Integer> mEventQueue = new ArrayBlockingQueue<>(8);
    private final LinkedBlockingQueue<Boolean> mConditionQueue = new LinkedBlockingQueue<>();
    private AudioCapture mAudioCapture;

    public UMDAIDLImpl(IBinder audioBinder, IBinder cameraBinder) {
        this.mAudio  = (audioBinder  != null) ? IUMDAdaptor.Stub.asInterface(audioBinder)  : null;
        this.mCamera = (cameraBinder != null) ? IUMDAdaptor.Stub.asInterface(cameraBinder) : null;
        Log.d(TAG, "created binders: audio=" + (mAudio != null) + " camera=" + (mCamera != null));
    }

    private final IUMDAdaptorCallback mAidlCb = new IUMDAdaptorCallback.Stub() {
        @Override
        public void onAudioUevent(int status) {
            Log.d(TAG, "onAudioUevent (AIDL)");
            try {
                mEventQueue.put(status);
                mConditionQueue.put(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public int onAudioBufferReceive(byte[] data) {
            Log.d(TAG, "onAudioBufferReceive (AIDL)");
            if (mAudioCapture != null && data != null) {
                ArrayList<Byte> list = new ArrayList<>(data.length);
                for (byte b : data) list.add(b);
                mAudioCapture.audioCapture(list);
            }
            return 0;
        }

        @Override
        public String getInterfaceHash() {
            return IUMDAdaptor.HASH;
        }

        @Override
        public int getInterfaceVersion() {
            return IUMDAdaptor.VERSION;
        }
    };

    @Override
    public int initUAC() {
        if (mAudio == null) {
            Log.d(TAG, "initUAC(): audio instance not available");
            return -1;
        }
        try {
            return mAudio.initUAC(mAidlCb);
        } catch (RemoteException e) {
            Log.e(TAG, "initUAC remote error", e);
            return -1;
        }
    }

    @Override
    public void deinitUAC() {
        if (mAudio == null) {
            Log.d(TAG, "deinitUAC(): audio not available");
            return;
        }
        try {
            mAudio.deInitUAC();
        } catch (RemoteException e) {
            Log.e(TAG, "deinitUAC remote error", e);
        }
    }

    @Override
    public int submitAudioBuffer(ArrayList<Byte> data) {
        if (mAudio == null) {
            Log.d(TAG, "submitAudioBuffer(): audio not available");
            return -1;
        }
        if (data == null) return -1;
        byte[] b = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) b[i] = data.get(i);
        try {
            return mAudio.submitAudioBuffer(b);
        } catch (RemoteException e) {
            Log.e(TAG, "submitAudioBuffer remote error", e);
            return -1;
        }
    }

    @Override
    public void setAudioBufferSize(int size) {
        if (mAudio == null) {
            Log.d(TAG, "setAudioBufferSize(): audio not available");
            return;
        }
        try {
            mAudio.setAudioBufferSize(size);
        } catch (RemoteException e) {
            Log.e(TAG, "setAudioBufferSize remote error", e);
        }
    }

    @Override
    public void setAudioCapture(AudioCapture capture) {
        Log.d(TAG, "setAudioCapture()");
        this.mAudioCapture = capture;
    }

    @Override
    public ArrayBlockingQueue<Integer> getEventQueue() {
        return mEventQueue;
    }

    @Override
    public LinkedBlockingQueue<Boolean> getConditionQueue() {
        return mConditionQueue;
    }

    @Override
    public AudioState setAudioState(int status) {
        switch (status) {
            case vendor.qti.hardware.umd_aidl.AudioStatus.AUDIO_STATE_INVALID:
                Log.d(TAG,"current audio invalid");
                return AudioState._invalid;
            case vendor.qti.hardware.umd_aidl.AudioStatus.AUDIO_STATE_PLAYBACK:
                Log.d(TAG,"current audio playback");
                return AudioState._playback;
            case vendor.qti.hardware.umd_aidl.AudioStatus.AUDIO_STATE_CAPTURE:
                Log.d(TAG,"current audio capture");
                return AudioState._capture;
            case vendor.qti.hardware.umd_aidl.AudioStatus.AUDIO_STATE_PLAYBACK_CAPTURE:
                Log.d(TAG,"current audio playback capture");
                return AudioState._playback_capture;
            case vendor.qti.hardware.umd_aidl.AudioStatus.AUDIO_STATE_PAUSED:
                Log.d(TAG,"current audio paused");
                return AudioState._paused;
            default:
                return AudioState._invalid;
        }
    }

    @Override
    public int initUVC() {
        if (mCamera == null) {
            Log.d(TAG, "initUVC(): camera instance not available");
            return -1;
        }
        try {
            return mCamera.initUVC();
        } catch (RemoteException e) {
            Log.e(TAG, "initUVC remote error", e);
            return -1;
        }
    }

    @Override
    public void deinitUVC() {
        if (mCamera == null) {
            Log.d(TAG, "deinitUVC(): /camera not available");
            return;
        }
        try {
            mCamera.deInitUVC();
        } catch (RemoteException e) {
            Log.e(TAG, "deinitUVC remote error", e);
        }
    }
}

