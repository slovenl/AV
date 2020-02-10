cmake_minimum_required(VERSION 3.4.1)


include_directories(./)
add_library(
        player_jni

        SHARED

        player_jni.cpp
        SPlayer.cpp)

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