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

file(GLOB source_dir "${CMAKE_SOURCE_DIR}/src/main/cpp/*.cpp")
#file(GLOB jni_dir "src/main/jni/*.cpp")
file(GLOB camera_dir "src/main/cpp/camera_preview/*.cpp")
file(GLOB egl_dir "src/main/cpp/libcommon/egl_core/*.cpp")
file(GLOB common_root "${CMAKE_SOURCE_DIR}/src/main/cpp/libcommon/*.cpp")
file(GLOB queue_dir "${CMAKE_SOURCE_DIR}/src/main/cpp/libcommon/message_queue/*.cpp")
file(GLOB opengl_dir "${CMAKE_SOURCE_DIR}/src/main/cpp/libcommon/opengl_media/*.cpp")
file(GLOB render_dir "${CMAKE_SOURCE_DIR}/src/main/cpp/libcommon/opengl_media/render/*.cpp")
file(GLOB texture_dir "${CMAKE_SOURCE_DIR}/src/main/cpp/libcommon/opengl_media/texture/*.cpp")
file(GLOB copy_dir "${CMAKE_SOURCE_DIR}/src/main/cpp/libcommon/opengl_media/texture_copier/*.cpp")
file(GLOB opensl_dir "${CMAKE_SOURCE_DIR}/src/main/cpp/libcommon/opensl_media/*.cpp")
add_library( # Sets the name of the library.
        native-lib
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        src/main/jni/ChangbaRecordingPreviewScheduler.cpp
        ${source_dir}
        ${camera_dir}
        ${egl_dir}
        ${queue_dir}
        ${opengl_dir}
        ${render_dir}
        ${texture_dir}
        ${copy_dir}
        ${common_root}
        ${opensl_dir})


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.
        android
        log
        EGL
        OpenSLES
        GLESv2)