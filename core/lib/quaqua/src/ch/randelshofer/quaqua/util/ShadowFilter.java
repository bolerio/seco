/*
 * @(#)ShadowFilter.java  1.0  28 March 2005
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.util;

import java.awt.*;
import java.awt.image.*;
/**
 * ShadowFilter changes the color of an image to all black, and 
 * reduces the alpha channel to 50 percent.
 * This is used by the Quaqua Look and Feel, to create a shadow image.
 *
 * @author  Werner Randelshofer
 * @version 1.0  28 March 2005  Created.
 */
public class ShadowFilter extends RGBImageFilter {
    
    /** Creates a new instance. */
    public ShadowFilter() {
        canFilterIndexColorModel = true;
    }
    
    /**
     * Creates a shadow image
     */
    public static Image createShadowImage (Image i) {
	ImageFilter filter = new ShadowFilter();
	ImageProducer prod = new FilteredImageSource(i.getSource(), filter);
	Image filteredImage = Toolkit.getDefaultToolkit().createImage(prod);
	return filteredImage;
    }
    
    public int filterRGB(int x, int y, int rgb) {
        return (rgb & 0xfe000000) >>> 1; 
    }    
}
