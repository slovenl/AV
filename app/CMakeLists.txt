# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# now build app's shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wall")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
add_compile_options(-w)  #不显示编译警告
include_directories("src/main/jni/3rdparty/ffmpeg/include")
include_directories("src/main/jni/audio_effect/libaudio_effect/audio_effect_filter_impl/pitch_shift/include")

file(GLOB camera_preview "src/main/jni/camera_preview/*.cpp")

set(VIDEO_CONSUMER src/main/jni/video_consumer)
file(GLOB libvideo_consumer "${VIDEO_CONSUMER}/libvideo_consumer/*.cpp")
file(GLOB libaudio_consumer "${VIDEO_CONSUMER}/libaudio_encoder/*.cpp")
file(GLOB libmusic_consumer "${VIDEO_CONSUMER}/libmusic_merger/*.cpp")

set(VIDEO_ENCODER src/main/jni/libvideo_encoder)
file(GLOB hw_encoder "${VIDEO_ENCODER}/hw_encoder/*.cpp")
file(GLOB soft_encoder "${VIDEO_ENCODER}/soft_encoder/*.cpp")
file(GLOB video_encoder "${VIDEO_ENCODER}/*.cpp")

file(GLOB root "src/main/jni/*.cpp")

set(THIRD_PARTY src/main/jni/3rdparty)
file(GLOB jsoncpp "${THIRD_PARTY}/jsoncpp/*.cpp")
file(GLOB markup "${THIRD_PARTY}/markup/*.cpp")

set(AUDIO_EFFECT src/main/jni/audio_effect)
set(LIB_AUDIO_EFFECT ${AUDIO_EFFECT}/libaudio_effect)
set(libresampler ${AUDIO_EFFECT}/libresampler)
file(GLOB audio_effect "${LIB_AUDIO_EFFECT}/audio_effect/*.cpp")
file(GLOB sox_effect_param "${LIB_AUDIO_EFFECT}/audio_effect/sox_effect_param/*.cpp")
file(GLOB audio_effect_filter "${LIB_AUDIO_EFFECT}/audio_effect_filter/*.cpp")
file(GLOB agc "${LIB_AUDIO_EFFECT}/audio_effect_filter_impl/agc/*.cpp")
file(GLOB delay_output "${LIB_AUDIO_EFFECT}/audio_effect_filter_impl/delay_output/*.cpp")
file(GLOB double_u "${LIB_AUDIO_EFFECT}/audio_effect_filter_impl/double_u/*.cpp")
file(GLOB fade "${LIB_AUDIO_EFFECT}/audio_effect_filter_impl/fade/*.cpp")
file(GLOB pitch_shift "${LIB_AUDIO_EFFECT}/audio_effect_filter_impl/pitch_shift/*.cpp")
file(GLOB SoundTouch "${LIB_AUDIO_EFFECT}/audio_effect_filter_impl/pitch_shift/SoundTouch/*.cpp")
#file(GLOB SoundTouch "${LIB_AUDIO_EFFECT}/audio_effect_filter/audio_effect_filter_impl/pitch_shift/SoundTouch/*.cpp")
file(GLOB sox_effect "${LIB_AUDIO_EFFECT}/audio_effect_filter_impl/sox_effect/*.cpp")
file(GLOB specific "${LIB_AUDIO_EFFECT}/audio_effect_filter_impl/specific/*.cpp")
file(GLOB vocal_detector "${LIB_AUDIO_EFFECT}/audio_effect_filter_impl/vocal_detector/*.cpp")
file(GLOB vocal_volume_detector "${LIB_AUDIO_EFFECT}/audio_effect_filter_impl/vocal_volume_detector/*.cpp")
file(GLOB vocal_adjust "${LIB_AUDIO_EFFECT}/audio_effect_filter_impl/volume_adjust/*.cpp")
file(GLOB adjust_echo_reverb "${LIB_AUDIO_EFFECT}/audio_effect_impl/adjust_echo_reverb/*.cpp")
file(GLOB auto_tune "${LIB_AUDIO_EFFECT}/audio_effect_impl/auto_tune/*.cpp")
file(GLOB harmonic "${LIB_AUDIO_EFFECT}/audio_effect_impl/harmonic/*.cpp")
file(GLOB robot "${LIB_AUDIO_EFFECT}/audio_effect_impl/robot/*.cpp")
file(GLOB audio_effect_processor "${LIB_AUDIO_EFFECT}/audio_effect_processor/*.cpp")
file(GLOB live "${LIB_AUDIO_EFFECT}/audio_effect_processor/live/*.cpp")
file(GLOB post "${LIB_AUDIO_EFFECT}/audio_effect_processor/post/*.cpp")
file(GLOB recording "${LIB_AUDIO_EFFECT}/audio_effect_processor/recording/*.cpp")


set(LIVE_CORE src/main/jni/liblivecore)
file(GLOB audio_encoder "${LIVE_CORE}/audio_encoder/*.cpp")
file(GLOB common "${LIVE_CORE}/common/*.cpp")
file(GLOB consumer "${LIVE_CORE}/consumer/*.cpp")
file(GLOB platform_dependent "${LIVE_CORE}/platform_dependent/*.cpp")
file(GLOB publisher "${LIVE_CORE}/publisher/*.cpp")

set(LIBCOMMON src/main/jni/libcommon)
file(GLOB libcommon "${LIBCOMMON}/*.cpp")
file(GLOB egl_core "${LIBCOMMON}/egl_core/*.cpp")
file(GLOB message_queue "${LIBCOMMON}/message_queue/*.cpp")
file(GLOB opengl_media "${LIBCOMMON}/opengl_media/*.cpp")
file(GLOB opengl_media_render "${LIBCOMMON}/opengl_media/render/*.cpp")
file(GLOB opengl_media_texture "${LIBCOMMON}/opengl_media/opengl_media/texture/*.cpp")
file(GLOB opengl_media_texture_copier "${LIBCOMMON}/opengl_media/opengl_media/texture_copier/*.cpp")
file(GLOB opensl_media "${LIBCOMMON}/opengl_media/opensl_media/*.cpp")

set(LIBEDITCORE src/main/jni/libeditcore)
file(GLOB libeditcore "${LIBEDITCORE}/*.cpp")
file(GLOB libeditcore_common "${LIBEDITCORE}/common/*.cpp")
file(GLOB libeditcore_decoder "${LIBEDITCORE}/decoder/*.c*")
file(GLOB libeditcore_editmodel "${LIBEDITCORE}/editmodel/*.cpp")
file(GLOB libeditcore_editmodel_blur_scene "${LIBEDITCORE}/editmodel/blur_scene/*.cpp")
file(GLOB libeditcore_editmodel_header_fade_scene "${LIBEDITCORE}/editmodel/header_fade_scene/*.cpp")
file(GLOB libeditcore_editmodel_header_screen_scene "${LIBEDITCORE}/editmodel/header_screen_scene/*.cpp")
file(GLOB libeditcore_editmodel_mask_alpha "${LIBEDITCORE}/editmodel/mask_alpha/*.cpp")
file(GLOB libeditcore_editmodel_overlay_scene "${LIBEDITCORE}/editmodel/overlay_scene/*.cpp")
file(GLOB libeditcore_editmodel_png_sequence "${LIBEDITCORE}/editmodel/png_sequence/*.cpp")
file(GLOB libeditcore_editmodel_selective_blur_scene "${LIBEDITCORE}/editmodel/selective_blur_scene/*.cpp")
file(GLOB libeditcore_editmodel_text_scene "${LIBEDITCORE}/editmodel/text_scene/*.cpp")
file(GLOB libeditcore_editmodel_trailer_scene "${LIBEDITCORE}/editmodel/trailer_scene/*.cpp")
file(GLOB libeditcore_editmodel_transition_scene "${LIBEDITCORE}/editmodel/transition_scene/*.cpp")
file(GLOB libeditcore_editmodel_video_decoder_scene "${LIBEDITCORE}/editmodel/video_decoder_scene/*.cpp")
file(GLOB libeditcore_editmodel_video_overlay_scene "${LIBEDITCORE}/editmodel/video_overlay_scene/*.cpp")

file(GLOB libeditcore_platform_dependent "${LIBEDITCORE}/platform_dependent/*.cpp")
file(GLOB libeditcore_videoeffect "${LIBEDITCORE}/videoeffect/*.cpp")
file(GLOB libeditcore_videoeffect_blur_scene "${LIBEDITCORE}/videoeffect/blur_scene/*.cpp")
file(GLOB libeditcore_videoeffect_gif_scene "${LIBEDITCORE}/videoeffect/gif_scene/*.cpp")
file(GLOB libeditcore_videoeffect_header_fade_scene "${LIBEDITCORE}/videoeffect/header_fade_scene/*.cpp")
file(GLOB libeditcore_videoeffect_header_screen_scene "${LIBEDITCORE}/videoeffect/header_screen_scene/*.cpp")
file(GLOB libeditcore_videoeffect_header_iamge_effect "${LIBEDITCORE}/videoeffect/image_effect/*.cpp")
file(GLOB libeditcore_videoeffect_iamge_effect_assistant_util "${LIBEDITCORE}/videoeffect/image_effect/assistant_util/*.cpp")
file(GLOB libeditcore_videoeffect_iamge_effect_beautify_face "${LIBEDITCORE}/videoeffect/image_effect/beautify_face/*.cpp")
file(GLOB libeditcore_videoeffect_iamge_effect_ghost_scene "${LIBEDITCORE}/videoeffect/image_effect/ghost_scene/*.cpp")
file(GLOB libeditcore_videoeffect_iamge_effect_ghost_scene_blur_mirror "${LIBEDITCORE}/videoeffect/image_effect/ghost_scene/blur_mirror/*.cpp")
file(GLOB libeditcore_videoeffect_iamge_effect_ghost_scene_carousel "${LIBEDITCORE}/videoeffect/image_effect/ghost_scene/carousel/*.cpp")
file(GLOB libeditcore_videoeffect_iamge_effect_ghost_scene_soul_stuff "${LIBEDITCORE}/videoeffect/image_effect/ghost_scene/soul_stuff/*.cpp")
file(GLOB libeditcore_videoeffect_iamge_effect_ghost_scene_tilt_shift "${LIBEDITCORE}/videoeffect/image_effect/ghost_scene/tilt_shift/*.cpp")
file(GLOB libeditcore_videoeffect_iamge_effect_ghost_scene_x_ray "${LIBEDITCORE}/videoeffect/image_effect/ghost_scene/x_ray/*.cpp")


file(GLOB libeditcore_videoeffect_iamge_effect_highpass "${LIBEDITCORE}/videoeffect/image_effect/highpass/*.cpp")
file(GLOB libeditcore_videoeffect_iamge_effect_thin "${LIBEDITCORE}/videoeffect/image_effect/thin/*.cpp")
file(GLOB libeditcore_videoeffect_iamge_effect_time_stretch "${LIBEDITCORE}/videoeffect/image_effect/time_stretch/*.cpp")
file(GLOB libeditcore_videoeffect_iamge_effect_whitening "${LIBEDITCORE}/videoeffect/image_effect/whitening/*.cpp")


file(GLOB libeditcore_videoeffect_mask_alpha "${LIBEDITCORE}/videoeffect/mask_alpha/*.cpp")
file(GLOB libeditcore_videoeffect_overlay_scene "${LIBEDITCORE}/videoeffect/overlay_scene/*.cpp")
file(GLOB libeditcore_videoeffect_png_sequence "${LIBEDITCORE}/videoeffect/png_sequence/*.cpp")
file(GLOB libeditcore_videoeffect_saturation_scene "${LIBEDITCORE}/videoeffect/saturation_scene/*.cpp")
file(GLOB libeditcore_videoeffect_screen_scene "${LIBEDITCORE}/videoeffect/screen_scene/*.cpp")
file(GLOB libeditcore_videoeffect_text_scene "${LIBEDITCORE}/videoeffect/text_scene/*.cpp")
file(GLOB libeditcore_videoeffect_trailer_scene "${LIBEDITCORE}/videoeffect/trailer_scene/*.cpp")
file(GLOB libeditcore_videoeffect_transition_scene "${LIBEDITCORE}/videoeffect/transition_scene/*.cpp")
file(GLOB libeditcore_videoeffect_video_overlay_scene "${LIBEDITCORE}/videoeffect/video_overlay_scene/*.cpp")

#file(GLOB jni_dir "src/main/jni/*.cpp")
add_library( # Sets the name of the library.
        native-lib
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        ${root}
        ${libcommon}
        ${egl_core}
        ${message_queue}
        ${opengl_media}
        ${opengl_media_render}
        ${opengl_media_texture}
        ${opengl_media_texture_copier}
        ${opensl_media}
        ${libeditcore}
        ${libeditcore_common}
        ${libeditcore_decoder}
        ${libeditcore_editmodel}
        ${libeditcore_editmodel_blur_scene}
        ${libeditcore_editmodel_header_fade_scene}
        ${libeditcore_editmodel_header_screen_scene}
        ${libeditcore_editmodel_mask_alpha}
        ${libeditcore_editmodel_overlay_scene}
        ${libeditcore_editmodel_png_sequence}
        ${libeditcore_editmodel_selective_blur_scene}
        ${libeditcore_editmodel_text_scene}
        ${libeditcore_editmodel_trailer_scene}
        ${libeditcore_editmodel_transition_scene}
        ${libeditcore_editmodel_video_decoder_scene}
        ${libeditcore_editmodel_video_overlay_scene}

        ${libeditcore_platform_dependent}
        ${libeditcore_videoeffect}
        ${libeditcore_videoeffect_blur_scene}
        ${libeditcore_videoeffect_gif_scene}
        ${libeditcore_videoeffect_header_fade_scene}
        ${libeditcore_videoeffect_header_screen_scene}
        ${libeditcore_videoeffect_header_iamge_effect}
        ${libeditcore_videoeffect_iamge_effect_assistant_util}
        ${libeditcore_videoeffect_iamge_effect_beautify_face}
        ${libeditcore_videoeffect_iamge_effect_ghost_scene}
        ${libeditcore_videoeffect_iamge_effect_ghost_scene_blur_mirror}
        ${libeditcore_videoeffect_iamge_effect_ghost_scene_carousel}
        ${libeditcore_videoeffect_iamge_effect_ghost_scene_soul_stuff}
        ${libeditcore_videoeffect_iamge_effect_ghost_scene_tilt_shift}
        ${libeditcore_videoeffect_iamge_effect_ghost_scene_x_ray}



        ${libeditcore_videoeffect_iamge_effect_highpass}
        ${libeditcore_videoeffect_iamge_effect_thin}
        ${libeditcore_videoeffect_iamge_effect_time_stretch}
        ${libeditcore_videoeffect_iamge_effect_whitening}



        ${libeditcore_videoeffect_mask_alpha}
        ${libeditcore_videoeffect_overlay_scene}
        ${libeditcore_videoeffect_png_sequence}
        ${libeditcore_videoeffect_saturation_scene}
        ${libeditcore_videoeffect_screen_scene}
        ${libeditcore_videoeffect_text_scene}
        ${libeditcore_videoeffect_trailer_scene}
        ${libeditcore_videoeffect_transition_scene}
        ${libeditcore_videoeffect_video_overlay_scene}

        ${jsoncpp}
        ${markup}

        ${audio_effect}
        ${libresampler}
        ${sox_effect_param}
        ${audio_effect_filter}
        ${agc}
        ${delay_output}
        ${double_u}
        ${fade}
        ${pitch_shift}
#        ${SoundTouch}
        ${sox_effect}
        ${specific}
        ${vocal_detector}
        ${vocal_volume_detector}
        ${vocal_adjust}
        ${adjust_echo_reverb}
        ${auto_tune}
        ${harmonic}
        ${robot}
        ${audio_effect_processor}
        ${live}
        ${post}
        ${recording}

        ${camera_preview}

        ${audio_encoder}
        ${common}
        ${consumer}
        ${platform_dependent}
        ${publisher}

        ${libvideo_consumer}
        ${libaudio_consumer}
        ${libmusic_consumer}

        ${hw_encoder}
        ${soft_encoder}
        ${video_encoder})


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
set(PATH_TO_PRE_BUILT src/main/jni/3rdparty/prebuilt/${ANDROID_ABI})

add_library(libavutil STATIC IMPORTED)
set_target_properties(libavutil PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libavutil.a)

add_library(libavfilter STATIC IMPORTED)
set_target_properties(libavfilter PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libavfilter.a)

add_library(libavformat STATIC IMPORTED)
set_target_properties(libavformat PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libavformat.a)

add_library(libavcodec STATIC IMPORTED)
set_target_properties(libavcodec PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libavcodec.a)

add_library(libpostproc STATIC IMPORTED)
set_target_properties(libpostproc PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libpostproc.a)

add_library(libswresample STATIC IMPORTED)
set_target_properties(libswresample PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libswresample.a)

add_library(libswscale STATIC IMPORTED)
set_target_properties(libswscale PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libswscale.a)

add_library(libfdk-aac STATIC IMPORTED)
set_target_properties(libfdk-aac PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libfdk-aac.a)

add_library(libvo-aacenc STATIC IMPORTED)
set_target_properties(libvo-aacenc PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libvo-aacenc.a)

add_library(libx264 STATIC IMPORTED)
set_target_properties(libx264 PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libx264.a)

add_library(libsox STATIC IMPORTED)
set_target_properties(libx264 PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libsox.a)

#add_library(libsox STATIC IMPORTED)
#set_target_properties(libx264 PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libsox.a)


target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.
        android
        log
        EGL
        OpenSLES
        GLESv2
        # 引入ffmpeg相关静态库
        libavutil
        libavfilter
        libavformat
        libavcodec
        libpostproc
        libswresample
        libswscale
        libfdk-aac
        libvo-aacenc
        libx264
        )