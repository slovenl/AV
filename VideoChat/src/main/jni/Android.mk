LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_CFLAGS := -D__STDC_CONSTANT_MACROS -fpermissive


LOCAL_MODULE    := yuv
LOCAL_SRC_FILES := yuv.cpp

include $(BUILD_SHARED_LIBRARY)
