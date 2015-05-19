/*
 * @(#)QuaquaCheckBoxUI.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.util.Debug;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.table.*;
import javax.swing.plaf.basic.*;
import java.beans.*;
import javax.swing.text.View;

/**
 * QuaquaCheckBoxUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaCheckBoxUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaCheckBoxUI extends BasicCheckBoxUI implements VisuallyLayoutable {

    private final static QuaquaCheckBoxUI checkboxUI = new QuaquaCheckBoxUI();
    private final static PropertyChangeHandler propertyChangeListener = new PropertyChangeHandler();
    private boolean defaults_initialized = false;

    /**
     * Preferred spacing between check boxes and other components.
     * /
    private final static Insets regularSpacing = new Insets(6,6,6,6);
    private final static Insets smallSpacing = new Insets(6,6,6,6);
    private final static Insets miniSpacing = new Insets(5,5,5,5);
     */
    public static ComponentUI createUI(JComponent b) {
        return checkboxUI;
    }
    // ********************************
    //        Install PLAF
    // ********************************

    @Override
    protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        if (!defaults_initialized) {
            defaults_initialized = true;
        }
        QuaquaUtilities.installProperty(b, "opaque", UIManager.get("CheckBox.opaque"));
        //b.setOpaque(false);
        b.setRequestFocusEnabled(UIManager.getBoolean("CheckBox.requestFocusEnabled"));
        b.setFocusable(UIManager.getBoolean("CheckBox.focusable"));
    }

    // ********************************
    //        Uninstall PLAF
    // ********************************
    @Override
    protected void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        defaults_initialized = false;
    }

    @Override
    protected void installListeners(AbstractButton b) {
        super.installListeners(b);
        b.addPropertyChangeListener(propertyChangeListener);
    }

    @Override
    protected void uninstallListeners(AbstractButton b) {
        super.uninstallListeners(b);
        b.removePropertyChangeListener(propertyChangeListener);
    }

    @Override
    protected BasicButtonListener createButtonListener(AbstractButton b) {
        return new QuaquaButtonListener(b);
    }

    private boolean isSmall(JComponent c) {
        boolean isSmall=QuaquaUtilities.getSizeVariant(c)==QuaquaUtilities.SizeVariant.SMALL;
        
        return isSmall;
    }

        public Icon getDefaultIcon(JComponent c) {
        switch (QuaquaUtilities.getSizeVariant(c)) {
            default:
                return UIManager.getIcon("CheckBox.icon");
            case SMALL:
                return UIManager.getIcon("CheckBox.smallIcon");
            case MINI:
                return UIManager.getIcon("CheckBox.miniIcon");
        }
    }

    /* These Dimensions/Rectangles are allocated once for all
     * RadioButtonUI.paint() calls.  Re-using rectangles
     * rather than allocating them in each paint call substantially
     * reduced the time it took paint to run.  Obviously, this
     * method can't be re-entered.
     */
    private static Dimension size = new Dimension();
    private static Rectangle viewR = new Rectangle();
    private static Rectangle iconR = new Rectangle();
    private static Rectangle textR = new Rectangle();
    private static Insets viewInsets = new Insets(0, 0, 0, 0);

    @Override
    public void paint(Graphics g, JComponent c) {
        Object oldHints = QuaquaUtilities.beginGraphics((Graphics2D) g);
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();

        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();

        Insets i = getInsets(b, viewInsets);
        size = b.getSize(size);
        viewR.x = i.left;
        viewR.y = i.top;
        viewR.width = size.width - (i.right + viewR.x);
        viewR.height = size.height - (i.bottom + viewR.y);
        iconR.x = iconR.y = iconR.width = iconR.height = 0;
        textR.x = textR.y = textR.width = textR.height = 0;

        Icon altIcon = b.getIcon();
        Icon selectedIcon = null;
        Icon disabledIcon = null;

        String text = SwingUtilities.layoutCompoundLabel(
                c, fm, b.getText(), altIcon != null ? altIcon : getDefaultIcon(c),
                b.getVerticalAlignment(), b.getHorizontalAlignment(),
                b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                viewR, iconR, textR,
                b.getText() == null ? 0 : Methods.invokeGetter(b, "getIconTextGap", 4));

        // fill background
        if (c.isOpaque()) {
            g.setColor(b.getBackground());
            g.fillRect(0, 0, size.width, size.height);
        }

        // Paint the radio button
        if (altIcon != null) {

            if (!model.isEnabled()) {
                if (model.isSelected()) {
                    altIcon = b.getDisabledSelectedIcon();
                } else {
                    altIcon = b.getDisabledIcon();
                }
            } else if (model.isPressed() && model.isArmed()) {
                altIcon = b.getPressedIcon();
                if (altIcon == null) {
                    // Use selected icon
                    altIcon = b.getSelectedIcon();
                }
            } else if (model.isSelected()) {
                if (b.isRolloverEnabled() && model.isRollover()) {
                    altIcon = (Icon) b.getRolloverSelectedIcon();
                    if (altIcon == null) {
                        altIcon = (Icon) b.getSelectedIcon();
                    }
                } else {
                    altIcon = (Icon) b.getSelectedIcon();
                }
            } else if (b.isRolloverEnabled() && model.isRollover()) {
                altIcon = (Icon) b.getRolloverIcon();
            }

            if (altIcon == null) {
                altIcon = b.getIcon();
            }

            altIcon.paintIcon(c, g, iconR.x, iconR.y);

        } else {
            Icon defaultIcon = getDefaultIcon(c);
            if (defaultIcon != null) {
                defaultIcon.paintIcon(c, g, iconR.x, iconR.y);
            }
        }


        // Draw the Text
        if (text != null) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (v != null) {
                v.paint(g, textR);
            } else {
                paintText(g, b, textR, text);
                if (b.hasFocus() && b.isFocusPainted()
                        && textR.width > 0 && textR.height > 0) {
                    paintFocus(g, textR, size);
                }
            }
        }
        QuaquaUtilities.endGraphics((Graphics2D) g, oldHints);
        Debug.paint(g, c, this);
    }

    @Override
    protected void paintFocus(Graphics g, Rectangle textRect, Dimension size) {
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
        int mnemonicIndex = Methods.invokeGetter(b, "getDisplayedMnemonicIndex", -1);
        String style = (String) b.getClientProperty("Quaqua.Label.style");

        /* Draw the Text */
        if (style != null && style.equals("shadow") && model.isEnabled()) {
            g.setColor(UIManager.getColor("Label.shadowForeground"));
            QuaquaUtilities.drawStringUnderlineCharAt(g, text, mnemonicIndex,
                    textRect.x + getTextShiftOffset(),
                    textRect.y + fm.getAscent() + getTextShiftOffset() + 1);
        }
        if (model.isEnabled()) {
            /*** paint the text normally */
            g.setColor(b.getForeground());
        } else {
            Color c = UIManager.getColor("CheckBox.disabledForeground");
            g.setColor((c != null) ? c : b.getForeground());
        }
        QuaquaUtilities.drawStringUnderlineCharAt(g, text, mnemonicIndex,
                textRect.x + getTextShiftOffset(),
                textRect.y + fm.getAscent() + getTextShiftOffset());
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTabbedPaneUI.
     */
    public static class PropertyChangeHandler implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            AbstractButton src = (AbstractButton) evt.getSource();
            if (name.equals("Frame.active") && src.isSelected()) {
                src.repaint();
            } else if (name.equals("JComponent.sizeVariant")) {
                QuaquaUtilities.applySizeVariant(src);

            }
        }
    }

    /**
     * The preferred size of the radio button
     */
    @Override
    public Dimension getPreferredSize(JComponent c) {
        if (c.getComponentCount() > 0) {
            return null;
        }

        AbstractButton b = (AbstractButton) c;

        String text = b.getText();

        Icon buttonIcon = (Icon) b.getIcon();
        if (buttonIcon == null) {
            buttonIcon = getDefaultIcon(c);
        }

        Font font = b.getFont();
        FontMetrics fm = c.getFontMetrics(font);

        viewR.x = viewR.y = 0;
        viewR.width = Short.MAX_VALUE;
        viewR.height = Short.MAX_VALUE;
        iconR.x = iconR.y = iconR.width = iconR.height = 0;
        textR.x = textR.y = textR.width = textR.height = 0;

        SwingUtilities.layoutCompoundLabel(
                c, fm, text, buttonIcon,
                b.getVerticalAlignment(), b.getHorizontalAlignment(),
                b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                viewR, iconR, textR,
                text == null ? 0 : Methods.invokeGetter(b, "getIconTextGap", 4));

        // find the union of the icon and text rects (from Rectangle.java)
        int x1 = Math.min(iconR.x, textR.x);
        int x2 = Math.max(iconR.x + iconR.width,
                textR.x + textR.width);
        int y1 = Math.min(iconR.y, textR.y);
        int y2 = Math.max(iconR.y + iconR.height,
                textR.y + textR.height);
        int width = x2 - x1;
        int height = y2 - y1;

        viewInsets = getInsets(b, viewInsets);
        width += viewInsets.left + viewInsets.right;
        height += viewInsets.top + viewInsets.bottom;
        return new Dimension(width, height);
    }

    /**
     * Workaround for Matisse GUI builder.
     */
    private Insets getInsets(AbstractButton b, Insets i) {
        i = b.getInsets(i);
        if (UIManager.getBoolean("CheckBox.enforceVisualMargin")) {
            Insets vmargin = UIManager.getInsets("Component.visualMargin");
            i.left = Math.max(i.left, vmargin.left);
            i.top = Math.max(i.top, vmargin.top);
            i.bottom = Math.max(i.bottom, vmargin.bottom);
            i.right = Math.max(i.right, vmargin.right);
        }
        return i;
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
        return QuaquaUtilities.layoutCompoundLabel(
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
        Rectangle rect = new Rectangle(0, 0, width, height);
        if (type == VisuallyLayoutable.CLIP_BOUNDS) {
            return rect;
        }

        AbstractButton b = (AbstractButton) c;
        String text = b.getText();
        Icon icon = (b.isEnabled()) ? b.getIcon() : b.getDisabledIcon();
        if (icon == null) {
            icon = getDefaultIcon(b);
        }

        if ((icon == null) && (text == null)) {
            return rect;
        }

        FontMetrics fm = c.getFontMetrics(c.getFont());
        Insets insets = getInsets(b, viewInsets);

        viewR.x = insets.left;
        viewR.y = insets.top;
        viewR.width = width - (insets.left + insets.right);
        viewR.height = height - (insets.top + insets.bottom);

        iconR.x = iconR.y = iconR.width = iconR.height = 0;
        textR.x = textR.y = textR.width = textR.height = 0;

        String clippedText =
                layoutCL(b, fm, text, icon, viewR, iconR, textR);

        Rectangle textBounds = Fonts.getPerceivedBounds(text, c.getFont(), c);
        int ascent = fm.getAscent();
        textR.y += ascent + textBounds.y;
        textR.height = textBounds.height;

        // Determine rect rectangle
        switch (type) {
            case VisuallyLayoutable.COMPONENT_BOUNDS:
                rect = textR.union(iconR);
                break;
            case VisuallyLayoutable.TEXT_BOUNDS:
                rect.setBounds(textR);
                break;
        }

        return rect;
    }
}
