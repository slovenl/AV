//
// Created by Administrator on 2019/6/4.
//

#ifndef PALYERWANGYI_JAVACALLHELPER_H
#define PALYERWANGYI_JAVACALLHELPER_H


#include <jni.h>

class JavaCallHelper {
public:
    JavaCallHelper(JavaVM *_javaVM, JNIEnv *_env, jobject &_jobj);

    ~JavaCallHelper();
    void onError(int thread, int code);

    void onParpare(int thread);

    void onProgress(int thread, int progress);
private:
    JavaVM *javaVM;
    JNIEnv *env;
    jobject jobj;
    jmethodID jmid_prepare;
    jmethodID jmid_error;
    jmethodID jmid_progress;
};

#endif //PALYERWANGYI_JAVACALLHELPER_H
