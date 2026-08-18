/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package org.codeaurora.umdadaptor;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public interface UMDInterface{
    int initUAC();
    void deinitUAC();
    int initUVC();
    void deinitUVC();
    int submitAudioBuffer(ArrayList<Byte> data);
    void setAudioBufferSize(int size);

    enum AudioState {
    _invalid,
    _playback,
    _capture,
    _playback_capture,
    _paused
    }
    AudioState setAudioState(int status);
    void setAudioCapture(AudioCapture mCapture);
    ArrayBlockingQueue<Integer> getEventQueue();
    LinkedBlockingQueue<Boolean> getConditionQueue();
}
