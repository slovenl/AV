
cmake_minimum_required(VERSION 3.4.1)
file(GLOB SOURCE src/main/cpp/*.cpp )
add_library(
        wangyiplayer
        SHARED
        ${SOURCE}

)
set(my_lib_path ${CMAKE_SOURCE_DIR}/src/main/cpp/libs/${ANDROID_ABI})
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${my_lib_path} ")


include_directories(src/main/cpp/include/)
target_link_libraries(
        wangyiplayer
        avfilter avformat    avcodec  avutil  swresample swscale
        z
        OpenSLES
        android
        log )