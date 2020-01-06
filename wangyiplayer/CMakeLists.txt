
cmake_minimum_required(VERSION 3.4.1)
file(GLOB SOURCE src/main/cpp/*.cpp )
add_library(
        wangyiplayer
        SHARED
        ${SOURCE}

)
set(my_lib_path ${CMAKE_SOURCE_DIR}/src/main/cpp/libs/${ANDROID_ABI})
#CMAKE_CXX_FLAGS 编译路径 -L 源路径，就可以直接找到  avfilter avformat 等.a静态库
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${my_lib_path} ")


include_directories(src/main/cpp/include/)
target_link_libraries(
        wangyiplayer
        avfilter avformat    avcodec  avutil  swresample swscale
        z
        OpenSLES
        android
        log )