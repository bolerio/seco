/*
 * @(#)QuaquaButtonUI.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.border.BackgroundBorder;
import ch.randelshofer.quaqua.border.PressedCueBorder;
import ch.randelshofer.quaqua.util.Debug;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

/**
 * QuaquaButtonUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaButtonUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaButtonUI extends BasicButtonUI implements VisuallyLayoutable {
    // Shared UI object

    private final static QuaquaButtonUI buttonUI = new QuaquaButtonUI();
    /* These Insets/Rectangles are allocated once for all
     * RadioButtonUI.getPreferredSize() calls.  Re-using rectangles
     * rather than allocating them in each call substantially
     * reduced the time it took getPreferredSize() to run.  Obviously,
     * this method can't be re-entered.
     */
    private static Rectangle viewR = new Rectangle();
    private static Rectangle iconR = new Rectangle();
    private static Rectangle textR = new Rectangle();
    private static Insets viewInsets = new Insets(0, 0, 0, 0);
    // Has the shared instance defaults been initialized?
    private boolean defaults_initialized = false;
//    private static HashSet<AbstractButton> animatedComponents = new HashSet<AbstractButton>();

    /**
     * Push Button.
     * Preferred spacing between buttons and other components.
     * /
     * private final static Insets pushRegularSpacing = new Insets(12,12,12,12);
     * private final static Insets pushSmallSpacing = new Insets(10,10,10,10);
     * private final static Insets pushMiniSpacing = new Insets(8,8,8,8);
     * /**
     * Metal Button.
     * Preferred spacing between buttons and other components.
     * /
     * private final static Insets metalRegularSpacing = new Insets(12,12,12,12);
     * private final static Insets metalSmallSpacing = new Insets(8,8,8,8);
     * /**
     * Bevel Button.
     * Preferred spacing between buttons and other components.
     *
     * Large Spacing is used, if the bevel button contains an icon that
     * is 24 x 24 pixels or larger.
     * /
     * private final static Insets bevelLargeSpacing = new Insets(8,8,8,8);
     * private final static Insets bevelRegularSpacing = new Insets(0,0,0,0);
     * /**
     * Square Button.
     * Preferred spacing between buttons and other components.
     * /
     * private final static Insets squareSpacing = new Insets(0,0,0,0);
     * /**
     * Icon Button (buttons without border and an icon, and, optionally, text)
     * Preferred spacing between buttons and other components.
     *
     * Large Spacing is used, if the icon button contains an icon that
     * is 24 x 24 pixels or larger.
     * /
     * private final static Insets iconLargeSpacing = new Insets(8,8,8,8);
     * private final static Insets iconRegularSpacing = new Insets(0,0,0,0);
     * /**
     * Round Button and Help Button.
     * /
     * private final static Insets roundSpacing = new Insets(12,12,12,12);
     */
    // ********************************
    //          Create PLAF
    // ********************************
    public static ComponentUI createUI(JComponent c) {
        return buttonUI;
    }

    @Override
    protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);

        String pp = getPropertyPrefix();
        //b.setOpaque(QuaquaManager.getBoolean(pp+"opaque"));
        QuaquaUtilities.installProperty(b, "opaque", UIManager.get(pp + "opaque"));
        b.setRequestFocusEnabled(UIManager.getBoolean(pp + "requestFocusEnabled"));
        b.setFocusable(UIManager.getBoolean(pp + "focusable"));
    }

    @Override
    protected BasicButtonListener createButtonListener(AbstractButton b) {
        return new QuaquaButtonListener(b);
    }

    private static Font getFont(JComponent c) {

        Font f = QuaquaUtilities.getSizeVariantFont(c);
        return f;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (c instanceof JButton) {
            JButton b = (JButton) c;
            if (b.isDefaultButton()) {
                QuaquaDefaultButtonAnimator.addDefaultButton(b);
            }
        }
        g.setFont(getFont(c));

        String style = (String) c.getClientProperty("Quaqua.Button.style");
        if (style == null) {
            style = (String) c.getClientProperty("JButton.buttonType");
        }
        if (style != null && style.equals("help")) {
            Insets insets = c.getInsets();
            if (insets == null) {
                insets = new Insets(0, 0, 0, 0);
            }
            QuaquaUtilities.SizeVariant sv = QuaquaUtilities.getSizeVariant(c);
            switch (sv) {
                default:
                    UIManager.getIcon("Button.helpIcon").paintIcon(c, g, insets.left, insets.top);
                    break;
                case SMALL:
                    UIManager.getIcon("Button.smallHelpIcon").paintIcon(c, g, insets.left, insets.top);
                    break;
                case MINI:
                    UIManager.getIcon("Button.miniHelpIcon").paintIcon(c, g, insets.left, insets.top);
                    break;
            }
            Debug.paint(g, c, this);
            return;
        }

        Object oldHints = QuaquaUtilities.beginGraphics((Graphics2D) g);
        if (((AbstractButton) c).isBorderPainted()) {
            Border b = c.getBorder();
            if (b != null && b instanceof BackgroundBorder) {
                ((BackgroundBorder) b).getBackgroundBorder().paintBorder(c, g, 0, 0, c.getWidth(), c.getHeight());
            }
        } else {
            g.setColor(c.getBackground());
            Insets ins = c.getInsets();
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
        super.paint(g, c);
        QuaquaUtilities.endGraphics((Graphics2D) g, oldHints);
        Debug.paint(g, c, this);
    }

    /**
     * As of Java 2 platform v 1.4 this method should not be used or overriden.
     * Use the paintText method which takes the AbstractButton argument.
     */
    @Override
    protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
        paintText(g, (AbstractButton) c, textRect, text);
    }

    /**
     * Method which renders the text of the current button.
     * <p>
     * @param g Graphics context
     * @param b Current button to render
     * @param textRect Bounding rectangle to render the text.
     * @param text String to render
     * @since 1.4
     */
    @Override
    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
        ButtonModel model = b.getModel();
        FontMetrics fm = g.getFontMetrics();
        
        // Suppress mnemonic
        //int mnemonicIndex = Methods.invokeGetter(b, "getDisplayedMnemonicIndex", -1);
        int mnemonicIndex = -1;
        boolean borderHasPressedCue = borderHasPressedCue(b);

        /* Draw the Text */
        if (model.isPressed() && model.isArmed() && !borderHasPressedCue) {
            g.setColor(new Color(0xa0000000, true));
            QuaquaUtilities.drawStringUnderlineCharAt(g, text, mnemonicIndex,
                    textRect.x + getTextShiftOffset(),
                    textRect.y + fm.getAscent() + getTextShiftOffset() + 1);
        }

        if (model.isEnabled()) {
            /*** paint the text normally */
            g.setColor(b.getForeground());
        } else {
            Color c = UIManager.getColor(getPropertyPrefix() + "disabledForeground");
            g.setColor((c != null) ? c : b.getForeground());
        }
        QuaquaUtilities.drawStringUnderlineCharAt(g, text, mnemonicIndex,
                textRect.x + getTextShiftOffset(),
                textRect.y + fm.getAscent() + getTextShiftOffset());
    }

    @Override
    protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        Icon icon = b.getIcon();
        Icon tmpIcon = null;
        Icon shadowIcon = null;
        boolean borderHasPressedCue = borderHasPressedCue(b);

        if (icon == null) {
            return;
        }

        if (!model.isEnabled()) {
            if (model.isSelected()) {
                tmpIcon = (Icon) b.getDisabledSelectedIcon();
            } else {
                tmpIcon = (Icon) b.getDisabledIcon();
            }
        } else if (model.isPressed() && model.isArmed()) {
            tmpIcon = (Icon) b.getPressedIcon();
            if (tmpIcon != null) {
                // revert back to 0 offset
                clearTextShiftOffset();
            } else if (icon != null
                    && icon instanceof ImageIcon
                    && !borderHasPressedCue) {
                // Create an icon on the fly.
                // Note: This is only needed for borderless buttons, which
                //       have no other way to provide feedback about the pressed
                //       state.
                tmpIcon = new ImageIcon(
                        HalfbrightFilter.createHalfbrightImage(
                        ((ImageIcon) icon).getImage()));
                shadowIcon = new ImageIcon(
                        ShadowFilter.createShadowImage(
                        ((ImageIcon) icon).getImage()));
            }
        } else if (b.isRolloverEnabled() && model.isRollover()) {
            if (model.isSelected()) {
                tmpIcon = b.getRolloverSelectedIcon();
                if (tmpIcon == null) {
                    tmpIcon = b.getSelectedIcon();
                }
            } else {
                tmpIcon = (Icon) b.getRolloverIcon();
            }
        } else if (model.isSelected()) {
            tmpIcon = b.getSelectedIcon();
        }

        if (tmpIcon != null) {
            icon = tmpIcon;
        }

        if (model.isPressed() && model.isArmed()) {
            if (shadowIcon != null) {
                shadowIcon.paintIcon(c, g, iconRect.x + getTextShiftOffset(),
                        iconRect.y + getTextShiftOffset() + 1);
            }
            icon.paintIcon(c, g, iconRect.x + getTextShiftOffset(),
                    iconRect.y + getTextShiftOffset());
        } else {
            icon.paintIcon(c, g, iconRect.x, iconRect.y);
        }

    }

    private boolean isFixedHeight(JComponent c) {
        Border b = c.getBorder();
        if (b != null && b instanceof BackgroundBorder) {
            b = ((BackgroundBorder) b).getBackgroundBorder();
            if (b != null && b instanceof QuaquaButtonBorder) {
                return ((QuaquaButtonBorder) b).isFixedHeight(c);
            }
        }
        return false;
    }

    private boolean borderHasPressedCue(AbstractButton c) {
        if (c.isBorderPainted()) {
            Border b = c.getBorder();
            if (b instanceof PressedCueBorder) {
                return ((PressedCueBorder) b).hasPressedCue(c);
            }
            return b != null;
        } else {
            return false;
        }
    }

    /**
     * This method is here, to let QuaquaButtonListener access this
     * property.
     */
    @Override
    protected String getPropertyPrefix() {
        return super.getPropertyPrefix();
    }
    // ********************************
    //          Layout Methods
    // ********************************

    @Override
    public Dimension getMinimumSize(JComponent c) {
        AbstractButton b = (AbstractButton) c;
        String style = (String) c.getClientProperty("Quaqua.Button.style");
        if (style == null) {
            style = (String) c.getClientProperty("JButton.buttonType");
        }
        if (style == null) {
            if (b.getBorder() instanceof UIResource
                    && b.isBorderPainted()) {
                style = "push";
            } else {
                style = "plain";
            }
        }
        if (style.equals("help")) {
            return getPreferredSize(c);
        }
        Dimension d = super.getMinimumSize(c);
        if (isFixedHeight(c)) {
            Dimension p = getPreferredSize(c);
            if (d != null && p != null) {
                d.height = Math.max(d.height, p.height);
            }
        }
        boolean isSmall = QuaquaUtilities.getSizeVariant(c) == QuaquaUtilities.SizeVariant.SMALL;
        if (!isSmall && style.equals("push") //
                && b.getIcon() == null && b.getText() != null) {
            if (d != null) {
                d.width = Math.max(d.width, UIManager.getInt("Button.minimumWidth"));
            }
        }
        return d;
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        String style = (String) c.getClientProperty("Quaqua.Button.style");
        if (style == null) {
            style = (String) c.getClientProperty("JButton.buttonType");
        }
        if (style != null && style.equals("help")) {
            return getPreferredSize(c);
        }
        Dimension d = super.getMaximumSize(c);
        if (isFixedHeight(c)) {
            Dimension p = getPreferredSize(c);
            d.height = Math.max(d.height, p.height);
        }
        return d;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return QuaquaButtonUI.getPreferredSize((AbstractButton) c);
    }

    protected static String getStyle(Component c, String defaultStyle) {

        String s = null;
        JComponent jc = (c instanceof JComponent) ? (JComponent) c : null;
        if (jc != null) {
            s = (String) jc.getClientProperty("Quaqua.Button.style");
            if (s == null) {
                s = (String) jc.getClientProperty("JButton.buttonType");
            }
        }
        if (s == null) {
            if (c.getParent() instanceof JToolBar) {
                String tbs = (String) ((JToolBar) c.getParent()).getClientProperty("Quaqua.ToolBar.style");
                if (tbs != null && ("gradient".equals(tbs) || "placard".equals(tbs))) {
                    s = "gradient";
                } else {
                    s = "toolBar";
                }
            }
        }
        if (s == null || "segmented".equals(s) || "toggle".equals(s)
                || "segmentedRoundRect".equals(s) || "segmentedCapsule".equals(s)
                || s.contains("segmentedTextured")) {
            String segmentPosition = jc == null ? null : (String) jc.getClientProperty("JButton.segmentPosition");
            if (segmentPosition != null) {
                if ("first".equals(segmentPosition)) {
                    s = "toggleWest";
                } else if ("middle".equals(segmentPosition)) {
                    s = "toggleCenter";
                } else if ("last".equals(segmentPosition)) {
                    s = "toggleEast";
                }
            }
        }
        if (s == null) {
            s = (defaultStyle == null) ? "push" : defaultStyle;
        }

        // coerce synonyms
        if ("placard".equals(s) || "segmentedGradient".equals(s)) {
            s = "gradient";
        }

        return s;
    }

    public static Dimension getPreferredSize(AbstractButton b) {
        String style = getStyle(b, "push");
        QuaquaUtilities.SizeVariant sv = QuaquaUtilities.getSizeVariant(b);
        if ("help".equals(style)) {
            Icon helpIcon;
            switch (sv) {
                default:
                    helpIcon = UIManager.getIcon("Button.helpIcon");
                    break;
                case SMALL:
                    helpIcon = UIManager.getIcon("Button.smallHelpIcon");
                    break;
                case MINI:
                    helpIcon = UIManager.getIcon("Button.miniHelpIcon");
                    break;
            }
            Insets insets = b.getInsets();
            if (insets == null) {
                insets = new Insets(0, 0, 0, 0);
            }
            return new Dimension(
                    helpIcon.getIconWidth() + insets.left + insets.right,
                    helpIcon.getIconHeight() + insets.top + insets.bottom);
        }
        if (b.getComponentCount() > 0) {
            return null;
        }

        int textIconGap = Methods.invokeGetter(b, "getIconTextGap", 4);
        Icon icon = (Icon) b.getIcon();
        String text = b.getText();

        Font font = getFont(b);

        FontMetrics fm;
        try {
            fm = b.getFontMetrics(font);
        } catch (NullPointerException e) {
            // NPE occurs when no font render context can be found.
            // getFontMetrics should handle that internally and just
            // return null, but it does not.
            return new Dimension(0, 0);
        }

        viewR.x = viewR.y = 0;
        viewR.width = Short.MAX_VALUE;
        viewR.height = Short.MAX_VALUE;
        iconR.x = iconR.y = iconR.width = iconR.height = 0;
        textR.x = textR.y = textR.width = textR.height = 0;

        SwingUtilities.layoutCompoundLabel(
                (JComponent) b, fm, text, icon,
                b.getVerticalAlignment(), b.getHorizontalAlignment(),
                b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                viewR, iconR, textR, (text == null ? 0 : textIconGap));

        /* The preferred size of the button is the size of
         * the text and icon rectangles plus the buttons insets.
         */

        Rectangle r = iconR.union(textR);

        //if (b.isBorderPainted()) {
        Insets insets = b.getInsets();
        if (insets == null) {
            insets = new Insets(0, 0, 0, 0);
        }
        r.width += insets.left + insets.right;
        r.height += insets.top + insets.bottom;

        if (!(b.getBorder() instanceof UIResource)) {
            Insets margin = b.getMargin();
            if (margin != null) {
                r.width += margin.left + margin.right;
                r.height += margin.top + margin.bottom;
            }
        }
        //}
        if (sv == QuaquaUtilities.SizeVariant.REGULAR && style.equals("push")
                && b.getIcon() == null && b.getText() != null) {
            r.width = Math.max(r.width, UIManager.getInt("Button.minimumWidth"));
        }
        return r.getSize();
    }

    /**
     * Forwards the call to SwingUtilities.layoutCompoundLabel().
     * This method is here so that a subclass could do Label specific
     * layout and to shorten the method name a little.
     *
     * @see SwingUtilities#layoutCompoundLabel
     */
    protected String layoutCL(
            AbstractButton c,
            FontMetrics fontMetrics,
            String text,
            Icon icon,
            Rectangle viewR,
            Rectangle iconR,
            Rectangle textR) {
        return SwingUtilities.layoutCompoundLabel(
                c,
                fontMetrics,
                text,
                icon,
                c.getVerticalAlignment(),
                c.getHorizontalAlignment(),
                c.getVerticalTextPosition(),
                c.getHorizontalTextPosition(),
                viewR,
                iconR,
                textR,
                Methods.invokeGetter(c, "getIconTextGap", 4));
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        Rectangle vb = getVisualBounds(c, VisuallyLayoutable.TEXT_BOUNDS, width, height);
        return (vb == null) ? -1 : vb.y + vb.height;
    }

    public Rectangle getVisualBounds(JComponent c, int type, int width, int height) {
        Rectangle bounds = new Rectangle(0, 0, width, height);
        if (type == VisuallyLayoutable.CLIP_BOUNDS) {
            return bounds;
        }

        AbstractButton b = (AbstractButton) c;

        if (type == VisuallyLayoutable.COMPONENT_BOUNDS
                && b.getBorder() != null
                && b.isBorderPainted()) {
            Border border = b.getBorder();
            if (border instanceof BackgroundBorder) {
                border = ((BackgroundBorder) border).getBackgroundBorder();
            }
            if (border instanceof VisualMargin) {
                InsetsUtil.subtractInto(
                        ((VisualMargin) border).getVisualMargin(c),
                        bounds);
            } else if (border instanceof QuaquaButtonBorder) {
                InsetsUtil.subtractInto(
                        ((QuaquaButtonBorder) border).getVisualMargin(c),
                        bounds);
            }

            return bounds;
        }

        String style = getStyle(b, null);

        String text = style.equals("help") ? null : b.getText();
        boolean isEmpty = (text == null || text.length() == 0);
        if (isEmpty) {
            text = " ";
        }
        Icon icon = (b.isEnabled()) ? b.getIcon() : b.getDisabledIcon();

        if ((icon == null) && (text == null)) {
            return null;
        }

        FontMetrics fm;
        try {
            fm = c.getFontMetrics(getFont(c));
        } catch (NullPointerException e) {
            // getFontMetrics does not handle missing font render context.
            return null;
        }
        Insets insets = c.getInsets(viewInsets);
        if (insets == null) {
            insets = new Insets(0, 0, 0, 0);
        }
        viewR.x = insets.left;
        viewR.y = insets.top;
        viewR.width = width - (insets.left + insets.right);
        viewR.height = height - (insets.top + insets.bottom);

        iconR.x = iconR.y = iconR.width = iconR.height = 0;
        textR.x = textR.y = textR.width = textR.height = 0;

        String clippedText =
                layoutCL(b, fm, text, icon, viewR, iconR, textR);

        Rectangle textBounds = Fonts.getPerceivedBounds(text, getFont(c), c);
        if (isEmpty) {
            textBounds.width = 0;
        }
        int ascent = fm.getAscent();
        textR.x += textBounds.x;
        textR.width = textBounds.width;
        textR.y += ascent + textBounds.y;
        textR.height -= fm.getHeight() - textBounds.height;

        bounds.setBounds(textR);
        if (type == VisuallyLayoutable.COMPONENT_BOUNDS) {
            if (!iconR.isEmpty()) {
                bounds.add(iconR);
            }
        }
        return bounds;
    }
    /*
    private static void animateButton(final AbstractButton b) {
    if (animatedComponents.add(b)) {
    Timer animationTimer;
    long sleep = 1000 / 30; // 30 fps
    animationTimer = new Timer((int) sleep, new ActionListener() {
    
    public void actionPerformed(ActionEvent event) {
    animatedComponents.remove(b);
    b.repaint();
    }
    });
    animationTimer.start();
    }
    }*/
}
