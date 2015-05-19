/*
 * @(#)QuaquaMenuPainter.java  
 *
 * Copyright (c) 2009-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.View;

/**
 * QuaquaMenuPainter.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaMenuPainter.java 464 2014-03-22 12:32:00Z wrandelshofer $
 */
public class QuaquaMenuPainter {

    /**
     * Shared Instance.
     */
    private static QuaquaMenuPainter instance;

    public static QuaquaMenuPainter getInstance() {
        if (instance == null) {
            instance = new QuaquaMenuPainter();
        }
        return instance;
    }
    final static int defaultMenuItemGap = 2;
    final static int kAcceleratorArrowSpace = 0;
    final static int kAcceleratorArrowMargin = 20;
    final static Rectangle zeroRect = new Rectangle(0, 0, 0, 0);
    final static Rectangle iconRect = new Rectangle();
    final static Rectangle textRect = new Rectangle();
    final static Rectangle acceleratorRect = new Rectangle();
    final static Rectangle checkIconRect = new Rectangle();
    final static Rectangle arrowIconRect = new Rectangle();
    final static Rectangle viewRect = new Rectangle(32767, 32767);
    static Rectangle r = new Rectangle();
    private PlaceholderIcon placeholderIcon = new PlaceholderIcon();

    private static class PlaceholderIcon implements Icon {

        public Dimension dim;
        public Container parent;

        public int getIconHeight() {
            return dim.height;
        }

        public int getIconWidth() {
            return dim.width;
        }

        public void paintIcon(java.awt.Component component, java.awt.Graphics graphics, int param, int param3) {
            // do nothing
        }
    };

    private void resetRects() {
        iconRect.setBounds(zeroRect);
        textRect.setBounds(zeroRect);
        acceleratorRect.setBounds(zeroRect);
        checkIconRect.setBounds(zeroRect);
        arrowIconRect.setBounds(zeroRect);
        viewRect.setBounds(0, 0, 32767, 32767);
        r.setBounds(zeroRect);
    }

    private Dimension getMinimumIconSize(Container parent) {
        Dimension d = new Dimension();
        Component[] c = parent.getComponents();
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) c[i];
                Icon icon = b.getIcon();
                if (icon != null) {
                    d.width = Math.max(d.width, icon.getIconWidth());
                    d.height = Math.max(d.height, icon.getIconHeight());
                }
            }
        }
        return d;
    }

    protected void paintMenuItem(QuaquaMenuPainterClient client, Graphics g,
            JComponent c, Icon checkIcon, Icon arrowIcon, Color background,
            Color foreground, Color disabledForeground, Color selectionForeground,
            int textIconGap, Font acceleratorFont) {
        Object oldHints = QuaquaUtilities.beginGraphics((Graphics2D) g);


        JMenuItem menuItem = (JMenuItem) c;
        ButtonModel buttonModel = menuItem.getModel();
        int width = menuItem.getWidth();
        int height = menuItem.getHeight();
        Insets insets = c.getInsets();
        resetRects();

        viewRect.setBounds(0, 0, width, height);
        viewRect.x += insets.left;
        viewRect.y += insets.top;
        viewRect.width -= insets.right + viewRect.x;
        viewRect.height -= insets.bottom + viewRect.y;

        Font savedFont = g.getFont();
        Color savedColor = g.getColor();

        Font textFont = c.getFont();
        g.setFont(textFont);
        FontMetrics textFM = g.getFontMetrics(textFont);
        FontMetrics acceleratorFM = g.getFontMetrics(acceleratorFont);
        if (c.isOpaque()) {
            client.paintBackground(g, c, width, height);
        }

        KeyStroke accelerator = menuItem.getAccelerator();
        String modifiersText = "";
        String acceleratorKeyText = "";
        boolean isLeftToRight = QuaquaUtilities.isLeftToRight(c);
        if (accelerator != null) {
            // Beware: In Java 1.4, Method getModifiers() has been moved from class
            // KeyStroke to the new superclass AWTKeyStroke. This superclass
            // did not exist in the Java 1.3 API. Thus we have to take care
            // when we compile this class, that we do so against the 1.3 API.
            int modifiers = accelerator.getModifiers();
            if (modifiers > 0) {
                modifiersText = QuaquaUtilities.getKeyModifiersText(modifiers, isLeftToRight);
            }
            acceleratorKeyText = getAcceleratorKeyText(accelerator);
        }
        String clippedText = layoutMenuItem(menuItem, textFM, menuItem.getText(),
                acceleratorFM, acceleratorKeyText, modifiersText,
                menuItem.getIcon(), checkIcon, arrowIcon,
                menuItem.getVerticalAlignment(),
                menuItem.getHorizontalAlignment(),
                menuItem.getVerticalTextPosition(),
                menuItem.getHorizontalTextPosition(), viewRect,
                iconRect, textRect, acceleratorRect,
                checkIconRect, arrowIconRect,
                menuItem.getText() == null ? 0 : textIconGap, textIconGap);

        java.awt.Container container;
        for (container = menuItem.getParent();
                container != null && !(container instanceof JPopupMenu);
                container = container.getParent()) {
            // empty for-loop body
        }

        boolean isEnabled = (buttonModel.isEnabled()
                && (container == null || container.isVisible()));

        if (menuItem.getIcon() != null) {
            paintIcon(g, c, iconRect, isEnabled);
        }

        boolean isSelected = false;
        if (!isEnabled) {
            g.setColor(disabledForeground);
        } else if (buttonModel.isArmed()
                || c instanceof JMenu && buttonModel.isSelected()) {
            g.setColor(selectionForeground);
            isSelected = true;
        } else {
            g.setColor(menuItem.getForeground());
        }

        if (checkIcon != null) {
            paintCheck(g, c, checkIcon);
        }

        if (acceleratorKeyText != null && !acceleratorKeyText.equals("")) {
            int baseline = acceleratorRect.y + textFM.getAscent();
            if (modifiersText.equals("")) {
                g.drawString(acceleratorKeyText, acceleratorRect.x, baseline);
            } else {
                int modifiers = accelerator.getModifiers();

                // To give a visual queue for accelerators which use the
                // java.awt.event.InputEvent.ALT_GRAPH_MASK as modifier,
                // we underline the option key.
                // We do this using the underline mnemonic feature of our
                // drawString method further below.
                int mnemonicChar = 0;
                if ((modifiers & InputEvent.ALT_GRAPH_MASK) > 0) {
                    mnemonicChar = '\u2325'; // Unicode: OPTION KEY
                }
                int acceleratorKeyWidth = Math.max(
                        textFM.charWidth('M'),
                        SwingUtilities.computeStringWidth(textFM, acceleratorKeyText));
                if (isLeftToRight) {
                    g.setFont(acceleratorFont);
                    drawString(client, g, modifiersText, mnemonicChar,
                            acceleratorRect.x, baseline, isEnabled, isSelected);
                    g.setFont(textFont);
                    g.drawString(acceleratorKeyText,
                            (acceleratorRect.x
                            + acceleratorRect.width
                            - kAcceleratorArrowSpace - acceleratorKeyWidth),
                            baseline);
                } else {
                    int x = acceleratorRect.x + kAcceleratorArrowSpace + acceleratorKeyWidth;
                    g.setFont(acceleratorFont);
                    drawString(
                            client, g, modifiersText, mnemonicChar,
                            x, baseline, isEnabled, isSelected);

                    g.setFont(textFont);
                    g.drawString(
                            acceleratorKeyText,
                            x - textFM.stringWidth(acceleratorKeyText),
                            baseline);
                }
            }
        }
        if (clippedText != null && !clippedText.equals("")) {
            View view = (View) c.getClientProperty("html");
            if (view != null) {
                view.paint(g, textRect);
            } else {
                drawString(
                        client, g, clippedText,
                        buttonModel.getMnemonic(), textRect.x,
                        textRect.y + textFM.getAscent(), isEnabled,
                        isSelected);
            }
        }
        if (arrowIcon != null) {
            if (buttonModel.isArmed()
                    || c instanceof JMenu && buttonModel.isSelected()) {
                g.setColor(foreground);
            }
            if (useCheckAndArrow(menuItem)) {
                arrowIcon.paintIcon(c, g, arrowIconRect.x,
                        arrowIconRect.y);
            }
        }

        g.setColor(savedColor);
        g.setFont(savedFont);
        QuaquaUtilities.endGraphics((Graphics2D) g, oldHints);
    }

    protected Dimension getPreferredMenuItemSize(
            JComponent c, Icon checkIcon, Icon arrowIcon, int textIconGap, Font acceleratorFont) {
        JMenuItem menuItem = (JMenuItem) c;
        Icon icon = menuItem.getIcon();
        String text = menuItem.getText();
        KeyStroke accelerator = menuItem.getAccelerator();
        String acceleratorKeyText = "";
        String modifiersText = "";
        if (accelerator != null) {
            int modifiers = accelerator.getModifiers();
            if (modifiers > 0) {
                modifiersText = QuaquaUtilities.getKeyModifiersText(modifiers, true);
            }
            acceleratorKeyText = getAcceleratorKeyText(accelerator);
        }
        Font textFont = menuItem.getFont();
        FontMetrics textFM = menuItem.getFontMetrics(textFont);
        FontMetrics acceleratorFM = (menuItem.getFontMetrics(acceleratorFont));

        resetRects();

        layoutMenuItem(
                menuItem, textFM, text, acceleratorFM,
                acceleratorKeyText, modifiersText, icon, checkIcon, arrowIcon,
                menuItem.getVerticalAlignment(),
                menuItem.getHorizontalAlignment(),
                menuItem.getVerticalTextPosition(),
                menuItem.getHorizontalTextPosition(), viewRect,
                iconRect, textRect, acceleratorRect, checkIconRect,
                arrowIconRect, text == null ? 0 : textIconGap, textIconGap);

        r.setBounds(textRect);
        r = SwingUtilities.computeUnion(iconRect.x, iconRect.y, iconRect.width,
                iconRect.height, r);

        boolean hasNoAccelerator = acceleratorKeyText == null || acceleratorKeyText.equals("");
        if (!hasNoAccelerator) {
            r.width += acceleratorRect.width + kAcceleratorArrowMargin;
        }

        boolean isUseCheckAndArrow = useCheckAndArrow(menuItem);
        if (isUseCheckAndArrow) {
            r.width += checkIconRect.width;
            r.width += textIconGap;

            // We do not add extra space for the arrow icon, because we
            // want it to share its position with the accelerator.
            if (arrowIconRect.width + kAcceleratorArrowMargin > acceleratorRect.width) {
                r.width += arrowIconRect.width + kAcceleratorArrowMargin - acceleratorRect.width;
            }
            //r.width += textIconGap;
            //r.width += arrowIconRect.width;
        }

        Insets insets = menuItem.getInsets();
        if (insets != null) {
            r.width += insets.left + insets.right;
            r.height += insets.top + insets.bottom;
        }

        Insets margin;
        if (isUseCheckAndArrow) {
            insets = UIManager.getInsets("Menu.margin");
        } else {
            insets = UIManager.getInsets("MenuBar.margin");
        }
        if (insets != null) {
            r.width += insets.left + insets.right;
            r.height += insets.top + insets.bottom;
        }

        //r.width += 4 + textIconGap;
        //r.height = Math.max(r.height, 16);
        return r.getSize();
    }
    /* */

    protected void paintCheck(Graphics g, JComponent c,
            Icon checkIcon) {
        if (useCheckAndArrow((JMenuItem) c)) {
            checkIcon.paintIcon(c, g, checkIconRect.x,
                    checkIconRect.y);
        }
    }

    protected void paintIcon(Graphics g, JComponent c,
            Rectangle rectangle, boolean isEnabled) {
        AbstractButton abstractButton = (AbstractButton) c;
        ButtonModel buttonModel = abstractButton.getModel();
        Icon icon;
        if (!isEnabled) {
            icon = abstractButton.getDisabledIcon();
        } else if (buttonModel.isPressed() && buttonModel.isArmed()) {
            icon = abstractButton.getPressedIcon();
            if (icon == null) {
                icon = abstractButton.getIcon();
            }
        } else {
            icon = abstractButton.getIcon();
        }
        if (icon != null) {
            icon.paintIcon(c, g, rectangle.x, rectangle.y);
        }
    }

    
    public void drawString(QuaquaMenuPainterClient client,
            Graphics g, String text, int mnemonicChar, int x,
            int y, boolean isEnabled, boolean isSelected) {
        int mnemonicPos = -1;
        if (mnemonicChar != 0) {
            char mnemonicUpperCase = Character.toUpperCase((char) mnemonicChar);
            char mnemonicLowerCase = Character.toLowerCase((char) mnemonicChar);
            int upperCasePos = text.indexOf(mnemonicUpperCase);
            int lowerCasePos = text.indexOf(mnemonicLowerCase);
            if (upperCasePos == -1) {
                mnemonicPos = lowerCasePos;
            } else if (lowerCasePos == -1) {
                mnemonicPos = upperCasePos;
            } else {
                mnemonicPos = (lowerCasePos < upperCasePos) ? lowerCasePos : upperCasePos;
            }
        }
        g.drawString(text, x, y);
        if (mnemonicPos != -1) {
            FontMetrics fm = g.getFontMetrics();
            int underlineX = x + fm.stringWidth(text.substring(0, mnemonicPos));
            int underlineY = y;
            int underlineWidth = fm.charWidth(text.charAt(mnemonicPos));
            int underlineHeight = 1;
            g.fillRect(underlineX, underlineY + fm.getDescent() - 1,
                    underlineWidth, underlineHeight);
        }
    }

    /*
     */
    private boolean useCheckAndArrow(JMenuItem menuItem) {
        return !(menuItem instanceof JMenu && ((JMenu) menuItem).isTopLevelMenu());
    }

    /**
     * Layouts the components of the menu item.
     */
    private String layoutMenuItem(
            JMenuItem menuItem,
            FontMetrics textFM, String text,
            FontMetrics acceleratorFM, String acceleratorKeyText, String modifiersText,
            Icon icon, Icon checkIcon, Icon arrowIcon,
            int verticalAlignment, int horizontalAlignment,
            int verticalTextPosition, int horizontalTextPosition,
            Rectangle viewRect, Rectangle iconRect,
            Rectangle textRect, Rectangle acceleratorRect,
            Rectangle checkIconRect, Rectangle arrowIconRect,
            int textIconGap, int textCheckIconGap) {
        if (menuItem.getParent() != null) {
            placeholderIcon.dim = getMinimumIconSize((Container) menuItem.getParent());
            icon = (placeholderIcon.getIconWidth() == 0) ? null : placeholderIcon;
        }


        SwingUtilities.layoutCompoundLabel(
                menuItem, textFM, text,
                icon, SwingConstants.TOP, SwingConstants.LEFT,
                verticalTextPosition, horizontalTextPosition,
                viewRect, iconRect, textRect,
                textIconGap);


        boolean hasNoAccelerator = acceleratorKeyText == null || acceleratorKeyText.equals("");
        if (hasNoAccelerator) {
            acceleratorRect.width = acceleratorRect.height = 0;
            acceleratorKeyText = "";
        } else {
            acceleratorRect.width = (SwingUtilities.computeStringWidth(acceleratorFM, modifiersText)
                    + kAcceleratorArrowSpace);

            acceleratorRect.width += Math.max(
                    textFM.charWidth('M'),
                    SwingUtilities.computeStringWidth(textFM, acceleratorKeyText));
            acceleratorRect.height = acceleratorFM.getHeight();
        }

        boolean isUseCheckAndArrow = useCheckAndArrow(menuItem);
        if (isUseCheckAndArrow) {
            if (checkIcon != null) {
                checkIconRect.width = checkIcon.getIconWidth();
                checkIconRect.height = checkIcon.getIconHeight();
                textRect.x += checkIconRect.width + textCheckIconGap;
                iconRect.x += checkIconRect.width + textCheckIconGap;
            } else {
                checkIconRect.width = checkIconRect.height = 0;
            }
            if (arrowIcon != null) {
                arrowIconRect.width = arrowIcon.getIconWidth();
                arrowIconRect.height = arrowIcon.getIconHeight();
            } else {
                arrowIconRect.width = arrowIconRect.height = 0;
            }
        }
        Rectangle labelRect = iconRect.union(textRect);

        // Accelerator and arrow icon share the same location
        // -> We assume that a menu item never uses both at the same time.
        //acceleratorRect.x += viewRect.width - arrowIconRect.width - acceleratorRect.width;
        acceleratorRect.x = viewRect.width - acceleratorRect.width;// - kAcceleratorArrowMargin;
        //acceleratorRect.y = viewRect.y + viewRect.height / 2 - acceleratorRect.height / 2;
        acceleratorRect.y = textRect.y;
        if (isUseCheckAndArrow) {
            //arrowIconRect.x = acceleratorRect.x + acceleratorRect.width - arrowIconRect.width;
            arrowIconRect.x = viewRect.width - arrowIconRect.width;
            arrowIconRect.y = (viewRect.y + labelRect.height / 2 - arrowIconRect.height / 2);
            checkIconRect.y = (viewRect.y + labelRect.height / 2 - checkIconRect.height / 2);
            //checkIconRect.x = 0;
        }

        if (!QuaquaUtilities.isLeftToRight(menuItem)) {
            int width = viewRect.width;
            checkIconRect.x = width - (checkIconRect.x + checkIconRect.width);
            iconRect.x = width - (iconRect.x + iconRect.width);
            textRect.x = width - (textRect.x + textRect.width);
            acceleratorRect.x = width - (acceleratorRect.x + acceleratorRect.width);
            arrowIconRect.x = width - (arrowIconRect.x + arrowIconRect.width);
        }

        Insets margin;
        if (isUseCheckAndArrow) {
            margin = UIManager.getInsets("Menu.margin");
        } else {
            margin = UIManager.getInsets("MenuBar.margin");
        }
        if (margin != null) {
            checkIconRect.x += margin.left;
            checkIconRect.y += margin.top;
            iconRect.x += margin.left;
            iconRect.y += margin.top;
            textRect.x += margin.left;
            textRect.y += margin.top;
            acceleratorRect.x -= margin.right;
            acceleratorRect.y += margin.top;
            arrowIconRect.x -= margin.right;
            arrowIconRect.y += margin.top;
        }
        return text;
    }

    private String getAcceleratorKeyText(KeyStroke accelerator) {
        StringBuffer buf = new StringBuffer();
        if (accelerator != null) {
            int keyCode = accelerator.getKeyCode();
            if (keyCode != 0) {
                switch (keyCode) {
                    case KeyEvent.VK_ENTER:
                        //buf.append('\u2305'); // Unicode: PROJECTIVE
                        //buf.append('\u23ce'); // Unicode: RETURN SYMBOL
                        buf.append('\u21a9'); // Unicode: LEFTWARDS ARROW WITH HOOK
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        buf.append('\u232b'); // Unicode: ERASE TO THE LEFT
                        break;
                    case KeyEvent.VK_DELETE:
                        buf.append('\u2326'); // Unicode: ERASE TO THE RIGHT
                        break;
                    case KeyEvent.VK_UP:
                        buf.append('\u2191'); // Unicode: UPWARDS ARROW
                        break;
                    case KeyEvent.VK_DOWN:
                        buf.append('\u2193'); // Unicode: DOWNWARDS ARROW
                        break;
                    case KeyEvent.VK_LEFT:
                        buf.append('\u2190'); // Unicode: LEFTWARDS ARROW
                        break;
                    case KeyEvent.VK_RIGHT:
                        buf.append('\u2192'); // Unicode: RIGHTWARDS ARROW
                        break;
                    case KeyEvent.VK_PLUS:
                        buf.append('+');
                        break;
                    case KeyEvent.VK_MINUS:
                        buf.append('-');
                        break;
                    default:
                        buf.append(KeyEvent.getKeyText(keyCode));
                        break;
                }
            } else {
                buf.append(accelerator.getKeyChar());
            }
        }
        return buf.toString();
    }
}

