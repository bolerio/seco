/*
 * @(#)Fonts.java 
 *
 * Copyright (c) 2003-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.util;

import ch.randelshofer.quaqua.*;

import java.util.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * Fonts.
 *
 * @author  Werner Randelshofer, Switzerland
 * @version $Id: Fonts.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class Fonts {

    private static volatile HashMap fonts;
    /**
     * Default font render context for Mac OS X.
     * XXX - We should create the default depending on properties of the current
     * Look and Feel.
     */
    private static FontRenderContext defaultFontRenderContext = new FontRenderContext(new AffineTransform(), true, true);

    /** Creates a new instance. */
    private Fonts() {
    }

    private static void init() {
        if (fonts == null) {
            synchronized (Fonts.class) {
                if (fonts == null) {
                    fonts = new HashMap();
                    Font dialogFont = UIManager.getFont("Label.font");

                    Font emphasizedDialogFont = dialogFont.deriveFont(Font.BOLD);
                    Font smallDialogFont;
                    if (dialogFont.getSize() >= 13) {
                        smallDialogFont = dialogFont.deriveFont((float) (dialogFont.getSize() - 2));
                    } else {
                        smallDialogFont = dialogFont;
                    }
                    Font emphasizedSmallDialogFont = smallDialogFont.deriveFont(Font.BOLD);


                    fonts.put("Dialog", dialogFont);
                    fonts.put("EmphasizedDialog", emphasizedDialogFont);
                    fonts.put("SmallDialog", smallDialogFont);
                    fonts.put("EmphasizedSmallDialog", emphasizedSmallDialogFont);
                    fonts.put("Application", dialogFont);
                    fonts.put("Label", dialogFont.deriveFont(10f));
                    fonts.put("MiniDialog", dialogFont.deriveFont(9f));
                    fonts.put("Monospace", new Font("Courier", Font.PLAIN, dialogFont.getSize()));


                    if (QuaquaManager.getProperty("java.version").startsWith("1.3")) {
                        fonts.put("DialogTag", "<font face='" + dialogFont.getName() + "'>");
                        fonts.put("/DialogTag", "</font>");
                        fonts.put("SmallDialogTag", "<font face='" + dialogFont.getName() + "' size=-1>");
                        fonts.put("/SmallDialogTag", "</font>");
                        fonts.put("MiniDialogTag", "<font face='" + dialogFont.getName() + "' size=-2>");
                        fonts.put("/MiniDialogTag", "</font>");
                        fonts.put("EmphasizedDialogTag", "<font face='" + dialogFont.getName() + "'><b>");
                        fonts.put("/EmphasizedDialogTag", "</b></font>");
                    } else {
                        fonts.put("DialogTag", "");
                        fonts.put("/DialogTag", "");
                        fonts.put("SmallDialogTag", "<font size=-1>");
                        fonts.put("/SmallDialogTag", "</font>");
                        fonts.put("MiniDialogTag", "<font size=-2>");
                        fonts.put("/MiniDialogTag", "</font>");
                        fonts.put("EmphasizedDialogTag", "<b>");
                        fonts.put("/EmphasizedDialogTag", "</b>");
                    }
                }
            }
        }
    }

    /**
     * The dialog font is used for text in menus, modeless dialogs, and titles
     * of document windows.
     */
    public static Font getDialogFont() {
        init();
        return (Font) fonts.get("Dialog");
    }

    /**
     * Use emphasized dialog fonts sparingly. Emphasized (bold) dialog font is
     * used in only two places in the interface: the application name in an
     * About window and the message text in an option pane.
     */
    public static Font getEmphasizedDialogFont() {
        init();
        return (Font) fonts.get("EmphasizedDialog");
    }

    /**
     * The small dialog font is used for informative text in alerts.
     * It is also the default font for headings in lists, for help tags, and for
     * text in the small versions of many controls. You can also use it to
     * provide additional information about settings in various windows.
     */
    public static Font getSmallDialogFont() {
        init();
        return (Font) fonts.get("SmallDialog");
    }

    /**
     * You might use emphasized small dialog font to title a group of settings
     * that appear without a group box, or for brief informative text below a
     * text field.
     */
    public static Font getEmphasizedSmallDialogFont() {
        init();
        return (Font) fonts.get("EmphasizedSmallDialog");
    }

    /**
     * If your application creates text documents, use the application font as
     * the default for user-created content.
     */
    public static Font getApplicationFont() {
        init();
        return (Font) fonts.get("Application");
    }

    /**
     * If your application needs monospaced fonts, use the monospace font.
     */
    public static Font getMonospaceFont() {
        init();
        return (Font) fonts.get("Monospace");
    }

    /**
     * The label font is used for labels with controls such as sliders and icon
     * bevel buttons. You should rarely need to use this font in dialogs, but
     * may find it useful in utility windows when space is at a premium.
     */
    public static Font getLabelFont() {
        init();
        return (Font) fonts.get("Label");
    }

    /**
     * If necessary, the mini dialog font can be used for utility window labels
     * and text.
     */
    public static Font getMiniDialogFont() {
        init();
        return (Font) fonts.get("MiniDialog");
    }

    /**
     * Puts an HTML font tag for the Dialog Font around the specified text.
     */
    public static String dialogFontTag(String text) {
        init();
        return fonts.get("DialogTag") + text + fonts.get("/DialogTag");
    }

    /**
     * Puts an HTML font tag for the Small Dialog Font around the specified text.
     */
    public static String smallDialogFontTag(String text) {
        init();
        return fonts.get("SmallDialogTag") + text + fonts.get("/SmallDialogTag");
    }

    /**
     * Puts an HTML font tag for the Small Dialog Font around the specified text.
     */
    public static String miniDialogFontTag(String text) {
        init();
        return fonts.get("MiniDialogTag") + text + fonts.get("/MiniDialogTag");
    }

    /**
     * Puts an HTML font tag for the Emphasized Dialog Font around the specified text.
     */
    public static String emphasizedDialogFontTag(String text) {
        init();
        return fonts.get("EmphasizedDialogTag") + text + fonts.get("/EmphasizedDialogTag");
    }

    /**
     * Gets the visually perceived ascent of the specified character.
     */
    public static int getVisualAscent(Font f, Component c, char ch) {
        Graphics2D g = (Graphics2D) c.getGraphics();
        FontRenderContext frc = (g == null) ? defaultFontRenderContext : g.getFontRenderContext();
        GlyphVector gv = f.createGlyphVector(frc, new char[]{ch});
        return -gv.getVisualBounds().getBounds().y;
    }

    /**
     * Returns the perceived bounds of the specified string, if it is rendered
     * using the specified font on the provided component.
     */
    public static Rectangle getPerceivedBounds(String str, Font f, Component c) {
        if (str == null) {
            return new Rectangle(0, 0, 0, 0);
        } else {
            FontRenderContext frc;
            Graphics2D g = ((Graphics2D) c.getGraphics());
            if (g != null) {
                frc = g.getFontRenderContext();
                g.dispose();
            } else {
                frc = new FontRenderContext(new AffineTransform(), true, true);
            }
            int scriptSystem = (str == null || str.length() == 0) ? ScriptSystem.LATIN : ScriptSystem.getScriptSystemOf(str.charAt(0));
            int ascent = getPerceivedAscent(f, frc, scriptSystem);
            int descent = getPerceivedDescent(f, frc, scriptSystem);
            Rectangle2D stringBounds = f.getStringBounds(str, frc);
            return new Rectangle(1, -ascent, (int) stringBounds.getWidth() - 2, ascent + descent);
        }
    }

    /**
     * Returns the perceived ascent of the specified font, if text is written
     * using the specified script system and font render context.
     * <p>
     * The perceived ascent is a distance above the baseline, chosen by the
     * font designer and the same for all glyphs in a script system, that often
     * corresponds approximately to the tops of the uppercase letters in a Latin
     * script system. Uppercase letters are chosen because, among the regularly
     * used glyphs in a font, they are generally the tallest.
     * <p>
     * The value returned by this method differs from the ascent returned by
     * java.awt.font.LineMetrics.getAscent(), in that the perceived ascent does
     * not include diacritical marks of the font. Therefore the perceived ascent
     * is useful for visually north alignig text with graphical elements.
     */
    public static int getPerceivedAscent(Font f, FontRenderContext frc, int system) {
        if (ScriptSystem.getBaseline(system) == Font.HANGING_BASELINE) {
            // Shortcut, also prevents that we are returning bad values based
            // on the measurement character used.
            return 0;
        } else {
            char ch = ScriptSystem.getMeasurementChar(system);
            GlyphVector gv = f.createGlyphVector(frc, new char[]{ch});
            return gv.getVisualBounds().getBounds().height;
        }
    }

    /**
     * Returns the perceived descent of the specified font, if text is written
     * using the specified script system and font render context.
     * <p>
     * The perceived descent is a distance below the baseline that usually
     * corresponds to the bottoms of uppercase letters in a Latin script system,
     * whithout the descenders of lower case characters. In a Latin script
     * system the perceived descent is usually 0. The descent is the same
     * distance from the baseline for all glyphs in the script system.
     * <p>
     * The value returned by this method differs from the one that would be
     * returned by java.awt.font.LineMetrics.getAscent(), in that the perceived
     * descent does not include the descender of lower case characters and
     * diacritical marks below them. Therefore the perceived descent is useful
     * for visully south aligning text with graphical elements.
     */
    public static int getPerceivedDescent(Font f, FontRenderContext frc, int system) {
        if (ScriptSystem.getBaseline(system) == Font.ROMAN_BASELINE) {
            // Shortcut, also prevents that we are returning bad values based
            // on the measurement character used.
            return 0;
        } else {
            char ch = ScriptSystem.getMeasurementChar(system);
            GlyphVector gv = f.createGlyphVector(frc, new char[]{ch});
            Rectangle bounds = gv.getVisualBounds().getBounds();
            return bounds.y + bounds.height;
        }
    }
}
