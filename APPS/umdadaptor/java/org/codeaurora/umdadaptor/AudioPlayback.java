/*
 * Copyright (c) 2023 Qualcomm Innovation Center, Inc. All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package org.codeaurora.umdadaptor;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import vendor.qti.hardware.umd.V1_0.IUMDAdaptor;

public class AudioPlayback {

    private static final String TAG = "AudioPlayBack";
    private static final String DEFAULT_SAMPLE_RATE = "48000";
    private static final String DEFAULT_CH_MASK = "2";
    private static final int AUDIO_QUEUE_SIZE = 8;
    private static final String PLAYBACK_SAMPLE_RATE_PROP = "persist.vendor.umd.pb.srate";
    private static final String PLAYBACK_CHANNEL_MASK_PROP = "persist.vendor.umd.pb.chmask";
    private int mAudioBufferBytes;
    private int mAudioSampleRate;
    private int mRecorderChannels;
    private int mRecorderAudioEncoding;
    private AudioRecord mAudioRecorder = null;
    AudioManager mAudioManager;
    AudioDeviceInfo[] mAudioDeviceInfo;
    AudioDeviceInfo mAudioDevice = null;
    private Thread mRecordThread = null;
    private Thread mAudioSubmitThread = null;
    ArrayBlockingQueue<byte[]> mAudioQueue = new ArrayBlockingQueue<>(AUDIO_QUEUE_SIZE);
    AtomicBoolean mIsAudioRecordThreadRunning = new AtomicBoolean(false);
    AtomicBoolean mIsAudioSubmitThreadRunning = new AtomicBoolean(false);
    Semaphore mAudioSemaphore = new Semaphore(2);
    IUMDAdaptor mUMDAdaptor;

    public AudioPlayback(Context context, IUMDAdaptor umdadaptor) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mUMDAdaptor = umdadaptor;
        mAudioSampleRate = Integer.parseInt(
                SystemProperties.get(PLAYBACK_SAMPLE_RATE_PROP, DEFAULT_SAMPLE_RATE));

        int chmask = Integer.parseInt(
                SystemProperties.get(PLAYBACK_CHANNEL_MASK_PROP, DEFAULT_CH_MASK));
        if (chmask == 2) {
            mRecorderChannels = AudioFormat.CHANNEL_IN_STEREO;
        } else if (chmask == 1)
            mRecorderChannels = AudioFormat.CHANNEL_IN_MONO;

        mRecorderAudioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        mAudioBufferBytes = AudioRecord.getMinBufferSize(mAudioSampleRate,
                mRecorderChannels,
                mRecorderAudioEncoding);

        try {
            mUMDAdaptor.setAudioBufferSize(mAudioBufferBytes);
        } catch (RemoteException e) {
            Log.i(TAG, "Remote Exception");
        }
    }

    private ArrayList<Byte> toByteArray(@NonNull byte[] data, int offset, int length) {
        ArrayList<Byte> byteArray = new ArrayList<Byte>(length);
        for (int i = 0; i < length; i++) {
            byteArray.add(Byte.valueOf(data[offset + i]));
        }
        return byteArray;
    }

    public void start() {
        if (mAudioRecorder == null) {
            Log.v(TAG, "start enter");
            mAudioDeviceInfo = mAudioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
            for (AudioDeviceInfo d : mAudioDeviceInfo) {
                if (d.getType() == AudioDeviceInfo.TYPE_BUILTIN_MIC)
                    mAudioDevice = d;
            }
            if (mAudioDevice != null) {
                mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                        mAudioSampleRate, mRecorderChannels,
                        mRecorderAudioEncoding, mAudioBufferBytes);
                mAudioRecorder.setPreferredDevice(mAudioDevice);
                mAudioRecorder.startRecording();

                try {
                    mAudioSemaphore.acquire(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mRecordThread = new Thread(new Runnable() {
                    public void run() {
                        Log.v(TAG, "mRecordThread enter");
                        mIsAudioRecordThreadRunning.set(true);
                        mAudioSemaphore.release();
                        byte[] bData = new byte[mAudioBufferBytes];
                        while (mIsAudioRecordThreadRunning.get()) {
                            mAudioRecorder.read(bData, 0, mAudioBufferBytes, AudioRecord.READ_BLOCKING);
                            try {
                                mAudioQueue.put(bData);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.v(TAG, "mRecordThread exit");
                    }
                }, "Audio Record Thread");
                mRecordThread.start();
                mAudioSubmitThread = new Thread(new Runnable() {
                    public void run() {
                        Log.v(TAG, "mAudioSubmitThread enter");
                        mIsAudioSubmitThreadRunning.set(true);
                        mAudioSemaphore.release();
                        while (mIsAudioSubmitThreadRunning.get()) {
                            if (!mAudioQueue.isEmpty()) {
                                byte[] bData = new byte[0];
                                try {
                                    bData = mAudioQueue.take();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                ArrayList<Byte> data = toByteArray(bData, 0, bData.length);
                                try {
                                    mUMDAdaptor.submitAudioBuffer(data);
                                } catch (RemoteException e) {
                                    Log.i(TAG, "Remote Exception in Audio Submit thread");
                                }
                            }
                        }
                        Log.v(TAG, "mAudioSubmitThread exit");
                    }
                }, "Audio Submit Thread");
                mAudioSubmitThread.start();
            }
            Log.v(TAG, "start exit");
        }
    }

    public void stop() {
        Log.v(TAG, "stop enter");
        if (mAudioDevice != null) {
            try {
                mAudioDevice = null;
                mAudioSemaphore.acquire(2);
                mAudioSemaphore.release(2);
                mIsAudioRecordThreadRunning.set(false);
                mIsAudioSubmitThreadRunning.set(false);

                mRecordThread.join();
                mRecordThread = null;

                mAudioSubmitThread.join();
                mAudioSubmitThread = null;

                mAudioQueue.clear();

                if (mAudioRecorder != null) {
                    mAudioRecorder.stop();
                    mAudioRecorder.release();
                    mAudioRecorder = null;
                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.v(TAG, "stop exit");
    }
}
