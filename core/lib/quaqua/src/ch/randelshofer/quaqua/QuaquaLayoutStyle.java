/*
 * @(#)QuaquaLayoutStyle.java
 *
 * Copyright (c) 2007-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import java.awt.*;
import javax.swing.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * A QuaquaLayoutStyle can be queried for the preferred gaps between two
 * JComponents, or between a JComponent and a parent Container.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaLayoutStyle.java 464 2014-03-22 12:32:00Z wrandelshofer $
 */
public class QuaquaLayoutStyle extends LayoutStyle {

    private final static boolean DEBUG = false;
    /** Mini size style. */
    private final static int MINI = 0;
    /** Small size style. */
    private final static int SMALL = 1;
    /** Regular size style. */
    private final static int REGULAR = 2;
    /**
     * The containerGapDefinitions array defines the preferred insets (child gaps)
     * of a parent container towards one of its child components.
     *
     * Note: As of now, we do not yet specify the preferred gap from a child
     * to its parent. Therefore we may not be able to treat all special cases.
     *
     * This array is used to initialize the containerGaps HashMap.
     *
     * The array has the following structure, which is supposed to be a
     * a compromise between legibility and code size.
     * containerGapDefinitions[0..n] = preferred insets for some parent UI's
     * containerGapDefinitions[][0..m-3] = name of parent UI,
     *                                 optionally followed by a full stop and
     *                                 a style name
     * containerGapDefinitions[][m-2] = mini insets
     * containerGapDefinitions[][m-1] = small insets
     * containerGapDefinitions[][m] = regular insets
     */
    private final static Object[][] containerGapDefinitions = {
        // Format:
        // { list of parent UI's,
        //   mini insets, small insets, regular insets }

        {"TabbedPaneUI",
            new Insets(6, 10, 10, 10), new Insets(6, 10, 10, 12), new Insets(12, 20, 20, 20)
        },
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGLayout/chapter_19_section_3.html#//apple_ref/doc/uid/TP30000360/DontLinkElementID_27
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGLayout/chapter_19_section_3.html#//apple_ref/doc/uid/TP30000360/DontLinkElementID_26
        // note for small and mini size: leave 8 to 10 pixels on top
        // note for regular size: leave only 12 pixel at top if tabbed pane UI
        {"RootPaneUI",
            new Insets(8, 10, 10, 10), new Insets(8, 10, 10, 12), new Insets(14, 20, 20, 20)
        },
        // These child gaps are used for all other components
        {"default",
            new Insets(8, 10, 10, 10), new Insets(8, 10, 10, 12), new Insets(14, 20, 20, 20)
        },};
    /**
     * The relatedGapDefinitions table defines the preferred gaps
     * of one party of two related components.
     *
     * The effective preferred gap is the maximum of the preferred gaps of
     * both parties.
     *
     * This array is used to initialize the relatedGaps HashMap.
     *
     * The array has the following structure, which is supposed to be a
     * a compromise between legibility and code size.
     * containerGapDefinitions[0..n] = preferred gaps for a party of a two related UI's
     * containerGapDefinitions[][0..m-3] = name of UI
     *                                 optionally followed by a full stop and
     *                                 a style name
     * containerGapDefinitions[][m-2] = mini insets
     * containerGapDefinitions[][m-1] = small insets
     * containerGapDefinitions[][m] = regular insets
     */
    private final static Object[][] relatedGapDefinitions = {
        // Format:
        // { list of UI's,
        //   mini insets, small insets, regular insets }

        // Push Button:
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_2.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF104
        {"ButtonUI", "ButtonUI.push", "ButtonUI.text",
            "ToggleButtonUI.push", "ToggleButtonUI.text",
            new Insets(8, 8, 8, 8), new Insets(10, 10, 10, 10), new Insets(12, 12, 12, 12)
        },
        // Metal Button
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_2.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF187
        {"ButtonUI.metal", "ToggleButtonUI.metal",
            new Insets(8, 8, 8, 8), new Insets(8, 8, 8, 8), new Insets(12, 12, 12, 12)
        },
        // Bevel Button (Rounded and Square)
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_2.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF112
        {"ButtonUI.bevel", "ButtonUI.toggle", "ButtonUI.square",
            "ToggleButtonUI", "ToggleButtonUI.bevel", "ToggleButtonUI.square", "ToggleButtonUI.toggle",
            new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)
        },
        // Bevel Button (Rounded and Square)
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_2.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF112
        {"ButtonUI.bevel.largeIcon", "ToggleButtonUI.bevel.largeIcon",
            new Insets(8, 8, 8, 8), new Insets(8, 8, 8, 8), new Insets(8, 8, 8, 8)
        },
        // Icon Button
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_2.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF189
        {"ButtonUI.icon",
            new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)
        },
        {"ButtonUI.icon.largeIcon",
            new Insets(8, 8, 8, 8), new Insets(8, 8, 8, 8), new Insets(8, 8, 8, 8)
        },
        // Round Button
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_2.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF191
        {"ButtonUI.round", "ToggleButtonUI.round",
            new Insets(12, 12, 12, 12), new Insets(12, 12, 12, 12), new Insets(12, 12, 12, 12)
        },
        // Help Button
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_2.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF193
        {"ButtonUI.help",
            new Insets(12, 12, 12, 12), new Insets(12, 12, 12, 12), new Insets(12, 12, 12, 12)
        },
        // Segmented Control
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_3.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF196
        {"ButtonUI.toggleCenter", "ToggleButtonUI.toggleCenter",
            new Insets(8, 0, 8, 0), new Insets(10, 0, 10, 0), new Insets(12, 0, 12, 0)
        },
        {"ButtonUI.toggleEast", "ToggleButtonUI.toggleEast",
            new Insets(8, 0, 8, 8), new Insets(10, 0, 10, 10), new Insets(12, 0, 12, 12)
        },
        {"ButtonUI.toggleWest", "ToggleButtonUI.toggleWest",
            new Insets(8, 8, 8, 0), new Insets(10, 10, 10, 0), new Insets(12, 12, 12, 0)
        },
        {"ButtonUI.toolBarTab", "ToggleButtonUI.toolBarTab",
            new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)
        },
        // Color Well Button
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_3.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF213
        {"ButtonUI.colorWell", "ToggleButtonUI.colorWell",
            new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)
        },
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_3.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF198
        // FIXME - The following values are given in the AHIG.
        // In reality, the values further below seem to be more appropriate.
        // Which ones are right?
        //{ "CheckBoxUI", new Insets(7, 5, 7, 5), new Insets(8, 6, 8, 6), new Insets(8, 8, 8, 8) },
        {"CheckBoxUI",
            new Insets(6, 5, 6, 5), new Insets(7, 6, 7, 6), new Insets(7, 6, 7, 6)
        },
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_3.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF198
        {"ComboBoxUI",
            new Insets(8, 5, 8, 5), new Insets(10, 6, 10, 6), new Insets(12, 8, 12, 8)
        },
        // There is no spacing given for labels in Apples Guidelines.
        // We use the values here, which is the minimum of the spacing of all
        // other components.
        {"LabelUI",
            new Insets(6, 8, 6, 8), new Insets(6, 8, 6, 8), new Insets(6, 8, 6, 8)
        },
        // ? spacing not given
        {"ListUI",
            new Insets(5, 5, 5, 5), new Insets(6, 6, 6, 6), new Insets(6, 6, 6, 6)
        },
        // ? spacing not given
        {"PanelUI",
            new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)
        },
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_5.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF106
        // ? spacing not given
        {"ProgressBarUI",
            new Insets(8, 8, 8, 8), new Insets(10, 10, 10, 10), new Insets(12, 12, 12, 12)
        },
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_3.html#//apple_ref/doc/uid/20000957-TP30000359-BIAHBFAD
        {"RadioButtonUI",
            new Insets(5, 5, 5, 5), new Insets(6, 6, 6, 6), new Insets(6, 6, 6, 6)
        },
        //http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_6.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF114
        // ? spacing not given. We use the same like for text fields
        {"ScrollPaneUI",
            new Insets(6, 8, 6, 8), new Insets(6, 8, 6, 8), new Insets(8, 10, 8, 10)
        },
        //http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_8.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF214
        // ? spacing not given
        //http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGLayout/chapter_19_section_2.html#//apple_ref/doc/uid/20000957-TP30000360-CHDEACGD
        {"SeparatorUI",
            new Insets(8, 8, 8, 8), new Insets(10, 10, 10, 10), new Insets(12, 12, 12, 12)
        },
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_4.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF115
        {"SliderUI.horizontal",
            new Insets(6, 8, 6, 8), new Insets(6, 10, 6, 10), new Insets(6, 12, 6, 12)
        },
        {"SliderUI.vertical",
            new Insets(8, 8, 8, 8), new Insets(10, 10, 10, 10), new Insets(12, 12, 12, 12)
        },
        //http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_4.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF204
        {"SpinnerUI",
            new Insets(6, 8, 6, 8), new Insets(6, 8, 6, 8), new Insets(8, 10, 8, 10)
        },
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_7.html#//apple_ref/doc/uid/20000957-TP30000359-CHDDBIJE
        // ? spacing not given
        {"SplitPaneUI",
            new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)
        },
        // http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_7.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF105
        // ? spacing not given
        {"TabbedPaneUI",
            new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)
        },
        {"TableUI",
            new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)
        },
        // ? spacing not given
        {"TextAreaUI", "EditorPaneUI", "TextPaneUI",
            new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)
        },
        //http://developer.apple.com/documentation/UserExperience/Conceptual/OSXHIGuidelines/XHIGControls/chapter_18_section_6.html#//apple_ref/doc/uid/20000957-TP30000359-TPXREF225
        {"TextFieldUI", "FormattedTextFieldUI", "PasswordFieldUI",
            new Insets(6, 8, 6, 8), new Insets(6, 8, 6, 8), new Insets(8, 10, 8, 10)
        },
        // ? spacing not given
        {"TreeUI",
            new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)
        },};
    private final static Object[][] unrelatedGapDefinitions = {
        // UI, mini, small, regular
        {"ButtonUI.help",
            new Insets(24, 24, 24, 24), new Insets(24, 24, 24, 24), new Insets(24, 24, 24, 24)
        },
        {"default",
            new Insets(12, 12, 12, 12), new Insets(14, 14, 14, 14), new Insets(16, 16, 16, 16)
        },};
    /**
     * The indentGapDefinitions table defines the preferred indentation
     * for components that are indented after the specified component.
     *
     * This array is used to initialize the indentGaps HashMap.
     *
     * The array has the following structure, which is supposed to be a
     * a compromise between legibility and code size.
     * indentGapDefinitions[0..n] = preferred gaps for a party of a two related UI's
     * indentGapDefinitions[][0..m-3] = name of UI
     *                                 optionally followed by a full stop and
     *                                 a style name
     * indentGapDefinitions[][m-2] = mini insets
     * indentGapDefinitions[][m-1] = small insets
     * indentGapDefinitions[][m] = regular insets
     */
    private final static Object[][] indentGapDefinitions = {
        // UI, mini, small, regular
        {"default",
            new Insets(16, 16, 16, 16), new Insets(20, 20, 20, 20), new Insets(25, 25, 25, 25)
        },};

    /**
     * Creates a hash map for the specified definitions array.
     *
     * Each entry of the hash map has the name of an UI (optionally followed by
     * a full stop and a style name) as its key <String>
     * and an array of Insets as its value <Insets[]>.
     */
    private static HashMap createMap(Object[][] definitions) {
        HashMap map = new HashMap();
        for (int i = 0; i < definitions.length; i++) {
            int keys = 0;
            while (keys < definitions[i].length
                    && (definitions[i][keys] instanceof String)) {
                keys++;
            }
            Insets[] values = new Insets[definitions[i].length - keys];
            for (int j = keys; j < definitions[i].length; j++) {
                values[j - keys] = (Insets) definitions[i][j];
            }
            for (int j = 0; j < keys; j++) {
                map.put(definitions[i][j], values);
            }
        }
        return map;
    }
    /**
     * The relatedGapDefinitions table defines the preferred gaps
     * of one party of two related components.
     */
    private final static HashMap relatedGaps = createMap(relatedGapDefinitions);
    /**
     * The unrelatedGapDefinitions table defines the preferred gaps
     * of one party of two unrelated components.
     */
    private final static HashMap unrelatedGaps = createMap(unrelatedGapDefinitions);
    /**
     * The containerGapDefinitions array defines the preferred insets (child gaps)
     * of a parent component towards one of its children.
     */
    private final static HashMap containerGaps = createMap(containerGapDefinitions);
    /**
     * The indentGapDefinitions table defines the preferred indentation
     * for components that are indented after the specified component.
     */
    private final static HashMap indentGaps = createMap(indentGapDefinitions);

    /**
     * Creates a new instance.
     */
    public QuaquaLayoutStyle() {
    }

    /**
     * Returns the amount of space to use between two components.
     * The return value indicates the distance to place
     * <code>component2</code> relative to <code>component1</code>.
     * For example, the following returns the amount of space to place
     * between <code>component2</code> and <code>component1</code>
     * when <code>component2</code> is placed vertically above
     * <code>component1</code>:
     * <pre>
     *   int gap = getPreferredGap(component1, component2,
     *                             LayoutStyle.RELATED,
     *                             SwingConstants.NORTH, parent);
     * </pre>
     * The <code>type</code> parameter indicates the type
     * of gap being requested.  It can be one of the following values:
     * <table>
     * <caption>Meaning of type values</caption>
     * <tr><td><code>RELATED</code>
     *     <td>If the two components will be contained in
     *         the same parent and are showing similar logically related
     *         items, use <code>RELATED</code>.
     * <tr><td><code>UNRELATED</code>
     *     <td>If the two components will be
     *          contained in the same parent but show logically unrelated items
     *          use <code>UNRELATED</code>.
     * <tr><td><code>INDENT</code>
     *     <td>Used to obtain the preferred distance to indent a component
     *         relative to another.  For example, if you want to horizontally
     *         indent a JCheckBox relative to a JLabel use <code>INDENT</code>.
     *         This is only useful for the horizontal axis.
     * </table>
     * <p>
     * It's important to note that some look and feels may not distinguish
     * between <code>RELATED</code> and <code>UNRELATED</code>.
     * <p>
     * The return value is not intended to take into account the
     * current size and position of <code>component2</code> or
     * <code>component1</code>.  The return value may take into
     * consideration various properties of the components.  For
     * example, the space may vary based on font size, or the preferred
     * size of the component.
     *
     * @param component1 the <code>JComponent</code>
     *               <code>component2</code> is being placed relative to
     * @param component2 the <code>JComponent</code> being placed
     * @param type how the two components are being placed
     * @param position the position <code>component2</code> is being placed
     *        relative to <code>component1</code>; one of
     *        <code>SwingConstants.NORTH</code>,
     *        <code>SwingConstants.SOUTH</code>,
     *        <code>SwingConstants.EAST</code> or
     *        <code>SwingConstants.WEST</code>
     * @param parent the parent of <code>component2</code>; this may differ
     *        from the actual parent and may be null
     * @return the amount of space to place between the two components
     * @throws IllegalArgumentException if <code>position</code> is not
     *         one of <code>SwingConstants.NORTH</code>,
     *         <code>SwingConstants.SOUTH</code>,
     *         <code>SwingConstants.EAST</code> or
     *         <code>SwingConstants.WEST</code>; <code>type</code> not one
     *         of <code>INDENT</code>, <code>RELATED</code>
     *         or <code>UNRELATED</code>; or <code>component1</code> or
     *         <code>component2</code> is null
     */
    public int getPreferredGap(JComponent component1, JComponent component2,
            javax.swing.LayoutStyle.ComponentPlacement type, int position, Container parent) {
        int result;

        if (type == javax.swing.LayoutStyle.ComponentPlacement.INDENT) {
            // Compute gap
            int sizeVariant = getSizeVariant(component1);
            Insets vgap = getVisualIndent(component1);
            Insets pgap = getPreferredGap(component1, javax.swing.LayoutStyle.ComponentPlacement.INDENT, sizeVariant);
            switch (position) {
                case SwingConstants.NORTH:
                    result = (vgap.bottom > 8) ? vgap.bottom : pgap.bottom;
                    break;
                case SwingConstants.SOUTH:
                    result = (vgap.top > 8) ? vgap.top : pgap.top;
                    break;
                case SwingConstants.EAST:
                    result = (vgap.left > 8) ? vgap.left : pgap.left;
                    break;
                case SwingConstants.WEST:
                default:
                    result = (vgap.right > 8) ? vgap.right : pgap.right;
                    break;
            }

            // Compensate for visual margin
            Insets visualMargin2 = getVisualMargin(component2);
            switch (position) {
                case SwingConstants.NORTH:
                    result -= visualMargin2.bottom;
                    break;
                case SwingConstants.SOUTH:
                    result -= visualMargin2.top;
                    break;
                case SwingConstants.EAST:
                    result -= visualMargin2.left;
                    break;
                case SwingConstants.WEST:
                    result -= visualMargin2.right;
                default:
                    break;
            }
        } else {

            // If the two components have different size styles, we use the
            // smaller size style to determine the gap
            int sizeVariant = Math.min(getSizeVariant(component1), getSizeVariant(component2));
            Insets gap1 = getPreferredGap(component1, type, sizeVariant);
            Insets gap2 = getPreferredGap(component2, type, sizeVariant);

            // The AHIG defines the minimal spacing for a component
            // therefore we use the larger of the two gap values.
            switch (position) {
                case SwingConstants.NORTH:
                    result = Math.max(gap1.top, gap2.bottom);
                    break;
                case SwingConstants.SOUTH:
                    result = Math.max(gap1.bottom, gap2.top);
                    break;
                case SwingConstants.EAST:
                    result = Math.max(gap1.right, gap2.left);
                    break;
                case SwingConstants.WEST:
                default:
                    result = Math.max(gap1.left, gap2.right);
                    break;
            }

            // Compensate for visual margin
            Insets visualMargin1 = getVisualMargin(component1);
            Insets visualMargin2 = getVisualMargin(component2);

            switch (position) {
                case SwingConstants.NORTH:
                    result -= visualMargin1.top + visualMargin2.bottom;
                    break;
                case SwingConstants.SOUTH:
                    result -= visualMargin1.bottom + visualMargin2.top;
                    break;
                case SwingConstants.EAST:
                    result -= visualMargin1.right + visualMargin2.left;
                    break;
                case SwingConstants.WEST:
                    result -= visualMargin1.left + visualMargin2.right;
                default:
                    break;
            }
        }
        //System.out.println("QuaquaLayoutStyle.getPreferredGap:"+component1.getClass()+"@"+component1.hashCode()+","+position+","+component2.getClass()+"@"+component2.hashCode()+":"+result);

        return result;
    }

    private Insets getPreferredGap(JComponent component, javax.swing.LayoutStyle.ComponentPlacement type, int sizeStyle) {
        Insets gap = null;

        HashMap gapMap;

        switch (type) {
            case INDENT:
                gapMap = indentGaps;
                break;
            case RELATED:
                gapMap = relatedGaps;
                break;
            case UNRELATED:
            default:
                gapMap = unrelatedGaps;
                break;
        }

        String uid = component.getUIClassID();
        String style = null;
        if (uid.equals("ButtonUI") || uid.equals("ToggleButtonUI")) {
            style = (String) component.getClientProperty("Quaqua.Button.style");
            if (style == null) {
                style = (String) component.getClientProperty("JButton.buttonType");
            }
        } else if (uid.equals("ProgressBarUI")) {
            style = (((JProgressBar) component).getOrientation() == JProgressBar.HORIZONTAL)
                    ? "horizontal"
                    : "vertical";
        } else if (uid.equals("SliderUI")) {
            style = (((JSlider) component).getOrientation() == JSlider.HORIZONTAL)
                    ? "horizontal"
                    : "vertical";
        } else if (uid.equals("TabbedPaneUI")) {
            switch (((JTabbedPane) component).getTabPlacement()) {
                case JTabbedPane.TOP:
                    style = "top";
                    break;
                case JTabbedPane.LEFT:
                    style = "left";
                    break;
                case JTabbedPane.BOTTOM:
                    style = "bottom";
                    break;
                case JTabbedPane.RIGHT:
                    style = "right";
                    break;
            }
        }
        String key = (style == null) ? uid : uid + "." + style;
        Insets[] gaps = (Insets[]) gapMap.get(key);
        if (gaps == null) {
            gaps = (Insets[]) gapMap.get(uid);
        }
        if (gaps == null) {
            gaps = (Insets[]) gapMap.get("default");
        }

        return (gaps == null) ? new Insets(0, 0, 0, 0) : gaps[sizeStyle];
    }

    /**
     * Returns the amount of space to position a component inside its
     * parent.
     *
     * @param component the <code>Component</code> being positioned
     * @param position the position <code>component</code> is being placed
     *        relative to its parent; one of
     *        <code>SwingConstants.NORTH</code>,
     *        <code>SwingConstants.SOUTH</code>,
     *        <code>SwingConstants.EAST</code> or
     *        <code>SwingConstants.WEST</code>
     * @param parent the parent of <code>component</code>; this may differ
     *        from the actual parent and may be null
     * @return the amount of space to place between the component and specified
     *         edge
     * @throws IllegalArgumentException if <code>position</code> is not
     *         one of <code>SwingConstants.NORTH</code>,
     *         <code>SwingConstants.SOUTH</code>,
     *         <code>SwingConstants.EAST</code> or
     *         <code>SwingConstants.WEST</code>;
     *         or <code>component</code> is null
     */
    public int getContainerGap(JComponent component, int position,
            Container parent) {
        int result;

        int sizeVariant = Math.min(getSizeVariant(component), getSizeVariant(parent));

        // Compute gap
        Insets gap = getContainerGap(parent, sizeVariant);

        switch (position) {
            case SwingConstants.NORTH:
                result = gap.top;
                break;
            case SwingConstants.SOUTH:
                result = gap.bottom;
                break;
            case SwingConstants.EAST:
                result = gap.right;
                break;
            case SwingConstants.WEST:
            default:
                result = gap.left;
                break;
        }

        // Compensate for visual margin
        Insets visualMargin = getVisualMargin(component);
        switch (position) {
            case SwingConstants.NORTH:
                result -= visualMargin.top;
                break;
            case SwingConstants.SOUTH:
                result -= visualMargin.bottom;
                // Radio buttons in Quaqua are 1 pixel too high, in order
                // to align their baselines with other components, when no
                // baseline aware layout manager is used.
                if (component instanceof JRadioButton) {
                    result--;
                }
                break;
            case SwingConstants.EAST:
                result -= visualMargin.left;
                break;
            case SwingConstants.WEST:
                result -= visualMargin.right;
            default:
                break;
        }
        //System.out.println("QuaquaLayoutStyle.getContainerGap:"+component.getClass()+"@"+component.hashCode()+","+position+","+parent.getClass()+":"+result);
        return result;
    }

    private Insets getContainerGap(Container container, int sizeStyle) {
        Insets gap = null;

        HashMap gapMap = containerGaps;

        String uid;
        if (container instanceof JComponent) {
            uid = ((JComponent) container).getUIClassID();
        } else if (container instanceof Dialog) {
            uid = "Dialog";
        } else if (container instanceof Frame) {
            uid = "Frame";
        } else if (container instanceof java.applet.Applet) {
            uid = "Applet";
        } else if (container instanceof Panel) {
            uid = "Panel";
        } else {
            uid = "default";
        }

        String style = null;
        // FIXME insert style code here for JInternalFrame with palette style
        String key = (style == null) ? uid : uid + "." + style;

        Insets[] gaps = (Insets[]) gapMap.get(key);
        if (gaps == null) {
            gaps = (Insets[]) gapMap.get(uid);
        }
        if (gaps == null) {
            gaps = (Insets[]) gapMap.get("default");
        }
        if (gaps == null) {
            if (DEBUG) {
                System.out.println("AquaLayoutStyle.noGaps for " + uid);
            }
        }
        return (gaps == null) ? new Insets(0, 0, 0, 0) : gaps[sizeStyle];
    }

    private Insets getVisualMargin(JComponent component) {
        try {
            Method getUI = component.getClass().getMethod("getUI", new Class[0]);
            Object ui = getUI.invoke(component, new Object[0]);
            if (ui instanceof VisuallyLayoutable) {
                Dimension size = component.getPreferredSize();
                Rectangle visualBounds = ((VisuallyLayoutable) ui).getVisualBounds(component, VisuallyLayoutable.COMPONENT_BOUNDS, size.width, size.height);
                return new Insets(visualBounds.y, visualBounds.x, size.height - visualBounds.y - visualBounds.height, size.width - visualBounds.x - visualBounds.width);
            }
        } catch (NoSuchMethodException e) {
            // This can happen for subclasses of JComponent, which do
            // not have an UI delegate.
            // Fall through.
        } catch (Exception e) {
            InternalError error = new InternalError();
            //error.initCause(e);
            throw error;
        }
        return new Insets(0, 0, 0, 0);
    }

    private Insets getVisualIndent(JComponent component) {
        try {
            Method getUI = component.getClass().getMethod("getUI", new Class[0]);
            Object ui = getUI.invoke(component, new Object[0]);
            if (ui instanceof VisuallyLayoutable) {
                Dimension size = component.getPreferredSize();
                Rectangle visualBounds = ((VisuallyLayoutable) ui).getVisualBounds(component, VisuallyLayoutable.TEXT_BOUNDS, size.width, size.height);
                return new Insets(visualBounds.y, visualBounds.x, size.height - visualBounds.y - visualBounds.height, size.width - visualBounds.x - visualBounds.width);
            }
        } catch (NoSuchMethodException e) {
            // This can happen for subclasses of JComponent, which do
            // not have an UI delegate.
            // Fall through
        } catch (Exception e) {
            InternalError error = new InternalError();
            // error.initCause(e);
            throw error;
        }
        return new Insets(0, 0, 0, 0);
    }

    /**
     * Returns the size variant of a specified component.
     *
     * @return REGULAR, SMALL or MINI.
     */
    private int getSizeVariant(Component c) {
        // Look for size variant client property
        if (c instanceof JComponent) {
            String variant = (String) ((JComponent) c).getClientProperty("JComponent.sizeVariant");
            if (variant != null) {
                if (variant.equals("regular")) {
                    return REGULAR;
                }
                if (variant.equals("mini")) {
                    return MINI;
                }
                if (variant.equals("small")) {
                    return SMALL;
                }
            }
        }
        // Aqua components have a different style depending on the
        // font size used.
        // 13 Point = Regular
        // 11 Point = Small
        //  9 Point = Mini
        Font f = c.getFont();
        if (f==null) return REGULAR;
        int fontSize = f.getSize();
        return (fontSize >= 13) ? REGULAR : ((fontSize > 9) ? SMALL : MINI);
    }
}
