/*
 * @(#)QuaquaSquareButtonBorder.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
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
 * QuaquaSquareButtonBorder.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaSquareButtonBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaSquareButtonBorder extends CachedPainter implements Border {
    private final static Color[] defaultColors = {
        new Color(0x828282), // border
        new Color(0xfdfdfd), // first highlight line at top
        new Color(0xefefef), // second highlight line at top
        new Color(0xdfdfdf), // third hightlight line at top
        new Color(0xf1f1f1), // shadow line at bottom
        new Color(0xe2e2e2), // gradient top
        new Color(0xfdfdfd), // gradient bottom
        new Color(0,0,0,15), // outer alpha at right and left
        new Color(0,0,0,7)   // inner alpha at right and left
    };
    private final static Color[] selectedColors = {
        new Color(0x484848), // border
        new Color(0x909090), // first highlight line at top
        new Color(0x858585), // second highlight line at top
        new Color(0x737373), // third hightlight line at top
        new Color(0x868686), // shadow line at bottom
        new Color(0x787878), // gradient top
        new Color(0x909090), // gradient bottom
        new Color(0,0,0,15), // outer alpha at right and left
        new Color(0,0,0,7)   // inner alpha at right and left
    };
    private final static Color[] disabledColors = {
        new Color(0x99828282, true), // border
        new Color(0x99fdfdfd, true), // first highlight line at top
        new Color(0x99efefef, true), // second highlight line at top
        new Color(0x99dfdfdf, true), // third hightlight line at top
        new Color(0x99f1f1f1, true), // shadow line at bottom
        new Color(0x99e2e2e2, true), // gradient top
        new Color(0x99fdfdfd, true), // gradient bottom
        new Color(0,0,0,15), // outer alpha at right and left
        new Color(0,0,0,7)   // inner alpha at right and left
    };
    private final static Color[] disabledSelectedColors = {
        new Color(0x99484848, true), // border
        new Color(0x99909090, true), // first highlight line at top
        new Color(0x99858585, true), // second highlight line at top
        new Color(0x99737373, true), // third hightlight line at top
        new Color(0x99868686, true), // shadow line at bottom
        new Color(0x99787878, true), // gradient top
        new Color(0x99909090, true), // gradient bottom
        new Color(0,0,0,15), // outer alpha at right and left
        new Color(0,0,0,7)   // inner alpha at right and left
    };
    
    /** Creates a new instance of QuaquaSquareButtonBorder */
    public QuaquaSquareButtonBorder() {
        super(8);
    }
    
    public Insets getBorderInsets(Component c) {
        return new Insets(1, 1, 1, 1);
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

        AbstractButton button = (AbstractButton) c;
        ButtonModel model = button.getModel();
        
        Color[] colors;
        if (button.isEnabled()) {
            colors = (model.isSelected() || model.isArmed() && model.isPressed()) ? selectedColors : defaultColors;
        } else {
            colors = (model.isSelected()) ? disabledSelectedColors : disabledColors;
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
        
        g.setColor(colors[0]);
        g.drawRect(0, 0, width - 1, height - 1);
        
        g.setColor(colors[1]);
        g.drawLine(1, 1, width - 2, 1); 

        g.setColor(colors[2]);
        g.drawLine(1, 2, width - 2, 2); 

        g.setColor(colors[3]);
        g.drawLine(1, 3, width - 2, 3); 

        g.setColor(colors[4]);
        g.drawLine(1, height - 2, width - 2, height - 2); 
        
        Paint oldPaint = g.getPaint();
        g.setPaint(new LinearGradientPaint(0, 4, colors[5], 0, height - 6, colors[6]));
        g.fillRect(1, 4, width - 2, height - 6);
        
        g.setColor(colors[7]);
        g.setPaint(oldPaint);
        g.drawLine(1, 1, 1, height - 2);
        g.drawLine(width - 2, 1, width - 2, height - 2);

        g.setColor(colors[8]);
        g.drawLine(2, 2, 2, height - 3);
        g.drawLine(width - 3, 3, width - 3, height - 3);
        
        g.dispose();
    }    
}
