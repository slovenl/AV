#ifndef AV_LOG_H
#define AV_LOG_H

#include <android/log.h>
#define TAG "SLOVEN"


#define LOGV(...)   __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...)   __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define PRINT LOGI(__FUNCTION__, __LINE__, __FILE__)

#endif //AV_LOG_H
