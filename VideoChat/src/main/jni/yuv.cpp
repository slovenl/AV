//
// Created by mabin1 on 2017/10/17.
//
#include <stdio.h>
#include <stdint.h>
#include <jni.h>
void NV21ToYUV420sp(JNIEnv *pEnv, jobject pObj,jbyteArray byteArray) {
    int arrayLength = pEnv->GetArrayLength(byteArray);
    int framesize =arrayLength*2/3;
    jbyte* nv21=pEnv->GetByteArrayElements(byteArray,NULL);
	jbyte temp;
	for(int i = framesize; i < arrayLength; i+=2){
        temp = nv21[i];
        nv21[i] = nv21[i+1];
        nv21[i+1] = temp;
    }
    pEnv->ReleaseByteArrayElements(byteArray, nv21,0);
}
void YV12ToI420(JNIEnv *pEnv, jobject pObj,jbyteArray byteArray) {
	int arrayLength = pEnv->GetArrayLength(byteArray);
	jbyte* yv12=pEnv->GetByteArrayElements(byteArray,NULL);
	int wh4 = arrayLength/6; //wh4 = width*height/4
	jbyte temp;
	for (int i=wh4*4; i<wh4*5; i++)
	{
		temp = yv12[i];
		yv12[i] = yv12[i+wh4];
		yv12[i+wh4] = temp;
	}
	pEnv->ReleaseByteArrayElements(byteArray, yv12,0);
}

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[2];
	nm[0].name = "nativeNV21ToYUV420sp";
	nm[0].signature = "([B)V";
	nm[0].fnPtr = (void*)NV21ToYUV420sp;

    nm[1].name = "nativeYV12ToI420";
    nm[1].signature = "([B)V";
    nm[1].fnPtr = (void*)YV12ToI420;

	jclass cls = (env)->FindClass("videochat/ju/com/videochat/Yuv");
	//Register methods with env->RegisterNatives.
	(env)->RegisterNatives(cls, nm, 2);
	return JNI_VERSION_1_6;
}
