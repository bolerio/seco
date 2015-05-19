/*
 * @(#)QuaquaNativeScrollPaneBorder.java  
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.border.AbstractFocusedPainter;
import ch.randelshofer.quaqua.border.ImageBevelBorder;
import ch.randelshofer.quaqua.border.VisualMarginBorder;
import ch.randelshofer.quaqua.osx.OSXAquaPainter;
import ch.randelshofer.quaqua.osx.OSXAquaPainter.Size;
import ch.randelshofer.quaqua.osx.OSXAquaPainter.State;
import ch.randelshofer.quaqua.osx.OSXAquaPainter.Widget;
import ch.randelshofer.quaqua.util.Debug;
import ch.randelshofer.quaqua.util.InsetsUtil;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;

/**
 * {@code QuaquaNativeScrollPaneBorder}.
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaNativeScrollPaneBorder.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class QuaquaNativeScrollPaneBorder extends VisualMarginBorder implements UIResource {

    private final static int ARG_SMALL_SIZE = 32;
    private BufferedImage regularPainterImage;
    private BufferedImage smallPainterImage;
    private BufferedImage regularFocusImage;
    private BufferedImage smallFocusImage;
    private ImageBevelBorder regularIbb;
    private ImageBevelBorder smallIbb;
    private OSXAquaPainter painter;
    private Insets imageInsets;
    private Insets borderInsets;

    public QuaquaNativeScrollPaneBorder() {
        painter = new OSXAquaPainter();
        imageInsets = new Insets(0, 0, 0, 0);
        borderInsets = new Insets(1,1,1,1);
    }

    @Override
    public void paintBorder(Component cc, Graphics g, int x, int y, int width, int height) {
        int args = 0;

        // Determine which component is relevant for the state of the border
        Component c = cc;
        if (c instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) c;
            c = sp.getViewport().getView();
            if (c==null)c=sp;
        }
        JTextComponent b = (c instanceof JTextComponent) ? (JTextComponent) c : null;
        boolean editable = b == null || b.isEditable();

        // PREPARE THE PAINTER
        // -------------------
        {

            State state;
            if (QuaquaUtilities.isOnActiveWindow(c)) {
                state = State.active;
                args |= 1;
            } else {
                state = State.inactive;
            }


            Widget widget;
            widget = Widget.frameTextField;
            //widget = Widget.frameListBox;



            painter.setWidget(widget);

            if (!cc.isEnabled() || !editable) {
                state = State.disabled;
                args |= 4;
            }
            painter.setState(state);

            boolean isFocusedAndEditable = QuaquaUtilities.isFocused(c) && editable;
            args |= (isFocusedAndEditable) ? 16 : 0;
            painter.setValueByKey(OSXAquaPainter.Key.focused, isFocusedAndEditable ? 1 : 0);

            Size size;
            switch (QuaquaUtilities.getSizeVariant(c)) {
                case REGULAR:
                default:
                    size = Size.regular;
                    break;
                case SMALL:
                    size = Size.small;
                    args |= ARG_SMALL_SIZE;
                    break;
                case MINI:
                    size = Size.small; // paint mini with small artwork
                    args |= ARG_SMALL_SIZE;
                    break;

            }
            painter.setSize(size);
        }
        // Create an ImageBevelBorder
        // FIXME - We have a caching opportunity here!
        // -------------------------------------------
        {
            Insets vm = getVisualMargin(cc);

            // The painter can not render text fields in arbitrary sizes.
            // We render it first into an ImageBevelBorder, and then onto the
            // image.
            BufferedImage painterImg;
            BufferedImage ibbImg;
            BufferedImage focusImg;
            ImageBevelBorder ibb;
            int fixedWidth, fixedHeight, fixedYOffset;
            int slack = 6;
            if ((args & ARG_SMALL_SIZE) == ARG_SMALL_SIZE) {
                fixedWidth = 40 + slack * 2;
                fixedHeight = 19 + slack * 2;
                fixedYOffset = 3;

                if (smallPainterImage == null) {
                    smallPainterImage = new BufferedImage(fixedWidth, fixedHeight, BufferedImage.TYPE_INT_ARGB_PRE);
                }
                painterImg = smallPainterImage;
                if (smallFocusImage == null) {
                    smallFocusImage = new BufferedImage(fixedWidth, fixedHeight, BufferedImage.TYPE_INT_ARGB_PRE);
                }
                focusImg = smallFocusImage;
                if (smallIbb == null) {
                    ibbImg = new BufferedImage(fixedWidth, fixedHeight, BufferedImage.TYPE_INT_ARGB_PRE);
                    ibb = smallIbb = new ImageBevelBorder(ibbImg, new Insets(4 + slack, 4 + slack, 4 + slack, 4 + slack), new Insets(4 + slack, 4 + slack, 4 + slack, 4 + slack));
                } else {
                    ibb = smallIbb;
                }
            } else {
                fixedWidth = 40 + slack * 2;
                fixedHeight = 22 + slack * 2;
                fixedYOffset = 3;

                if (regularPainterImage == null) {
                    regularPainterImage = new BufferedImage(fixedWidth, fixedHeight, BufferedImage.TYPE_INT_ARGB_PRE);
                }
                painterImg = regularPainterImage;
                if (regularFocusImage == null) {
                    regularFocusImage = new BufferedImage(fixedWidth, fixedHeight, BufferedImage.TYPE_INT_ARGB_PRE);
                }
                focusImg = regularFocusImage;
                if (regularIbb == null) {
                    ibbImg = new BufferedImage(fixedWidth, fixedHeight, BufferedImage.TYPE_INT_ARGB_PRE);
                    ibb = regularIbb = new ImageBevelBorder(ibbImg, new Insets(8 + slack, 8 + slack, 8 + slack, 8 + slack), new Insets(8 + slack, 8 + slack, 8 + slack, 8 + slack));
                } else {
                    ibb = regularIbb;
                }
            }
            ibbImg = (BufferedImage) ibb.getImage();

            Graphics2D pg = painterImg.createGraphics();
            pg.setColor(new Color(0x0, true));
            pg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
            pg.fillRect(0, 0, painterImg.getWidth(), painterImg.getHeight());
            pg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            pg.dispose();
            painter.paint(painterImg,//
                    slack, fixedYOffset + slack,//
                    painterImg.getWidth() - 2 * slack, painterImg.getHeight() - 2 * slack);


            Graphics2D ibbg = ibbImg.createGraphics();
            ibbg.setColor(new Color(0x0, true));
            ibbg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
            ibbg.fillRect(0, 0, painterImg.getWidth(), painterImg.getHeight());
            ibbg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            ibbg.drawImage(painterImg, 0, 0, null);

            if (QuaquaUtilities.isFocused(c) && editable) {
                AbstractFocusedPainter.paintFocusRing(painterImg, focusImg, ibbg, 0, 0);
            }

            ibbg.dispose();

            ibb.paintBorder(c, g,//
                    x+imageInsets.left - slack + vm.left, //
                    y+imageInsets.top - slack + vm.top,//
                    width - imageInsets.left - imageInsets.right  + 2 * slack - vm.left - vm.right, //
                    height - imageInsets.top - imageInsets.bottom  + 2 * slack - vm.top - vm.bottom);
            ibbg.dispose();

        }
        if (c instanceof JComponent)
        Debug.paint(g, (JComponent)c, null);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets ins = super.getBorderInsets(c, insets);
        InsetsUtil.addTo(borderInsets, ins);
        return ins;
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}