#include "live_show_video_controller.h"

#define LOG_TAG "LiveShowVideoController"
#include <stdio.h>

extern bool isNeedBuriedPoint;
extern BuriedPoint buriedPoint;
extern long buriedPointStart;

LiveShowVideoController::LiveShowVideoController() {
	isNeedBuriedPoint = true;
}

LiveShowVideoController::~LiveShowVideoController() {
}

void LiveShowVideoController::setRTMPCurl(char* rtmp_curl){
	if(synchronizer){
		((LiveShowAVSynchronizer*)synchronizer)->setRTMPCurl(rtmp_curl);
	}
}

void LiveShowVideoController::destroy(){
	LOGI("LiveShowVideoController::destory");
	VideoPlayerController::destroy();
	buriedStaticsDataCallback();
}

bool LiveShowVideoController::initAVSynchronizer() {
	synchronizer = new LiveShowAVSynchronizer();
	return synchronizer->init(requestHeader, g_jvm, obj,
			minBufferedDuration, maxBufferedDuration);
}

jobject LiveShowVideoController::getArrayListObj(JNIEnv * env,
		vector<float>& data, jclass list_class,jmethodID list_construct,
		jmethodID list_add, jclass double_class, jmethodID double_init) {
	jobject list_obj = env->NewObject(list_class, list_construct, "");
	if (list_obj) {
		for (int i = 0; i < data.size(); i++) {
			jobject double_object = env->NewObject(double_class, double_init,
					data[i]);
			env->CallBooleanMethod(list_obj, list_add, double_object);
			env->DeleteLocalRef(double_object);
		}
	}

	return list_obj;
}

void LiveShowVideoController::buriedStaticsDataCallback(){
	if (obj) {
		JNIEnv * env;
		int status = 0;
		bool needAttach = false;
		status = g_jvm->GetEnv((void **) (&env), JNI_VERSION_1_4);
		// don't know why, if detach directly, will crash
		if (status < 0) {
			if (g_jvm->AttachCurrentThread(&env, NULL) != JNI_OK) {
				LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
				return;
			}
			needAttach = true;
		}

		jclass jcls = env->GetObjectClass(obj);

		jmethodID statisticsCallbackFunc = env->GetMethodID(jcls, "statisticsCallbackFromNative", "(JFFFIFLjava/util/List;Ljava/util/List;Ljava/util/List;)V");

		jclass list_class = env->FindClass("java/util/ArrayList");
		jmethodID list_construct = env->GetMethodID(list_class , "<init>", "()V");
		jmethodID list_add = env->GetMethodID(list_class,
									"add", "(Ljava/lang/Object;)Z");

		jclass double_class = env->FindClass("java/lang/Double");
		jmethodID double_init =env->GetMethodID(double_class,"<init>","(D)V");

		jobject retryList_obj = getArrayListObj(env, buriedPoint.retryOpen, list_class, list_construct, list_add, double_class, double_init);
		jobject videoFullList_obj = getArrayListObj(env, buriedPoint.videoQueueFull, list_class,list_construct, list_add ,double_class, double_init);
		jobject videoEmptyList_obj = getArrayListObj(env, buriedPoint.videoQueueEmpty, list_class,list_construct, list_add,  double_class, double_init);

		buriedPoint.videoQueueEmpty.clear();
		buriedPoint.videoQueueFull.clear();
		buriedPoint.retryOpen.clear();

		if (statisticsCallbackFunc){
			env->CallVoidMethod(obj, statisticsCallbackFunc, buriedPoint.beginOpen, buriedPoint.successOpen, buriedPoint.firstScreenTimeMills, buriedPoint.failOpen,
					buriedPoint.failOpenType, buriedPoint.duration,  retryList_obj, videoFullList_obj, videoEmptyList_obj);
		}

		env->DeleteLocalRef(retryList_obj);
		env->DeleteLocalRef(videoFullList_obj);
		env->DeleteLocalRef(videoEmptyList_obj);

		env->DeleteLocalRef(double_class);
		env->DeleteLocalRef(list_class);

		env->DeleteLocalRef(jcls);
	}
}

const char* LiveShowVideoController::getBuriedPoints() {
	string buriedPointsStr;
	buriedPointsStr.clear();

	// 开始试图去打开一个直播流
	string str = "B_0.000";

	string comma = ",";

	buriedPointsStr += str;
	buriedPointsStr += comma;

	// 成功打开流
	char temp[256];
	memset(temp,0,256);
	snprintf(temp, 256, "%.3f", buriedPoint.successOpen);
	str = temp;

	buriedPointsStr += "O_";
	buriedPointsStr += str;
	buriedPointsStr += comma;

	// 首屏时间
	memset(temp, 0, 256);
	snprintf(temp, 256, "%.3f", buriedPoint.firstScreenTimeMills);
	str = temp;

	buriedPointsStr += "T_";
	buriedPointsStr += str;
	buriedPointsStr += comma;

	// 拉流时长
	memset(temp, 0, 256);
	snprintf(temp, 256, "%.3f", buriedPoint.duration);
	str = temp;

	buriedPointsStr += "S_";
	buriedPointsStr += str;
	buriedPointsStr += comma;

	// 流打开失败
	memset(temp, 0, 256);
	snprintf(temp, 256, "%d", buriedPoint.failOpenType);
	str = temp;

	buriedPointsStr += "W";
	buriedPointsStr += str;
	buriedPointsStr += "_";

	memset(temp, 0, 256);
	snprintf(temp, 256, "%.3f", buriedPoint.failOpen);
	str = temp;
	buriedPointsStr += str;
	buriedPointsStr += comma;

	// 重试
	for (int i = 0; i < buriedPoint.retryOpen.size(); i++) {
		memset(temp, 0, 256);
		snprintf(temp, 256, "%.3f", buriedPoint.retryOpen[i]);
		str = temp;

		buriedPointsStr += "R_";
		buriedPointsStr += str;
		buriedPointsStr += comma;
	}
	buriedPoint.retryOpen.clear();

	// 解码缓冲区满
	for (int i = 0; i < buriedPoint.videoQueueFull.size(); i++) {
		memset(temp, 0, 256);
		snprintf(temp, 256, "%.3f", buriedPoint.videoQueueFull[i]);
		str = temp;

		buriedPointsStr += "F_";
		buriedPointsStr += str;
		buriedPointsStr += comma;
	}
	buriedPoint.videoQueueFull.clear();

	// 解码缓冲区空
	for (int i = 0; i < buriedPoint.videoQueueEmpty.size(); i++) {
		memset(temp, 0, 256);
		snprintf(temp, 256, "%.3f", buriedPoint.videoQueueEmpty[i]);
		str = temp;

		buriedPointsStr += "E_";
		buriedPointsStr += str;
		buriedPointsStr += comma;
	}
	buriedPoint.videoQueueEmpty.clear();

	LOGI("buriedPointsStr is %s", buriedPointsStr.c_str());

	return buriedPointsStr.c_str();
}
