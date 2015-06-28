LOCAL_PATH := $(call my-dir)
 

include $(CLEAR_VARS)
 
LOCAL_LDLIBS    := -llog -ljnigraphics
LOCAL_CPP_EXTENSION := .cpp
LOCAL_MODULE    := blur
LOCAL_SRC_FILES := blur.c main.cpp
 
LOCAL_CFLAGS    =  -ffast-math -O3 -funroll-loops
 
include $(BUILD_SHARED_LIBRARY)


