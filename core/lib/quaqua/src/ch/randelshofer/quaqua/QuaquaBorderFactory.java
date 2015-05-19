/*
 * @(#)QuaquaBorderFactory.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.border.QuaquaNativeButtonStateBorder;
import ch.randelshofer.quaqua.border.CachedImageBevelBorder;
import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.border.BackgroundBorderUIResource;
import ch.randelshofer.quaqua.border.ButtonStateBorder;
import ch.randelshofer.quaqua.border.FocusedBorder;
import ch.randelshofer.quaqua.border.ImageBevelBorder;
import ch.randelshofer.quaqua.border.QuaquaNativeImageBevelBorder;
import ch.randelshofer.quaqua.osx.OSXAquaPainter;
import java.awt.*;
import javax.swing.border.*;
import java.awt.image.*;

/**
 * Creates an ImageBevelBorder instance optimized for this JVM.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaBorderFactory.java 464 2014-03-22 12:32:00Z wrandelshofer $
 */
public class QuaquaBorderFactory {

    /**
     * Prevent instance creation of the factory.
     */
    private QuaquaBorderFactory() {
        //1.4.2_05
    }

    /** Creates a new instance of an ImageBevelBorder optimized for this JVM.
     * @param img the image
     * @param borderInsets the border insets
     * @return the created border
    */
    public static Border create(Image img, Insets borderInsets) {
        return create(img, borderInsets, borderInsets);
    }

    /**
     * Creates a new instance of an ImageBevelBorder with the given image and insets.
     * The image has different insets than the border.
     * @param img the image
     * @param imageInsets the image insets
     * @param borderInsets the border insets
     * @return the created border
     */
    public static Border create(Image img, Insets imageInsets, Insets borderInsets) {
        return create(img, imageInsets, borderInsets, true, null, true);
    }

    /**
     * Creates a new instance of an ImageBevelBorder with the given image and insets.
     * The image has different insets than the border.
     * @param img the image
     * @param borderInsets the border insets
     * @param fillContentArea whether to fill the content area
     * @param isCaching whether to cache the rendered border
     * @return the created border
     */
    public static Border create(Image img, Insets borderInsets, boolean fillContentArea, boolean isCaching) {
        return create(img, borderInsets, borderInsets, fillContentArea, null, isCaching);
    }

    /**
     * Creates a new instance of an ImageBevelBorder with the given image and insets.
     * The image has different insets than the border.
     * @param img the image
     * @param imageInsets the image insets
     * @param borderInsets the border insets
     * @param fillContentArea whether to fill the content area
     * @return the created border
     */
    public static Border create(Image img, Insets imageInsets, Insets borderInsets, boolean fillContentArea) {
        return create(img, imageInsets, borderInsets, fillContentArea, null, true);
    }

    /**
     * Creates a new instance of an ImageBevelBorder with the given image and insets.
     * The image has different insets than the border.
     * @param img the image
     * @param imageInsets the image insets
     * @param borderInsets the border insets
     * @param fillContentArea whether to fill the content area
     * @param fillColor the fill color
     * @param isCaching whether to cache the rendered border
     * @return the created border
     */
    public static Border create(Image img, Insets imageInsets, Insets borderInsets, boolean fillContentArea, Color fillColor, boolean isCaching) {
        if (isCaching) {
            return new CachedImageBevelBorder.UIResource(img, imageInsets, borderInsets, fillContentArea);
        } else {
            return new ImageBevelBorder.UIResource(img, imageInsets, borderInsets, fillContentArea, fillColor);
        }
    }
    /**
     * Creates a new instance of NativeImageBevelBorder with the given widget and insets.
     * The image has different insets than the border.
     * @param widget the native widget
     * @param painterInsets the insets between the widget and the bevel
     * @param imageBevel bevel insets
     * @param borderInsets the border insets
     * @param fillContentArea whether to fill the content area
     * @return the created border
     */
    public static Border createNativeImageBevelBorder(OSXAquaPainter.Widget widget, Insets painterInsets, Insets imageBevel, Insets borderInsets, boolean fillContentArea) {
            return new QuaquaNativeImageBevelBorder.UIResource(widget, painterInsets,imageBevel, borderInsets, fillContentArea);
    }

    /**
     * Creates a new instance of a border for square buttons.
     * @return the created border
     */
    public static Border createSquareButtonBorder() {
        return new QuaquaSquareButtonBorder();
    }

    /**
     * Creates a new instance of a border for placard buttons.
     * @return the created border
     */
    public static Border createPlacardButtonBorder() {
        return new QuaquaPlacardButtonBorder();
    }

    public static Border create(String location, Insets borderInsets, boolean fill) {
        return create(QuaquaIconFactory.createImage(location), borderInsets, borderInsets, fill, null, false);
    }

    public static Border create(String location, Insets imageInsets, Insets borderInsets, boolean fill) {
        return create(QuaquaIconFactory.createImage(location), imageInsets, borderInsets, fill, null, false);
    }

    public static Border create(String location, Insets imageInsets, Insets borderInsets, boolean fill, Color fillColor) {
        return create(QuaquaIconFactory.createImage(location), imageInsets, borderInsets, fill, fillColor, false);
    }
    public static Border create(String location, Rectangle subimage, Insets imageInsets, Insets borderInsets, boolean fill) {
        return create(QuaquaIconFactory.createBufferedImage(location, subimage), imageInsets, borderInsets, fill, null, false);
    }
    public static Border create(String location, Rectangle subimage, Insets imageInsets, Insets borderInsets, boolean fill, Color fillColor) {
        return create(QuaquaIconFactory.createBufferedImage(location, subimage), imageInsets, borderInsets, fill, fillColor, false);
    }

    public static Border createBackgroundBorder(String location, Insets imageInsets, Insets borderInsets, boolean fill) {
        return new BackgroundBorderUIResource(create(QuaquaIconFactory.createImage(location), imageInsets, borderInsets, fill, null, false));
    }

    public static Border createBackgroundBorder(String location, Insets imageInsets, Insets borderInsets, boolean fill, Color fillColor) {
        return new BackgroundBorderUIResource(create(QuaquaIconFactory.createImage(location), imageInsets, borderInsets, fill, fillColor, false));
    }

    public static Border createButtonStateBorder(String location, int tileCount, boolean isTiledHorizontaly,
            Insets imageInsets, Insets borderInsets, boolean fill) {
        return new ButtonStateBorder.UIResource(QuaquaIconFactory.createImage(location), tileCount, isTiledHorizontaly,
                imageInsets, borderInsets, fill);
    }

    public static Border createNativeButtonStateBorder(OSXAquaPainter.Widget widget,
            Insets imageInsets, Insets borderInsets, boolean drawFocusRing) {
        try {
            Border border = new QuaquaNativeButtonStateBorder.UIResource(widget,
                    imageInsets, borderInsets);

            if (drawFocusRing) {
                border = new FocusedBorder.UIResource(border);
            }

            return border;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Creates an array of ImageBevelBorders.
     *
     * @param location URL of the image that contains the border images.
     * @param insets Insets of the borders.
     * @param count Number of borders to generate.
     * @param horizontal True, if the image is to be split horizontally to get
     * the individual image of each border. If set to false, the image is split
     * vertically.
     * @return the created border array of type {@code Border[]}
     */
    public static Object create(String location, Insets insets, int count, boolean horizontal) {
        return create(location, insets, count, horizontal, true, true);
    }

    public static Object create(String location, Insets insets, int count, boolean horizontal, boolean fill, boolean isCaching) {
        BufferedImage[] images = Images.split(
                QuaquaIconFactory.createImage(location),
                count, horizontal);
        Border[] borders = new Border[count];
        for (int i = 0; i < count; i++) {
            borders[i] = create(images[i], insets, insets, fill, null, isCaching);
        }
        return borders;
    }

    public static Border createButtonBorder(String type) {
        return new BackgroundBorderUIResource(new QuaquaButtonBorder("push"));
    }
}
