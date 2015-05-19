/*
 * @(#)QuaquaButtonBorder.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.border.VisualMarginBorder;
import ch.randelshofer.quaqua.border.OverlayBorder;
import ch.randelshofer.quaqua.border.FocusBorder;
import ch.randelshofer.quaqua.border.ButtonStateBorder;
import ch.randelshofer.quaqua.border.AnimatedBorder;
import ch.randelshofer.quaqua.border.CompositeVisualMarginBorder;
import ch.randelshofer.quaqua.border.PressedCueBorder;
import ch.randelshofer.quaqua.osx.OSXAquaPainter.SegmentPosition;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;

import ch.randelshofer.quaqua.util.Images;
import ch.randelshofer.quaqua.util.InsetsUtil;
import javax.swing.plaf.InsetsUIResource;

/**
 * QuaquaButtonBorder.
 * This border uses client properties and font sizes of a JComponent to
 * determine which style the border shall have.
 * For some styles, the JComponent should honour size constrictions.
 * <p>
 * The following values of the client property <code>Quaqua.Button.style</code>
 * are supported:
 * <ul>
 * <li><code>push</code> Rounded push button. Maximum height of the JComponent
 * shall be constrained to its preferred height.</li>
 * <li><code>square</code> Square button. No size constraints.</li>
 * <li><code>placard</code> or <code>gradient</code> Placard button. No size constraints.</li>
 * <li><code>colorWell</code> Square button with color area in the center.
 * No size constraints.</li>
 * <li><code>bevel</code> Rounded Bevel button. No size constraints.</li>
 * <li><code>toggle</code> or <code>segmented</code> Toggle button. Maximum height of the JComponent
 * shall be constrained to its preferred height.</li>
 * <li><code>toggleWest</code> West Toggle button. Maximum height of the JComponent
 * shall be constrained to its preferred height.</li>
 * <li><code>toggleEast</code> East Toggle button. Maximum height of the JComponent
 * shall be constrained to its preferred height.</li>
 * <li><code>toggleCenter</code> Center Toggle button. Maximum height of the JComponent
 * shall be constrained to its preferred height.</li>
 * <li><code>toolBar</code> ToolBar button. No size constraints.</li>
 * <li><code>toolBarTab</code> ToolBar Tab button. No size constraints.</li>
 * <li><code>toolBarRollover</code> ToolBar button with rollover effect. No size constraints.</li>
 * </ul>
 * If the <code>Quaqua.Button.style</code> property is missing, then the
 * following values of the client property <code>JButton.buttonType</code>
 * are supported:
 * <ul>
 * <li><code>text</code> Rounded push button. Maximum height of the JComponent
 * shall be constrained to its preferred height.</li>
 * <li><code>toolBar</code> Square button. No size constraints.</li>
 * <li><code>icon</code> Rounded Bevel button. No size constraints.</li>
 * </ul>
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaButtonBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaButtonBorder implements Border, PressedCueBorder, UIResource {
    // Shared borders

    private static Border regularPushButtonBorder;
    private static Border smallPushButtonBorder;
    private static Border squareBorder;
    private static Border placardBorder;
    private static Border colorWellBorder;
    private static Border bevelBorder;
    private static Border toolBarBorder;
    private static Border toolBarRolloverBorder;
    private static Border toolBarTabBorder;
    private static Border toggleWestBorder;
    private static Border toggleEastBorder;
    private static Border toggleCenterBorder;
    private static Border toggleBorder;
    private static Border helpBorder;
    private static Border tableHeaderBorder;
    /**
     * The default client property value to be used, when no client property
     * has been specified for the JComponent.
     */
    private String defaultStyle;

    /** Creates a new instance. */
    public QuaquaButtonBorder(String defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    /** Returns a Border that implements the VisualMargin interface. */
    public Border getActualBorder(Component c) {
        Border b = null;
        String style = getStyle(c);

        JComponent jc = c instanceof JComponent ? (JComponent) c : null;
        String segpos = (jc == null) ? "only" : (String) jc.getClientProperty("JButton.segmentPosition");
        if ("toggleEast".equals(style)) {
            segpos = "first";
        } else if ("toggleCenter".equals(style)) {
            segpos = "middle";
        } else if ("toggleWest".equals(style)) {
            segpos = "last";
        }
        if (segpos == null//
                || !"first".equals(segpos) && !"middle".equals(segpos) && ! !"last".equals(segpos)) {
            segpos = "only";
        }

        // Explicitly chosen styles
        if ("text".equals(style) || "push".equals(style)) {
            switch (QuaquaUtilities.getSizeVariant(c)) {
                case SMALL:
                case MINI:
                    b = getSmallPushButtonBorder();
                    break;
                default:
                    b = getRegularPushButtonBorder();
                    break;
            }
        } else if ("toolBar".equals(style)) {
            if (toolBarBorder == null) {
                toolBarBorder = new CompositeVisualMarginBorder(new CompoundBorder(
                        new EmptyBorder(-1, -1, -1, -2),
                        new QuaquaToolBarButtonStateBorder(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/Toggle.borders.png")),
                        10, true,
                        new Insets(8, 10, 15, 10), new Insets(4, 6, 4, 6), true, false)),//
                        0, 0, 0, 0);
            }
            b = toolBarBorder;
        } else if ("toolBarRollover".equals(style)) {
            if (toolBarRolloverBorder == null) {
                toolBarRolloverBorder = new CompositeVisualMarginBorder(new CompoundBorder(
                        new EmptyBorder(-1, -1, -1, -2),
                        new QuaquaToolBarButtonStateBorder(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/Toggle.borders.png")),
                        10, true,
                        new Insets(8, 10, 15, 10), new Insets(4, 6, 4, 6), true, true)),//
                        0, 0, 0, 0);
            }
            b = toolBarRolloverBorder;
        } else if ("toolBarTab".equals(style)) {
            if (toolBarTabBorder == null) {
                toolBarTabBorder = new QuaquaToolBarTabButtonBorder();
            }
            b = toolBarTabBorder;
        } else if ("square".equals(style) || "toolbar".equals(style)) {
            b = getSquareBorder();
        } else if ("gradient".equals(style)) {
            b = getPlacardBorder();
        } else if ("tableHeader".equals(style)) {
            b = getTableHeaderBorder();
        } else if ("colorWell".equals(style)) {
            if (colorWellBorder == null) {
                colorWellBorder = new CompositeVisualMarginBorder(
                        new OverlayBorder(
                        new QuaquaColorWellBorder() /*)*/,
                        new CompoundBorder(
                        new EmptyBorder(-2, -2, -2, -2),
                        new FocusBorder(
                        QuaquaBorderFactory.create(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/Square.focusRing.png")),
                        new Insets(10, 9, 10, 8), new Insets(6, 9, 6, 9), true)))),//
                        0, 0, 0, 0);
            }
            b = colorWellBorder;
        } else if ("icon".equals(style) || "bevel".equals(style)) {
            if (bevelBorder == null) {
                Insets borderInsets = new Insets(4, 3, 3, 3);
                Border focusBorder = new FocusBorder(
                        QuaquaBorderFactory.create(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/RoundedBevel.focusRing.png")),
                        new Insets(10, 9, 10, 8), borderInsets, true));

                bevelBorder = new CompositeVisualMarginBorder(
                        new CompoundBorder(
                        new EmptyBorder(-3, -2, -2, -2),
                        new OverlayBorder(
                        new ButtonStateBorder(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/RoundedBevel.borders.png")),
                        10, true,
                        new Insets(10, 9, 10, 8), borderInsets, true),
                        new CompoundBorder(
                        new EmptyBorder(0, -1, 0, -1),
                        focusBorder))),//
                        0, 0, 0, 0);
            }
            b = bevelBorder;
        } else if ("only".equals(segpos) && ("toggle".equals(style) || "segmented".equals(style)
                || "segmentedRoundRect".equals(style) || "segmentedCapsule".equals(style)
                || style.contains("segmentedTextured"))) {
            if (toggleBorder == null) {
                Insets borderInsets = new Insets(3, 5, 3, 5);
                toggleBorder = new CompositeVisualMarginBorder(
                        new OverlayBorder(
                        new ButtonStateBorder(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/Toggle.borders.png")),
                        10, true,
                        new Insets(8, 10, 15, 10), borderInsets, true),
                        new FocusBorder(
                        QuaquaBorderFactory.create(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/Toggle.focusRing.png")),
                        new Insets(8, 10, 15, 10), borderInsets, false))),
                        2, 2, 2, 2);
            }
            b = toggleBorder;
        } else if ("first".equals(segpos)
                || "toggleEast".equals(style)) {
            if (toggleEastBorder == null) {
                Insets borderInsets = new Insets(3, 1, 3, 5);
                toggleEastBorder = new CompositeVisualMarginBorder(
                        new OverlayBorder(
                        new ButtonStateBorder(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/Toggle.east.borders.png")),
                        10, true,
                        new Insets(8, 1, 15, 10), borderInsets, true),
                        new FocusBorder(
                        QuaquaBorderFactory.create(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/Toggle.east.focusRing.png")),
                        new Insets(8, 4, 15, 10), borderInsets, false))),
                        2, 0, 2, 2, false, true, false, false);
            }
            b = toggleEastBorder;
        } else if ("middle".equals(segpos) || "toggleCenter".equals(style)) {
            if (toggleCenterBorder == null) {
                Insets borderInsets = new Insets(3, 1, 3, 1);
                toggleCenterBorder = new CompositeVisualMarginBorder(
                        new OverlayBorder(
                        new ButtonStateBorder(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/Toggle.center.borders.png")),
                        10, true,
                        new Insets(8, 0, 15, 1), borderInsets, true),
                        new FocusBorder(
                        QuaquaBorderFactory.create(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/Toggle.center.focusRing.png")),
                        new Insets(8, 4, 15, 4), borderInsets, false))),
                        2, 0, 2, 0, false, true, false, true);
            }
            b = toggleCenterBorder;
        } else if ("last".equals(segpos) || "toggleWest".equals(style)) {
            if (toggleWestBorder == null) {
                Insets borderInsets = new Insets(3, 5, 3, 1);
                toggleWestBorder = new CompositeVisualMarginBorder(
                        new OverlayBorder(
                        new ButtonStateBorder(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/Toggle.west.borders.png")),
                        10, true,
                        new Insets(8, 10, 15, 1), borderInsets, true),
                        new FocusBorder(
                        QuaquaBorderFactory.create(
                        Images.createImage(QuaquaButtonBorder.class.getResource("images/Toggle.west.focusRing.png")),
                        new Insets(8, 10, 15, 4), borderInsets, false))),
                        2, 2, 2, 0, false, false, false, true);
            }
            b = toggleWestBorder;
        } else if ("help".equals(style)) {
            if (helpBorder == null) {
                helpBorder = new VisualMarginBorder(2, 3, 2, 3);
            }
            b = helpBorder;
            // Implicit styles
        } else if (c.getParent() instanceof JToolBar) {
            b = getSquareBorder();
        } else {
            switch (QuaquaUtilities.getSizeVariant(c)) {
                case SMALL:
                case MINI:
                    b = getSmallPushButtonBorder();
                    break;
                default:
                    b = getRegularPushButtonBorder();
                    break;
            }
        }
        if (b == null) {
            throw new InternalError(style);
        }
        return b;
    }

    private Border getRegularPushButtonBorder() {
        if (regularPushButtonBorder == null) {
            Insets borderInsets = new Insets(1, 5, 1, 5);
            BufferedImage[] imageFrames = Images.split(
                    Images.createImage(QuaquaButtonBorder.class.getResource("images/Button.default.png")),
                    12, true);
            Border[] borderFrames = new Border[12];
            for (int i = 0; i < 12; i++) {
                borderFrames[i] = QuaquaBorderFactory.create(
                        imageFrames[i],
                        new Insets(11, 13, 13, 13),
                        borderInsets,
                        true);
            }
            ButtonStateBorder buttonStateBorder = new ButtonStateBorder(
                    Images.split(
                    Images.createImage(QuaquaButtonBorder.class.getResource("images/Button.borders.png")),
                    10, true),
                    new Insets(11, 13, 13, 13),
                    borderInsets,
                    true);

            buttonStateBorder.setBorder(
                    ButtonStateBorder.DEFAULT,
                    new AnimatedBorder(borderFrames, 100));

            regularPushButtonBorder = new CompositeVisualMarginBorder(
                    new OverlayBorder(
                    buttonStateBorder,
                    new FocusBorder(
                    QuaquaBorderFactory.create(
                    Images.createImage(QuaquaButtonBorder.class.getResource("images/Button.focusRing.png")),
                    new Insets(12, 13, 12, 13),
                    borderInsets,
                    false))),
                    2, 4, 2, 4);
        }
        return regularPushButtonBorder;
    }

    private Border getSquareBorder() {
        if (squareBorder == null) {
            squareBorder = new CompositeVisualMarginBorder(
                    new OverlayBorder(
                    QuaquaBorderFactory.createSquareButtonBorder(),
                    new CompoundBorder(
                    new EmptyBorder(-2, -2, -2, -2),
                    new FocusBorder(
                    QuaquaBorderFactory.create(
                    Images.createImage(QuaquaButtonBorder.class.getResource("images/Square.focusRing.png")),
                    new Insets(10, 9, 10, 8), new Insets(6, 9, 6, 9), true)))),
                    0, 0, 0, 0) {

                @Override
                protected Insets getVisualMargin(Component c, Insets insets) {
                    String s = getStyle(c);

                    insets = super.getVisualMargin(c, new InsetsUIResource(0, 0, 0, 0));

                    if (insets instanceof javax.swing.plaf.UIResource) {
                        switch (getSegmentPosition(c)) {
                            case first:
                                insets.right = -1;
                                break;
                            case middle:
                                insets.left = 0;
                                insets.right = -1;
                                break;
                            case last:
                                insets.left = 0;
                                break;
                        }
                    }
                    return insets;
                }
            };
        }
        return squareBorder;
    }

    private Border getPlacardBorder() {
        if (placardBorder == null) {
            placardBorder = new CompositeVisualMarginBorder(
                    new OverlayBorder(
                    new CompoundBorder(
                    new EmptyBorder(-1, 0, -1, 0),
                    QuaquaBorderFactory.createPlacardButtonBorder()),
                    new CompoundBorder(
                    new EmptyBorder(-1, -1, -1, -1),
                    new FocusBorder(
                    QuaquaBorderFactory.create(
                    Images.createImage(QuaquaButtonBorder.class.getResource("images/Square.focusRing.png")),
                    new Insets(10, 9, 10, 8), new Insets(6, 9, 6, 9), true)))),
                    0, 0, 0, 0) {

                @Override
                protected Insets getVisualMargin(Component c, Insets insets) {
                    String s = getStyle(c);

                    insets = super.getVisualMargin(c, new InsetsUIResource(0, 0, 0, 0));
                    if (insets instanceof javax.swing.plaf.UIResource) {
                        if ("gradient".equals(s) && (c.getParent() instanceof JToolBar)) {
                            String ts = (String) ((JToolBar) c.getParent()).getClientProperty("Quaqua.ToolBar.style");
                            if (ts != null && ("placard".equals(ts) || "gradient".equals(ts))) {
                                InsetsUtil.clear(insets);
                            }
                        }
                    }

                    if (insets instanceof javax.swing.plaf.UIResource) {
                        switch (getSegmentPosition(c)) {
                            case first:
                                insets.right = -1;
                                break;
                            case middle:
                                insets.left = 0;
                                insets.right = -1;
                                break;
                            case last:
                                insets.left = 0;
                                break;
                        }
                    }
                    return insets;
                }
            };
        }
        return placardBorder;
    }

    private Border getTableHeaderBorder() {
        if (tableHeaderBorder == null) {
            tableHeaderBorder = new CompositeVisualMarginBorder(
                    new ButtonStateBorder(
                    Images.createImage(QuaquaButtonBorder.class.getResource("images/TableHeader.borders.png")),
                    4, true, new Insets(7, 1, 8, 1), new Insets(1, 2, 1, 2), true),
                    0, 0, 0, 0);
        }
        return tableHeaderBorder;
    }

    private Border getSmallPushButtonBorder() {
        if (smallPushButtonBorder == null) {
            Insets borderInsets = new Insets(3, 8, 3, 8);
            BufferedImage[] imageFrames = Images.split(
                    Images.createImage(QuaquaButtonBorder.class.getResource("images/Button.small.default.png")),
                    12, true);
            Border[] borderFrames = new Border[12];
            for (int i = 0; i < 12; i++) {
                borderFrames[i] = QuaquaBorderFactory.create(
                        imageFrames[i],
                        new Insets(9, 13, 12, 13),
                        borderInsets,
                        true);
            }
            ButtonStateBorder buttonStateBorder = new ButtonStateBorder(
                    Images.split(
                    Images.createImage(QuaquaButtonBorder.class.getResource("images/Button.small.borders.png")),
                    10, true),
                    new Insets(9, 13, 12, 13),
                    borderInsets,
                    true);
            buttonStateBorder.setBorder(
                    ButtonStateBorder.DEFAULT,
                    new AnimatedBorder(borderFrames, 100));

            smallPushButtonBorder = new CompositeVisualMarginBorder(
                    new CompoundBorder(
                    new EmptyBorder(-2, -3, -2, -3),
                    new OverlayBorder(
                    buttonStateBorder,
                    new FocusBorder(
                    QuaquaBorderFactory.create(
                    Images.createImage(QuaquaButtonBorder.class.getResource("images/Button.small.focusRing.png")),
                    new Insets(9, 14, 12, 14),
                    borderInsets,
                    false)))),
                    0, 0, 0, 0);
        }
        return smallPushButtonBorder;
    }

    /**
     * Returns the default button margin for the specified component.
     *
     * FIXME: We should not create a new Insets instance on each method call.
     */
    public Insets getDefaultMargin(JComponent c) {
        Insets margin = null;
        String style = getStyle(c);
        QuaquaUtilities.SizeVariant sizeVariant = QuaquaUtilities.getSizeVariant(c);
        boolean isSmall = sizeVariant == QuaquaUtilities.SizeVariant.SMALL //
                || sizeVariant == QuaquaUtilities.SizeVariant.MINI;


        // Explicitly chosen styles
        if ("text".equals(style) || "push".equals(style)) {
            if (isSmall) {
                margin = new Insets(1, 3, 1, 3);
            } else {
                margin = new Insets(1, 6, 2, 6);
            }
        } else if ("toolBar".equals(style)) {
            margin = new Insets(0, 0, 0, 0);
        } else if ("toolBarRollover".equals(style)) {
            margin = new Insets(0, 0, 0, 0);
        } else if ("toolBarTab".equals(style)) {
            margin = new Insets(0, 0, 0, 0);
        } else if ("square".equals(style)) {
            if (isSmall) {
                margin = new Insets(3, 6, 3, 6);
            } else {
                margin = new Insets(3, 6, 3, 6);
            }
        } else if ("gradient".equals(style)) {
            if (isSmall) {
                margin = new Insets(2, 6, 2, 6);
            } else {
                margin = new Insets(2, 6, 2, 6);
            }
        } else if ("colorWell".equals(style)) {
            if (isSmall) {
                margin = new Insets(1, 6, 1, 6);
            } else {
                margin = new Insets(1, 6, 2, 6);
            }
        } else if ("icon".equals(style) || "bevel".equals(style)) {
            if (isSmall) {
                margin = new Insets(1, 6, 1, 6);
            } else {
                margin = new Insets(1, 6, 2, 6);
            }
        } else if ("toggle".equals(style)) {
            if (isSmall) {
                margin = new Insets(1, 5, 1, 5);
            } else {
                margin = new Insets(1, 5, 2, 5);
            }
        } else if ("toggleEast".equals(style)) {
            if (isSmall) {
                margin = new Insets(1, 5, 1, 5);
            } else {
                margin = new Insets(1, 5, 2, 5);
            }
        } else if ("toggleCenter".equals(style)) {
            if (isSmall) {
                margin = new Insets(1, 5, 1, 5);
            } else {
                margin = new Insets(1, 5, 2, 5);
            }
        } else if ("toggleWest".equals(style)) {
            if (isSmall) {
                margin = new Insets(1, 5, 1, 5);
            } else {
                margin = new Insets(1, 5, 2, 5);
            }
        } else if ("help".equals(style)) {
            margin = new Insets(0, 0, 0, 0);

            // Implicit styles
        } else if (c.getParent() instanceof JToolBar) {
            margin = new Insets(0, 0, 0, 0);
        } else {
            if (isSmall) {
                margin = new Insets(1, 4, 1, 4);
            } else {
                margin = new Insets(1, 8, 2, 8);
            }
        }
        return margin;
    }

    public boolean isFixedHeight(JComponent c) {
        String style = getStyle(c).toLowerCase();
        return "text".equals(style) || "push".equals(style) || style.startsWith("toggle");
    }

    protected String getStyle(Component c) {
        return QuaquaButtonUI.getStyle(c,defaultStyle);
    }

    /**
     * Returns true, if this border has a visual cue for the pressed
     * state of the button.
     * If the border has no visual cue, then the ButtonUI has to provide
     * it by some other means.
     */
    public boolean hasPressedCue(JComponent c) {
        Border b = getActualBorder(c);
        boolean haspc;
        if (b instanceof PressedCueBorder) {
            haspc = ((PressedCueBorder) b).hasPressedCue(c);
        }
        haspc = b != toolBarBorder;
        return haspc;
    }

    public Insets getVisualMargin(Component c) {
        return ((VisualMargin) getActualBorder(c)).getVisualMargin(c);
    }

    /**
     * Returns true, if this border has a visual cue for the disabled
     * state of the button.
     * If the border has no visual cue, then the ButtonUI has to provide
     * it by some other means.
     * /
     * public boolean hasDisabledCue(JComponent c) {
     * return false;
     * }*/
    public Insets getBorderInsets(Component c) {
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            Insets insets = (Insets) jc.getClientProperty("Quaqua.Border.insets");
            if (insets != null) {
                return (Insets) insets.clone();
            }
        }

        boolean isBorderPainted = true;
        if (c instanceof AbstractButton) {
            isBorderPainted = ((AbstractButton) c).isBorderPainted();
        }
        Insets insets;
        if (!isBorderPainted) {
            insets = (Insets) UIManager.getInsets("Component.visualMargin").clone();
        } else {
            insets = getActualBorder((JComponent) c).getBorderInsets(c);
        }

        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            Insets margin = b.getMargin();
            if (margin == null || (margin instanceof UIResource)) {
                if (isBorderPainted) {
                    margin = getDefaultMargin((JComponent) c);
                }
            }
            if (margin != null) {
                InsetsUtil.addTo(margin, insets);
            }
        }

        return insets;
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        getActualBorder((JComponent) c).paintBorder(c, g, x, y, width, height);
    }

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
}
