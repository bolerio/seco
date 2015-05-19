/*
 * @(#)QuaquaJaguarTabbedPaneUI.java  
 *
 * Copyright (c) 2001-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.jaguar;

import ch.randelshofer.quaqua.*;
import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.util.Debug;
import ch.randelshofer.quaqua.util.NavigatableTabbedPaneUI;
import ch.randelshofer.quaqua.color.PaintableColor;
import ch.randelshofer.quaqua.color.TextureColor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import java.util.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A replacement for the AquaTabbedPaneUI for Mac OS X 10.2 Jaguar.
 * Tabs of tabbed panes are stacked instead of moved into a popup menu,
 * if not enough space is available to render all tabs in a single line.
 * <p>
 * Supports the following client properties on the children of the JTabbedPane:
 * <code>Quaqua.TabbedPaneChild.contentBackground</code> specifies the background 
 * Color to be used to fill the content border.
 * <code>Quaqua.TabbedPaneChild.contentInsets</code> specifies the insets 
 * to be used to lay out the child component inside the JTabbedPane.
 *
 * @author Werner Randelshofer, Switzerland
 * @version $Id: QuaquaJaguarTabbedPaneUI.java 464 2014-03-22 12:32:00Z wrandelshofer $
 */
public class QuaquaJaguarTabbedPaneUI extends BasicTabbedPaneUI
        implements VisuallyLayoutable, NavigatableTabbedPaneUI {
    // private HierarchyListener hierarchyListener;

    protected int minTabWidth = 40;
    protected Color tabAreaBackground;
    protected Color selectColor;
    protected Color selectHighlight;
    protected Color disabledForeground;
    private Insets currentContentBorderInsets = new Insets(0, 0, 0, 0);
    private int tabCount;
    /**
     * This variable is set in method paint() and used in all subsequent paint...() methods.
     */
    private boolean isFrameActive;

    /**
     * This is the border painted around the content area.
     * Don't cache this border because it usually encompasses a huge are on
     * a panel.
     */
    private Border getContentBorder() {
        return UIManager.getBorder("TabbedPane.wrap.contentBorder");
    }

    private static Border createImageBorder(String name, Insets insets) {
        return QuaquaBorderFactory.create(
                Images.createImage(QuaquaJaguarTabbedPaneUI.class.getResource(name)),
                insets);
    }

    private static Border createNonCachedImageBorder(String name, Insets insets) {
        return QuaquaBorderFactory.create(
                Images.createImage(QuaquaJaguarTabbedPaneUI.class.getResource(name)),
                insets, insets, true, null, false);
    }

    /**
     * This is the bar used when the tabs are at the top.
     *
     * Indices 0: active
     *         1: inactive
     *         2: disabled
     */
    private Border getBarTopBorder(int i) {
        Border[] borders = (Border[]) UIManager.get("TabbedPane.wrapBarTopBorders");
        return borders[i];
    }

    /**
     * This is the bar used when the tabs are at the bottom.
     */
    private Border getBarBottomBorder(int i) {
        Border[] borders = (Border[]) UIManager.get("TabbedPane.wrapBarBottomBorders");
        return borders[i];
    }

    //--
    /**
     * This is the bar used when the tabs are at the right.
     */
    private Border getBarRightBorder(int i) {
        Border[] borders = (Border[]) UIManager.get("TabbedPane.wrapBarRightBorders");
        return borders[i];
    }

    /**
     * This is the bar used when the tabs are at the left.
     */
    private Border getBarLeftBorder(int i) {
        Border[] borders = (Border[]) UIManager.get("TabbedPane.wrapBarLeftBorders");
        return borders[i];
    }
    //--
    /**
     * This is a tab when the tabs are at the top.
     *
     * Indices 0: enabled
     *         1: selected
     *         2: inactive
     *         3: disabled
     *         4: disabled selected
     */
    private static Border[] tabTopBorder;

    private Border getTabTopBorder(int i) {
        if (tabTopBorder == null) {
            Insets insets = new Insets(12, 8, 11, 8);
            tabTopBorder = new Border[]{
                        createImageBorder("images/TabbedPane.tabTop.png", insets),
                        createImageBorder("images/TabbedPane.tabTop.S.png", insets),
                        createImageBorder("images/TabbedPane.tabTop.I.png", insets),
                        createImageBorder("images/TabbedPane.tabTop.D.png", insets),
                        createImageBorder("images/TabbedPane.tabTop.DS.png", insets),};
        }
        return tabTopBorder[i];
    }
    //--
    /**
     * This is a tab when the tabs are at the bottom.
     */
    private static Border[] tabBottomBorder;

    private Border getTabBottomBorder(int i) {
        if (tabBottomBorder == null) {
            Insets insets = new Insets(11, 8, 12, 8);
            tabBottomBorder = new Border[]{
                        createImageBorder("images/TabbedPane.tabBottom.png", insets),
                        createImageBorder("images/TabbedPane.tabBottom.S.png", insets),
                        createImageBorder("images/TabbedPane.tabBottom.I.png", insets),
                        createImageBorder("images/TabbedPane.tabBottom.D.png", insets),
                        createImageBorder("images/TabbedPane.tabBottom.DS.png", insets),};
        }
        return tabBottomBorder[i];
    }
    /**
     * This is a tab when the tabs are at the right.
     */
    private static Border[] tabRightBorder;

    private Border getTabRightBorder(int i) {
        if (tabRightBorder == null) {
            Insets insets = new Insets(11, 1, 11, 7);
            Insets insetsS = new Insets(11, 2, 11, 7);
            tabRightBorder = new Border[]{
                        createImageBorder("images/TabbedPane.tabRight.png", insets),
                        createImageBorder("images/TabbedPane.tabRight.S.png", insetsS),
                        createImageBorder("images/TabbedPane.tabRight.I.png", insetsS),
                        createImageBorder("images/TabbedPane.tabRight.D.png", insets),
                        createImageBorder("images/TabbedPane.tabRight.DS.png", insetsS),};
        }
        return tabRightBorder[i];
    }
    /**
     * This is a tab when the tabs are at the left.
     */
    private static Border[] tabLeftBorder;

    private Border getTabLeftBorder(int i) {
        if (tabLeftBorder == null) {
            Insets insets = new Insets(11, 7, 11, 1);
            Insets insetsS = new Insets(11, 7, 11, 2);
            tabLeftBorder = new Border[]{
                        createImageBorder("images/TabbedPane.tabLeft.png", insets),
                        createImageBorder("images/TabbedPane.tabLeft.S.png", insetsS),
                        createImageBorder("images/TabbedPane.tabLeft.I.png", insetsS),
                        createImageBorder("images/TabbedPane.tabLeft.D.png", insets),
                        createImageBorder("images/TabbedPane.tabLeft.DS.png", insetsS),};
        }
        return tabLeftBorder[i];
    }
    private Hashtable mnemonicToIndexMap;
    /**
     * InputMap used for mnemonics. Only non-null if the JTabbedPane has
     * mnemonics associated with it. Lazily created in initMnemonics.
     */
    private InputMap mnemonicInputMap;
    /**
     * Changeable UIManager property prefix.
     * The value is changed by QuaquaPantherTabbedPaneUI.
     */
    private String propertyPrefix = "TabbedPane" + ".";

    protected String getPropertyPrefix() {
        return propertyPrefix;
    }

    public void setPropertyPrefix(String newValue) {
        propertyPrefix = newValue;
    }

    public static ComponentUI createUI(JComponent x) {
        return new QuaquaJaguarTabbedPaneUI();
    }

    @Override
    protected LayoutManager createLayoutManager() {
        return new TabbedPaneLayout();
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return new QuaquaPropertyChangeHandler();
    }

    @Override
    protected MouseListener createMouseListener() {
        return new QuaquaMouseHandler();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();

        String prefix = getPropertyPrefix();

        LookAndFeel.installColorsAndFont(tabPane, prefix + "background",
                prefix + "foreground", prefix + "font");
        // Workaround for Java 1.4: For some reason, the background color 
        // is not set by LookAndFeel.installColorsAndFont.
        tabPane.setBackground(UIManager.getColor(prefix + "background"));

        highlight = UIManager.getColor(prefix + "light");
        lightHighlight = UIManager.getColor(prefix + "highlight");
        shadow = UIManager.getColor(prefix + "shadow");
        darkShadow = UIManager.getColor(prefix + "darkShadow");
        focus = UIManager.getColor(prefix + "focus");

        textIconGap = UIManager.getInt(prefix + "textIconGap");
        tabInsets = UIManager.getInsets(prefix + "tabInsets");
        selectedTabPadInsets = UIManager.getInsets(prefix + "selectedTabPadInsets");
        tabAreaInsets = UIManager.getInsets(prefix + "tabAreaInsets");
        contentBorderInsets = UIManager.getInsets(prefix + "contentBorderInsets");
        tabRunOverlay = UIManager.getInt(prefix + "tabRunOverlay");

        tabAreaBackground = UIManager.getColor(prefix + "tabAreaBackground");
        selectColor = UIManager.getColor(prefix + "selected");
        selectHighlight = UIManager.getColor(prefix + "selectHighlight");
        //selectColor = UIManager.getColor("MenuItem.selectionBackground");
        disabledForeground = UIManager.getColor(prefix + "disabledForeground");

        LookAndFeel.installBorder(tabPane, prefix + "border");

        tabPane.setOpaque(UIManager.getBoolean(prefix + "opaque"));
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement,
            int tabIndex, int x, int y, int w, int h,
            boolean isSelected) {
        int bottom = y + (h - 1);
        int right = x + (w - 1);

        switch (tabPlacement) {
            case LEFT:
                paintTabBorderLeft(tabIndex, g, x, y, w, h, bottom, right, isSelected);
                break;
            case BOTTOM:
                paintTabBorderBottom(tabIndex, g, x, y, w, h, bottom, right, isSelected);
                break;
            case RIGHT:
                paintTabBorderRight(tabIndex, g, x, y, w, h, bottom, right, isSelected);
                break;
            case TOP:
            default:
                paintTabBorderTop(tabIndex, g, x, y, w, h, bottom, right, isSelected);
        }
    }

    protected void paintTabBorderTop(int tabIndex, Graphics g,
            int x, int y, int w, int h,
            int btm, int rght,
            boolean isSelected) {
        /*
         * Indices 0: enabled
         *         1: selected
         *         2: inactive
         *         3: disabled
         *         4: disabled selected
         */
        int border;
        if (isSelected) {
            if (tabPane.isEnabled()) {
                if (isFrameActive) {
                    border = 1;
                } else {
                    border = 2;
                }
            } else {
                border = 4;
            }
            getTabTopBorder(border).paintBorder(tabPane, g, x - 1, y - 1, w + 3, h + 2);
        } else {
            if (tabPane.isEnabled()) {
                if (isFrameActive) {
                    border = 0;
                } else {
                    // border = 3;
                    border = 0; /* click-through behavior:use same border for inactive frames, we have got click */
                }
            } else {
                border = 3;
            }
            getTabTopBorder(border).paintBorder(tabPane, g, x - 1, y - 1, w + 3, h + 1);
        }
    }

    protected boolean shouldFillGap(int currentRun, int tabIndex, int x, int y) {
        boolean result = false;

        if (currentRun == runCount - 2) {  // If it's the second to last row.

            Rectangle lastTabBounds = getTabBounds(tabPane, tabPane.getTabCount() - 1);
            Rectangle tabBounds = getTabBounds(tabPane, tabIndex);
            if (QuaquaUtilities.isLeftToRight(tabPane)) {
                int lastTabRight = lastTabBounds.x + lastTabBounds.width - 1;

                // is the right edge of the last tab to the right
                // of the left edge of the current tab?
                if (lastTabRight > tabBounds.x + 2) {
                    return true;
                }
            } else {
                int lastTabLeft = lastTabBounds.x;
                int currentTabRight = tabBounds.x + tabBounds.width - 1;

                // is the left edge of the last tab to the left
                // of the right edge of the current tab?
                if (lastTabLeft < currentTabRight - 2) {
                    return true;
                }
            }
        } else {
            // fill in gap for all other rows except last row
            result = currentRun != runCount - 1;
        }

        return result;
    }

    protected Color getColorForGap(int currentRun, int x, int y) {
        final int shadowWidth = 4;
        int selectedIndex = tabPane.getSelectedIndex();
        int startIndex = tabRuns[currentRun + 1];
        int endIndex = lastTabInRun(tabPane.getTabCount(), currentRun + 1);
        int tabOverGap = -1;
        // Check each tab in the row that is 'on top' of this row
        for (int i = startIndex; i <= endIndex; ++i) {
            Rectangle tabBounds = getTabBounds(tabPane, i);
            int tabLeft = tabBounds.x;
            int tabRight = (tabBounds.x + tabBounds.width) - 1;
            // Check to see if this tab is over the gap
            if (QuaquaUtilities.isLeftToRight(tabPane)) {
                if (tabLeft <= x && tabRight - shadowWidth > x) {
                    return selectedIndex == i ? selectColor : tabPane.getBackgroundAt(i);
                }
            } else {
                if (tabLeft + shadowWidth < x && tabRight >= x) {
                    return selectedIndex == i ? selectColor : tabPane.getBackgroundAt(i);
                }
            }
        }

        return tabPane.getBackground();
    }

    protected void paintTabBorderLeft(int tabIndex, Graphics g,
            int x, int y, int w, int h,
            int btm, int rght,
            boolean isSelected) {
        /*
         * Indices 0: enabled
         *         1: selected
         *         2: inactive
         *         3: disabled
         *         4: disabled selected
         */
        int border;
        if (isSelected) {
            if (tabPane.isEnabled()) {
                if (isFrameActive) {
                    border = 1;
                } else {
                    border = 2;
                }
            } else {
                border = 4;
            }
            getTabLeftBorder(border).paintBorder(tabPane, g, x - 2, y, w + 4, h + 1);
        } else {
            if (tabPane.isEnabled()) {
                if (isFrameActive) {
                    border = 0;
                } else {
                    // border = 3;
                    border = 0; /* click-through behavior:use same border for inactive frames, we have got click */
                }
            } else {
                border = 3;
            }
            getTabLeftBorder(border).paintBorder(tabPane, g, x - 2, y, w + 2, h + 1);
        }
    }

    protected void paintTabBorderBottom(int tabIndex, Graphics g,
            int x, int y, int w, int h,
            int btm, int rght,
            boolean isSelected) {
        /*
         * Indices 0: enabled
         *         1: selected
         *         2: inactive
         *         3: disabled
         *         4: disabled selected
         */
        int border;
        if (isSelected) {
            if (tabPane.isEnabled()) {
                if (isFrameActive) {
                    border = 1;
                } else {
                    border = 2;
                }
            } else {
                border = 4;
            }
            getTabBottomBorder(border).paintBorder(tabPane, g, x - 1, y - 1, w + 3, h + 2);
        } else {
            if (tabPane.isEnabled()) {
                if (isFrameActive) {
                    border = 0;
                } else {
                    // border = 3;
                    border = 0; /* click-through behavior:use same border for inactive frames, we have got click */
                }
            } else {
                border = 3;
            }
            getTabBottomBorder(border).paintBorder(tabPane, g, x - 1, y, w + 3, h + 1);
        }
    }

    protected void paintTabBorderRight(int tabIndex, Graphics g,
            int x, int y, int w, int h,
            int btm, int rght,
            boolean isSelected) {
        /*
         * Indices 0: enabled
         *         1: selected
         *         2: inactive
         *         3: disabled
         *         4: disabled selected
         */
        int border;
        if (isSelected) {
            if (tabPane.isEnabled()) {
                if (isFrameActive) {
                    border = 1;
                } else {
                    border = 2;
                }
            } else {
                border = 4;
            }
            getTabRightBorder(border).paintBorder(tabPane, g, x - 2, y, w + 4, h + 1);
        } else {
            if (tabPane.isEnabled()) {
                if (isFrameActive) {
                    border = 0;
                } else {
                    // border = 3;
                    border = 0; /* click-through behavior:use same border for inactive frames, we have got click */
                }
            } else {
                border = 3;
            }
            getTabRightBorder(border).paintBorder(tabPane, g, x, y, w + 2, h + 1);
        }
    }

    @Override
    public void update(Graphics g, JComponent c) {
        if (c.isOpaque()) {
            g.setColor(tabAreaBackground);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
        paint(g, c);
    }

    /**
     * Overridden to do nothing for the Quaqua L&amp;F.
     */
    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement,
            int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    }

    /**
     * Overridden to do nothing for the Quaqua L&amp;F.
     */
    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    protected Insets getVisualMargin() {
        Insets margin = (Insets) tabPane.getClientProperty("Quaqua.Component.visualMargin");
        if (margin == null) {
            margin = UIManager.getInsets("Component.visualMargin");
        }
        switch (tabPane.getTabPlacement()) {
            case LEFT:
                return InsetsUtil.add(-1, -2, -4, -3, margin);
            case BOTTOM:
                return InsetsUtil.add(-1, -3, -5, -3, margin);
            case RIGHT:
                return InsetsUtil.add(-1, -3, -4, -2, margin);
            case TOP:
            default:
                return InsetsUtil.add(-3, -3, -4, -3, margin);
        }
    }

    /**
     * Overridden to return specific shift values for the Quaqua L&amp;F.
     * FIXME We should find another way to align the labels properly.
     */
    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        switch (tabPlacement) {
            case LEFT:
                return 0;
            case BOTTOM:
                return -1;
            case RIGHT:
                return 0;
            case TOP:
            default:
                return 1;
        }
    }

    protected void repaintTabArea() {
        int tabPlacement = tabPane.getTabPlacement();

        Rectangle clipRect = new Rectangle();
        Insets insets = InsetsUtil.add(tabPane.getInsets(), getVisualMargin());
        Dimension size = tabPane.getSize();

        switch (tabPlacement) {
            case LEFT:
                clipRect.setBounds(
                        insets.left,
                        insets.top,
                        calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth) + 6,
                        size.height - insets.bottom - insets.top);
                break;
            case BOTTOM:
                int totalTabHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                clipRect.setBounds(
                        insets.left,
                        size.height - insets.bottom - totalTabHeight - 6,
                        size.width - insets.left - insets.right,
                        totalTabHeight + 6);
                break;
            case RIGHT:
                int totalTabWidth = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                clipRect.setBounds(
                        size.width - insets.right - totalTabWidth - 6,
                        insets.top,
                        totalTabWidth + 6,
                        size.height - insets.top - insets.bottom);
                break;
            case TOP:
            default:
                clipRect.setBounds(
                        insets.left,
                        insets.top,
                        size.width - insets.right - insets.left,
                        calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight) + 6);
        }

        tabPane.repaint(clipRect);
    }

    @Override
    public void paint(Graphics gr, JComponent c) {
        Graphics2D g = (Graphics2D) gr;
        Object oldHints = QuaquaUtilities.beginGraphics(g);
        int tabPlacement = tabPane.getTabPlacement();
        isFrameActive = QuaquaUtilities.isOnActiveWindow(c);

        Dimension size = c.getSize();

        // Paint the background for the tab area
        if (tabPane.isOpaque()) {
            //g.setColor( c.getBackground() );
            g.setPaint(TextureColor.getPaint(c.getBackground(), c));
            g.fillRect(0, 0, size.width, size.height);
            /*
            switch ( tabPlacement ) {
            case LEFT:
            g.fillRect( insets.left, insets.top,
            calculateTabAreaWidth( tabPlacement, runCount, maxTabWidth ),
            size.height - insets.bottom - insets.top );
            break;
            case BOTTOM:
            int totalTabHeight = calculateTabAreaHeight( tabPlacement, runCount, maxTabHeight );
            g.fillRect( insets.left, size.height - insets.bottom - totalTabHeight,
            size.width - insets.left - insets.right,
            totalTabHeight );
            break;
            case RIGHT:
            int totalTabWidth = calculateTabAreaWidth( tabPlacement, runCount, maxTabWidth );
            g.fillRect( size.width - insets.right - totalTabWidth,
            insets.top, totalTabWidth,
            size.height - insets.top - insets.bottom );
            break;
            case TOP:
            default:
            g.fillRect( insets.left, insets.top,
            size.width - insets.right - insets.left,
            calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight) );
            paintHighlightBelowTab();
            }*/
        }

        int tc = tabPane.getTabCount();

        if (tabCount != tc) {
            tabCount = tc;
            updateMnemonics();
        }

        int selectedIndex = tabPane.getSelectedIndex();
        //int tabPlacement = tabPane.getTabPlacement();

        ensureCurrentLayout();

        // Paint content border
        paintContentBorder(g, tabPlacement, selectedIndex);


        // Paint tab area
        // If scrollable tabs are enabled, the tab area will be
        // painted by the scrollable tab panel instead.
        //

        //if (!scrollableTabLayoutEnabled()) { // WRAP_TAB_LAYOUT
        paintTabArea(g, tabPlacement, selectedIndex);

        QuaquaUtilities.endGraphics((Graphics2D) g, oldHints);
        Debug.paint(g, c, this);
    }

    /**
     * Paints the tabs in the tab area.
     * Invoked by paint().
     * The graphics parameter must be a valid <code>Graphics</code>
     * object.  Tab placement may be either:
     * <code>JTabbedPane.TOP</code>, <code>JTabbedPane.BOTTOM</code>,
     * <code>JTabbedPane.LEFT</code>, or <code>JTabbedPane.RIGHT</code>.
     * The selected index must be a valid tabbed pane tab index (0 to
     * tab count - 1, inclusive) or -1 if no tab is currently selected.
     * The handling of invalid parameters is unspecified.
     *
     * @param g the graphics object to use for rendering
     * @param tabPlacement the placement for the tabs within the JTabbedPane
     * @param selectedIndex the tab index of the selected component
     *
     * @since 1.4
     */
    @Override
    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        Rectangle iconRect = new Rectangle(),
                textRect = new Rectangle();
        Rectangle clipRect = g.getClipBounds();

        // Paint tabRuns of tabs from back to front
        Rectangle tabClipRect = new Rectangle();
        for (int i = runCount - 1; i >= 0; i--) {
            int start = tabRuns[i];
            int next = tabRuns[(i == runCount - 1) ? 0 : i + 1];
            int end = (next != 0 ? next - 1 : tabCount - 1);
            for (int j = start; j <= end; j++) {
                tabClipRect.setBounds(rects[j]);
                tabClipRect.grow(2, 2);
                if (tabClipRect.intersects(clipRect)) {
                    paintTab(g, tabPlacement, rects, j, iconRect, textRect);
                }
            }
        }

        // Paint selected tab if its in the front run
        // since it may overlap other tabs
        if (selectedIndex >= 0 && getRunForTab(tabCount, selectedIndex) == 0) {
            tabClipRect.setBounds(rects[selectedIndex]);
            tabClipRect.grow(2, 2);
            if (tabClipRect.intersects(clipRect)) {
                paintTab(g, tabPlacement, rects, selectedIndex, iconRect, textRect);
            }
        }
    }

    private void ensureCurrentLayout() {
        if (!tabPane.isValid()) {
            tabPane.validate();
        }
        /* If tabPane doesn't have a peer yet, the validate() call will
         * silently fail.  We handle that by forcing a layout if tabPane
         * is still invalid.  See bug 4237677.
         */
        if (!tabPane.isValid()) {
            TabbedPaneLayout layout = (TabbedPaneLayout) tabPane.getLayout();
            layout.calculateLayoutInfo();
        }
    }

    /**
     * Reloads the mnemonics. This should be invoked when a memonic changes,
     * when the title of a mnemonic changes, or when tabs are added/removed.
     */
    protected void updateMnemonics() {
        // XXX - This needs JDK 1.4 to work.
        resetMnemonics();
        for (int counter = tabPane.getTabCount() - 1; counter >= 0;
                counter--) {
            int mnemonic = tabPane.getMnemonicAt(counter);

            if (mnemonic > 0) {
                addMnemonic(counter, mnemonic);
            }
        }
    }

    /**
     * Resets the mnemonics bindings to an empty state.
     */
    protected void resetMnemonics() {
        if (mnemonicToIndexMap != null) {
            mnemonicToIndexMap.clear();
            mnemonicInputMap.clear();
        }
    }

    /**
     * Adds the specified mnemonic at the specified index.
     */
    protected void addMnemonic(int index, int mnemonic) {
        if (mnemonicToIndexMap == null) {
            initMnemonics();
        }
        mnemonicInputMap.put(KeyStroke.getKeyStroke(mnemonic, Event.ALT_MASK),
                "setSelectedIndex");
        mnemonicToIndexMap.put(mnemonic, index);
    }

    /**
     * Installs the state needed for mnemonics.
     */
    private void initMnemonics() {
        mnemonicToIndexMap = new Hashtable();
        mnemonicInputMap = new InputMapUIResource();
        mnemonicInputMap.setParent(SwingUtilities.getUIInputMap(tabPane,
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
        SwingUtilities.replaceUIInputMap(tabPane,
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                mnemonicInputMap);
    }

    protected void paintHighlightBelowTab() {
    }

    /**
     * Overridden to do nothing for the Quaqua L&amp;F.
     */
    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement,
            Rectangle[] rects, int tabIndex,
            Rectangle iconRect, Rectangle textRect,
            boolean isSelected) {
    }

    @Override
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        if (tabPlacement == RIGHT || tabPlacement == LEFT) {
            return new Insets(tabInsets.top, 6, tabInsets.bottom, 5);
        } else {
            return tabInsets;
        }
    }

    @Override
    protected Insets getTabAreaInsets(int tabPlacement) {
        //return currentTabAreaInsets;
        // FIXME We should change the layout code instead of adjusting the
        // insets.
        Insets i = (Insets) super.getTabAreaInsets(tabPlacement).clone();
        switch (tabPlacement) {
            case TOP:
                i.top -= 1;
                break;
            case LEFT:
                break;
            case BOTTOM:
                i.bottom += 1;
                break;
            case RIGHT:
                break;
        }
        return i;
    }

    @Override
    protected Insets getContentBorderInsets(int tabPlacement) {
        // We eliminate the insets at the tab location
        // because the content border is drawn as a shadow,
        // which runs through below the tabs.
        Insets insets = contentBorderInsets;

        Component selectedComponent = tabPane.getSelectedComponent();
        if (selectedComponent instanceof JComponent) {
            insets = (Insets) ((JComponent) selectedComponent).getClientProperty("Quaqua.TabbedPaneChild.contentInsets");
        }
        if (insets == null) {
            insets = contentBorderInsets;
        }
        currentContentBorderInsets.top = insets.top + 3;
        currentContentBorderInsets.left = insets.left + 3;
        currentContentBorderInsets.bottom = insets.bottom + 3;
        currentContentBorderInsets.right = insets.right + 3;

        switch (tabPlacement) {
            case LEFT:
                currentContentBorderInsets.left += 5;
                currentContentBorderInsets.bottom += 1;
                break;
            case RIGHT:
                currentContentBorderInsets.right += 4;
                currentContentBorderInsets.bottom += 1;
                break;
            case BOTTOM:
                currentContentBorderInsets.bottom += 1;
                break;
            case TOP:
            default:
                currentContentBorderInsets.top += 4;
        }
        return currentContentBorderInsets;
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        int width = tabPane.getWidth();
        int height = tabPane.getHeight();
        Insets insets = InsetsUtil.add(tabPane.getInsets(), getVisualMargin());

        int x = insets.left;
        int y = insets.top;
        int w = width - insets.right - insets.left;
        int h = height - insets.top - insets.bottom;

        switch (tabPlacement) {
            case LEFT:
                x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                w -= (x - insets.left);
                paintContentArea(g, x + 6, y + 1, w - 8, h - 6);
                paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, x, y, w, h);
                break;
            case RIGHT:
                w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                paintContentArea(g, x + 2, y + 1, w - 7, h - 6);
                paintContentBorderRightEdge(g, tabPlacement, selectedIndex, x, y, w, h);
                break;
            case BOTTOM:
                h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                paintContentArea(g, x + 2, y + 1, w - 4, h - 6);
                paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
                break;
            case TOP:
            default:
                y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                h -= (y - insets.top);
                paintContentArea(g, x + 2, y + 7, w - 4, h - 12);
                paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
        }
    }

    private int getDepth(JTabbedPane tp) {
        int depth = 1;
        Component parent = tp.getParent();
        while (parent != null && !(parent instanceof RootPaneContainer)) {
            if (parent instanceof JTabbedPane) {
                depth++;
            }
            parent = parent.getParent();
        }
        return depth;
    }

    protected void paintContentArea(Graphics gr, int x, int y, int width, int height) {
        Graphics2D g = (Graphics2D) gr;

        Color contentBackground = null;
        Component selectedComponent = tabPane.getSelectedComponent();
        if (selectedComponent instanceof JComponent) {
            contentBackground = (Color) ((JComponent) selectedComponent).getClientProperty("Quaqua.TabbedPaneChild.contentBackground");
        }
        if (contentBackground == null) {
            contentBackground = tabPane.getBackground();
        }
        g.setPaint(PaintableColor.getPaint(contentBackground, tabPane, 0, -getDepth(tabPane) % 4));
        g.fillRect(x, y, width, height);
    }

    @Override
    protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
            int selectedIndex,
            int x, int y, int w, int h) {
        Insets contentBorderInsets = getContentBorder().getBorderInsets(tabPane);
        getContentBorder().paintBorder(tabPane, g, x, y - contentBorderInsets.top + 1, w, h + contentBorderInsets.top - 1);
        /*
         * Indices 0: active
         *         1: inactive
         *         2: disabled
         */
        int bar;
        if (tabPane.isEnabled()) {
            if (isFrameActive) {
                bar = 0;
            } else {
                bar = 1;
            }
        } else {
            bar = 2;
        }

        getBarTopBorder(bar).paintBorder(
                tabPane, g,
                x + contentBorderInsets.left,
                y,
                w - contentBorderInsets.left - contentBorderInsets.right,
                7);
    }

    @Override
    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
            int selectedIndex,
            int x, int y, int w, int h) {
        Insets contentBorderInsets = getContentBorder().getBorderInsets(tabPane);
        getContentBorder().paintBorder(tabPane, g, x, y, w, h + contentBorderInsets.bottom);

        /*
         * Indices 0: active
         *         1: inactive
         *         2: disabled
         */
        int bar;
        if (tabPane.isEnabled()) {
            if (isFrameActive) {
                bar = 0;
            } else {
                bar = 1;
            }
        } else {
            bar = 2;
        }

        getBarBottomBorder(bar).paintBorder(
                tabPane, g,
                x + contentBorderInsets.left,
                y + h - 6,
                w - contentBorderInsets.left - contentBorderInsets.right,
                6);
    }

    @Override
    protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
            int selectedIndex,
            int x, int y, int w, int h) {
        getContentBorder().paintBorder(tabPane, g, x, y, w, h);

        /*
         * Indices 0: active
         *         1: inactive
         *         2: disabled
         */
        int bar;
        if (tabPane.isEnabled()) {
            if (isFrameActive) {
                bar = 0;
            } else {
                bar = 1;
            }
        } else {
            bar = 2;
        }

        getBarLeftBorder(bar).paintBorder(tabPane, g, x, y + 1, 7, h - 6);
    }

    @Override
    protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
            int selectedIndex,
            int x, int y, int w, int h) {
        getContentBorder().paintBorder(tabPane, g, x, y, w + 3, h);

        /*
         * Indices 0: active
         *         1: inactive
         *         2: disabled
         */
        int bar;
        if (tabPane.isEnabled()) {
            if (isFrameActive) {
                bar = 0;
            } else {
                bar = 1;
            }
        } else {
            bar = 2;
        }

        getBarRightBorder(bar).paintBorder(tabPane, g, x + w - 6, y + 1, 6, h - 6);
    }

    @Override
    protected int calculateMaxTabHeight(int tabPlacement) {
        FontMetrics metrics = getFontMetrics();
        int height = metrics.getHeight();
        boolean tallerIcons = false;

        for (int i = 0; i < tabPane.getTabCount(); ++i) {
            Icon icon = tabPane.getIconAt(i);
            if (icon != null) {
                if (icon.getIconHeight() > height) {
                    tallerIcons = true;
                    break;
                }
            }
        }
        return super.calculateMaxTabHeight(tabPlacement)
                - (tallerIcons ? (tabInsets.top + tabInsets.bottom) : 0);
    }

    @Override
    protected int getTabRunOverlay(int tabPlacement) {
        if (tabPlacement == LEFT || tabPlacement == RIGHT) {
            return 2;
        } else {
            return 1;
        }
        /*
        // Tab runs laid out vertically should overlap
        // at least as much as the largest slant
        if ( tabPlacement == LEFT || tabPlacement == RIGHT ) {
        int maxTabHeight = calculateMaxTabHeight(tabPlacement);
        return maxTabHeight / 2;
        }
        return 0;
         */
    }

    // Don't rotate runs!
    protected boolean shouldRotateTabRuns(int tabPlacement, int selectedRun) {
        return false;
    }

    // Pad all tab runs if there is more than one run.
    @Override
    protected boolean shouldPadTabRun(int tabPlacement, int run) {
        return runCount > 1;
    }

    private boolean isLastInRun(int tabIndex) {
        int run = getRunForTab(tabPane.getTabCount(), tabIndex);
        int lastIndex = lastTabInRun(tabPane.getTabCount(), run);
        return tabIndex == lastIndex;
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement,
            Font font, FontMetrics metrics, int tabIndex,
            String title, Rectangle textRect,
            boolean isSelected) {

        g.setFont(font);

        // This needs JDK 1.4 to work.
        View v = getTextViewForTab(tabIndex);

        if (v != null) {
            // html
            v.paint(g, textRect);
        } else {
            // plain text
            // This needs JDK 1.4 to work.
            int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);

            if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex)) {
                g.setColor(tabPane.getForegroundAt(tabIndex));
                QuaquaUtilities.drawStringUnderlineCharAt(g,
                        title, mnemIndex,
                        textRect.x, textRect.y + metrics.getAscent());

            } else { // tab disabled
                g.setColor(disabledForeground);
                QuaquaUtilities.drawStringUnderlineCharAt(g,
                        title, mnemIndex,
                        textRect.x, textRect.y + metrics.getAscent());
            }
        }
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of MetalTabbedPaneUI.
     */
    public class TabbedPaneLayout extends BasicTabbedPaneUI.TabbedPaneLayout {

        @Override
        protected void calculateTabRects(int tabPlacement, int tabCount) {
            Dimension size = tabPane.getSize();
            Insets insets = InsetsUtil.add(tabPane.getInsets(), getVisualMargin());
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            FontMetrics metrics = getFontMetrics();
            int fontHeight = metrics.getHeight();
            int selectedIndex = tabPane.getSelectedIndex();
            int tabRunOverlay;
            int i, j;
            int x, y;
            int returnAt;
            boolean verticalTabRuns = (tabPlacement == LEFT || tabPlacement == RIGHT);
            boolean leftToRight = QuaquaUtilities.isLeftToRight(tabPane);

            //
            // Calculate bounds within which a tab run must fit
            //
            switch (tabPlacement) {
                case LEFT:
                    maxTabWidth = calculateMaxTabWidth(tabPlacement);
                    x = insets.left + tabAreaInsets.left;
                    y = insets.top + tabAreaInsets.top;
                    returnAt = size.height - (insets.bottom + tabAreaInsets.bottom);
                    break;
                case RIGHT:
                    maxTabWidth = calculateMaxTabWidth(tabPlacement);
                    x = size.width - insets.right - tabAreaInsets.right - maxTabWidth;
                    y = insets.top + tabAreaInsets.top;
                    returnAt = size.height - (insets.bottom + tabAreaInsets.bottom);
                    break;
                case BOTTOM:
                    maxTabHeight = calculateMaxTabHeight(tabPlacement);
                    x = insets.left + tabAreaInsets.left;
                    y = size.height - insets.bottom - tabAreaInsets.bottom - maxTabHeight;
                    returnAt = size.width - (insets.right + tabAreaInsets.right);
                    break;
                case TOP:
                default:
                    maxTabHeight = calculateMaxTabHeight(tabPlacement);
                    x = insets.left + tabAreaInsets.left;
                    y = insets.top + tabAreaInsets.top;
                    returnAt = size.width - (insets.right + tabAreaInsets.right);
                    break;
            }

            tabRunOverlay = getTabRunOverlay(tabPlacement);

            runCount = 0;
            selectedRun = -1;

            if (tabCount == 0) {
                return;
            }

            // Run through tabs and partition them into runs
            Rectangle rect;
            for (i = 0; i < tabCount; i++) {
                rect = rects[i];

                if (!verticalTabRuns) {
                    // Tabs on TOP or BOTTOM....
                    if (i > 0) {
                        rect.x = rects[i - 1].x + rects[i - 1].width;
                    } else {
                        tabRuns[0] = 0;
                        runCount = 1;
                        maxTabWidth = 0;
                        rect.x = x;
                    }
                    rect.width = calculateTabWidth(tabPlacement, i, metrics);
                    maxTabWidth = Math.max(maxTabWidth, rect.width);

                    // Never move a TAB down a run if it is in the first column.
                    // Even if there isn't enough room, moving it to a fresh
                    // line won't help.
                    if (rect.x != 2 + insets.left && rect.x + rect.width > returnAt) {
                        if (runCount > tabRuns.length - 1) {
                            expandTabRunsArray();
                        }
                        tabRuns[runCount] = i;
                        runCount++;
                        rect.x = x;
                    }
                    // Initialize y position in case there's just one run
                    rect.y = y;
                    rect.height = maxTabHeight/* - 2*/;

                } else {
                    // Tabs on LEFT or RIGHT...
                    if (i > 0) {
                        rect.y = rects[i - 1].y + rects[i - 1].height;
                    } else {
                        tabRuns[0] = 0;
                        runCount = 1;
                        maxTabHeight = 0;
                        rect.y = y;
                    }
                    rect.height = calculateTabHeight(tabPlacement, i, fontHeight);
                    maxTabHeight = Math.max(maxTabHeight, rect.height);

                    // Never move a TAB over a run if it is in the first run.
                    // Even if there isn't enough room, moving it to a fresh
                    // column won't help.
                    if (rect.y != 2 + insets.top && rect.y + rect.height > returnAt) {
                        if (runCount > tabRuns.length - 1) {
                            expandTabRunsArray();
                        }
                        tabRuns[runCount] = i;
                        runCount++;
                        rect.y = y;
                    }
                    // Initialize x position in case there's just one column
                    rect.x = x;
                    rect.width = maxTabWidth/* - 2*/;

                }
                if (i == selectedIndex) {
                    selectedRun = runCount - 1;
                }
            }

            if (runCount > 1) {
                // Re-distribute tabs in case last run has leftover space
                normalizeTabRuns(tabPlacement, tabCount, verticalTabRuns ? y : x, returnAt);

                selectedRun = getRunForTab(tabCount, selectedIndex);

                // Rotate run array so that selected run is first
                if (shouldRotateTabRuns(tabPlacement)) {
                    rotateTabRuns(tabPlacement, selectedRun);
                }
            }

            // Determine how much we want to pad the tabs
            int maxPad = 0;
            /*
            for (i = runCount - 1; i >= 0; i--) {
            int start = tabRuns[i];
            int next = tabRuns[i == (runCount - 1)? 0 : i + 1];
            int end = (next != 0? next - 1 : tabCount - 1);
            
            maxPad = Math.max(maxPad, rects[end].x + rects[end].width - rects[start].x);
            }*/
            switch (tabPlacement) {
                case LEFT:
                case RIGHT:
                    maxPad = size.height - tabAreaInsets.top - tabAreaInsets.bottom;
                    break;
                case BOTTOM:
                case TOP:
                default:
                    maxPad = size.width - tabAreaInsets.left - tabAreaInsets.right;
                    break;
            }

            // Step through runs from back to front to calculate
            // tab y locations and to pad runs appropriately
            for (i = runCount - 1; i >= 0; i--) {
                int start = tabRuns[i];
                int next = tabRuns[i == (runCount - 1) ? 0 : i + 1];
                int end = (next != 0 ? next - 1 : tabCount - 1);
                if (!verticalTabRuns) {
                    for (j = start; j <= end; j++) {
                        rect = rects[j];
                        rect.y = y;
                        rect.x += getTabRunIndent(tabPlacement, i);
                    }
                    if (shouldPadTabRun(tabPlacement, i)) {
                        padTabRun(tabPlacement, start, end, maxPad);
                    }
                    if (tabPlacement == BOTTOM) {
                        y -= (maxTabHeight - tabRunOverlay);
                    } else {
                        y += (maxTabHeight - tabRunOverlay);
                    }
                } else {
                    for (j = start; j <= end; j++) {
                        rect = rects[j];
                        rect.x = x;
                        rect.y += getTabRunIndent(tabPlacement, i);
                    }
                    if (shouldPadTabRun(tabPlacement, i)) {
                        padTabRun(tabPlacement, start, end, maxPad);
                    }
                    if (tabPlacement == RIGHT) {
                        x -= (maxTabWidth - tabRunOverlay);
                    } else {
                        x += (maxTabWidth - tabRunOverlay);
                    }
                }
            }

            // Pad the selected tab so that it appears raised in front
            padSelectedTab(tabPlacement, selectedIndex);

            // if right to left and tab placement on the top or
            // the bottom, flip x positions and adjust by widths
            if (!leftToRight && !verticalTabRuns) {
                int rightMargin = size.width - (insets.right + tabAreaInsets.right);
                for (i = 0; i < tabCount; i++) {
                    rects[i].x = rightMargin - rects[i].x - rects[i].width;
                }
            }


            //
            // Center tabs vertically or horizontally
            // If centered horizontally ensure that all tab runs have
            // the same width.
            Integer propertyValue = (Integer) tabPane.getClientProperty("Quaqua.TabbedPane.tabAlignment");
            int tabAlignment = (propertyValue != null && propertyValue.intValue() == SwingConstants.LEADING) ? SwingConstants.LEADING : SwingConstants.CENTER;
            if (tabAlignment == SwingConstants.CENTER) {
                switch (tabPlacement) {
                    case LEFT:
                    case RIGHT: {
                        int availableTabAreaHeight = size.height - insets.top - insets.bottom - tabAreaInsets.top - tabAreaInsets.bottom;
                        int usedTabAreaHeight = 0;
                        int pad = 0;
                        for (int run = 0; run < runCount; run++) {
                            int firstIndex = tabRuns[run];
                            int lastIndex = lastTabInRun(tabCount, run);
                            if (run == 0) {
                                usedTabAreaHeight = 0;
                                for (i = firstIndex; i <= lastIndex; i++) {
                                    usedTabAreaHeight += rects[i].height;
                                }
                                pad = (availableTabAreaHeight - usedTabAreaHeight) / 2;
                            }
                            for (i = firstIndex; i <= lastIndex; i++) {
                                rects[i].y += pad;
                            }
                        }
                        break;
                    }
                    case BOTTOM:
                    case TOP:
                    default: {
                        int availableTabAreaWidth = size.width - insets.left - insets.right - tabAreaInsets.left - tabAreaInsets.right;
                        for (int run = 0; run < runCount; run++) {
                            int firstIndex = tabRuns[run];
                            int lastIndex = lastTabInRun(tabCount, run);
                            int usedTabAreaWidth = 0;
                            for (i = firstIndex; i <= lastIndex; i++) {
                                usedTabAreaWidth += rects[i].width;
                            }
                            int pad = (availableTabAreaWidth - usedTabAreaWidth) / 2;
                            for (i = firstIndex; i <= lastIndex; i++) {
                                rects[i].x += pad;
                            }
                        }

                        break;
                    }
                }
            }
        }

        @Override
        protected void normalizeTabRuns(int tabPlacement, int tabCount,
                int start, int max) {
            // Only normalize the runs for top & bottom;  normalizing
            // doesn't look right for Metal's vertical tabs
            // because the last run isn't padded and it looks odd to have
            // fat tabs in the first vertical runs, but slimmer ones in the
            // last (this effect isn't noticeable for horizontal tabs).
            if (tabPlacement == TOP || tabPlacement == BOTTOM) {

                boolean verticalTabRuns = (tabPlacement == LEFT || tabPlacement == RIGHT);
                int run = runCount - 1;
                boolean keepAdjusting = true;
                double weight = 1.33;

                // At this point the tab runs are packed to fit as many
                // tabs as possible, which can leave the last run with a lot
                // of extra space (resulting in very fat tabs on the last run).
                // So we'll attempt to distribute this extra space more evenly
                // across the runs in order to make the runs look more consistent.
                //
                // Starting with the last run, determine whether the last tab in
                // the previous run would fit (generously) in this run; if so,
                // move tab to current run and shift tabs accordingly.  Cycle
                // through remaining runs using the same algorithm.
                //
                while (keepAdjusting) {
                    int last = lastTabInRun(tabCount, run);
                    int prevLast = lastTabInRun(tabCount, run - 1);
                    int end;
                    int prevLastLen;

                    if (!verticalTabRuns) {
                        end = rects[last].x + rects[last].width;
                        prevLastLen = (int) (maxTabWidth * weight);
                    } else {
                        end = rects[last].y + rects[last].height;
                        prevLastLen = (int) (maxTabHeight * weight * 2);
                    }

                    // Check if the run has enough extra space to fit the last tab
                    // from the previous row...
                    if (max - end > prevLastLen) {

                        // Insert tab from previous row and shift rest over
                        tabRuns[run] = prevLast;
                        if (!verticalTabRuns) {
                            rects[prevLast].x = start;
                        } else {
                            rects[prevLast].y = start;
                        }
                        for (int i = prevLast + 1; i <= last; i++) {
                            if (!verticalTabRuns) {
                                rects[i].x = rects[i - 1].x + rects[i - 1].width;
                            } else {
                                rects[i].y = rects[i - 1].y + rects[i - 1].height;
                            }
                        }

                    } else if (run == runCount - 1) {
                        // no more room left in last run, so we're done!
                        keepAdjusting = false;
                    }
                    if (run - 1 > 0) {
                        // check previous run next...
                        run -= 1;
                    } else {
                        // check last run again...but require a higher ratio
                        // of extraspace-to-tabsize because we don't want to
                        // end up with too many tabs on the last run!
                        run = runCount - 1;
                        weight += .33;
                    }
                }
            }
        }

        // Don't rotate runs!
        @Override
        protected void rotateTabRuns(int tabPlacement, int selectedRun) {
        }

        // Don't pad selected tab
        @Override
        protected void padSelectedTab(int tabPlacement, int selectedIndex) {
        }

        @Override
        public void calculateLayoutInfo() {
            int tabCount = tabPane.getTabCount();
            assureRectsCreated(tabCount);
            calculateTabRects(tabPane.getTabPlacement(), tabCount);
        }

        @Override
        protected void padTabRun(int tabPlacement, int start, int end, int max) {
            // Only pad tab runs if they are on top or bottom
            if (tabPlacement == TOP || tabPlacement == BOTTOM) {
                super.padTabRun(tabPlacement, start, end, max);
            }
        }
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTabbedPaneUI.
     */
    public class QuaquaPropertyChangeHandler extends BasicTabbedPaneUI.PropertyChangeHandler {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (name.equals("Frame.active")) {
                repaintTabArea();
            } else if (name.equals("JComponent.sizeVariant")) {
                QuaquaUtilities.applySizeVariant(tabPane);
            }
            // Forward everyhting except tabLayoutPolicy change to super class.
            // tabLayoutPolicy must not be forward, because it would break
            // the functionality of class
            // ch.randelshofer.quaqua.panther.QuaquaPantherJaguarTabbedPaneUI.
            if (!name.equals("tabLayoutPolicy")) {
                super.propertyChange(evt);
            }
        }
    }

    public Rectangle getVisualBounds(
            JComponent c, int type, int width, int height) {
        Rectangle rect = new Rectangle(0, 0, width, height);
        InsetsUtil.subtractInto(c.getInsets(), rect);
        if (type != VisuallyLayoutable.CLIP_BOUNDS) {
            Insets margin = getVisualMargin();

            // XXX - Since we have to use bogus values here, our layout
            //       must be bogus as well.
            switch (((JTabbedPane) c).getTabPlacement()) {
                case LEFT:
                    InsetsUtil.addTo(1, 2, 5, 3, margin);
                    break;

                case BOTTOM:
                    InsetsUtil.addTo(1, 3, 5, 3, margin);
                    break;

                case RIGHT:
                    InsetsUtil.addTo(1, 3, 5, 2, margin);
                    break;

                case TOP:
                default:
                    InsetsUtil.addTo(3, 3, 5, 3, margin);
                    break;

            }


            rect.x += margin.left;
            rect.y += margin.top;
            rect.width -= margin.left + margin.right;
            rect.height -= margin.top + margin.bottom;
        }

        return rect;
    }

    /**
     * Returns the tab index which intersects the specified point
     * in the coordinate space of the component where the
     * tabs are actually rendered, which could be the JTabbedPane
     * (for WRAP_TAB_LAYOUT) or a ScrollableTabPanel (SCROLL_TAB_LAYOUT).
     */
    private int getTabAtLocation(int x, int y) {
        ensureCurrentLayout();

        int tCount = tabPane.getTabCount();
        for (int i = 0; i < tCount; i++) {
            if (rects[i].contains(x, y)) {
                return i;
            }

        }
        return -1;
    }

    @Override
    public int getBaseline(JComponent component, int width, int height) {
        return -1;
    }

    @Override
    public void navigateSelectedTab(int direction) {
        super.navigateSelectedTab(direction);
    }

    public Integer getIndexForMnemonic(int mnemonic) {
        return (Integer) mnemonicToIndexMap.get(mnemonic);
    }

    public boolean requestFocusForVisibleComponent() {
        Component visibleComponent = getVisibleComponent();
        if (visibleComponent != null && visibleComponent.isFocusTraversable()) {
            QuaquaUtilities.compositeRequestFocus(visibleComponent);
            return true;
        } else if (visibleComponent instanceof JComponent) {
            if (((JComponent) visibleComponent).requestDefaultFocus()) {
                return true;
            }

        }
        return false;
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTabbedPaneUI.
     */
    public class QuaquaMouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (!tabPane.isEnabled()) {
                return;
            }
            int tabIndex = getTabAtLocation(e.getX(), e.getY());
            if (tabIndex >= 0 && tabPane.isEnabledAt(tabIndex)) {
                if (tabIndex == tabPane.getSelectedIndex()) {
                    if (tabPane.isRequestFocusEnabled()) {
                        tabPane.requestFocus();
                        tabPane.repaint(getTabBounds(tabPane, tabIndex));
                    }
                } else {
                    tabPane.setSelectedIndex(tabIndex);
                }
            }
        }
    }

    @Override
    protected void installComponents() {
        // empty
        // We must not call super, because with Java 1.4 and higher,
        // this would set the 'tabScroller' variable with a non-null value.
    }

    @Override
    protected ChangeListener createChangeListener() {
        return new TabSelectionHandler();
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTabbedPaneUI.
     */
    private static class TabSelectionHandler implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            JTabbedPane tabPane = (JTabbedPane) e.getSource();
            tabPane.revalidate();
            tabPane.repaint();
            /*
            if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            int index = tabPane.getSelectedIndex();
            if (index < rects.length && index != -1) {
            tabScroller.tabPanel.scrollRectToVisible(rects[index]);
            }
            }*/
        }
    }
}
