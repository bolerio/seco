/*
 * @(#)InsetsUtil.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.util;

import java.awt.*;
/**
 * Utilities for Insets.
 *
 * @author  Werner Randelshofer
 * @version $Id: InsetsUtil.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class InsetsUtil {

    public static void setTo(int top,int left , int bottom, int right, Insets i2) {
               i2.top = top; 
        i2.left = left; 
        i2.bottom = bottom; 
        i2.right = right;

    }
    
    /** Prevent instance creation. */
    private InsetsUtil() {
    }
    
    public static Insets add(Insets i1, Insets i2) {
        return new Insets(
        i1.top + i2.top, 
        i1.left + i2.left, 
        i1.bottom + i2.bottom, 
        i1.right + i2.right
        );
    }
    public static Insets add(int top, int left, int bottom, int right, Insets i1) {
        return new Insets(
        i1.top + top, 
        i1.left + left, 
        i1.bottom + bottom, 
        i1.right + right
        );
    }
    public static void addTo(int top, int left, int bottom, int right, Insets i2) {
        i2.top += top; 
        i2.left += left; 
        i2.bottom += bottom; 
        i2.right += right;
    }
    public static void addTo(Insets i1, Insets i2) {
        i2.top += i1.top; 
        i2.left += i1.left; 
        i2.bottom += i1.bottom; 
        i2.right += i1.right;
    }
    public static void addTo(Insets i1, Rectangle i2) {
        i2.x -= i1.top; 
        i2.y-= i1.left; 
        i2.height += i1.top+i1.bottom; 
        i2.width+= i1.left+i1.right;
    }
    public static void addTo(Insets i1, Dimension i2) {
        i2.height += i1.top+i1.bottom; 
        i2.width+= i1.left+i1.right;
    }
    public static void setTo(Insets i1, Insets i2) {
        i2.top = i1.top; 
        i2.left = i1.left; 
        i2.bottom = i1.bottom; 
        i2.right = i1.right;
    }
    public static void clear(Insets i2) {
        i2.top = 
        i2.left =
        i2.bottom = 
        i2.right = 0;
    }
    public static void subtractInto(Insets i1, Rectangle r2) {
        r2.x += i1.left;
        r2.y += i1.top;
        r2.width -= i1.left + i1.right;
        r2.height -= i1.top + i1.bottom;
        }
    public static void subtractInto(int top, int left, int bottom, int right, Rectangle r2) {
        r2.x += left;
        r2.y += top;
        r2.width -= left + right;
        r2.height -= top + bottom;
        }
        public static void subtractInto(Insets i1, Insets i2) {
        i2.top -= i1.top; 
        i2.left -= i1.left; 
        i2.bottom -= i1.bottom; 
        i2.right -= i1.right;
    }

}
