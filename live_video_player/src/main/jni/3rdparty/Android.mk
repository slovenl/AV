LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := lib3rdparty

LOCAL_CPP_EXTENSION := .cc .cpp
LOCAL_ARM_MODE := arm

LOCAL_C_INCLUDES := \
$(LOCAL_PATH)/../libcommon 
                           
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

LOCAL_EXPORT_LDLIBS     := -llog

include $(BUILD_STATIC_LIBRARY)
