/*
 * @(#)QuaquaNativeButtonStateBorder.java 
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.border;

import ch.randelshofer.quaqua.VisualMargin;
import javax.swing.JComponent;
import ch.randelshofer.quaqua.QuaquaUtilities;
import ch.randelshofer.quaqua.osx.OSXAquaPainter;
import ch.randelshofer.quaqua.util.CachedPainter;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import javax.swing.border.Border;
import static ch.randelshofer.quaqua.osx.OSXAquaPainter.*;

/**
 * Native Aqua border for an {@code AbstractButton}.
 * This border draws everything except the focus ring.
 * <p>
 * To draw the focus wring, wrap this border into a
 * {@link ch.randelshofer.quaqua.border.FocusedBorder}.
 * <p>
 * This border is not suited for large components because it creates a
 * temporary BufferedImage with the same size of the component.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuaquaNativeBorder extends CachedPainter implements Border, VisualMargin {

    protected OSXAquaPainter painter;
    protected Insets imageInsets;
    protected Insets borderInsets;
    protected final static int ARG_ACTIVE = 0;
    protected final static int ARG_PRESSED = 1;
    protected final static int ARG_DISABLED = 2;
    protected final static int ARG_ROLLOVER = 3;
    protected final static int ARG_SELECTED = 4;
    protected final static int ARG_FOCUSED = 5;
    protected final static int ARG_SIZE_VARIANT = 6;//2 bits
    protected final static int ARG_SEGPOS = 8;
    protected final static int ARG_WIDGET = 11;// 7 bits
    protected final static int ARG_TRAILING_SEPARATOR = 18;
    protected final static int ARG_ORIENTATION=19;

    public QuaquaNativeBorder(OSXAquaPainter.Widget widget) {
        this(12,widget, new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0));
    }
    public QuaquaNativeBorder(int cacheSize,OSXAquaPainter.Widget widget) {
        this(cacheSize,widget, new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0));
    }

    public QuaquaNativeBorder(OSXAquaPainter.Widget widget, Insets imageInsets, Insets borderInsets) {
        this(12,widget,imageInsets,borderInsets);
    }
    public QuaquaNativeBorder(int cacheSize,OSXAquaPainter.Widget widget, Insets imageInsets, Insets borderInsets) {
        super(cacheSize);
        painter = new OSXAquaPainter();
        painter.setWidget(widget);
        this.imageInsets = imageInsets;
        this.borderInsets = borderInsets;
        
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Insets vm = getVisualMargin(c);
                x += vm.left;
                y += vm.top;
                width -= vm.left + vm.right;
                height -= vm.top + vm.bottom;
                
        int args = 0;
        State state;
        if (QuaquaUtilities.isOnActiveWindow(c)) {
            state = State.active;
            args |= 1 << ARG_ACTIVE;
        } else {
            state = State.inactive;
        }
            if (!c.isEnabled()) {
                state = State.disabled;
                args |= 1 << ARG_DISABLED;
            }
        painter.setState(state);

        boolean isFocused = QuaquaUtilities.isFocused(c);
        args |= (isFocused) ? 1 << ARG_FOCUSED : 0;
        painter.setValueByKey(Key.focused, isFocused ? 1 : 0);

        Size size;

        switch (QuaquaUtilities.getSizeVariant(c)) {
            case REGULAR:
            default:
                size = Size.regular;
                break;
            case SMALL:
                size = Size.small;
                break;
            case MINI:
                size = Size.mini;
                break;

        }
        painter.setSize(size);
        args |= size.getId() << ARG_SIZE_VARIANT;

        paint(c, g, x, y, width, height, args);
    }
    
    @Override
    protected Image createImage(Component c, int w, int h,
            GraphicsConfiguration config) {

        return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);

    }

    @Override
    protected void paintToImage(Component c, Image img, int w, int h, Object args) {
        Graphics2D ig = (Graphics2D) img.getGraphics();
        ig.setColor(new Color(0x0, true));
        ig.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
        ig.fillRect(0, 0, img.getWidth(null), img.getHeight(null));
        ig.dispose();
        painter.paint((BufferedImage) img,//
                imageInsets.left, imageInsets.top,//
               w - imageInsets.left - imageInsets.right, //
                h - imageInsets.top - imageInsets.bottom);
    }

    @Override
    protected void paintToImage(Component c, Graphics g, int w, int h, Object args) {
        // round up image size to reduce memory thrashing
       BufferedImage img=(BufferedImage)createImage(c,(w/32+1)*32,(h/32+1)*32,null);
       paintToImage(c,img,w,h,args);
       g.drawImage(img, 0, 0, null);
       img.flush();
    }

    public Insets getBorderInsets(Component c) {
        return (Insets) borderInsets.clone();
    }

    public boolean isBorderOpaque() {
        return false;
    }
public Insets getVisualMargin(Component c) {
        Insets vm = null;
        if (c instanceof JComponent) {
            vm = (Insets) ((JComponent) c).getClientProperty("Quaqua.Component.visualMargin");
        }
        return vm == null ? new Insets(0, 0, 0, 0) : (Insets) vm.clone();
    }

    public static class UIResource extends QuaquaNativeBorder implements javax.swing.plaf.UIResource {

        public UIResource(OSXAquaPainter.Widget widget) {
            super(widget);
        }

        /**
         * Creates a new instance.
         * All borders must have the same dimensions.
         */
        public UIResource(OSXAquaPainter.Widget widget, Insets imageInsets, Insets borderInsets) {
            super(widget, imageInsets, borderInsets);
        }
    }
}
