/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui;

import java.awt.Component;

import seco.notebook.NotebookUI;

/**
 * The interface all option panes must implement.<p>
 *
 * See {@link EditPlugin} for information on how jEdit obtains and constructs
 * option pane instances.<p>
 *
 * Note that in most cases it is much easier to extend
 * {@link AbstractOptionPane} instead.
 *
 * @author Slava Pestov
 * @version $Id: OptionPane.java,v 1.1 2006/08/29 16:59:51 bizi Exp $
 */
public interface OptionPane
{
	/**
	 * Returns the internal name of this option pane. 
	 */
	String getName();

	/**
	 * Returns the component that should be displayed for this option pane.
	 */
	Component getComponent();

	/**
	 * This method is called every time the option pane is displayed.
	 */
	void init();

	/**
	 * Called when the options dialog's "ok" button is clicked.
	 * This should save any properties being edited in this option
	 * pane.
	 */
	void save();
	
	void resetDefaults();
	
	void setNotebookUI(NotebookUI nbui);
}

