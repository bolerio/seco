/*
 * @(#)ToolbarButtonStateBorder.java  1.1  2005-11-30
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.border.ButtonStateBorder;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * ToolbarButtonStateBorder.
 *
 * @author  Werner Randelshofer
 * @version 1.1 2005-11-30 Constructor with tiled image added.
 * <br>1.0  29 March 2005  Created.
 */
public class QuaquaToolBarButtonStateBorder extends ButtonStateBorder {
    private boolean isRollover;
    
    public QuaquaToolBarButtonStateBorder(Border e, Border ep, Border es, Border eps, Border d, Border ds, Border i, Border is, Border di, Border dis, boolean isRollover) {
        super(e,ep,es,eps,d,ds,i,is,di,dis);
    }
    /**
     * Creates a new instance.
     * All borders must have the same insets.
     */
    public QuaquaToolBarButtonStateBorder(Border[] borders, boolean isRollover) {
        super(borders);
        this.isRollover = isRollover;
    }
    /**
     * Creates a new instance.
     * All icons must have the same dimensions.
     */
    public QuaquaToolBarButtonStateBorder(Image[] images, Insets borderInsets, Insets imageInsets, boolean fill, boolean isRollover) {
        super(images, borderInsets, imageInsets, fill);
        this.isRollover = isRollover;
    }
    
    /**
     * Creates a new instance.
     * All borders must have the same dimensions.
     */
    public QuaquaToolBarButtonStateBorder(Image tiledImage, int tileCount, boolean isTiledHorizontaly,
    Insets imageInsets, Insets borderInsets, boolean fill, boolean isRollover) {
        super(tiledImage, tileCount, isTiledHorizontaly, imageInsets, borderInsets, fill);
        this.isRollover = isRollover;
    }
    
    public void paintBorder(Component c, Graphics gr, int x, int y, int width, int height) {
        boolean paint = false;
        if (c instanceof AbstractButton) {
            ButtonModel model = ((AbstractButton) c).getModel();
            
            if (isRollover) {
                paint = model.isRollover() || model.isSelected();
            } else {
                paint = model.isSelected();
            }
        }
        if (paint) {
            super.paintBorder(c, gr, x, y, width, height);
        }
    }
}
