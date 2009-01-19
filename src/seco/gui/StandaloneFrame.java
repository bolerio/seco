/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.event.CaretListener;

import seco.notebook.GUIHelper;
import seco.notebook.StatusBar;
import seco.notebook.gui.AKDockLayout;

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
        getContentPane().add(GUIHelper.getJTabbedPane(), BorderLayout.CENTER);
        getContentPane().add(statusPane, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(1000, 700));
        setMinimumSize(new Dimension(1000, 700));
        pack();
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
