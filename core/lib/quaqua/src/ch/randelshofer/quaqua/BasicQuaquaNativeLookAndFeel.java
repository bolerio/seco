/*
 * @(#)BasicQuaquaNativeLookAndFeel.java
 *
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.border.VisualMarginBorder;
import ch.randelshofer.quaqua.osx.OSXPreferences;
import ch.randelshofer.quaqua.color.*;
import ch.randelshofer.quaqua.osx.OSXAquaPainter;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.security.*;
import sun.awt.AppContext;

/**
 * The BasicQuaquaNativeLookAndFeel contains the look and feel properties that are
 * commonly uses by QuaquaLookAndFeel classes which use native painters.
 *
 * @author  Werner Randelshofer
 * @version $Id: BasicQuaquaNativeLookAndFeel.java 464 2014-03-22 12:32:00Z wrandelshofer $
 */
public class BasicQuaquaNativeLookAndFeel extends LookAndFeelProxy15 {

    protected final static String commonDir = "/ch/randelshofer/quaqua/images/";
    protected final static String jaguarDir = "/ch/randelshofer/quaqua/jaguar/images/";
    protected final static String pantherDir = "/ch/randelshofer/quaqua/panther/images/";
    protected final static String tigerDir = "/ch/randelshofer/quaqua/tiger/images/";
    protected final static String leopardDir = "/ch/randelshofer/quaqua/leopard/images/";
    protected final static String snowLeopardDir = "/ch/randelshofer/quaqua/snowleopard/images/";
    protected final static String lionDir = "/ch/randelshofer/quaqua/lion/images/";

    /** Creates a new instance.
     * @param targetClassName Proxy target.
     */
    public BasicQuaquaNativeLookAndFeel(String targetClassName) {
        try {
            setTarget((LookAndFeel) Class.forName(targetClassName).newInstance());
        } catch (Exception e) {
            throw new InternalError(
                    "Unable to instanciate target Look and Feel \"" + targetClassName + "\". " + e.getMessage());
        }
    }

    /**
     * Return a string that identifies this look and feel.  This string
     * will be used by applications/services that want to recognize
     * well known look and feel implementations.  Presently
     * the well known names are "Motif", "Windows", "Mac", "Metal".  Note
     * that a LookAndFeel derived from a well known superclass
     * that doesn't make any fundamental changes to the look or feel
     * shouldn't override this method.
     */
    @Override
    public String getID() {
        return "Aqua";
    }

    /**
     * This method is called once by UIManager.setLookAndFeel to create
     * the look and feel specific defaults table.  Other applications,
     * for example an application builder, may also call this method.
     *
     * @see #initialize
     * @see #uninitialize
     * @see UIManager#setLookAndFeel
     */
    @Override
    public UIDefaults getDefaults() {
        UIDefaults table = target.getDefaults();

        initClassDefaults(table);
        initSystemColorDefaults(table);
        initComponentDefaults(table);

        installKeyboardFocusManager();
        installPopupFactory();
        installMouseGrabber();

        return table;
    }

    @Override
    protected final void initComponentDefaults(UIDefaults table) {
        initResourceBundle(table);
        initColorDefaults(table);
        initInputMapDefaults(table);
        initFontDefaults(table);
        initGeneralDefaults(table);
        initDesignDefaults(table);
    }

    protected void initResourceBundle(UIDefaults table) {
        ResourceBundle bundle = ResourceBundle.getBundle(
                "ch.randelshofer.quaqua.Labels",
                Locale.getDefault(),
                getClass().getClassLoader());
        for (Enumeration i = bundle.getKeys(); i.hasMoreElements();) {
            String key = (String) i.nextElement();
            table.put(key, bundle.getObject(key));
        }
    }
    /**
     * List of well known highlight colors.
     *
     * The values in the second dimension of this array are used as follows:
     * 0 = Highlight color for text
     * 1 = Highlight color for lists, tables and trees
     *
     * If an other color is chosen, we compute the color values
     * using the following algorithm.
     * color, color - 10 % brightness, color - 20% brightness
     */
    private final static int[][] selectionColors = {
        // Graphite
        {0xffc7d0db, 0x778da8},
        // Silver
        {0xffc6c6c6, 0x7f7f7f},
        // Blue
        {0xffb5d5ff, 0x3875d7},
        // Gold
        {0xfffbed73, 0xffc11f},
        // Red
        {0xffffb18c, 0xf34648},
        // Orange
        {0xffffd281, 0xff8a22},
        // Green
        {0xffc3f991, 0x66c547},
        // Purple
        {0xffe9b8ff, 0x8c4eb8},};

    /**
     * Load the SystemColors into the defaults table.  The keys
     * for SystemColor defaults are the same as the names of
     * the public fields in SystemColor.  If the table is being
     * created on a native Windows platform we use the SystemColor
     * values, otherwise we create color uiDefaults whose values match
     * the defaults Windows95 colors.
     */
    @Override
    protected void initSystemColorDefaults(UIDefaults table) {
        ColorUIResource textSelectionBackground;
        ColorUIResource listSelectionBackground;
        ColorUIResource listSelectionForeground;
        ColorUIResource listSelectionBorderColor;
        ColorUIResource inactiveSelectionBackground = new ColorUIResource(0xd0d0d0);

        // Get text selection background from Mac OS X system preferences
        String colorValues = OSXPreferences.getString(OSXPreferences.GLOBAL_PREFERENCES, "AppleHighlightColor", "0.709800 0.835300 1.000000");
        try {
            float[] rgb = new float[3];
            StringTokenizer tt = new StringTokenizer(colorValues);
            for (int i = 0; i < 3; i++) {
                String value = tt.nextToken();
                rgb[i] = Float.valueOf(value).floatValue();
            }
            textSelectionBackground = new ColorUIResource(rgb[0], rgb[1], rgb[2]);
        } catch (Exception e) {
            textSelectionBackground = new ColorUIResource(0xffb5d5ff); // blue
        }

        // Derive list selection colors from text selection background
        listSelectionBorderColor = new ColorUIResource(0x808080);
        if (QuaquaManager.getProperty("Quaqua.selectionStyle", "auto").equals("bright")) {
            listSelectionForeground = new ColorUIResource(0x000000);
            listSelectionBackground = textSelectionBackground;
        } else {
            listSelectionForeground = new ColorUIResource(0xffffff);

            int textSelectionRGB = textSelectionBackground.getRGB() | 0xff000000;

            // For some well known text selection colors, we look up a table
            // to determine the color for list selection backgrounds.
            listSelectionBackground = null;
            for (int i = 0; i < selectionColors.length; i++) {
                if (selectionColors[i][0] == textSelectionRGB) {
                    listSelectionBackground = new ColorUIResource(selectionColors[i][1]);
                    break;
                }
            }

            // If it is not a well known color, we use a 20 percent darker color
            // for the list selection background.
            if (listSelectionBackground == null) {
                float[] hsb = Color.RGBtoHSB(
                        textSelectionBackground.getRed(),
                        textSelectionBackground.getGreen(),
                        textSelectionBackground.getBlue(),
                        null);
                listSelectionBackground = new ColorUIResource(Color.getHSBColor(hsb[0], hsb[1], hsb[2] * 0.8f));
            }
        }

        boolean isGraphite = OSXPreferences.getString(OSXPreferences.GLOBAL_PREFERENCES, "AppleAquaColorVariant", "1").equals("6");


        Object[] uiDefaults = {
            "desktop", new ColorUIResource(isGraphite ? 0x647185 : 0x3a69aa), /* Color of the desktop background */
            "activeCaption", table.get("InternalFrame.activeTitleBackground"), /* Color for captions (title bars) when they are active. */
            "activeCaptionText", new ColorUIResource(0x000000), /* Text color for text in captions (title bars). */
            "activeCaptionBorder", table.get("InternalFrame.borderColor"), /* Border color for caption (title bar) window borders. */
            "inactiveCaption", table.get("InternalFrame.inactiveTitleBackground"), /* Color for captions (title bars) when not active. */
            "inactiveCaptionText", new ColorUIResource(0x666666), /* Text color for text in inactive captions (title bars). */
            "inactiveCaptionBorder", table.get("InternalFrame.borderColor"), /* Border color for inactive caption (title bar) window borders. */
            "window", table.get("control"), /* Default color for the interior of windows */
            "windowBorder", table.get("control"), /* ??? */
            "windowText", new ColorUIResource(0x000000), /* ??? */
            "menu", table.get("MenuItem.background"), /* Background color for menus */
            "menuText", new ColorUIResource(0x000000), /* Text color for menus  */
            "text", new ColorUIResource(0xffffff), /* Text background color */
            "textText", new ColorUIResource(0x000000), /* Text foreground color */
            "textHighlight", textSelectionBackground, /* Text background color when selected */
            "textHighlightText", new ColorUIResource(0x000000), /* Text color when selected */
            "textInactiveText", new ColorUIResource(0x808080), /* Text color when disabled */
            "control", table.get("control"), /* Default color for controls (buttons, sliders, etc) */
            "controlText", new ColorUIResource(0x000000), /* Default color for text in controls */
            "controlHighlight", new ColorUIResource(0xC0C0C0), /* Specular highlight (opposite of the shadow) */
            "controlLtHighlight", new ColorUIResource(0xFFFFFF), /* Highlight color for controls */
            "controlShadow", new ColorUIResource(0x808080), /* Shadow color for controls */
            "controlDkShadow", new ColorUIResource(0x000000), /* Dark shadow color for controls */
            "scrollbar", table.get("control"), /* Scrollbar background (usually the "track") */
            "info", new ColorUIResource(0xffffc1), /* ??? */
            "infoText", new ColorUIResource(0x000000), /* ??? */
            // Quaqua specific 'system' colors
            "list", new ColorUIResource(0xffffff), /* List background color */
            "listText", new ColorUIResource(0x000000), /* List foreground color */
            "listHighlight", listSelectionBackground, /* List background color when selected */
            "listHighlightText", listSelectionForeground, /* List color when selected */
            "listHighlightBorder", listSelectionBorderColor, /* List color when selected */
            "listInactiveHighlight", inactiveSelectionBackground, /* List color when selected */
            "listInactiveText", new ColorUIResource(0x808080), /* List color when disabled */
            "menuHighlightText", new ColorUIResource(0xffffff), /* Menu text color when selected */
            "menuHighlight", table.get("Menu.selectionBackground"), /* Menu background color when selected */};
        putDefaults(table, uiDefaults);
    }

    protected void initColorDefaults(UIDefaults table) {
        // Shared Colors
        Object controlForeground = table.get("controlText");
        Object controlBackground = table.get("control");

        Object textBackground = table.get("text");
        Object textForeground = table.get("textText");
        Object textSelectionBackground = table.get("textHighlight");
        Object translucentColor = new AlphaColorUIResource(0x00000000);

        Object disabledForeground = table.get("textInactiveText");
        ColorUIResource inactiveSelectionBackground = new ColorUIResource(208, 208, 208);
        Object inactiveSelectionForeground = controlForeground;

        Object menuBackground = table.get("menu");
        Object menuForeground = table.get("menuText");
        Object menuSelectionForeground = table.get("menuHighlightText");
        Object menuSelectionBackground = table.get("menuHighlight");
        Object menuDisabledBackground = menuBackground;
        Object menuDisabledForeground = disabledForeground;

        Object listBackground = table.get("list");
        Object listForeground = table.get("listText");

        Object listSelectionBackground = new InactivatableColorUIResource(
                ((Color) table.get("listHighlight")).getRGB(),
                inactiveSelectionBackground.getRGB());
        Object listSelectionForeground = new InactivatableColorUIResource(
                ((Color) table.get("listHighlightText")).getRGB(),
                ((Color) inactiveSelectionForeground).getRGB());

        ColorUIResource listSelectionBorderColor = (ColorUIResource) table.get("listHighlightBorder");
        ColorUIResource listAlternateBackground = OSXPreferences.getString(OSXPreferences.GLOBAL_PREFERENCES, "AppleAquaColorVariant", "1").equals("6") ? new ColorUIResource(0xf0f0f0) : new ColorUIResource(0xedf3fe);



        // Init
        Object[] uiDefaults = {
            "Browser.selectionBackground", listSelectionBackground,
            "Browser.selectionForeground", listSelectionForeground,
            "Browser.selectionBorderColor", listSelectionBorderColor,
            "Browser.inactiveSelectionBackground", inactiveSelectionBackground,
            "Browser.inactiveSelectionForeground", inactiveSelectionForeground,
            "Button.background", controlBackground,
            "Button.foreground", controlForeground,
            "Button.disabledForeground", disabledForeground,
            //"Button.shadow", ???,
            //"Button.darkShadow", ???,
            //"Button.light", ???,
            //"Button.highlight", ???,

            "CheckBox.background", controlBackground,
            "CheckBox.foreground", controlForeground,
            "CheckBox.disabledForeground", disabledForeground,
            //"CheckBox.shadow", ???,
            //"CheckBox.darkShadow", ???,
            //"CheckBox.light", ???,
            //"CheckBox.highlight", ???,

            "CheckBoxMenuItem.background", menuBackground,
            "CheckBoxMenuItem.foreground", menuForeground,
            "CheckBoxMenuItem.selectionForeground", menuSelectionForeground,
            "CheckBoxMenuItem.selectionBackground", menuSelectionBackground,
            "CheckBoxMenuItem.disabledForeground", disabledForeground,
            "CheckBoxMenuItem.acceleratorForeground", menuForeground,
            "CheckBoxMenuItem.acceleratorSelectionForeground", menuSelectionForeground,
            "ColorChooser.background", controlBackground,
            "ColorChooser.foreground", controlForeground,
            //"ColorChooser.swatchesDefaultRecentColor", ...,
            //"ColorChooser.swatchesRecentSwatchSize", ...,

            // Note: The following colors are used in color lists.
            //       It is important that these colors are neutral (black, white
            //       or a shade of gray with saturation 0).
            //       If they aren't neutral, human perception of the color
            //       is negatively affected.
            "ColorChooser.listSelectionBackground", new ColorUIResource(0xd4d4d4),
            "ColorChooser.listSelectionForeground", new ColorUIResource(0x000000),
            "ComboBox.background", controlBackground,
            "ComboBox.foreground", controlForeground,
            //"ComboBox.buttonBackground", ...,
            //"ComboBox.buttonDarkShadow", ...,
            //"ComboBox.buttonHighlight", ...,
            //"ComboBox.buttonShadow", ...,
            "ComboBox.disabledBackground", controlBackground,
            "ComboBox.disabledForeground", disabledForeground,
            "ComboBox.selectionBackground", menuSelectionBackground,
            "ComboBox.selectionForeground", menuSelectionForeground,
            "Dialog.background", controlBackground,
            "Dialog.foreground", controlForeground,
            "Desktop.background", table.get("desktop"),
            "EditorPane.background", textBackground,
            "EditorPane.caretForeground", textForeground,
            "EditorPane.foreground", textForeground,
            "EditorPane.inactiveBackground", textBackground,
            "EditorPane.inactiveForeground", disabledForeground,
            "EditorPane.selectionBackground", textSelectionBackground,
            "EditorPane.selectionForeground", textForeground,
            "FileChooser.previewLabelForeground", textForeground,
            "FileChooser.previewValueForeground", textForeground,
            "FormattedTextField.background", textBackground,
            "FormattedTextField.foreground", textForeground,
            "FormattedTextField.inactiveBackground", textBackground,
            "FormattedTextField.inactiveForeground", disabledForeground,
            "FormattedTextField.selectionBackground", textSelectionBackground,
            "FormattedTextField.selectionForeground", textForeground,
            "InternalFrame.titlePaneBackground.small", makeTextureColor(0xf4f4f4, commonDir + "Frame.titlePane.small.png"),
            "InternalFrame.vTitlePaneBackground.small", makeTextureColor(0xf4f4f4, commonDir + "Frame.vTitlePane.small.png"),
            "InternalFrame.titlePaneForeground.small", controlForeground,
            "InternalFrame.titlePaneShadow.small", new ColorUIResource(0x8e8e8e),
            "InternalFrame.closeIcon.small", makeFrameButtonStateIcon(commonDir + "Frame.closeIcons.small.png", 12),
            "InternalFrame.maximizeIcon.small", makeFrameButtonStateIcon(commonDir + "Frame.maximizeIcons.small.png", 12),
            "InternalFrame.iconifyIcon.small", makeFrameButtonStateIcon(commonDir + "Frame.iconifyIcons.small.png", 12),
            "InternalFrame.titlePaneBackground", makeTextureColor(0xf4f4f4, commonDir + "Frame.titlePane.png"),
            "InternalFrame.vTitlePaneBackground", makeTextureColor(0xf4f4f4, commonDir + "Frame.vTitlePane.png"),
            "InternalFrame.titlePaneForeground", controlForeground,
            "InternalFrame.titlePaneShadow", new ColorUIResource(0x8e8e8e),
            "InternalFrame.closeIcon", makeFrameButtonStateIcon(commonDir + "Frame.closeIcons.png", 12),
            "InternalFrame.maximizeIcon", makeFrameButtonStateIcon(commonDir + "Frame.maximizeIcons.png", 12),
            "InternalFrame.iconifyIcon", makeFrameButtonStateIcon(commonDir + "Frame.iconifyIcons.png", 12),
            "InternalFrame.titlePaneBackground.mini", makeTextureColor(0xf4f4f4, commonDir + "Frame.titlePane.mini.png"),
            "InternalFrame.vTitlePaneBackground.mini", makeTextureColor(0xf4f4f4, commonDir + "Frame.vTitlePane.mini.png"),
            "InternalFrame.titlePaneForeground.mini", controlForeground,
            "InternalFrame.titlePaneShadow.mini", new ColorUIResource(0x8e8e8e),
            "InternalFrame.closeIcon.mini", makeFrameButtonStateIcon(commonDir + "Frame.closeIcons.mini.png", 12),
            "InternalFrame.maximizeIcon,mini", makeFrameButtonStateIcon(commonDir + "Frame.maximizeIcons.mini.png", 12),
            "InternalFrame.iconifyIcon.mini", makeFrameButtonStateIcon(commonDir + "Frame.iconifyIcons.mini.png", 12),
            "InternalFrame.resizeIcon", makeIcon(getClass(), commonDir + "Frame.resize.png"),
            "Label.background", controlBackground,
            "Label.foreground", controlForeground,
            "Label.disabledForeground", disabledForeground,
            //"Label.disabledShadow", ???,

            "List.alternateBackground.0", listAlternateBackground,
            "List.alternateBackground.1", listBackground,
            "List.background", textBackground,
            "List.foreground", controlForeground,
            "List.selectionBackground", listSelectionBackground,
            "List.selectionForeground", listSelectionForeground,
            //"List.inactiveSelectionBackground", listInactiveSelectionBackground,
            //"List.inactiveSelectionForeground", listInactiveSelectionForeground,

            "Menu.background", menuBackground,
            "Menu.foreground", menuForeground,
            "Menu.acceleratorForeground", menuForeground,
            "Menu.acceleratorSelectionForeground", menuSelectionForeground,
            "Menu.selectionBackground", menuSelectionBackground,
            "Menu.selectionForeground", menuSelectionForeground,
            "Menu.disabledBackground", menuDisabledBackground,
            "Menu.disabledForeground", menuDisabledForeground,
            //"MenuBar.background", table.get("MenuBar.background"),
            "MenuBar.background", menuBackground,
            "MenuBar.foreground", menuForeground,
            "MenuItem.background", menuBackground,
            "MenuItem.foreground", menuForeground,
            "MenuItem.acceleratorForeground", menuForeground,
            "MenuItem.acceleratorSelectionForeground", menuSelectionForeground,
            "MenuItem.selectionBackground", menuSelectionBackground,
            "MenuItem.selectionForeground", menuSelectionForeground,
            "MenuItem.disabledBackground", menuDisabledBackground,
            "MenuItem.disabledForeground", menuDisabledForeground,
            "MenuSeparator.background", menuBackground,
            "OptionPane.background", controlBackground,
            "OptionPane.foreground", controlForeground,
            "OptionPane.messageForeground", controlForeground,
            "Panel.background", controlBackground,
            "Panel.foreground", controlForeground,
            "PasswordField.background", textBackground,
            "PasswordField.foreground", textForeground,
            "PasswordField.caretForeground", textForeground,
            "PasswordField.inactiveBackground", textBackground,
            "PasswordField.inactiveForeground", disabledForeground,
            "PasswordField.selectionBackground", textSelectionBackground,
            "PasswordField.selectionForeground", textForeground,
            "PopupMenu.foreground", menuForeground,
            "PopupMenu.background", menuBackground,
            "PopupMenu.selectionBackground", menuSelectionBackground,
            "RadioButton.disabledForeground", disabledForeground,
            "RadioButton.background", controlBackground,
            "RadioButton.foreground", controlForeground,
            //"RadioButton.shadow", ???,
            //"RadioButton.darkShadow", ???,
            //"RadioButton.light", ???,
            //"RadioButton.highlight", ???,

            "RadioButtonMenuItem.foreground", controlForeground,
            "RadioButtonMenuItem.selectionForeground", menuSelectionForeground,
            "RadioButtonMenuItem.background", menuBackground,
            "RadioButtonMenuItem.foreground", menuForeground,
            "RadioButtonMenuItem.acceleratorForeground", menuForeground,
            "RadioButtonMenuItem.acceleratorSelectionForeground", menuSelectionForeground,
            "RadioButtonMenuItem.selectionBackground", menuSelectionBackground,
            "RadioButtonMenuItem.selectionForeground", menuSelectionForeground,
            "RadioButtonMenuItem.disabledBackground", menuDisabledBackground,
            "RadioButtonMenuItem.disabledForeground", menuDisabledForeground,
            "RootPane.background", controlBackground,
            "ScrollBar.background", controlBackground,
            "ScrollBar.foreground", controlForeground,
            //"ScrollBar.track", ???,
            //"ScrollBar.trackHighlight", ???,
            //"ScrollBar.thumb", ???,
            //"ScrollBar.thumbHighlight", ???,
            //"ScrollBar.thumbDarkShadow", ???,
            //"ScrollBar.thumbShadow", ???,

            "ScrollPane.background", controlBackground,
            "ScrollPane.foreground", controlForeground,
            "Separator.background", controlBackground,
            "Separator.foreground", new ColorUIResource(0x808080),
            "Separator.highlight", new ColorUIResource(0xe0e0e0),
            "Separator.shadow", new ColorUIResource(0x808080),
            "Slider.background", controlBackground,
            "Slider.foreground", controlForeground,
            //"Slider.highlight", ???,
            //"Slider.shadow", ???,
            //"Slider.focus", ???,

            "Spinner.background", textBackground,
            "Spinner.foreground", controlForeground,
            "Spinner.borderPainted", Boolean.TRUE,
            "SplitPane.background", controlBackground,
            "SplitPane.foreground", controlForeground,
            //"SplitPane.highlight", ???,
            //"SplitPane.darkHighlight", ???,
            //"SplitPane.shadow", ???,
            //"SplitPane.darkShadow", ???,
            "SplitPaneDivider.draggingColor", new AlphaColorUIResource(0xa0666666),
            "TabbedPane.background", controlBackground,
            "TabbedPane.disabledForeground", disabledForeground,
            "TabbedPane.foreground", controlForeground,
            "TabbedPane.wrap.background", controlBackground,
            "TabbedPane.wrap.disabledForeground", disabledForeground,
            "TabbedPane.wrap.foreground", controlForeground,
            "TabbedPane.wrap.contentBorder", makeImageBevelBorder(
            jaguarDir + "TabbedPane.contentBorder.png", new Insets(8, 7, 8, 7), new Insets(1, 3, 3, 3), false),
            "TabbedPane.wrapBarTopBorders", makeImageBevelBorders(
            jaguarDir + "TabbedPane.wrapBarsTop.png", new Insets(0, 1, 0, 1), 3, true),
            "TabbedPane.wrapBarBottomBorders", makeImageBevelBorders(
            jaguarDir + "TabbedPane.wrapBarsBottom.png", new Insets(0, 1, 0, 1), 3, false),
            "TabbedPane.wrapBarRightBorders", makeImageBevelBorders(
            jaguarDir + "TabbedPane.wrapBarsRight.png", new Insets(1, 0, 1, 0), 3, true),
            "TabbedPane.wrapBarLeftBorders", makeImageBevelBorders(
            jaguarDir + "TabbedPane.wrapBarsLeft.png", new Insets(1, 0, 1, 0), 3, true),
            "TabbedPane.scroll.background", controlBackground,
            "TabbedPane.scroll.disabledForeground", disabledForeground,
            "TabbedPane.scroll.foreground", controlForeground,
            "Table.alternateBackground.0", listAlternateBackground,
            "Table.alternateBackground.1", listBackground,
            "Table.focusCellBackground", listBackground,
            "Table.focusCellForeground", listForeground,
            "Table.background", listBackground,
            "Table.foreground", listForeground,
            "Table.selectionBackground", listSelectionBackground,
            "Table.selectionForeground", listSelectionForeground,
            "Table.gridColor", new AlphaColorUIResource(0x33000000),
            "Table.focusCellForeground", listSelectionForeground,
            "Table.focusCellBackground", listSelectionBackground,
            "TableHeader.background", controlBackground,
            "TableHeader.foreground", controlForeground,
            "TextArea.background", textBackground,
            "TextArea.foreground", textForeground,
            "TextArea.inactiveForeground", disabledForeground,
            "TextArea.selectionBackground", textSelectionBackground,
            "TextArea.selectionForeground", textForeground,
            "TextField.background", textBackground,
            "TextField.foreground", textForeground,
            "TextField.inactiveBackground", textBackground,
            "TextField.inactiveForeground", disabledForeground,
            "TextField.inactiveSelectionBackground", inactiveSelectionBackground,
            "TextField.selectionBackground", textSelectionBackground,
            "TextField.selectionForeground", textForeground,
            "TextPane.background", textBackground,
            "TextPane.foreground", textForeground,
            "TextPane.inactiveForeground", disabledForeground,
            "TextPane.selectionBackground", textSelectionBackground,
            "TextPane.selectionForeground", textForeground,
            "ToggleButton.background", controlBackground,
            "ToggleButton.disabledForeground", disabledForeground,
            "ToggleButton.foreground", controlForeground,
            //"ToggleButton.shadow", ???,
            //"ToggleButton.darkShadow", ???,
            //"ToggleButton.light", ???,
            //"ToggleButton.highlight", ???,

            //"ToolBar.background", table.get("control"),
            //"ToolBar.foreground", table.get("controlText"),
            //"ToolBar.shadow", table.getColor("controlShadow"),
            //"ToolBar.darkShadow", table.getColor("controlDkShadow"),
            //"ToolBar.light", table.getColor("controlHighlight"),
            //"ToolBar.highlight", table.getColor("controlLtHighlight"),
            //"ToolBar.dockingBackground", table.get("control"),
            //"ToolBar.floatingBackground", table.get("control"),
            "ToolBar.dockingForeground", listSelectionBackground,
            "ToolBar.floatingForeground", new AlphaColorUIResource(0x00000000),
            "ToolTip.foreground", table.get("infoText"),
            "ToolTip.background", table.get("info"),
            "Tree.alternateBackground.0", listAlternateBackground,
            "Tree.alternateBackground.1", listBackground,
            "Tree.selectionBackground", listSelectionBackground,
            "Tree.selectionBorderColor", listSelectionBorderColor,
            "Tree.selectionForeground", listSelectionForeground,
            "Tree.controlForeground", listForeground,
            "Tree.textBackground", translucentColor,
            "Tree.textForeground", listForeground,
            "Viewport.background", listBackground,
            "Viewport.foreground", listForeground,};
        putDefaults(table, uiDefaults);
    }

    protected void initInputMapDefaults(UIDefaults table) {
        // Input map for text fields
        Object fieldInputMap = new UIDefaults.LazyInputMap(new String[]{
                    //, DefaultEditorKit.insertContentAction,
                    //, DefaultEditorKit.insertBreakAction,
                    //, DefaultEditorKit.insertTabAction,
                    "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                    "DELETE", DefaultEditorKit.deleteNextCharAction,
                    //, DefaultEditorKit.readOnlyAction,
                    //, DefaultEditorKit.writableAction,
                    "meta X", DefaultEditorKit.cutAction,
                    "meta C", DefaultEditorKit.copyAction,
                    "meta V", DefaultEditorKit.pasteAction,
                    "CUT", DefaultEditorKit.cutAction,
                    "COPY", DefaultEditorKit.copyAction,
                    "PASTE", DefaultEditorKit.pasteAction,
                    //, DefaultEditorKit.beepAction,
                    //, DefaultEditorKit.pageUpAction,
                    //, DefaultEditorKit.pageDownAction,
                    //, DefaultEditorKit.selectionPageUpAction,
                    //, DefaultEditorKit.selectionPageDownAction,
                    //, DefaultEditorKit.selectionPageLeftAction,
                    //, DefaultEditorKit.selectionPageRightAction,
                    "RIGHT", DefaultEditorKit.forwardAction,
                    "KP_RIGHT", DefaultEditorKit.forwardAction,
                    "LEFT", DefaultEditorKit.backwardAction,
                    "KP_LEFT", DefaultEditorKit.backwardAction,
                    "shift RIGHT", DefaultEditorKit.selectionForwardAction,
                    "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
                    "shift LEFT", DefaultEditorKit.selectionBackwardAction,
                    "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
                    "UP", DefaultEditorKit.upAction,
                    "KP_UP", DefaultEditorKit.upAction,
                    "DOWN", DefaultEditorKit.downAction,
                    "KP_DOWN", DefaultEditorKit.downAction,
                    "shift UP", DefaultEditorKit.selectionUpAction,
                    "shift KP_UP", DefaultEditorKit.selectionUpAction,
                    "shift DOWN", DefaultEditorKit.selectionDownAction,
                    "shift KP_DOWN", DefaultEditorKit.selectionDownAction,
                    //, DefaultEditorKit.beginWordAction,
                    //, DefaultEditorKit.endWordAction,
                    //, DefaultEditorKit.selectionBeginWordAction,
                    //, DefaultEditorKit.selectionEndWordAction,
                    "alt LEFT", DefaultEditorKit.previousWordAction,
                    "alt KP_LEFT", DefaultEditorKit.previousWordAction,
                    "alt RIGHT", DefaultEditorKit.nextWordAction,
                    "alt KP_RIGHT", DefaultEditorKit.nextWordAction,
                    "alt shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
                    "alt shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
                    "alt shift RIGHT", DefaultEditorKit.selectionNextWordAction,
                    "alt shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
                    "alt UP", DefaultEditorKit.beginLineAction,
                    "alt KP_UP", DefaultEditorKit.beginLineAction,
                    "ctrl LEFT", DefaultEditorKit.beginLineAction,
                    "ctrl KP_LEFT", DefaultEditorKit.beginLineAction,
                    "meta LEFT", DefaultEditorKit.beginLineAction,
                    "meta KP_LEFT", DefaultEditorKit.beginLineAction,
                    "alt DOWN", DefaultEditorKit.endLineAction,
                    "alt KP_DOWN", DefaultEditorKit.endLineAction,
                    "ctrl RIGHT", DefaultEditorKit.endLineAction,
                    "ctrl KP_RIGHT", DefaultEditorKit.endLineAction,
                    "meta RIGHT", DefaultEditorKit.endLineAction,
                    "meta KP_RIGHT", DefaultEditorKit.endLineAction,
                    "ctrl shift LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "ctrl shift KP_LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "meta shift LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "meta shift KP_LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "ctrl shift RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "ctrl shift KP_RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "meta shift RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "meta shift KP_RIGHT", DefaultEditorKit.selectionEndLineAction,
                    //, DefaultEditorKit.beginParagraphAction,
                    //, DefaultEditorKit.endParagraphAction,
                    //, DefaultEditorKit.selectionBeginParagraphAction,
                    //, DefaultEditorKit.selectionEndParagraphAction,
                    "HOME", DefaultEditorKit.beginAction,
                    "END", DefaultEditorKit.endAction,
                    "meta UP", DefaultEditorKit.beginAction,
                    "meta KP_UP", DefaultEditorKit.beginAction,
                    "meta DOWN", DefaultEditorKit.endAction,
                    "meta KP_DOWN", DefaultEditorKit.endAction,
                    "shift HOME", DefaultEditorKit.selectionBeginAction,
                    "shift END", DefaultEditorKit.selectionEndAction,
                    //, DefaultEditorKit.selectWordAction,
                    //, DefaultEditorKit.selectLineAction,
                    //, DefaultEditorKit.selectParagraphAction,
                    "meta A", DefaultEditorKit.selectAllAction,
                    "meta shift A", "unselect"/*DefaultEditorKit.unselectAction*/,
                    "controlBackground shift O", "toggle-componentOrientation", /*DefaultEditorKit.toggleComponentOrientation*/
                    "alt DELETE", QuaquaEditorKit.deleteNextWordAction,
                    "alt BACK_SPACE", QuaquaEditorKit.deletePrevWordAction,
                    "ENTER", JTextField.notifyAction,});
        // Input map for password fields
        Object passwordFieldInputMap = new UIDefaults.LazyInputMap(new String[]{
                    //, DefaultEditorKit.insertContentAction,
                    //, DefaultEditorKit.insertBreakAction,
                    //, DefaultEditorKit.insertTabAction,
                    "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                    "DELETE", DefaultEditorKit.deleteNextCharAction,
                    //, DefaultEditorKit.readOnlyAction,
                    //, DefaultEditorKit.writableAction,
                    "meta X", DefaultEditorKit.beepAction,
                    "meta C", DefaultEditorKit.beepAction,
                    "meta V", DefaultEditorKit.pasteAction,
                    "CUT", DefaultEditorKit.beepAction,
                    "COPY", DefaultEditorKit.beepAction,
                    "PASTE", DefaultEditorKit.pasteAction,
                    //, DefaultEditorKit.beepAction,
                    //, DefaultEditorKit.pageUpAction,
                    //, DefaultEditorKit.pageDownAction,
                    //, DefaultEditorKit.selectionPageUpAction,
                    //, DefaultEditorKit.selectionPageDownAction,
                    //, DefaultEditorKit.selectionPageLeftAction,
                    //, DefaultEditorKit.selectionPageRightAction,
                    "RIGHT", DefaultEditorKit.forwardAction,
                    "KP_RIGHT", DefaultEditorKit.forwardAction,
                    "LEFT", DefaultEditorKit.backwardAction,
                    "KP_LEFT", DefaultEditorKit.backwardAction,
                    "shift RIGHT", DefaultEditorKit.selectionForwardAction,
                    "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
                    "shift LEFT", DefaultEditorKit.selectionBackwardAction,
                    "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
                    "UP", DefaultEditorKit.upAction,
                    "KP_UP", DefaultEditorKit.upAction,
                    "DOWN", DefaultEditorKit.downAction,
                    "KP_DOWN", DefaultEditorKit.downAction,
                    "shift UP", DefaultEditorKit.selectionUpAction,
                    "shift KP_UP", DefaultEditorKit.selectionUpAction,
                    "shift DOWN", DefaultEditorKit.selectionDownAction,
                    "shift KP_DOWN", DefaultEditorKit.selectionDownAction,
                    //, DefaultEditorKit.beginWordAction,
                    //, DefaultEditorKit.endWordAction,
                    //, DefaultEditorKit.selectionBeginWordAction,
                    //, DefaultEditorKit.selectionEndWordAction,
                    "alt LEFT", DefaultEditorKit.previousWordAction,
                    "alt KP_LEFT", DefaultEditorKit.previousWordAction,
                    "alt RIGHT", DefaultEditorKit.nextWordAction,
                    "alt KP_RIGHT", DefaultEditorKit.nextWordAction,
                    "alt shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
                    "alt shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
                    "alt shift RIGHT", DefaultEditorKit.selectionNextWordAction,
                    "alt shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
                    "alt UP", DefaultEditorKit.beginLineAction,
                    "alt KP_UP", DefaultEditorKit.beginLineAction,
                    "ctrl LEFT", DefaultEditorKit.beginLineAction,
                    "ctrl KP_LEFT", DefaultEditorKit.beginLineAction,
                    "meta LEFT", DefaultEditorKit.beginLineAction,
                    "meta KP_LEFT", DefaultEditorKit.beginLineAction,
                    "alt DOWN", DefaultEditorKit.endLineAction,
                    "alt KP_DOWN", DefaultEditorKit.endLineAction,
                    "ctrl RIGHT", DefaultEditorKit.endLineAction,
                    "ctrl KP_RIGHT", DefaultEditorKit.endLineAction,
                    "meta RIGHT", DefaultEditorKit.endLineAction,
                    "meta KP_RIGHT", DefaultEditorKit.endLineAction,
                    "ctrl shift LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "ctrl shift KP_LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "meta shift LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "meta shift KP_LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "ctrl shift RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "ctrl shift KP_RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "meta shift RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "meta shift KP_RIGHT", DefaultEditorKit.selectionEndLineAction,
                    //, DefaultEditorKit.beginParagraphAction,
                    //, DefaultEditorKit.endParagraphAction,
                    //, DefaultEditorKit.selectionBeginParagraphAction,
                    //, DefaultEditorKit.selectionEndParagraphAction,
                    "HOME", DefaultEditorKit.beginAction,
                    "END", DefaultEditorKit.endAction,
                    "meta UP", DefaultEditorKit.beginAction,
                    "meta KP_UP", DefaultEditorKit.beginAction,
                    "meta DOWN", DefaultEditorKit.endAction,
                    "meta KP_DOWN", DefaultEditorKit.endAction,
                    "shift HOME", DefaultEditorKit.selectionBeginAction,
                    "shift END", DefaultEditorKit.selectionEndAction,
                    //, DefaultEditorKit.selectWordAction,
                    //, DefaultEditorKit.selectLineAction,
                    //, DefaultEditorKit.selectParagraphAction,
                    "meta A", DefaultEditorKit.selectAllAction,
                    "meta shift A", "unselect"/*DefaultEditorKit.unselectAction*/,
                    "controlBackground shift O", "toggle-componentOrientation", /*DefaultEditorKit.toggleComponentOrientation*/
                    "alt DELETE", QuaquaEditorKit.deleteNextWordAction,
                    "alt BACK_SPACE", QuaquaEditorKit.deletePrevWordAction,
                    "ENTER", JTextField.notifyAction,});
        // Input map for spinner editors
        Object spinnerInputMap = new UIDefaults.LazyInputMap(new String[]{
                    //, DefaultEditorKit.insertContentAction,
                    //, DefaultEditorKit.insertBreakAction,
                    //, DefaultEditorKit.insertTabAction,
                    "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                    "DELETE", DefaultEditorKit.deleteNextCharAction,
                    //, DefaultEditorKit.readOnlyAction,
                    //, DefaultEditorKit.writableAction,
                    "meta X", DefaultEditorKit.cutAction,
                    "meta C", DefaultEditorKit.copyAction,
                    "meta V", DefaultEditorKit.pasteAction,
                    "CUT", DefaultEditorKit.cutAction,
                    "COPY", DefaultEditorKit.copyAction,
                    "PASTE", DefaultEditorKit.pasteAction,
                    //, DefaultEditorKit.beepAction,
                    //, DefaultEditorKit.pageUpAction,
                    //, DefaultEditorKit.pageDownAction,
                    //, DefaultEditorKit.selectionPageUpAction,
                    //, DefaultEditorKit.selectionPageDownAction,
                    //, DefaultEditorKit.selectionPageLeftAction,
                    //, DefaultEditorKit.selectionPageRightAction,
                    "RIGHT", DefaultEditorKit.forwardAction,
                    "KP_RIGHT", DefaultEditorKit.forwardAction,
                    "LEFT", DefaultEditorKit.backwardAction,
                    "KP_LEFT", DefaultEditorKit.backwardAction,
                    "shift RIGHT", DefaultEditorKit.selectionForwardAction,
                    "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
                    "shift LEFT", DefaultEditorKit.selectionBackwardAction,
                    "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
                    //"UP", DefaultEditorKit.upAction,
                    //"KP_UP", DefaultEditorKit.upAction,
                    //"DOWN", DefaultEditorKit.downAction,
                    //"KP_DOWN", DefaultEditorKit.downAction,
                    "UP", "increment",
                    "KP_UP", "increment",
                    "DOWN", "decrement",
                    "KP_DOWN", "decrement",
                    "shift UP", DefaultEditorKit.selectionUpAction,
                    "shift KP_UP", DefaultEditorKit.selectionUpAction,
                    "shift DOWN", DefaultEditorKit.selectionDownAction,
                    "shift KP_DOWN", DefaultEditorKit.selectionDownAction,
                    //, DefaultEditorKit.beginWordAction,
                    //, DefaultEditorKit.endWordAction,
                    //, DefaultEditorKit.selectionBeginWordAction,
                    //, DefaultEditorKit.selectionEndWordAction,
                    "alt LEFT", DefaultEditorKit.previousWordAction,
                    "alt KP_LEFT", DefaultEditorKit.previousWordAction,
                    "alt RIGHT", DefaultEditorKit.nextWordAction,
                    "alt KP_RIGHT", DefaultEditorKit.nextWordAction,
                    "alt shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
                    "alt shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
                    "alt shift RIGHT", DefaultEditorKit.selectionNextWordAction,
                    "alt shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
                    "alt UP", DefaultEditorKit.beginLineAction,
                    "alt KP_UP", DefaultEditorKit.beginLineAction,
                    "ctrl LEFT", DefaultEditorKit.beginLineAction,
                    "ctrl KP_LEFT", DefaultEditorKit.beginLineAction,
                    "meta LEFT", DefaultEditorKit.beginLineAction,
                    "meta KP_LEFT", DefaultEditorKit.beginLineAction,
                    "alt DOWN", DefaultEditorKit.endLineAction,
                    "alt KP_DOWN", DefaultEditorKit.endLineAction,
                    "ctrl RIGHT", DefaultEditorKit.endLineAction,
                    "ctrl KP_RIGHT", DefaultEditorKit.endLineAction,
                    "meta RIGHT", DefaultEditorKit.endLineAction,
                    "meta KP_RIGHT", DefaultEditorKit.endLineAction,
                    "ctrl shift LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "ctrl shift KP_LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "meta shift LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "meta shift KP_LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "ctrl shift RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "ctrl shift KP_RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "meta shift RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "meta shift KP_RIGHT", DefaultEditorKit.selectionEndLineAction,
                    //, DefaultEditorKit.beginParagraphAction,
                    //, DefaultEditorKit.endParagraphAction,
                    //, DefaultEditorKit.selectionBeginParagraphAction,
                    //, DefaultEditorKit.selectionEndParagraphAction,
                    "HOME", DefaultEditorKit.beginAction,
                    "END", DefaultEditorKit.endAction,
                    "meta UP", DefaultEditorKit.beginAction,
                    "meta KP_UP", DefaultEditorKit.beginAction,
                    "meta DOWN", DefaultEditorKit.endAction,
                    "meta KP_DOWN", DefaultEditorKit.endAction,
                    "shift HOME", DefaultEditorKit.selectionBeginAction,
                    "shift END", DefaultEditorKit.selectionEndAction,
                    //, DefaultEditorKit.selectWordAction,
                    //, DefaultEditorKit.selectLineAction,
                    //, DefaultEditorKit.selectParagraphAction,
                    "meta A", DefaultEditorKit.selectAllAction,
                    "meta shift A", "unselect"/*DefaultEditorKit.unselectAction*/,
                    "controlBackground shift O", "toggle-componentOrientation", /*DefaultEditorKit.toggleComponentOrientation*/
                    "alt DELETE", QuaquaEditorKit.deleteNextWordAction,
                    "alt BACK_SPACE", QuaquaEditorKit.deletePrevWordAction,
                    "ENTER", JTextField.notifyAction,});
        // Input map for multiline text fields
        Object multilineInputMap = new UIDefaults.LazyInputMap(new String[]{
                    //, DefaultEditorKit.insertContentAction,
                    "shift ENTER", DefaultEditorKit.insertBreakAction,
                    "alt ENTER", DefaultEditorKit.insertBreakAction,
                    "ENTER", DefaultEditorKit.insertBreakAction,
                    "TAB", DefaultEditorKit.insertTabAction,
                    "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                    "DELETE", DefaultEditorKit.deleteNextCharAction,
                    //, DefaultEditorKit.readOnlyAction,
                    //, DefaultEditorKit.writableAction,
                    "meta X", DefaultEditorKit.cutAction,
                    "meta C", DefaultEditorKit.copyAction,
                    "meta V", DefaultEditorKit.pasteAction,
                    "CUT", DefaultEditorKit.cutAction,
                    "COPY", DefaultEditorKit.copyAction,
                    "PASTE", DefaultEditorKit.pasteAction,
                    //, DefaultEditorKit.beepAction,
                    "PAGE_UP", DefaultEditorKit.pageUpAction,
                    "PAGE_DOWN", DefaultEditorKit.pageDownAction,
                    "shift PAGE_UP", "selection-page-up",
                    "shift PAGE_DOWN", "selection-page-down",
                    "ctrl shift PAGE_UP", "selection-page-left",
                    "ctrl shift PAGE_DOWN", "selection-page-right",
                    "RIGHT", DefaultEditorKit.forwardAction,
                    "KP_RIGHT", DefaultEditorKit.forwardAction,
                    "LEFT", DefaultEditorKit.backwardAction,
                    "KP_LEFT", DefaultEditorKit.backwardAction,
                    "shift RIGHT", DefaultEditorKit.selectionForwardAction,
                    "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
                    "shift LEFT", DefaultEditorKit.selectionBackwardAction,
                    "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
                    "UP", DefaultEditorKit.upAction,
                    "KP_UP", DefaultEditorKit.upAction,
                    "DOWN", DefaultEditorKit.downAction,
                    "KP_DOWN", DefaultEditorKit.downAction,
                    "shift UP", DefaultEditorKit.selectionUpAction,
                    "shift KP_UP", DefaultEditorKit.selectionUpAction,
                    "shift DOWN", DefaultEditorKit.selectionDownAction,
                    "shift KP_DOWN", DefaultEditorKit.selectionDownAction,
                    //, DefaultEditorKit.beginWordAction,
                    //, DefaultEditorKit.endWordAction,
                    //, DefaultEditorKit.selectionBeginWordAction,
                    //, DefaultEditorKit.selectionEndWordAction,
                    "alt LEFT", DefaultEditorKit.previousWordAction,
                    "alt KP_LEFT", DefaultEditorKit.previousWordAction,
                    "alt RIGHT", DefaultEditorKit.nextWordAction,
                    "alt KP_RIGHT", DefaultEditorKit.nextWordAction,
                    "alt shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
                    "alt shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
                    "alt shift RIGHT", DefaultEditorKit.selectionNextWordAction,
                    "alt shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
                    "alt UP", DefaultEditorKit.beginLineAction,
                    "alt KP_UP", DefaultEditorKit.beginLineAction,
                    "ctrl LEFT", DefaultEditorKit.beginLineAction,
                    "ctrl KP_LEFT", DefaultEditorKit.beginLineAction,
                    "meta LEFT", DefaultEditorKit.beginLineAction,
                    "meta KP_LEFT", DefaultEditorKit.beginLineAction,
                    "alt DOWN", DefaultEditorKit.endLineAction,
                    "alt KP_DOWN", DefaultEditorKit.endLineAction,
                    "ctrl RIGHT", DefaultEditorKit.endLineAction,
                    "ctrl KP_RIGHT", DefaultEditorKit.endLineAction,
                    "meta RIGHT", DefaultEditorKit.endLineAction,
                    "meta KP_RIGHT", DefaultEditorKit.endLineAction,
                    "ctrl shift LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "ctrl shift KP_LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "meta shift LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "meta shift KP_LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "ctrl shift RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "ctrl shift KP_RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "meta shift RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "meta shift KP_RIGHT", DefaultEditorKit.selectionEndLineAction,
                    //, DefaultEditorKit.beginParagraphAction,
                    //, DefaultEditorKit.endParagraphAction,
                    //, DefaultEditorKit.selectionBeginParagraphAction,
                    //, DefaultEditorKit.selectionEndParagraphAction,
                    "HOME", DefaultEditorKit.beginAction,
                    "END", DefaultEditorKit.endAction,
                    "meta UP", DefaultEditorKit.beginAction,
                    "meta KP_UP", DefaultEditorKit.beginAction,
                    "meta DOWN", DefaultEditorKit.endAction,
                    "meta KP_DOWN", DefaultEditorKit.endAction,
                    "shift HOME", DefaultEditorKit.selectionBeginAction,
                    "shift END", DefaultEditorKit.selectionEndAction,
                    //, DefaultEditorKit.selectWordAction,
                    //, DefaultEditorKit.selectLineAction,
                    //, DefaultEditorKit.selectParagraphAction,
                    "meta A", DefaultEditorKit.selectAllAction,
                    "meta shift A", "unselect"/*DefaultEditorKit.unselectAction*/,
                    "controlBackground shift O", "toggle-componentOrientation", /*DefaultEditorKit.toggleComponentOrientation*/
                    "alt DELETE", QuaquaEditorKit.deleteNextWordAction,
                    "alt BACK_SPACE", QuaquaEditorKit.deletePrevWordAction,});

        // Input map for the editors of combo boxes
        Object comboEditorInputMap = new UIDefaults.LazyInputMap(new String[]{
                    //, DefaultEditorKit.insertContentAction,
                    //, DefaultEditorKit.insertBreakAction,
                    //, DefaultEditorKit.insertTabAction,
                    "BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
                    "DELETE", DefaultEditorKit.deleteNextCharAction,
                    //, DefaultEditorKit.readOnlyAction,
                    //, DefaultEditorKit.writableAction,
                    "meta X", DefaultEditorKit.cutAction,
                    "meta C", DefaultEditorKit.copyAction,
                    "meta V", DefaultEditorKit.pasteAction,
                    "CUT", DefaultEditorKit.cutAction,
                    "COPY", DefaultEditorKit.copyAction,
                    "PASTE", DefaultEditorKit.pasteAction,
                    //, DefaultEditorKit.beepAction,
                    //, DefaultEditorKit.pageUpAction,
                    //, DefaultEditorKit.pageDownAction,
                    //, DefaultEditorKit.selectionPageUpAction,
                    //, DefaultEditorKit.selectionPageDownAction,
                    //, DefaultEditorKit.selectionPageLeftAction,
                    //, DefaultEditorKit.selectionPageRightAction,
                    "RIGHT", DefaultEditorKit.forwardAction,
                    "KP_RIGHT", DefaultEditorKit.forwardAction,
                    "LEFT", DefaultEditorKit.backwardAction,
                    "KP_LEFT", DefaultEditorKit.backwardAction,
                    "shift RIGHT", DefaultEditorKit.selectionForwardAction,
                    "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
                    "shift LEFT", DefaultEditorKit.selectionBackwardAction,
                    "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
                    //"UP", DefaultEditorKit.upAction,
                    //"DOWN", DefaultEditorKit.downAction,
                    //"shift UP", DefaultEditorKit.selectionUpAction,
                    //"shift DOWN", DefaultEditorKit.selectionDownAction,
                    //, DefaultEditorKit.beginWordAction,
                    //, DefaultEditorKit.endWordAction,
                    //, DefaultEditorKit.selectionBeginWordAction,
                    //, DefaultEditorKit.selectionEndWordAction,
                    "alt LEFT", DefaultEditorKit.previousWordAction,
                    "alt KP_LEFT", DefaultEditorKit.previousWordAction,
                    "alt RIGHT", DefaultEditorKit.nextWordAction,
                    "alt KP_RIGHT", DefaultEditorKit.nextWordAction,
                    "alt shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
                    "alt shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
                    "alt shift RIGHT", DefaultEditorKit.selectionNextWordAction,
                    "alt shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
                    //"alt UP", DefaultEditorKit.beginLineAction,
                    "ctrl LEFT", DefaultEditorKit.beginLineAction,
                    "meta LEFT", DefaultEditorKit.beginLineAction,
                    //"alt DOWN", DefaultEditorKit.endLineAction,
                    "ctrl RIGHT", DefaultEditorKit.endLineAction,
                    "meta RIGHT", DefaultEditorKit.endLineAction,
                    "ctrl shift LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "meta shift LEFT", DefaultEditorKit.selectionBeginLineAction,
                    "ctrl shift RIGHT", DefaultEditorKit.selectionEndLineAction,
                    "meta shift RIGHT", DefaultEditorKit.selectionEndLineAction,
                    //, DefaultEditorKit.beginParagraphAction,
                    //, DefaultEditorKit.endParagraphAction,
                    //, DefaultEditorKit.selectionBeginParagraphAction,
                    //, DefaultEditorKit.selectionEndParagraphAction,
                    //"HOME", DefaultEditorKit.beginAction,
                    //"END", DefaultEditorKit.endAction,
                    //"meta UP", DefaultEditorKit.beginAction,
                    //"meta DOWN", DefaultEditorKit.endAction,
                    "shift HOME", DefaultEditorKit.selectionBeginAction,
                    "shift END", DefaultEditorKit.selectionEndAction,
                    //, DefaultEditorKit.selectWordAction,
                    //, DefaultEditorKit.selectLineAction,
                    //, DefaultEditorKit.selectParagraphAction,
                    "meta A", DefaultEditorKit.selectAllAction,
                    "meta shift A", "unselect"/*DefaultEditorKit.unselectAction*/,
                    "controlBackground shift O", "toggle-componentOrientation", /*DefaultEditorKit.toggleComponentOrientation*/
                    "alt DELETE", QuaquaEditorKit.deleteNextWordAction,
                    "alt BACK_SPACE", QuaquaEditorKit.deletePrevWordAction,
                    "ENTER", JTextField.notifyAction,});

        UIDefaults.LazyInputMap tabbedPaneFocusInputMap =
                new UIDefaults.LazyInputMap(new Object[]{
                    "RIGHT", "navigateRight",
                    "KP_RIGHT", "navigateRight",
                    "LEFT", "navigateLeft",
                    "KP_LEFT", "navigateLeft",
                    "UP", "navigateUp",
                    "KP_UP", "navigateUp",
                    "DOWN", "navigateDown",
                    "KP_DOWN", "navigateDown",
                    "meta DOWN", "requestFocusForVisibleComponent",
                    "meta KP_DOWN", "requestFocusForVisibleComponent",});
        UIDefaults.LazyInputMap tabbedPaneAncestorInputMap =
                new UIDefaults.LazyInputMap(new Object[]{
                    "meta PAGE_DOWN", "navigatePageDown",
                    "meta PAGE_UP", "navigatePageUp",
                    "meta UP", "requestFocus",
                    "meta KP_UP", "requestFocus",});

        UIDefaults.LazyInputMap tableAncestorInputMap =
                new UIDefaults.LazyInputMap(new Object[]{
                    "meta C", "copy",
                    "meta V", "paste",
                    "meta X", "cut",
                    "COPY", "copy",
                    "PASTE", "paste",
                    "CUT", "cut",
                    "RIGHT", "selectNextColumn",
                    "KP_RIGHT", "selectNextColumn",
                    "shift RIGHT", "selectNextColumnExtendSelection",
                    "shift KP_RIGHT", "selectNextColumnExtendSelection",
                    "meta shift RIGHT", "selectNextColumnExtendSelection",
                    "meta shift KP_RIGHT", "selectNextColumnExtendSelection",
                    "meta RIGHT", "selectNextColumnChangeLead",
                    "meta KP_RIGHT", "selectNextColumnChangeLead",
                    "LEFT", "selectPreviousColumn",
                    "KP_LEFT", "selectPreviousColumn",
                    "shift LEFT", "selectPreviousColumnExtendSelection",
                    "shift KP_LEFT", "selectPreviousColumnExtendSelection",
                    "meta shift LEFT", "selectPreviousColumnExtendSelection",
                    "meta shift KP_LEFT", "selectPreviousColumnExtendSelection",
                    "meta LEFT", "selectPreviousColumnChangeLead",
                    "meta KP_LEFT", "selectPreviousColumnChangeLead",
                    "DOWN", "selectNextRow",
                    "KP_DOWN", "selectNextRow",
                    "shift DOWN", "selectNextRowExtendSelection",
                    "shift KP_DOWN", "selectNextRowExtendSelection",
                    "meta shift DOWN", "selectNextRowExtendSelection",
                    "meta shift KP_DOWN", "selectNextRowExtendSelection",
                    "meta DOWN", "selectNextRowChangeLead",
                    "meta KP_DOWN", "selectNextRowChangeLead",
                    "UP", "selectPreviousRow",
                    "KP_UP", "selectPreviousRow",
                    "shift UP", "selectPreviousRowExtendSelection",
                    "shift KP_UP", "selectPreviousRowExtendSelection",
                    "meta shift UP", "selectPreviousRowExtendSelection",
                    "meta shift KP_UP", "selectPreviousRowExtendSelection",
                    "meta UP", "selectPreviousRowChangeLead",
                    "meta KP_UP", "selectPreviousRowChangeLead",
                    "HOME", "selectFirstColumn",
                    "shift HOME", "selectFirstColumnExtendSelection",
                    "meta shift HOME", "selectFirstRowExtendSelection",
                    "meta HOME", "selectFirstRow",
                    "END", "selectLastColumn",
                    "shift END", "selectLastColumnExtendSelection",
                    "meta shift END", "selectLastRowExtendSelection",
                    "meta END", "selectLastRow",
                    "PAGE_UP", "scrollUpChangeSelection",
                    "shift PAGE_UP", "scrollUpExtendSelection",
                    "meta shift PAGE_UP", "scrollLeftExtendSelection",
                    "meta PAGE_UP", "scrollLeftChangeSelection",
                    "PAGE_DOWN", "scrollDownChangeSelection",
                    "shift PAGE_DOWN", "scrollDownExtendSelection",
                    "meta shift PAGE_DOWN", "scrollRightExtendSelection",
                    "meta PAGE_DOWN", "scrollRightChangeSelection",
                    "TAB", "selectNextColumnCell",
                    "shift TAB", "selectPreviousColumnCell",
                    "ENTER", "selectNextRowCell",
                    "shift ENTER", "selectPreviousRowCell",
                    "meta A", "selectAll",
                    "meta shift A", "clearSelection",
                    "ESCAPE", "cancel",
                    "meta PERIOD", "cancel",
                    "F2", "startEditing",
                    "SPACE", "addToSelection",
                    "meta SPACE", "toggleAndAnchor",
                    "shift SPACE", "extendTo",
                    "meta shift SPACE", "moveSelectionTo"
                });

        UIDefaults.LazyInputMap tableAncestorInputMapRightToLeft =
                new UIDefaults.LazyInputMap(new Object[]{
                    "RIGHT", "selectPreviousColumn",
                    "KP_RIGHT", "selectPreviousColumn",
                    "shift RIGHT", "selectPreviousColumnExtendSelection",
                    "shift KP_RIGHT", "selectPreviousColumnExtendSelection",
                    "meta shift RIGHT", "selectPreviousColumnExtendSelection",
                    "meta shift KP_RIGHT", "selectPreviousColumnExtendSelection",
                    "shift RIGHT", "selectPreviousColumnChangeLead",
                    "shift KP_RIGHT", "selectPreviousColumnChangeLead",
                    "LEFT", "selectNextColumn",
                    "KP_LEFT", "selectNextColumn",
                    "shift LEFT", "selectNextColumnExtendSelection",
                    "shift KP_LEFT", "selectNextColumnExtendSelection",
                    "meta shift LEFT", "selectNextColumnExtendSelection",
                    "meta shift KP_LEFT", "selectNextColumnExtendSelection",
                    "meta LEFT", "selectNextColumnChangeLead",
                    "meta KP_LEFT", "selectNextColumnChangeLead",
                    "meta PAGE_UP", "scrollRightChangeSelection",
                    "meta PAGE_DOWN", "scrollLeftChangeSelection",
                    "meta shift PAGE_UP", "scrollRightExtendSelection",
                    "meta shift PAGE_DOWN", "scrollLeftExtendSelection",});

        // Assign the defaults
        Object[] uiDefaults = {
            //"Button.focusInputMap", ...,

            //"CheckBox.focusInputMap", ...,

            //"ComboBox.actionMap", ...,
            "ComboBox.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{
                "ESCAPE", "hidePopup",
                "meta PERIOD", "hidePopup",
                "PAGE_UP", "pageUpPassThrough",
                "PAGE_DOWN", "pageDownPassThrough",
                "HOME", "homePassThrough",
                "END", "endPassThrough",
                "DOWN", "selectNext",
                "KP_DOWN", "selectNext",
                "alt DOWN", "togglePopup",
                "alt KP_DOWN", "togglePopup",
                "alt UP", "togglePopup",
                "alt KP_UP", "togglePopup",
                "SPACE", "spacePopup",
                "ENTER", "enterPressed",
                "UP", "selectPrevious",
                "KP_UP", "selectPrevious"
            }),
            "ComboBox.editorInputMap", comboEditorInputMap,
            //"Desktop.ancestorInputMap", ...,

            "EditorPane.focusInputMap", multilineInputMap,
            //
            "FileChooser.ancestorInputMap",
            new UIDefaults.LazyInputMap(new Object[]{
                "ESCAPE", "cancelSelection",
                "meta PERIOD", "cancelSelection",
                "F5", "refresh",}),
            //
            "FormattedTextField.focusInputMap", fieldInputMap,
            "FormattedTextField.keyBindings", null,
            //
            "List.focusInputMap",
            new UIDefaults.LazyInputMap(new Object[]{
                "meta C", "copy",
                "meta V", "paste",
                "meta X", "cut",
                "COPY", "copy",
                "PASTE", "paste",
                "CUT", "cut",
                "control INSERT", "copy",
                "shift INSERT", "paste",
                "shift DELETE", "cut",
                "UP", "selectPreviousRow",
                "KP_UP", "selectPreviousRow",
                "shift UP", "selectPreviousRowExtendSelection",
                "shift KP_UP", "selectPreviousRowExtendSelection",
                "meta shift UP", "selectPreviousRowExtendSelection",
                "meta shift KP_UP", "selectPreviousRowExtendSelection",
                "meta UP", "selectPreviousRowChangeLead",
                "meta KP_UP", "selectPreviousRowChangeLead",
                "DOWN", "selectNextRow",
                "KP_DOWN", "selectNextRow",
                "shift DOWN", "selectNextRowExtendSelection",
                "shift KP_DOWN", "selectNextRowExtendSelection",
                "meta shift DOWN", "selectNextRowExtendSelection",
                "meta shift KP_DOWN", "selectNextRowExtendSelection",
                "meta DOWN", "selectNextRowChangeLead",
                "meta KP_DOWN", "selectNextRowChangeLead",
                "LEFT", "selectPreviousColumn",
                "KP_LEFT", "selectPreviousColumn",
                "shift LEFT", "selectPreviousColumnExtendSelection",
                "shift KP_LEFT", "selectPreviousColumnExtendSelection",
                "meta shift LEFT", "selectPreviousColumnExtendSelection",
                "meta shift KP_LEFT", "selectPreviousColumnExtendSelection",
                "meta LEFT", "selectPreviousColumnChangeLead",
                "meta KP_LEFT", "selectPreviousColumnChangeLead",
                "RIGHT", "selectNextColumn",
                "KP_RIGHT", "selectNextColumn",
                "shift RIGHT", "selectNextColumnExtendSelection",
                "shift KP_RIGHT", "selectNextColumnExtendSelection",
                "meta shift RIGHT", "selectNextColumnExtendSelection",
                "meta shift KP_RIGHT", "selectNextColumnExtendSelection",
                "meta RIGHT", "selectNextColumnChangeLead",
                "meta KP_RIGHT", "selectNextColumnChangeLead",
                "HOME", "selectFirstRow",
                "shift HOME", "selectFirstRowExtendSelection",
                "meta shift HOME", "selectFirstRowExtendSelection",
                "meta HOME", "selectFirstRowChangeLead",
                "END", "selectLastRow",
                "shift END", "selectLastRowExtendSelection",
                "meta shift END", "selectLastRowExtendSelection",
                "meta END", "selectLastRowChangeLead",
                "PAGE_UP", "scrollUp",
                "shift PAGE_UP", "scrollUpExtendSelection",
                "meta shift PAGE_UP", "scrollUpExtendSelection",
                "meta PAGE_UP", "scrollUpChangeLead",
                "PAGE_DOWN", "scrollDown",
                "shift PAGE_DOWN", "scrollDownExtendSelection",
                "meta shift PAGE_DOWN", "scrollDownExtendSelection",
                "meta PAGE_DOWN", "scrollDownChangeLead",
                "meta A", "selectAll",
                "meta SLASH", "selectAll",
                "meta shift A", "clearSelection",
                "meta BACK_SLASH", "clearSelection",
                "SPACE", "addToSelection",
                "meta SPACE", "toggleAndAnchor",
                "shift SPACE", "extendTo",
                "meta shift SPACE", "moveSelectionTo"
            }),
            "List.focusInputMap.RightToLeft",
            new UIDefaults.LazyInputMap(new Object[]{
                "LEFT", "selectNextColumn",
                "KP_LEFT", "selectNextColumn",
                "shift LEFT", "selectNextColumnExtendSelection",
                "shift KP_LEFT", "selectNextColumnExtendSelection",
                "meta shift LEFT", "selectNextColumnExtendSelection",
                "meta shift KP_LEFT", "selectNextColumnExtendSelection",
                "meta LEFT", "selectNextColumnChangeLead",
                "meta KP_LEFT", "selectNextColumnChangeLead",
                "RIGHT", "selectPreviousColumn",
                "KP_RIGHT", "selectPreviousColumn",
                "shift RIGHT", "selectPreviousColumnExtendSelection",
                "shift KP_RIGHT", "selectPreviousColumnExtendSelection",
                "meta shift RIGHT", "selectPreviousColumnExtendSelection",
                "meta shift KP_RIGHT", "selectPreviousColumnExtendSelection",
                "meta RIGHT", "selectPreviousColumnChangeLead",
                "meta KP_RIGHT", "selectPreviousColumnChangeLead",}),
            //
            "OptionPane.windowBindings", new Object[]{
                "ESCAPE", "close",
                "meta PERIOD", "close"},
            //
            "PasswordField.focusInputMap", passwordFieldInputMap,
            "PasswordField.keyBindings", null,
            //
            "RadioButton.focusInputMap",
            new UIDefaults.LazyInputMap(new Object[]{
                "SPACE", "pressed",
                "released SPACE", "released",
                "RETURN", "pressed",
                "UP", "selectPreviousButton",
                "KP_UP", "selectPreviousButton",
                "DOWN", "selectNextButton",
                "KP_DOWN", "selectNextButton",
                "LEFT", "selectPreviousButton",
                "KP_LEFT", "selectPreviousButton",
                "RIGHT", "selectNextButton",
                "KP_RIGHT", "selectNextButton",}),
            //
            // These bindings are only enabled when there is a default
            // button set on the RootPane.
            "RootPane.defaultButtonWindowKeyBindings", new Object[]{
                "ENTER", "press",
                "released ENTER", "release",
                "ctrl ENTER", "press",
                "ctrl released ENTER", "release"
            },
            "Spinner.ancestorInputMap",
            new UIDefaults.LazyInputMap(new Object[]{
                "UP", "increment",
                "KP_UP", "increment",
                "DOWN", "decrement",
                "KP_DOWN", "decrement",}),
            "Spinner.focusInputMap", spinnerInputMap,
            "TabbedPane.focusInputMap", tabbedPaneFocusInputMap,
            "TabbedPane.ancestorInputMap", tabbedPaneAncestorInputMap,
            "TabbedPane.actionMap", table.get("TabbedPane.actionMap"),
            "TabbedPane.wrap.focusInputMap", tabbedPaneFocusInputMap,
            "TabbedPane.wrap.ancestorInputMap", tabbedPaneAncestorInputMap,
            "TabbedPane.wrap.actionMap", table.get("TabbedPane.actionMap"),
            "TabbedPane.scroll.focusInputMap", tabbedPaneFocusInputMap,
            "TabbedPane.scroll.ancestorInputMap", tabbedPaneAncestorInputMap,
            "TabbedPane.scroll.actionMap", table.get("TabbedPane.actionMap"),
            //
            "Table.focusInputMap", tableAncestorInputMap,
            "Table.focusInputMap.rightToLeft", tableAncestorInputMapRightToLeft,
            "Table.ancestorInputMap", tableAncestorInputMap,
            "Table.ancestorInputMap.rightToLeft", tableAncestorInputMapRightToLeft,
            //
            "TextArea.focusInputMap", multilineInputMap,
            "TextArea.keyBindings", null,
            "TextField.focusInputMap", fieldInputMap,
            "TextField.keyBindings", null,
            "TextPane.focusInputMap", multilineInputMap,
            "TextPane.keyBindings", null,
            "Tree.focusInputMap",
            new UIDefaults.LazyInputMap(new Object[]{
                "meta C", "copy",
                "meta V", "paste",
                "meta X", "cut",
                "COPY", "copy",
                "PASTE", "paste",
                "CUT", "cut",
                "UP", "selectPrevious",
                "KP_UP", "selectPrevious",
                "shift UP", "selectPreviousExtendSelection",
                "shift KP_UP", "selectPreviousExtendSelection",
                "DOWN", "selectNext",
                "KP_DOWN", "selectNext",
                "shift DOWN", "selectNextExtendSelection",
                "shift KP_DOWN", "selectNextExtendSelection",
                "RIGHT", "selectChild",
                "KP_RIGHT", "selectChild",
                "LEFT", "selectParent",
                "KP_LEFT", "selectParent",
                "PAGE_UP", "scrollUpChangeSelection",
                "shift PAGE_UP", "scrollUpExtendSelection",
                "PAGE_DOWN", "scrollDownChangeSelection",
                "shift PAGE_DOWN", "scrollDownExtendSelection",
                "HOME", "selectFirst",
                "alt UP", "selectFirst",
                "shift HOME", "selectFirstExtendSelection",
                "END", "selectLast",
                "alt DOWN", "selectLast",
                "shift END", "selectLastExtendSelection",
                "F2", "startEditing",
                "meta A", "selectAll",
                "meta shift A", "clearSelection",
                "meta SPACE", "toggleSelectionPreserveAnchor",
                "shift SPACE", "extendSelection",
                "meta HOME", "selectFirstChangeLead",
                "meta END", "selectLastChangeLead",
                "meta UP", "selectPreviousChangeLead",
                "meta KP_UP", "selectPreviousChangeLead",
                "meta DOWN", "selectNextChangeLead",
                "meta KP_DOWN", "selectNextChangeLead",
                "meta PAGE_DOWN", "scrollDownChangeLead",
                "meta shift PAGE_DOWN", "scrollDownExtendSelection",
                "meta PAGE_UP", "scrollUpChangeLead",
                "meta shift PAGE_UP", "scrollUpExtendSelection",
                "meta LEFT", "scrollLeft",
                "meta KP_LEFT", "scrollLeft",
                "meta RIGHT", "scrollRight",
                "meta KP_RIGHT", "scrollRight",
                "SPACE", "toggleSelectionPreserveAnchor",}),};

        putDefaults(table, uiDefaults);
    }

    /**
     * Returns the base font for which system fonts are derived.
     * @return Lucida Grande, Plain, 13.
     */
    protected Font getBaseSystemFont() {
        return new FontUIResource("Lucida Grande", Font.PLAIN, 13);
    }

    protected void initFontDefaults(UIDefaults table) {
        Font baseSystemFont = getBaseSystemFont();

        // *** Shared Fonts
        // Some of the following comments have been taken from Apples Human Interface
        // Guidelines, Revision 2004-12-02.
        float fourteen = 14f;
        float thirteen = 13f;
        float twelve = 12f;
        float eleven = 11f;
        float ten = 10f;
        float nine = 9f;
        int fontPlain = Font.PLAIN;
        int fontBold = Font.BOLD;
        // The system font (Lucida Grande Regular 13 pt) is used for text in
        // menus, dialogs, and full-size controls.
        Object systemFont = new UIDefaults.ProxyLazyValue(
                "javax.swing.plaf.FontUIResource",
                null,
                new Object[]{baseSystemFont.deriveFont(fontPlain, thirteen)});
        // Use the emphasized system font (Lucida Grande Bold 13 pt) sparingly. It
        // is used for the message text in alerts.
        Object emphasizedSystemFont = new UIDefaults.ProxyLazyValue(
                "javax.swing.plaf.FontUIResource",
                null,
                new Object[]{baseSystemFont.deriveFont(fontBold, thirteen)});
        // The small system font (Lucida Grande Regular 11 pt) is used for
        // informative text in alerts. It is also the default font for column
        // headings in lists, for help tags, and for small controls. You can also
        // use it to provide additional information about settings in various
        // windows, such as the QuickTime pane in System OSXPreferences.
        Object smallSystemFont = new UIDefaults.ProxyLazyValue(
                "javax.swing.plaf.FontUIResource",
                null,
                new Object[]{baseSystemFont.deriveFont(fontPlain, eleven)});
        // Use the emphasized small system font (Lucida Grande Bold 11 pt)
        // sparingly. You might use it to title a group of settings that appear
        // without a group box, or for brief informative text below a text field.
        Object emphasizedSmallSystemFont = new UIDefaults.ProxyLazyValue(
                "javax.swing.plaf.FontUIResource",
                null,
                new Object[]{baseSystemFont.deriveFont(fontBold, eleven)});
        // The mini system font (Lucida Grande Regular 9 pt) is used for mini
        // controls. It can also be used for utility window labels and text.
        Object miniSystemFont = new UIDefaults.ProxyLazyValue(
                "javax.swing.plaf.FontUIResource",
                null,
                new Object[]{baseSystemFont.deriveFont(fontPlain, nine)});

        // An emphasized mini system font (Lucida Grande Bold 9 pt) is available for
        // cases in which the emphasized small system font is too large.
        Object emphasizedMiniSystemFont = new UIDefaults.ProxyLazyValue(
                "javax.swing.plaf.FontUIResource",
                null,
                new Object[]{baseSystemFont.deriveFont(fontBold, nine)});

        // If your application creates text documents, use the application font
        // (Lucida Grande Regular 13 pt) as the default font for user-created
        // content.
        Object applicationFont = baseSystemFont;
        // The label font (Lucida Grande Regular 10 pt) is used for the labels on
        // toolbar buttons and to label tick marks on full-size sliders. You should
        // rarely need to use this font. For an example of this font used to label a
        // slider controlBackground, see the Spoken User Interface pane in Speech preferences.
        Object labelFont = new UIDefaults.ProxyLazyValue(
                "javax.swing.plaf.FontUIResource",
                null,
                new Object[]{baseSystemFont.deriveFont(fontPlain, ten)});
        // Use the view font (Lucida Grande Regular 12pt) as the default font of
        // text in lists and tables.
        Object viewFont = new UIDefaults.ProxyLazyValue(
                "javax.swing.plaf.FontUIResource",
                null,
                new Object[]{baseSystemFont.deriveFont(fontPlain, twelve)});
        // The menu font (Lucida Grande Regular 14 pt) is used for text in menus and
        // window title bars.
        Object menuFont = new UIDefaults.ProxyLazyValue(
                "javax.swing.plaf.FontUIResource",
                null,
                new Object[]{baseSystemFont.deriveFont(fontPlain, fourteen)});

        // Set font sizes according to default size style.
        if (QuaquaManager.getProperty("Quaqua.sizeStyle", "regular").equals("small")) {
            viewFont = smallSystemFont;
            systemFont = smallSystemFont;
            emphasizedSystemFont = emphasizedSmallSystemFont;
            //smallSystemFont = smallSystemFont;
            menuFont = smallSystemFont;
            applicationFont = smallSystemFont;
            //labelFont = labelFont;
        }

        Object[] uiDefaults = {
            "SystemFont", systemFont,
            "EmphasizedSystemFont", emphasizedSystemFont,
            "SmallSystemFont", smallSystemFont,
            "EmphasizedSmallSystemFont", emphasizedSmallSystemFont,
            "MiniSystemFont", miniSystemFont,
            "EmphasizedMiniSystemFont", emphasizedMiniSystemFont,
            "ApplicationFont", applicationFont,
            "LabelFont", labelFont,
            "ViewFont", viewFont,
            "MenuFont", menuFont,
            "Browser.font", viewFont,
            "Button.font", systemFont,
            "Button.smallFont", smallSystemFont, // Maybe we should use Component.smallFont instead?

            "CheckBox.font", systemFont,
            "CheckBoxMenuItem.acceleratorFont", menuFont,
            "CheckBoxMenuItem.font", menuFont,
            "ColorChooser.font", smallSystemFont,
            "ColorChooser.crayonsFont", systemFont,
            "ComboBox.font", systemFont,
            "EditorPane.font", applicationFont,
            "FormattedTextField.font", applicationFont,
            "FileChooser.previewLabelFont", smallSystemFont,
            "FileChooser.previewValueFont", smallSystemFont,
            "IconButton.font", smallSystemFont, // ??
            "InternalFrame.optionDialogTitleFont", menuFont,
            "InternalFrame.titleFont", menuFont,
            "Label.font", systemFont,
            "List.font", viewFont,
            "List.focusCellHighlightBorder",
            new UIDefaults.ProxyLazyValue(
            "javax.swing.plaf.BorderUIResource$LineBorderUIResource",
            new Object[]{table.get("listHighlightBorder")}),
            "List.cellNoFocusBorder",
            new UIDefaults.ProxyLazyValue(
            "javax.swing.plaf.BorderUIResource$EmptyBorderUIResource",
            new Object[]{1, 1, 1, 1}),
            "Menu.acceleratorFont", menuFont,
            "Menu.font", menuFont,
            "MenuBar.font", menuFont,
            "MenuItem.acceleratorFont", menuFont,
            "MenuItem.font", menuFont,
            "OptionPane.buttonFont", systemFont,
            "OptionPane.font", systemFont,
            // We use a plain font for HTML messages to make the examples in the
            // Java Look and Feel Guidelines work.
            "OptionPane.messageFont", emphasizedSystemFont,
            "OptionPane.htmlMessageFont", systemFont,
            "Panel.font", systemFont,
            "PasswordField.font", applicationFont,
            "PopupMenu.font", menuFont,
            "ProgressBar.font", systemFont,
            "RadioButton.font", systemFont,
            "RadioButtonMenuItem.acceleratorFont", menuFont,
            "RadioButtonMenuItem.font", menuFont,
            "RootPane.font", systemFont,
            "ScrollBar.font", systemFont,
            "ScrollPane.font", systemFont,
            "Slider.font", systemFont,
            "Slider.labelFont", labelFont,
            "Spinner.font", systemFont,
            "TabbedPane.font", systemFont,
            "TabbedPane.smallFont", smallSystemFont, // ??
            "TabbedPane.wrap.font", systemFont,
            "TabbedPane.wrap.smallFont", smallSystemFont, // ??
            "TabbedPane.scroll.font", systemFont,
            "TabbedPane.scroll.smallFont", smallSystemFont, // ??
            "Table.font", viewFont,
            "TableHeader.font", smallSystemFont,
            "TextArea.font", applicationFont,
            "TextField.font", applicationFont,
            "TextPane.font", applicationFont,
            "TitledBorder.font", smallSystemFont,
            "ToggleButton.font", systemFont,
            "ToolBar.font", miniSystemFont,
            "ToolBar.titleFont", miniSystemFont,
            "ToolTip.font", smallSystemFont,
            "Tree.font", viewFont,
            "Viewport.font", systemFont,};

        putDefaults(table, uiDefaults);
    }

    /**
     * The defaults initialized here are common to all Quaqua Look and Feels.
     * @param table Table onto which defaults are to be appended.
     */
    protected void initGeneralDefaults(UIDefaults table) {
        String javaVersion = QuaquaManager.getProperty("java.version", "");

        String systemFontName = getBaseSystemFont().getName();

        // Focus behavior
        Boolean isRequestFocusEnabled = Boolean.valueOf(QuaquaManager.getProperty("Quaqua.requestFocusEnabled", "false"));

        // True if all controls are focusable,
        // false if only text boxes and lists are focusable.
        // Set this value to true if requestFocus is enabled or
        // if bit 2 of AppleKeyboardUIMode is set.
        String prefValue = OSXPreferences.getString(OSXPreferences.GLOBAL_PREFERENCES, "AppleKeyboardUIMode", "2");
        int intValue;
        try {
            intValue = Integer.valueOf(prefValue);
        } catch (NumberFormatException e) {
            intValue = 2;
        }
        Boolean allControlsFocusable = isRequestFocusEnabled || ((intValue & 2) == 2);

        Object dialogBorder = new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaBorders$DialogBorder");

        Object questionDialogBorder = new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaBorders$QuestionDialogBorder");
        // Shared colors
        ColorUIResource listSelectionBorderColor = (ColorUIResource) table.get("listHighlightBorder");
        Color menuBackground = (Color) table.get("menu");

        // Set visual margin.
        int[] values = QuaquaManager.getProperty("Quaqua.visualMargin", new int[]{3, 3, 3, 3});
        InsetsUIResource visualMargin = new InsetsUIResource(values[0], values[1], values[2], values[3]);

        // Opaqueness
        Boolean opaque = Boolean.valueOf(QuaquaManager.getProperty("Quaqua.opaque", "false"));

        // Autovalidation
        Boolean autovalidate = Boolean.valueOf(QuaquaManager.getProperty("Quaqua.FileChooser.autovalidate", "true"));

        // Popup menus for all text components
        Object textComponentPopupHandler = new UIDefaults.ProxyLazyValue("ch.randelshofer.quaqua.QuaquaTextComponentPopupHandler");
        // Focus handler for all text fields
        Object textFieldFocusHandler = new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaTextFieldFocusHandler");

        // TextField auto selection
        Boolean autoselect = Boolean.valueOf(QuaquaManager.getProperty("Quaqua.TextComponent.autoSelect", "true"));
        // *** Shared Borders
        Object textFieldBorder = new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaNativeTextFieldBorder$UIResource",
                new Object[]{new Insets(0,0,0,0), new Insets(6,8,6,8), true});
        
        Object buttonBorder = new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaNativeButtonBorder$UIResource"
                );

        // True if file choosers orders by type
        boolean isOrderFilesByType = false;
        // True if file choosers shows all files by default
        prefValue = OSXPreferences.getString(//
                OSXPreferences.FINDER_PREFERENCES, "AppleShowAllFiles", "false")//
                .toLowerCase();
        boolean isFileHidingEnabled = prefValue.equals("false") || prefValue.equals("no");
        boolean isQuickLookEnabled = Boolean.valueOf(QuaquaManager.getProperty("Quaqua.FileChooser.quickLookEnabled","true"));

        // Enforce visual margin
        // Set this to true, to workaround Matisse issue #
        //
        // Enforce margin is used to workaround a workaround in the Matisse
        // design tool for NetBeans. Matisse removes borders from some
        // components in order to workaround some ugliness in the look
        // and feels that ship with the J2SE.
        Boolean enforceVisualMargin = Boolean.valueOf(QuaquaManager.getProperty("Quaqua.enforceVisualMargin", "false"));

        Object[] uiDefaults = {
            "Browser.sizeHandleIcon", new UIDefaults.ProxyLazyValue("ch.randelshofer.quaqua.QuaquaIconFactory", "createIcon",
            new Object[]{commonDir + "Browser.sizeHandleIcon.png", 1, Boolean.TRUE, 1}),
            //
            "Button.actionMap", new QuaquaLazyActionMap(QuaquaButtonListener.class),
            "Button.border", buttonBorder,
            //
            // This must be set to false to make default button on option panes
            // work as expected when running Java 1.5.
            "Button.defaultButtonFollowsFocus", Boolean.FALSE,
            "Button.margin", new InsetsUIResource(2, 2, 2, 2),
            "Button.opaque", opaque,
            "Button.textIconGap", 4,
            "Button.textShiftOffset", 0,
            "Button.helpIcon",  makeNativeButtonStateIcon(OSXAquaPainter.Widget.buttonRoundHelp,0,1,21,21,true),
            "Button.smallHelpIcon",  makeNativeButtonStateIcon(OSXAquaPainter.Widget.buttonRoundHelp,0,1,18,18,true),
            "Button.miniHelpIcon",  makeNativeButtonStateIcon(OSXAquaPainter.Widget.buttonRoundHelp,1,1,15,15,true),
            "Button.requestFocusEnabled", isRequestFocusEnabled,
            // Note: Minimum width only affects regular sized buttons with push button style
            "Button.minimumWidth", 80,
            "Button.focusable", allControlsFocusable,
            //
            //"CheckBox.background", ...,
            "CheckBox.border", new VisualMarginBorder(0, 0, 0, 0),
            "CheckBox.icon", makeNativeButtonStateIcon(OSXAquaPainter.Widget.buttonCheckBox, 0,0,16,20,true),
            "CheckBox.smallIcon", makeNativeButtonStateIcon(OSXAquaPainter.Widget.buttonCheckBox, 0,0,14,16,true),
            "CheckBox.miniIcon", makeNativeButtonStateIcon(OSXAquaPainter.Widget.buttonCheckBox, 0,-1,10,15,true),
            "CheckBox.margin", new InsetsUIResource(0, 0, 0, 0),
            "CheckBox.opaque", opaque,
            //"CheckBox.select", ...,
            "CheckBox.textIconGap", 4,
            "CheckBox.textShiftOffset", 0,
            "CheckBox.requestFocusEnabled", isRequestFocusEnabled,
            "CheckBoxMenuItem.borderPainted", Boolean.TRUE,
            "CheckBox.focusable", allControlsFocusable,
            // Set this to true, to workaround Matisse issue #
            // Enforce margin is used to workaround a workaround in the Matisse
            // design tool for NetBeans. Matisse removes borders from some
            // components in order to workaround some ugliness in the look
            // and feels that ship with the J2SE.
            "CheckBox.enforceVisualMargin", enforceVisualMargin,
            //
            // class names of default choosers
            "ColorChooser.defaultChoosers", new String[]{
                "ch.randelshofer.quaqua.colorchooser.ColorWheelChooser",
                "ch.randelshofer.quaqua.colorchooser.ColorSlidersChooser",
                "ch.randelshofer.quaqua.colorchooser.ColorPalettesChooser",
                "ch.randelshofer.quaqua.colorchooser.SwatchesChooser",
                "ch.randelshofer.quaqua.colorchooser.CrayonsChooser",
                "ch.randelshofer.quaqua.colorchooser.QuaquaColorPicker",},
            //"ColorChooser.swatchesDefaultRecentColor", ...,
            //"ColorChooser.swatchesRecentSwatchSize", ...,
            "ColorChooser.swatchesSwatchSize", new DimensionUIResource(5, 5),
            "ColorChooser.resetMnemonic", -1,
            "ColorChooser.crayonsImage", makeImage(commonDir + "ColorChooser.crayons.png"),
            "ColorChooser.textSliderGap", 0,
            "ColorChooser.colorPalettesIcon", makeButtonStateIcon(commonDir + "ColorChooser.colorPalettesIcons.png", 3),
            "ColorChooser.colorSlidersIcon", makeButtonStateIcon(commonDir + "ColorChooser.colorSlidersIcons.png", 3),
            "ColorChooser.colorSwatchesIcon", makeButtonStateIcon(commonDir + "ColorChooser.colorSwatchesIcons.png", 3),
            "ColorChooser.colorWheelIcon", makeButtonStateIcon(commonDir + "ColorChooser.colorWheelIcons.png", 3),
            "ColorChooser.crayonsIcon", makeButtonStateIcon(commonDir + "ColorChooser.crayonsIcons.png", 3),
            "ColorChooser.imagePalettesIcon", makeButtonStateIcon(commonDir + "ColorChooser.imagePalettesIcons.png", 3),
            // Icon of the color picker tool
            "ColorChooser.colorPickerIcon", makeIcon(getClass(), commonDir + "ColorChooser.colorPickerIcon.png"),
            // Magnifying glass used as the cursor image
            "ColorChooser.colorPickerMagnifier", makeBufferedImage(commonDir + "ColorChooser.colorPickerMagnifier.png"),
            // Hot spot of the magnifier cursor
            "ColorChooser.colorPickerHotSpot", new UIDefaults.ProxyLazyValue("java.awt.Point", new Object[]{29, 29}),
            // Pick point relative to hot spot
            "ColorChooser.colorPickerPickOffset", new UIDefaults.ProxyLazyValue("java.awt.Point", new Object[]{-13, -13}),
            // Rectangle used for drawing the mask of the magnifying glass
            "ColorChooser.colorPickerGlassRect", new UIDefaults.ProxyLazyValue("java.awt.Rectangle", new Object[]{2, 2, 29, 29}),
            // Capture rectangle. Width and height must be equal sized and must be odd.
            // The position of the capture rectangle is relative to the hot spot.
            "ColorChooser.colorPickerCaptureRect", new UIDefaults.ProxyLazyValue("java.awt.Rectangle", new Object[]{-15, -15, 5, 5}),
            // Zoomed (magnified) capture image. Width and height must be a multiple of the capture rectangles size.
            "ColorChooser.colorPickerZoomRect", new UIDefaults.ProxyLazyValue("java.awt.Rectangle", new Object[]{4, 4, 25, 25}),
            "ColorChooser.ColorSlider.northThumb.small", makeSliderThumbIcon(commonDir + "Slider.northThumbs.small.png"),
            "ColorChooser.ColorSlider.westThumb.small", makeSliderThumbIcon(commonDir + "Slider.westThumbs.small.png"),
            //
            "ComboBox.buttonBorder", makeNativeButtonStateBorder(OSXAquaPainter.Widget.buttonComboBox, new Insets(0, -10, 0, -2), new Insets(0, 0, 0, 0), true),
            "ComboBox.smallButtonBorder", makeNativeButtonStateBorder(OSXAquaPainter.Widget.buttonComboBox, new Insets(0, -20, 0, -1), new Insets(0, 0, 0, 0), true),
            "ComboBox.miniButtonBorder", makeNativeButtonStateBorder(OSXAquaPainter.Widget.buttonComboBox, new Insets(0, -12, 0, -2), new Insets(0, 0, 0, 0), true),
            "ComboBox.cellBorder", null,
            "ComboBox.editorBorder", textFieldBorder,
            "ComboBox.smallCellBorder", null,
            "ComboBox.cellAndButtonBorder", makeNativeButtonStateBorder(OSXAquaPainter.Widget.buttonPopUp, new Insets(1, 1, 0, 0), new Insets(3, 3, 3, 3), true),
            "ComboBox.smallCellAndButtonBorder", makeNativeButtonStateBorder(OSXAquaPainter.Widget.buttonPopUp, new Insets(1, 0, 0, 0), new Insets(3, 3, 3, 3), true),
            "ComboBox.miniCellAndButtonBorder", makeNativeButtonStateBorder(OSXAquaPainter.Widget.buttonPopUp, new Insets(1, 2, 0, 1), new Insets(3, 3, 3, 3), true),
            "ComboBox.buttonInsets", new Insets(-3, -3, -3, -3),
            "ComboBox.border", new VisualMarginBorder(2, 0, 2, 0),
            "ComboBox.dropDownIcon", null,
            "ComboBox.opaque", opaque,
            "ComboBox.popupIcon", null,
            "ComboBox.smallPopupIcon", null,
            "ComboBox.miniPopupIcon", null,
            "ComboBox.cellEditorPopupIcon", makeButtonStateIcon(commonDir + "ComboBox.small.popupIcons.png", 6),
            "ComboBox.smallDropDownIcon", null,
            "ComboBox.miniDropDownIcon", null,
            "ComboBox.dropDownWidth",18,
            "ComboBox.smallDropDownWidth",16,
            "ComboBox.miniDropDownWidth",14,
            "ComboBox.popupWidth",19,
            "ComboBox.smallPopupWidth",17,
            "ComboBox.miniPopupWidth",16,
            "ComboBox.maximumRowCount", 8,
            "ComboBox.arrowButtonInsets",new InsetsUIResource(4, 8,3,5),
            "ComboBox.smallArrowButtonInsets",new InsetsUIResource(4, 6,3,5),
            "ComboBox.miniArrowButtonInsets",new InsetsUIResource(4, 3,3,5),
            "ComboBox.requestFocusEnabled", isRequestFocusEnabled,
            "ComboBox.showPopupOnNavigation", Boolean.TRUE,
            // Set this to Boolean.TRUE to get the same preferred height for
            // non-editable combo boxes and editable-combo boxes.
            "ComboBox.harmonizePreferredHeight", Boolean.FALSE,
            // The values for this margin are ignored. We dynamically compute a margin
            // for the various button styles that we support, if we encounter a
            // a margin that is an instanceof a UIResource.
            "ComboBoxButton.margin", new InsetsUIResource(0, 0, 0, 0),
            // Setting this to true makes the combo box UI change the foreground
            // color of the editor to the the foreground color of the JComboBox.
            // True is needed for rendering of combo boxes in JTables.
            "ComboBox.changeEditorForeground", Boolean.TRUE,
            
            "ComboBox.focusable", allControlsFocusable,
             
             
            //
            // The visual margin is used to allow each component having room
            // for a cast shadow and a focus ring, and still supporting a
            // consistent visual arrangement of all components aligned to their
            // visualy perceived lines.
            // FIXME: This should be either a global system property
            // "Quaqua.visualMargin" or a per-component property e.g.
            // "Button.visualMargin".
            "Component.visualMargin", visualMargin,
            //
            //"DesktopIcon.border", ...

            //"EditorPane.border", ...
            //"EditorPane.caretBlinkRate", ...
            "EditorPane.margin", new InsetsUIResource(1, 3, 1, 3),
            "EditorPane.popupHandler", textComponentPopupHandler,
            //
            "FileChooser.autovalidate", autovalidate,
            //
            "FileChooser.browserFocusCellHighlightBorder",
            new UIDefaults.ProxyLazyValue(
            "javax.swing.plaf.BorderUIResource$LineBorderUIResource",
            new Object[]{table.get("listHighlightBorder")}),
            "FileChooser.browserCellBorder",
            new UIDefaults.ProxyLazyValue(
            "javax.swing.plaf.BorderUIResource$EmptyBorderUIResource",
            new Object[]{new Insets(1, 1, 1, 1)}),
            "FileChooser.disclosureButtonIcon", makeButtonStateIcon(
            leopardDir + "FileChooser.disclosureButtonIcons.png", 10),
            //
            "FileChooser.fileHidingEnabled", isFileHidingEnabled,
            "FileChooser.quickLookEnabled", isQuickLookEnabled,
            "FileChooser.homeFolderIcon", makeIcon(getClass(), commonDir + "FileChooser.homeFolderIcon.png"),
            "FileChooser.orderByType", isOrderFilesByType,
            "FileChooser.previewLabelForeground", new ColorUIResource(0x000000),
            "FileChooser.previewValueForeground", new ColorUIResource(0x000000),
            "FileChooser.previewLabelInsets", new InsetsUIResource(1, 0, 0, 1),
            "FileChooser.previewLabelDelimiter", ":",
            "FileChooser.splitPaneDividerSize", 4,
            "FileChooser.speed", (QuaquaManager.getProperty("Quaqua.FileChooser.speed") != null && QuaquaManager.getProperty("Quaqua.FileChooser.speed").equals("true")),
            //
            "FileView.computerIcon", makeIcon(getClass(), commonDir + "FileView.computerIcon.png"),
            "FileView.directoryIcon", makeIcon(getClass(), commonDir + "FileView.directoryIcon.png"),
            "FileView.fileIcon", makeIcon(getClass(), commonDir + "FileView.fileIcon.png"),
            "FileView.aliasBadge", makeIcon(getClass(), commonDir + "FileView.aliasBadge.png"),
            //
           "FormattedTextField.border", textFieldBorder,
            "FormattedTextField.opaque", opaque,
            "FormattedTextField.focusHandler", textFieldFocusHandler,
            "FormattedTextField.popupHandler", textComponentPopupHandler,
            "FormattedTextField.autoSelect", autoselect,
            "Label.border", new VisualMarginBorder(0, 0, 0, 0),
            "Label.opaque", opaque,
            //
            "List.cellRenderer", new UIDefaults.ProxyLazyValue("ch.randelshofer.quaqua.QuaquaDefaultListCellRenderer"),
            //
            "Menu.borderPainted", Boolean.TRUE,
            "MenuItem.borderPainted", Boolean.TRUE,
            // The negative values are used to take account for the visual margin
            "OptionPane.border", new BorderUIResource.EmptyBorderUIResource(15 - 3, 24 - 3, 20 - 3, 24 - 3),
            "OptionPane.messageAreaBorder", new BorderUIResource.EmptyBorderUIResource(0, 0, 0, 0),
            "OptionPane.buttonAreaBorder", new BorderUIResource.EmptyBorderUIResource(16 - 3, 0, 0, 0),
            "OptionPane.errorIcon", new UIDefaults.ProxyLazyValue(
            "ch.randelshofer.quaqua.QuaquaIconFactory", "createOptionPaneIcon", new Object[]{JOptionPane.ERROR_MESSAGE}),
            "OptionPane.errorIconResource", "/ch/randelshofer/quaqua/images/OptionPane.errorIcon.png",
            "OptionPane.informationIcon", new UIDefaults.ProxyLazyValue(
            "ch.randelshofer.quaqua.QuaquaIconFactory", "createOptionPaneIcon", new Object[]{JOptionPane.INFORMATION_MESSAGE}),
            "OptionPane.questionIcon", new UIDefaults.ProxyLazyValue(
            "ch.randelshofer.quaqua.QuaquaIconFactory", "createOptionPaneIcon", new Object[]{JOptionPane.QUESTION_MESSAGE}),
            "OptionPane.warningIcon", new UIDefaults.ProxyLazyValue(
            "ch.randelshofer.quaqua.QuaquaIconFactory", "createOptionPaneIcon", new Object[]{JOptionPane.WARNING_MESSAGE}),
            "OptionPane.warningIconResource", "/ch/randelshofer/quaqua/images/OptionPane.warningIcon.png",
            "OptionPane.css", "<head>"
            + "<style type=\"text/css\">"
            + "b { font: 13pt \"" + systemFontName + "\" }"
            + "p { font: 11pt \"" + systemFontName + "\"; margin-top: 8px }"
            + "</style>"
            + "</head>",
            "OptionPane.messageLabelWidth", 360,
            "OptionPane.maxCharactersPerLineCount", 60,
            "Panel.opaque", opaque,
            //
            "PopupMenu.enableHeavyWeightPopup", Boolean.TRUE,
            //
            "PasswordField.border", textFieldBorder,
            "PasswordField.opaque", opaque,
            "PasswordField.focusHandler", textFieldFocusHandler,
            "PasswordField.popupHandler", textComponentPopupHandler,
            "PasswordField.autoSelect", autoselect,
            //
            "RadioButton.border", new VisualMarginBorder(0, 0, 0, 0),
            // The values for this margin are ignored. We dynamically compute a margin
            // for the various button styles that we support, if we encounter a
            // a margin that is an instanceof a UIResource.
            "RadioButton.margin", new InsetsUIResource(0, 0, 0, 0),
            "RadioButton.icon", makeNativeButtonStateIcon(OSXAquaPainter.Widget.buttonRadio, 0,1,16,20,true),
            "RadioButton.smallIcon", makeNativeButtonStateIcon(OSXAquaPainter.Widget.buttonRadio, 0,1,14,17,true),
            "RadioButton.miniIcon", makeNativeButtonStateIcon(OSXAquaPainter.Widget.buttonRadio, 0,1,10,16,true),
            "RadioButton.opaque", opaque,
            "RadioButton.textIconGap", 4,
            "RadioButton.textShiftOffset", 0,
            "RadioButton.requestFocusEnabled", isRequestFocusEnabled,
            "RadioButtonMenuItem.borderPainted", Boolean.TRUE,
            "RadioButton.enforceVisualMargin", enforceVisualMargin,
            "RadioButton.focusable", allControlsFocusable,
            //
            // RootPane
            "RootPane.opaque", Boolean.TRUE,
            "RootPane.frameBorder", new UIDefaults.ProxyLazyValue(
            "ch.randelshofer.quaqua.QuaquaBorders$FrameBorder"),
            "RootPane.plainDialogBorder", dialogBorder,
            "RootPane.informationDialogBorder", dialogBorder,
            "RootPane.errorDialogBorder", new UIDefaults.ProxyLazyValue(
            "ch.randelshofer.quaqua.QuaquaBorders$ErrorDialogBorder"),
            "RootPane.colorChooserDialogBorder", questionDialogBorder,
            "RootPane.fileChooserDialogBorder", questionDialogBorder,
            "RootPane.questionDialogBorder", questionDialogBorder,
            "RootPane.warningDialogBorder", new UIDefaults.ProxyLazyValue(
            "ch.randelshofer.quaqua.QuaquaBorders$WarningDialogBorder"),
            // These bindings are only enabled when there is a default
            // button set on the rootpane.
            "RootPane.defaultButtonWindowKeyBindings", new Object[]{
                "ENTER", "press",
                "released ENTER", "release",
                "ctrl ENTER", "press",
                "ctrl released ENTER", "release"
            },
            // Setting this property to null disables snapping
            // Note: snapping is only in effect for look and feel decorated
            // windows
            "RootPane.windowSnapDistance", 10,
            // Default value for "apple.awt.draggableWindowBackground"
            "RootPane.draggableWindowBackground", Boolean.FALSE,
            // Default value for "apple.awt.windowShadow"
            "RootPane.windowShadow", Boolean.TRUE,
            "ScrollBar.focusable", Boolean.FALSE,
            //
            "ScrollPane.requesFocusEnabled", Boolean.FALSE,
            "ScrollPane.focusable", Boolean.FALSE,
            "ScrollPane.opaque", opaque,
            "ScrollPane.growBoxSize",new DimensionUIResource(0,0),
            //
            "Separator.border", new VisualMarginBorder(),
            //
            "Sheet.showAsSheet", Boolean.TRUE,
            //
            "Slider.roundThumb", makeSliderThumbIcon(commonDir + "Slider.roundThumbs.png"),
            "Slider.roundThumb.small", makeSliderThumbIcon(commonDir + "Slider.roundThumbs.small.png"),
            "Slider.southThumb", makeSliderThumbIcon(commonDir + "Slider.southThumbs.png"),
            "Slider.eastThumb", makeSliderThumbIcon(commonDir + "Slider.eastThumbs.png"),
            "Slider.northThumb", makeSliderThumbIcon(commonDir + "Slider.northThumbs.png"),
            "Slider.westThumb", makeSliderThumbIcon(commonDir + "Slider.westThumbs.png"),
            "Slider.eastThumb.small", makeSliderThumbIcon(commonDir + "Slider.eastThumbs.small.png"),
            "Slider.southThumb.small", makeSliderThumbIcon(commonDir + "Slider.southThumbs.small.png"),
            "Slider.northThumb.small", makeSliderThumbIcon(commonDir + "Slider.northThumbs.small.png"),
            "Slider.westThumb.small", makeSliderThumbIcon(commonDir + "Slider.westThumbs.small.png"),
            "Slider.opaque", opaque,
            "Slider.requestFocusEnabled", isRequestFocusEnabled,
            "Slider.tickColor", new ColorUIResource(0x808080),
            "Slider.focusInsets", new Insets(0, 0, 0, 0),
            "Slider.verticalTracks", makeImageBevelBorders(commonDir + "Slider.verticalTracks.png", new Insets(4, 5, 4, 0), 2, true),
            "Slider.horizontalTracks", makeImageBevelBorders(commonDir + "Slider.horizontalTracks.png", new Insets(5, 4, 0, 4), 2, false),
            "Slider.focusable", allControlsFocusable,
            //
            "Spinner.arrowButtonBorder", null,
            "Spinner.arrowButtonInsets", null,
            "Spinner.border", null,
            "Spinner.editorBorderPainted", Boolean.TRUE,
            "Spinner.opaque", opaque,
            "Spinner.north", makeButtonStateIcon(commonDir + "Spinner.north.png", 10),
            "Spinner.south", makeButtonStateIcon(commonDir + "Spinner.south.png", 10),
            "Spinner.smallNorth", makeButtonStateIcon(commonDir + "Spinner.small.north.png", 10),
            "Spinner.smallSouth", makeButtonStateIcon(commonDir + "Spinner.small.south.png", 10),
            //"SplitPane.actionMap", ???,
            //"SplitPane.ancestorInputMap", ???,
            "SplitPane.opaque", opaque,
            "SplitPane.border", null,
            "SplitPane.dividerSize", 10,
            "SplitPane.thumbDimple", makeIcon(getClass(), commonDir + "SplitPane.thumbDimple.png"),
            "SplitPane.barDimple", makeIcon(getClass(), commonDir + "SplitPane.barDimple.png"),
            "SplitPane.hBar", makeImageBevelBorder(commonDir + "SplitPane.hBar.png", new Insets(4, 0, 5, 0), true),
            "SplitPane.vBar", makeImageBevelBorder(commonDir + "SplitPane.vBar.png", new Insets(0, 4, 0, 5), true),
            "SplitPane.upArrow", makeIcon(getClass(), commonDir + "SplitPane.upArrow.png"),
            "SplitPane.downArrow", makeIcon(getClass(), commonDir + "SplitPane.downArrow.png"),
            "SplitPane.rightArrow", makeIcon(getClass(), commonDir + "SplitPane.rightArrow.png"),
            "SplitPane.leftArrow", makeIcon(getClass(), commonDir + "SplitPane.leftArrow.png"),
            "SplitPane.focusable", Boolean.FALSE,
            "SplitPane.requestFocusEnabled", Boolean.FALSE,
            "SplitPaneDivider.border", null,
            "SplitPaneDivider.focusable", Boolean.FALSE,
            "SplitPaneDivider.requestFocusEnabled", Boolean.FALSE,
            //
            "TabbedPane.opaque", opaque,
            "TabbedPane.wrap.opaque", opaque,
            "TabbedPane.scroll.opaque", opaque,
            "TabbedPane.requestFocusEnabled", isRequestFocusEnabled,
            "TabbedPane.textIconGap", 4,
            "TabbedPane.scroll.textIconGap", 4,
            "TabbedPane.wrap.textIconGap", 4,
            "Table.focusCellHighlightBorder", new BorderUIResource.LineBorderUIResource(listSelectionBorderColor),
            //"Table.focusCellHighlightBorder", new BorderUIResource.LineBorderUIResource(Color.black),
            //"TableHeader.cellBorder", new UIDefaults.ProxyLazyValue(
            //"ch.randelshofer.quaqua.QuaquaTableHeaderBorder$UIResource",
            //new Object[]{commonDir + "TableHeader.borders.png", new Insets(6, 1, 9, 1)}),
            //
            "TextArea.margin", new InsetsUIResource(1, 3, 1, 3),
            "TextArea.opaque", Boolean.TRUE,
            "TextArea.popupHandler", textComponentPopupHandler,
            //
            "TextComponent.showNonEditableCaret",QuaquaManager.getProperty("Quaqua.showNonEditableCaret", "true").equals("true"),
            //
            "TextField.border", textFieldBorder,
            "TextField.opaque", opaque,
            "TextField.focusHandler", textFieldFocusHandler,
            "TextField.popupHandler", textComponentPopupHandler,
            "TextField.autoSelect", autoselect,
            //
            "TextPane.margin", new InsetsUIResource(1, 3, 1, 3),
            "TextPane.opaque", Boolean.TRUE,
            "TextPane.popupHandler", textComponentPopupHandler,
            //
            "ToggleButton.border", buttonBorder,
            "ToggleButton.margin", new InsetsUIResource(2, 2, 2, 2),
            "ToggleButton.opaque", opaque,
            "ToggleButton.textIconGap", 4,
            "ToggleButton.textShiftOffset", 0,
            "ToggleButton.requestFocusEnabled", isRequestFocusEnabled,
            "ToggleButton.focusable", allControlsFocusable,
            //
            "ToolBar.border", new UIDefaults.ProxyLazyValue("ch.randelshofer.quaqua.QuaquaNativeToolBarBorder$UIResource"),
            // The separatorSize is set to null, because we dynamically compute different
            // sizes depending on the orientation of the separator.
            "ToolBar.separatorSize", null,
            "ToolBar.margin", new InsetsUIResource(0, 0, 0, 0),
            "ToolBar.borderBright", new AlphaColorUIResource(0x999999),
            "ToolBar.borderDark", new ColorUIResource(0x8c8c8c),
            "ToolBar.borderDivider", new ColorUIResource(0x9f9f9f),
            "ToolBar.borderDividerInactive", new ColorUIResource(0x9f9f9f),
            "ToolBar.bottom.gradient", new Color[]{new Color(0xd8d8d8), new Color(0xbdbdbd), new Color(0xaeaeae), new Color(0x969696)},
            "ToolBar.bottom.gradientInactive", new Color[]{new Color(0xeeeeee), new Color(0xe4e4e4), new Color(0xcfcfcf)},
            // The toolbar is opaque because of the gradient that we want to paint.
            "ToolBar.opaque", Boolean.TRUE,
            //
            "ToolTip.border", new BorderUIResource.LineBorderUIResource(new ColorUIResource(0x303030)),
            //
            "Tree.collapsedIcon", makeIcon(getClass(), commonDir + "Tree.collapsedIcon.png"),
            "Tree.expandedIcon", makeIcon(getClass(), commonDir + "Tree.expandedIcon.png"),
            "Tree.leftChildIndent", 7,
            "Tree.line", new AlphaColorUIResource(0x00000000),
            "Tree.paintLines", Boolean.FALSE,
            "Tree.rightChildIndent", 13,
            "Tree.rowHeight", 19,
            "Tree.leafIcon", makeIcon(getClass(), commonDir + "Tree.leafIcon.png"),
            "Tree.openIcon", makeIcon(getClass(), commonDir + "Tree.openIcon.png"),
            "Tree.closedIcon", makeIcon(getClass(), commonDir + "Tree.closedIcon.png"),
            "Tree.showsRootHandles", Boolean.TRUE,
            //"Tree.editorBorder", new VisualMarginBorder(3,3,3,3),

            "Viewport.opaque", Boolean.TRUE,
            "Quaqua.Debug.colorizePaintEvents", (QuaquaManager.getProperty("Quaqua.Debug.colorizePaintEvents", "false")),
            "Quaqua.Debug.showClipBounds", (QuaquaManager.getProperty("Quaqua.Debug.showClipBounds", "false").equals("true")),
            "Quaqua.Debug.showVisualBounds", (QuaquaManager.getProperty("Quaqua.Debug.showVisualBounds", "false").equals("true")),
            "Quaqua.Debug.clipBoundsForeground", new AlphaColorUIResource(0, 0, 255, 128),
            "Quaqua.Debug.componentBoundsForeground", new AlphaColorUIResource(255, 0, 0, 128),
            "Quaqua.Debug.textBoundsForeground", new AlphaColorUIResource(255, 0, 0, 128),
            "ClassLoader", getClass().getClassLoader(),};
        putDefaults(table, uiDefaults);
    }

    protected Object makeImage(String location) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createImage",
                new Object[]{location});
    }

    protected Object makeBufferedImage(String location) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createBufferedImage",
                new Object[]{location});
    }
    protected Object makeBufferedImage(String location, Rectangle subimage) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createBufferedImage",
                new Object[]{location, subimage});
    }

    public static Object makeIcon(Class baseClass, String location) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createIcon",
                new Object[]{baseClass, location});
    }

    public static Object makeIcon(Class baseClass, String location, Point shift) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createIcon",
                new Object[]{baseClass, location, shift});
    }

    public static Object makeIcon(Class baseClass, String location, Rectangle shiftAndSize) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createIcon",
                new Object[]{baseClass, location, shiftAndSize});
    }

    protected static Object makeIcons(String location, int states, boolean horizontal) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createIcons",
                new Object[]{location, states, horizontal});
    }
    protected static Object makeIcons(String location,Rectangle subimage, int states, boolean horizontal) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createIcons",
                new Object[]{location,subimage, states, horizontal});
    }

    public static Object makeNativeSidebarIcon(String path, int size, Color color, Color selectionColor) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createNativeSidebarIcon",
                new Object[]{path, size, size, color, selectionColor});
    }
    public static Object makeNativeIcon(String path, int size) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createNativeIcon",
                new Object[]{path, size});
    }

    public static Object makeNativeIcon(String path, int width, int height) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createNativeIcon",
                new Object[]{path, width, height});
    }
    protected Object makeNativeButtonStateIcon(OSXAquaPainter.Widget widget, 
            int xoffset, int yoffset, int width, int height, boolean withFocusRing) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createNativeButtonStateIcon",
                new Object[]{widget, xoffset, yoffset, width, height, withFocusRing });

    }

    protected static Object makeButtonStateIcon(String location, int states) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createButtonStateIcon",
                new Object[]{location, states});
    }

    protected static Object makeButtonStateIcon(String location, int states, Point shift) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createButtonStateIcon",
                new Object[]{location, states, shift});
    }

    protected static Object makeButtonStateIcon(String location, int states, Rectangle shift) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createButtonStateIcon",
                new Object[]{location, states, shift});
    }

    protected static Object makeFrameButtonStateIcon(String location, int states) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createFrameButtonStateIcon",
                new Object[]{location, states});
    }

    protected static Object makeSliderThumbIcon(String location) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createSliderThumbIcon",
                new Object[]{location});
    }

    protected Object makeOverlaidButtonStateIcon(
            String location1, int states1,
            String location2, int states2,
            Rectangle layoutRect) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaIconFactory", "createOverlaidButtonStateIcon",
                new Object[]{
                    location1, states1,
                    location2, states2,
                    layoutRect
                });
    }
    protected Object makeImageBevelBorder(String location, Rectangle subimage, Insets insets, boolean fill) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaBorderFactory", "create",
                new Object[]{location, subimage, insets, insets, fill});
    }



    protected Object makeImageBevelBorder(String location, Insets insets) {
        return makeImageBevelBorder(location, insets, false);
    }

    protected Object makeImageBevelBorder(String location, Insets insets, boolean fill) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaBorderFactory", "create",
                new Object[]{location, insets, fill});
    }

    protected Object makeImageBevelBorder(String location, Insets insets, boolean fill, Color fillColor) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaBorderFactory", "create",
                new Object[]{location, insets, insets, fill, fillColor});
    }

    protected Object makeImageBevelBorder(String location, Insets imageInsets, Insets borderInsets, boolean fill) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaBorderFactory", "create",
                new Object[]{location, imageInsets, borderInsets, fill});
    }

    protected Object makeImageBevelBackgroundBorder(String location, Insets imageInsets, Insets borderInsets, boolean fill) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaBorderFactory", "createBackgroundBorder",
                new Object[]{location, imageInsets, borderInsets, fill});
    }

    protected Object makeImageBevelBorders(String location, Insets insets, int states, boolean horizontal) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaBorderFactory", "create",
                new Object[]{location, insets, states, horizontal});
    }
    protected Object makeNativeImageBevelBorder(OSXAquaPainter.Widget widget, Insets painterInsets, Insets imageBevelInsets, Insets borderInsets, boolean fill) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaBorderFactory", "createNativeImageBevelBorder",
                new Object[]{widget, painterInsets, imageBevelInsets, borderInsets, fill});
    }

    protected Object makeTextureColor(int rgb, String location) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.color.TextureColor$UIResource",
                new Object[]{rgb, location});
    }
    protected Object makeButtonStateBorder(String location, int tileCount, boolean isTiledHorizontaly,
            Insets imageInsets, Insets borderInsets, boolean fill) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaBorderFactory", "createButtonStateBorder",
                new Object[]{location, tileCount, isTiledHorizontaly, imageInsets, borderInsets, fill});

    }

    protected Object makeNativeButtonStateBorder(OSXAquaPainter.Widget widget, 
            Insets imageInsets, Insets borderInsets, boolean withFocusRing) {
        return new UIDefaults.ProxyLazyValue(
                "ch.randelshofer.quaqua.QuaquaBorderFactory", "createNativeButtonStateBorder",
                new Object[]{widget, imageInsets, borderInsets, withFocusRing});

    }


    /**
     * Init design specific look and feel defaults.
     * @param table Onto which defaults are appended.
     */
    protected void initDesignDefaults(UIDefaults table) {
    }

    /**
     * Returns true if the <code>LookAndFeel</code> returned
     * <code>RootPaneUI</code> instances support providing Window decorations
     * in a <code>JRootPane</code>.
     * <p>
     * This implementation returns true, since it does support providing
     * these border and window title pane decorations.
     *
     * @return True if the RootPaneUI instances created support client side
     *              decorations
     * @see JDialog#setDefaultLookAndFeelDecorated
     * @see JFrame#setDefaultLookAndFeelDecorated
     * @see JRootPane#setWindowDecorationStyle
     * @since 1.4
     */
    @Override
    public boolean getSupportsWindowDecorations() {
        return false;
    }

    protected boolean isJaguarTabbedPane() {
        String property;
        try {
            property = QuaquaManager.getProperty("Quaqua.tabLayoutPolicy");
            if (property == null) {
                property = QuaquaManager.getProperty("Quaqua.TabbedPane.design", "auto");
            }
        } catch (AccessControlException e) {
            property = "auto";
        }
        return property.equals("jaguar") || property.equals("wrap");
    }

    protected void installKeyboardFocusManager() {
        if (QuaquaManager.getProperty("Quaqua.TextComponent.autoSelect", "true").equals("true")) {
            String javaVersion = QuaquaManager.getProperty("java.version", "");
            if (javaVersion.startsWith("1.5")) {
                try {
                    KeyboardFocusManager.setCurrentKeyboardFocusManager(new QuaquaKeyboardFocusManager());
                } catch (SecurityException ex) {
                    System.err.print("Warning: " + this + " couldn't install QuaquaKeyboardFocusManager.");
                    //ex.printStackTrace();
                }
            }
        }
    }

    /** Installs the QuaquaPopupFactory if the PopupMenuUI is included. */
    protected void installPopupFactory() {
        // Fix for issue 132: Don't install QuaquaPopupFactory, because it
        // causes popups to appear behind dialog windows if one of the window
        // ancestors has "alwaysOnTop" set to true.
        /*
        if (isUIIncluded("PopupMenuUI")) {
            if (QuaquaManager.getOS() >= QuaquaManager.LEOPARD) {
                try {
                    PopupFactory.setSharedInstance(new QuaquaPopupFactory());
                } catch (SecurityException ex) {
                    System.err.print("Warning: " + this + " couldn't install QuaquaPopupFactory.");
                    //ex.printStackTrace();
                }
            }
        }*/
    }

    /** Installs the QuaquaPopupMenuUI.MouseGrabber if the PopupMenuUI is included. */
    protected void installMouseGrabber() {
        if (isUIIncluded("PopupMenuUI")) {
            AppContext context = AppContext.getAppContext();
            synchronized (QuaquaPopupMenuUI.MOUSE_GRABBER_KEY) {
                Object mouseGrabber = context.get(
                        QuaquaPopupMenuUI.MOUSE_GRABBER_KEY);
                if (mouseGrabber == null) {
                    mouseGrabber = new QuaquaPopupMenuUI.MouseGrabber();
                    context.put(QuaquaPopupMenuUI.MOUSE_GRABBER_KEY, mouseGrabber);
                }
            }
            synchronized (QuaquaPopupMenuUI.MENU_KEYBOARD_HELPER_KEY) {
                Object helper =
                        context.get(QuaquaPopupMenuUI.MENU_KEYBOARD_HELPER_KEY);
                if (helper == null) {
                    helper = new QuaquaPopupMenuUI.MenuKeyboardHelper();
                    context.put(QuaquaPopupMenuUI.MENU_KEYBOARD_HELPER_KEY, helper);
                    MenuSelectionManager msm = MenuSelectionManager.defaultManager();
                    msm.addChangeListener((QuaquaPopupMenuUI.MenuKeyboardHelper) helper);
                }
            }
        }
    }

    protected void uninstallMouseGrabber() {
        AppContext context = AppContext.getAppContext();
        synchronized (QuaquaPopupMenuUI.MOUSE_GRABBER_KEY) {
            Object mouseGrabber = context.get(
                    QuaquaPopupMenuUI.MOUSE_GRABBER_KEY);
            if (mouseGrabber instanceof QuaquaPopupMenuUI.MouseGrabber) {
                ((QuaquaPopupMenuUI.MouseGrabber) mouseGrabber).uninstall();
                context.put(QuaquaPopupMenuUI.MOUSE_GRABBER_KEY, null);
            }
        }
        synchronized (QuaquaPopupMenuUI.MENU_KEYBOARD_HELPER_KEY) {
            Object helper =
                    context.get(QuaquaPopupMenuUI.MENU_KEYBOARD_HELPER_KEY);
            if (helper instanceof QuaquaPopupMenuUI.MenuKeyboardHelper) {
                context.put(QuaquaPopupMenuUI.MENU_KEYBOARD_HELPER_KEY, null);
                ((QuaquaPopupMenuUI.MenuKeyboardHelper) helper).uninstall();
                MenuSelectionManager msm = MenuSelectionManager.defaultManager();
                msm.removeChangeListener((QuaquaPopupMenuUI.MenuKeyboardHelper) helper);
            }
        }
    }

    /** Use this to test if an UI is included.
     * An UI may be implicitly or explicitly included, or may be explicitly 
     * excluded.  
     * 
     * @param ui For example "LabelUI".
     * @return True if UI is included.
     */
    protected boolean isUIIncluded(String ui) {
        Set included = QuaquaManager.getIncludedUIs();
        Set excluded = QuaquaManager.getExcludedUIs();

        if (excluded == null) {
            // everyting is implicitly excluded
            return false;
        } else if (included == null && excluded.isEmpty()) {
            // everyting is implicitly included, nothing is explicitly excluded
            return true;
        } else if (included != null && excluded.isEmpty()) {
            // something is explicitly included, nothing is explicitly excluded
            return included.contains(ui);
        } else if (included == null) {
            return !excluded.contains(ui);
        } else {
            // something is explicitly included, something is explicitly excluded
            return included.contains(ui) && !excluded.contains(ui);
        }
    }

    /**
     * Puts defaults into the specified UIDefaults table.
     * Honors QuaquaManager.getIncludedUIs() and QuaquaManager.getExcludedUIs().
     * 
     * @param table Table onto which defaults are appended.
     * @param keyValueList Key value list of the defaults.
     */
    protected void putDefaults(UIDefaults table, Object[] keyValueList) {
        Set included = QuaquaManager.getIncludedUIs();
        Set excluded = QuaquaManager.getExcludedUIs();

        if (excluded == null) {
            // everyting is implicitly excluded
            return;
        } else if (included == null && excluded.isEmpty()) {
            // everyting is implicitly included, nothing is explicitly excluded
            table.putDefaults(keyValueList);
        } else if (included != null && excluded.isEmpty()) {
            // something is explicitly included, nothing is explicitly excluded
            for (int i = 0; i < keyValueList.length; i += 2) {
                if (keyValueList[i] instanceof String) {
                    String name = (String) keyValueList[i];
                    int p = name.indexOf('.');
                    if (p == -1 && name.endsWith("UI")) {
                        name = name.substring(0, name.length() - 2);
                        p = 1;
                    } else if (p != -1) {
                        name = name.substring(0, p);
                    }
                    if (p == -1 || name.equals("Component") || included.contains(name)) {
                        table.put(keyValueList[i], keyValueList[i + 1]);
                    }
                } else {
                    table.put(keyValueList[i], keyValueList[i + 1]);
                }
            }
        } else if (included == null) {
            // everything is implicitly included, something is explicitly excluded
            for (int i = 0; i < keyValueList.length; i += 2) {
                if (keyValueList[i] instanceof String) {
                    String name = (String) keyValueList[i];
                    int p = name.indexOf('.');
                    if (p == -1 && name.endsWith("UI")) {
                        name = name.substring(0, name.length() - 2);
                        p = 1;
                    } else if (p != -1) {
                        name = name.substring(0, p);
                    }
                    if (p == -1 || !excluded.contains(name)) {
                        table.put(keyValueList[i], keyValueList[i + 1]);
                    }
                } else {
                    table.put(keyValueList[i], keyValueList[i + 1]);
                }
            }
        } else {
            // something is explicitly included, something is explicitly excluded
            for (int i = 0; i < keyValueList.length; i += 2) {
                if (keyValueList[i] instanceof String) {
                    String name = (String) keyValueList[i];
                    int p = name.indexOf('.');
                    if (p == -1 && name.endsWith("UI")) {
                        name = name.substring(0, name.length() - 2);
                        p = 1;
                    } else if (p != -1) {
                        name = name.substring(0, p);
                    }
                    if (p == -1 || //
                            (name.equals("Component") || included.contains(name))//
                            && !excluded.contains(name)) {
                        table.put(keyValueList[i], keyValueList[i + 1]);
                    }
                } else {
                    table.put(keyValueList[i], keyValueList[i + 1]);
                }
            }
        }
    }

    @Override
    public void uninitialize() {
        uninstallPopupFactory();
        uninstallKeyboardFocusManager();
        uninstallMouseGrabber();
        super.uninitialize();
    }

    protected void uninstallPopupFactory() {
        try {
            if (PopupFactory.getSharedInstance() instanceof QuaquaPopupFactory) {
                PopupFactory.setSharedInstance(new PopupFactory());
            }
        } catch (SecurityException ex) {
            System.err.print("Warning: " + this + " couldn't uninstall QuaquaPopupFactory.");
            //ex.printStackTrace();
        }
    }

    protected void uninstallKeyboardFocusManager() {
        try {
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager() instanceof QuaquaKeyboardFocusManager) {
                KeyboardFocusManager.setCurrentKeyboardFocusManager(new DefaultKeyboardFocusManager());
            }
            // currentManager.

        } catch (SecurityException ex) {
            System.err.print("Warning: " + this + " couldn't uninstall QuaquaKeyboardFocusManager.");
            //ex.printStackTrace();
        }
    }
}
