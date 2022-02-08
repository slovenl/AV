LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -DHAVE_CONFIG_H -DFPM_ARM -ffast-math -O3
LOCAL_STATIC_LIBRARIES := libcommontool libmedia libeglcore libmessagequeue
LOCAL_C_INCLUDES += \
    $(LOCAL_PATH)/../libcommon \
    $(LOCAL_PATH)/../3rdparty \
    $(LOCAL_PATH)/../3rdparty/ffmpeg/include 
    
LOCAL_SRC_FILES = \
./libvideoplayer/common/circle_texture_queue.cpp \
./libvideoplayer/decoder/video_decoder.cpp \
./libvideoplayer/decoder/decoder_request_header.cpp \
./libvideoplayer/decoder/ffmpeg_video_decoder.cpp \
./libvideoplayer/decoder/mediacodec_video_decoder.cpp \
./libvideoplayer/sync/av_synchronizer.cpp \
./libvideoplayer/drag/av_drag_synchronizer.cpp \
./libvideoplayer/texture_uploader/texture_frame_uploader.cpp \
./libvideoplayer/texture_uploader/gpu_texture_frame_uploader.cpp \
./libvideoplayer/texture_uploader/yuv_texture_frame_uploader.cpp \
./libvideoplayer/audio_output.cpp \
./libvideoplayer/video_output.cpp \
./libvideoplayer/video_player_controller.cpp \
./libvideoplayer/drag/video_player_drag_controller.cpp 

# Link with OpenSL ES
LOCAL_LDLIBS += -lOpenSLES
# Link with OpenGL ES
LOCAL_LDLIBS += -lGLESv2

LOCAL_MODULE := libvideoplayer
include $(BUILD_STATIC_LIBRARY)
