/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.event.CaretListener;

import seco.ThisNiche;
import seco.notebook.StatusBar;
import seco.notebook.gui.AKDockLayout;
import seco.things.CellGroup;
import seco.things.CellGroupMember;

/**
 * 
 * @author bizi
 */
public class StandaloneFrame extends TopFrame
{
    private StatusBar status;
    private JPanel statusPane;

    StandaloneFrame()
    {
       
    }

    protected void initFrame()
    {
        // Create the status area.
        statusPane = new JPanel(new GridLayout(1, 1));
        status = new StatusBar(this);
        status.propertiesChanged();
        statusPane.add(status);

        setJMenuBar(GUIHelper.getMenuBar());
        getContentPane().setLayout(new AKDockLayout());
        getContentPane().add(GUIHelper.getMainToolBar(), AKDockLayout.NORTH);
        getContentPane().add(getJTabbedPane(), BorderLayout.CENTER);
        getContentPane().add(statusPane, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(1000, 700));
        setMinimumSize(new Dimension(1000, 700));
        pack();
    }
    
    private Component getJTabbedPane()
    {
        CellGroup top = ThisNiche.hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
        for(int i = 0; i < top.getArity(); i++)
        {
            CellGroupMember cgm = top.getElement(i);
            if(cgm instanceof CellGroup && 
                    TabbedPaneVisual.getHandle().equals(cgm.getVisual()))
            {
                focusedContainerHandle = top.getTargetAt(i);
                TabbedPaneVisual v = ThisNiche.hg.get(TabbedPaneVisual.getHandle());
                return v.bind(cgm);
            }
        }
        //no tabbedPane, add one
        CellGroup group = new CellGroup("TabbedPaneCellGroup");
        focusedContainerHandle = ThisNiche.hg.add(group);
        group.setVisual(TabbedPaneVisual.getHandle());
        ThisNiche.hg.update(group);
        top.insert(top.getArity(), group);
        return TabbedPaneU.createTabbedPane(group);
    } 

    public CaretListener getCaretListener()
    {
        return status;
    }

    public void setStatusBarMessage(String message)
    {
        if (status != null)
            status.setMessage(message);
    }

    private boolean html_toolbar_added;

    public void showHTMLToolBar(boolean show_or_hide)
    {
        if (!html_toolbar_added)
        {
            getContentPane()
                    .add(GUIHelper.getHTMLToolBar(), AKDockLayout.NORTH);
            getContentPane().invalidate();
            getContentPane().repaint();
            html_toolbar_added = true;
        }
        GUIHelper.getHTMLToolBar().setEnabled(show_or_hide);
    }

}
