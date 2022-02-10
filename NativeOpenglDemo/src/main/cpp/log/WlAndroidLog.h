//
// Created by yangw on 2019-2-17.
//

#ifndef NATIVEOPENGLDEMO_WLANDROIDLOG_H
#define NATIVEOPENGLDEMO_WLANDROIDLOG_H

#include "android/log.h"

#define LOGD(FORMAT, ...) __android_log_print(ANDROID_LOG_DEBUG, "ywl5320", FORMAT, ##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR, "ywl5320", FORMAT, ##__VA_ARGS__);

#endif //NATIVEOPENGLDEMO_WLANDROIDLOG_H
