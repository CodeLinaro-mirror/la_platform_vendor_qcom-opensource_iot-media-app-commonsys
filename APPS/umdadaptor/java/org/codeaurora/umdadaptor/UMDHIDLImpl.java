/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package org.codeaurora.umdadaptor;

import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import vendor.qti.hardware.umd.V1_0.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

public class UMDHIDLImpl implements UMDInterface {

    IUMDAdaptor hidlAdaptor;
    private static final String TAG = "UMDHIDLImpl";
    ArrayBlockingQueue<Integer> mEventQueue = new ArrayBlockingQueue<>(8);
    LinkedBlockingQueue<Boolean> mConditionQueue = new LinkedBlockingQueue<Boolean>();
    private AudioCapture mAudioCapture = null;

    public UMDHIDLImpl(IUMDAdaptor mServer) {
        hidlAdaptor = mServer;
        if(hidlAdaptor == null) {
            Log.d(TAG,"hidlAdaptor is Null");
        }
    }

    private vendor.qti.hardware.umd.V1_0.IUMDAdaptorCallback hidlCallback = new vendor.qti.hardware.umd.V1_0.IUMDAdaptorCallback.Stub() {
        @Override
        public void onAudioUevent(int status) {
            Log.d(TAG,"onAudioUEvent called from HIDL");
            try {
                mEventQueue.put(status);
                mConditionQueue.put(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int onAudioBufferReceive(ArrayList<Byte> data) {
            Log.d(TAG,"onAudioBufferReceive called from HIDL");
            mAudioCapture.audioCapture(data);
            return 0;
        }
    };

    @Override
    public AudioState setAudioState(int state) {
        switch(state) {
            case AudioStatus.AUDIO_STATE_INVALID:
                return AudioState._invalid;
            case AudioStatus.AUDIO_STATE_PLAYBACK:
                return AudioState._playback;
            case AudioStatus.AUDIO_STATE_CAPTURE:
                return AudioState._capture;
            case AudioStatus.AUDIO_STATE_PLAYBACK_CAPTURE:
                return AudioState._playback_capture;
            case AudioStatus.AUDIO_STATE_PAUSED:
                return AudioState._paused;
        }
        return null;
    }

    @Override
    public int initUAC() {
        Log.d(TAG,"initUAC from HIDL class");
        int retVal = 0;
        try {
            retVal = hidlAdaptor.initUAC(hidlCallback);
            return retVal;
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception",e);
            return -1;
        }
    }

    @Override
    public int initUVC() {
        Log.d(TAG,"initUVC from HIDL class");
        int retVal = 0;
        try {
            retVal = hidlAdaptor.initUVC();
            return retVal;
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception",e);
            return -1;
        }
    }

    @Override
    public void deinitUAC() {
        Log.d(TAG,"deinitUAC from HIDL class");
        try {
            hidlAdaptor.deInitUAC();
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception",e);
        }
        return;
    }

    @Override
    public void deinitUVC() {
        Log.d(TAG,"deinitUVC from HIDL class");
        try {
            hidlAdaptor.deInitUVC();
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception",e);
        }
        return;
    }

    @Override
    public int submitAudioBuffer(ArrayList<Byte> data) {
        Log.d(TAG,"submitBuffer from HIDL class");
        int retVal = 0;
        try {
            retVal = hidlAdaptor.submitAudioBuffer(data);
            return retVal;
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception",e);
            return -1;
        }
    }

    @Override
    public void setAudioBufferSize(int size) {
        Log.d(TAG,"setAudioBufferSize from HIDL class");
        try {
            hidlAdaptor.setAudioBufferSize(size);
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception",e);
        }
        return;
    }

    @Override
    public void setAudioCapture(AudioCapture mCapture) {
        Log.d(TAG,"setAudioCapture from hidl impl class");
        mAudioCapture = mCapture;
        return;
    }

    @Override
    public ArrayBlockingQueue<Integer> getEventQueue() {
        Log.d(TAG,"getEventQueue from hidl class");
        return mEventQueue;
    }

    @Override
    public LinkedBlockingQueue<Boolean> getConditionQueue(){
        Log.d(TAG,"getConditionQueue from hidl class");
        return mConditionQueue;
    }
}
