/*
 * @(#)QuaquaTableUI.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.color.InactivatableColorUIResource;
import ch.randelshofer.quaqua.util.ViewportPainter;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
 * QuaquaTableUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaTableUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaTableUI extends BasicTableUI
        implements ViewportPainter {

    private PropertyChangeListener propertyChangeListener;
    private ListSelectionListener listSelectionListener;
    private TableColumnModelListener columnModelListener;
    private Handler handler;
    private boolean isStriped = false;

    /** Creates a new instance. */
    public QuaquaTableUI() {
    }

    public static ComponentUI createUI(JComponent c) {
        return new QuaquaTableUI();
    }

    /**
     * Creates the key listener for handling keyboard navigation in the JTable.
     */
    @Override
    protected KeyListener createKeyListener() {
        return getHandler();
    }

    private Color getAlternateColor(int modulo) {
        if (modulo == 0) {
            return UIManager.getColor("Table.alternateBackground.0");
        } else {
            return UIManager.getColor("Table.alternateBackground.1");
        }
    }

    /**
     * Attaches listeners to the JTable.
     */
    @Override
    protected void installListeners() {
        super.installListeners();
        propertyChangeListener = createPropertyChangeListener();
        table.addPropertyChangeListener(propertyChangeListener);
        listSelectionListener = createListSelectionListener();
        if (table.getSelectionModel() != null) {
            table.getSelectionModel().addListSelectionListener(listSelectionListener);
        }
        columnModelListener = createTableColumnModelListener();
        if (table.getColumnModel() != null) {
            table.getColumnModel().addColumnModelListener(columnModelListener);
        }
        // table.add
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        table.removePropertyChangeListener(propertyChangeListener);
        if (table.getSelectionModel() != null) {
            table.getSelectionModel().removeListSelectionListener(listSelectionListener);
        }
        if (table.getColumnModel() != null) {
            table.getColumnModel().removeColumnModelListener(columnModelListener);
        }
        propertyChangeListener = null;
        listSelectionListener = null;

    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        Object property = table.getClientProperty("Quaqua.Table.style");
        isStriped = property != null && property.equals("striped");
        updateStriped();
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        // table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);

        // By default, terminate editing on focus lost.
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // FIXME - Intercell spacings different from 1,1 don't work currently
        //table.setIntercellSpacing(new Dimension(4,4));
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
    }

    private void updateStriped() {
        /*if (isStriped) {
        table.setIntercellSpacing(new Dimension(1, 1));
        } else {
        //getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        table.setIntercellSpacing(new Dimension(1, 1));
        }*/
    }

    /** Paint a representation of the <code>table</code> instance
     * that was set in installUI().
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        if (table.getRowCount() <= 0 || table.getColumnCount() <= 0) {
            return;
        }
        Rectangle clip = g.getClipBounds();
        Point upperLeft = clip.getLocation();
        Point lowerRight = new Point(clip.x + clip.width - 1, clip.y + clip.height - 1);
        int rMin = table.rowAtPoint(upperLeft);
        int rMax = table.rowAtPoint(lowerRight);
        // This should never happen.
        if (rMin == -1) {
            rMin = 0;
        }
        // If the table does not have enough rows to fill the view we'll get -1.
        // Replace this with the row2 of the last row2.
        if (rMax == -1) {
            rMax = table.getRowCount() - 1;
        }

        boolean ltr = table.getComponentOrientation().isLeftToRight();
        int cMin = table.columnAtPoint(ltr ? upperLeft : lowerRight);
        int cMax = table.columnAtPoint(ltr ? lowerRight : upperLeft);
        // This should never happen.
        if (cMin == -1) {
            cMin = 0;
        }
        // If the table does not have enough columns to fill the view we'll get -1.
        // Replace this with the row2 of the last column.
        if (cMax == -1) {
            cMax = table.getColumnCount() - 1;
        }

        // Paint the cells.
        paintCells(g, rMin, rMax, cMin, cMax);
        // Paint the grid.
        paintGrid(g, rMin, rMax, cMin, cMax);
    }

    public void paintViewport(Graphics g, JViewport c) {
        Dimension vs = c.getSize();
        Dimension ts = table.getSize();
        Point p = table.getLocation();
        int rh = table.getRowHeight();
        int n = table.getRowCount();
        int row = Math.abs(p.y / rh);
        int th = n * rh - row * rh;


        if (isStriped) {
            // Fill the viewport with alternate color 1
            g.setColor(getAlternateColor(1));
            g.fillRect(0, 0, c.getWidth(), c.getHeight());

            // Now check if we need to paint some stripes
            g.setColor(getAlternateColor(0));

            // Paint empty rows at the right to fill the viewport
            if (ts.width < vs.width) {
                for (int y = p.y + row * rh, ymax = Math.min(th, vs.height); y < ymax; y += rh) {
                    if (row % 2 == 0) {
                        g.fillRect(0, y, vs.width, rh);
                    }
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
        } else {
            // Fill the viewport with the background color of the table
            g.setColor(table.getBackground());
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }

        // Paint the horizontal grid lines
        if (table.getShowHorizontalLines()) {
            g.setColor(table.getGridColor());
            if (ts.width < vs.width) {
                row = Math.abs(p.y / rh);
                int y = p.y + row * rh + rh - 1;
                while (y < th) {
                    g.drawLine(0, y, vs.width, y);
                    y += rh;
                }
            }
            if (th < vs.height) {
                int y = th + rh - 1;
                while (y < vs.height) {
                    g.drawLine(0, y, vs.width, y);
                    y += rh;
                }
            }
        }


        // Paint the vertical grid lines
        if (th < vs.height && table.getShowVerticalLines()) {
            g.setColor(table.getGridColor());
            TableColumnModel cm = table.getColumnModel();
            n = cm.getColumnCount();
            int y = th;
            int x = table.getX() - 1;
            for (int i = 0; i < n; i++) {
                TableColumn col = cm.getColumn(i);
                x += col.getWidth();
                g.drawLine(x, y, x, vs.height);
            }
        }
    }

    /*
     * Paints the grid lines within <I>aRect</I>, using the grid
     * color set with <I>setGridColor</I>. Paints vertical lines
     * if <code>getShowVerticalLines()</code> returns true and paints
     * horizontal lines if <code>getShowHorizontalLines()</code>
     * returns true.
     */
    private void paintGrid(Graphics g, int rMin, int rMax, int cMin, int cMax) {
        g.setColor(table.getGridColor());
        Rectangle minCell = table.getCellRect(rMin, cMin, true);
        Rectangle maxCell = table.getCellRect(rMax, cMax, true);
        Rectangle damagedArea = minCell.union(maxCell);

        if (table.getShowHorizontalLines()) {
            int tableWidth = damagedArea.x + damagedArea.width;
            int y = damagedArea.y;
            for (int row = rMin; row <= rMax; row++) {
                y += table.getRowHeight(row);
                g.drawLine(damagedArea.x, y - 1, tableWidth - 1, y - 1);
            }
        }
        if (table.getShowVerticalLines()) {
            JTableHeader header = table.getTableHeader();
            TableColumn draggedColumn = (header == null) ? null : header.getDraggedColumn();
            Rectangle vacatedColumnRect;
            if (draggedColumn != null) {
                int draggedColumnIndex = viewIndexForColumn(draggedColumn);

                Rectangle minDraggedCell = table.getCellRect(rMin, draggedColumnIndex, true);
                Rectangle maxDraggedCell = table.getCellRect(rMax, draggedColumnIndex, true);

                vacatedColumnRect = minDraggedCell.union(maxDraggedCell);

                // Move to the where the cell has been dragged.
                vacatedColumnRect.x += header.getDraggedDistance();
            } else {
                vacatedColumnRect = new Rectangle(0, 0, -1, -1);
            }

            TableColumnModel cm = table.getColumnModel();
            int tableHeight = damagedArea.y + damagedArea.height;
            int x;
            if (table.getComponentOrientation().isLeftToRight()) {
                x = damagedArea.x;
                for (int column = cMin; column <= cMax; column++) {
                    int w = cm.getColumn(column).getWidth();
                    x += w;
                    if (x < vacatedColumnRect.x || x > vacatedColumnRect.x + vacatedColumnRect.width) {
                        g.drawLine(x - 1, 0, x - 1, tableHeight - 1);
                    }
                }
            } else {
                x = damagedArea.x + damagedArea.width;
                for (int column = cMin; column < cMax; column++) {
                    int w = cm.getColumn(column).getWidth();
                    x -= w;
                    if (x < vacatedColumnRect.x || x > vacatedColumnRect.x + vacatedColumnRect.width) {
                        g.drawLine(x - 1, 0, x - 1, tableHeight - 1);
                    }
                }
                x -= cm.getColumn(cMax).getWidth();
                g.drawLine(x, 0, x, tableHeight - 1);
            }
        }
    }

    private void paintDraggedArea(Graphics g, int rMin, int rMax, TableColumn draggedColumn, int distance) {
        int draggedColumnIndex = viewIndexForColumn(draggedColumn);

        Rectangle minCell = table.getCellRect(rMin, draggedColumnIndex, true);
        Rectangle maxCell = table.getCellRect(rMax, draggedColumnIndex, true);

        Rectangle vacatedColumnRect = minCell.union(maxCell);

        // Paint a gray well in place of the moving column.
        g.setColor(table.getParent().getBackground());
        g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y,
                vacatedColumnRect.width, vacatedColumnRect.height);

        // Move to the where the cell has been dragged.
        vacatedColumnRect.x += distance;

        // Fill the background.
        g.setColor(table.getBackground());
        g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y,
                vacatedColumnRect.width, vacatedColumnRect.height);

        // Paint the vertical grid lines if necessary.
        if (table.getShowVerticalLines()) {
            g.setColor(table.getGridColor());
            int x1 = vacatedColumnRect.x;
            int y1 = vacatedColumnRect.y;
            int x2 = x1 + vacatedColumnRect.width - 1;
            int y2 = y1 + vacatedColumnRect.height - 1;
            // Left
            g.drawLine(x1 - 1, y1, x1 - 1, y2);
            // Right
            g.drawLine(x2, y1, x2, y2);
        }

        boolean isFocused = isFocused();

        for (int row = rMin; row <= rMax; row++) {
            // Render the cell value
            Rectangle r = table.getCellRect(row, draggedColumnIndex, false);
            r.x += distance;
            paintCell(g, r, row, draggedColumnIndex, isFocused);

            // Paint the (lower) horizontal grid line if necessary.
            if (table.getShowHorizontalLines()) {
                g.setColor(table.getGridColor());
                Rectangle rcr = table.getCellRect(row, draggedColumnIndex, true);
                rcr.x += distance;
                int x1 = rcr.x;
                int y1 = rcr.y;
                int x2 = x1 + rcr.width - 1;
                int y2 = y1 + rcr.height - 1;
                g.drawLine(x1, y2, x2, y2);
            }
        }
    }

    private int viewIndexForColumn(TableColumn aColumn) {
        TableColumnModel cm = table.getColumnModel();
        for (int column = 0; column < cm.getColumnCount(); column++) {
            if (cm.getColumn(column) == aColumn) {
                return column;
            }
        }
        return -1;
    }

    private boolean isFocused() {
        return table.isEditing() || QuaquaUtilities.isFocused(table);
    }

    private void paintCells(Graphics g, int rMin, int rMax, int cMin, int cMax) {
        boolean isFocused = isFocused();
        JTableHeader header = table.getTableHeader();
        TableColumn draggedColumn = (header == null) ? null : header.getDraggedColumn();

        TableColumnModel cm = table.getColumnModel();
        int columnMargin = cm.getColumnMargin();

        Rectangle cellRect;
        TableColumn aColumn;
        int columnWidth;
        if (table.getComponentOrientation().isLeftToRight()) {
            for (int row = rMin; row <= rMax; row++) {
                cellRect = table.getCellRect(row, cMin, false);
                for (int column = cMin; column <= cMax; column++) {
                    aColumn = cm.getColumn(column);
                    columnWidth = aColumn.getWidth();
                    cellRect.width = columnWidth - columnMargin;
                    if (aColumn != draggedColumn) {
                        paintCell(g, cellRect, row, column, isFocused);
                    }
                    cellRect.x += columnWidth;
                }
            }
        } else {
            for (int row = rMin; row <= rMax; row++) {
                cellRect = table.getCellRect(row, cMin, false);
                aColumn = cm.getColumn(cMin);
                if (aColumn != draggedColumn) {
                    columnWidth = aColumn.getWidth();
                    cellRect.width = columnWidth - columnMargin;
                    paintCell(g, cellRect, row, cMin, isFocused);
                }
                for (int column = cMin + 1; column <= cMax; column++) {
                    aColumn = cm.getColumn(column);
                    columnWidth = aColumn.getWidth();
                    cellRect.width = columnWidth - columnMargin;
                    cellRect.x -= columnWidth;
                    if (aColumn != draggedColumn) {
                        paintCell(g, cellRect, row, column, isFocused);
                    }
                }
            }
        }

        // Paint the dragged column if we are dragging.
        if (draggedColumn != null) {
            paintDraggedArea(g, rMin, rMax, draggedColumn, header.getDraggedDistance());
        }

        // Remove any renderers that may be left in the rendererPane.
        rendererPane.removeAll();

    }

    private void paintCell(Graphics g, Rectangle cellRect, int row, int column, boolean isFocused) {
        // Ugly dirty hack to get correct painting of inactive tables
        Color background = UIManager.getColor("Table.selectionBackground");
        Color foreground = UIManager.getColor("Table.selectionForeground");
        if (background instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) background).setActive(isFocused
                    && (table.getRowSelectionAllowed() || table.getColumnSelectionAllowed()));
        }
        if (foreground instanceof InactivatableColorUIResource) {
            // Note: We must draw with inactive color if the current cell is not selected.
            //       Otherwise, we get white text on white background.
            ((InactivatableColorUIResource) foreground).setActive(isFocused
                    && (table.getRowSelectionAllowed() || table.getColumnSelectionAllowed())
                    && table.isCellSelected(row, column));
        }

        Dimension spacing = table.getIntercellSpacing();
        if (table.getShowHorizontalLines()) {
            spacing.height -= 1;
        }
        if (table.getShowVerticalLines()) {
            spacing.width -= 1;
        }

        if (table.isEditing() && table.getEditingRow() == row
                && table.getEditingColumn() == column) {
            Component component = table.getEditorComponent();
            // Unless a font has been explicitly set on the editor, we
            // set the font used by the table.
            if (component.getFont() instanceof UIResource) {
                component.setFont(table.getFont());
            }
            // Unless a background color has been explicitly set on the editor,
            // we set the background color used by the table.
            if (component.getBackground() instanceof UIResource) {
                // BEGIN FIX QUAQUA-146 Cell editor has wrong colors when
                //                      cell is selected
                if (table.isCellSelected(row, column)) {
                    component.setBackground(background);
                } else {
                    if (isStriped) {
                        component.setBackground(getAlternateColor(row % 2));
                    } else {
                        component.setBackground(table.getBackground());
                    }
                }
                // END FIX QUAQUA-146
            }
            component.setBounds(cellRect);
            component.validate();
        } else {
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            Component component = table.prepareRenderer(renderer, row, column);

            if (table.isCellSelected(row, column)) {
                g.setColor(background);
                g.fillRect(cellRect.x - spacing.width, cellRect.y, cellRect.width + spacing.width * 2, cellRect.height);
            } else if (isStriped) {
                g.setColor(getAlternateColor(row % 2));
                g.fillRect(cellRect.x - spacing.width, cellRect.y, cellRect.width + spacing.width * 2, cellRect.height + spacing.height);
            }

            if ((component instanceof UIResource) && (component instanceof JComponent)) {
                ((JComponent) component).setOpaque(false);
            }

            //component.setBackground(background);
            rendererPane.paintComponent(g, component, table, cellRect.x, cellRect.y,
                    cellRect.width, cellRect.height, true);

        }
        // Ugly dirty hack to get proper rendering of inactive tables
        // Here we clean up the values of the "active" property of the selection
        // colors.
        if (!isFocused) {
            if (background instanceof InactivatableColorUIResource) {
                ((InactivatableColorUIResource) background).setActive(true);
            }
            if (foreground instanceof InactivatableColorUIResource) {
                ((InactivatableColorUIResource) foreground).setActive(true);
            }
        }
    }

    /**
     * Creates the mouse listener for the JTable.
     */
    @Override
    protected MouseInputListener createMouseInputListener() {
        return getHandler();
    }

    /**
     * Creates the property change listener for the JTable.
     */
    private PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }

    /**
     * Creates the list selection listener for the JTable.
     */
    private ListSelectionListener createListSelectionListener() {
        return getHandler();
    }

    /**
     * Creates the list selection listener for the JTable.
     */
    private TableColumnModelListener createTableColumnModelListener() {
        return getHandler();
    }

    /**
     * Lazily creates the handler.
     */
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    /**
     * Creates the focus listener for handling keyboard navigation in the JTable.
     */
    @Override
    protected FocusListener createFocusListener() {
        return getHandler();
    }

    private static int getAdjustedLead(JTable table,
            boolean row,
            ListSelectionModel model) {

        int index = model.getLeadSelectionIndex();
        int compare = row ? table.getRowCount() : table.getColumnCount();
        return index < compare ? index : -1;
    }

    private static int getAdjustedLead(JTable table, boolean row) {
        return row ? getAdjustedLead(table, row, table.getSelectionModel())
                : getAdjustedLead(table, row, table.getColumnModel().getSelectionModel());
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTableUI.
     */
    /**
     * PropertyChangeListener for the table. Updates the appropriate
     * varaible, or TreeState, based on what changes.
     */
    private class Handler implements
            PropertyChangeListener, ListSelectionListener,//
            TableColumnModelListener, FocusListener, MouseInputListener, //
            KeyListener {

        private boolean isAdjustingRowSelection;
        // Component receiving mouse events during editing.
        // May not be editorComponent.
        private Component dispatchComponent;
        private boolean mouseReleaseDeselects;
        private final static int MOUSE_DRAG_DOES_NOTHING = 0;
        private final static int MOUSE_DRAG_SELECTS = 1;
        private final static int MOUSE_DRAG_TOGGLES_SELECTION = 2;
        private final static int MOUSE_DRAG_STARTS_DND = 3;
        private int mouseDragAction;
        /** index of previously toggled row. */
        private int toggledRow = -1;
        /** index of previously toggled column. */
        private int toggledColumn = -1;

        public void propertyChange(PropertyChangeEvent event) {
            String name = event.getPropertyName();

            if (name.equals("Quaqua.Table.style")) {
                Object value = event.getNewValue();
                isStriped = value != null && value.equals("striped");
                updateStriped();
            } else if (name.equals("showVerticalLines")
                    || name.equals("showHorizontalLines")) {
                if (table.getParent() instanceof JViewport) {
                    table.getParent().repaint();
                }
            } else if (name.equals("selectionModel")) {
                if (event.getOldValue() != null) {
                    ((ListSelectionModel) event.getOldValue()).removeListSelectionListener(listSelectionListener);
                }
                if (event.getNewValue() != null) {
                    ((ListSelectionModel) event.getNewValue()).addListSelectionListener(listSelectionListener);
                }
            } else if (name.equals("columnModel")) {
                if (event.getOldValue() != null) {
                    ((TableColumnModel) event.getOldValue()).removeColumnModelListener(columnModelListener);
                }
                if (event.getNewValue() != null) {
                    ((TableColumnModel) event.getNewValue()).addColumnModelListener(columnModelListener);
                }
            } else if (name.equals("tableCellEditor")) {
                table.repaint();
            } else if (name.equals("JComponent.sizeVariant")) {
                QuaquaUtilities.applySizeVariant(table);
            }
        }

        public void columnAdded(TableColumnModelEvent e) {
        }

        public void columnRemoved(TableColumnModelEvent e) {
        }

        public void columnMoved(TableColumnModelEvent e) {
        }

        public void columnMarginChanged(ChangeEvent e) {
        }

        private int getAdjustedIndex(int index, boolean row) {
            int compare = row ? table.getRowCount() : table.getColumnCount();
            return index < compare ? index : -1;
        }

        public void columnSelectionChanged(ListSelectionEvent e) {
            ListSelectionModel selectionModel = table.getSelectionModel();
            int firstIndex = limit(e.getFirstIndex(), 0, table.getColumnCount() - 1);
            int lastIndex = limit(e.getLastIndex(), 0, table.getColumnCount() - 1);
            int minRow = 0;
            int maxRow = table.getRowCount() - 1;
            if (table.getRowSelectionAllowed()) {
                minRow = selectionModel.getMinSelectionIndex();
                maxRow = selectionModel.getMaxSelectionIndex();
                int leadRow = getAdjustedIndex(selectionModel.getLeadSelectionIndex(), true);

                if (minRow == -1 || maxRow == -1) {
                    if (leadRow == -1) {
                        // nothing to repaint, return
                        return;
                    }

                    // only thing to repaint is the lead
                    minRow = maxRow = leadRow;
                } else {
                    // We need to consider more than just the range between
                    // the min and max selected index. The lead row, which could
                    // be outside this range, should be considered also.
                    if (leadRow != -1) {
                        minRow = Math.min(minRow, leadRow);
                        maxRow = Math.max(maxRow, leadRow);
                    }
                }
            }
            Rectangle firstColumnRect = table.getCellRect(minRow, firstIndex, false);
            Rectangle lastColumnRect = table.getCellRect(maxRow, lastIndex, false);
            Rectangle dirtyRegion = firstColumnRect.union(lastColumnRect);
            Dimension intercellSpacing = table.getIntercellSpacing();
            if (intercellSpacing != null) {
                dirtyRegion.width += table.getIntercellSpacing().width;
            }
            table.repaint(dirtyRegion);
        }

        /**
         * This is a reimplementation of the JTable.valueChanged method,
         * with the only difference, that we repaint the cells _including_ the
         * intercell spacing.
         * 
         * @param e
         */
        public void valueChanged(ListSelectionEvent e) {
            boolean isAdjusting = e.getValueIsAdjusting();
            if (isAdjustingRowSelection && !isAdjusting) {
                // The assumption is that when the model is no longer adjusting
                // we will have already gotten all the changes, and therefore
                // don't need to do an additional paint.
                isAdjustingRowSelection = false;
                return;
            }
            isAdjustingRowSelection = isAdjusting;
            // The getCellRect() calls will fail unless there is at least one column.
            if (table.getRowCount() <= 0 || table.getColumnCount() <= 0) {
                return;
            }
            int firstIndex = limit(e.getFirstIndex(), 0, table.getRowCount() - 1);
            int lastIndex = limit(e.getLastIndex(), 0, table.getRowCount() - 1);
            Rectangle firstRowRect = table.getCellRect(firstIndex, 0, true);
            Rectangle lastRowRect = table.getCellRect(lastIndex, table.getColumnCount() - 1, true);
            Rectangle dirtyRegion = firstRowRect.union(lastRowRect);
            dirtyRegion.width += table.getIntercellSpacing().width;
            table.repaint(dirtyRegion);
        }

        private int limit(int i, int a, int b) {
            return Math.min(b, Math.max(i, a));
        }

        //  The Table's mouse listener methods.
        public void mouseClicked(MouseEvent e) {
        }

        private void setDispatchComponent(MouseEvent e) {
            Component editorComponent = table.getEditorComponent();
            Point p = e.getPoint();
            Point p2 = SwingUtilities.convertPoint(table, p, editorComponent);
            dispatchComponent = SwingUtilities.getDeepestComponentAt(editorComponent,
                    p2.x, p2.y);
        }

        private boolean repostEvent(MouseEvent e) {
            // Check for isEditing() in case another event has
            // caused the editor to be removed. See bug #4306499.
            if (dispatchComponent == null || !table.isEditing()) {
                return false;
            }
            MouseEvent e2 = SwingUtilities.convertMouseEvent(table, e, dispatchComponent);
            dispatchComponent.dispatchEvent(e2);
            return true;
        }

        private void setValueIsAdjusting(boolean flag) {
            table.getSelectionModel().setValueIsAdjusting(flag);
            table.getColumnModel().getSelectionModel().setValueIsAdjusting(flag);
        }

        private boolean shouldIgnore(MouseEvent e) {
            return e.isConsumed() || (!(SwingUtilities.isLeftMouseButton(e) && table.isEnabled())) || e.isPopupTrigger()
                    && (table.rowAtPoint(e.getPoint()) == -1
                    || table.isRowSelected(table.rowAtPoint(e.getPoint())));
        }

        public void mousePressed(MouseEvent e) {
            if (QuaquaUtilities.shouldIgnore(e, table)) {
                return;
            }
            if (table.isEditing() && !table.getCellEditor().stopCellEditing()) {
                Component editorComponent = table.getEditorComponent();
                if (editorComponent != null && !editorComponent.hasFocus()) {
                    QuaquaUtilities.compositeRequestFocus(editorComponent);
                }
                return;
            }

            mouseDragAction = MOUSE_DRAG_DOES_NOTHING;
            mouseReleaseDeselects = false;
            toggledRow = toggledColumn = -1;

            Point p = e.getPoint();
            int row = table.rowAtPoint(p);
            int column = table.columnAtPoint(p);

            if (table.isEnabled()) {
                // Note: Some applications depend on selection changes only occuring
                // on focused components. Maybe we must not do any changes to the
                // selection changes at all, when the compnent is not focused?
                table.requestFocusInWindow();

                // Maybe edit cell
                if (table.editCellAt(row, column, e)) {
                    setDispatchComponent(e);
                    repostEvent(e);

                    if (!table.getCellEditor().shouldSelectCell(e)) {
                        return;
                    }
                } else {
                }

                if (row != -1 && column != -1) {
                    if (table.isRowSelected(row) && e.isPopupTrigger()) {
                        // Do not change the selection, if the item is already
                        // selected, and the user triggers the popup menu.
                    } else {
                        int anchorIndex = table.getSelectionModel().getAnchorSelectionIndex();
                        if ((e.getModifiersEx() & (MouseEvent.META_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK)) //
                                == MouseEvent.META_DOWN_MASK) {
                            // toggle the selection
                            table.changeSelection(row, column, true, false);
                            toggledRow = row;
                            toggledColumn = column;
                            mouseDragAction = MOUSE_DRAG_TOGGLES_SELECTION;
                        } else if ((e.getModifiersEx() & (MouseEvent.SHIFT_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK))//
                                == MouseEvent.SHIFT_DOWN_MASK
                                && anchorIndex != -1) {
                            // add all rows to the selection from the anchor to the row
                            table.changeSelection(row, column, false, true);
                            mouseDragAction = MOUSE_DRAG_SELECTS;
                        } else if ((e.getModifiersEx() & (MouseEvent.SHIFT_DOWN_MASK | MouseEvent.META_DOWN_MASK)) == 0) {
                            if (table.isCellSelected(row, column)) {
                                mouseReleaseDeselects = table.isFocusOwner();
                                mouseDragAction = MOUSE_DRAG_STARTS_DND;
                            } else {
                                // Only select the cell
                                table.changeSelection(row, column, false, false);
                                mouseDragAction = MOUSE_DRAG_SELECTS;
                            }
                        }
                    }
                }

                table.getSelectionModel().setValueIsAdjusting(mouseDragAction != MOUSE_DRAG_DOES_NOTHING);
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (QuaquaUtilities.shouldIgnore(e, table)) {
                return;
            }
            int row = table.rowAtPoint(e.getPoint());
            int column = table.columnAtPoint(e.getPoint());
            repostEvent(e);

            if (table.isEnabled()) {

                mouseDragAction = MOUSE_DRAG_DOES_NOTHING;
                if (mouseReleaseDeselects) {
                    table.changeSelection(row, column, false, false);
                }
                table.getSelectionModel().setValueIsAdjusting(false);

                if (table.isRequestFocusEnabled() && !table.isEditing()) {
                    table.requestFocus();
                }
            }
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        //  The Table's mouse motion listener methods.
        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            if (!table.isEnabled() || shouldIgnore(e)) {
                return;
            }
            CellEditor editor = table.getCellEditor();
            if (editor == null || editor.shouldSelectCell(e)) {
                mouseReleaseDeselects = false;
                if (mouseDragAction == MOUSE_DRAG_SELECTS) {
                    int row = table.rowAtPoint(e.getPoint());
                    int column = table.columnAtPoint(e.getPoint());
                    if (row != -1 && column != -1) {
                        Rectangle cellBounds = table.getCellRect(row, column, true);
                        table.scrollRectToVisible(cellBounds);
                        table.changeSelection(row, column, false, true);
                    }
                } else if (mouseDragAction == MOUSE_DRAG_TOGGLES_SELECTION) {
                    int row = table.rowAtPoint(e.getPoint());
                    int column = table.columnAtPoint(e.getPoint());
                    boolean isCellSelection = table.getCellSelectionEnabled();
                    if (row != -1 && column != -1
                            && ((!isCellSelection && row != toggledRow)
                            || (isCellSelection && (row != toggledRow || column != toggledColumn)))) {
                        Rectangle cellBounds = table.getCellRect(row, column, true);
                        table.scrollRectToVisible(cellBounds);
                        table.changeSelection(row, column, true, false);
                        toggledRow = row;
                        toggledColumn = column;
                    }
                } else if (mouseDragAction == MOUSE_DRAG_STARTS_DND) {
                    if (table.getDragEnabled()) {
                        TransferHandler th = table.getTransferHandler();
                        int action = QuaquaUtilities.mapDragOperationFromModifiers(e, th);
                        if (action != TransferHandler.NONE) {
                            /* notify the BeforeDrag instance * /
                            if (bd != null) {
                            bd.dragStarting(dndArmedEvent);
                            }*/
                            th.exportAsDrag(table, e, action);
                            //clearState();
                        }
                    }

                }
            }
        }

        // BEGIN FocusListener
        public void focusGained(FocusEvent e) {
            repaintSelection();
        }

        public void focusLost(FocusEvent e) {
            repaintSelection();
        }

        private void repaintSelection() {
            final int[] rows = table.getSelectedRows();
            if (rows.length > 0) {
                //
                // only repaint visible rows
                //
                int firstRow = 0;
                int lastRow = table.getRowCount();
                int firstCol = 0;
                int lastCol = table.getColumnCount();
                if (table.getParent() instanceof JViewport) {
                    final JViewport pp = (JViewport) table.getParent();
                    final Point currentPos = pp.getViewPosition();
                    final Dimension extentSize = pp.getExtentSize();
                    // 1/-1 allow for rows & cols partially in the rect
                    firstRow = table.rowAtPoint(currentPos) - 1;
                    firstCol = table.columnAtPoint(currentPos) - 1;
                    lastRow = table.rowAtPoint(new Point(currentPos.x, currentPos.y + extentSize.height)) + 1;
                    lastCol = table.columnAtPoint(new Point(currentPos.x + extentSize.width, currentPos.y)) + 1;
                }

                if (rows[0] <= lastRow && rows[rows.length - 1] >= firstRow) {
                    for (int r = 0; r < rows.length; r++) {
                        int rr = rows[r];
                        if (rr >= firstRow) {
                            if (rr <= lastRow) {
                                for (int c = firstCol; c < lastCol; c++) {
                                    table.repaint(table.getCellRect(rr, c, false));
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }
        // END FocusListener

        // BEGIN KeyListener
        public void keyPressed(KeyEvent e) {
            // Eat away META down keys..
            // We need to do this, because the JTable.processKeyBinding(…)
            // method does not treat VK_META as a modifier key, and starts
            // editing a cell whenever this key is pressed.

            // XXX - This is bogus but seems to work. Consider disabling
            // automatic editing in JTable by setting the client property
            // "JTable.autoStartsEdit" to Boolean.FALSE and doing all the
            // processing here.

            if (e.getKeyCode() == KeyEvent.VK_META) {
                e.consume();
            }
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(e.getKeyChar(),
                    e.getModifiers());

            // We register all actions using ANCESTOR_OF_FOCUSED_COMPONENT
            // which means that we might perform the appropriate action
            // in the table and then forward it to the editor if the editor
            // had focus. Make sure this doesn't happen by checking our
            // InputMaps.
            InputMap map = table.getInputMap(JComponent.WHEN_FOCUSED);
            if (map != null && map.get(keyStroke) != null) {
                return;
            }
            map = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            if (map != null && map.get(keyStroke) != null) {
                return;
            }

            keyStroke = KeyStroke.getKeyStrokeForEvent(e);

            // The AWT seems to generate an unconsumed \r event when
            // ENTER (\n) is pressed.
            if (e.getKeyChar() == '\r') {
                return;
            }

            int leadRow = getAdjustedLead(table, true);
            int leadColumn = getAdjustedLead(table, false);
            if (leadRow != -1 && leadColumn != -1 && !table.isEditing()) {
                // We only start editing if the meta key is not down.
                if ((e.getModifiersEx() & InputEvent.META_DOWN_MASK) == 0) {
                    if (!table.editCellAt(leadRow, leadColumn)) {
                        return;
                    }
                }
            }

            // Forwarding events this way seems to put the component
            // in a state where it believes it has focus. In reality
            // the table retains focus - though it is difficult for
            // a user to tell, since the caret is visible and flashing.

            // Calling table.requestFocus() here, to get the focus back to
            // the table, seems to have no effect.

            Component editorComp = table.getEditorComponent();
            if (table.isEditing() && editorComp != null) {
                if (editorComp instanceof JComponent) {
                    JComponent component = (JComponent) editorComp;
                    map = component.getInputMap(JComponent.WHEN_FOCUSED);
                    Object binding = (map != null) ? map.get(keyStroke) : null;
                    if (binding == null) {
                        map = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                        binding = (map != null) ? map.get(keyStroke) : null;
                    }
                    if (binding != null) {
                        ActionMap am = component.getActionMap();
                        Action action = (am != null) ? am.get(binding) : null;
                        if (action != null && SwingUtilities.notifyAction(action, keyStroke, e, component,
                                e.getModifiers())) {
                            e.consume();
                        }
                    }
                }
            }
        }
        // END KeyListener
    } // End of QuaquaTableUI.Handler
}
