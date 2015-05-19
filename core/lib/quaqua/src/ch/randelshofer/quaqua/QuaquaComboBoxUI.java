/*
 * @(#)QuaquaComboBoxUI.java 
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.util.Debug;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.beans.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Quaqua UI for JComboBox.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaComboBoxUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaComboBoxUI extends BasicComboBoxUI implements VisuallyLayoutable {
    //private HierarchyListener hierarchyListener;
    //MetalComboBoxUI
    // Control the selection behavior of the JComboBox when it is used
    // in the JTable DefaultCellEditor.

    private boolean isTableCellEditor = false;
    public static final String IS_TABLE_CELL_EDITOR = "JComboBox.isTableCellEditor";
    private final static Border tableCellEditorBorder = new EmptyBorder(0, 2, 0, 0);
    static final StringBuffer HIDE_POPUP_KEY = new StringBuffer("HidePopupKey");
    /**
     * Optional: if specified, these insets act as padding around the cell
     * renderer when laying out and painting the "selected" item in the 
     * combo box. BasicComboBoxUI uses a single combo box renderer for rendering
     * both the main combo box item and also all the items in the dropdown
     * for the combo box. padding allows you to specify addition insets in
     * addition to those specified by the cell renderer.
     */
    private Insets padding;
// Flag for calculating the display size
    private boolean isDisplaySizeDirty = true;
    // Cached the size that the display needs to render the largest item
    private Dimension cachedDisplaySize = new Dimension(0, 0);
    private boolean sameBaseline;
    private QuaquaComboBoxUIHandler handler;
    /**
     * This is tricky, this variables is needed for DefaultKeySelectionManager
     * to take into account time factor.
     */
    private long lastTime = 0L;
    private long time = 0L;

    /**
     * Preferred spacing between combo boxes and other components.
     * /
     * private final static Insets regularSpacing = new Insets(12,12,12,12);
     * private final static Insets smallSpacing = new Insets(10,10,10,10);
     * private final static Insets miniSpacing = new Insets(8,8,8,8);
     */
    public static ComponentUI createUI(JComponent c) {
        return new QuaquaComboBoxUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        // Is this combo box a cell editor?
        Boolean value = (Boolean) c.getClientProperty(IS_TABLE_CELL_EDITOR);
        if (value == null) {
            value = (Boolean) c.getClientProperty("JComboBox.lightweightKeyboardNavigation");
        }
        setTableCellEditor(value != null && value.equals(Boolean.TRUE));

        // Note: we need to invoke c.setOpaque explicitly, installProperty does
        //       not seem to work.
        //LookAndFeel.installProperty(c, "opaque", UIManager.get("ComboBox.opaque"));
        c.setOpaque(UIManager.getBoolean("ComboBox.opaque"));

        comboBox.setRequestFocusEnabled(UIManager.getBoolean("ComboBox.requestFocusEnabled"));

        // We can't set this property because it breaks the behavior of editable
        // combo boxes.
        comboBox.setFocusable(comboBox.isEditable() || UIManager.getBoolean("ComboBox.focusable"));
        //
        QuaquaUtilities.applySizeVariant(comboBox);
        if (arrowButton != null) {
            arrowButton.putClientProperty("JComponent.sizeVariant", comboBox.getClientProperty("JComponent.sizeVariant"));
        }

    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        comboBox.setMaximumRowCount(UIManager.getInt("ComboBox.maximumRowCount"));
        padding = UIManager.getInsets("ComboBox.padding");
    }

    /**
     * Create and install the listeners for the combo box and its model.
     * This method is called when the UI is installed.
     */
    @Override
    protected void installListeners() {
        if ((itemListener = createItemListener()) != null) {
            comboBox.addItemListener(itemListener);
        }
        if ((propertyChangeListener = createPropertyChangeListener()) != null) {
            comboBox.addPropertyChangeListener(propertyChangeListener);
        }
        if ((keyListener = createKeyListener()) != null) {
            comboBox.addKeyListener(keyListener);
        }
        if ((focusListener = createFocusListener()) != null) {
            comboBox.addFocusListener(focusListener);
        }
        if ((popupMouseListener = popup.getMouseListener()) != null) {
            comboBox.addMouseListener(popupMouseListener);
        }
        if ((popupMouseMotionListener = popup.getMouseMotionListener()) != null) {
            comboBox.addMouseMotionListener(popupMouseMotionListener);
        }
        if ((popupKeyListener = popup.getKeyListener()) != null) {
            comboBox.addKeyListener(popupKeyListener);
        }
        /*
        if ((hierarchyListener = createHierarchyListener()) != null) {
        comboBox.addHierarchyListener(hierarchyListener);
        }
         */
        if (comboBox.getModel() != null) {
            if ((listDataListener = createListDataListener()) != null) {
                comboBox.getModel().addListDataListener(listDataListener);
            }
        }
    }

    /**
     * Remove the installed listeners from the combo box and its model.
     * The number and types of listeners removed and in this method should be
     * the same that was added in <code>installListeners</code>
     */
    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        /*
        if (hierarchyListener != null) {
        comboBox.removeHierarchyListener(hierarchyListener);
        hierarchyListener = null;
        }*/
    }

    public KeyListener getKeyListener() {
        return keyListener;
    }
    /*
    protected HierarchyListener createHierarchyListener() {
    return new ComponentActivationHandler(comboBox);
    }*/

    boolean isTableCellEditor() {
        return isTableCellEditor;
    }

    @Override
    protected ComboBoxEditor createEditor() {
        return new QuaquaComboBoxEditor.UIResource();
    }

    @Override
    protected ComboPopup createPopup() {
        QuaquaComboPopup p = new QuaquaComboPopup(comboBox, this);
        p.getAccessibleContext().setAccessibleParent(comboBox);
        return p;
    }

    @Override
    protected JButton createArrowButton() {
        JButton button = new QuaquaComboBoxButton(this, comboBox, getArrowIcon(),
                comboBox.isEditable(),
                currentValuePane,
                listBox);
        button.putClientProperty("Quaqua.Component.cellRendererFor", comboBox);
        button.setMargin(new Insets(0, 1, 1, 3));
        return button;
    }
    /* Creates a <code>KeyListener</code> which will be added to the
     * combo box. If this method returns null then it will not be added
     * to the combo box.
     * 
     * @return an instance <code>KeyListener</code> or null
     */

    protected KeyListener createKeyListener() {
        return getHandler();
    }

    /**
     * Creates a <code>FocusListener</code> which will be added to the combo box.
     * If this method returns null then it will not be added to the combo box.
     *
     * @return an instance of a <code>FocusListener</code> or null
     */
    @Override
    protected FocusListener createFocusListener() {
        return getHandler();
    }

    /**
     * Creates a list data listener which will be added to the
     * <code>ComboBoxModel</code>. If this method returns null then
     * it will not be added to the combo box model.
     *
     * @return an instance of a <code>ListDataListener</code> or null
     */
    @Override
    protected ListDataListener createListDataListener() {
        return getHandler();
    }

    @Override
    public PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }

    // Syncronizes the ToolTip text for the components within the combo box to be the 
    // same value as the combo box ToolTip text.
    private void updateToolTipTextForChildren() {
        Component[] children = comboBox.getComponents();
        for (int i = 0; i < children.length; ++i) {
            if (children[i] instanceof JComponent) {
                ((JComponent) children[i]).setToolTipText(comboBox.getToolTipText());
            }
        }
    }

    private void setTableCellEditor(boolean b) {
        isTableCellEditor = b;
        updateTableCellEditor();
    }

    private void updateTableCellEditor() {
        boolean b = isTableCellEditor();
        //comboBox.setOpaque(b);
        if (editor instanceof JComponent) {
            JComponent jeditor = (JComponent) editor;
            jeditor.setBorder(b ? tableCellEditorBorder : UIManager.getBorder("ComboBox.editorBorder"));
        }
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (editor != null
                && UIManager.getBoolean("ComboBox.changeEditorForeground")) {
            editor.setForeground(c.getForeground());
        }
        Debug.paint(g, c, this);
    }

    @Override
    public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
    }

    /**
     * Paints the background of the currently selected item.
     */
    @Override
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
    }

    /**
     * Returns whether or not the supplied keyCode maps to a key that is used for
     * navigation.  This is used for optimizing key input by only passing non-
     * navigation keys to the type-ahead mechanism.  Subclasses should override this
     * if they change the navigation keys.
     */
    @Override
    protected boolean isNavigationKey(int keyCode) {
        return keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN
                || keyCode == KeyEvent.VK_KP_UP || keyCode == KeyEvent.VK_KP_DOWN;
    }

    private boolean isNavigationKey(int keyCode, int modifiers) {
        InputMap inputMap = comboBox.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = KeyStroke.getKeyStroke(keyCode, modifiers);

        if (inputMap != null && inputMap.get(key) != null) {
            return true;
        }
        return false;
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of <FooUI>.
     */
    //
    // Shared Handler, implements all listeners
    //
    private class QuaquaComboBoxUIHandler implements ActionListener, FocusListener,
            KeyListener, LayoutManager,
            ListDataListener, PropertyChangeListener {
        //    
        //
        // PropertyChangeListener
        //

        private void superPropertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (e.getSource() == editor) {
                // If the border of the editor changes then this can effect
                // the size of the editor which can cause the combo's size to
                // become invalid so we need to clear size caches
                if ("border".equals(propertyName)) {
                    isMinimumSizeDirty = true;
                    isDisplaySizeDirty = true;
                    comboBox.revalidate();
                }
            } else {
                JComboBox comboBox = (JComboBox) e.getSource();
                if (propertyName == "model") {
                    ComboBoxModel newModel = (ComboBoxModel) e.getNewValue();
                    ComboBoxModel oldModel = (ComboBoxModel) e.getOldValue();

                    if (oldModel != null && listDataListener != null) {
                        oldModel.removeListDataListener(listDataListener);
                    }

                    if (newModel != null && listDataListener != null) {
                        newModel.addListDataListener(listDataListener);
                    }

                    if (editor != null) {
                        comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
                    }
                    isMinimumSizeDirty = true;
                    isDisplaySizeDirty = true;
                    comboBox.revalidate();
                    comboBox.repaint();
                } else if (propertyName == "editor" && comboBox.isEditable()) {
                    addEditor();
                    comboBox.revalidate();
                } else if (propertyName == "editable") {
                    if (comboBox.isEditable()) {
                        comboBox.setRequestFocusEnabled(false);
                        addEditor();
                    } else {
                        comboBox.setRequestFocusEnabled(true);
                        removeEditor();
                    }

                    updateToolTipTextForChildren();

                    comboBox.revalidate();
                } else if (propertyName == "enabled") {
                    boolean enabled = comboBox.isEnabled();
                    if (editor != null) {
                        editor.setEnabled(enabled);
                    }
                    if (arrowButton != null) {
                        arrowButton.setEnabled(enabled);
                    }
                    comboBox.repaint();
                } else if (propertyName == "focusable") {
                    boolean focusable = comboBox.isFocusable();
                    if (editor != null) {
                        editor.setFocusable(focusable);
                    }
                    if (arrowButton != null) {
                        arrowButton.setFocusable(focusable);
                    }
                    comboBox.repaint();
                } else if (propertyName == "maximumRowCount") {
                    if (isPopupVisible(comboBox)) {
                        setPopupVisible(comboBox, false);
                        setPopupVisible(comboBox, true);
                    }
                } else if (propertyName == "font") {
                    listBox.setFont(comboBox.getFont());
                    if (editor != null) {
                        editor.setFont(comboBox.getFont());
                    }
                    isMinimumSizeDirty = true;
                    comboBox.validate();
                } else if (propertyName == JComponent.TOOL_TIP_TEXT_KEY) {
                    updateToolTipTextForChildren();
                } else if (propertyName == QuaquaComboBoxUI.IS_TABLE_CELL_EDITOR) {
                    Boolean inTable = (Boolean) e.getNewValue();
                    isTableCellEditor = inTable.equals(Boolean.TRUE) ? true : false;
                } else if (propertyName == "prototypeDisplayValue") {
                    isMinimumSizeDirty = true;
                    isDisplaySizeDirty = true;
                    comboBox.revalidate();
                } else if (propertyName == "renderer") {
                    isMinimumSizeDirty = true;
                    isDisplaySizeDirty = true;
                    comboBox.revalidate();
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            superPropertyChange(e);
            String name = e.getPropertyName();

            if (e.getSource() == editor) {
                // If the border of the editor changes then this can effect
                // the size of the editor which can cause the combo's size to
                // become invalid so we need to clear size caches
                if (name == null || "border".equals(name)) {
                    isMinimumSizeDirty = true;
                }
            }

            if (name == null) {
            } else if (name.equals("model")//
                    || name.equals("prototypeDisplayValue")//
                    || name.equals("renderer")) {
                isMinimumSizeDirty = true;
            } else if (name.equals("editable")) {
                QuaquaComboBoxButton button = (QuaquaComboBoxButton) arrowButton;
                button.setIconOnly(comboBox.isEditable());
                updateTableCellEditor();

                // FIXME - This may cause mayhem!
                comboBox.setFocusable(comboBox.isEditable() || UIManager.getBoolean("ComboBox.focusable"));

                comboBox.repaint();
            } else if (name.equals("background")) {
                Color color = (Color) e.getNewValue();
                arrowButton.setBackground(color);
            } else if (name.equals("foreground")) {
                Color color = (Color) e.getNewValue();
                arrowButton.setForeground(color);
                listBox.setForeground(color);
            } else if (name.equals(IS_TABLE_CELL_EDITOR)) {
                Boolean inTable = (Boolean) e.getNewValue();
                setTableCellEditor(inTable.equals(Boolean.TRUE) ? true : false);
            } else if (name.equals("JComboBox.lightweightKeyboardNavigation")) {
                // In Java 1.3 we have to use this property to guess whether we
                // are a table cell editor or not.
                setTableCellEditor(e.getNewValue() != null && e.getNewValue().equals("Lightweight"));
            } else if (name.equals("JComponent.sizeVariant")) {
                QuaquaUtilities.applySizeVariant(comboBox);
                arrowButton.putClientProperty("JComponent.sizeVariant", e.getNewValue());
            }
        }
        //
        // KeyListener
        //

        // This listener checks to see if the key event isn't a navigation
        // key.  If it finds a key event that wasn't a navigation key it
        // dispatches it to JComboBox.selectWithKeyChar() so that it can do
        // type-ahead.
        public void keyPressed(KeyEvent e) {
            if (isNavigationKey(e.getKeyCode(), e.getModifiers())) {
                lastTime = 0L;
            } else if (comboBox.isEnabled() && comboBox.getModel().getSize() != 0
                    && isTypeAheadKey(e) && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                time = e.getWhen();
                if (comboBox.selectWithKeyChar(e.getKeyChar())) {
                    e.consume();
                }
            }
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
        }

        private boolean isTypeAheadKey(KeyEvent e) {
            return !e.isAltDown() && !e.isControlDown() && !e.isMetaDown();
        }

        //
        // FocusListener
        //
        // NOTE: The class is added to both the Editor and ComboBox.
        // The combo box listener hides the popup when the focus is lost.
        // It also repaints when focus is gained or lost.
        public void focusGained(FocusEvent e) {
            ComboBoxEditor comboBoxEditor = comboBox.getEditor();

            if ((comboBoxEditor != null)
                    && (e.getSource() == comboBoxEditor.getEditorComponent())) {
                return;
            }
            hasFocus = true;
            comboBox.repaint();

            if (comboBox.isEditable() && editor != null) {
                editor.requestFocus();
            }
        }

        public void focusLost(FocusEvent e) {
            ComboBoxEditor editor = comboBox.getEditor();
            if ((editor != null)
                    && (e.getSource() == editor.getEditorComponent())) {
                Object item = editor.getItem();

                Object selectedItem = comboBox.getSelectedItem();
                if (!e.isTemporary() && item != null
                        && !item.equals((selectedItem == null) ? "" : selectedItem)) {
                    comboBox.actionPerformed(new ActionEvent(editor, 0, "",
                            EventQueue.getMostRecentEventTime(), 0));
                }
            }

            hasFocus = false;
            if (!e.isTemporary()) {
                setPopupVisible(comboBox, false);
            }
            comboBox.repaint();
        }

        //
        // ListDataListener
        //
        // This listener watches for changes in the ComboBoxModel
        public void contentsChanged(ListDataEvent e) {
            if (!(e.getIndex0() == -1 && e.getIndex1() == -1)) {
                isMinimumSizeDirty = true;
                comboBox.revalidate();
            }

            // set the editor with the selected item since this
            // is the event handler for a selected item change.
            if (comboBox.isEditable() && editor != null) {
                comboBox.configureEditor(comboBox.getEditor(),
                        comboBox.getSelectedItem());
            }

            isDisplaySizeDirty = true;
            comboBox.repaint();
        }

        public void intervalAdded(ListDataEvent e) {
            contentsChanged(e);
        }

        public void intervalRemoved(ListDataEvent e) {
            contentsChanged(e);
        }

        //
        // LayoutManager
        //
        // This layout manager handles the 'standard' layout of combo boxes.
        // It puts the arrow button to the right and the editor to the left.
        // If there is no editor it still keeps the arrow button to the right.
        public void addLayoutComponent(String name, Component comp) {
        }

        public void removeLayoutComponent(Component comp) {
        }

        public Dimension preferredLayoutSize(Container parent) {
            return parent.getPreferredSize();
        }

        public Dimension minimumLayoutSize(Container parent) {
            return parent.getMinimumSize();
        }

        //
        // ActionListener
        //
        // Fix for 4515752: Forward the Enter pressed on the
        // editable combo box to the default button 
        // Note: This could depend on event ordering. The first ActionEvent
        // from the editor may be handled by the JComboBox in which case, the
        // enterPressed action will always be invoked.
        public void actionPerformed(ActionEvent evt) {
            Object item = comboBox.getEditor().getItem();
            if (item != null) {
                if (!comboBox.isPopupVisible() && !item.equals(comboBox.getSelectedItem())) {
                    comboBox.setSelectedItem(comboBox.getEditor().getItem());
                }
                ActionMap am = comboBox.getActionMap();
                if (am != null) {
                    Action action = am.get("enterPressed");
                    if (action != null) {
                        action.actionPerformed(new ActionEvent(comboBox, evt.getID(),
                                evt.getActionCommand(),
                                evt.getModifiers()));
                    }
                }
            }
        }

        @Override
        public void layoutContainer(Container parent) {
            layoutComboBox(parent, this);
        }

        public void superLayout(Container parent) {
            JComboBox cb = (JComboBox) parent;
            int width = cb.getWidth();
            int height = cb.getHeight();

            Insets insets = getInsets();
            int buttonSize = height - (insets.top + insets.bottom);

            Rectangle cvb;
            if (arrowButton != null) {
                if (QuaquaUtilities.isLeftToRight(cb)) {
                    // FIXME - This should be 6 minus 2, whereas two needs to be
                    // derived from the TextFieldUI
                    //int plusHeight = (isSmallSizeVariant()) ? 4 : 4;
                    int plusHeight = (isSmall()) ? - 2 : - 2;
                    arrowButton.setBounds(
                            width - getArrowWidth() - insets.right,
                            insets.top /*+ margin.top - 3*/,
                            getArrowWidth(),
                            buttonSize /*- margin.top - margin.bottom*/ + plusHeight);
                } else {
                    arrowButton.setBounds(insets.left, insets.top,
                            getArrowWidth(), buttonSize);
                }
            }
            if (editor != null) {
                cvb = rectangleForCurrentValue();
                editor.setBounds(cvb);
            }
        }
    }

    /**
     * As of Java 2 platform v1.4 this method is no longer used. Do not call or
     * override. All the functionality of this method is in the
     * QuaquaComboBoxPropertyChangeListener.
     *
     * @deprecated As of Java 2 platform v1.4.
     */
    protected void editablePropertyChanged(PropertyChangeEvent e) {
    }

    @Override
    protected LayoutManager createLayoutManager() {
        return getHandler();
    }

    // This is here because of a bug in the compiler.
    // When a protected-inner-class-savvy compiler comes out we
    // should move this into QuaquaComboBoxLayoutManager.
    public void layoutComboBox(Container parent, QuaquaComboBoxUIHandler manager) {
        if (comboBox.isEditable()) {
            manager.superLayout(parent);
        } else {
            if (arrowButton != null) {
                Insets insets = comboBox.getInsets();
                Insets buttonInsets = UIManager.getInsets("ComboBox.buttonInsets");
                if (buttonInsets != null) {
                    insets = new Insets(insets.top + buttonInsets.top,
                            insets.left + buttonInsets.left, insets.bottom + buttonInsets.bottom, insets.right + buttonInsets.right);
                }
                int width = comboBox.getWidth();
                int height = comboBox.getHeight();
                arrowButton.setBounds(insets.left, insets.top,
                        width - (insets.left + insets.right),
                        height - (insets.top + insets.bottom));
            }
        }
    }

    protected Icon getArrowIcon() {
        if (isTableCellEditor()) {
            return UIManager.getIcon("ComboBox.cellEditorPopupIcon");
            /* The following does not work as expected:
            if (comboBox.isEditable()) {
            return UIManager.getIcon("ComboBox.smallDropDownIcon");
            } else {
            return UIManager.getIcon("ComboBox.smallPopupIcon");
            }*/
        } else {
            if (comboBox.isEditable()) {
                switch (QuaquaUtilities.getSizeVariant(comboBox)) {
                    default:
                        return UIManager.getIcon("ComboBox.dropDownIcon");
                    case SMALL:
                        return UIManager.getIcon("ComboBox.smallDropDownIcon");
                    case MINI:
                        return UIManager.getIcon("ComboBox.miniDropDownIcon");
                }
            } else {
                switch (QuaquaUtilities.getSizeVariant(comboBox)) {
                    default:
                        return UIManager.getIcon("ComboBox.popupIcon");
                    case SMALL:
                        return UIManager.getIcon("ComboBox.smallPopupIcon");
                    case MINI:
                        return UIManager.getIcon("ComboBox.miniPopupIcon");
                }
            }
        }
    }

    protected int getArrowWidth() {
        if (isTableCellEditor()) {
            return 7;
        } else {
            if (comboBox.isEditable()) {
                switch (QuaquaUtilities.getSizeVariant(comboBox)) {
                    default:
                        return UIManager.getInt("ComboBox.dropDownWidth");
                    case SMALL:
                        return UIManager.getInt("ComboBox.smallDropDownWidth");
                    case MINI:
                        return UIManager.getInt("ComboBox.miniDropDownWidth");
                }
            } else {
                switch (QuaquaUtilities.getSizeVariant(comboBox)) {
                    default:
                        return UIManager.getInt("ComboBox.popupWidth");
                    case SMALL:
                        return UIManager.getInt("ComboBox.smallPopupWidth");
                    case MINI:
                        return UIManager.getInt("ComboBox.miniPopupWidth");
                }
            }
        }
    }

    /**
     * As of Java 2 platform v1.4 this method is no
     * longer used.
     *
     * @deprecated As of Java 2 platform v1.4.
     */
    protected void removeListeners() {
        if (propertyChangeListener != null) {
            comboBox.removePropertyChangeListener(propertyChangeListener);
        }
    }

    protected boolean isSmall() {
        boolean isSmall = QuaquaUtilities.getSizeVariant(comboBox) == QuaquaUtilities.SizeVariant.SMALL;

        return isSmall;
    }

    /**
     * Returns the area that is reserved for drawing the currently selected item.
     * Note: Changes in this method also require changes in method getMinimumSize.
     */
    @Override
    protected Rectangle rectangleForCurrentValue() {
        return rectangleForCurrentValue(comboBox.getWidth(), comboBox.getHeight());
    }

    /**
     * Returns the area that is reserved for drawing the currently selected item.
     * Note: Changes in this method also require changes in method getMinimumSize.
     */
    protected Rectangle rectangleForCurrentValue(int width, int height) {
        Insets insets = getInsets();
        Insets margin = getMargin();
        if (comboBox.isEditable()) {
            if (!isTableCellEditor()) {
                insets.right -= margin.right;
                /*
                insets.left--;
                insets.top--;
                insets.bottom--;*/
                insets.left -= margin.left - 2;
                insets.top -= margin.top - 2;
                insets.bottom -= margin.bottom - 2;
            }
        } else {

            if (isTableCellEditor()) {
                insets.top -= 1;
            } else {

                // no right-margin because we
                // want no gap between button and renderer!
                switch (QuaquaUtilities.getSizeVariant(comboBox)) {
                    default:
                        insets.left += 6;
                        insets.top += margin.top;
                        insets.left += margin.left;
                        insets.bottom += margin.bottom;
                        break;
                    case SMALL:
                        insets.left += 4;
                        insets.top += margin.top;
                        insets.left += margin.left;
                        insets.bottom += margin.bottom;
                        break;
                    case MINI:
                        insets.left += 3;
                        insets.top += margin.top;
                        insets.left += margin.left;
                        insets.bottom += margin.bottom;
                        break;
                }
            }


        }
        return new Rectangle(
                insets.left,
                insets.top,
                width - getArrowWidth() - insets.right - insets.left,
                height - insets.top - insets.bottom);
    }

    protected Insets getMargin() {
        Insets margin = (Insets) comboBox.getClientProperty("Quaqua.Component.visualMargin");
        if (margin == null) {
            margin = UIManager.getInsets("Component.visualMargin");
        }
        return (margin == null) ? new Insets(0, 0, 0, 0) : (Insets) margin.clone();
    }

    /** 
     * Returns the calculated size of the display area. The display area is the
     * portion of the combo box in which the selected item is displayed. This 
     * method will use the prototype display value if it has been set. 
     * <p>
     * For combo boxes with a non trivial number of items, it is recommended to
     * use a prototype display value to significantly speed up the display 
     * size calculation.
     * 
     * @return the size of the display area calculated from the combo box items
     * @see javax.swing.JComboBox#setPrototypeDisplayValue
     */
    @Override
    protected Dimension getDisplaySize() {
        if (!isDisplaySizeDirty) {
            return new Dimension(cachedDisplaySize);
        }
        Dimension result = new Dimension();

        ListCellRenderer renderer = comboBox.getRenderer();
        if (renderer == null) {
            renderer = new DefaultListCellRenderer();
        }

        sameBaseline = true;

        Object prototypeValue = comboBox.getPrototypeDisplayValue();
        if (prototypeValue != null) {
            // Calculates the dimension based on the prototype value
            result = getSizeForComponent(renderer.getListCellRendererComponent(listBox,
                    prototypeValue,
                    -1, false, false));
        } else {
            // Calculate the dimension by iterating over all the elements in the combo
            // box list.
            ComboBoxModel model = comboBox.getModel();
            int modelSize = model.getSize();
            int baseline = -1;
            Dimension d;

            Component cpn;

            if (modelSize > 0) {
                for (int i = 0; i < modelSize; i++) {
                    // Calculates the maximum height and width based on the largest
                    // element
                    Object value = model.getElementAt(i);
                    Component c = renderer.getListCellRendererComponent(
                            listBox, value, -1, false, false);
                    d = getSizeForComponent(c);
                    if (sameBaseline && value != null
                            && (!(value instanceof String) || !"".equals(value))) {
                        // BEGIN FIX QUAQUA-151 JComponent.getBaseline() is not available in J2SE5.
                        int newBaseline;//=c.getBaseline(d.width, d.height);
                        try {
                            newBaseline = (Integer) Methods.invoke(c, "getBaseline", new Class[]{Integer.TYPE, Integer.TYPE}, new Object[]{d.width, d.height});
                        } catch (NoSuchMethodException ex) {
                            newBaseline = -1;
                        }
                        // END FIX QUAQUA-151

                        if (newBaseline == -1) {
                            sameBaseline = false;
                        } else if (baseline == -1) {
                            baseline = newBaseline;
                        } else if (baseline != newBaseline) {
                            sameBaseline = false;
                        }
                    }
                    result.width = Math.max(result.width, d.width);
                    result.height = Math.max(result.height, d.height);
                }
            } else {
                result = getDefaultSize();
                if (comboBox.isEditable()) {
                    result.width = 100;
                }
            }
        }

        if (comboBox.isEditable()) {
            Dimension d = editor.getPreferredSize();
            result.width = Math.max(result.width, d.width);
            result.height = Math.max(result.height, d.height);
        }

        // calculate in the padding
        if (padding != null) {
            result.width += padding.left + padding.right;
            result.height += padding.top + padding.bottom;
        }

        // Set the cached value
        cachedDisplaySize.setSize(result.width, result.height);
        isDisplaySizeDirty = false;

        return result;
    }

    /**
     * This has been refactored out in hopes that it may be investigated and
     * simplified for the next major release. adding/removing
     * the component to the currentValuePane and changing the font may be 
     * redundant operations.
     */
    private Dimension getSizeForComponent(Component comp) {
        currentValuePane.add(comp);
        comp.setFont(comboBox.getFont());
        Dimension d = comp.getPreferredSize();
        currentValuePane.remove(comp);
        return d;
    }

    /**
     * Note: Changes in this method also require changes in method rectangelForCurrentValue.
     */
    @Override
    public Dimension getMinimumSize(JComponent c) {
        if (!isMinimumSizeDirty) {
            return new Dimension(cachedMinimumSize);
        }

        Dimension size = null;
        if (!comboBox.isEditable()
                && arrowButton != null
                && arrowButton instanceof QuaquaComboBoxButton) {

            Insets buttonInsets;
            switch (QuaquaUtilities.getSizeVariant(comboBox)) {
                default:
                    buttonInsets = UIManager.getInsets("ComboBox.arrowButtonInsets");
                    break;
                case SMALL:
                    buttonInsets = UIManager.getInsets("ComboBox.smallArrowButtonInsets");
                    break;
                case MINI:
                    buttonInsets = UIManager.getInsets("ComboBox.miniArrowButtonInsets");
                    break;
            }
            buttonInsets = (Insets) buttonInsets.clone();
            buttonInsets.right += getArrowWidth();

            Insets insets = getInsets();
            size = getDisplaySize();
            size.width += insets.left + insets.right
                    + buttonInsets.left + buttonInsets.right;
            size.height += insets.top + insets.bottom
                    + buttonInsets.top + buttonInsets.bottom;

        } else if (comboBox.isEditable()
                && arrowButton != null
                && editor != null) {
            Insets buttonInsets;
            Insets insets = comboBox.getInsets();
            Insets margin = getMargin();
            buttonInsets = new Insets(2 - margin.top, 4 - margin.left, 2 - margin.bottom, getArrowWidth());

            // Margin is included in display size, therefore no need to add
            // it to size. We subtract the margin at the right, because we
            // want the text field's focus ring to glow over the right button.
            size = getDisplaySize();
            size.width += insets.left + insets.right
                    + buttonInsets.left + buttonInsets.right;
            size.height += insets.top + insets.bottom
                    + buttonInsets.top + buttonInsets.bottom;


            size.width += getArrowWidth();
        } else {
            size = super.getMinimumSize(c);
            if (size == null) {
                size = new Dimension(0, 0);
            }
        }

        cachedMinimumSize.setSize(size.width, size.height);
        isMinimumSizeDirty = false;
        return new Dimension(cachedMinimumSize);
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        Dimension size = getPreferredSize(c);
        if (size != null && !(c.getParent() instanceof JToolBar)) {
            size.width = Short.MAX_VALUE;
        }
        return size;
    }

    /**
     * Returns the shared listener.
     */
    private QuaquaComboBoxUIHandler getHandler() {
        if (handler == null) {
            handler = new QuaquaComboBoxUIHandler();
        }
        return handler;
    }

    @Override
    public int getBaseline(JComponent c, int width, int height) {
        Rectangle vb = getVisualBounds(c, VisuallyLayoutable.TEXT_BOUNDS, width, height);
        return (vb == null) ? -1 : vb.y + vb.height;
    }

    public Rectangle getVisualBounds(JComponent c, int layoutType, int width, int height) {
        Rectangle bounds = new Rectangle(0, 0, width, height);
        if (layoutType == VisuallyLayoutable.CLIP_BOUNDS) {
            return bounds;
        }

        if (c != comboBox) {
            return null;
        }

        Rectangle buttonRect = new Rectangle();
        Rectangle editorRect = null;

        Insets insets = getInsets();
        Insets margin = getMargin();
        int buttonSize = height - (insets.top + insets.bottom);
        Rectangle cvb;
        if (arrowButton != null) {
            if (QuaquaUtilities.isLeftToRight(comboBox)) {
                int plusHeight = (isSmall()) ? 5 : 4;
                buttonRect.setBounds(
                        width - getArrowWidth() - insets.right,
                        insets.top + margin.top - 2,
                        getArrowWidth(),
                        buttonSize - margin.top - margin.bottom + plusHeight);
            } else {
                buttonRect.setBounds(insets.left, insets.top,
                        getArrowWidth(), buttonSize);
            }
        }
        editorRect = rectangleForCurrentValue(width, height);

        // FIXME we shouldn't hardcode this and determine the real visual
        // bounds of the renderer instead.
        // Subtract 2 from x because of the insets of the renderer
        editorRect.x += 1;
        editorRect.width -= 2;

        switch (layoutType) {
            case VisuallyLayoutable.COMPONENT_BOUNDS:
                if (!isTableCellEditor()) {
                    if (editor != null) {
                        bounds.x += margin.left;
                        bounds.y += margin.top;
                        bounds.width -= margin.left + margin.right;
                        bounds.height -= margin.top + margin.bottom + 1;
                    } else {
                        bounds.x += margin.left;
                        bounds.y += margin.top;
                        bounds.width -= margin.left + margin.right;
                        bounds.height -= margin.top + margin.bottom;
                    }
                }
                break;
            case VisuallyLayoutable.TEXT_BOUNDS:
                Object renderer = (editor == null)
                        ? (Object) comboBox.getRenderer().getListCellRendererComponent(listBox, comboBox.getSelectedItem(), comboBox.getSelectedIndex(), false, comboBox.hasFocus())
                        : (Object) editor;
                if ((renderer instanceof JComponent)
                        && (Methods.invokeGetter(renderer, "getUI", null) instanceof VisuallyLayoutable)) {
                    bounds = ((VisuallyLayoutable) Methods.invokeGetter(renderer, "getUI", null)).getVisualBounds((JComponent) renderer, layoutType, editorRect.width, editorRect.height);
                    bounds.x += editorRect.x;
                    bounds.y += editorRect.y;
                } else {
                    bounds.setBounds(editorRect);
                }
                break;
        }
        return bounds;
    }

    /**
     * This listener hides the popup when the focus is lost.  It also repaints
     * when focus is gained or lost.
     *
     * This public inner class should be treated as protected.
     * Instantiate it only within subclasses of
     * <code>BasicComboBoxUI</code>.
     */
    public class GlowFocusHandler extends BasicComboBoxUI.FocusHandler {

        @Override
        public void focusGained(FocusEvent e) {
            super.focusGained(e);
            glowyRepaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            super.focusLost(e);
            glowyRepaint();
        }

        private void glowyRepaint() {
            if (comboBox.getParent() != null) {
                Rectangle r = comboBox.getBounds();
                r.grow(2, 2);
                comboBox.getParent().repaint(r.x, r.y, r.width, r.height);
            }
        }
    }
}
