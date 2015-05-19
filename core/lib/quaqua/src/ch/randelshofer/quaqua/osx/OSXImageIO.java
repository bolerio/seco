/*
 * @(#)OSXImageIO.java
 * 
 * Copyright (c) 2009-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.osx;

import ch.randelshofer.quaqua.QuaquaManager;
import ch.randelshofer.quaqua.ext.batik.ext.awt.image.codec.tiff.TIFFDecodeParam;
import ch.randelshofer.quaqua.ext.batik.ext.awt.image.codec.tiff.TIFFImageDecoder;
import ch.randelshofer.quaqua.ext.batik.ext.awt.image.codec.util.MemoryCacheSeekableStream;
import ch.randelshofer.quaqua.util.Images;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.AccessControlException;

/**
 * {@code OSXImageIO} can read images using the Mac OS X Cocoa NSImage API.
 * <p>
 * Images are read using the Cocoa class
 * <a href="http://developer.apple.com/documentation/Cocoa/Reference/ApplicationKit/Classes/NSImage_Class/Reference/Reference.html"
 * >NSImage</a>.
 *
 * @author Werner Randelshofer
 * @version $Id: OSXImageIO.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class OSXImageIO {

    private static int EXPECTED_NATIVE_CODE_VERSION = 1;

    /** Prevent instance creation. */
    private OSXImageIO() {
    }

    /**
     * Reads a <code>BufferedImage</code> from the supplied <code>File</code>
     * using the Cocoa NSImage API.
     */
    public static BufferedImage read(File file) throws IOException {
        if (!isNativeCodeAvailable()) {
            throw new IOException("Native code is not available");
        }

        byte[] tiffData = nativeRead(file.getPath());
        if (tiffData == null) {
            throw new IOException("Couldn't read image from file " + file);
        }

        return decodeTIFF(tiffData);
    }

    /**
     * Reads a <code>BufferedImage</code> scaled to the specified size
     * from the supplied <code>File</code> using the Cocoa NSImage API.
     *
     * @param file
     *            The file containing the image.
     * @param width
     *            The preferred width.
     * @param height
     *            The preferred height.
     */
    public static BufferedImage read(File file, int width, int height) throws IOException {
        if (!isNativeCodeAvailable()) {
            throw new IOException("Native code is not available");
        }
        byte[] tiffData = nativeRead(file.getPath(), width, height);
        if (tiffData == null) {
            throw new IOException("Couldn't read image from file " + file);
        }

        return decodeTIFF(tiffData);
    }

    /**
     * Reads a <code>BufferedImage</code> from the supplied array using the
     * Cocoa NSImage API.
     */
    public static BufferedImage read(byte[] data) throws IOException {
        if (!isNativeCodeAvailable()) {
            throw new IOException("Native code is not available");
        }
        byte[] tiffData = nativeRead(data);
        if (tiffData == null) {
            throw new IOException("Couldn't read image from data array");
        }

        return decodeTIFF(tiffData);
    }

    /**
     * Returns a <code>BufferedImage</code> as the result of decoding
     * the data in the Mac OS X system clipboard using the Cocoa NSImage API.
     */
    public static BufferedImage readSystemClipboard() throws IOException {
        if (!isNativeCodeAvailable()) {
            throw new IOException("Native code is not available");
        }
        byte[] tiffData = nativeReadSystemClipboard();
        if (tiffData == null) {
            throw new IOException("Couldn't read an image from the clipboard");
        }

        return decodeTIFF(tiffData);
    }

    /** Decodes a byte array with a TIFF encoded image. */
    private static BufferedImage decodeTIFF(byte[] tiffData) throws IOException {

        TIFFImageDecoder decoder = new TIFFImageDecoder(
                new MemoryCacheSeekableStream(new ByteArrayInputStream(tiffData)),
                new TIFFDecodeParam());

        RenderedImage rImg = decoder.decodeAsRenderedImage(0);
        return Images.toBufferedImage(rImg);
    }

    /**
     * Reads an image from the specified path and returns it in a TIFF
     * encoded byte array.
     */
    private native static byte[] nativeRead(String path);

    /**
     * Reads an image from the specified path, scales it to the specified
     * size and returns it in a TIFF encoded byte array.
     * <p>
     * A preferred size is used if it is stored in the image file, but you
     * cannot expect the resulting Image to have these dimensions.<br>
     * If your application needs a specific size, you should check the
     * dimension.
     * <p>
     * If JNI fails to load the image (e.g. the native code could not be
     * loaded), <code>null</code> is returned.
     *
     * @param file
     *            The file containing the image.
     * @param width
     *            The preferred width.
     * @param height
     *            The preferred height.
     * @return The image loaded. <code>null</code>, if no image could be loaded
     *         by JNI.
     */
    private native static byte[] nativeRead(String path, int widht, int height);

    /**
     * Reads an image from the specified byte array, and returns it in a TIFF
     * encoded byte array.
     *
     * @param data
     *            The file containing the image data.
     * @return The image loaded. <code>null</code>, if no image could be loaded
     *         by JNI.
     */
    private native static byte[] nativeRead(byte[] data);

    /**
     * Attempts to read an image from the system clipboard.
     *
     * @return The image loaded. <code>null</code>, if no image could be loaded
     *         from the system clipboard.
     */
    private native static byte[] nativeReadSystemClipboard();


    //---
    /**
     * Returns the version of the native code library. If the version
     * does not match with the version that we expect, we can not use
     * it.
     * @return The version number of the native code.
     */
    private static native int nativeGetNativeCodeVersion();
    /**
     * This variable is set to true, if native code is available.
     */
    private static volatile Boolean isNativeCodeAvailable;

    /**
     * Returns true if native code is available.
     * This method also loads the native code.
     */
    public static boolean isNativeCodeAvailable() {
        if (isNativeCodeAvailable == null) {
            synchronized (OSXImageIO.class) {
                if (isNativeCodeAvailable == null) {
                    boolean success = false;
                    try {
                        String value = QuaquaManager.getProperty("Quaqua.jniIsPreloaded");
                        if (value == null) {
                            value = QuaquaManager.getProperty("Quaqua.JNI.isPreloaded");
                        }
                        String libraryName = null;
                        if (value != null && value.equals("true")) {
                            success = true;
                        } else {
                            // Try to load 64-bit libraries if possible
                            String[] libraryNames;
                            String osArch = System.getProperty("os.arch");
                            if (osArch.equals("x86_64")) {
                                libraryNames = new String[]{"quaqua64"};
                            } else {
                                libraryNames = new String[]{"quaqua64", "quaqua"};
                            }
                            for (int i=0;i<libraryNames.length;i++) {
                                libraryName=libraryNames[i];
                                try {
                                    JNILoader.loadLibrary(libraryName);
                                    success = true;
                                    break;
                                } catch (UnsatisfiedLinkError e) {
                                    System.err.println("Warning: " + OSXImageIO.class + " couldn't load library \"" + System.mapLibraryName(libraryName) + "\". " + e);
                                    success = false;
                                } catch (AccessControlException e) {
                                    System.err.println("Warning: " + OSXImageIO.class + " access controller denied loading library \"" + System.mapLibraryName(libraryName) + "\". " + e);
                                    success = false;
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                    System.err.println("Warning: " + OSXImageIO.class + " couldn't load library \"" + System.mapLibraryName(libraryName) + "\". " + e);
                                    success = false;
                                }
                            }
                        }

                        if (success) {
                            try {
                                int nativeCodeVersion = nativeGetNativeCodeVersion();
                                if (nativeCodeVersion != EXPECTED_NATIVE_CODE_VERSION) {
                                    System.err.println("Warning: " + OSXImageIO.class + " can't use library " + libraryName + ". It has version " + nativeCodeVersion + " instead of " + EXPECTED_NATIVE_CODE_VERSION);
                                    success = false;
                                }
                            } catch (UnsatisfiedLinkError e) {
                                System.err.println("Warning: " + OSXImageIO.class + " could load library " + libraryName + " but can't use it. " + e);
                                success = false;
                            }
                        }

                    } finally {
                        isNativeCodeAvailable = Boolean.valueOf(success);
                    }
                }
            }
        }
        return isNativeCodeAvailable == Boolean.TRUE;
    }
}
