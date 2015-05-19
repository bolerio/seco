/*
 * @(#)QuaquaPantherScrollTabbedPaneUI.java  
 *
 * Copyright (c) 2006-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.panther;

import ch.randelshofer.quaqua.color.PaintableColor;
import ch.randelshofer.quaqua.*;
import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.border.BackgroundBorder;
import ch.randelshofer.quaqua.util.Debug;
import ch.randelshofer.quaqua.util.NavigatableTabbedPaneUI;
import ch.randelshofer.quaqua.color.TextureColor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import java.util.*;

/**
 * A replacement for Apple's AquaTabbedPaneUI for Mac OS X 10.3 Panther.
 * All tabs are placed in one run. Tabs on the left and on the right
 * are rotated by 90 degrees.
 * If the tabs don't fit into one run, they are shortened.
 * Tabs on the top and on the bottom are only shortend as at least one tab can
 * be shown fully. If this isn't possible, a combo box is shown instead of the
 * tabs.
 * <p>
 * Supports the following client properties on the JTabbedPane:
 * <ul>
 * <li>
 * <code>Quaqua.TabbedPane.contentBorderPainted</code> specifies whether the
 * content border should be painted.</li>
 * </ul>
 * <p>
 * Supports the following client properties on the children of the JTabbedPane:
 * <ul>
 * <li>
 * <code>Quaqua.TabbedPaneChild.contentBackground</code> specifies the background
 * Color to be used to fill the content border.</li>
 * <li>
 * <code>Quaqua.TabbedPaneChild.contentInsets</code> specifies the insets
 * to be used to lay out the child component inside the JTabbedPane.</li>
 * </ul>
 *
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaPantherScrollTabbedPaneUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaPantherScrollTabbedPaneUI extends BasicTabbedPaneUI
        implements NavigatableTabbedPaneUI {

    private final static boolean DEBUG = false;

    // For use when tabLayoutPolicy = SCROLL_TAB_LAYOUT
    private /*static*/ class TabsComboBox extends JComboBox implements UIResource {

        public TabsComboBox() {
            // TabsComboBox must never by opaque.
            setOpaque(false);
        }

        @Override
        public void setBorder(Border b) {
            // Only allow UIResource borders
            if (b instanceof UIResource) {
                super.setBorder(b);
            }
        }

        @Override
        public boolean hasFocus() {
            return tabPane.hasFocus();
        }

        @Override
        public boolean isFocusOwner() {
            return tabPane.isFocusOwner();
        }
    }
    private TabsComboBox tabsCombo;
    private ItemListener itemListener;
    private ContainerListener containerListener;
    private Vector htmlViews;
    private Insets outerTabInsets;
    // Tab insets used when font size is <= 111.
    private Insets smallTabInsets;
    private Insets smallOuterTabInsets;
    private Insets currentContentBorderInsets = new Insets(0, 0, 0, 0);
    /**
     * Visual margin of the tabs. This margin depends on the artwork used by
     * the tab background.
     */
    private static Insets tabVisualMargin = new Insets(3, 3, 3, 3);
    /**
     * Number of tabs. When the count differs, the mnemonics are updated.
     */
    private int tabCount;
    private Hashtable mnemonicToIndexMap;
    
    public Integer getIndexForMnemonic(int mnemonic) {
        return (Integer) mnemonicToIndexMap.get(mnemonic);
    }
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
    private TabsComboBoxModel tabsComboModel = new TabsComboBoxModel();

    /**
     * Creates a new instance.
     */
    public QuaquaPantherScrollTabbedPaneUI() {
    }

    public static ComponentUI createUI(JComponent x) {
        return new QuaquaPantherScrollTabbedPaneUI();
    }

    protected ItemListener createItemListener() {
        return new ItemHandler();
    }

    @Override
    protected LayoutManager createLayoutManager() {
        return new QuaquaTabbedPaneLayout();
    }

    @Override
    protected void installDefaults() {

        super.installDefaults();

        String prefix = getPropertyPrefix();

        LookAndFeel.installColorsAndFont(tabPane, prefix + "background",
                prefix + "foreground", prefix + "font");
        highlight = UIManager.getColor(prefix + "light");
        lightHighlight = UIManager.getColor(prefix + "highlight");
        shadow = UIManager.getColor(prefix + "shadow");
        darkShadow = UIManager.getColor(prefix + "darkShadow");
        focus = UIManager.getColor(prefix + "focus");

        textIconGap = UIManager.getInt(prefix + "textIconGap");
        tabInsets = UIManager.getInsets(prefix + "tabInsets");
        smallTabInsets = UIManager.getInsets(prefix + "smallTabInsets");
        selectedTabPadInsets = UIManager.getInsets(prefix + "selectedTabPadInsets");
        tabAreaInsets = UIManager.getInsets(prefix + "tabAreaInsets");
        contentBorderInsets = UIManager.getInsets(prefix + "contentBorderInsets");
        tabRunOverlay = UIManager.getInt(prefix + "tabRunOverlay");

        LookAndFeel.installBorder(tabPane, prefix + "border");

        tabPane.setOpaque(UIManager.getBoolean(prefix + "opaque"));

        outerTabInsets = UIManager.getInsets(prefix + "outerTabInsets");
        smallOuterTabInsets = UIManager.getInsets(prefix + "smallOuterTabInsets");

        if (outerTabInsets == null) {
            outerTabInsets = new Insets(0, 0, 0, 0);
        }
        if (smallOuterTabInsets == null) {
            smallOuterTabInsets = new Insets(0, 0, 0, 0);
        }

        tabPane.setOpaque(UIManager.getBoolean(prefix + "opaque"));
    }

    @Override
    protected void installComponents() {
        if (tabsCombo == null) {
            tabsCombo = new TabsComboBox();
            tabsCombo.setModel(tabsComboModel);
            tabsCombo.setFont(tabPane.getFont());
            // tabsCombo.setFocusable(false);
        }
        tabPane.add(tabsCombo);
        tabsComboModel.setModel(tabPane);
    }

    @Override
    protected void uninstallComponents() {
        tabPane.remove(tabsCombo);
        tabsComboModel.setModel(tabPane);
    }

    @Override
    protected void installListeners() {
        if ((propertyChangeListener = createPropertyChangeListener()) != null) {
            tabPane.addPropertyChangeListener(propertyChangeListener);
        }
        if ((tabChangeListener = createChangeListener()) != null) {
            tabPane.addChangeListener(tabChangeListener);
        }
        if ((mouseListener = createMouseListener()) != null) {
            tabPane.addMouseListener(mouseListener);
        }
        if ((itemListener = createItemListener()) != null) {
            tabsCombo.addItemListener(itemListener);
        }
        if ((focusListener = createFocusListener()) != null) {
            tabPane.addFocusListener(focusListener);
        }
        // PENDING(api) : See comment for ContainerHandler
        if ((containerListener = new ContainerHandler()) != null) {
            tabPane.addContainerListener(containerListener);
            if (tabPane.getTabCount() > 0) {
                htmlViews = createHTMLVector();
            }
        }
    }

    @Override
    protected void uninstallListeners() {
        if (itemListener != null) {
            if (tabsCombo != null) {
                tabsCombo.removeItemListener(itemListener);
            }
        }

        if (mouseListener != null) {
            tabPane.removeMouseListener(mouseListener);
            mouseListener = null;
        }
        if (focusListener != null) {
            tabPane.removeFocusListener(focusListener);
            focusListener = null;
        }

        // PENDING(api): See comment for ContainerHandler
        if (containerListener != null) {
            tabPane.removeContainerListener(containerListener);
            containerListener = null;
            if (htmlViews != null) {
                htmlViews.removeAllElements();
                htmlViews = null;
            }
        }
        if (tabChangeListener != null) {
            tabPane.removeChangeListener(tabChangeListener);
            tabChangeListener = null;
        }
        if (propertyChangeListener != null) {
            tabPane.removePropertyChangeListener(propertyChangeListener);
            propertyChangeListener = null;
        }
    }

    @Override
    protected MouseListener createMouseListener() {
        return new QuaquaMouseHandler();
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return new QuaquaPropertyHandler();
    }

    @Override
    protected ChangeListener createChangeListener() {
        return new QuaquaTabSelectionHandler();
    }

    @Override
    protected FocusListener createFocusListener() {
        return new FocusHandler();
    }

    @Override
    protected void installKeyboardActions() {
        InputMap km = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        SwingUtilities.replaceUIInputMap(tabPane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                km);
        km = getInputMap(JComponent.WHEN_FOCUSED);
        SwingUtilities.replaceUIInputMap(tabPane, JComponent.WHEN_FOCUSED, km);
        ActionMap am = getActionMap();

        SwingUtilities.replaceUIActionMap(tabPane, am);
    }

    protected Insets getVisualMargin() {
        Insets visualMargin = (Insets) tabPane.getClientProperty("Quaqua.Component.visualMargin");
        if (visualMargin == null) {
            visualMargin = UIManager.getInsets("Component.visualMargin");
            if (visualMargin == null) {
                visualMargin = new Insets(0, 0, 0, 0);
            }
        }
        return visualMargin;
    }

    protected Insets getTabVisualMargin() {
        return tabVisualMargin;
    }

    protected Insets getInsets() {
        Insets insets = tabPane.getInsets();

        Insets visualMargin = getVisualMargin();

        insets.top += visualMargin.top;
        insets.left += visualMargin.left;
        insets.bottom += visualMargin.bottom;
        insets.right += visualMargin.right;
        return insets;
    }

    @Override
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        boolean isSmall=QuaquaUtilities.getSizeVariant(tabPane)==QuaquaUtilities.SizeVariant.SMALL;
        

        int tCount = tabPane.getTabCount();
        Insets insets;
        if (tCount == 1) {
            insets = (Insets) (isSmall ? smallOuterTabInsets : outerTabInsets).clone();
        } else if (tabIndex == 0) {
            insets = isSmall ? new Insets(smallOuterTabInsets.top, smallOuterTabInsets.left, smallTabInsets.bottom, smallTabInsets.right) : new Insets(outerTabInsets.top, outerTabInsets.left, tabInsets.bottom, tabInsets.right);
        } else if (tabIndex == tCount - 1) {
            insets = isSmall ? new Insets(smallTabInsets.top, smallTabInsets.left, smallOuterTabInsets.bottom, smallOuterTabInsets.right) : new Insets(tabInsets.top, tabInsets.left, outerTabInsets.bottom, outerTabInsets.right);
        } else {
            insets = (Insets) (isSmall ? smallTabInsets : tabInsets).clone();
        }

        Insets visualMargin = getTabVisualMargin();
        InsetsUtil.addTo(visualMargin, insets);
        return insets;

    }

    @Override
    protected Insets getContentBorderInsets(int tabPlacement) {
        Insets insets = null;

        Component selectedComponent = tabPane.getSelectedComponent();
        if (selectedComponent instanceof JComponent) {
            insets = (Insets) ((JComponent) selectedComponent).getClientProperty("Quaqua.TabbedPaneChild.contentInsets");
        }
        if (insets == null) {
            insets = contentBorderInsets;
        }

        currentContentBorderInsets.top = insets.top;
        currentContentBorderInsets.left = insets.left;
        currentContentBorderInsets.bottom = insets.bottom;
        currentContentBorderInsets.right = insets.right;

        switch (tabPlacement) {
            case LEFT:
                currentContentBorderInsets.left -= 3;
                break;
            case RIGHT:
                currentContentBorderInsets.right -= 3;
                break;
            case BOTTOM:
                currentContentBorderInsets.bottom -= 2;
                break;
            case TOP:
            default:
                currentContentBorderInsets.top -= 3;
                break;
        }
        return currentContentBorderInsets;
    }

    @Override
    public void paint(Graphics gr, JComponent c) {
        Graphics2D g = (Graphics2D) gr;
        Object oldHints = QuaquaUtilities.beginGraphics(g);

        if (c.isOpaque()) {
            g.setPaint(PaintableColor.getPaint(c.getBackground(), c));
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }


        int tc = tabPane.getTabCount();

        if (tabCount != tc) {
            tabCount = tc;
            updateMnemonics();
        }

        int selectedIndex = tabPane.getSelectedIndex();
        int tabPlacement = tabPane.getTabPlacement();

        ensureCurrentLayout();

        // Paint content border
        // XXX - Add client property to switch this on or off
        paintContentBorder(g, tabPlacement, selectedIndex);

        // Paint tab area
        // If scrollable tabs are enabled, the tab area will be
        // painted by the scrollable tab panel instead.
        //
        if (!tabsCombo.isVisible()) { // WRAP_TAB_LAYOUT
            paintTabArea(g, tabPlacement, selectedIndex);
        }

        QuaquaUtilities.endGraphics((Graphics2D) g, oldHints);
        Debug.paint(g, c, this);
    }

    @Override
    protected void paintContentBorder(Graphics gr, int tabPlacement, int selectedIndex) {
        Boolean isContentBorderPainted = (Boolean) tabPane.getClientProperty("Quaqua.TabbedPane.contentBorderPainted");
        if (isContentBorderPainted != null && !isContentBorderPainted.booleanValue()) {
            return;
        }

        Graphics2D g = (Graphics2D) gr;


        int width = tabPane.getWidth();
        int height = tabPane.getHeight();
        Insets insets = getInsets();

        int x = insets.left;
        int y = insets.top;
        int w = width - insets.right - insets.left;
        int h = height - insets.top - insets.bottom;

        switch (tabPlacement) {
            case LEFT:
                // Note: we subtract 3, because this is the visual right margin of
                // the content border
                x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth) / 2;
                w -= (x - insets.left);
                break;
            case RIGHT:
                // Note: we subtract 3, because this is the visual right margin of
                // the content border
                w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth) / 2;
                break;
            case BOTTOM:
                // Note: we subtract 3, because this is the visual bottom margin of
                // the content border
                h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight) / 2;
                break;
            case TOP:
            default:
                // Note: we subtract 3, because this is the visual top margin of
                // the content border
                y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight) / 2;
                h -= (y - insets.top);
        }

        Color contentBackground = null;
        Border b;
        Component selectedComponent = tabPane.getSelectedComponent();
        if (selectedComponent instanceof JComponent) {
            contentBackground = (Color) ((JComponent) selectedComponent).getClientProperty("Quaqua.TabbedPaneChild.contentBackground");
        }
        if (contentBackground != null) {
            g.setPaint(TextureColor.getPaint(contentBackground, tabPane));
            g.fillRoundRect(x, y, w - 1, h - 1, 12, 12);
            b = UIManager.getBorder(getPropertyPrefix() + "emptyContentBorder");
        } else {
            b = UIManager.getBorder(getPropertyPrefix() + "contentBorder");
        }
        if (b instanceof BackgroundBorder) {
            b = ((BackgroundBorder) b).getBackgroundBorder();
        }
        if (b != null) {
            b.paintBorder(tabPane, g, x, y, w, h);
        }
    }

    protected String layoutTabLabel(int tabPlacement,
            FontMetrics metrics, int tabIndex,
            String title, Icon icon,
            Rectangle tabRect, Rectangle iconRect,
            Rectangle textRect, boolean isSelected) {
        textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

        View v = getTextViewForTab(tabIndex);
        if (v != null) {
            tabPane.putClientProperty("html", v);
        }

        String croppedLabel = SwingUtilities.layoutCompoundLabel((JComponent) tabPane,
                metrics, title, icon,
                SwingUtilities.CENTER,
                SwingUtilities.CENTER,
                SwingUtilities.CENTER,
                SwingUtilities.TRAILING,
                tabRect,
                iconRect,
                textRect,
                textIconGap);

        // Workaround for Apple's Java 1.5.0_05. We get a 0 width for rotated tabs
        if (textRect.width == 0) {
            textRect.x = tabRect.x;
        }



        tabPane.putClientProperty("html", null);

        int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        iconRect.x += xNudge;
        iconRect.y += yNudge;
        textRect.x += xNudge;
        textRect.y += yNudge;

        return croppedLabel;
    }

    @Override
    protected void paintTabArea(Graphics gr, int tabPlacement, int selectedIndex) {

        Graphics2D g;
        if (tabPlacement == LEFT || tabPlacement == RIGHT) {
            g = (Graphics2D) gr.create();
            AffineTransform t = new AffineTransform();
            if (tabPlacement == LEFT) {
                t.rotate(-Math.PI / 2, tabPane.getHeight() / 2, tabPane.getHeight() / 2);
            } else {
                t.rotate(Math.PI / 2, tabPane.getWidth() / 2, tabPane.getWidth() / 2);
            }
            g.transform(t);
        } else {
            g = (Graphics2D) gr;
        }

        int tCount = tabPane.getTabCount();

        Rectangle iconRect = new Rectangle(),
                textRect = new Rectangle();
        Rectangle clipRect = gr.getClipBounds();

        for (int j = 0; j < tCount; j++) {
            if (rects[j].intersects(clipRect)) {
                paintTab(g, tabPlacement, rects, j, iconRect, textRect);
            }
        }

        if (tabPlacement == LEFT || tabPlacement == RIGHT) {
            g.dispose();
        }
    }

    @Override
    protected void paintTab(Graphics gr, int tabPlacement,
            Rectangle[] rects, int tabIndex,
            Rectangle iconRect, Rectangle textRect) {
        Graphics2D g = (Graphics2D) gr;

        Rectangle tabRect = rects[tabIndex];
        if (tabPlacement == LEFT) {
            tabRect = new Rectangle(tabPane.getHeight() - tabRect.y - tabRect.height, tabRect.x, tabRect.height, tabRect.width);
        } else if (tabPlacement == RIGHT) {
            tabRect = new Rectangle(tabRect.y, tabPane.getWidth() - tabRect.x - tabRect.width, tabRect.height, tabRect.width);
        }
        int selectedIndex = tabPane.getSelectedIndex();
        boolean isSelected = selectedIndex == tabIndex;

        paintTabBackground(g, tabPlacement, tabIndex, tabRect.x, tabRect.y,
                tabRect.width, tabRect.height, isSelected);

        paintTabBorder(g, tabPlacement, tabIndex, tabRect.x, tabRect.y,
                tabRect.width, tabRect.height, isSelected);

        String title = tabPane.getTitleAt(tabIndex);
        Font font = tabPane.getFont();
        FontMetrics metrics = gr.getFontMetrics(font);
        Icon icon = getIconForTab(tabIndex);

        Insets insets = getTabInsets(tabPlacement, tabIndex);
        Rectangle innerTabRect;
        innerTabRect = new Rectangle(
                tabRect.x + insets.left,
                tabRect.y,// + insets.top,
                tabRect.width - insets.left - insets.right,
                tabRect.height// - insets.top - insets.bottom
                );

        title = layoutTabLabel(tabPlacement, metrics, tabIndex, title, icon,
                innerTabRect, iconRect, textRect, isSelected);
        /*
        g.setColor(Color.red);
        g.draw(textRect);
        g.setColor(Color.green);
        g.draw(innerTabRect);
        g.setColor(Color.blue);
        g.draw(iconRect);
         */
        paintText(g, tabPlacement, font, metrics,
                tabIndex, title, textRect, isSelected);

        paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);

        paintFocusIndicator(g, tabPlacement, rects, tabIndex,
                iconRect, textRect, isSelected);

    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement,
            int tabIndex,
            int x, int y, int w, int h,
            boolean isSelected) {
        
        String prefix = getPropertyPrefix();

        // Native tab pane border
        Border tb=UIManager.getBorder(prefix+"tabBorder");
        if (tb!=null) {
            
            
            return;
        }
        
        // Non-native tab pane border
        Border[] b;
        if (tabPane.getTabCount() == 1) {
            b = (Border[]) UIManager.get(prefix + "tabBorders");
        } else if (tabIndex == 0) {
            b = (Border[]) UIManager.get(prefix + "westTabBorders");
        } else if (tabIndex == tabPane.getTabCount() - 1) {
            b = (Border[]) UIManager.get(prefix + "eastTabBorders");
        } else {
            b = (Border[]) UIManager.get(prefix + "centerTabBorders");
        }

        boolean isEnabled = tabPane.isEnabled();
        boolean isOnActive = QuaquaUtilities.isOnActiveWindow(tabPane);
        int i;
        if (isEnabled && isOnActive) {
            i = (isSelected) ? 2 : 0;
        } else if (isEnabled) {
            i = (isSelected) ? 7 : 6;
        } else {
            i = (isSelected) ? 9 : 8;
        }

        // Compensate for visual margin of the border which is 3,3,3,3 and the
        // visual margin settings in effect
        Insets visualMargin = getTabVisualMargin();
        y -= 3 - visualMargin.top;
        h += 6 - visualMargin.top - visualMargin.bottom;
        if (tabIndex == 0) {
            x -= 3 - visualMargin.left;
        }
        if (tabIndex == 0 || tabIndex == tabPane.getTabCount() - 1) {
            w += 6 - visualMargin.left - visualMargin.right;
        }


        if (b != null) {
            b[i].paintBorder(tabPane, g, x, y, w, h);
        }
    }
    
    /**
     * this function draws the border around each tab
     * note that this function does now draw the background of the tab.
     * that is done elsewhere
     */
    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement,
            int tabIndex,
            int x, int y, int w, int h,
            boolean isSelected) {
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement,
            Font font, FontMetrics metrics, int tabIndex,
            String title, Rectangle textRect,
            boolean isSelected) {
        g.setFont(font);

        View v = getTextViewForTab(tabIndex);
        if (v != null) {
            // html
            v.paint(g, textRect);
        } else {
            // plain text
            int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);

            if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex)) {
                g.setColor(tabPane.getForegroundAt(tabIndex));
                BasicGraphicsUtils.drawStringUnderlineCharAt(g,
                        title, mnemIndex,
                        textRect.x, textRect.y + metrics.getAscent());

            } else { // tab disabled
                String prefix = getPropertyPrefix();

                Color c = UIManager.getColor(prefix + "disabledForeground");
                g.setColor((c != null) ? c : tabPane.getForeground());
                BasicGraphicsUtils.drawStringUnderlineCharAt(g,
                        title, mnemIndex,
                        textRect.x, textRect.y + metrics.getAscent());

            }
        }
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement,
            Rectangle[] rects, int tabIndex,
            Rectangle iconRect, Rectangle textRect,
            boolean isSelected) {
        String prefix = getPropertyPrefix();


        if (tabPane.hasFocus() && isSelected) {
            Border b;
            Rectangle r = rects[tabIndex];

            if (tabPlacement == LEFT) {
                r = new Rectangle(tabPane.getHeight() - r.y - r.height, r.x, r.height, r.width);
            } else if (tabPlacement == RIGHT) {
                r = new Rectangle(r.y, tabPane.getWidth() - r.x - r.width, r.height, r.width);
            }

            if (tabPane.getTabCount() == 1) {
                b = (Border) UIManager.get(prefix + "tabFocusRing");
            } else if (tabIndex == 0) {
                b = (Border) UIManager.get(prefix + "westTabFocusRing");
            } else if (tabIndex == tabPane.getTabCount() - 1) {
                b = (Border) UIManager.get(prefix + "eastTabFocusRing");
            } else {
                b = (Border) UIManager.get(prefix + "centerTabFocusRing");
            }

            // Compensate for visual margin of the border which is 3,3,3,3 and the
            // visual margin settings in effect
            int x = r.x;
            int y = r.y;
            int w = r.width;
            int h = r.height;
            Insets visualMargin = getTabVisualMargin();
            y -= 3 - visualMargin.top;
            h += 6 - visualMargin.top - visualMargin.bottom;
            if (tabIndex == 0) {
                x -= 3 - visualMargin.left;
            }
            if (tabIndex == 0 || tabIndex == tabPane.getTabCount() - 1) {
                w += 6 - visualMargin.left - visualMargin.right;
            }
            if (b != null) {
                b.paintBorder(tabPane, g, x, y, w, h);
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

    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    /**
     * Returns the tab index which intersects the specified point
     * in the JTabbedPane's coordinate space.
     */
    @Override
    public int tabForCoordinate(JTabbedPane pane, int x, int y) {
        return getTabAtLocation(x, y);
    }

    /**
     * Returns the bounds of the specified tab in the coordinate space
     * of the JTabbedPane component.  This is required because the tab rects
     * are by default defined in the coordinate space of the component where
     * they are rendered, which could be the JTabbedPane
     * (for WRAP_TAB_LAYOUT) or a ScrollableTabPanel (SCROLL_TAB_LAYOUT).
     * This method should be used whenever the tab rectangle must be relative
     * to the JTabbedPane itself and the result should be placed in a
     * designated Rectangle object (rather than instantiating and returning
     * a new Rectangle each time). The tab index parameter must be a valid
     * tabbed pane tab index (0 to tab count - 1, inclusive).  The destination
     * rectangle parameter must be a valid <code>Rectangle</code> instance.
     * The handling of invalid parameters is unspecified.
     *
     * @param tabIndex the index of the tab
     * @param dest the rectangle where the result should be placed
     * @return the resulting rectangle
     *
     * @since 1.4
     */
    @Override
    protected Rectangle getTabBounds(int tabIndex, Rectangle dest) {
        if (tabsCombo.isVisible()) {
            dest.x = tabsCombo.getX();
            dest.y = tabsCombo.getY();
            dest.width = tabsCombo.getWidth();
            dest.height = tabsCombo.getHeight();
        } else {
            dest.width = rects[tabIndex].width;
            dest.height = rects[tabIndex].height;

            dest.x = rects[tabIndex].x;
            dest.y = rects[tabIndex].y;
        }
        return dest;
    }

    /**
     * Returns the tab index which intersects the specified point
     * in the coordinate space of the component where the
     * tabs are actually rendered, which could be the JTabbedPane
     * (for WRAP_TAB_LAYOUT) or a ScrollableTabPanel (SCROLL_TAB_LAYOUT).
     */
    private int getTabAtLocation(int x, int y) {
        ensureCurrentLayout();

        if (tabsCombo.isVisible()) {
            if (tabsCombo.contains(x - tabsCombo.getX(), y - tabsCombo.getY())) {
                return tabsCombo.getSelectedIndex();
            }
        } else {
            int tCount = tabPane.getTabCount();
            for (int i = 0; i < tCount; i++) {
                if (rects[i].contains(x, y)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the index of the tab closest to the passed in location, note
     * that the returned tab may not contain the location x,y.
     */
    private int getClosestTab(int x, int y) {
        int min = 0;
        int tCount = Math.min(rects.length, tabPane.getTabCount());
        int max = tCount;
        int tabPlacement = tabPane.getTabPlacement();
        boolean useX = (tabPlacement == TOP || tabPlacement == BOTTOM);
        int want = (useX) ? x : y;

        while (min != max) {
            int current = (max + min) >>> 1; // Compute average without overflow
            int minLoc;
            int maxLoc;

            if (useX) {
                minLoc = rects[current].x;
                maxLoc = minLoc + rects[current].width;
            } else {
                minLoc = rects[current].y;
                maxLoc = minLoc + rects[current].height;
            }
            if (want < minLoc) {
                max = current;
                if (min == max) {
                    return Math.max(0, current - 1);
                }
            } else if (want >= maxLoc) {
                min = current;
                if (max - min <= 1) {
                    return Math.max(current + 1, tCount - 1);
                }
            } else {
                return current;
            }
        }
        return min;
    }

    /**
     * Reloads the mnemonics. This should be invoked when a memonic changes,
     * when the title of a mnemonic changes, or when tabs are added/removed.
     */
    private void updateMnemonics() {
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

    /**
     * Returns the text View object required to render stylized text (HTML) for
     * the specified tab or null if no specialized text rendering is needed
     * for this tab. This is provided to support html rendering inside tabs.
     *
     * @param tabIndex the index of the tab
     * @return the text view to render the tab's text or null if no
     *         specialized rendering is required
     *
     * @since 1.4
     */
    @Override
    protected View getTextViewForTab(int tabIndex) {
        if (htmlViews != null) {
            return (View) htmlViews.elementAt(tabIndex);
        }
        return null;
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        int height = 0;
        View v = getTextViewForTab(tabIndex);
        if (v != null) {
            // html
            height += (int) v.getPreferredSpan(View.Y_AXIS);
        } else {
            // plain text
            height += fontHeight;
        }
        Icon icon = getIconForTab(tabIndex);
        Insets tInsets = getTabInsets(tabPlacement, tabIndex);

        if (icon != null) {
            height = Math.max(height, icon.getIconHeight());
        }
        height += tInsets.top + tInsets.bottom;// + 2;
        return height;
    }

    @Override
    protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
        Insets tAreaInsets = getTabAreaInsets(tabPlacement);
        int tRunOverlay = getTabRunOverlay(tabPlacement);
        return (horizRunCount > 0 ? horizRunCount * (maxTabHeight - tRunOverlay) + tRunOverlay
                + tAreaInsets.top + tAreaInsets.bottom : 0);
    }

    @Override
    protected int calculateTabAreaWidth(int tabPlacement, int vertRunCount, int maxTabWidth) {
        Insets tAreaInsets = getTabAreaInsets(tabPlacement);
        int tRunOverlay = getTabRunOverlay(tabPlacement);
        int result = (vertRunCount > 0 ? vertRunCount * (maxTabWidth - tRunOverlay) + tRunOverlay
                + tAreaInsets.left + tAreaInsets.right : 0);
        return result;
    }

    InputMap getInputMap(int condition) {
        String prefix = getPropertyPrefix();

        if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            return (InputMap) UIManager.get(prefix + "ancestorInputMap");
        } else if (condition == JComponent.WHEN_FOCUSED) {
            return (InputMap) UIManager.get(prefix + "focusInputMap");
        }
        return null;
    }

    protected void repaintTabArea() {
        int tabPlacement = tabPane.getTabPlacement();

        Rectangle clipRect = new Rectangle();
        Insets insets = getInsets();//InsetsUtil.add(tabPane.getInsets(), getVisualMargin());
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

    ActionMap getActionMap() {
        String prefix = getPropertyPrefix();

        ActionMap map = (ActionMap) UIManager.get(prefix + "actionMap");

        if (map == null) {
            map = createActionMap();
            if (map != null) {
                UIManager.getLookAndFeelDefaults().put(prefix + "actionMap",
                        map);
            }
        }
        return map;
    }

    ActionMap createActionMap() {
        ActionMap map = new ActionMapUIResource();
        map.put("navigateNext", new NextAction());
        map.put("navigatePrevious", new PreviousAction());
        map.put("navigateRight", new RightAction());
        map.put("navigateLeft", new LeftAction());
        map.put("navigateUp", new UpAction());
        map.put("navigateDown", new DownAction());
        map.put("navigatePageUp", new PageUpAction());
        map.put("navigatePageDown", new PageDownAction());
        map.put("requestFocus", new RequestFocusAction());
        map.put("requestFocusForVisibleComponent",
                new RequestFocusForVisibleAction());
        map.put("setSelectedIndex", new SetSelectedIndexAction());
        return map;
    }

    @Override
    protected boolean shouldRotateTabRuns(int tabPlacement) {
        return false;
    }

    /* GES 2/3/99:
    The container listener code was added to support HTML
    rendering of tab titles.

    Ideally, we would be able to listen for property changes
    when a tab is added or its text modified.  At the moment
    there are no such events because the Beans spec doesn't
    allow 'indexed' property changes (i.e. tab 2's text changed
    from A to B).

    In order to get around this, we listen for tabs to be added
    or removed by listening for the container events.  we then
    queue up a runnable (so the component has a chance to complete
    the add) which checks the tab title of the new component to see
    if it requires HTML rendering.

    The Views (one per tab title requiring HTML rendering) are
    stored in the htmlViews Vector, which is only allocated after
    the first time we run into an HTML tab.  Note that this vector
    is kept in step with the number of pages, and nulls are added
    for those pages whose tab title do not require HTML rendering.

    This makes it easy for the paint and layout code to tell
    whether to invoke the HTML engine without having to check
    the string during time-sensitive operations.

    When we have added a way to listen for tab additions and
    changes to tab text, this code should be removed and
    replaced by something which uses that.  */
    private class ContainerHandler implements ContainerListener {

        public void componentAdded(ContainerEvent e) {
            JTabbedPane tp = (JTabbedPane) e.getContainer();
            Component child = e.getChild();
            if (child instanceof UIResource) {
                return;
            }
            int index = tp.indexOfComponent(child);
            String title = tp.getTitleAt(index);
            boolean isHTML = BasicHTML.isHTMLString(title);
            if (isHTML) {
                if (htmlViews == null) {    // Initialize vector
                    htmlViews = createHTMLVector();
                } else {                  // Vector already exists
                    View v = BasicHTML.createHTMLView(tp, title);
                    htmlViews.insertElementAt(v, index);
                }
            } else {                             // Not HTML
                if (htmlViews != null) {           // Add placeholder
                    htmlViews.insertElementAt(null, index);
                }                                // else nada!
            }
        }

        public void componentRemoved(ContainerEvent e) {
            JTabbedPane tp = (JTabbedPane) e.getContainer();
            Component child = e.getChild();
            if (child instanceof UIResource) {
                return;
            }

            // NOTE 4/15/2002 (joutwate):
            // This fix is implemented using client properties since there is
            // currently no IndexPropertyChangeEvent.  Once
            // IndexPropertyChangeEvents have been added this code should be
            // modified to use it.
            Integer indexObj =
                    (Integer) tp.getClientProperty("__index_to_remove__");
            if (indexObj != null) {
                int index = indexObj.intValue();
                if (htmlViews != null && htmlViews.size() >= index) {
                    htmlViews.removeElementAt(index);
                }
            }
        }
    }

    private Vector createHTMLVector() {
        Vector hViews = new Vector();
        int count = tabPane.getTabCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                String title = tabPane.getTitleAt(i);
                if (BasicHTML.isHTMLString(title)) {
                    hViews.addElement(BasicHTML.createHTMLView(tabPane, title));
                } else {
                    hViews.addElement(null);
                }
            }
        }
        return hViews;
    }

    public boolean requestFocusForVisibleComponent() {
        Component visibleComponent = getVisibleComponent();
        if (visibleComponent.isFocusable()) {
            visibleComponent.requestFocus();
            return true;
        } else if (visibleComponent instanceof JComponent) {
            if (((JComponent) visibleComponent).requestDefaultFocus()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void navigateSelectedTab(int direction) {
        // super.navigateSelectedTab(direction);
        int tabPlacement = tabPane.getTabPlacement();
        if (tabsCombo.isVisible()) {
            tabPlacement = -1;
        }


        /*
        int current = QuaquaManager.getBoolean(
        "TabbedPane.selectionFollowsFocus") ?
        tabPane.getSelectedIndex() : getFocusIndex();*/
        int current = tabPane.getSelectedIndex();
        int tCount = tabPane.getTabCount();
        boolean leftToRight = QuaquaUtilities.isLeftToRight(tabPane);

        // If we have no tabs then don't navigate.
        if (tCount <= 0) {
            return;
        }

        int offset;
        switch (tabPlacement) {
            case LEFT:
                switch (direction) {
                    case NEXT:
                        selectNextTab(current);
                        break;
                    case PREVIOUS:
                        selectPreviousTab(current);
                        break;
                    case NORTH:
                        selectNextTab(current);
                        break;
                    case SOUTH:
                        selectPreviousTab(current);
                        break;
                    case WEST:
                        offset = getTabRunOffset(tabPlacement, tCount, current, false);
                        selectAdjacentRunTab(tabPlacement, current, offset);
                        break;
                    case EAST:
                        offset = getTabRunOffset(tabPlacement, tCount, current, true);
                        selectAdjacentRunTab(tabPlacement, current, offset);
                        break;
                    default:
                }
                break;
            case RIGHT:
                switch (direction) {
                    case NEXT:
                        selectNextTab(current);
                        break;
                    case PREVIOUS:
                        selectPreviousTab(current);
                        break;
                    case NORTH:
                        selectPreviousTabInRun(current);
                        break;
                    case SOUTH:
                        selectNextTabInRun(current);
                        break;
                    case WEST:
                        offset = getTabRunOffset(tabPlacement, tCount, current, false);
                        selectAdjacentRunTab(tabPlacement, current, offset);
                        break;
                    case EAST:
                        offset = getTabRunOffset(tabPlacement, tCount, current, true);
                        selectAdjacentRunTab(tabPlacement, current, offset);
                        break;
                    default:
                }
                break;
            case BOTTOM:
            case TOP:
                switch (direction) {
                    case NEXT:
                        selectNextTab(current);
                        break;
                    case PREVIOUS:
                        selectPreviousTab(current);
                        break;
                    case NORTH:
                    /*
                    offset = getTabRunOffset(tabPlacement, tabCount, current, false);
                    selectAdjacentRunTab(tabPlacement, current, offset);
                    break;*/
                    case SOUTH:
                        /*
                        offset = getTabRunOffset(tabPlacement, tabCount, current, true);
                        selectAdjacentRunTab(tabPlacement, current, offset);*/
                        break;
                    case EAST:
                        if (leftToRight) {
                            selectNextTabInRun(current);
                        } else {
                            selectPreviousTabInRun(current);
                        }
                        break;
                    case WEST:
                        if (leftToRight) {
                            selectPreviousTabInRun(current);
                        } else {
                            selectNextTabInRun(current);
                        }
                        break;
                    default:
                }
                break;
            default:
                switch (direction) {
                    case NEXT:
                        selectNextTab(current);
                        break;
                    case PREVIOUS:
                        selectPreviousTab(current);
                        break;
                    case NORTH:
                        selectPreviousTab(current);
                        break;
                    case SOUTH:
                        selectNextTab(current);
                        break;
                    case EAST:
                        if (leftToRight) {
                            selectNextTabInRun(current);
                        } else {
                            selectPreviousTabInRun(current);
                        }
                        break;
                    case WEST:
                        if (leftToRight) {
                            selectPreviousTabInRun(current);
                        } else {
                            selectNextTabInRun(current);
                        }
                        break;
                    default:
                }
        }
    }

    private static class RightAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            NavigatableTabbedPaneUI ui = (NavigatableTabbedPaneUI) pane.getUI();
            ui.navigateSelectedTab(EAST);
        }
    };

    private static class LeftAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            NavigatableTabbedPaneUI ui = (NavigatableTabbedPaneUI) pane.getUI();
            ui.navigateSelectedTab(WEST);
        }
    };

    private static class UpAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            NavigatableTabbedPaneUI ui = (NavigatableTabbedPaneUI) pane.getUI();
            ui.navigateSelectedTab(NORTH);
        }
    };

    private static class DownAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            NavigatableTabbedPaneUI ui = (NavigatableTabbedPaneUI) pane.getUI();
            ui.navigateSelectedTab(SOUTH);
        }
    };

    private static class NextAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            NavigatableTabbedPaneUI ui = (NavigatableTabbedPaneUI) pane.getUI();
            ui.navigateSelectedTab(NEXT);
        }
    };

    private static class PreviousAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            NavigatableTabbedPaneUI ui = (NavigatableTabbedPaneUI) pane.getUI();
            ui.navigateSelectedTab(PREVIOUS);
        }
    };

    private static class PageUpAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            NavigatableTabbedPaneUI ui = (NavigatableTabbedPaneUI) pane.getUI();
            int tabPlacement = pane.getTabPlacement();
            if (tabPlacement == TOP || tabPlacement == BOTTOM) {
                ui.navigateSelectedTab(WEST);
            } else {
                ui.navigateSelectedTab(NORTH);
            }
        }
    };

    private static class PageDownAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            NavigatableTabbedPaneUI ui = (NavigatableTabbedPaneUI) pane.getUI();
            int tabPlacement = pane.getTabPlacement();
            if (tabPlacement == TOP || tabPlacement == BOTTOM) {
                ui.navigateSelectedTab(EAST);
            } else {
                ui.navigateSelectedTab(SOUTH);
            }
        }
    };

    private static class RequestFocusAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            pane.requestFocus();
        }
    };

    private static class RequestFocusForVisibleAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            NavigatableTabbedPaneUI ui = (NavigatableTabbedPaneUI) pane.getUI();
            ui.requestFocusForVisibleComponent();
        }
    };

    /**
     * Selects a tab in the JTabbedPane based on the String of the
     * action command. The tab selected is based on the first tab that
     * has a mnemonic matching the first character of the action command.
     */
    private static class SetSelectedIndexAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JTabbedPane pane = (JTabbedPane) e.getSource();

            if (pane != null && (pane.getUI() instanceof BasicTabbedPaneUI)) {
                NavigatableTabbedPaneUI ui = (NavigatableTabbedPaneUI) pane.getUI();
                String command = e.getActionCommand();

                if (command != null && command.length() > 0) {
                    int mnemonic = (int) e.getActionCommand().charAt(0);
                    if (mnemonic >= 'a' && mnemonic <= 'z') {
                        mnemonic -= ('a' - 'A');
                    }
                    Integer index = ui.getIndexForMnemonic(mnemonic);
                    if (index != null && pane.isEnabledAt(index.intValue())) {
                        pane.setSelectedIndex(index.intValue());
                    }
                }
            }
        }
    };

    /**
     * Handles item changes in the tabsCombo JComboBox.
     *
     * FIXME - Actually, this listener is not needed. The combo box model
     * already does the job for us.
     */
    private static class ItemHandler implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            //    tabPane.setSelectedIndex(tabsCombo.getSelectedIndex());
        }
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTabbedPaneUI.
     */
    public class QuaquaTabbedPaneLayout //implements LayoutManager {
            extends BasicTabbedPaneUI.TabbedPaneLayout {

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return calculateSize(false);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return calculateSize(true);
        }

        @Override
        protected Dimension calculateSize(boolean minimum) {
            int tabPlacement = tabPane.getTabPlacement();
            Insets insets = getInsets();
            Insets contentInsets = getContentBorderInsets(tabPlacement);
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);

            Dimension zeroSize = new Dimension(0, 0);
            int height = contentInsets.top + contentInsets.bottom;
            int width = contentInsets.left + contentInsets.right;
            int cWidth = 0;
            int cHeight = 0;

            // Determine minimum size required to display largest
            // child in each dimension
            //
            for (int i = 0; i < tabPane.getTabCount(); i++) {
                Component component = tabPane.getComponentAt(i);
                if (component != null) {
                    Dimension size = zeroSize;
                    size = minimum ? component.getMinimumSize() : component.getPreferredSize();

                    if (size != null) {
                        cHeight = Math.max(size.height, cHeight);
                        cWidth = Math.max(size.width, cWidth);
                    }
                }
            }
            // Add content border insets to minimum size
            width += cWidth;
            height += cHeight;
            int tabExtent = 0;

            // Calculate how much space the tabs will need, based on the
            // minimum size required to display largest child + content border
            //
            switch (tabPlacement) {
                case LEFT:
                case RIGHT:
                    height = Math.max(height, calculateMaxTabHeight(tabPlacement)
                            + tabAreaInsets.top + tabAreaInsets.bottom);
                    tabExtent = preferredTabAreaWidth(tabPlacement, height);
                    width += tabExtent;
                    break;
                case TOP:
                case BOTTOM:
                default:
                    width = Math.max(width, calculateMaxTabWidth(tabPlacement)
                            + tabAreaInsets.left + tabAreaInsets.right);
                    tabExtent = preferredTabAreaHeight(tabPlacement, width);
                    height += tabExtent;
            }
            return new Dimension(width + insets.left + insets.right,
                    height + insets.bottom + insets.top);

        }

        @Override
        protected int preferredTabAreaHeight(int tabPlacement, int width) {
            FontMetrics metrics = getFontMetrics();
            int tabCount = tabPane.getTabCount();
            int total = 0;
            if (tabCount > 0) {
                int rows = 1;
                int x = 0;

                int maxTabHeight = calculateMaxTabHeight(tabPlacement);

                for (int i = 0; i < tabCount; i++) {
                    int tabWidth = calculateTabWidth(tabPlacement, i, metrics);

                    if (x != 0 && x + tabWidth > width) {
                        rows++;
                        x = 0;
                    }
                    x += tabWidth;
                }
                total = calculateTabAreaHeight(tabPlacement, rows, maxTabHeight);
            }
            return total;
        }

        @Override
        protected int preferredTabAreaWidth(int tabPlacement, int height) {
            FontMetrics metrics = getFontMetrics();
            int tabCount = tabPane.getTabCount();
            int total = 0;
            if (tabCount > 0) {
                int columns = 1;
                int y = 0;
                int fontHeight = metrics.getHeight();

                if (tabPlacement == LEFT || tabPlacement == RIGHT) {
                    maxTabWidth = calculateMaxTabHeight(tabPlacement);
                } else {
                    maxTabWidth = calculateMaxTabWidth(tabPlacement);
                }

                for (int i = 0; i < tabCount; i++) {
                    int tabHeight = calculateTabHeight(tabPlacement, i, fontHeight);

                    if (y != 0 && y + tabHeight > height) {
                        columns++;
                        y = 0;
                    }
                    y += tabHeight;
                }
                total = calculateTabAreaWidth(tabPlacement, columns, maxTabWidth);
            }
            return total;
        }

        @Override
        public void layoutContainer(Container parent) {
            int tabPlacement = tabPane.getTabPlacement();
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            Insets insets = getInsets();//tabPane.getInsets();
            int selectedIndex = tabPane.getSelectedIndex();
            Component visibleComponent = getVisibleComponent();

            calculateLayoutInfo();

            if (selectedIndex < 0) {
                if (visibleComponent != null) {
                    // The last tab was removed, so remove the component
                    setVisibleComponent(null);
                }
            } else {
                int tx, ty, tw, th; // tab area bounds
                int cx, cy, cw, ch; // content area bounds
                Insets contentInsets = getContentBorderInsets(tabPlacement);

                Component selectedComponent = tabPane.getComponentAt(selectedIndex);
                boolean shouldChangeFocus = false;

                // In order to allow programs to use a single component
                // as the display for multiple tabs, we will not change
                // the visible component if the currently selected tab
                // has a null component.  This is a bit dicey, as we don't
                // explicitly state we support this in the spec, but since
                // programs are now depending on this, we're making it work.
                //
                if (selectedComponent != null) {
                    if (selectedComponent != visibleComponent
                            && visibleComponent != null) {
                        if (QuaquaUtilities.findFocusOwner(visibleComponent) != null) {
                            shouldChangeFocus = true;
                        }
                    }
                    setVisibleComponent(selectedComponent);
                }

                Rectangle bounds = tabPane.getBounds();
                int numChildren = tabPane.getComponentCount();

                if (numChildren > 0) {

                    switch (tabPlacement) {
                        case LEFT:
                            // calculate tab area bounds
                            tw = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                            th = bounds.height - insets.top - insets.bottom;
                            tx = insets.left;
                            ty = insets.top;

                            // calculate content area bounds
                            cx = insets.left + tw + contentInsets.left;
                            cy = insets.top + contentInsets.top;
                            cw = bounds.width - insets.left - insets.right - tw
                                    - contentInsets.left - contentInsets.right;
                            ch = bounds.height - insets.top - insets.bottom
                                    - contentInsets.top - contentInsets.bottom;
                            break;
                        case RIGHT:
                            // calculate tab area bounds
                            tw = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                            th = bounds.height - insets.top - insets.bottom;
                            tx = bounds.width - insets.right - tw;
                            ty = insets.top;

                            // calculate content area bounds
                            cx = insets.left + contentInsets.left;
                            cy = insets.top + contentInsets.top;
                            cw = bounds.width - insets.left - insets.right - tw
                                    - contentInsets.left - contentInsets.right;
                            ch = bounds.height - insets.top - insets.bottom
                                    - contentInsets.top - contentInsets.bottom;
                            break;
                        case BOTTOM:
                            // calculate tab area bounds
                            tw = bounds.width - insets.left - insets.right;
                            th = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                            tx = insets.left;
                            ty = bounds.height - insets.bottom - th;

                            // calculate content area bounds
                            cx = insets.left + contentInsets.left;
                            cy = insets.top + contentInsets.top;
                            cw = bounds.width - insets.left - insets.right
                                    - contentInsets.left - contentInsets.right;
                            ch = bounds.height - insets.top - insets.bottom - th
                                    - contentInsets.top - contentInsets.bottom;
                            break;
                        case TOP:
                        default:
                            // calculate tab area bounds
                            tw = bounds.width - insets.left - insets.right;
                            th = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                            tx = insets.left;
                            ty = insets.top;

                            // calculate content area bounds
                            cx = insets.left + contentInsets.left;
                            cy = insets.top + th + contentInsets.top;
                            cw = bounds.width - insets.left - insets.right
                                    - contentInsets.left - contentInsets.right;
                            ch = bounds.height - insets.top - insets.bottom - th
                                    - contentInsets.top - contentInsets.bottom;
                    }

                    for (int i = 0; i < numChildren; i++) {
                        Component child = tabPane.getComponent(i);
                        if (child instanceof TabsComboBox) {
                            Dimension preferredSize = child.getPreferredSize();
                            preferredSize.width = Math.min(tw - tabAreaInsets.left - tabAreaInsets.right, preferredSize.width);

                            Integer propertyValue = (Integer) tabPane.getClientProperty("Quaqua.TabbedPane.tabAlignment");
                            int tabAlignment = (propertyValue != null && propertyValue.intValue() == SwingConstants.LEADING) ? SwingConstants.LEADING : SwingConstants.CENTER;
                            if (tabAlignment == SwingConstants.CENTER) {
                                child.setBounds(
                                        tx + (tw - preferredSize.width) / 2,
                                        ty + (th - preferredSize.height) / 2,
                                        preferredSize.width,
                                        preferredSize.height);
                            } else {
                                child.setBounds(
                                        tx + tabAreaInsets.left,
                                        ty + (th - preferredSize.height) / 2,
                                        preferredSize.width,
                                        preferredSize.height);
                            }
                        } else {

                            child.setBounds(cx, cy, cw, ch);
                        }
                    }
                }

                if (shouldChangeFocus) {
                    if (!requestFocusForVisibleComponent()) {
                        tabPane.requestFocus();
                    }
                }
            }
        }

        @Override
        public void calculateLayoutInfo() {
            int tabCount = tabPane.getTabCount();
            assureRectsCreated(tabCount);
            calculateTabRects(tabPane.getTabPlacement(), tabCount);
        }

        @Override
        protected void calculateTabRects(int tabPlacement, int tabCount) {
            FontMetrics metrics = getFontMetrics();
            Dimension size = tabPane.getSize();
            Insets insets = getInsets(); //tabPane.getInsets();
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
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
                    //maxTabWidth = calculateMaxTabWidth(tabPlacement);
                    maxTabWidth = calculateMaxTabHeight(tabPlacement);
                    x = insets.left + tabAreaInsets.left;
                    y = insets.top + tabAreaInsets.top;
                    returnAt = size.height - (insets.bottom + tabAreaInsets.bottom);
                    break;
                case RIGHT:
                    //maxTabWidth = calculateMaxTabWidth(tabPlacement);
                    maxTabWidth = calculateMaxTabHeight(tabPlacement);
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

            Rectangle rect;

            // Squeeze tabs if they don't fit into the tab run
            int avgTabSize;
            int availableTabSize;
            if (!verticalTabRuns) {
                availableTabSize = returnAt - x;
                avgTabSize = availableTabSize / tabCount;
            } else {
                availableTabSize = returnAt - y;
                avgTabSize = availableTabSize / tabCount;
            }

            int redistributableSize = 0;
            int totalTabSize = 0;
            int minTabSize = Integer.MAX_VALUE;
            for (i = 0; i < tabCount; i++) {
                rect = rects[i];
                if (!verticalTabRuns) {
                    // Tabs on TOP or BOTTOM...
                    rect.width = calculateTabWidth(tabPlacement, i, metrics);
                    maxTabWidth = Math.max(maxTabWidth, rect.width);
                    totalTabSize += rect.width;
                    if (rect.width < avgTabSize) {
                        redistributableSize += avgTabSize - rect.width;
                    }
                } else {
                    // Tabs on LEFT or RIGHT....
                    //rect.height = calculateTabHeight(tabPlacement, i, fontHeight);
                    rect.height = calculateTabWidth(tabPlacement, i, metrics);
                    maxTabHeight = Math.max(maxTabHeight, rect.height);
                    totalTabSize += rect.height;
                    if (rect.height < avgTabSize) {
                        redistributableSize += avgTabSize - rect.height;
                    }
                }
            }
            if (verticalTabRuns
                    || totalTabSize <= availableTabSize
                    || redistributableSize > 0 && tabPane.getClientProperty("Quaqua.TabbedPane.shortenTabs") != Boolean.FALSE) {
                // Tabs are on the LEFT or RIGHT or
                // enough space is available or
                // redistributable size is available and we are allowed to
                // shorten the tabs
                tabsCombo.setVisible(false);
            } else {
                // No redistributable size is available
                // We put all tabs into a combo box
                tabsCombo.setVisible(true);
            }

            // Run through tabs and partition them into runs
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

                    if (rect.width > avgTabSize) {
                        rect.width = Math.min(rect.width, redistributableSize + avgTabSize);
                        redistributableSize -= rect.width - avgTabSize;
                    }

                    rect.y = y;
                    rect.height = maxTabHeight/* - 2*/;

                } else {
                    // Tabs on LEFT or RIGHT...
                    if (tabPlacement == LEFT) {
                        if (rect.height > avgTabSize) {
                            rect.height = Math.min(rect.height, redistributableSize + avgTabSize);
                            redistributableSize -= rect.height - avgTabSize;
                        }

                        rect.x = x;
                        rect.width = maxTabWidth/* - 2*/;
                        if (i > 0) {
                            rect.y = rects[i - 1].y - rect.height;
                        } else {
                            tabRuns[0] = 0;
                            runCount = 1;
                            maxTabHeight = 0;
                            rect.y = returnAt - rect.height;
                        }
                    } else {
                        if (i > 0) {
                            rect.y = rects[i - 1].y + rects[i - 1].height;
                        } else {
                            tabRuns[0] = 0;
                            runCount = 1;
                            maxTabHeight = 0;
                            rect.y = y;
                        }

                        if (rect.height > avgTabSize) {
                            rect.height = Math.min(rect.height, redistributableSize + avgTabSize);
                            redistributableSize -= rect.height - avgTabSize;
                        }

                        rect.x = x;
                        rect.width = maxTabWidth/* - 2*/;
                    }
                }
                if (i == selectedIndex) {
                    selectedRun = runCount - 1;
                }
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
                        padTabRun(tabPlacement, start, end, returnAt);
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
                        padTabRun(tabPlacement, start, end, returnAt);
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
                            if (tabPlacement == LEFT) {
                                pad = -pad;
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

        /*
         * Rotates the run-index array so that the selected run is run[0]
         */
        @Override
        protected void rotateTabRuns(int tabPlacement, int selectedRun) {
            for (int i = 0; i < selectedRun; i++) {
                int save = tabRuns[0];
                for (int j = 1; j < runCount; j++) {
                    tabRuns[j - 1] = tabRuns[j];
                }
                tabRuns[runCount - 1] = save;
            }
        }

        @Override
        protected void normalizeTabRuns(int tabPlacement, int tabCount,
                int start, int max) {
            boolean verticalTabRuns = (tabPlacement == LEFT || tabPlacement == RIGHT);
            int run = runCount - 1;
            boolean keepAdjusting = true;
            double weight = 1.25;

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
                    weight += .25;
                }
            }
        }

        @Override
        protected void padTabRun(int tabPlacement, int start, int end, int max) {
            Rectangle lastRect = rects[end];
            if (tabPlacement == TOP || tabPlacement == BOTTOM) {
                int runWidth = (lastRect.x + lastRect.width) - rects[start].x;
                int deltaWidth = max - (lastRect.x + lastRect.width);
                float factor = (float) deltaWidth / (float) runWidth;

                for (int j = start; j <= end; j++) {
                    Rectangle pastRect = rects[j];
                    if (j > start) {
                        pastRect.x = rects[j - 1].x + rects[j - 1].width;
                    }
                    pastRect.width += Math.round((float) pastRect.width * factor);
                }
                lastRect.width = max - lastRect.x;
            } else {
                int runHeight = (lastRect.y + lastRect.height) - rects[start].y;
                int deltaHeight = max - (lastRect.y + lastRect.height);
                float factor = (float) deltaHeight / (float) runHeight;

                for (int j = start; j <= end; j++) {
                    Rectangle pastRect = rects[j];
                    if (j > start) {
                        pastRect.y = rects[j - 1].y + rects[j - 1].height;
                    }
                    pastRect.height += Math.round((float) pastRect.height * factor);
                }
                lastRect.height = max - lastRect.y;
            }
        }

        @Override
        protected void padSelectedTab(int tabPlacement, int selectedIndex) {

            if (selectedIndex >= 0) {
                Rectangle selRect = rects[selectedIndex];
                Insets padInsets = getSelectedTabPadInsets(tabPlacement);
                selRect.x -= padInsets.left;
                selRect.width += (padInsets.left + padInsets.right);
                selRect.y -= padInsets.top;
                selRect.height += (padInsets.top + padInsets.bottom);
            }
        }
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTabbedPaneUI.
     */
    public class QuaquaMouseHandler extends BasicTabbedPaneUI.MouseHandler {

        @Override
        public void mousePressed(MouseEvent evt) {
            if (!tabsCombo.isVisible()) {
                super.mousePressed(evt);
            }
        }
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTabbedPaneUI.
     */
    public class QuaquaPropertyHandler extends BasicTabbedPaneUI.PropertyChangeHandler {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if ("enabled".equals(name)) {
                tabsCombo.setEnabled(tabPane.isEnabled());
            } else if (name.equals("Frame.active")) {
                repaintTabArea();
            } else if (name.equals("font")) {
                tabsCombo.setFont((Font) evt.getNewValue());
            } else if (name.equals("JComponent.sizeVariant")) {
                QuaquaUtilities.applySizeVariant(tabPane);
            }
            // Forward everything except tabLayoutPolicy change to super class.
            // tabLayoutPolicy must not be forward, because it would break
            // the functionality of class
            // ch.randelshofer.quaqua.panther.QuaquaPantherTabbedPaneUI.
            if (!name.equals("tabLayoutPolicy")) {
                super.propertyChange(evt);
            }

            if (name == "indexForTitle"//
                    || name == "indexForNullComponent") {
                Integer index = (Integer) evt.getNewValue();
                if (htmlViews != null) {
                    htmlViews.removeElementAt(index);
                }
                updateHtmlViews(index);
            }
        }

        private void updateHtmlViews(int index) {
            String title = tabPane.getTitleAt(index);
            boolean isHTML = BasicHTML.isHTMLString(title);
            if (isHTML) {
                if (htmlViews == null) {    // Initialize vector
                    htmlViews = createHTMLVector();
                } else {                  // Vector already exists
                    View v = BasicHTML.createHTMLView(tabPane, title);
                    htmlViews.insertElementAt(v, index);
                }
            } else {                             // Not HTML
                if (htmlViews != null) {           // Add placeholder
                    htmlViews.insertElementAt(null, index);
                }                                // else nada!
            }
            updateMnemonics();
            ((TabsComboBoxModel)tabsCombo.getModel()).stateChanged(null);
        }
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTabbedPaneUI.
     */
    private static class QuaquaTabSelectionHandler implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            JTabbedPane tabPane = (JTabbedPane) e.getSource();
            tabPane.revalidate();
            tabPane.repaint();
        }
    }

    private static class TabsComboBoxModel extends AbstractListModel implements ComboBoxModel, ContainerListener, ChangeListener {

        private JTabbedPane model;

        public void setModel(JTabbedPane newValue) {
            if (model != null) {
                model.removeContainerListener(this);
                model.removeChangeListener(this);
                if (model.getTabCount() > 0) {
                    fireIntervalRemoved(this, 0, model.getTabCount() - 1);
                }
            }
            model = newValue;
            if (model != null) {
                model.addContainerListener(this);
                model.addChangeListener(this);
                if (model.getTabCount() > 0) {
                    fireIntervalAdded(this, 0, model.getTabCount() - 1);
                }
                fireContentsChanged(this, 0, model.getTabCount()-1);
            }
        }

        public Object getElementAt(int index) {
            return (model == null) ? null : model.getTitleAt(index);
        }

        public Object getSelectedItem() {
            if (model == null) {
                return null;
            }

            int index = model.getSelectedIndex();
            return (index != -1 && index < model.getTabCount()) ? getElementAt(index) : null;
        }

        public int getSize() {
            // FIXME - This variable may not yet be up to date after we
            // received a componentAdded/componentRemoved event.
            return (model == null) ? 0 : model.getTabCount();
        }

        public void setSelectedItem(Object anItem) {
            if (model == null) {
                return;
            }

            for (int i = 0, n = model.getTabCount(); i < n; i++) {
                if (model.getTitleAt(i).equals(anItem)) {
                    model.setSelectedIndex(i);
                    break;
                }
            }
        }

        public void componentAdded(ContainerEvent evt) {
            Component child = evt.getChild();
            if (!(child instanceof UIResource)) {
                int index = model.indexOfComponent(child);
                fireIntervalAdded(this, index, index);
            }
        }

        public void componentRemoved(ContainerEvent evt) {
            Component child = evt.getChild();
            if (!(child instanceof UIResource)) {
                // FIXME - This is not accurate. We should have the index which
                // changed.
                fireIntervalRemoved(this, 0, 0);
            }
        }

        public void stateChanged(ChangeEvent e) {
                fireContentsChanged(this, 0, model.getTabCount()-1);
        }
    }

    private class FocusHandler implements FocusListener {

        public void focusGained(FocusEvent e) {
            repaintTab(tabPane.getSelectedIndex());
        }

        public void focusLost(FocusEvent e) {
            repaintTab(tabPane.getSelectedIndex());
        }
    }

    /**
     * Repaints the specified tab.
     */
    private void repaintTab(int index) {
        // If we're not valid that means we will shortly be validated and
        // painted, which means we don't have to do anything here.
        if (index >= 0 && index < tabPane.getTabCount()) {
            tabPane.repaint(getTabBounds(tabPane, index));
        }
    }
}
