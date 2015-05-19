/*
 * @(#)QuaquaTableHeaderUI.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.border.BackgroundBorder;
import ch.randelshofer.quaqua.util.Methods;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
 * QuaquaTableHeaderUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaTableHeaderUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaTableHeaderUI extends BasicTableHeaderUI {
    // Listeners that are attached to the JTable

    protected TableColumnModelListener columnModelListener;

    /** Creates a new instance. */
    public QuaquaTableHeaderUI() {
    }

    public static ComponentUI createUI(JComponent c) {
        return new QuaquaTableHeaderUI();
    }

    @Override
    public void installDefaults() {
        super.installDefaults();
        if (header.getDefaultRenderer() instanceof JLabel) {
            ((JLabel) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEADING);
            //((JLabel) header.getDefaultRenderer()).setOpaque(false);
        }
    }

    private int viewIndexForColumn(TableColumn aColumn) {
        TableColumnModel cm = header.getColumnModel();
        for (int column = 0; column < cm.getColumnCount(); column++) {
            if (cm.getColumn(column) == aColumn) {
                return column;
            }
        }
        return -1;
    }
    //
    // Paint Methods and support
    //

    @Override
    public void paint(Graphics g, JComponent c) {
        if (header.getColumnModel().getColumnCount() <= 0) {
            return;
        }
        boolean ltr = header.getComponentOrientation().isLeftToRight();

        Rectangle clip = g.getClipBounds();
        Point left = clip.getLocation();
        Point right = new Point(clip.x + clip.width - 1, clip.y);
        TableColumnModel cm = header.getColumnModel();
        int cMin = header.columnAtPoint(ltr ? left : right);
        int cMax = header.columnAtPoint(ltr ? right : left);
        // This should never happen. 
        if (cMin == -1) {
            cMin = 0;
        }
        /*
        // If the table does not have enough columns to fill the view we'll get -1.
        // Replace this with the index of the last column.
        if (cMax == -1) {
        cMax = cm.getColumnCount()-1;
        }*/
        // If the table does not have enough columns to fill the view we'll get -1.
        // Replace this with the index of the last column.
        if (cMax == -1) {
            cMax = cm.getColumnCount() - 1;
            Border cellBorder = UIManager.getBorder("TableHeader.cellBorder");
            if (cellBorder instanceof QuaquaTableHeaderBorder) {
                QuaquaTableHeaderBorder qthb = ((QuaquaTableHeaderBorder) cellBorder);
                qthb.setColumnIndex(-1);
            }
            if (cellBorder instanceof BackgroundBorder) {
                cellBorder = ((BackgroundBorder) cellBorder).getBackgroundBorder();
            }
            if (cellBorder != null) {
                cellBorder.paintBorder(header, g, cMax, 0, header.getWidth() - cMax, header.getHeight());
            }
        }

        TableColumn draggedColumn = header.getDraggedColumn();
        int columnWidth;
        Rectangle cellRect = header.getHeaderRect(ltr ? cMin : cMax);
        TableColumn aColumn;
        if (ltr) {
            for (int column = cMin; column <= cMax; column++) {
                aColumn = cm.getColumn(column);
                columnWidth = aColumn.getWidth();
                cellRect.width = columnWidth;
                if (aColumn != draggedColumn) {
                    paintCell(g, cellRect, column);
                }
                cellRect.x += columnWidth;
            }
        } else {
            for (int column = cMax; column >= cMin; column--) {
                aColumn = cm.getColumn(column);
                columnWidth = aColumn.getWidth();
                cellRect.width = columnWidth;
                if (aColumn != draggedColumn) {
                    paintCell(g, cellRect, column);
                }
                cellRect.x += columnWidth;
            }
        }

        // Paint the dragged column if we are dragging. 
        if (draggedColumn != null) {
            int draggedColumnIndex = viewIndexForColumn(draggedColumn);
            Rectangle draggedCellRect = header.getHeaderRect(draggedColumnIndex);

            // Draw a gray well in place of the moving column. 
            g.setColor(header.getParent().getBackground());
            g.fillRect(draggedCellRect.x, draggedCellRect.y,
                    draggedCellRect.width, draggedCellRect.height);

            draggedCellRect.x += header.getDraggedDistance();

            // Fill the background.
            g.setColor(header.getBackground());
            g.fillRect(draggedCellRect.x, draggedCellRect.y,
                    draggedCellRect.width, draggedCellRect.height);

            paintCell(g, draggedCellRect, draggedColumnIndex);
        }

        // Remove all components in the rendererPane.
        rendererPane.removeAll();
    }

    /**
     * Attaches listeners to the JTableHeader.
     */
    @Override
    protected void installListeners() {
        columnModelListener = createColumnModelListener();

        header.getColumnModel().addColumnModelListener(columnModelListener);
        super.installListeners();
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        header.getColumnModel().removeColumnModelListener(columnModelListener);

        columnModelListener = null;
    }

    private TableColumnModelListener createColumnModelListener() {
        return new ColumnHandler();
    }

    private Component getHeaderRenderer(int columnIndex) {
        TableColumn aColumn = header.getColumnModel().getColumn(columnIndex);
        TableCellRenderer renderer = aColumn.getHeaderRenderer();
        if (renderer == null) {
            renderer = header.getDefaultRenderer();
        }
        return renderer.getTableCellRendererComponent(header.getTable(),
                aColumn.getHeaderValue(), false, false,
                -1, columnIndex);
    }

    private void paintCell(Graphics g, Rectangle cellRect, int columnIndex) {
        Component component = getHeaderRenderer(columnIndex);

        if ((component instanceof JComponent)
                && (((JComponent) component).getBorder() instanceof QuaquaTableHeaderBorder)) {
            QuaquaTableHeaderBorder thb = (QuaquaTableHeaderBorder) ((JComponent) component).getBorder();
            thb.setColumnIndex(columnIndex);

            // Table sorting is only supported since J2SE 6.
            // Use reflection to determine whether the current column is sorted.
            boolean isSorted = false;
            Object rowSorter = Methods.invokeGetter(header.getTable(), "getRowSorter", null);
            List sortKeys = (List) ((rowSorter == null) ? null : Methods.invokeGetter(rowSorter, "getSortKeys", null));
            Object sortKey = (sortKeys == null || sortKeys.isEmpty()) ? null : sortKeys.get(0);
            int sortedColumnIndex = (sortKey == null) ? -1 : Methods.invokeGetter(sortKey, "getColumn", -1);
            isSorted = sortedColumnIndex == columnIndex;

            thb.setSorted(isSorted);
            thb.setOnActiveWindow(QuaquaUtilities.isOnActiveWindow(header.getTable()));
            rendererPane.paintComponent(g, component, header, cellRect.x, cellRect.y,
                    cellRect.width, cellRect.height, true);
            thb.setSorted(false);
            thb.setOnActiveWindow(true);
        } else {
            rendererPane.paintComponent(g, component, header, cellRect.x, cellRect.y,
                    cellRect.width, cellRect.height, true);
        }
    }

    private class ColumnHandler implements TableColumnModelListener {

        private void updateViewport() {
            JTable table = header.getTable();
            Object property = (table == null) ? null : table.getClientProperty("Quaqua.Table.style");
            if (property != null && property.equals("striped")
                    && table.getParent() instanceof JViewport) {
                JViewport viewport = (JViewport) table.getParent();
                if (viewport.getHeight() > table.getHeight()) {
                    viewport.repaint(0, table.getHeight(), viewport.getWidth(), viewport.getHeight() - table.getHeight());
                }
            }
        }

        public void columnAdded(TableColumnModelEvent e) {
            updateViewport();
        }

        public void columnMarginChanged(ChangeEvent e) {
            updateViewport();
        }

        public void columnMoved(TableColumnModelEvent e) {
            updateViewport();
        }

        public void columnRemoved(TableColumnModelEvent e) {
            updateViewport();
        }

        public void columnSelectionChanged(ListSelectionEvent e) {
            updateViewport();
        }
    }

    /**
     * Return the preferred size of the header. The preferred height is the 
     * maximum of the preferred heights of all of the components provided 
     * by the header renderers. The preferred width is the sum of the 
     * preferred widths of each column (plus inter-cell spacing).
     */
    @Override
    public Dimension getPreferredSize(JComponent c) {
        long width = 0;
        Enumeration enumeration = header.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
            TableColumn aColumn = (TableColumn) enumeration.nextElement();
            width = width + aColumn.getPreferredWidth();
        }
        return createHeaderSize(width);
    }

    private Dimension createHeaderSize(long width) {
        // None of the callers include the intercell spacing, do it here.
        if (width > Integer.MAX_VALUE) {
            width = Integer.MAX_VALUE;
        }
        return new Dimension((int) width, getHeaderHeight());
    }

    private int getHeaderHeight() {
        int height = 0;

        int emptyHeight = 0;

        boolean accomodatedDefault = false;
        TableColumnModel columnModel = header.getColumnModel();
        for (int column = 0; column < columnModel.getColumnCount(); column++) {
            TableColumn aColumn = columnModel.getColumn(column);
            // Configuring the header renderer to calculate its preferred size is expensive.
            // Optimise this by assuming the default renderer always has the same height.
            if (aColumn.getHeaderRenderer() != null || !accomodatedDefault) {
                Component comp = getHeaderRenderer(column);
                int rendererHeight = comp.getPreferredSize().height;
                height = Math.max(height, rendererHeight);

                if (comp instanceof JComponent) {
                    Border b = ((JComponent) comp).getBorder();
                    if (b != null && (b instanceof UIResource)) {
                        Insets insets = b.getBorderInsets(comp);
                        emptyHeight = Math.max(emptyHeight, insets.top + insets.bottom);
                    }
                }


                // If the header value is empty (== "") in the
                // first column (and this column is set up
                // to use the default renderer) we will
                // return zero from this routine and the header
                // will disappear altogether. Avoiding the calculation
                // of the preferred size is such a performance win for
                // most applications that we will continue to
                // use this cheaper calculation, handling these
                // issues as `edge cases'.
                if (rendererHeight > emptyHeight) {
                    accomodatedDefault = true;
                }
            }
        }
        // If all header cells are empty, we make the border disappear alltogether
        return (height <= emptyHeight) ? 0 : height;
    }
}
