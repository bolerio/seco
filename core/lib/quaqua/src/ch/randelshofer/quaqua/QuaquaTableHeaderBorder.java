/*
 * @(#)QuaquaTableHeaderBorder.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.border.BackgroundBorder;
import java.awt.*;
//import javax.swing.text.*;
import javax.swing.border.*;

/**
 * QuaquaTableHeaderBorder.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaTableHeaderBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaTableHeaderBorder implements BackgroundBorder {

    /** Location of the border images. */
    private String imagesLocation;
    private Insets imageInsets;
    /** Array with image bevel borders.
     * This array is created lazily.
     **/
    private Border[] borders;
    /**
     * Column index.
     */
    private int columnIndex = 0;
    /**
     * Whether the column is sorted.
     */
    private boolean isSorted = false;
    /**
     * Whether the column has focus.
     */
    private boolean isOnActiveWindow = false;
    private Border tableHeaderBackground = new Border() {

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            getBorder(c).paintBorder(c, g, x, y, width, height);
        }
    };

    /** Creates a new instance. */
    public QuaquaTableHeaderBorder(String imagesLocation, Insets imageInsets) {
        this.imagesLocation = imagesLocation;
        this.imageInsets = imageInsets;
    }

    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, null);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        return new Insets(1, 2, 1, 2);
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    }

    public Border getBackgroundBorder() {
        return tableHeaderBackground;
    }

    /**
     * FIXME: We should find a better way to pass the column index to the border.
     */
    public void setColumnIndex(int index) {
        this.columnIndex = index;
    }

    /**
     * FIXME: We should find a better way to pass the sorted state to the border.
     */
    public void setSorted(boolean b) {
        this.isSorted = b;
    }

    /**
     * FIXME: We should find a better way to pass the column index to the border.
     */
    public void setOnActiveWindow(boolean b) {
        this.isOnActiveWindow = b;
    }

    private Border getBorder(Component c) {
        if (borders == null) {
            borders = (Border[]) QuaquaBorderFactory.create(imagesLocation, imageInsets, 4, true, true, true);
        }
        return borders[isSorted && isOnActiveWindow ? 2 : 0];
    }

    public static class UIResource extends QuaquaTableHeaderBorder implements javax.swing.plaf.UIResource {

        public UIResource(String imagesLocation, Insets imageInsets) {
            super(imagesLocation, imageInsets);
        }
    }
}
