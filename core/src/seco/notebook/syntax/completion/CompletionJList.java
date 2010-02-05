/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package seco.notebook.syntax.completion;

import java.awt.*;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

//import org.netbeans.editor.LocaleSupport;
//import org.netbeans.editor.ext.CompletionQuery;


/**
* @author Miloslav Metelka, Dusan Balek
* @version 1.00
*/

public class CompletionJList extends JList {
    
    private static final int DARKER_COLOR_COMPONENT = 5;
    
    private final RenderComponent renderComponent;
    
    private Graphics cellPreferredSizeGraphics;

    private int fixedItemHeight;
    private int maxVisibleRowCount;
    
    public CompletionJList(int maxVisibleRowCount, MouseListener mouseListener) {
        this.maxVisibleRowCount = maxVisibleRowCount;
        addMouseListener(mouseListener);
        setLayoutOrientation(JList.VERTICAL);
        setFixedCellHeight(fixedItemHeight = Math.max(CompletionLayout.COMPLETION_ITEM_HEIGHT, getFontMetrics(getFont()).getHeight()));
        setModel(new Model(Collections.EMPTY_LIST));

        renderComponent = new RenderComponent();
        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new ListCellRenderer() {
            private ListCellRenderer defaultRenderer = new DefaultListCellRenderer();

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if( value instanceof CompletionItem ) {
                    CompletionItem item = (CompletionItem)value;
                    renderComponent.setItem(item);
                    renderComponent.setSelected(isSelected);
                    Color bgColor;
                    Color fgColor;
                    if (isSelected) {
                        bgColor = list.getSelectionBackground();
                        fgColor = list.getSelectionForeground();
                    } else { // not selected
                        bgColor = list.getBackground();
                        if ((index % 2) == 0) { // every second item slightly different
                            bgColor = new Color(
                                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                            );
                        }
                        fgColor = list.getForeground();
                    }
                    // quick check Component.setBackground() always fires change
                    if (renderComponent.getBackground() != bgColor) {
                        renderComponent.setBackground(bgColor);
                    }
                    if (renderComponent.getForeground() != fgColor) {
                        renderComponent.setForeground(fgColor);
                    }
                    return renderComponent;

                } else {
                    return defaultRenderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus);
                }
            }
        });
        //getAccessibleContext().setAccessibleName(LocaleSupport.getString("ACSN_CompletionView"));
        //getAccessibleContext().setAccessibleDescription(LocaleSupport.getString("ACSD_CompletionView"));
    }
    
    void setData(List data) {
        if (data != null) {
            int itemCount = data.size();
            ((Model)getModel()).setData(data);
            if (itemCount > 0) {
                setSelectedIndex(0);
            }
            int visibleRowCount = Math.min(itemCount, maxVisibleRowCount);
            setVisibleRowCount(visibleRowCount);
        }
    }
    

    public void up() {
        int size = getModel().getSize();
        if (size > 0) {
            setSelectedIndex((getSelectedIndex() - 1 + size) % size);
            ensureIndexIsVisible(getSelectedIndex());
        }
    }

    public void down() {
        int size = getModel().getSize();
        if (size > 0) {
            setSelectedIndex((getSelectedIndex() + 1) % size);
            ensureIndexIsVisible(getSelectedIndex());
        }
    }

    public void pageUp() {
        if (getModel().getSize() > 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int ind = Math.max(getSelectedIndex() - pageSize, 0);

            setSelectedIndex(ind);
            ensureIndexIsVisible(ind);
        }
    }

    public void pageDown() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int ind = Math.min(getSelectedIndex() + pageSize, lastInd);

            setSelectedIndex(ind);
            ensureIndexIsVisible(ind);
        }
    }

    public void begin() {
        if (getModel().getSize() > 0) {
            setSelectedIndex(0);
            ensureIndexIsVisible(0);
        }
    }

    public void end() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            setSelectedIndex(lastInd);
            ensureIndexIsVisible(lastInd);
        }
    }

    static class Model extends AbstractListModel {

        List data;

        static final long serialVersionUID = 3292276783870598274L;

        public Model(List data) {
            this.data = data;
        }
        
        public void setData(List data) {
            List oldData = this.data;
            this.data = data;
            fireContentsChanged(this, 0, oldData.size());
        }

        public int getSize() {
            return data.size();
        }

        public Object getElementAt(int index) {
            return (index >= 0 && index < data.size()) ? data.get(index) : null;
        }

        List getData() {
            return data;
        }

    }
    
    private final class RenderComponent extends JComponent {
        
        private CompletionItem item;
        
        private boolean selected;
        
        void setItem(CompletionItem item) {
            this.item = item;
        }
        
        void setSelected(boolean selected) {
            this.selected = selected;
        }
        
       
        public void paintComponent(Graphics g) {
            // Although the JScrollPane without horizontal scrollbar
            // is explicitly set with a preferred size
            // it does not force its items with the only width into which
            // they can render (and still leaves them with the preferred width
            // of the widest item).
            // Therefore the item's render width is taken from the viewport's width.
            int itemRenderWidth = ((JViewport)CompletionJList.this.getParent()).getWidth();
            Color bgColor = getBackground();
            Color fgColor = getForeground();
            int height = getHeight();

            // Clear the background
            g.setColor(bgColor);
            g.fillRect(0, 0, itemRenderWidth, height);
            g.setColor(fgColor);

            // Render the item
            item.render(g, getFont(), getForeground(), bgColor,
                    itemRenderWidth, getHeight(), selected);
        }
        
        public Dimension getPreferredSize() {
            if (cellPreferredSizeGraphics == null) {
                // CompletionJList.this.getGraphics() is null
                cellPreferredSizeGraphics = java.awt.GraphicsEnvironment.
                        getLocalGraphicsEnvironment().getDefaultScreenDevice().
                        getDefaultConfiguration().createCompatibleImage(1, 1).getGraphics();
                assert (cellPreferredSizeGraphics != null);
            }
            return new Dimension(item.getPreferredWidth(cellPreferredSizeGraphics, getFont()),
                    fixedItemHeight);
        }

    }

}
