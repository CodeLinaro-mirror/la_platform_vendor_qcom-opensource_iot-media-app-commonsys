/*
 * Copyright (c) 2023 Qualcomm Innovation Center, Inc. All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package org.codeaurora.umdadaptor;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.os.SystemProperties;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import android.os.Process;

import vendor.qti.hardware.umd.V1_0.IUMDAdaptor;
import vendor.qti.hardware.umd.V1_0.IUMDAdaptorCallback;

public class AudioCapture {

    private static final String TAG = "AudioCapture";
    private static final String DEFAULT_SAMPLE_RATE = "44100";
    private static final String DEFAULT_CH_MASK = "2";
    private static final int AUDIO_QUEUE_SIZE = 8;
    private static final int UNDER_RUN_THRESHHOLD = 5;
    private static final String CAPTURE_SAMPLE_RATE_PROP = "persist.vendor.umd.cp.srate";
    private static final String CAPTURE_CHANNEL_MASK_PROP = "persist.vendor.umd.cp.chmask";
    private int mAudioBufferBytes;
    private int mAudioSampleRate;
    private int mAudioChannels;
    private int mAudioEncoding;
    AudioManager mAudioManager;
    private Thread mAudioTrackThread = null;
    ArrayBlockingQueue<ArrayList<Byte>> mAudioQueue = new ArrayBlockingQueue<>(AUDIO_QUEUE_SIZE);
    AtomicBoolean mIsAudioTrackThreadRunning = new AtomicBoolean(false);
    private AudioTrack mAudioTrack = null;
    Semaphore mAudioSemaphore = new Semaphore(1);
    IUMDAdaptor mUMDAdaptor;

    public AudioCapture(Context context, IUMDAdaptor umdadaptor) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mUMDAdaptor = umdadaptor;
    }

    public void audioCapture(ArrayList<Byte> data) {
        try {
            mAudioQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private byte[] arrayListToByteArray(ArrayList<Byte> list) {
        Byte[] byteArray = list.toArray(new Byte[list.size()]);
        int i = 0;
        byte[] result = new byte[list.size()];
        for (Byte b : byteArray) {
            result[i++] = b.byteValue();
        }
        return result;
    }

    public void start() {
        if (mAudioTrack == null) {
            Log.v(TAG, "start enter");
            mAudioSampleRate = Integer.parseInt(SystemProperties.get(CAPTURE_SAMPLE_RATE_PROP, DEFAULT_SAMPLE_RATE));
            int chmask = Integer.parseInt(SystemProperties.get(CAPTURE_CHANNEL_MASK_PROP, DEFAULT_CH_MASK));
            if (chmask == 2) {
                mAudioChannels = AudioFormat.CHANNEL_IN_STEREO;
            } else if (chmask == 1)
                mAudioChannels = AudioFormat.CHANNEL_IN_MONO;
            mAudioEncoding = AudioFormat.ENCODING_PCM_16BIT;
            mAudioBufferBytes = AudioTrack.getMinBufferSize(mAudioSampleRate,
                    mAudioChannels,
                    mAudioEncoding);

            try {
                mAudioSemaphore.acquire(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mAudioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(mAudioEncoding)
                            .setSampleRate(mAudioSampleRate)
                            .setChannelMask(mAudioChannels)
                            .build())
                    .setBufferSizeInBytes(mAudioBufferBytes)
                    .build();
            mAudioTrack.play();

            mAudioTrackThread = new Thread(new Runnable() {
                public void run() {
                    Log.v(TAG, "mAudioTrackThread enter");
                    Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                    Boolean firstTime = true;
                    mIsAudioTrackThreadRunning.set(true);
                    mAudioSemaphore.release();
                    while (mIsAudioTrackThreadRunning.get()) {
                        if (!mAudioQueue.isEmpty()) {
                            if (firstTime) {
                                if (mAudioQueue.remainingCapacity() > UNDER_RUN_THRESHHOLD)
                                    continue;
                                else
                                    firstTime = false;
                            }
                            ArrayList<Byte> bData = new ArrayList<>();
                            try {
                                bData = mAudioQueue.take();
                                byte[] temp = arrayListToByteArray(bData);
                                mAudioTrack.write(temp, 0, temp.length);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Log.v(TAG, "mAudioTrackThread exit");
                }
            }, "Audio Track Thread");
            mAudioTrackThread.start();
            Log.v(TAG, "start exit");
        }
    }

    public void stop() {
        Log.v(TAG, "stop enter");
        if (mAudioTrack != null) {
            try {
                mAudioSemaphore.acquire(1);
                mAudioSemaphore.release(1);
                mIsAudioTrackThreadRunning.set(false);
                mAudioTrackThread.join();
                mAudioTrackThread = null;
                mAudioQueue.clear();

                if (mAudioTrack != null) {
                    mAudioTrack.stop();
                    mAudioTrack.release();
                    mAudioTrack = null;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.v(TAG, "stop exit");
    }
}
