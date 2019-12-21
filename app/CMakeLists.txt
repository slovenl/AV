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

include_directories("src/main/jni/3rdparty/ffmpeg/include")

file(GLOB source_dir "src/main/jni/*.cpp")
file(GLOB source_first_dir "src/main/jni/*/*.cpp")
file(GLOB source_second_dir "/src/main/jni/*/*/*.cpp" )
file(GLOB source_third_dir "src/main/jni/*/*/*/*.cpp")
file(GLOB source_fouth_dir "src/main/jni/*/*/*/*/*.cpp")
#file(GLOB jni_dir "src/main/jni/*.cpp")
add_library( # Sets the name of the library.
        native-lib
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        ${source_dir}
        ${source_first_dir}
        ${source_second_dir}
        ${source_third_dir}
        ${source_fouth_dir})


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