#include <jni.h>
#include "android/log.h"


#ifndef __KK__LOG__
#define __KK__LOG__
#ifdef __cplusplus
extern "C" {
#endif


#define  LOG_TAG    "JNI"
//__android_log_print(int prio, const char *tag,  const char *fmt, ...)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)




#ifdef __cplusplus
}
#endif
#endif
