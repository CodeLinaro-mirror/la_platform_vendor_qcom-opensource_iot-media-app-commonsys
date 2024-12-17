LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

res_dir := res $(LOCAL_PATH)/res

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES += java/org/codeaurora/umdadaptor/MainActivity.java
LOCAL_SRC_FILES += java/org/codeaurora/umdadaptor/AudioCapture.java
LOCAL_SRC_FILES += java/org/codeaurora/umdadaptor/AudioPlayback.java
LOCAL_SRC_FILES += java/org/codeaurora/umdadaptor/UMDService.java


LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dir))
LOCAL_USE_AAPT2 := true

LOCAL_JAVA_LIBRARIES := com.google.android.material_material \

LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PACKAGE_NAME := UMDAdaptor
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_STATIC_JAVA_LIBRARIES += vendor.qti.hardware.umd-V1.0-java
LOCAL_SYSTEM_EXT_MODULE:= true

#include $(BUILD_PACKAGE)
