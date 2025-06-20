/*
 *Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 *SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package org.codeaurora.umdadaptor;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class UMDProvider {
    private static final String TAG = "UMDProvider";

    private static final String AIDL_AUDIO  =
        "vendor.qti.hardware.umd_aidl.IUMDAdaptor/audio";
    private static final String AIDL_CAMERA =
        "vendor.qti.hardware.umd_aidl.IUMDAdaptor/camera";

    public static UMDInterface createUMDImpl() {
        Log.d(TAG, "Trying split AIDL instances");
        IBinder bAudio  = ServiceManager.getService(AIDL_AUDIO);
        IBinder bCamera = ServiceManager.getService(AIDL_CAMERA);
        if (bAudio != null || bCamera != null) {
            Log.d(TAG, "Using split AIDL: audio=" + (bAudio != null) + ", camera=" + (bCamera != null));
            return new UMDAIDLImpl(bAudio, bCamera);
        }

        try {
            Log.d(TAG, "Trying HIDL fallback");
            vendor.qti.hardware.umd.V1_0.IUMDAdaptor hidl =
                vendor.qti.hardware.umd.V1_0.IUMDAdaptor.getService(true);
            if (hidl != null) {
                Log.d(TAG, "HIDL server found");
                return new UMDHIDLImpl(hidl);
            }
        } catch (RemoteException e) {
            Log.d(TAG, "HIDL connection error", e);
        }

        Log.e(TAG, "No UMD service (AIDL/HIDL) found");
        return null;
    }
}
