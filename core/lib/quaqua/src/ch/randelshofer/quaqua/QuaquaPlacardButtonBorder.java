/*
 * @(#)QuaquaPlacardButtonBorder.java
 *
 * Copyright (c) 2006-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.ext.batik.ext.awt.LinearGradientPaint;
import ch.randelshofer.quaqua.util.CachedPainter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Transparency;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * QuaquaPlacardButtonBorder.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaPlacardButtonBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaPlacardButtonBorder extends CachedPainter implements Border {
    private final static Color[] defaultColors = {
        new Color(0xd8d8d8), // border top 1
        new Color(0x7d7d7d), // border top 2
        new Color(0x979797), // border left and right
        new Color(0x979797), // border bottom 1
        new Color(0xf5f5f5), // border bottom 2
        /*
        new Color(0xcacaca), // border gradient top
        new Color(0xb8b8b8), // border gradient bottom
         */
        new Color(0xfefefe), // inner border line top
        new Color(0xf3f3f3), // inner border line bottom
        new Color(0xfdfdfd), // shine box top
        new Color(0xf3f3f3), // shine box bottom
        new Color(0xe6e6e6), // shadow box
    };
    private final static Color[] selectedColors = {
        new Color(0xd8d8d8), // border top 1
        new Color(0x424242), // border top 2
        new Color(0x565656), // border left and right
        new Color(0x515151), // border bottom 1
        new Color(0xb9b9b9), // border bottom 2
        /*
        new Color(0x838383), // border gradient top
        new Color(0x737373), // border gradient bottom
         */
        new Color(0xa5a5a5), // inner border line top
        new Color(0x969696), // inner border line bottom
        new Color(0xa5a5a5), // shine box top
        new Color(0x9e9e9e), // shine box bottom
        new Color(0x969696), // shadow box
    };
    private final static Color[] disabledColors = {
        new Color(0xd8d8d8), // border top 1
        new Color(0x7d7d7d), // border top 2
        new Color(0x979797), // border left and right
        new Color(0x979797), // border bottom 1
        new Color(0xf5f5f5), // border bottom 2
        /*
        new Color(0xcacaca), // border gradient top
        new Color(0xb8b8b8), // border gradient bottom
         */
        new Color(0xfefefe), // inner border line top
        new Color(0xf3f3f3), // inner border line bottom
        new Color(0xfdfdfd), // shine box top
        new Color(0xf3f3f3), // shine box bottom
        new Color(0xe6e6e6), // shadow box
    };
    private final static Color[] disabledSelectedColors = {
        new Color(0xd8d8d8), // border top 1
        new Color(0x424242), // border top 2
        new Color(0x565656), // border left and right
        new Color(0x515151), // border bottom 1
        new Color(0xb9b9b9), // border bottom 2
        /*
        new Color(0x838383), // border gradient top
        new Color(0x737373), // border gradient bottom
         */
        new Color(0xa5a5a5), // inner border line top
        new Color(0x969696), // inner border line bottom
        new Color(0xa5a5a5), // shine box top
        new Color(0x9e9e9e), // shine box bottom
        new Color(0x969696), // shadow box
    };
    
    /** Creates a new instance of QuaquaSquareButtonBorder */
    public QuaquaPlacardButtonBorder() {
        super(8);
    }
    
    public Insets getBorderInsets(Component c) {
        return new Insets(2, 1, 2, 1);
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
    
    /**
     * Creates the image to cache.  This returns a translucent image.
     *
     * @param c Component painting to
     * @param w Width of image to create
     * @param h Height to image to create
     * @param config GraphicsConfiguration that will be
     *        rendered to, this may be null.
     */
    @Override
    protected Image createImage(Component c, int w, int h,
    GraphicsConfiguration config) {
        if (config == null) {
            return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
        }
        return config.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
    }
    
    public void paintBorder(Component c, Graphics gr, int x, int y, int width, int height) {
        if ( height <= 0 || width <= 0 ) {
            return;
        }
        
        Color[] colors;
        if (c instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) c;
            ButtonModel model = button.getModel();
            
            if (button.isEnabled()) {
                colors = (model.isSelected() || model.isArmed() && model.isPressed()) ? selectedColors : defaultColors;
            } else {
                colors = (model.isSelected()) ? disabledSelectedColors : disabledColors;
            }
        } else {
            colors = (c.isEnabled()) ? defaultColors : disabledColors;
        }
        paint(c, gr, x, y, width, height, colors);
    }
    
    protected void paintToImage(Component c, Graphics gr, int width, int height, Object args) {
        // Cast Graphics to Graphics2D
        // Workaround for Java 1.4 and 1.4 on Mac OS X 10.4. We create a new
        // Graphics object instead of just casting the provided one. This is
        // because drawing texture paints appears to confuse the Graphics object.
        Graphics2D g = (Graphics2D) gr.create();
        
        Color[] colors = (Color[]) args;
        
        Paint oldPaint = g.getPaint();
        
        // Note: We draw the gradient paints first, because Apple's Java 
        // 1.4.2_05 draws them 1 Pixel too wide on the left
        // draw inner border lines
        g.setPaint(new LinearGradientPaint(0, 2, colors[5], 0, height - 3, colors[6]));
        g.drawLine(1, 2, 1, height - 3);
        g.drawLine(width - 2, 2, width - 2, height - 3);
        
        // draw shine box
        int sheight = (int) (height * 0.45);
        g.setPaint(new LinearGradientPaint(0, 2, colors[7], 0, sheight, colors[8]));
        g.fillRect(2, 2, width - 4, sheight - 1);
        
        // draw border
        g.setColor(colors[0]);
        g.drawLine(0, 0, width - 1, 0);
        g.setColor(colors[1]);
        g.drawLine(0, 1, width - 1, 1);
        g.setColor(colors[2]);
        g.drawLine(0, 2, 0, height - 3);
        g.drawLine(width - 1, 2, width - 1, height - 3);
        g.setColor(colors[3]);
        g.drawLine(0, height - 2, width - 1, height - 2);
        g.setColor(colors[4]);
        g.drawLine(0, height - 1, width - 1, height - 1);
        
        // draw shadow box
        g.setColor(colors[9]);
        g.fillRect(2, sheight + 1, width - 4, height - sheight - 3);
        
        g.setPaint(oldPaint);
        g.dispose();
    }
}
