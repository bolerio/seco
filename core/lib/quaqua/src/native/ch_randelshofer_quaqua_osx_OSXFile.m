/*
 * @(#)ch_randelshofer_quaqua_osx_OSXFile.m
 *
 * Copyright (c) 2004-2007 Werner Randelshofer, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms.
 */

/**
 * Native code for class ch.randelshofer.quaqua.osx.OSXFile.
 *
 * @version $Id: ch_randelshofer_quaqua_osx_OSXFile.m 458 2013-05-29 15:44:33Z wrandelshofer $
 */

#include <stdio.h>
#include <jni.h>
#include "ch_randelshofer_quaqua_osx_OSXFile.h"
#import <Cocoa/Cocoa.h>
#import <CoreServices/CoreServices.h>

/*
 * Related documentation:
 * ----------------------
 * Serializing an Alias into a stream of bytes:
 * http://developer.apple.com/qa/qa2004/qa1350.html
 */



/*
 * Class:     ch_randelshofer_quaqua_osx_OSXFile
 * Method:    getFileType
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeGetFileType
  (JNIEnv *env, jclass instance, jstring pathJ) {

    // Assert arguments
    if (pathJ == NULL) return false;

    // Convert Java String to C char array
    const char* pathC;
    pathC = (*env)->GetStringUTFChars(env, pathJ, 0);

    // Do the API calls
    FSRef fileRef;
    OSErr err;
    Boolean isAlias, isFolder;
    err = FSPathMakeRef(pathC, &fileRef, NULL);
    if (err == 0) {
        err = FSIsAliasFile(&fileRef, &isAlias, &isFolder);
    }

    // Release the C char array
    (*env)->ReleaseStringUTFChars(env, pathJ, pathC);

    // Return the result
    return (err == 0) ?
		((isAlias) ? 2 : ((isFolder) ? 1 : 0)) :
		-1;
}
/*
 * Class:     ch_randelshofer_quaqua_osx_OSXFile
 * Method:    resolveAlias
 * Signature: (Ljava/lang/String;Z)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeResolveAlias__Ljava_lang_String_2Z
 (JNIEnv *env, jclass instance, jstring aliasPathJ, jboolean noUI)
{
    // Assert arguments
    if (aliasPathJ == NULL) return false;

    // Convert Java filename to C filename
    const char *aliasPathC;
    aliasPathC = (*env)->GetStringUTFChars(env, aliasPathJ, 0);
    
    // Do the API calls
    FSRef fileRef;
    OSErr err;
    OSStatus status;
    Boolean wasAliased, targetIsFolder;
    UInt8 resolvedPathC[2048];

    int outputBufLen;
    err = FSPathMakeRef(aliasPathC, &fileRef, NULL);
    if (err == 0) {
        err = FSResolveAliasFileWithMountFlags(
                             &fileRef, 
                             true, // resolve alias chains
                             &targetIsFolder,
                             &wasAliased,
                             (noUI) ? kResolveAliasFileNoUI : 0 // mount flags
              );
    }
    if (err == 0) {
        if (wasAliased) {
            status = FSRefMakePath(&fileRef, resolvedPathC, 2048);
            if (status != 0) err = 1;
        }
    }

    // Release the C filename
    (*env)->ReleaseStringUTFChars(env, aliasPathJ, aliasPathC);


    // Return the result
    return (err == 0 && wasAliased) ? (*env)->NewStringUTF(env, resolvedPathC) : NULL;
}

/*
 * Class:     ch_randelshofer_quaqua_osx_OSXFile
 * Method:    resolveAliasType
 * Signature: (Ljava/lang/String;Z)I
 */
JNIEXPORT jint JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeResolveAliasType__Ljava_lang_String_2Z
   (JNIEnv *env, jclass instance, jstring aliasPathJ, jboolean noUI)
{
    // Assert arguments
    if (aliasPathJ == NULL) return false;

    // Convert Java filename to C filename
    const char *aliasPathC;
    aliasPathC = (*env)->GetStringUTFChars(env, aliasPathJ, 0);
    
    // Do the API calls
    FSRef fileRef;
    OSErr err;
    OSStatus status;
    Boolean wasAliased, targetIsFolder;
    UInt8 resolvedPathC[2048];

    int outputBufLen;
    err = FSPathMakeRef(aliasPathC, &fileRef, NULL);
    if (err == 0) {
        err = FSResolveAliasFileWithMountFlags(
                             &fileRef, 
                             false, // resolve alias chains
                             &targetIsFolder,
                             &wasAliased,
                             (noUI) ? kResolveAliasFileNoUI : 0 // mount flags
              );
    }
    if (err == 0) {
        if (wasAliased) {
            status = FSRefMakePath(&fileRef, resolvedPathC, 2048);
            if (status != 0) err = 1;
        }
    }

    // Release the C filename
    (*env)->ReleaseStringUTFChars(env, aliasPathJ, aliasPathC);


    // Return the result
    return (err == 0) ? ((targetIsFolder) ? 1 : 0) : -1;
}

/*
 * Class:     ch_randelshofer_quaqua_osx_OSXFile
 * Method:    toSerializedAlias
 * Signature: (Ljava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeToSerializedAlias
  (JNIEnv *env, jclass instance, jstring aliasPathJ)
{
    // Assert arguments
    if (aliasPathJ == NULL) return NULL;

    //
    jbyteArray serializedAlias = NULL;

    // Convert Java filename to C filename
    const char *aliasPathC;
    aliasPathC = (*env)->GetStringUTFChars(env, aliasPathJ, 0);

    // Do the API calls
    FSRef fileRef;
    OSErr err;
    AliasHandle aliasHdl;
    CFDataRef dataRef = NULL;
    const UInt8* dataBytes; // bytes of dataRef
    int length; // length of the dataBytes array

    err = FSPathMakeRef(aliasPathC, &fileRef, NULL);
    if (err == 0) {
        err = FSNewAlias(NULL, &fileRef, &aliasHdl);
    }

    if (err == 0) {
        dataRef = CFDataCreate(
                        kCFAllocatorDefault,
                        (UInt8*) *aliasHdl,
                        GetHandleSize((Handle) aliasHdl)
                  );
        err = (NULL == dataRef);
    }
    if (err == 0) {
        length = CFDataGetLength(dataRef);
        serializedAlias = (*env)->NewByteArray(env, length);
        err = (NULL == serializedAlias);
    }
    if (err == 0) {
        dataBytes = CFDataGetBytePtr(dataRef);
        (*env)->SetByteArrayRegion(env, serializedAlias, 0, length, dataBytes);
    }

    // Release the C filename
    (*env)->ReleaseStringUTFChars(env, aliasPathJ, aliasPathC);

    // Release the other stuff
    if (dataRef != NULL) CFRelease(dataRef);
    if (aliasHdl != NULL) DisposeHandle((Handle) aliasHdl);

    // Return the result
    return serializedAlias;
}

/*
 * Class:     ch_randelshofer_quaqua_osx_OSXFile
 * Method:    jniResolveAlias
 * Signature: ([BZ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeResolveAlias___3BZ
  (JNIEnv *env, jclass instance, jbyteArray serializedAlias, jboolean noUI) 
{
    // Assert arguments
    if (serializedAlias == NULL) return false;
    
    //
    FSRef fileRef;
    OSErr err;
    AliasHandle aliasHdl;
    CFDataRef dataRef;
    UInt8* serializedAliasBytes; // bytes of serializedAlias
    int length; // length of serializedAlias
    UInt8 resolvedPathC[2048];
    Boolean wasChanged;
    OSStatus status;

    length = (*env)->GetArrayLength(env, serializedAlias);
    serializedAliasBytes = (*env)->GetByteArrayElements(env, serializedAlias, NULL);
    err = (NULL == serializedAliasBytes);

    if (err == 0) {
        dataRef = CFDataCreate(kCFAllocatorDefault,
                               (UInt8*) serializedAliasBytes, 
                               length
                  );

        aliasHdl = (AliasHandle) NewHandle(length);
        err = (NULL == aliasHdl);
    }
    if (err == 0) {
        CFDataGetBytes(dataRef,
                       CFRangeMake(0, length),
                       (UInt8*) *aliasHdl
       );
    
        err = FSResolveAliasWithMountFlags(NULL,
                             aliasHdl,
                             &fileRef,
                             &wasChanged,
                             (noUI) ? kResolveAliasFileNoUI : 0
              );
    }
    if (err == 0) {
        status = FSRefMakePath(&fileRef, resolvedPathC, 2048);
        if (status != 0) err = 1;
    }

    // Release allocated stuff
    (*env)->ReleaseByteArrayElements(env, serializedAlias, serializedAliasBytes, JNI_ABORT);
    if (aliasHdl != NULL) DisposeHandle((Handle) aliasHdl);

    // Return the result
    return (err == 0) ? (*env)->NewStringUTF(env, resolvedPathC) : NULL;
}

/*
 * Class:     ch_randelshofer_quaqua_osx_OSXFile
 * Method:    jniResolveAliasType
 * Signature: ([BZ)I
 */
JNIEXPORT jint JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeResolveAliasType___3BZ
  (JNIEnv *env, jclass instance, jbyteArray serializedAlias, jboolean noUI) 
{
    // Assert arguments
    if (serializedAlias == NULL) return false;
    
    //
    OSErr err;
    AliasHandle aliasHdl;
    CFDataRef dataRef;
    UInt8* serializedAliasBytes; // bytes of serializedAlias
    int length; // length of serializedAlias
    UInt8 resolvedPathC[2048];
    OSStatus status;
	FSAliasInfoBitmap whichInfo;
    FSAliasInfo info;

    length = (*env)->GetArrayLength(env, serializedAlias);
    serializedAliasBytes = (*env)->GetByteArrayElements(env, serializedAlias, NULL);
    err = (NULL == serializedAliasBytes);

    if (err == 0) {
        dataRef = CFDataCreate(kCFAllocatorDefault,
                               (UInt8*) serializedAliasBytes, 
                               length
                  );

        aliasHdl = (AliasHandle) NewHandle(length);
        err = (NULL == aliasHdl);
    }
    if (err == 0) {
        CFDataGetBytes(dataRef,
                       CFRangeMake(0, length),
                       (UInt8*) *aliasHdl
       );
    
		err = FSCopyAliasInfo (
			aliasHdl,
			NULL, //targetName
			NULL, //volumeName
			NULL, //pathString
			&whichInfo,
			&info
		);
    }
	
    // Release allocated stuff
    (*env)->ReleaseByteArrayElements(env, serializedAlias, serializedAliasBytes, JNI_ABORT);
    if (aliasHdl != NULL) DisposeHandle((Handle) aliasHdl);

    // Return the result
    return (err == 0 && (whichInfo & kFSAliasInfoIsDirectory) != 0) ?
		 ((info.isDirectory) ? 1 : 0) : 
		 -1;
}
  

/*
 * Class:     ch_randelshofer_quaqua_osx_OSXFile
 * Method:    getLabel
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeGetLabel
  (JNIEnv *env, jclass instance, jstring pathJ) {

    // Assert arguments
    if (pathJ == NULL) return -1;

    // Convert Java String to C char array
    const char *pathC = (*env)->GetStringUTFChars(env, pathJ, 0);

    // Do the API calls
    FSRef fileRef;
    OSErr err;
    FSCatalogInfo catalogInfo;
    FInfo *fileInfo;
    int fileLabel;
    err = FSPathMakeRef(pathC, &fileRef, NULL);
    if (err == 0) {
        err = FSGetCatalogInfo(&fileRef, kFSCatInfoFinderInfo, &catalogInfo, NULL, NULL, NULL);
    }
    if (err == 0) {
        fileInfo = (FInfo*) &catalogInfo.finderInfo;
        fileLabel = (fileInfo->fdFlags & 0xe) >> 1;
    }

    // Release the C char array
    (*env)->ReleaseStringUTFChars(env, pathJ, pathC);

    // Return the result
    return (err == 0) ? fileLabel : -1;
}

/*
 * Class:     ch_randelshofer_quaqua_osx_OSXFile
 * Method:    getKindString
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeGetKindString
  (JNIEnv *env, jclass instance, jstring pathJ) {

    // Assert arguments
    if (pathJ == NULL) return NULL;

    // Convert Java String to C char array
    const char *pathC = (*env)->GetStringUTFChars(env, pathJ, 0);

    // Do the API calls
    FSRef fileRef;
    OSErr err;
    CFStringRef outKindString;
    err = FSPathMakeRef(pathC, &fileRef, NULL);
    jstring kindJ;
    if (err == 0) {
        err = LSCopyKindStringForRef(&fileRef, &outKindString);
    }
    if (err == 0) {
        CFRange range;
        range.location = 0;
        // Note that CFStringGetLength returns the number of UTF-16 characters,
        // which is not necessarily the number of printed/composed characters
        range.length = CFStringGetLength(outKindString);
        UniChar charBuf[range.length];
        CFStringGetCharacters(outKindString, range, charBuf);
        kindJ = (*env)->NewString(env, (jchar *)charBuf, (jsize)range.length);
        CFRelease(outKindString);
    }

    // Release the C char array
    (*env)->ReleaseStringUTFChars(env, pathJ, pathC);

    // Return the result
    return (err == 0) ? kindJ : NULL;
}

/*
 * Class:     ch_randelshofer_quaqua_osx_OSXFile
 * Method:    getIconImage
 * Signature: (Ljava/lang/String;I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeGetIconImage
  (JNIEnv *env, jclass javaClass, jstring pathJ, jint size) {

    // Assert arguments
    if (pathJ == NULL) return NULL;

    // Prepare result
    jbyteArray result = NULL;
    
    // Allocate a memory pool
    NSAutoreleasePool* pool = [NSAutoreleasePool new];

    // Convert Java String to NS String
    const jchar *pathC = (*env)->GetStringChars(env, pathJ, NULL);
    NSString *pathNS = [NSString stringWithCharacters:(UniChar *)pathC
        length:(*env)->GetStringLength(env, pathJ)];
    (*env)->ReleaseStringChars(env, pathJ, pathC);

    // Get the icon image
    NSWorkspace* workspace = [NSWorkspace sharedWorkspace];
    NSImage* image = [workspace iconForFile:pathNS];
    //NSLog (@"%@", image);
    if (image != NULL) {

        // Create a scaled version of the image by choosing the best
        // representation.
        NSSize desiredSize = { size, size };
        NSImage* scaledImage = [[[NSImage alloc] initWithSize:desiredSize] autorelease];
        [scaledImage setSize: desiredSize];
        NSImageRep* imageRep;
        NSImageRep* bestRep = NULL;
        NSEnumerator *enumerator = [[image representations] objectEnumerator];
        while (imageRep = [enumerator nextObject]) {
            if ([imageRep pixelsWide] >= size &&
                (bestRep == NULL || [imageRep pixelsWide] < [bestRep pixelsWide]) ) {

                bestRep = imageRep;
            }
        }
        if (bestRep != NULL) {
            [scaledImage addRepresentation: bestRep];
        } else {
            // We should never get to here, but if we do, we use the
            // original image.
            scaledImage = image;
            [scaledImage setSize: desiredSize];
        }

        // Convert image to TIFF
        NSData* dataNS = [scaledImage TIFFRepresentation];
        if (dataNS != NULL) {
            unsigned len = [dataNS length];
            void* bytes = malloc(len);
            if (bytes != NULL) {
                [dataNS getBytes: bytes];
                result = (*env)->NewByteArray(env, len);
                if (result != NULL) {
                    (*env)->SetByteArrayRegion(env, result, 0, len, (jbyte*)bytes);
                }
            }
            free(bytes);
        }
    }


    // Release memory pool
    [pool release];
    
    return result;
}


/*
 * Class:     ch_randelshofer_quaqua_osx_OSXFile
 * Method:    nativeGetQuickLookThumbnailImage
 * Signature: (Ljava/lang/String;I)[B
 */
typedef long (*QuickLookRequest)(CFAllocatorRef, CFURLRef, CGSize, CFDictionaryRef);
JNIEXPORT jbyteArray JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeGetQuickLookThumbnailImage
(JNIEnv *env, jclass javaClass, jstring pathJ, jint size) {
    // Assert arguments
    if (pathJ == NULL) return NULL;
    
    // Prepare result
    jbyteArray result = NULL;
    
    // Allocate a memory pool
    NSAutoreleasePool* pool = [NSAutoreleasePool new];
    
    // Convert Java String to NS String
    const jchar *pathC = (*env)->GetStringChars(env, pathJ, NULL);
    NSString *pathNS = [NSString stringWithCharacters:(UniChar *)pathC
                                               length:(*env)->GetStringLength(env, pathJ)];
    (*env)->ReleaseStringChars(env, pathJ, pathC);
    NSURL *fileURL = [NSURL fileURLWithPath:pathNS];
    
    // Load the QuickLook bundle
    NSURL *bundleURL = [NSURL fileURLWithPath:@"/System/Library/Frameworks/QuickLook.framework"];
    CFBundleRef cfBundle = CFBundleCreate(kCFAllocatorDefault, (CFURLRef)bundleURL);
    NSBitmapImageRep *image = nil;
    // If we didn'succeed, the framework does not exist. The image is null, so null is returned 
    if (cfBundle) {
        NSDictionary *dict = [NSDictionary dictionaryWithObject:[NSNumber numberWithBool:YES] 
                                                         forKey:@"IconMode"];
        // Get the thumbnail function pointer
        QuickLookRequest functionRef = CFBundleGetFunctionPointerForName(cfBundle,
                                                                         CFSTR("QLThumbnailImageCreate"));
        // Perform the request
        CGImageRef ref = (CGImageRef) functionRef(kCFAllocatorDefault,
                                     (CFURLRef)fileURL, 
                                     CGSizeMake(size, size),
                                     (CFDictionaryRef)dict);

        CFRelease(cfBundle);
        // Use the created image to create a bitmap image representation
        if (ref) {
            image = [[NSBitmapImageRep alloc] initWithCGImage:ref];
            CFRelease(ref);
        }
    }
    
    //NSLog (@"%@", image);
    if (image != NULL) {
        // Convert image to TIFF
        NSData* dataNS = [image TIFFRepresentation];
        if (dataNS != NULL) {
            unsigned len = [dataNS length];
            void* bytes = malloc(len);
            [dataNS getBytes: bytes];
            result = (*env)->NewByteArray(env, len);
            (*env)->SetByteArrayRegion(env, result, 0, len, (jbyte*)bytes);
            free(bytes);
        }
    }
    
    
    // Release memory pool
    [pool release];
    
    return result;
}

/*
 * Class:     ch_randelshofer_quaqua_osx_OSXFile
 * Method:    getBasicItemInfoFlags
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeGetBasicItemInfoFlags
  (JNIEnv *env, jclass javaClass, jstring pathJ) {
    // Assert arguments
    if (pathJ == NULL) return -1;

    // Convert Java String to C char array
    const char *pathC = (*env)->GetStringUTFChars(env, pathJ, 0);

    // Do the API calls
    FSRef fileRef;
    OSErr err;
    LSItemInfoRecord itemInfoRecord;
    err = FSPathMakeRef(pathC, &fileRef, NULL);
    if (err == 0) {
        err = LSCopyItemInfoForRef(&fileRef, kLSRequestBasicFlagsOnly, &itemInfoRecord);
    }

    // Release the C char array
    (*env)->ReleaseStringUTFChars(env, pathJ, pathC);

    // Return the result
    return (err == 0) ? itemInfoRecord.flags : 0;
}

JNIEXPORT jstring JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeGetDisplayName
  (JNIEnv *env, jclass javaClass, jstring pathJ) {

    // Assert arguments
    if (pathJ == NULL) return NULL;

    // Allocate a memory pool
    NSAutoreleasePool* pool = [NSAutoreleasePool new];

    // Convert Java String to NS String
    const jchar *pathC = (*env)->GetStringChars(env, pathJ, NULL);
    NSString *pathNS = [NSString stringWithCharacters:(UniChar *)pathC
        length:(*env)->GetStringLength(env, pathJ)];
    (*env)->ReleaseStringChars(env, pathJ, pathC);

    // Do the API calls
    NSFileManager *fileManagerNS = [NSFileManager defaultManager];
    NSString *displayNameNS = [fileManagerNS displayNameAtPath: pathNS];

    // Convert NSString to jstring
    jstring displayNameJ = (*env)->NewStringUTF(env, [displayNameNS UTF8String]);

    // Release memory pool
    [pool release];

    // Return the result
    return displayNameJ;
}

/*
 * Class:     ch_randelshofer_quaqua_osx_OSXFile
 * Method:    getNativeCodeVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ch_randelshofer_quaqua_osx_OSXFile_nativeGetNativeCodeVersion
  (JNIEnv *env, jclass javaClass) {
    return 5;
}


/*JNI function definitions end*/
