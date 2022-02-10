
cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")

add_library( # Sets the name of the library.
             wlpush

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/wlpush.cpp
             src/main/cpp/WlQueue.cpp
             src/main/cpp/RtmpPush.cpp
             src/main/cpp/WlCallJava.cpp

             src/main/cpp/librtmp/amf.c
             src/main/cpp/librtmp/hashswf.c
             src/main/cpp/librtmp/log.c
             src/main/cpp/librtmp/parseurl.c
             src/main/cpp/librtmp/rtmp.c

             )


find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )


target_link_libraries( # Specifies the target library.
                       wlpush

                       ${log-lib} )