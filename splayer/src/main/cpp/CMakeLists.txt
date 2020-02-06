cmake_minimum_required(VERSION 3.4.1)

add_library(
        player_jni
        SHARED
        player_jni.cpp)

find_library(
        log-lib
        log)

find_library(
        android-lib
        android)

target_link_libraries(
        player_jni
        ${android-lib}
        ${log-lib})