# Follow these instructions to build and run SNPE with DSP via AOSP.

1) git apply SnpeOnAosp.patch

2) Paste the following files in current directory.

libs/snpe-release.aar (tested with snpe-1.55.0.2958)
libc++_shared.so (from extracting snpe-release.aar\jni\arm64-v8a\)
libSNPE.so (from extracting snpe-release.aar\jni\arm64-v8a\)
libsnpe-android.so (from extracting snpe-release.aar\jni\arm64-v8a\)
libsnpe_dsp_domains_v2.so (from extracting snpe-release.aar\jni\arm64-v8a\)
libsnpe_dsp_domains_v3.so (from extracting snpe-release.aar\jni\arm64-v8a\)

3) Make Android full build.

3) Flash full build and run the following in order

adb wait-for-device
adb root
adb remount
adb disable-verity
adb reboot

adb wait-for-device
timeout 60 > NUL
// The timeout is to wait for the home screen to be visible (so that filesystem is completely loaded)
adb root
adb remount
adb push qseg_person_align_1_quant.dlc /storage/emulated/0/DCIM/SnpeModel/qseg_person_align_1_quant.dlc
adb push libsnpe_dsp_v66_domains_v2_skel.so /vendor/lib/rfsa/adsp/libsnpe_dsp_v66_domains_v2_skel.so
adb push libSNPE.so /system/lib64/libSNPE.so
adb reboot

adb wait-for-device
adb root
adb remount
adb shell setenforce 0

Notes:

1) Refer to the following document to get SNPE SDK: https://developer.qualcomm.com/sites/default/files/docs/snpe/index.html
2) qseg_person_align_1_quant.dlc is the selfie segmentation model.
3) libsnpe_dsp_v66_domains_v2_skel.so comes from snpe-release.aar\jni\armeabi-v7a\
4) libSNPE.so comes from snpe-release.aar\jni\arm64-v8a\
