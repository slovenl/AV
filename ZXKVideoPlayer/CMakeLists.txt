# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

#set(
#	PATH_TO_MEDIACORE
#	CACHE STRING ""
#)
#set(PATH_TO_NATIVE ${PATH_TO_MEDIACORE}/src/main/cpp)
set(PATH_TO_NATIVE src/main/cpp)
set(PATH_TO_JNI_LAYER src/main/jni)
set(PATH_TO_THIRDPARTY ${PATH_TO_NATIVE}/thirdparty)
set(PATH_TO_PRE_BUILT ${PATH_TO_THIRDPARTY}/prebuilt/${ANDROID_ABI})

add_definitions("
-DGL_GLEXT_PROTOTYPES
-DEGL_EGLEXT_PROTOTYPES
")
add_compile_options(-w)  #不显示编译警告

message("sloven start add_subdirectory")
add_subdirectory(src/main/cpp)
message("sloven eng add_subdirectory")

include_directories(BEFORE ${PATH_TO_THIRDPARTY}/ffmpeg/include/)
include_directories(${PATH_TO_NATIVE}/)
include_directories(${PATH_TO_NATIVE}/common/)
include_directories(${PATH_TO_NATIVE}/video_player/)

file(GLOB FILES_JNI_LAYER "${PATH_TO_JNI_LAYER}/*.cpp")

add_library(native-lib
        SHARED
        ${FILES_JNI_LAYER}
        )

#add_library(libavfilter STATIC IMPORTED)
#set_target_properties(libavfilter PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libavfilter.a)
#
#add_library(libavformat STATIC IMPORTED)
#set_target_properties(libavformat PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libavformat.a)
#
#add_library(libavcodec STATIC IMPORTED)
#set_target_properties(libavcodec PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libavcodec.a)
#
#add_library(libpostproc STATIC IMPORTED)
#set_target_properties(libpostproc PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libpostproc.a)
#
#add_library(libswresample STATIC IMPORTED)
#set_target_properties(libswresample PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libswresample.a)
#
#add_library(libswscale STATIC IMPORTED)
#set_target_properties(libswscale PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libswscale.a)
#
#add_library(libavutil STATIC IMPORTED)
#set_target_properties(libavutil PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libavutil.a)
#
#add_library(libfdk-aac STATIC IMPORTED)
#set_target_properties(libfdk-aac PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libfdk-aac.a)
#
#add_library(libvo-aacenc STATIC IMPORTED)
#set_target_properties(libvo-aacenc PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libvo-aacenc.a)
#
#add_library(libx264 STATIC IMPORTED)
#set_target_properties(libx264 PROPERTIES IMPORTED_LOCATION ${PATH_TO_PRE_BUILT}/libx264.a)

target_link_libraries(native-lib
        # 引入系统的动态库
        log
        android
        GLESv2
        EGL
        z
        OpenSLES
        av-engine
        # 引入ffmpeg相关静态库
#        libavfilter
#        libavformat
#        libavcodec
#        libpostproc
#        libswresample
#        libswscale
#        libavutil
#        libfdk-aac
#        libvo-aacenc
#        libx264
        )