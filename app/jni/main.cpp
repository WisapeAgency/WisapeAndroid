
#include "main.h"

const char * classPathName = "com/wisape/android/util/image/NativeProcess";

extern "C" {
	extern void* functionToBlur(JNIEnv* env, jclass clzz, jobject bitmapOut, jint radius, jint threadCount, jint threadIndex, jint round);
}


void* stack_blur(JNIEnv* env, jclass clzz, jobject bitmapOut, jint radius, jint threadCount, jint threadIndex){
    //LOGE("stack_blur");
    functionToBlur(env,clzz,bitmapOut,radius,threadCount,threadIndex,1);//horizontal
    functionToBlur(env,clzz,bitmapOut,radius,threadCount,threadIndex,2);//vertical
}






static JNINativeMethod methods[] = {
    {"stack_blur", "(Landroid/graphics/Bitmap;III)V", (void*)stack_blur}
};

jint JNI_OnLoad(JavaVM* vm, void* reserved) {

	jclass clazz = NULL;
	JNIEnv* env = NULL;


	if(vm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		LOGE("JNI_OnLoad->GetEnv error!");
		return -1;
	}

	clazz = env->FindClass(classPathName);
	if(!clazz) {
		LOGE("JNI_OnLoad->FindClass error!");
		return -1;
	}

	if(env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
		LOGE("JNI_OnLoad->RegisterNatives error!");
		return -1;
	}

	return JNI_VERSION_1_6;
}