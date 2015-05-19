/*
 * @(#)ArrayUtil.java  
 *
 * Copyright (c) 2003-2010 Werner Randelshofer, Switzerland
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */

//package ch.randelshofer.util;
package ch.randelshofer.quaqua.util;
import java.util.*;
/**
 * ArrayUtil.
 *
 * @author  Werner Randelshofer
 * @version $Id: ArrayUtil.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class ArrayUtil {
    
    /** Prevent instance creation. */
    private ArrayUtil() {
    }
    
    /*
    public static Vector asVector(int[] a) {
        Vector list = new Vector(a.length);
        for (int i=0; i < a.length; i++) {
            list.addElement(new Integer(a[i]));
        }
        return list;
    }
    public static List asList(Object[] a) {
        return Arrays.asList(a);
    }*/
    public static ArrayList asList(int[] a) {
        ArrayList list = new ArrayList(a.length);
        for (int i=0; i < a.length; i++) {
            list.add(a[i]);
        }
        return list;
    }/*
    public static ArrayList asList(int[] a, int off, int len) {
        ArrayList list = new ArrayList(a.length);
        int end = off + len;
        for (; off < end; off++) {
            list.add(new Integer(a[off]));
        }
        return list;
    }
    */
    private final static char[] hexChars = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    
    public static String toHexString(byte[] b, int off, int len) {
        StringBuffer buf = new StringBuffer();
        int end = off + len;
        for (; off < end; off++) {
            buf.append(hexChars[(b[off] & 0xf0) >>> 4]);
            buf.append(hexChars[b[off] & 0x0f]);
        }
        return buf.toString();
    }
    
    public static String toHexString(byte[] b) {
        return toHexString(b, 0, b.length);
    }

    public static int[] truncate(int[] a, int off, int len) {
        int[] b = new int[len];
        System.arraycopy(a, off, b, 0, len);
        return b;
    }
}
