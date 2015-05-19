/*
 * @(#)ch_randelshofer_quaqua_osx_OSXAquaPainter.m
 *
 * Copyright (c) 2011 Werner Randelshofer, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms. 
 */

/**
 * Native code for class ch.randelshofer.quaqua.osx.OSXAquaPainter.
 *
 * @version $Id: ch_randelshofer_quaqua_osx_OSXAquaPainter.m 458 2013-05-29 15:44:33Z wrandelshofer $
 */
#include <jni.h>
#include "ch_randelshofer_quaqua_osx_OSXAquaPainter.h"

#ifndef __ppc__
#ifndef __ppc64__
#include <JavaNativeFoundation/JavaNativeFoundation.h>
#include <JavaRuntimeSupport/JavaRuntimeSupport.h>
/** Global renderer variable. */
static JRSUIRendererRef gRenderer = NULL;


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    getNativeCodeVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeGetNativeCodeVersion
  (JNIEnv *env, jclass javaClass) {
    return 1;
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeCreateControl
 * Signature: (Z)J
 */
JNIEXPORT jlong JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeCreateControl
  (JNIEnv *env, jclass javaClass, jboolean isFlipped) {
  return ptr_to_jlong(JRSUIControlCreate(isFlipped));
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeReleaseControl
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeReleaseControl
  (JNIEnv *env, jclass javaClass, jlong handle) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(handle);
    if (!control) return;
    JRSUIControlRelease(control);
}

/* Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativePaint
 * Signature: ([IIIJDDDD)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativePaint
  (JNIEnv *env, jclass javaClass, jintArray jimageData, jint jimgWidth, jint jimgHeight, 
    jlong jhandle, jdouble jx, jdouble jy, jdouble jwidth, jdouble jheight) {

    jboolean isCopy = JNI_FALSE;
    void *imageData = (*env)->GetPrimitiveArrayCritical(env, jimageData, &isCopy);
    if (!imageData) return;

    CGColorSpaceRef colorspace = CGColorSpaceCreateDeviceRGB();
    CGContextRef cgRef = CGBitmapContextCreate(imageData, jimgWidth, jimgHeight, 8, jimgWidth * 4, colorspace, kCGImageAlphaPremultipliedFirst | kCGBitmapByteOrder32Host);
    CGColorSpaceRelease(colorspace);
   
    JRSUIControlRef control = (JRSUIControlRef)jlong_to_ptr(jhandle);
    CGRect bounds = CGRectMake(jx, jy, jwidth, jheight);

    if (gRenderer == NULL) {
        gRenderer = JRSUIRendererCreate();
        if (gRenderer == NULL) return;
    }

    JRSUIControlDraw(gRenderer, control, cgRef, bounds);
    
    CGContextRelease(cgRef);
    
    (*env)->ReleasePrimitiveArrayCritical(env, jimageData, imageData, 0);
}

/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetWidget
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetWidget
  (JNIEnv *env, jclass javaClass, jlong jhandle, jint jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetWidget(control, jvalue);
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetValueByKey
 * Signature: (JID)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetValueByKey
  (JNIEnv *env, jclass javaClass, jlong jhandle, jint jkey, jdouble jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;

    CFTypeRef key = JRSUIGetKey(jkey);
    CFNumberRef value=CFNumberCreate(kCFAllocatorDefault,kCFNumberDoubleType,&jvalue);
    JRSUIControlSetValueByKey(control, key, value);

    CFRelease(value);
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetState
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetState
  (JNIEnv *env, jclass javaClass, jlong jhandle, jint jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetState(control, jvalue);
}
/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetSize
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetSize
 (JNIEnv *env, jclass javaClass, jlong jhandle, jint jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetSize(control, jvalue);
}

/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetDirection
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetDirection
  (JNIEnv *env, jclass javaClass, jlong jhandle, jint jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetDirection(control, jvalue);
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetOrientation
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetOrientation
 (JNIEnv *env, jclass javaClass, jlong jhandle, jint jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetOrientation(control, jvalue);
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetHorizontalAlignment
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetHorizontalAlignment
 (JNIEnv *env, jclass javaClass, jlong jhandle, jint jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetAlignmentHorizontal(control, jvalue);
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetVerticalAlignment
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetVerticalAlignment
  (JNIEnv *env, jclass javaClass, jlong jhandle, jint jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetAlignmentVertical(control, jvalue);
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetSegmentPosition
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetSegmentPosition
 (JNIEnv *env, jclass javaClass, jlong jhandle, jint jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetSegmentPosition(control, jvalue);
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetScrollBarPart
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetScrollBarPart
 (JNIEnv *env, jclass javaClass, jlong jhandle, jint jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetScrollBarPart(control, jvalue);
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetVariant
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetVariant
  (JNIEnv *env, jclass javaClass, jlong jhandle, jint jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetVariant(control, jvalue);
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetWindowType
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetWindowType
 (JNIEnv *env, jclass javaClass, jlong jhandle, jint jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetWindowType(control, jvalue);
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetShowArrows
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetShowArrows
 (JNIEnv *env, jclass javaClass, jlong jhandle, jboolean jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetShowArrows(control, jvalue);
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXAquaPainter
 * Method:    nativeSetAnimating
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_ch_randelshofer_quaqua_osx_OSXAquaPainter_nativeSetAnimating
  (JNIEnv *env, jclass javaClass, jlong jhandle, jboolean jvalue) {

    JRSUIControlRef control = (JRSUIControlRef) jlong_to_ptr(jhandle);
    if (!control) return;
    JRSUIControlSetAnimating(control, jvalue);
}


#endif /* ! __ppc64__ */
#endif /* ! __ppc__ */