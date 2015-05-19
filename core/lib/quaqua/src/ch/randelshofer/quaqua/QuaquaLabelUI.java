/*
 * @(#)QuaquaLabelUI.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.border.BackgroundBorder;
import ch.randelshofer.quaqua.color.InactivatableColorUIResource;
import ch.randelshofer.quaqua.util.Debug;
import ch.randelshofer.quaqua.color.PaintableColor;
import java.awt.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

/**
 * QuaquaLabelUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaLabelUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaLabelUI extends BasicLabelUI implements VisuallyLayoutable {

    protected final static QuaquaLabelUI labelUI = new QuaquaLabelUI();
    /* These rectangles/insets are allocated once for this shared LabelUI
     * implementation.  Re-using rectangles rather than allocating
     * them in each getPreferredSize call sped up the method substantially.
     */
    private static Rectangle iconR = new Rectangle();
    private static Rectangle textR = new Rectangle();
    private static Rectangle viewR = new Rectangle();
    private static Insets viewInsets = new Insets(0, 0, 0, 0);

    /**
     * Preferred spacing between labels and other components.
     * Pixels from colon and associated controls (RadioButton,
     * CheckBox)
     * /
     * private final static Insets associatedRegularSpacing = new Insets(8,8,8,8);
     * private final static Insets associatedSmallSpacing = new Insets(6,6,6,6);
     * private final static Insets associatedMiniSpacing = new Insets(5,5,5,5);
     */
    public static ComponentUI createUI(JComponent c) {
        return labelUI;
    }

    @Override
    protected void installDefaults(JLabel b) {
        super.installDefaults(b);

        // load shared instance defaults
        LookAndFeel.installBorder(b, "Label.border");

        // FIXME - Very, very dirty trick to achieve small labels on sliders
        //         This hack should be removed, when we implement a SliderUI
        //         on our own.
        if (b.getClass().getName().endsWith("LabelUIResource")) {
            b.setFont(UIManager.getFont("Slider.labelFont"));
        }
        
        QuaquaUtilities.applySizeVariant(b);
    }

    @Override
    public void paint(Graphics gr, JComponent c) {
        Graphics2D g = (Graphics2D) gr;
        Object oldHints = QuaquaUtilities.beginGraphics(g);

        // Paint background again so that the texture paint is drawn
        if (c.isOpaque()) {
            g.setPaint(PaintableColor.getPaint(c.getBackground(), c));
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }

        // Paint background border
        Border b = c.getBorder();
        if (b != null && b instanceof BackgroundBorder) {
            ((BackgroundBorder) b).getBackgroundBorder().paintBorder(c, g, 0, 0, c.getWidth(), c.getHeight());
        }

        super.paint(g, c);
        QuaquaUtilities.endGraphics(g, oldHints);
        Debug.paint(g, c, this);
    }

    /**
     * Paint label with disabled text color.
     *
     * @see #paint
     * @see #paintEnabledText
     */
    @Override
    protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY) {
        // Make sure we render with the right drawing properties and make sure
        // we can edit them by client properties
        Font font = l.getFont();
        Color foreground = UIManager.getColor("Label.disabledForeground");
        int accChar = -1; //l.getDisplayedMnemonicIndex();

        String style = (String) l.getClientProperty("Quaqua.Label.style");
        if (style != null) {
            boolean selected = style.endsWith("Selected");

            if (style.startsWith("category")) {

                s = s.toUpperCase();
                font = UIManager.getFont("Tree.sideBarCategory.font");
                style = (selected) ? "shadow" : "emboss";
                foreground = UIManager.getColor(selected ? "Tree.sideBarCategory.selectionForeground" : "Tree.sideBarCategory.foreground");

            } else if (style.startsWith("row")) {

                font = selected ? UIManager.getFont("Tree.sideBar.selectionFont") : UIManager.getFont("Tree.sideBar.font");

                // Preserve font style attributes as long as they don't interfere
                // with the font style of the sidebar.
                if (selected) {
                    font = font.deriveFont(l.getFont().getStyle() | font.getStyle());
                } else {
                    font = font.deriveFont(l.getFont().getStyle());
                }

                style = selected ? "shadow" : null;
            }

            if (style != null && style.equals("emboss")) {
                g.setFont(font);
                g.setColor(UIManager.getColor("Label.embossForeground"));
                QuaquaUtilities.drawString(g, s, accChar, textX, textY + 1);
            } else if (style != null && style.equals("shadow")) {
                g.setFont(font);
                g.setColor(UIManager.getColor("Label.shadowForeground"));
                QuaquaUtilities.drawString(g, s, accChar, textX, textY + 1);
            }
        }

        g.setFont(font);
        g.setColor(foreground);
        QuaquaUtilities.drawString(g, s, accChar,
                textX, textY);
    }

    @Override
    protected void paintEnabledText(JLabel l, Graphics g, String s, int textX, int textY) {
        int mnemIndex = l.getDisplayedMnemonicIndex();

        // Make sure we render with the right drawing properties and make sure
        // we can edit them by client properties
        Font font = l.getFont();
        Color foreground = l.getForeground();

        String style = (String) l.getClientProperty("Quaqua.Label.style");
        if (style != null) {
            boolean selected = style.endsWith("Selected");
            boolean active = style.indexOf("Inactive") == -1;

            if (style.startsWith("category")) {

                s = s.toUpperCase();
                font = UIManager.getFont(selected ? "Tree.sideBarCategory.selectionFont" : "Tree.sideBarCategory.font");
                foreground = UIManager.getColor(selected ? "Tree.sideBarCategory.selectionForeground" : "Tree.sideBarCategory.foreground");
                if (foreground instanceof InactivatableColorUIResource) {
                    ((InactivatableColorUIResource) foreground).setActive(active);
                }
                style = (selected) ? "shadow" : "emboss";

            } else if (style.startsWith("row")) {
                font = selected ? UIManager.getFont("Tree.sideBar.selectionFont") : UIManager.getFont("Tree.sideBar.font");
                if (font == null) {
                    font = l.getFont();
                }

                // Preserve font style attributes as long as they don't interfere
                // with the font style of the sidebar.
                if (selected) {
                    font = font.deriveFont(l.getFont().getStyle() & Font.ITALIC | font.getStyle());
                } else {
                    font = font.deriveFont(l.getFont().getStyle() & Font.ITALIC | font.getStyle());
                }


                foreground = UIManager.getColor(selected ? "Tree.sideBar.selectionForeground" : "Tree.sideBar.foreground");
                if (foreground instanceof InactivatableColorUIResource) {
                    ((InactivatableColorUIResource) foreground).setActive(active);
                }
                style = (selected) ? "shadow" : null;
            }
            if (style != null && style.equals("emboss")) {
                g.setFont(font);
                g.setColor(UIManager.getColor("Label.embossForeground"));
                QuaquaUtilities.drawString(g, s, mnemIndex, textX, textY + 1);
            } else if (style != null && style.equals("shadow")) {
                g.setFont(font);
                g.setColor(UIManager.getColor("Label.shadowForeground"));
                QuaquaUtilities.drawString(g, s, mnemIndex, textX, textY + 1);
            }
        }

        g.setFont(font);
        g.setColor(foreground);
        QuaquaUtilities.drawString(g, s, mnemIndex, textX, textY);
    }

    /**
     * Forwards the call to SwingUtilities.layoutCompoundLabel().
     * This method is here so that a subclass could do Label specific
     * layout and to shorten the method name a little.
     *
     * @see SwingUtilities#layoutCompoundLabel
     */
    @Override
    protected String layoutCL(
            JLabel label,
            FontMetrics fontMetrics,
            String text,
            Icon icon,
            Rectangle viewR,
            Rectangle iconR,
            Rectangle textR) {
        return SwingUtilities.layoutCompoundLabel(
                (JComponent) label,
                fontMetrics,
                text,
                icon,
                label.getVerticalAlignment(),
                label.getHorizontalAlignment(),
                label.getVerticalTextPosition(),
                label.getHorizontalTextPosition(),
                viewR,
                iconR,
                textR,
                label.getIconTextGap());
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

        JLabel b = (JLabel) c;
        String text = b.getText();
        boolean isEmpty = (text == null || text.length() == 0);
        if (isEmpty) {
            text = " ";
        }
        Icon icon = (b.isEnabled()) ? b.getIcon() : b.getDisabledIcon();

        Font f = c.getFont();
        FontMetrics fm = c.getFontMetrics(f);
        Insets insets = c.getInsets(viewInsets);

        viewR.x = insets.left;
        viewR.y = insets.top;
        viewR.width = width - (insets.left + insets.right);
        viewR.height = height - (insets.top + insets.bottom);

        iconR.x = iconR.y = iconR.width = iconR.height = 0;
        textR.x = textR.y = textR.width = textR.height = 0;

        String clippedText =
                layoutCL(b, fm, text, icon, viewR, iconR, textR);

        Rectangle textBounds = Fonts.getPerceivedBounds(text, f, c);
        if (isEmpty) {
            textBounds.width = 0;
        }
        int ascent = fm.getAscent();
        textR.x += textBounds.x;
        textR.width = textBounds.width;
        textR.y += ascent + textBounds.y;
        textR.height -= fm.getHeight() - textBounds.height;

        // Determine text rectangle
        switch (type) {
            case VisuallyLayoutable.COMPONENT_BOUNDS:
                if (icon != null) {
                    rect = textR.union(iconR);
                } else {
                    rect.setBounds(textR);
                }
                break;
            case VisuallyLayoutable.TEXT_BOUNDS:
                if (text == null) {
                    return rect;
                }
                rect.setBounds(textR);
                break;
        }

        return rect;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();

        if (name.equals("JComponent.sizeVariant")) {
            QuaquaUtilities.applySizeVariant((JLabel) evt.getSource());
        } else if (name.equals("Quaqua.Label.style")) {
            QuaquaUtilities.applySizeVariant((JLabel) evt.getSource());
        } else {
            super.propertyChange(evt);
        }
    }
}
