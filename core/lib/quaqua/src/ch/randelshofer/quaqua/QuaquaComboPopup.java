/*
 * @(#)QuaquaComboPopup.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.io.Serializable;
import java.beans.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import static java.lang.Math.*;

/**
 * QuaquaComboPopup.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaComboPopup.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaComboPopup extends BasicComboPopup {

    private QuaquaComboBoxUI qqui;
    private Handler handler;
    private Component lastFocused;
    private JRootPane invokerRootPane;
    private boolean focusTraversalKeysEnabled;
    /** comboCellBorder is used to accommodate the cell in the combo popup. */
    private final static Border comboCellBorder = new EmptyBorder(0, 7, 0, 7);

    public QuaquaComboPopup(JComboBox cBox, QuaquaComboBoxUI qqui) {
        super(cBox);
        this.qqui = qqui;
        updateCellRenderer(qqui.isTableCellEditor());
    }

    /**
     * Implementation of ComboPopup.show().
     */
    @Override
    public void show() {
        // BEGIN FIX for QUAQUA-137: in J2SE6 1.6_026 firePopupMenuWillBecomeVisible 
        // is no longer being called from the firePopupMenuWillBecomeVisible 
        // method within BasicComboPopup.
        comboBox.firePopupMenuWillBecomeVisible();
        // END FIX

        //System.out.println("QuaquaComboPopup@"+hashCode()+".show()");
        setListSelection(comboBox.getSelectedIndex());

        Point location = getPopupLocation();
        show(comboBox, location.x, location.y);
        // Must be done here after the call to show(..), because show does
        // replace the UI.
        setBorder(UIManager.getBorder("ComboBox.popupBorder"));

        // This is required to properly render the selection, when the JComboBox
        // is used as a table cell editor.
        list.repaint();

    }

    /**
     * Implementation of ComboPopup.hide().
     */
    @Override
    public void hide() {
        super.hide();
        //removeListenersAndResetFocus();
    }

    @Override
    protected void firePopupMenuWillBecomeVisible() {
        // BEGIN FIX for QUAQUA-137: in J2SE6 1.6_026 firePopupMenuWillBecomeVisible 
        // is no longer being called from the firePopupMenuWillBecomeVisible 
        // method within BasicComboPopup.
        Object[] listeners = listenerList.getListenerList();
        PopupMenuEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == PopupMenuListener.class) {
                if (e == null) {
                    e = new PopupMenuEvent(this);
                }
                ((PopupMenuListener) listeners[i + 1]).popupMenuWillBecomeVisible(e);
            }
        }
        // comboBox.firePopupMenuWillBecomeVisible() is called from BasicComboPopup.show() method
        // to let the user change the popup menu from the PopupMenuListener.popupMenuWillBecomeVisible()
        // END FIX
    }

    @Override
    public void setVisible(boolean newValue) {
        super.setVisible(newValue);
        if (newValue) {
            if (!comboBox.isEditable() && comboBox != KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()) {
                //
                // remember current focus owner
                lastFocused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                // request focus on root pane and install keybindings
                // used for menu navigation
                invokerRootPane = SwingUtilities.getRootPane(comboBox);
                if (invokerRootPane != null) {
                    //invokerRootPane.addFocusListener(rootPaneFocusListener);
                    invokerRootPane.requestFocus(true);
                    invokerRootPane.addKeyListener(getHandler());
                    focusTraversalKeysEnabled = invokerRootPane.getFocusTraversalKeysEnabled();
                    invokerRootPane.setFocusTraversalKeysEnabled(false);

                    /* InputMap menuInputMap = comboBox.getInputMap(popup, invokerRootPane);
                    addUIInputMap(invokerRootPane, menuInputMap);
                    addUIActionMap(invokerRootPane, menuActionMap);*/
                }
            }
        } else {
            if (lastFocused != null) {
                if (!lastFocused.requestFocusInWindow()) {
                    // Workarounr for 4810575.
                    // If lastFocused is not in currently focused window
                    // requestFocusInWindow will fail. In this case we must
                    // request focus by requestFocus() if it was not
                    // transferred from our popup.
                    Window cfw = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
                    if (cfw != null
                            && "###focusableSwingPopup###".equals(cfw.getName())) {
                        lastFocused.requestFocus();
                    }

                }
                lastFocused = null;
            }
            if (invokerRootPane != null) {
                invokerRootPane.removeKeyListener(getHandler());
                invokerRootPane.setFocusTraversalKeysEnabled(focusTraversalKeysEnabled);
                //removeUIInputMap(invokerRootPane, menuInputMap);
                //removeUIActionMap(invokerRootPane, menuActionMap);
                invokerRootPane = null;
            }
            // receivedKeyPressed = false;
        }
    }

    private void updateCellRenderer(boolean isTableCellEditor) {
        list.setCellRenderer(
                new QuaquaComboBoxCellRenderer(
                comboBox.getRenderer(), isTableCellEditor, comboBox.isEditable()));
    }

    private int getMaximumRowCount() {
        return (isEditable() || isTableCellEditor()) ? comboBox.getMaximumRowCount() : 100;
    }

    /**
     * Calculates the upper left location of the Popup.
     */
    private Point getPopupLocation() {
        Dimension popupSize = comboBox.getSize();
        Insets insets = getInsets();

        // reduce the width of the scrollpane by the insets so that the popup
        // is the same width as the combo box.
        popupSize.setSize(popupSize.width - (insets.right + insets.left),
                getPopupHeightForRowCount(getMaximumRowCount()));
        Rectangle popupBounds = computePopupBounds(0, comboBox.getBounds().height,
                popupSize.width, popupSize.height);
        Dimension scrollSize = popupBounds.getSize();
        Point popupLocation = popupBounds.getLocation();

        scroller.setMaximumSize(scrollSize);
        scroller.setPreferredSize(scrollSize);
        scroller.setMinimumSize(scrollSize);

        list.revalidate();

        return popupLocation;
    }

    /**
     * Sets the list selection index to the selectedIndex. This 
     * method is used to synchronize the list selection with the 
     * combo box selection.
     * 
     * @param selectedIndex the index to set the list
     */
    private void setListSelection(int selectedIndex) {
        if (selectedIndex == -1) {
            list.clearSelection();
        } else {
            list.setSelectedIndex(selectedIndex);
            list.ensureIndexIsVisible(selectedIndex);
        }
    }

    /**
     * Calculate the placement and size of the popup portion of the combo box based
     * on the combo box location and the enclosing screen bounds. If
     * no transformations are required, then the returned rectangle will
     * have the same values as the parameters.
     *
     * @param px starting x location
     * @param py starting y location
     * @param pw starting width
     * @param ph starting height
     * @return a rectangle which represents the placement and size of the popup
     */
    @Override
    protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Rectangle screen;
        int listWidth = getList().getPreferredSize().width;
        Insets margin = qqui.getMargin();
        boolean isTableCellEditor = isTableCellEditor();
        boolean hasScrollBars = hasScrollBars();
        boolean isEditable = isEditable();

        if (isTableCellEditor) {
            if (hasScrollBars) {
                pw = Math.max(pw, listWidth + 16);
            } else {
                pw = Math.max(pw, listWidth);
            }
        } else {
            if (hasScrollBars) {
                px += margin.left;
                pw = Math.max(pw - margin.left - margin.right, listWidth + 16);
            } else {
                if (isEditable) {
                    px += margin.left;
                    pw = Math.max(pw - qqui.getArrowWidth() - margin.left, listWidth);
                } else {
                    px += margin.left;
                    pw = Math.max(pw - qqui.getArrowWidth() - margin.left, listWidth);
                }
            }
        }

        // Take extended cell border into account which is used in
        // QuaquaListUI.
        Insets cellBorderInsets = comboCellBorder.getBorderInsets(this);
        pw += cellBorderInsets.left + cellBorderInsets.right;

        // Calculate the desktop dimensions relative to the combo box.
        GraphicsConfiguration gc = comboBox.getGraphicsConfiguration();
        Point p = new Point();
        SwingUtilities.convertPointFromScreen(p, comboBox);
        if (gc != null) {
            // Get the screen insets.
            Insets screenInsets = toolkit.getScreenInsets(gc);
            // Note: We must create a new rectangle here, because method
            // getBounds does not return a copy of a rectangle on J2SE 1.3.
            screen = new Rectangle(gc.getBounds());
            screen.width -= (screenInsets.left + screenInsets.right);
            screen.height -= (screenInsets.top + screenInsets.bottom);
            screen.x += screenInsets.left;
            screen.y += screenInsets.top;
        } else {
            screen = new Rectangle(p, toolkit.getScreenSize());
        }

        if (isDropDown()) {
            if (!isTableCellEditor) {
                if (isEditable) {
                    py -= margin.bottom + 2;
                } else {
                    py -= margin.bottom;
                }
            }
        } else {
            int yOffset;
            if (isTableCellEditor) {
                yOffset = 7;
            } else {
                yOffset = 3 - margin.top;
            }
            int selectedIndex = comboBox.getSelectedIndex();
            if (selectedIndex <= 0) {
                py = -yOffset;
            } else {
                py = -yOffset - list.getCellBounds(0, selectedIndex - 1).height;

            }
        }

        // Create rectangle with screen coordinates, fit it on the screen,
        // convert coordinates back to local coordinate system.
        Rectangle rect = new Rectangle(px - p.x, py - p.y, pw, ph);
        if (screen.contains(rect)) {
            // popup fits into screen bounds => nothing to do.
        } else if (rect.width <= screen.width && rect.height <= screen.height) {
            // popup intersects screen bounds but fits into screen => nudge into screen bounds.
            if (rect.x < screen.x) {
                rect.x = screen.x;
            } else if (rect.x + rect.width > screen.x + screen.width) {
                rect.x = screen.x + screen.width - rect.width;
            }
            if (rect.y < screen.y) {
                rect.y = screen.y;
            } else if (rect.y + rect.height > screen.y + screen.height) {
                rect.y = screen.y + screen.height - rect.height;
            }
        } else {
            // popup intersects is larger than screen bounds => maximize popup area
            if (rect.height > screen.height) {
                rect.y = screen.y;
            }
            if (rect.width > screen.width) {
                rect.x = screen.x;
            }
            rect = screen.intersection(rect);
        }
        rect.x += p.x;
        rect.y += p.y;

        // Add the preferred scroll bar width, if the popup contents does not fit
        // into the available rectangle.
        if (rect.height < ph && rect.x + rect.width < screen.x + screen.width + 16) {
            rect.width += 16;
        }

        return rect;
    }

    private boolean isDropDown() {
        return comboBox.isEditable() || hasScrollBars();
    }

    private boolean hasScrollBars() {
        return comboBox.getModel().getSize() > getMaximumRowCount();
    }

    private boolean isEditable() {
        return comboBox.isEditable();
    }

    private boolean isTableCellEditor() {
        return qqui.isTableCellEditor();
    }

    /**
     * Configures the popup portion of the combo box. This method is called
     * when the UI class is created.
     */
    @Override
    protected void configurePopup() {
        super.configurePopup();
        // FIXME - We need to convert the border into a non-UIResource object.
        // An UIResourceObject will be removed from the popup.
        //setBorder( new CompoundBorder(UIManager.getBorder("PopupMenu.border"), new EmptyBorder(0,0,0,0)));
        setBorder(UIManager.getBorder("ComboBox.popupBorder"));
        setFocusable(true);
    }

    @Override
    protected void configureList() {
        super.configureList();
        list.setBackground(UIManager.getColor("PopupMenu.background"));
        list.setFocusable(true);
        list.setRequestFocusEnabled(true);
        list.putClientProperty("Quaqua.List.style", "comboPopup");
    }

    /**
     * Creates a listener
     * that will watch for mouse-press and release events on the combo box.
     *
     * <strong>Warning:</strong>
     * When overriding this method, make sure to maintain the existing
     * behavior.
     *
     * @return a <code>MouseListener</code> which will be added to
     * the combo box or null
     */
    @Override
    protected MouseListener createMouseListener() {
        return getHandler();
    }

    /**
     * Creates the mouse motion listener which will be added to the combo
     * box.
     *
     * <strong>Warning:</strong>
     * When overriding this method, make sure to maintain the existing
     * behavior.
     *
     * @return a <code>MouseMotionListener</code> which will be added to
     *         the combo box or null
     */
    @Override
    protected MouseMotionListener createMouseMotionListener() {
        return getHandler();
    }

    /**
     * Creates the key listener that will be added to the combo box. If
     * this method returns null then it will not be added to the combo box.
     *
     * @return a <code>KeyListener</code> or null
     */
    @Override
    protected KeyListener createKeyListener() {
        return getHandler();
    }

    /**
     * Creates a list selection listener that watches for selection changes in
     * the popup's list.  If this method returns null then it will not
     * be added to the popup list.
     *
     * @return an instance of a <code>ListSelectionListener</code> or null
     */
    @Override
    protected ListSelectionListener createListSelectionListener() {
        return null;
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
        return null;
    }

    /**
     * Creates a mouse listener that watches for mouse events in
     * the popup's list. If this method returns null then it will
     * not be added to the combo box.
     *
     * @return an instance of a <code>MouseListener</code> or null
     */
    @Override
    protected MouseListener createListMouseListener() {
        return getHandler();
    }

    /**
     * Creates a mouse motion listener that watches for mouse motion
     * events in the popup's list. If this method returns null then it will
     * not be added to the combo box.
     *
     * @return an instance of a <code>MouseMotionListener</code> or null
     */
    @Override
    protected MouseMotionListener createListMouseMotionListener() {
        return getHandler();
    }

    /**
     * Creates a <code>PropertyChangeListener</code> which will be added to
     * the combo box. If this method returns null then it will not
     * be added to the combo box.
     *
     * @return an instance of a <code>PropertyChangeListener</code> or null
     */
    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }

    /**
     * Creates an <code>ItemListener</code> which will be added to the
     * combo box. If this method returns null then it will not
     * be added to the combo box.
     * <p>
     * Subclasses may override this method to return instances of their own
     * ItemEvent handlers.
     *
     * @return an instance of an <code>ItemListener</code> or null
     */
    @Override
    protected ItemListener createItemListener() {
        return getHandler();
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    private class Handler implements ItemListener, MouseListener,
            MouseMotionListener, PropertyChangeListener,
            Serializable, KeyListener {
        //
        // MouseListener
        // NOTE: this is added to both the JList and JComboBox
        //

        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            if (e.getSource() == list) {
                return;
            }
            if (!SwingUtilities.isLeftMouseButton(e) || !comboBox.isEnabled()) {
                return;
            }

            if (comboBox.isEditable()) {
                Component comp = comboBox.getEditor().getEditorComponent();
                if ((!(comp instanceof JComponent)) || ((JComponent) comp).isRequestFocusEnabled()) {
                    comp.requestFocus();
                }
            } else if (comboBox.isRequestFocusEnabled()) {
                comboBox.requestFocus();
            }
            togglePopup();
        }

        public void mouseReleased(MouseEvent e) {
            if (e.getSource() == list) {
                if (list.getModel().getSize() > 0) {
                    // JList mouse listener
                    if (comboBox.getSelectedIndex() != list.getSelectedIndex()) {
                        comboBox.setSelectedIndex(list.getSelectedIndex());
                    } else {
                        comboBox.getEditor().setItem(list.getSelectedValue());
                    }
                }
                comboBox.setPopupVisible(false);
                // workaround for cancelling an edited item (bug 4530953)
                if (comboBox.isEditable() && comboBox.getEditor() != null) {
                    comboBox.configureEditor(comboBox.getEditor(),
                            comboBox.getSelectedItem());
                }
                return;
            }
            // JComboBox mouse listener
            Component source = (Component) e.getSource();
            Dimension size = source.getSize();
            Rectangle bounds = new Rectangle(0, 0, size.width - 1, size.height - 1);
            if (!bounds.contains(e.getPoint())) {
                MouseEvent newEvent = convertMouseEvent(e);
                Point location = newEvent.getPoint();
                Rectangle r = new Rectangle();
                list.computeVisibleRect(r);
                if (r.contains(location)) {
                    if (comboBox.getSelectedIndex() != list.getSelectedIndex()) {
                        comboBox.setSelectedIndex(list.getSelectedIndex());
                    } else {
                        comboBox.getEditor().setItem(list.getSelectedValue());
                    }
                }
                comboBox.setPopupVisible(false);
            }
            hasEntered = false;
            stopAutoScrolling();
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        //
        // MouseMotionListener:
        // NOTE: this is added to both the List and ComboBox
        //
        public void mouseMoved(MouseEvent anEvent) {
            if (anEvent.getSource() == list) {
                Point location = anEvent.getPoint();
                Rectangle r = new Rectangle();
                list.computeVisibleRect(r);
                if (r.contains(location)) {
                    updateListBoxSelectionForEvent(anEvent, false);
                }
            }
        }

        public void mouseDragged(MouseEvent e) {
            if (e.getSource() == list) {
                return;
            }
            if (isVisible()) {
                MouseEvent newEvent = convertMouseEvent(e);
                Rectangle r = new Rectangle();
                list.computeVisibleRect(r);

                if (newEvent.getPoint().y >= r.y && newEvent.getPoint().y <= r.y + r.height - 1) {
                    hasEntered = true;
                    if (isAutoScrolling) {
                        stopAutoScrolling();
                    }
                    Point location = newEvent.getPoint();
                    if (r.contains(location)) {
                        updateListBoxSelectionForEvent(newEvent, false);
                    }
                } else {
                    if (hasEntered) {
                        int directionToScroll = newEvent.getPoint().y < r.y ? SCROLL_UP : SCROLL_DOWN;
                        if (isAutoScrolling && scrollDirection != directionToScroll) {
                            stopAutoScrolling();
                            startAutoScrolling(directionToScroll);
                        } else if (!isAutoScrolling) {
                            startAutoScrolling(directionToScroll);
                        }
                    } else {
                        if (e.getPoint().y < 0) {
                            hasEntered = true;
                            startAutoScrolling(SCROLL_UP);
                        }
                    }
                }
            }
        }

        //
        // PropertyChangeListener
        //
        public void propertyChange(PropertyChangeEvent e) {
            JComboBox comboBox = (JComboBox) e.getSource();
            String propertyName = e.getPropertyName();

            if (propertyName == "model") {
                ComboBoxModel oldModel = (ComboBoxModel) e.getOldValue();
                ComboBoxModel newModel = (ComboBoxModel) e.getNewValue();
                uninstallComboBoxModelListeners(oldModel);
                installComboBoxModelListeners(newModel);

                list.setModel(newModel);

                if (isVisible()) {
                    hide();
                }
            } else if (propertyName == "renderer") {
                list.setCellRenderer(comboBox.getRenderer());
                if (isVisible()) {
                    hide();
                }
            } else if (propertyName == "componentOrientation") {
                // Pass along the new component orientation
                // to the list and the scroller

                ComponentOrientation o = (ComponentOrientation) e.getNewValue();

                JList list = getList();
                if (list != null && list.getComponentOrientation() != o) {
                    list.setComponentOrientation(o);
                }

                if (scroller != null && scroller.getComponentOrientation() != o) {
                    scroller.setComponentOrientation(o);
                }

                if (o != getComponentOrientation()) {
                    setComponentOrientation(o);
                }
            } else if (propertyName == "lightWeightPopupEnabled") {
                setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
            } else if (propertyName.equals("renderer")
                    || propertyName.equals(QuaquaComboBoxUI.IS_TABLE_CELL_EDITOR)) {
                updateCellRenderer(e.getNewValue().equals(Boolean.TRUE));
            } else if (propertyName.equals("JComboBox.lightweightKeyboardNavigation")) {
                // In Java 1.3 we have to use this property to guess whether we
                // are a table cell editor or not.
                updateCellRenderer(e.getNewValue() != null && e.getNewValue().equals("Lightweight"));
            } else if (propertyName.equals("editable")) {
                updateCellRenderer(isTableCellEditor());
            }
        }

        //
        // ItemListener
        //
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                JComboBox comboBox = (JComboBox) e.getSource();
                setListSelection(comboBox.getSelectedIndex());
            }
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            processKeyEvent(e, true);

            // Forward key pressed events to QuaquaComboBoxUI when the
            // popup us showing, but the combo box is not focused.
            if (!e.isConsumed() && !comboBox.isEditable() && !comboBox.isFocusOwner()) {
                QuaquaComboBoxUI ui = (QuaquaComboBoxUI) comboBox.getUI();
                ui.getKeyListener().keyPressed(e);
            }
        }

        public void keyReleased(KeyEvent e) {
            processKeyEvent(e, false);
        }

        private boolean processKeyEvent(KeyEvent e, boolean pressed) {
            // Forward key pressed events to QuaquaComboBoxUI when the
            // popup us showing, but the combo box is not focused.
            if (!e.isConsumed() && !comboBox.isEditable() && !comboBox.isFocusOwner()) {
                int condition = WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
                // Get the KeyStroke
                KeyStroke ks;

                if (e.getID() == KeyEvent.KEY_TYPED) {
                    ks = KeyStroke.getKeyStroke(e.getKeyChar());
                } else {
                    ks = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers(),
                            (pressed ? false : true));
                }
                InputMap map = comboBox.getInputMap(condition/*, false*/);
                ActionMap am = comboBox.getActionMap(/*false*/);

                // System.out.println("QuaquaComboPopup@"+QuaquaComboPopup.this.hashCode()+".processKeyEvent " + ks);
                if (map != null && am != null && isEnabled()) {

                    Object binding = map.get(ks);
                    // System.out.println("  binding: " + binding);
                    Action action = (binding == null) ? null : am.get(binding);
                    if (action != null) {
                        e.consume();
                        return SwingUtilities.notifyAction(action, ks, e, comboBox,
                                e.getModifiers());
                    }
                }
            }
            return false;
        }
    }
    //
    // end Event Listeners
    //=================================================================
}
