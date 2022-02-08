LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -DHAVE_CONFIG_H -DFPM_ARM -ffast-math -O3
LOCAL_C_INCLUDES += \
    $(LOCAL_PATH)/../video_player \
    $(LOCAL_PATH)/../libcommon \
    $(LOCAL_PATH)/../libeditcore \
    $(LOCAL_PATH)/../3rdparty \
    $(LOCAL_PATH)/../3rdparty/ffmpeg/include 
    
LOCAL_SRC_FILES = \
./libliveshowvideoplayer/live_show_ffmpeg_video_decoder.cpp \
./libliveshowvideoplayer/live_show_av_synchronizer.cpp \
./libliveshowvideoplayer/live_show_mediacodec_video_decoder.cpp \
./libliveshowvideoplayer/live_show_video_controller.cpp

LOCAL_MODULE := libliveshowvideoplayer
include $(BUILD_STATIC_LIBRARY)
