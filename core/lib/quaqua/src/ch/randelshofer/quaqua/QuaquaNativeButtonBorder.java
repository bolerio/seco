/*
 * @(#)QuaquaNativeButtonBorder.java 
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua;

import java.util.Iterator;
import java.util.HashSet;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import javax.swing.JButton;
import ch.randelshofer.quaqua.border.PressedCueBorder;
import javax.swing.JToolBar;
import java.util.HashMap;
import ch.randelshofer.quaqua.border.CompositeVisualMarginBorder;
import ch.randelshofer.quaqua.border.VisualMarginBorder;
import ch.randelshofer.quaqua.util.Images;
import ch.randelshofer.quaqua.border.ButtonStateBorder;
import ch.randelshofer.quaqua.util.InsetsUtil;
import ch.randelshofer.quaqua.border.BackgroundBorder;
import ch.randelshofer.quaqua.border.FocusedBorder;
import javax.swing.JComponent;
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
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.border.Border;
import static ch.randelshofer.quaqua.osx.OSXAquaPainter.*;

/**
 * Native Aqua border for an {@code AbstractButton}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuaquaNativeButtonBorder extends VisualMarginBorder implements Border, PressedCueBorder, BackgroundBorder {

    /** Each border type needs to be tweaked in a different way.
     * We collect all the tweaks here.
     */
    private static class WidgetConfig {

        /** The type name used in JButton.buttonType and Quaqua.Button.style. */
        String[] type;
        /** The native widget id. */
        Widget widget;
        /** Whether the button needs the trailing separator hack. */
        boolean needsTrailingSeparatorHack;
        Insets[] margin = new Insets[3];
        /** Border insets. 
         * regular,small,mini
         */
        Insets[] borderInsets = new Insets[3];
        /** Image insets are used when rendering the native widget onto an image,
         * so that native widget's visual bounds are rendered with a fixed
         * inset of 3,3,3,3 into the image. The fixed inset allows for cast
         * shadows in the image.
         * 
         * only regular,small,mini,
         * first regular,small,mini,
         * middle regular,small,mini,
         * last regular small,mini
         */
        Insets[][] imageInsets = new Insets[4][3];
    }
    private final static HashMap<String, WidgetConfig> wcs;

    static {
        Object[][] a = {//
            // name, hasVisualMargin,widget,border insets, margin insets, image insets
            {"push", false, Widget.buttonPush, //
                new Insets[]{new Insets(1, 3, 2, 3), new Insets(1, 3, 1, 3), new Insets(1, 2, 1, 2)},//
                new Insets[]{new Insets(1, 14, 1, 14), new Insets(1, 5, 1, 5), new Insets(1, 4, 1, 4)},//
                new Insets[]{new Insets(2, -3, 0, -3), new Insets(2, -2, 0, -2), new Insets(1, 2, 0, 2)}},
            {"square", false, Widget.buttonBevel, //
                new Insets[]{new Insets(1, 1, 1, 1), new Insets(1, 1, 1, 1), new Insets(1, 1, 1, 1)},//
                new Insets[]{new Insets(2, 8, 2, 8), new Insets(1, 5, 1, 5), new Insets(1, 4, 1, 4)},//
                new Insets[]{
                    new Insets(3, 3, 3, 3), new Insets(3, 3, 3, 3), new Insets(3, 3, 3, 3),//only
                    new Insets(3, 3, 3, 2), new Insets(3, 3, 3, 2), new Insets(3, 3, 3, 2),// first
                    new Insets(3, 3, 3, 2), new Insets(3, 3, 3, 2), new Insets(3, 3, 3, 2),// middle
                    new Insets(3, 3, 3, 3), new Insets(3, 3, 3, 3), new Insets(3, 3, 3, 3)}},// last
            {"bevel", false, Widget.buttonBevelRound, //
                new Insets[]{new Insets(1, 1, 2, 1), new Insets(1, 1, 2, 1), new Insets(1, 1, 2, 1)},//
                new Insets[]{new Insets(3, 13, 4, 13), new Insets(3, 9, 1, 9), new Insets(3, 7, 3, 7)},//
                new Insets[]{new Insets(1, 1, 1, 1), new Insets(1, 1, 1, 1), new Insets(1, 1, 1, 1)}},
            {"gradient", "placard", false, Widget.buttonBevelInset, //
                new Insets[]{new Insets(1, 1, 1, 1), new Insets(1, 1, 1, 1), new Insets(1, 1, 1, 1)},//
                new Insets[]{new Insets(2, 8, 2, 8), new Insets(1, 5, 1, 5), new Insets(1, 4, 1, 4)},//
                new Insets[]{
                    new Insets(2, 3, 2, 3), new Insets(2, 3, 2, 3), new Insets(2, 3, 2, 3),// only
                    new Insets(2, 3, 2, 2), new Insets(2, 3, 2, 2), new Insets(2, 3, 2, 2),// first
                    new Insets(2, 3, 2, 2), new Insets(2, 3, 2, 2), new Insets(2, 3, 2, 2),// middle
                    new Insets(2, 3, 2, 3), new Insets(2, 3, 2, 3), new Insets(2, 3, 2, 3)}},// last
            {"tableHeader", false, Widget.buttonListHeader, //
                new Insets[]{new Insets(2, 4, 2, 4), new Insets(2, 4, 2, 4), new Insets(2, 4, 2, 4)},//
                new Insets[]{new Insets(1, 4, 1, 4), new Insets(1, 2, 1, 2), new Insets(1, 2, 1, 2)},//
                new Insets[]{new Insets(0, 2, 0, 3), new Insets(0, 2, 0, 3), new Insets(0, 2, 0, 3)}},
            {"textured", false, Widget.buttonPushTextured, //
                new Insets[]{new Insets(1, 3, 2, 3), new Insets(1, 3, 1, 3), new Insets(1, 2, 1, 2)},//
                new Insets[]{new Insets(1, 14, 1, 14), new Insets(1, 5, 1, 5), new Insets(1, 4, 1, 4)},//
                new Insets[]{new Insets(3, 3, 2, 3), new Insets(3, 3, 2, 3), new Insets(1, 3, 0, 3)}},
            {"roundRect", false, Widget.buttonPushInset, //
                new Insets[]{new Insets(1, 5, 1, 5), new Insets(1, 3, 1, 3), new Insets(1, 3, 1, 3)},//
                new Insets[]{new Insets(0, 6, 0, 6), new Insets(0, 5, 0, 5), new Insets(0, 4, 0, 4)},//
                new Insets[]{new Insets(0, 2, 0, 2), new Insets(0, 2, 0, 2), new Insets(1, 2, 0, 2)}},
            {"recessed", false, Widget.buttonPushScope, //
                new Insets[]{new Insets(1, 7, 1, 7), new Insets(1, 5, 1, 5), new Insets(1, 5, 1, 5)},//
                new Insets[]{new Insets(0, 6, 0, 6), new Insets(0, 5, 0, 5), new Insets(0, 4, 0, 4)},//
                new Insets[]{new Insets(0, 2, 0, 2), new Insets(0, 2, 0, 2), new Insets(1, 2, 0, 2)}},
            {"colorWell", false, null, //
                new Insets[]{new Insets(7, 6, 7, 6), new Insets(7, 6, 7, 6), new Insets(7, 6, 7, 6)},//
                new Insets[]{new Insets(1, 10, 1, 10), new Insets(1, 8, 1, 8), new Insets(1, 8, 1, 8)},//
                null},
            {"help", false, Widget.buttonRoundHelp, //
                new Insets[]{new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)},//
                new Insets[]{new Insets(1, 4, 1, 4), new Insets(1, 2, 1, 2), new Insets(1, 2, 1, 2)},//
                new Insets[]{new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)}},
            {"toolBar", false, null, //
                new Insets[]{new Insets(3, 3, 3, 3), new Insets(3, 3, 3, 3), new Insets(3, 3, 3, 3)},//
                new Insets[]{new Insets(1, 4, 1, 4), new Insets(1, 2, 1, 2), new Insets(1, 2, 1, 2)},//
                new Insets[]{new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)}},
            {"segmented", "toggle", "toggleEast", "toggleCenter", "toggleWest", true, Widget.buttonSegmented,//
                new Insets[]{new Insets(2, 13, 3, 13), new Insets(2, 8, 2, 8), new Insets(2, 7, 2, 7)},//
                new Insets[]{new Insets(1, 4, 1, 4), new Insets(1, 2, 1, 2), new Insets(1, 2, 1, 2)},//
                new Insets[]{new Insets(0, 1, 0, 1), new Insets(1, 1, 0, 1), new Insets(0, 2, 0, 2),//only regular,small,mini
                    new Insets(0, 1, 0, 0), new Insets(1, 1, 0, 0), new Insets(0, 2, 0, 0),//first 
                    new Insets(0, 0, 0, 0), new Insets(1, 0, 0, 1), new Insets(0, 0, 0, 0),//middle
                    new Insets(0, 0, 0, 1), new Insets(1, 1, 0, 1), new Insets(0, 0, 0, 2)}},//last
            {"segmentedRoundRect", "segmentedCapsule", "segmentedTextured", true, Widget.buttonSegmentedInset,//
                new Insets[]{new Insets(2, 13, 3, 13), new Insets(2, 8, 2, 8), new Insets(2, 7, 2, 7)},//
                new Insets[]{new Insets(1, 4, 1, 4), new Insets(1, 2, 1, 2), new Insets(1, 2, 1, 2)},//
                new Insets[]{new Insets(0, 1, 0, 1), new Insets(0, 1, 0, 1), new Insets(0, 2, 0, 2),//only regular,small,mini
                    new Insets(0, 1, 0, 0), new Insets(1, 1, 0, 0), new Insets(0, 2, 0, 0),//first 
                    new Insets(0, 0, 0, 0), new Insets(1, 0, 0, 1), new Insets(0, 0, 0, 0),//middle
                    new Insets(0, 0, 0, 1), new Insets(1, 1, 0, 1), new Insets(0, 0, 0, 2)}},//last
        };
        wcs = new HashMap<String, WidgetConfig>();
        for (int i = 0; i < a.length; i++) {
            int j = 0;
            int numNames = 0;
            WidgetConfig wp = new WidgetConfig();
            while (j < a[i].length) {
                while (a[i][j++] instanceof String) {
                }
                numNames = --j;
                //wp.hasVisualMargin = (Boolean) a[i][j++];
                wp.needsTrailingSeparatorHack = (Boolean) a[i][j++];
                wp.widget = (Widget) a[i][j++];
                Insets[] bi = (Insets[]) a[i][j++];
                wp.borderInsets[0] = bi[0];
                wp.borderInsets[1] = bi[1];
                wp.borderInsets[2] = bi[2];
                Insets[] mi = (Insets[]) a[i][j++];
                wp.margin[0] = mi[0];
                wp.margin[1] = mi[1];
                wp.margin[2] = mi[2];
                Insets[] ii = (Insets[]) a[i][j++];
                if (ii != null) {
                    int k = 0;
                    wp.imageInsets[0][0] = ii[0];
                    wp.imageInsets[0][1] = ii[1];
                    wp.imageInsets[0][2] = ii[2];
                    wp.imageInsets[1][0] = (ii.length == 3) ? ii[0] : ii[3];
                    wp.imageInsets[1][1] = (ii.length == 3) ? ii[1] : ii[4];
                    wp.imageInsets[1][2] = (ii.length == 3) ? ii[2] : ii[5];
                    wp.imageInsets[2][0] = (ii.length == 3) ? ii[0] : ii[6];
                    wp.imageInsets[2][1] = (ii.length == 3) ? ii[1] : ii[7];
                    wp.imageInsets[2][2] = (ii.length == 3) ? ii[2] : ii[8];
                    wp.imageInsets[3][0] = (ii.length == 3) ? ii[0] : ii[9];
                    wp.imageInsets[3][1] = (ii.length == 3) ? ii[1] : ii[10];
                    wp.imageInsets[3][2] = (ii.length == 3) ? ii[2] : ii[11];
                }
            }
            for (j = 0; j < numNames; j++) {
                wcs.put((String) a[i][j], wp);
            }
        }
    }
    private OSXAquaPainter painter;
    private Insets imageInsets;
    private Border backgroundBorder;

    /** The background border.
     * This border delegates the actual painting to different borders, because
     * {@link OSXAquaPainter} can not render all borders that we need.
     */
    private class BGBorder implements Border, PressedCueBorder, VisualMargin {

        private PressedCueBorder nativeBorder;
        private PressedCueBorder bevelBorder;
        private PressedCueBorder placardBorder;
        private PressedCueBorder toolBarBorder;
        private PressedCueBorder toolBarTabBorder;
        private PressedCueBorder colorWellBorder;

        private PressedCueBorder getActualBorder(Component c) {
            String s = getStyle(c);

            WidgetConfig wp = wcs.get(s);
            //return w==null?bevelBorder:nativeBorder;

            PressedCueBorder b;
            if (wp == null || wp.widget == null) {
                if ("colorWell".equals(s)) {
                    b = getColorWellBorder();
                } else if ("toolBar".equals(s)) {
                    b = getToolBarBorder();
                } else if ("toolBarTab".equals(s)) {
                    b = getToolBarTabBorder();
                } else {
                    b = getBevelBorder();
                }
            } else {
                b = getNativeBorder();
            }

            return b;
        }

        private PressedCueBorder getBevelBorder() {
            if (bevelBorder == null) {
                bevelBorder = new FocusedBorder(
                        new CompositeVisualMarginBorder(
                        new ButtonStateBorder(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/RoundedBevel.borders.png")), 10, true,
                        new Insets(10, 9, 10, 8), new Insets(0, 0, 0, 0), true),
                        3, 2, 2, 2));
            }
            return bevelBorder;
        }

        private PressedCueBorder getNativeBorder() {
            if (nativeBorder == null) {
                this.nativeBorder =
                        new FocusedBorder(new NativeBGBorder());
            }
            return nativeBorder;
        }

        private PressedCueBorder getPlacardBorder() {
            if (placardBorder == null) {
                // The placarBorder does not have a dynamic visual margin.
                placardBorder = new FocusedBorder(
                        new CompositeVisualMarginBorder(
                        new QuaquaPlacardButtonBorder(),
                        1, 0, 1, 0));

            }
            return placardBorder;
        }

        private PressedCueBorder getToolBarBorder() {
            if (toolBarBorder == null) {
                // The placardBorder does not have a dynamic visual margin.
                toolBarBorder =
                        new VisualMarginBorder(0, 4, 0, 4) {

                            @Override
                            public boolean hasPressedCue(JComponent c) {
                                return false;
                            }
                        };

            }
            return toolBarBorder;
        }

        private PressedCueBorder getColorWellBorder() {
            if (colorWellBorder == null) {
                colorWellBorder = new FocusedBorder(
                        new CompositeVisualMarginBorder(
                        new QuaquaColorWellBorder(),
                        0, 0, 0, 0));

            }
            return colorWellBorder;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            getActualBorder(c).paintBorder(c, g, x, y, width, height);
        }

        public Insets getBorderInsets(Component c) {
            return getActualBorder(c).getBorderInsets(c);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public Insets getVisualMargin(Component c) {
            return ((VisualMargin) getActualBorder(c)).getVisualMargin(c);
        }

        public boolean hasPressedCue(JComponent c) {
            return getActualBorder(c).hasPressedCue(c);
        }

        private PressedCueBorder getToolBarTabBorder() {
            if (toolBarTabBorder==null)
            toolBarTabBorder = new QuaquaToolBarTabButtonBorder();
            return toolBarTabBorder;
        }
    }

    /** This is the actual native button border. */
    private class NativeBGBorder extends CachedPainter implements PressedCueBorder, VisualMargin {

        private final static int ARG_ACTIVE = 0;
        private final static int ARG_PRESSED = 1;
        private final static int ARG_DISABLED = 2;
        private final static int ARG_ROLLOVER = 3;
        private final static int ARG_SELECTED = 4;
        private final static int ARG_FOCUSED = 5;
        private final static int ARG_PULSED = 6;
        private final static int ARG_SIZE_VARIANT = 7;//2 bits
        private final static int ARG_SEGPOS = 9;
        private final static int ARG_WIDGET = 12;// 7 bits
        private final static int ARG_TRAILING_SEPARATOR = 19;
        private final static int ARG_TRAILING_SEPARATOR_HACK = 20;
        private final static int ARG_ANIM_FRAME = 21;

        public NativeBGBorder() {
            super(12);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            AbstractButton b = null;
            JComponent jc = null;
            ButtonModel bm = null;
            if (c instanceof AbstractButton) {
                b = (AbstractButton) c;
                jc = b;
                bm = b.getModel();
            } else if (c instanceof JComponent) {
                jc = (JComponent) c;
            }
            String s = getStyle(c);
            WidgetConfig wc = wcs.get(s);
            Widget widget = wc.widget;

            int args = 0;
            OSXAquaPainter.State state;
            if (QuaquaUtilities.isOnActiveWindow(c)) {
                state = OSXAquaPainter.State.active;
                args |= 1 << ARG_ACTIVE;
                if (b instanceof JButton && ((JButton) b).isDefaultButton()) {
                    state = OSXAquaPainter.State.pulsed;
                    args |= 1 << ARG_PULSED;
                    long animationTime = System.currentTimeMillis();
                    args |= animationTime << ARG_ANIM_FRAME;
                    painter.setValueByKey(Key.animationTime, animationTime / 1000d);
                }
            } else {
                state = OSXAquaPainter.State.inactive;
            }
            if (bm != null) {
                if (bm.isRollover()) {
                    state = OSXAquaPainter.State.rollover;
                    args |= 1 << ARG_ROLLOVER;
                }
                if (bm.isArmed() && bm.isPressed()) {
                    state = OSXAquaPainter.State.pressed;
                    args |= 1 << ARG_PRESSED;
                }
                if (!bm.isEnabled()) {
                    state = OSXAquaPainter.State.disabled;
                    args |= 1 << ARG_DISABLED;
                }

            }
            painter.setState(state);

            int value = b == null ? 0 : (b.isSelected() ? 1 : 0);

            painter.setValueByKey(Key.value, value);
            args |= value << ARG_SELECTED;

            boolean isFocused = QuaquaUtilities.isFocused(c);
            painter.setValueByKey(Key.focused, isFocused ? 1 : 0);
            args |= (isFocused) ? 1 << ARG_FOCUSED : 0;





            Size size;
            switch (QuaquaUtilities.getSizeVariant(c)) {
                case REGULAR:
                default:
                    size = Size.regular;
                    args |= 0 << ARG_SIZE_VARIANT;
                    break;
                case SMALL:
                    size = Size.small;
                    args |= 1 << ARG_SIZE_VARIANT;
                    break;
                case LARGE:
                    size = Size.large;
                    args |= 2 << ARG_SIZE_VARIANT;
                    break;
                case MINI:
                    size = Size.mini;
                    args |= 3 << ARG_SIZE_VARIANT;
                    break;

            }
            painter.setSize(size);

            SegmentPosition segpos = getSegmentPosition(c);
            painter.setSegmentPosition(segpos);
            args |= segpos.getId() << ARG_SEGPOS;
            switch (segpos) {
                case first:
                case middle:
                    painter.setValueByKey(Key.segmentTrailingSeparator, 1);
                    args |= 1 << ARG_TRAILING_SEPARATOR;
                    if (wc.needsTrailingSeparatorHack) {
                        args |= 1 << ARG_TRAILING_SEPARATOR_HACK;
                    }
                    break;
                default:
                    painter.setValueByKey(Key.segmentTrailingSeparator, 0);
            }



            args |= widget.getId() << ARG_WIDGET;
            painter.setWidget(widget);

            int sizeIndex;
            switch (size) {
                case regular:
                default:
                    sizeIndex = 0;
                    break;
                case small:
                    sizeIndex = 1;
                    break;
                case mini:
                    sizeIndex = 2;
                    break;
            }
            int segIndex;
            switch (segpos) {
                case first:
                    segIndex = 1;
                    break;
                case middle:
                    segIndex = 2;
                    break;
                case last:
                    segIndex = 3;
                    break;
                case only:
                default:
                    segIndex = 0;
                    break;
            }
            imageInsets = wc.imageInsets[segIndex][sizeIndex];
            paint(c, g, x, y, width, height, args);
        }

        @Override
        protected Image createImage(Component c, int w, int h,
                GraphicsConfiguration config) {

            return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);

        }

        @Override
        protected void paintToImage(Component c, Image img, int w, int h, Object argso) {
            Graphics2D ig = (Graphics2D) img.getGraphics();
            ig.setColor(new Color(0x0, true));
            ig.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
            ig.fillRect(0, 0, img.getWidth(null), img.getHeight(null));
            ig.dispose();

            Insets vm = getVisualMargin(c);


            painter.paint((BufferedImage) img,//
                    imageInsets.left - 3 + vm.left, //
                    imageInsets.top - 3 + vm.top,//
                    w - imageInsets.left - imageInsets.right + 6 - vm.left - vm.right, //
                    h - imageInsets.top - imageInsets.bottom + 6 - vm.top - vm.bottom);

            // Workaround for trailing separators: for some reason they are not
            // drawn, so we draw them by ourselves.
            // FIXME - The color and offsets of the saparator should not be
            // hardcoded here.
            int args = (Integer) argso;
            if ((args & (1 << ARG_TRAILING_SEPARATOR_HACK)) != 0) {
                Graphics2D g = ((BufferedImage) img).createGraphics();
                g.setColor(new Color(0xeabbbbbb, true));
                g.drawLine(w - 1, 6, w - 1, h - 8);
                g.dispose();
            }
        }

        @Override
        protected void paintToImage(Component c, Graphics g, int w, int h, Object args) {
            // round up image size to reduce memory thrashing
            BufferedImage img = (BufferedImage) createImage(c, (w / 32 + 1) * 32, (h / 32 + 1) * 32, null);
            paintToImage(c, img, w, h, args);
            g.drawImage(img, 0, 0, null);
            img.flush();
        }

        /** Returns fake insets since this is just a background border. 
         * The real insets are returned by {@link QuaquaNativeButtonBorder#getBorderInsets}.
         */
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public Insets getVisualMargin(Component c) {
            Insets i = QuaquaNativeButtonBorder.this.getVisualMargin(c);
            return i;
        }

        public boolean hasPressedCue(JComponent c) {
            return true;
        }
    }

    public QuaquaNativeButtonBorder() {
        super(new Insets(0, 0, 0, 0));
        painter = new OSXAquaPainter();
        this.imageInsets = new Insets(0, 0, 0, 0);
    }

    public Border getBackgroundBorder() {
        if (backgroundBorder == null) {
            this.backgroundBorder = new BGBorder();
        }
        return backgroundBorder;
    }

    @Override
    public boolean hasPressedCue(JComponent c) {
        Border b = getBackgroundBorder();
        if (b instanceof PressedCueBorder) {
            return ((PressedCueBorder) b).hasPressedCue(c);
        }
        return true;
    }

    private String getStyle(Component c) {
        String s = null;
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            s = (String) jc.getClientProperty("Quaqua.Button.style");
            if (s == null) {
                s = (String) jc.getClientProperty("JButton.buttonType");
            }
        }

        if (s == null) {
            if (c.getParent() instanceof JToolBar) {
                String tbs = (String) ((JToolBar) c.getParent()).getClientProperty("Quaqua.ToolBar.style");
                if ("gradient".equals(tbs) || "placard".equals(tbs)) {
                    s = "gradient";
                } else {
                    s = "toolBar";
                }
            }
        }
        if (s == null || "push".equals(s)) {
            // must call super here, because visualmargin is based on style
            Insets vm = super.getVisualMargin(c, new Insets(0, 0, 0, 0));

            // push buttons can only have an inner size of 26 pixels or less
            if (c.getHeight() - vm.top - vm.bottom > 26
                    || QuaquaUtilities.getSizeVariant(c) == QuaquaUtilities.SizeVariant.LARGE) {
                s = "bevel";
            }
        }

        // coerce synonyms
        if ("placard".equals(s) || "segmentedGradient".equals("")) {
            s = "gradient";
        }


        return (s == null) ? "push" : s;
    }
    /*
    private Widget getWidget(Component c) {
    String s = getStyle(c);
    WidgetProperty wp = wps.get(s);
    return (wp == null) ? null : wp.widget;
    }*/

    private SegmentPosition getSegmentPosition(Component c) {
        String s = null;
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            s = (String) jc.getClientProperty("Quaqua.Button.style");
            if (s != null) {
                if ("toggleWest".equals(s)) {
                    return SegmentPosition.first;
                } else if ("toggleCenter".equals(s)) {
                    return SegmentPosition.middle;
                } else if ("toggleEast".equals(s)) {
                    return SegmentPosition.last;
                }
            }
            s = (String) jc.getClientProperty("JButton.segmentPosition");
            if (s != null) {
                if ("first".equals(s)) {
                    return SegmentPosition.first;
                } else if ("middle".equals(s)) {
                    return SegmentPosition.middle;
                } else if ("last".equals(s)) {
                    return SegmentPosition.last;
                }
            }
        }
        return SegmentPosition.only;
    }

    @Override
    protected Insets getVisualMargin(Component c, Insets insets) {
        String s = getStyle(c);

        insets = super.getVisualMargin(c, insets);
        if (insets instanceof javax.swing.plaf.UIResource) {
            if ("gradient".equals(s) && (c.getParent() instanceof JToolBar)) {
                String ts = (String) ((JToolBar) c.getParent()).getClientProperty("Quaqua.ToolBar.style");
                if ("placard".equals(ts) || "gradient".equals(ts)) {
                    InsetsUtil.clear(insets);
                }
            }
        }

        WidgetConfig wc = wcs.get(s);
        if (insets instanceof javax.swing.plaf.UIResource) {
            switch (getSegmentPosition(c)) {
                case first:
                    insets.right = 0;
                    break;
                case middle:
                    insets.left = insets.right = 0;
                    break;
                case last:
                    insets.left = 0;
                    break;
            }
        }
        return insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        if (c instanceof JComponent) {
            Insets i = (Insets) ((JComponent) c).getClientProperty("Quaqua.Border.insets");
            if (i != null) {
                InsetsUtil.setTo(i, insets);
                return insets;
            }
        }


        String s = getStyle(c);
        QuaquaUtilities.SizeVariant size = QuaquaUtilities.getSizeVariant(c);
        WidgetConfig wc = wcs.get(s);

        Insets i = getVisualMargin(c, insets);

        int sizeIndex;
        switch (size) {
            case REGULAR:
            default:
                sizeIndex = 0;
                break;
            case SMALL:
                sizeIndex = 1;
                break;
            case MINI:
                sizeIndex = 2;
                break;
        }
        if (wc != null) {
            Insets borderInsets = wc.borderInsets[sizeIndex];
            InsetsUtil.addTo(borderInsets, i);
        }

        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            Insets margin = b.getMargin();

            if (margin == null || (margin instanceof javax.swing.plaf.UIResource)) {
                if (b.isBorderPainted()) {
                    margin = wc==null||wc.margin.length<=sizeIndex?null:wc.margin[sizeIndex];
                }
            }
            if (margin != null) {
                InsetsUtil.addTo(margin, i);
            }
        }

        return i;
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    public static class UIResource extends QuaquaNativeButtonBorder implements javax.swing.plaf.UIResource {

        public UIResource() {
            super();
        }
    }
}
