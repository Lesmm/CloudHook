#include <jni.h>
#include <string>

#include <android/log.h>


extern "C" JNIEXPORT jstring


#define LOG_TAG "DEBUG"
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define ALOG(...) __android_log_print(ANDROID_LOG_VERBOSE, __VA_ARGS__)


JNICALL
Java_com_example_administrator_cloudhook_activity_MainActivity_stringFromJNI(JNIEnv *env, jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

jclass getAppClass(JNIEnv *jenv,const char *apn);

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void *reserved) {
    JNIEnv* env = NULL; //注册时在JNIEnv中实现的，所以必须首先获取它

    //从JavaVM获取JNIEnv，一般使用1.4的版本
    if(vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK)
        return -1;

    /*
     * 这里可以找到要注册的类，前提是这个类已经加载到java虚拟机中。
     * 这里说明，动态库和有native方法的类之间，没有任何对应关系。
     */
    static const char* const kClassName="java/lang/String";
    jclass clazz = env->FindClass(kClassName);
    if(clazz == NULL) {
        printf("cannot get class:%s\n", kClassName);
        return -1;
    }

    jclass activityCls = getAppClass(env, "com/example/administrator/cloudhook/activity/MainActivity");
    if (activityCls == NULL) {
        LOGD("Hello - MainActivity cls IS NULL ... ");
    } else {
        LOGD("Hello - MainActivity cls IS NOT NULL ... ");
    }

    jclass checkCls = getAppClass(env, "com/parse/ParseInstallation");

    //这里很重要，必须返回版本，否则加载会失败。
    return JNI_VERSION_1_4;
}


int ClearException(JNIEnv *jenv) {
    jthrowable exception = jenv->ExceptionOccurred();
    if (exception != NULL) {
        jenv->ExceptionDescribe();
        jenv->ExceptionClear();
        return true;
    }
    return false;
}

jclass findAppClass(JNIEnv *jenv,const char *apn) {
    //获取Loaders
    jclass clazzApplicationLoaders = jenv->FindClass("android/app/ApplicationLoaders");
    jthrowable exception = jenv->ExceptionOccurred();
    if (ClearException(jenv)) {
        ALOG("JniHelper - Exception","No class : %s", "android/app/ApplicationLoaders");
        return NULL;
    }
    jfieldID fieldApplicationLoaders = jenv->GetStaticFieldID(clazzApplicationLoaders,"gApplicationLoaders","Landroid/app/ApplicationLoaders;");
    if (ClearException(jenv)) {
        ALOG("JniHelper - Exception","No Static Field :%s","gApplicationLoaders");
        return NULL;
    }
    jobject objApplicationLoaders = jenv->GetStaticObjectField(clazzApplicationLoaders,fieldApplicationLoaders);
    if (ClearException(jenv)) {
        ALOG("JniHelper - Exception","GetStaticObjectField is failed [%s","gApplicationLoaders");
        return NULL;
    }
    // jfieldID fieldLoaders = jenv->GetFieldID(clazzApplicationLoaders,"mLoaders","Landroid/util/ArrayMap<Ljava/lang/String;Ljava/lang/ClassLoader;>;");
    jfieldID fieldLoaders = jenv->GetFieldID(clazzApplicationLoaders,"mLoaders","Ljava/util/Map;");
    if (ClearException(jenv)) {
        ALOG("JniHelper - Exception","No Field :%s","mLoaders");
        return NULL;
    }
    jobject objLoaders = jenv->GetObjectField(objApplicationLoaders,fieldLoaders);
    if (ClearException(jenv)) {
        ALOG("JniHelper - Exception","No object :%s","mLoaders");
        return NULL;
    }
    //提取map中的values
    jclass clazzHashMap = jenv->GetObjectClass(objLoaders);
    jmethodID methodValues = jenv->GetMethodID(clazzHashMap,"values","()Ljava/util/Collection;");
    jobject values = jenv->CallObjectMethod(objLoaders,methodValues);

    jclass clazzValues = jenv->GetObjectClass(values);
    jmethodID methodToArray = jenv->GetMethodID(clazzValues,"toArray","()[Ljava/lang/Object;");
    if (ClearException(jenv)) {
        ALOG("JniHelper - Exception","No Method:%s","toArray");
        return NULL;
    }

    jobjectArray classLoaders = (jobjectArray)jenv->CallObjectMethod(values,methodToArray);
    if (ClearException(jenv)) {
        ALOG("JniHelper - Exception","CallObjectMethod failed :%s","toArray");
        return NULL;
    }

    int size = jenv->GetArrayLength(classLoaders);

    for(int i = 0 ; i < size ; i ++){
        jobject classLoader = jenv->GetObjectArrayElement(classLoaders,i);
        jclass clazzCL = jenv->GetObjectClass(classLoader);
        jmethodID loadClass = jenv->GetMethodID(clazzCL,"loadClass","(Ljava/lang/String;)Ljava/lang/Class;");
        jstring param = jenv->NewStringUTF(apn);
        jclass tClazz = (jclass)jenv->CallObjectMethod(classLoader,loadClass,param);
        if (ClearException(jenv)) {
            ALOG("JniHelper - Exception","No");
            continue;
        }
        return tClazz;
    }
    ALOG("JniHelper - Exception","No");
    return NULL;
}

jclass getAppClass(JNIEnv *jenv,const char *apn) {
    jclass clazzTarget = jenv->FindClass(apn);
    if (ClearException(jenv)) {
        ALOG("Exception","ClassMethodHook[Can't find class:%s in bootclassloader",apn);

        clazzTarget = findAppClass(jenv, apn);
        if(clazzTarget == NULL){
            ALOG("Exception","%s","Error in findAppClass");
            return NULL;
        }
    }
    return clazzTarget;
}