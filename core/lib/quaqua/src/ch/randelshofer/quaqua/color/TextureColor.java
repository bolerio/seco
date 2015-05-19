/*
 * @(#)TextureColor.java 
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.color;

import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.*;
import java.awt.*;
import java.awt.image.*;
/**
 * This class used to pass TexturePaint's 'through' the Swing API, so that users
 * of our Look and Feel can work with TexturePaint's like with regular colors,
 * but Quaqua UI components will paint using the texture instead of with the
 * color.
 *
 * @author  Werner Randelshofer
 * @version $Id: TextureColor.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class TextureColor extends PaintableColor {
    protected Image texture;
    
    /** Creates a new instance. */
    public TextureColor(int rgb) {
        super(rgb);
    }
    public TextureColor(int r, int g, int b) {
        super(r, g, b);
    }
    public TextureColor(int r, int g, int b, int a) {
        super(r, g, b, a);
    }
    public TextureColor(int r, int g, int b, Image texture) {
        super(r, g, b);
        this.texture = texture;
    }
    public TextureColor(int r, int g, int b, int a, Image texture) {
        super(r, g, b, a);
        this.texture = texture;
    }
    public TextureColor(int rgb, String location) {
        super(rgb);
        this.texture = QuaquaIconFactory.createImage(location);
    }

    public BufferedImage getTexture() {
        texture = Images.toBufferedImage(texture);
        return (BufferedImage) texture;
    }
    
    public Paint getPaint(Component c, int x, int y, int width, int height) {
        BufferedImage txtr = getTexture();
        if (txtr != null) {
            Point p = getRootPaneOffset(c);
            return new TexturePaint(txtr, new Rectangle(
            p.x+x,p.y+y,txtr.getWidth(),txtr.getHeight()
            ));
        } else {
            return this;
        }
    }
    
    public static class UIResource extends TextureColor implements javax.swing.plaf.UIResource {
        public UIResource(int rgb) {
            super(rgb);
        }
        public UIResource(int r, int g, int b) {
            super(r, g, b);
        }
        public UIResource(int r, int g, int b, int a) {
            super(r, g, b, a);
        }
        public UIResource(int r, int g, int b, BufferedImage texture) {
            super(r, g, b, texture);
        }
        public UIResource(int r, int g, int b, int a, BufferedImage texture) {
            super(r, g, b, a, texture);
        }
        public UIResource(int rgb, String location) {
            super(rgb, location);
        }
    }
}
