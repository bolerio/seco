/*
 * @(#)QuaquaListUI.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.color.InactivatableColorUIResource;
import ch.randelshofer.quaqua.color.PaintableColor;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.event.*;
import java.lang.reflect.*;

/**
 * QuaquaListUI for Java 1.4.
 * 
 * @author Werner Randelshofer
 * @version $Id: QuaquaListUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaListUI extends BasicListUI {

    private boolean isStriped = false;
    private boolean isComboPopup = false;
    /**
     * This variable has the value of JList.VERTICAL, if Java 1.4 or higher is
     * present. In older Java VM's it has value 0.
     */
    private final static int VERTICAL;

    static {
        int value = 0;
        try {
            value = JList.class.getField("VERTICAL").getInt(null);
        } catch (Exception e) {
        }
        VERTICAL = value;
    }
    private final static Method getLayoutOrientation;

    static {
        Method value = null;
        try {
            value = JList.class.getMethod("getLayoutOrientation", (Class[]) null);
        } catch (Exception e) {
        }
        getLayoutOrientation = value;
    }

    /** Creates a new instance. */
    public QuaquaListUI() {
    }

    private Color getAlternateColor(int modulo) {
        if (modulo == 0) {
            return UIManager.getColor("List.alternateBackground.0");
        } else {
            return UIManager.getColor("List.alternateBackground.1");
        }
    }

    private void updateStriped() {
        Object value = list.getClientProperty("Quaqua.List.style");
        isStriped = value != null && value.equals("striped") && getLayoutOrientation() == VERTICAL;
    }

    private int getLayoutOrientation() {
        if (getLayoutOrientation != null) {
            try {
                return ((Integer) getLayoutOrientation.invoke(list, (Object[]) null)).intValue();
            } catch (Exception e) {
            }
        }
        return VERTICAL;
    }

    public void paintStripes(Graphics g, JComponent c) {
        if (isStriped && list.getModel() != null //&& list.getLayoutOrientation() == JList.VERTICAL
                ) {
            // Now check if we need to paint some stripes
            Dimension vs = c.getSize();
            Dimension ts = list.getSize();


            Point p = list.getLocation();
            int rh = list.getFixedCellHeight();
            int n = list.getModel().getSize();
            if (rh <= 0) {
                rh = (n == 0) ? 12 : getCellBounds(list, 0, 0).height;
            }
            int row = Math.abs(p.y / rh);
            int th = n * rh - row * rh;

            // Fill the background of the list with stripe color 1
            g.setColor(getAlternateColor(1));
            g.fillRect(0, 0, ts.width, ts.height);

            // Fill rectangles with stripe color 0
            g.setColor(getAlternateColor(0));

            // Paint empty rows at the right to fill the viewport
            if (ts.width < vs.width) {
                int y = p.y + row * rh;
                while (y < th) {
                    if (row % 2 == 0) {
                        g.fillRect(0, y, vs.width, rh);
                    }
                    y += rh;
                    row++;
                }
            }


            // Paint empty rows at the bottom to fill the viewport
            if (th < vs.height) {
                row = n;
                int y = th;
                while (y < vs.height) {
                    if (row % 2 == 0) {
                        g.fillRect(0, y, vs.width, rh);
                    }
                    y += rh;
                    row++;
                }
            }
        }
    }
    /**
     * The layout orientation of the list.
     */
    private int layoutOrientation;

    /**
     * Paint one List cell: compute the relevant state, get the "rubber stamp"
     * cell renderer component, and then use the CellRendererPane to paint it.
     * Subclasses may want to override this method rather than paint().
     *
     * @see #paint
     */
    @Override
    protected void paintCell(
            Graphics g,
            int row,
            Rectangle rowBounds,
            ListCellRenderer cellRenderer,
            ListModel dataModel,
            ListSelectionModel selModel,
            int leadIndex) {
        Object value = dataModel.getElementAt(row);
        boolean isEnabled = list.isEnabled();
        boolean isFocused = isEnabled
                && QuaquaUtilities.isFocused(list);
        boolean cellHasFocus = isFocused && (row == leadIndex);
        boolean isSelected = selModel.isSelectedIndex(row);

        Component rendererComponent =
                cellRenderer.getListCellRendererComponent(list, value, row, isSelected, cellHasFocus);

        int cx = rowBounds.x;
        int cy = rowBounds.y;
        int cw = rowBounds.width;
        int ch = rowBounds.height;

        if (list.isSelectedIndex(row)) {
            ((Graphics2D) g).setPaint(PaintableColor.getPaint(UIManager.getColor(isComboPopup ? "ComboBox.selectionBackground" : "List.selectionBackground"), rendererComponent, cx, cy, cw, ch));
            g.fillRect(cx, cy, cw, ch);
        } else {
            if (isStriped) {
                //rendererComponent.setBackground(getAlternateColor(row % 2));
                g.setColor(getAlternateColor(row % 2));
                g.fillRect(cx, cy, cw, ch);
            }
        }

        if (isComboPopup) {
            cx += 7;
            cw -= 14;
        }
        rendererPane.paintComponent(g, rendererComponent, list, cx, cy, cw, ch, true);
    }

    /**
     * Paint the rows that intersect the Graphics objects clipRect.  This
     * method calls paintCell as necessary.  Subclasses
     * may want to override these methods.
     *
     * @see #paintCell
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        paintStripes(g, c);

        boolean isFocused = QuaquaUtilities.isFocused(c);
        Object value = c.getClientProperty("Quaqua.List.style");
        isComboPopup = value != null && value.equals("comboPopup");
        Color selectionBackground = UIManager.getColor(isComboPopup ? "ComboBox.selectionBackground" : "List.selectionBackground");
        Color selectionForeground = UIManager.getColor(isComboPopup ? "ComboBox.selectionForeground" : "List.selectionForeground");
        if (selectionBackground instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) selectionBackground).setActive(isFocused);
        }
        if (selectionForeground instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) selectionForeground).setActive(isFocused);
        }
        // We need to mess with tree selection colors here, in case someone
        // is nesting a TreeCellRenderer into a ListCellRenderer.
        Color treeSelectionBackground = UIManager.getColor("Tree.selectionBackground");
        Color treeSelectionForeground = UIManager.getColor("Tree.selectionForeground");
        if (treeSelectionBackground instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) treeSelectionBackground).setActive(isFocused);
        }

        if (treeSelectionForeground instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) treeSelectionForeground).setActive(isFocused);
        }

        super.paint(g, c);
        if (selectionBackground instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) selectionBackground).setActive(true);
        }
        if (selectionForeground instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) selectionForeground).setActive(true);
        }
        if (treeSelectionBackground instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) treeSelectionBackground).setActive(true);
        }

        if (treeSelectionForeground instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) treeSelectionForeground).setActive(true);
        }
    }

    /**
     * Initialize JList properties, e.g. font, foreground, and background,
     * and add the CellRendererPane.  The font, foreground, and background
     * properties are only set if their current value is either null
     * or a UIResource, other properties are set if the current
     * value is null.
     *
     * @see #uninstallDefaults
     * @see #installUI
     * @see CellRendererPane
     */
    @Override
    protected void installDefaults() {
        super.installDefaults();
        updateStriped();
    }
    /*
    protected void installListeners() {
    list.addMouseListener(defaultDragRecognizer);
    list.addMouseMotionListener(defaultDragRecognizer);
    super.installListeners();

    // Remove the dreaded BasicDragGestureRecognizer from the list
    boolean removalSuccessful = false;
    MouseListener[] ml = list.getMouseListeners();
    for (int i = 0; i < ml.length; i++) {
    if (ml[i].getClass().getName().equals("javax.swing.plaf.basic.BasicListUI$ListDragGestureRecognizer")) {
    list.removeMouseListener(ml[i]);
    removalSuccessful = true;
    }
    }
    MouseMotionListener[] mml = list.getMouseMotionListeners();
    for (int i = 0; i < mml.length; i++) {
    if (mml[i].getClass().getName().equals("javax.swing.plaf.basic.BasicListUI$ListDragGestureRecognizer")) {
    list.removeMouseMotionListener(mml[i]);
    }
    }
    if (!removalSuccessful) {
    list.removeMouseListener(defaultDragRecognizer);
    list.removeMouseMotionListener(defaultDragRecognizer);
    }
    }

    protected void uninstallListeners() {
    super.uninstallListeners();
    list.removeMouseListener(defaultDragRecognizer);
    list.removeMouseMotionListener(defaultDragRecognizer);
    }
     */

    /**
     * Returns a new instance of QuaquaListUI.  QuaquaListUI delegates are
     * allocated one per JList.
     * 
     * @return A new ListUI implementation for the Windows look and feel.
     */
    public static ComponentUI createUI(JComponent list) {
        return new QuaquaListUI();
    }

    /**
     * Mouse input, and focus handling for JList.  An instance of this
     * class is added to the appropriate java.awt.Component lists
     * at installUI() time.  Note keyboard input is handled with JComponent
     * KeyboardActions, see installKeyboardActions().
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans<sup><font size="-2">TM</font></sup>
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     *
     * @see #createMouseInputListener
     * @see #installKeyboardActions
     * @see #installUI
     */
    public class MouseInputHandler implements MouseInputListener {

        private boolean mouseReleaseDeselects;
        private boolean mouseDragSelects;
        private MouseEvent armedEvent;
        private int dragThreshold;

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            int index = locationToIndex(list, e.getPoint());

            // Don't change selection, if user selected below of a list cell
            if (index != -1) {
                Rectangle cellBounds = list.getCellBounds(index, index);
                if (e.getY() > cellBounds.getY() + cellBounds.getHeight()) {
                    index = -1;
                }
            }
            armedEvent = e;
            dragThreshold = QuaquaUtilities.getDragThreshold();

            // Note: Some applications depend on selection changes only occuring
            // on focused components. Maybe we must not do any changes to the
            // selection changes at all, when the compnent is not focused?
            if (list.isEnabled()&&list.isRequestFocusEnabled())
            list.requestFocusInWindow();

            mouseDragSelects = false;
            mouseReleaseDeselects = false;
            if (index != -1) {
                if (!list.isEnabled() || list.isSelectedIndex(index) && e.isPopupTrigger()) {
                    // Do not change the selection, if the list is disabled
                    // or the item is already
                    // selected, and the user triggers the popup menu.
                } else {
                    int anchorIndex = list.getAnchorSelectionIndex();

                    if ((e.getModifiersEx() & (MouseEvent.META_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK)) == MouseEvent.META_DOWN_MASK) {
                        if (list.isSelectedIndex(index)) {
                            list.removeSelectionInterval(index, index);
                        } else {
                            list.addSelectionInterval(index, index);
                            mouseDragSelects = true;
                        }
                    } else if ((e.getModifiersEx() & (MouseEvent.SHIFT_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK)) == MouseEvent.SHIFT_DOWN_MASK
                            && anchorIndex != -1) {
                        list.setSelectionInterval(anchorIndex, index);
                        mouseDragSelects = true;
                    } else if ((e.getModifiersEx() & (MouseEvent.SHIFT_DOWN_MASK | MouseEvent.META_DOWN_MASK)) == 0) {
                        if (list.isSelectedIndex(index)) {
                            mouseReleaseDeselects = list.isFocusOwner();
                        } else {
                            list.setSelectionInterval(index, index);
                            mouseDragSelects = true;
                        }
                        list.getSelectionModel().setAnchorSelectionIndex(index);
                    }
                }
            }
            list.getSelectionModel().setValueIsAdjusting(mouseDragSelects);
        }

        public void mouseDragged(MouseEvent e) {
            mouseReleaseDeselects = false;

            // Abort if mouseDragged event is received without prior
            // mousePressed event.
            if (armedEvent == null) {
                return;
            }

            int dx = Math.abs(e.getX() - armedEvent.getX());
            int dy = Math.abs(e.getY() - armedEvent.getY());
            if (Math.sqrt(dx * dx + dy * dy) > dragThreshold) {
                if (mouseDragSelects) {
                    int index = locationToIndex(list, e.getPoint());
                    if (index != -1) {
                        Rectangle cellBounds = getCellBounds(list, index, index);
                        list.scrollRectToVisible(cellBounds);
                        int anchorIndex = list.getAnchorSelectionIndex();
                        list.setSelectionInterval(anchorIndex, index);
                    }
                } else {
                    if (list.getDragEnabled()) {
                        TransferHandler th = list.getTransferHandler();
                        int action = QuaquaUtilities.mapDragOperationFromModifiers(e, th);
                        if (action != TransferHandler.NONE) {
                            /* notify the BeforeDrag instance * /
                            if (bd != null) {
                            bd.dragStarting(dndArmedEvent);
                            }*/
                            th.exportAsDrag(list, armedEvent, action);
                            //clearState();
                        }
                    }
                }
            }
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
            mouseDragSelects = false;
            if (mouseReleaseDeselects) {
                int index = locationToIndex(list, e.getPoint());
                list.setSelectionInterval(index, index);
            }
            list.getSelectionModel().setValueIsAdjusting(false);
            if (list.isEnabled()&&list.isRequestFocusEnabled()) {
                list.requestFocus();
            }
        }
    }

    /**
     * Creates a delegate that implements MouseInputListener.
     * The delegate is added to the corresponding java.awt.Component listener
     * lists at installUI() time. Subclasses can override this method to return
     * a custom MouseInputListener, e.g.
     * <pre>
     * class MyListUI extends QuaquaListUI {
     *    protected MouseInputListener <b>createMouseInputListener</b>() {
     *        return new MyMouseInputHandler();
     *    }
     *    public class MyMouseInputHandler extends MouseInputHandler {
     *        public void mouseMoved(MouseEvent e) {
     *            // do some extra work when the mouse moves
     *            super.mouseMoved(e);
     *        }
     *    }
     * }
     * </pre>
     * 
     * @see MouseInputHandler
     * @see #installUI
     */
    @Override
    protected MouseInputListener createMouseInputListener() {
        return new MouseInputHandler();
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTableUI.
     */
    public class FocusHandler implements FocusListener {

        protected void repaintCellFocus() {
            int leadIndex = list.getLeadSelectionIndex();
            if (leadIndex != -1) {
                Rectangle r = getCellBounds(list, leadIndex, leadIndex);
                if (r != null) {
                    list.repaint(r.x, r.y, r.width, r.height);
                }
            }
        }

        /* The focusGained() focusLost() methods run when the JList
         * focus changes.
         */
        public void focusGained(FocusEvent event) {
            // hasFocus = true;
            repaintCellFocus();
        }

        public void focusLost(FocusEvent event) {
            // hasFocus = false;
            repaintCellFocus();
        }
    }

    @Override
    protected FocusListener createFocusListener() {
        return new FocusHandler();
    }

    @Override
    protected ListDataListener createListDataListener() {
        return new ListDataHandler();
    }

    /**
     * The PropertyChangeListener that's added to the JList at
     * installUI time.  When the value of a JList property that
     * affects layout changes, we set a bit in updateLayoutStateNeeded.
     * If the JLists model changes we additionally remove our listeners
     * from the old model.  Likewise for the JList selectionModel.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans<sup><font size="-2">TM</font></sup>
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     *
     * @see #maybeUpdateLayoutState
     * @see #createPropertyChangeListener
     * @see #installUI
     */
    public class PropertyChangeHandler extends BasicListUI.PropertyChangeHandler {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();

            if (name.equals("Quaqua.List.style")) {
                updateStriped();
            } else if ("layoutOrientation".equals(name)) {
                layoutOrientation = ((Integer) e.getNewValue()).intValue();
                updateStriped();
            } else if (name.equals("JComponent.sizeVariant")) {
                QuaquaUtilities.applySizeVariant(list);
            }
            super.propertyChange(e);
        }
    }

    /**
     * Creates an instance of PropertyChangeHandler that's added to
     * the JList by installUI().  Subclasses can override this method
     * to return a custom PropertyChangeListener, e.g.
     * <pre>
     * class MyListUI extends QuaquaListUI {
     *    protected PropertyChangeListener <b>createPropertyChangeListener</b>() {
     *        return new MyPropertyChangeListener();
     *    }
     *    public class MyPropertyChangeListener extends PropertyChangeHandler {
     *        public void propertyChange(PropertyChangeEvent e) {
     *            if (e.getPropertyName().equals("model")) {
     *                // do some extra work when the model changes
     *            }
     *            super.propertyChange(e);
     *        }
     *    }
     * }
     * </pre>
     * 
     * @see PropertyChangeListener
     * @see #installUI
     */
    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return new PropertyChangeHandler();
    }

    protected class ListDataHandler implements ListDataListener {
        //
        // ListDataListener
        //

        public void intervalAdded(ListDataEvent e) {
            updateLayoutStateNeeded = modelChanged;

            int minIndex = Math.min(e.getIndex0(), e.getIndex1());
            int maxIndex = Math.max(e.getIndex0(), e.getIndex1());

            /* Sync the SelectionModel with the DataModel.
             */

            ListSelectionModel sm = list.getSelectionModel();
            if (sm != null && sm.getMinSelectionIndex() != -1) {
                sm.insertIndexInterval(minIndex, maxIndex - minIndex + 1, true);
            }

            /* Repaint the entire list, from the origin of
             * the first added cell, to the bottom of the
             * component.
             */
            redrawList();
        }

        public void intervalRemoved(ListDataEvent e) {
            updateLayoutStateNeeded = modelChanged;

            /* Sync the SelectionModel with the DataModel.
             */

            ListSelectionModel sm = list.getSelectionModel();
            if (sm != null) {
                sm.removeIndexInterval(e.getIndex0(), e.getIndex1());
            }

            /* Repaint the entire list, from the origin of
             * the first removed cell, to the bottom of the
             * component.
             */

            redrawList();
        }

        public void contentsChanged(ListDataEvent e) {
            updateLayoutStateNeeded = modelChanged;

            if (list.getFixedCellHeight() == -1) {
                redrawList();
            } else {
                Rectangle bounds = list.getCellBounds(e.getIndex0(), e.getIndex1());
                if (bounds == null) {
                    redrawList();
                } else {
                    list.repaint(bounds);
                }
            }
        }
    }

    private void redrawList() {
        list.revalidate();
        list.repaint();
    }
    /*
    private static final ListDragGestureRecognizer defaultDragRecognizer =
    new ListDragGestureRecognizer();

    /**
     * Drag gesture recognizer for JList components
     * /
    static class ListDragGestureRecognizer extends QuaquaDragGestureRecognizer {

    /**
     * Determines if the following are true:
     * <ul>
     * <li>the press event is located over a selection
     * <li>the dragEnabled property is true
     * <li>A TranferHandler is installed
     * </ul>
     * <p>
     * This is implemented to perform the superclass behavior
     * followed by a check if the dragEnabled
     * property is set and if the location picked is selected.
     * /
    protected boolean isDragPossible(MouseEvent e) {
    if (super.isDragPossible(e)) {
    JList list = (JList) this.getComponent(e);
    if (list.getDragEnabled()) {
    QuaquaListUI ui = (QuaquaListUI) list.getUI();
    int row = ui.locationToIndex(list, e.getPoint());
    if ((row != -1) && list.isSelectedIndex(row)) {
    return true;
    /*		    } else if (row != -1 && list.getCellBounds(row, row).contains(e.getPoint())) {
    return true;         * /
    }
    }
    }
    return false;
    }
    }*/
}
