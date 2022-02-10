
cmake_minimum_required(VERSION 3.4.1)


add_library( # Sets the name of the library.
             native-lib #libnative-lib.so

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp
             src/main/cpp/egl/WlEglHelper.cpp
             src/main/cpp/egl/WlEglThread.cpp
             src/main/cpp/opengl/WlBaseOpengl.cpp
        src/main/cpp/opengl/WlFilterOne.cpp
        src/main/cpp/opengl/WlOpengl.cpp
        src/main/cpp/opengl/WlFilterTwo.cpp
        src/main/cpp/opengl/WlFilterYUV.cpp
        )

target_link_libraries( # Specifies the target library.
                       native-lib
                        EGL
                        GLESv2
                        android


                       log )