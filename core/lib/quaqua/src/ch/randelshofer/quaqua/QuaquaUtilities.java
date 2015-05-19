/*
 * @(#)QuaquaUtilities.java  
 *
 * Copyright (c) 2003-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.*;
import java.awt.image.*;
import java.lang.reflect.Method;
import java.net.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Utility class for the Quaqua LAF.
 *
 * @author Werner Randelshofer, Switzerland
 * @version $Id: QuaquaUtilities.java 464 2014-03-22 12:32:00Z wrandelshofer $
 */
public class QuaquaUtilities extends BasicGraphicsUtils implements SwingConstants {

    public enum SizeVariant {

        LARGE(3),REGULAR(2), SMALL(1), MINI(0);
        /** id establishes an ordering between the sizes. */
        int id;
        SizeVariant(int id) {
        this.id=id;
    }
        public int getId(){return id;}
    }
    private final static boolean DEBUG = false;
    /** Holds the class name of SwingUtilities2 once it has been resolved. */
    private static String swingUtilities2;

    /** Prevent instance creation. */
    private QuaquaUtilities() {
    }

    /*
     * Convenience function for determining ComponentOrientation.  Helps us
     * avoid having Munge directives throughout the code.
     */
    public static boolean isLeftToRight(Component c) {
        return c.getComponentOrientation().isLeftToRight();
    }

    /**
     * Draw a string with the graphics {@code g} at location
     * ({@code x}, {@code y})
     * just like {@code g.drawString} would.
     * The character at index {@code underlinedIndex}
     * in text will be underlined. If {@code index} is beyond the
     * bounds of {@code text} (including &lt; 0), nothing will be
     * underlined.
     *
     * @param g Graphics to draw with
     * @param text String to draw
     * @param underlinedIndex Index of character in text to underline
     * @param x x coordinate to draw at
     * @param y y coordinate to draw at
     * @since 1.4
     */
    public static void drawStringUnderlineCharAt(Graphics g, String text,
            int underlinedIndex, int x, int y) {
        g.drawString(text, x, y);
        if (underlinedIndex >= 0 && underlinedIndex < text.length()) {
            FontMetrics fm = g.getFontMetrics();
            int underlineRectX = x + fm.stringWidth(text.substring(0, underlinedIndex));
            int underlineRectY = y;
            int underlineRectWidth = fm.charWidth(text.charAt(underlinedIndex));
            int underlineRectHeight = 1;
            g.fillRect(underlineRectX, underlineRectY + fm.getDescent() - 1,
                    underlineRectWidth, underlineRectHeight);
        }
    }

    /**
     * Returns index of the first occurrence of {@code mnemonic}
     * within string {@code text}. Matching algorithm is not
     * case-sensitive.
     *
     * @param text The text to search through, may be null
     * @param mnemonic The mnemonic to find the character for.
     * @return index into the string if exists, otherwise -1
     */
    static int findDisplayedMnemonicIndex(String text, int mnemonic) {
        if (text == null || mnemonic == '\0') {
            return -1;
        }

        char uc = Character.toUpperCase((char) mnemonic);
        char lc = Character.toLowerCase((char) mnemonic);

        int uci = text.indexOf(uc);
        int lci = text.indexOf(lc);

        if (uci == -1) {
            return lci;
        } else if (lci == -1) {
            return uci;
        } else {
            return (lci < uci) ? lci : uci;
        }
    }

    /**
     * Returns true if the component is on a Dialog or a Frame, which is active,
     * or if it is on a Window, which is focused.
     * Always returns true, if the component has no parent window.
     */
    public static boolean isOnActiveWindow(Component c) {
        return isOnActiveWindow(c, false);
    }

    /**
     * Returns true if the component is on a Dialog or a Frame, which is active,
     * or if it is on a Window, which is focused.
     * Always returns true, if the component has no parent window.
     * <p>
     * @param c The component.
     * @param isActiveWhenSheetIsActive Set this to true, when the window should
     * be considered as active when its sheet dialog is active.
     */
    public static boolean isOnActiveWindow(Component c, boolean isActiveWhenSheetIsActive) {
        // In the RootPaneUI, we set a client property on the whole component
        // tree, if the ancestor Frame gets activated or deactivated.
        if (c instanceof JComponent) {
            Boolean value = (Boolean) ((JComponent) c).getClientProperty("Frame.active");
            // Unfortunately, the value is not always reliable.
            // Therefore we can only do a short circuit, if the value is true.
            if (value != null && value.booleanValue()) {
                return true;
                //return value.booleanValue();
            }
        }

        Window window = SwingUtilities.getWindowAncestor(c);
        boolean isOnActiveWindow;
        if (window == null) {
            isOnActiveWindow = true;
        } else if (window instanceof JWindow) {
            isOnActiveWindow = window.isActive()
                    || window.getName() == "###focusableSwingPopup###";// literal strings get interned
        } else if ((window instanceof Frame) || (window instanceof Dialog)) {
            isOnActiveWindow = window.isActive();
            if (!isOnActiveWindow && isActiveWhenSheetIsActive) {
                Window focusedWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
                isOnActiveWindow = focusedWindow != null && focusedWindow.getOwner() == window;

                // we return here, because we don't want to change the "Frame.active"
                // property.
                return isOnActiveWindow;
            }
        } else {
            if (window.getFocusableWindowState()) {
                isOnActiveWindow = window.isFocused();
            } else {
                isOnActiveWindow = true;
            }
        }

        // In case the activation property is true, we fix the value of the
        // client property, so that we can do a short circuit next time.
        if (isOnActiveWindow && (c instanceof JComponent)) {
            ((JComponent) c).putClientProperty("Frame.active", isOnActiveWindow);
        }
        return isOnActiveWindow;
    }

    /**
     * Returns a Mac OS X specific String describing the modifier key(s),
     * such as "Shift", or "Ctrl+Shift".
     *
     * @return string a text description of the combination of modifier
     *                keys that were held down during the event
     */
    public static String getKeyModifiersText(int modifiers, boolean leftToRight) {
        return getKeyModifiersUnicode(modifiers, leftToRight);
    }

    static String getKeyModifiersUnicode(int modifiers, boolean leftToRight) {
        char[] cs = new char[4];
        int count = 0;
        if (leftToRight) {
            if ((modifiers & InputEvent.CTRL_MASK) != 0) {
                cs[count++] = '\u2303';
            } // Unicode: UP ARROWHEAD

            if ((modifiers & (InputEvent.ALT_MASK | InputEvent.ALT_GRAPH_MASK)) != 0) {
                cs[count++] = '\u2325';
            } // Unicode: OPTION KEY

            if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
                cs[count++] = '\u21e7';
            } // Unicode: UPWARDS WHITE ARROW

            if ((modifiers & InputEvent.META_MASK) != 0) {
                cs[count++] = '\u2318';
            } // Unicode: PLACE OF INTEREST SIGN

        } else {
            if ((modifiers & InputEvent.META_MASK) != 0) {
                cs[count++] = '\u2318';
            } // Unicode: PLACE OF INTEREST SIGN

            if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
                cs[count++] = '\u21e7';
            } // Unicode: UPWARDS WHITE ARROW

            if ((modifiers & (InputEvent.ALT_MASK | InputEvent.ALT_GRAPH_MASK)) != 0) {
                cs[count++] = '\u2325';
            } // Unicode: OPTION KEY

            if ((modifiers & InputEvent.CTRL_MASK) != 0) {
                cs[count++] = '\u2303';
            } // Unicode: UP ARROWHEAD

        }
        return new String(cs, 0, count);
    }

    public static void repaintBorder(JComponent component) {
        JComponent c = component;
        Border border = null;
        Container container = component.getParent();
        if (container instanceof JViewport) {
            c = (JComponent) container.getParent();
            if (c != null) {
                border = c.getBorder();
            }
        }
        if (border == null) {
            border = component.getBorder();
            c = component;
        }
        if (border != null && c != null) {
            int w = c.getWidth();
            int h = c.getHeight();
            Insets insets = c.getInsets();
            c.repaint(0, 0, w, insets.top);
            c.repaint(0, insets.top, insets.left, h - insets.bottom - insets.top);
            c.repaint(0, h - insets.bottom, w, insets.bottom);
            c.repaint(w - insets.right, insets.top, insets.right, h - insets.bottom - insets.top);
        }
    }

    /** Turns on common rendering hints for UI delegates. */
    public static Object beginGraphics(Graphics2D graphics2d) {
        Object object = graphics2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        graphics2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        return object;
    }

    /** Restores rendering hints for UI delegates. */
    public static void endGraphics(Graphics2D graphics2d, Object oldHints) {
        if (oldHints != null) {
            graphics2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    oldHints);
        }
    }

    /**
     * Returns true, if the specified component is focus owner or permanent
     * focus owner and if the component is on an the active window.
     */
    public static boolean isFocused(Component component) {
        // When a component is used as a cell renderer, the focus can
        // not be determined from the component itself.
        if (component instanceof JComponent) {
            if (((JComponent) component).getClientProperty("Quaqua.Component.cellRendererFor") != null) {
                component = (Component) ((JComponent) component).getClientProperty("Quaqua.Component.cellRendererFor");
            }
        }

        //---
        try {
            boolean isFocusOwner = ((Boolean) Methods.invoke(component, "isFocusOwner")).booleanValue();

            Window ancestor = SwingUtilities.getWindowAncestor(component);
            Object kfm = Methods.invokeStatic("java.awt.KeyboardFocusManager", "getCurrentKeyboardFocusManager");

            return isFocusOwner
                    || component == Methods.invoke(kfm, "getPermanentFocusOwner")
                    && ancestor != null
                    && Methods.invokeGetter(ancestor, "isFocused", false);
        } catch (NoSuchMethodException e) {
            return component.hasFocus();
        }


    }

    static boolean isHeadless() {
        return Methods.invokeStaticGetter(GraphicsEnvironment.class, "isHeadless", false);
    }

    /** Returns the class name of SwingUtilities2. */
    private static String getSwingUtilities2() {
        if (swingUtilities2 == null) {
            // Location of SwingUtilities2 in J2SE6:
            swingUtilities2 = "sun.swing.SwingUtilities2";
            try {
                Class.forName(swingUtilities2);
            } catch (ClassNotFoundException ex) {
                // Location of SwingUtilities2 in J2SE5:
                swingUtilities2 = "com.sun.java.swing.SwingUtilities2";
                try {
                    Class.forName(swingUtilities2);
                } catch (ClassNotFoundException ex2) {
                    System.err.println("Warning QuaquaUtilities: Couldn't locate class " + swingUtilities2);
                }
            }
        }
        return swingUtilities2;
    }

    public static int getLeftSideBearing(Font f, String string) {
        return ((Integer) Methods.invokeStatic(
                getSwingUtilities2(), "getLeftSideBearing",
                new Class[]{Font.class, String.class}, new Object[]{f, string},
                0)).intValue();
    }

    /**
     * Invoked when the user attempts an invalid operation,
     * such as pasting into an uneditable {@code JTextField}
     * that has focus. The default implementation beeps. Subclasses
     * that wish different behavior should override this and provide
     * the additional feedback.
     *
     * @param component Component the error occured in, may be null
     *			indicating the error condition is not directly
     *			associated with a {@code Component}.
     */
    static void provideErrorFeedback(Component component) {
        Toolkit toolkit = null;
        if (component != null) {
            toolkit = component.getToolkit();
        } else {
            toolkit = Toolkit.getDefaultToolkit();
        }
        toolkit.beep();
    } // provideErrorFeedback()

    public static BufferedImage createBufferedImage(URL location) {
        Image image = Toolkit.getDefaultToolkit().createImage(location);
        BufferedImage buf;
        if (image instanceof BufferedImage) {
            buf = (BufferedImage) image;
        } else {
            loadImage(image);
            //buf = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            buf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.OPAQUE);

            Graphics g = buf.getGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image.flush();
        }
        return buf;
    }

    public static TexturePaint createTexturePaint(URL location) {
        BufferedImage texture = createBufferedImage(location);
        TexturePaint paint = new TexturePaint(texture, new Rectangle(0, 0, texture.getWidth(), texture.getHeight()));
        return paint;
    }

    /**
     * Loads the image, returning only when the image is loaded.
     * @param image the image
     */
    private static void loadImage(Image image) {
        Component component = new Component() {
        };
        MediaTracker tracker = new MediaTracker(component);
        synchronized (tracker) {
            int id = 0;

            tracker.addImage(image, id);
            try {
                tracker.waitForID(id, 0);
            } catch (InterruptedException e) {
                if (DEBUG) {
                    System.out.println("INTERRUPTED while loading Image");
                }
            }
            ///int loadStatus = tracker.statusID(id, false);
            tracker.removeImage(image, id);
        }
    }

    /**
     * Compute and return the location of the icons origin, the
     * location of origin of the text baseline, and a possibly clipped
     * version of the compound labels string.  Locations are computed
     * relative to the viewR rectangle.
     * The JComponents orientation (LEADING/TRAILING) will also be taken
     * into account and translated into LEFT/RIGHT values accordingly.
     */
    public static String layoutCompoundLabel(JComponent c,
            FontMetrics fm,
            String text,
            Icon icon,
            int verticalAlignment,
            int horizontalAlignment,
            int verticalTextPosition,
            int horizontalTextPosition,
            Rectangle viewR,
            Rectangle iconR,
            Rectangle textR,
            int textIconGap) {
        boolean orientationIsLeftToRight = true;
        int hAlign = horizontalAlignment;
        int hTextPos = horizontalTextPosition;

        if (c != null) {
            if (!(c.getComponentOrientation().isLeftToRight())) {
                orientationIsLeftToRight = false;
            }
        }

        // Translate LEADING/TRAILING values in horizontalAlignment
        // to LEFT/RIGHT values depending on the components orientation
        switch (horizontalAlignment) {
            case LEADING:
                hAlign = (orientationIsLeftToRight) ? LEFT : RIGHT;
                break;
            case TRAILING:
                hAlign = (orientationIsLeftToRight) ? RIGHT : LEFT;
                break;
        }

        // Translate LEADING/TRAILING values in horizontalTextPosition
        // to LEFT/RIGHT values depending on the components orientation
        switch (horizontalTextPosition) {
            case LEADING:
                hTextPos = (orientationIsLeftToRight) ? LEFT : RIGHT;
                break;
            case TRAILING:
                hTextPos = (orientationIsLeftToRight) ? RIGHT : LEFT;
                break;
        }

        return layoutCompoundLabelImpl(c,
                fm,
                text,
                icon,
                verticalAlignment,
                hAlign,
                verticalTextPosition,
                hTextPos,
                viewR,
                iconR,
                textR,
                textIconGap);
    }

    /**
     * Compute and return the location of the icons origin, the
     * location of origin of the text baseline, and a possibly clipped
     * version of the compound labels string.  Locations are computed
     * relative to the viewR rectangle.
     * This layoutCompoundLabel() does not know how to handle LEADING/TRAILING
     * values in horizontalTextPosition (they will default to RIGHT) and in
     * horizontalAlignment (they will default to CENTER).
     * Use the other version of layoutCompoundLabel() instead.
     */
    public static String layoutCompoundLabel(
            FontMetrics fm,
            String text,
            Icon icon,
            int verticalAlignment,
            int horizontalAlignment,
            int verticalTextPosition,
            int horizontalTextPosition,
            Rectangle viewR,
            Rectangle iconR,
            Rectangle textR,
            int textIconGap) {
        return layoutCompoundLabelImpl(null, fm, text, icon,
                verticalAlignment,
                horizontalAlignment,
                verticalTextPosition,
                horizontalTextPosition,
                viewR, iconR, textR, textIconGap);
    }

    /**
     * Compute and return the location of the icons origin, the
     * location of origin of the text baseline, and a possibly clipped
     * version of the compound labels string.  Locations are computed
     * relative to the viewR rectangle.
     * This layoutCompoundLabel() does not know how to handle LEADING/TRAILING
     * values in horizontalTextPosition (they will default to RIGHT) and in
     * horizontalAlignment (they will default to CENTER).
     * Use the other version of layoutCompoundLabel() instead.
     *
     * This is the same as SwingUtilities.layoutCompoundLabelImpl, except for
     * the algorithm for clipping the text. If a text is too long, "..." are
     * inserted at the middle of the text instead of at the end.
     */
    private static String layoutCompoundLabelImpl(
            JComponent c,
            FontMetrics fm,
            String text,
            Icon icon,
            int verticalAlignment,
            int horizontalAlignment,
            int verticalTextPosition,
            int horizontalTextPosition,
            Rectangle viewR,
            Rectangle iconR,
            Rectangle textR,
            int textIconGap) {
        /* Initialize the icon bounds rectangle iconR.
         */

        if (icon != null) {
            iconR.width = icon.getIconWidth();
            iconR.height = icon.getIconHeight();
        } else {
            iconR.width = iconR.height = 0;
        }

        /* Initialize the text bounds rectangle textR.  If a null
         * or and empty String was specified we substitute "" here
         * and use 0,0,0,0 for textR.
         */

        boolean textIsEmpty = (text == null) || text.equals("");
        int lsb = 0;

        View v = null;
        if (textIsEmpty) {
            textR.width = textR.height = 0;
            text = "";
        } else {
            v = (c != null) ? (View) c.getClientProperty("html") : null;
            if (v != null) {
                textR.width = (int) v.getPreferredSpan(View.X_AXIS);
                textR.height = (int) v.getPreferredSpan(View.Y_AXIS);
            } else {
                textR.width = SwingUtilities.computeStringWidth(fm, text);

                lsb = getLeftSideBearing(fm.getFont(), text);
                if (lsb < 0) {
                    // If lsb is negative, add it to the width, the
                    // text bounds will later be adjusted accordingly.
                    textR.width -= lsb;
                }
                textR.height = fm.getHeight();
            }
        }

        /* Unless both text and icon are non-null, we effectively ignore
         * the value of textIconGap.  The code that follows uses the
         * value of gap instead of textIconGap.
         */

        int gap = (textIsEmpty || (icon == null)) ? 0 : textIconGap;

        if (!textIsEmpty) {

            /* If the label text string is too wide to fit within the available
             * space "..." and as many characters as will fit will be
             * displayed instead.
             */

            int availTextWidth;

            if (horizontalTextPosition == CENTER) {
                availTextWidth = viewR.width;
            } else {
                availTextWidth = viewR.width - (iconR.width + gap);
            }


            if (textR.width > availTextWidth) {
                if (v != null) {
                    textR.width = availTextWidth;
                } else {
                    String clipString = "...";
                    int totalWidth = SwingUtilities.computeStringWidth(fm, clipString);
                    int nChars;
                    int len = text.length();
                    for (nChars = 0; nChars < len; nChars++) {
                        int charIndex = (nChars % 2 == 0) ? nChars / 2 : len - 1 - nChars / 2;
                        totalWidth += fm.charWidth(text.charAt(charIndex));
                        if (totalWidth > availTextWidth) {
                            break;
                        }
                    }
                    text = text.substring(0, nChars / 2) + clipString + text.substring(len - nChars / 2);
                    textR.width = SwingUtilities.computeStringWidth(fm, text);
                }
            }
        }


        /* Compute textR.x,y given the verticalTextPosition and
         * horizontalTextPosition properties
         */

        if (verticalTextPosition == TOP) {
            if (horizontalTextPosition != CENTER) {
                textR.y = 0;
            } else {
                textR.y = -(textR.height + gap);
            }
        } else if (verticalTextPosition == CENTER) {
            textR.y = (iconR.height / 2) - (textR.height / 2);
        } else { // (verticalTextPosition == BOTTOM)

            if (horizontalTextPosition != CENTER) {
                textR.y = iconR.height - textR.height;
            } else {
                textR.y = (iconR.height + gap);
            }
        }

        if (horizontalTextPosition == LEFT) {
            textR.x = -(textR.width + gap);
        } else if (horizontalTextPosition == CENTER) {
            textR.x = (iconR.width / 2) - (textR.width / 2);
        } else { // (horizontalTextPosition == RIGHT)

            textR.x = (iconR.width + gap);
        }

        /* labelR is the rectangle that contains iconR and textR.
         * Move it to its proper position given the labelAlignment
         * properties.
         *
         * To avoid actually allocating a Rectangle, Rectangle.union
         * has been inlined below.
         */
        int labelR_x = Math.min(iconR.x, textR.x);
        int labelR_width = Math.max(iconR.x + iconR.width,
                textR.x + textR.width) - labelR_x;
        int labelR_y = Math.min(iconR.y, textR.y);
        int labelR_height = Math.max(iconR.y + iconR.height,
                textR.y + textR.height) - labelR_y;

        int dx, dy;

        if (verticalAlignment == TOP) {
            dy = viewR.y - labelR_y;
        } else if (verticalAlignment == CENTER) {
            dy = (viewR.y + (viewR.height / 2)) - (labelR_y + (labelR_height / 2));
        } else { // (verticalAlignment == BOTTOM)

            dy = (viewR.y + viewR.height) - (labelR_y + labelR_height);
        }

        if (horizontalAlignment == LEFT) {
            dx = viewR.x - labelR_x;
        } else if (horizontalAlignment == RIGHT) {
            dx = (viewR.x + viewR.width) - (labelR_x + labelR_width);
        } else { // (horizontalAlignment == CENTER)

            dx = (viewR.x + (viewR.width / 2))
                    - (labelR_x + (labelR_width / 2));
        }

        /* Translate textR and glypyR by dx,dy.
         */

        textR.x += dx;
        textR.y += dy;

        iconR.x += dx;
        iconR.y += dy;

        if (lsb < 0) {
            // lsb is negative. We previously adjusted the bounds by lsb,
            // we now need to shift the x location so that the text is
            // drawn at the right location. The result is textR does not
            // line up with the actual bounds (on the left side), but we will
            // have provided enough space for the text.
            textR.width += lsb;
            textR.x -= lsb;
        }

        return text;
    }

    /**
     * Uses some unsupported (dangerous) API calls on the native peers to make
     * a window translucent. If the API is not found, this method leaves the
     * window opaque.
     *
     * @param w The Window.
     * @param value The alpha channel for the window.
     */
    static void setWindowAlpha(Window w, int value) {
        if (w == null) {
            return;
        }


        if (w instanceof RootPaneContainer) {
            JRootPane rp = ((RootPaneContainer) w).getRootPane();

            // Window alpha is for J2SE 5 on Mac OS X 10.5
            // See: http://developer.apple.com/technotes/tn2007/tn2196.html#WINDOW_ALPHA
            rp.putClientProperty("Window.alpha", new Float(value / 255f));

        }
    }

    static void setWindowAlphaOld(Window w, int value) {
        if (w == null) {
            return;
        }

        if (QuaquaManager.isOSX()) {
            // Try Mac API
            /*
            // Platform neutral API
            w.setBackground(new Color(0, 0, 0, value));
             */

            // Java 1.4.2_05 does not support window alpha.
            // Setting window alpha only sets the background color of the window
            // to white.

            if (w.getPeer() == null) {
                w.pack();
            }
            java.awt.peer.ComponentPeer peer = w.getPeer();
            try {
                // Alpha API for Apple's Java 1.4 + 1.5 on Mac OS X 10.4 Tiger.
                Methods.invoke(peer, "setAlpha", (float) (value / 255f));
                // Platform neutral API
                w.setBackground(new Color(255, 255, 255, value));
                if (w instanceof RootPaneContainer) {
                    ((RootPaneContainer) w).getContentPane().setBackground(new Color(255, 255, 255, 0));
                }
            } catch (Throwable e) {
                // Alpha API for Apple's Java 1.3.
                if (QuaquaManager.getProperty("java.version").startsWith("1.3")) {
                    try {
                        Methods.invoke(peer, "_setAlpha", value);
                    } catch (Throwable e2) {
                        // Platform neutral API
                        w.setBackground(new Color(255, 255, 255, value));
                        if (w instanceof RootPaneContainer) {
                            ((RootPaneContainer) w).getContentPane().setBackground(new Color(255, 255, 255, 0));
                        }
                    }
                }
            }
        } else {
            // Try J2SE 6 Update 10 API on Windows
            try {
                Class clazz = Class.forName("com.sun.awt.AWTUtilities");
                Method method =
                        clazz.getMethod("setWindowOpaque", new Class[]{java.awt.Window.class, Boolean.TYPE});
                method.invoke(clazz, new Object[]{w, Boolean.FALSE});
            } catch (Throwable e2) {
                // silently ignore this exception.
            }
        }
    }

    /** Copied from BasicLookAndFeel.
     */
    public static Component compositeRequestFocus(Component component) {
        try {
            if (component instanceof Container) {
                Container container = (Container) component;
                if (Methods.invokeGetter(container, "isFocusCycleRoot", false)) {

                    Object policy = Methods.invokeGetter(container, "getFocusTraversalPolicy", null);
                    Component comp = (Component) Methods.invoke(policy, "getDefaultComponent", Container.class, container);
                    if (comp != null) {
                        comp.requestFocus();
                        return comp;
                    }
                }
                Container rootAncestor = (Container) Methods.invokeGetter(container, "getFocusCycleRootAncestor", null);
                if (rootAncestor != null) {
                    Object policy = Methods.invokeGetter(rootAncestor, "getFocusTraversalPolicy", null);
                    Component comp = (Component) Methods.invoke(policy, "getComponentAfter",
                            new Class[]{Container.class, Component.class},
                            new Object[]{rootAncestor, container});

                    if (comp != null && SwingUtilities.isDescendingFrom(comp, container)) {
                        comp.requestFocus();
                        return comp;
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            // ignore
        }
        if (Methods.invokeGetter(component, "isFocusable", true)) {
            component.requestFocus();
            return component;
        }
        return null;
    }

    /**
     * Convenience method for installing a property with the specified name
     * and value on a component if that property has not already been set
     * by the client program.  This method is intended to be used by
     * UI delegate instances that need to specify a default value for a
     * property of primitive type (boolean, int, ..), but do not wish
     * to override a value set by the client.  Since primitive property
     * values cannot be wrapped with the UIResource marker, this method
     * uses private state to determine whether the property has been set
     * by the client.
     * @throws IllegalArgumentException if the specified property is not
     *         one which can be set using this method
     * @throws ClassCastException may be thrown if the property value
     *         specified does not match the property's type
     * @throws NullPointerException may be thrown if c or propertyValue is null
     * @param c the target component for installing the property
     * @param propertyName String containing the name of the property to be set
     */
    public static void installProperty(JComponent c,
            String propertyName, Object value) {
        LookAndFeel.installProperty(c, propertyName, value);
    }

    /**
     * Returns the ui that is of type {@code klass}, or null if
     * one can not be found.
     */
    public static Object getUIOfType(ComponentUI ui, Class klass) {
        if (klass.isInstance(ui)) {
            return ui;
        }
        return null;
    }

    public static void adjustFocus(JComponent tree) {
        try {
            Methods.invokeStatic(getSwingUtilities2(), "adjustFocus", JComponent.class, tree);
        } catch (NoSuchMethodException ex) {
            tree.requestFocusInWindow();
        }
    }

    static boolean shouldIgnore(MouseEvent e, JComponent tree) {
        //return QuaquaUtilities2.shouldIgnore(e, tree);
        try {
            return ((Boolean) Methods.invokeStatic(getSwingUtilities2(), "shouldIgnore",
                    new Class[]{MouseEvent.class, JComponent.class},
                    new Object[]{e, tree})).booleanValue();
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }

    /** Gets the size variant of a component.
     * <p>
     * The size variant can be explicitly set using the client property
     * "JComponent.sizeVariant="regular"|"small"|"mini".
     * <p>
     * The default size variant is "regular".
     * If a component is a cell renderer, the default size variant is "small".
     * 
     * @param c
     * @return size variant.
     */
    public static SizeVariant getSizeVariant(Component c) {
        if (c == null) {
            return SizeVariant.REGULAR;
        }
        SizeVariant sv = null;
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            String p = (String) jc.getClientProperty("JComponent.sizeVariant");
            if (p != null) {
                if (p.equals("large")) {
                    sv = SizeVariant.LARGE;
                }else if (p.equals("regular")) {
                    sv = SizeVariant.REGULAR;
                } else if (p.equals("small")) {
                    sv = SizeVariant.SMALL;
                } else if (p.equals("mini")) {
                    sv = SizeVariant.MINI;
                }
            }
        }
        if (sv==null) {
            
              if ((c instanceof TableCellRenderer)
                || (c instanceof TableCellEditor)
                || (c.getParent() instanceof JTable)) sv=SizeVariant.SMALL;
        }
        
        if (sv == null) {
            Font f = c.getFont();
            if (f != null) {
                int fs = f.getSize();
                if (fs <= 9) {
                    sv = SizeVariant.MINI;
                } else if (fs <= 11) {
                    sv = SizeVariant.SMALL;
                } else if (fs <= 14) {
                    sv = SizeVariant.REGULAR;
                } else {
                    sv = SizeVariant.LARGE;
                }
            }
        }
        return sv == null ? SizeVariant.REGULAR : sv;
    }

    public static Font getSizeVariantFont(JComponent c) {
        Font font = c.getFont();
        if (font == null || (font instanceof UIResource)) {
            switch (getSizeVariant(c)) {
                case REGULAR:
                default:
                    font = UIManager.getFont("SystemFont");
                    break;
                case SMALL:
                    font = UIManager.getFont("SmallSystemFont");
                    break;
                case MINI:
                    font = UIManager.getFont("MiniSystemFont");
                    break;
            }

            String pstyle = (String) c.getClientProperty("Quaqua.Tree.style");
            if (pstyle != null && (pstyle.equals("sideBar")||pstyle.equals("sourceList"))) {
                font = UIManager.getFont("Tree.sideBar.selectionFont");
            }
            String bstyle=(String)c.getClientProperty("Quaqua.Button.style");
            if (bstyle==null) bstyle=(String)c.getClientProperty("JButton.buttonType");
            if (bstyle!=null && bstyle.equals("tableHeader")) {
                font=UIManager.getFont("TableHeader.font");
            }
        }
        return font == null ? UIManager.getFont("SystemFont") : font;

    }

    public static void applySizeVariant(JComponent c) {
        Font font = getSizeVariantFont(c);
        c.setFont(font);
    }

    public static int getDragThreshold() {
        try {
            Integer value = (Integer) Methods.invokeStatic(DragSource.class, "getDragThreshold");
            return value.intValue();
        } catch (Throwable ex) {
            //ex.printStackTrace();
            return 5;
        }
    }

    public static int mapDragOperationFromModifiers(MouseEvent me,
            TransferHandler th) {
        return convertModifiersToDropAction(me.getModifiersEx(), th.getSourceActions((JComponent) me.getSource()));
    }

    public static int convertModifiersToDropAction(final int modifiers,
            final int supportedActions) {
        int dropAction = DnDConstants.ACTION_NONE;

        /*
         * Fix for 4285634.
         * Calculate the drop action to match Motif DnD behavior.
         * If the user selects an operation (by pressing a modifier key),
         * return the selected operation or ACTION_NONE if the selected
         * operation is not supported by the drag source.
         * If the user doesn't select an operation search the set of operations
         * supported by the drag source for ACTION_MOVE, then for
         * ACTION_COPY, then for ACTION_LINK and return the first operation
         * found.
         */
        switch (modifiers & (InputEvent.SHIFT_DOWN_MASK
                | InputEvent.CTRL_DOWN_MASK)) {
            case InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK:
                dropAction = DnDConstants.ACTION_LINK;
                break;
            case InputEvent.CTRL_DOWN_MASK:
                dropAction = DnDConstants.ACTION_COPY;
                break;
            case InputEvent.SHIFT_DOWN_MASK:
                dropAction = DnDConstants.ACTION_MOVE;
                break;
            default:
                if ((supportedActions & DnDConstants.ACTION_MOVE) != 0) {
                    dropAction = DnDConstants.ACTION_MOVE;
                } else if ((supportedActions & DnDConstants.ACTION_COPY) != 0) {
                    dropAction = DnDConstants.ACTION_COPY;
                } else if ((supportedActions & DnDConstants.ACTION_LINK) != 0) {
                    dropAction = DnDConstants.ACTION_LINK;
                }
        }

        return dropAction & supportedActions;
    }

    /**
     * Return the child {@code Component} of the specified
     * {@code Component} that is the focus owner, if any.
     *
     * @param c the root of the {@code Component} hierarchy to
     *        search for the focus owner
     * @return the focus owner, or {@code null} if there is no focus
     *         owner, or if the focus owner is not {@code comp}, or a
     *         descendant of {@code comp}
     *
     * @see java.awt.KeyboardFocusManager#getFocusOwner
     */
    public static Component findFocusOwner(Component c) {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

        // verify focusOwner is a descendant of c
        for (Component temp = focusOwner; temp != null;
                temp = (temp instanceof Window) ? null : temp.getParent()) {
            if (temp == c) {
                return focusOwner;
            }
        }

        return null;
    }

    public static boolean isOnTexturedWindow(Component c) {
        boolean isTextured;
        JRootPane rootPane = SwingUtilities.getRootPane(c);
        if (rootPane != null) {
            isTextured = rootPane.getClientProperty("apple.awt.brushMetalLook") == Boolean.TRUE;
        } else {
            isTextured = false;
        }
        return isTextured;
    }
    
    /** Returns the visual bounds of the component given in the local
     * coordinate system of the component.
     * 
     * @param c The component.
     * @param type A type from {@link VisuallyLayoutable}.
     * @return The visual bounds. Returns null if the given type is not applicable.
     */
    public static Rectangle getVisualBounds(Component c, int type) {
        if (c instanceof JComponent) {
            JComponent jc=(JComponent)c;
            ComponentUI ui=(ComponentUI)Methods.invokeGetter(jc, "getUI",null);
            if (ui instanceof VisuallyLayoutable) {
                VisuallyLayoutable vl=(VisuallyLayoutable)ui;
                return vl.getVisualBounds(jc,type,jc.getWidth(),jc.getHeight());
                
            }
        }
            return type==VisuallyLayoutable.CLIP_BOUNDS?new Rectangle(0,0,c.getWidth(),c.getHeight()):null;
    }
}
