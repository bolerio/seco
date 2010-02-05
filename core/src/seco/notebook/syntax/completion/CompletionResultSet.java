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

import java.util.Collection;

import javax.swing.JToolTip;

/**
 * Listener interface for passing the query results.
 * 
 * @see CompletionProvider#createTask
 * 
 * @author Miloslav Metelka, Dusan Balek
 * @version 1.01
 */

public final class CompletionResultSet
{

    /**
     * Sort type returned from {@link #getSortType()} that prefers priority of
     * the item ({@link CompletionItem#getSortPriority()}) over the text of the
     * item ({@link CompletionItem#getSortText()}).
     */
    public static final int PRIORITY_SORT_TYPE = 0;

    /**
     * Sort type returned from {@link #getSortType()} that prefers text of the
     * item ({@link CompletionItem#getSortText()}). over the priority of the
     * item ({@link CompletionItem#getSortPriority()})
     */
    public static final int TEXT_SORT_TYPE = 1;

    private CompletionResultSetImpl impl;

    CompletionResultSet(CompletionResultSetImpl impl)
    {
        this.impl = impl;
        impl.setResultSet(this);
    }

    /**
     * Set title that will be assigned to the completion popup window. <br>
     * It's only relevant to set the title when providing completion items for
     * {@link CompletionProvider#COMPLETION_QUERY_TYPE}. <br>
     * If there will be multiple completion providers setting this property for
     * the given mime-type then only the first one (according to the xml-layer
     * registration order) will be taken into account.
     */
    public void setTitle(String title)
    {
        impl.setTitle(title);
    }

    public String getTitle()
    {
        return impl.getTitle();
    }

    public Collection<CompletionItem> getData()
    {
        return impl.getItems();
    }

    /**
     * Set the document offset to which the returned completion items or
     * documentation or tooltip should be anchored. <br>
     * If there will be multiple completion providers setting this property for
     * the given mime-type then only the first one (according to the xml-layer
     * registration order) will be taken into account.
     */
    public void setAnchorOffset(int anchorOffset)
    {
        impl.setAnchorOffset(anchorOffset);
    }

    /**
     * Add the completion item to this result set. <br>
     * This method can be called multiple times until all the items have been
     * added to ths result set. <br>
     * After the adding is completed @link #finish()} must be called to confirm
     * that the result set will no longer be modified.
     * 
     * @param item
     *            non-null completion item.
     * @return true if adding of the items can continue or false if there is
     *         already too many items to be practical to display in the listbox
     *         so subsequent adding should preferably be discontinued.
     */
    public boolean addItem(CompletionItem item)
    {
        return impl.addItem(item);
    }

    /**
     * Add the collection of the completion items to this result set. <br>
     * This method can be called multiple times until all the items have been
     * added to ths result set. <br>
     * After the adding is completed @link #finish()} must be called to confirm
     * that the result set will no longer be modified.
     * 
     * @param items
     *            collection of items to be added.
     * @return true if adding of the items can continue or false if there is
     *         already too many items to be practical to display in the listbox
     *         so subsequent adding should preferably be discontinued.
     */
    public boolean addAllItems(Collection/* <CompletionItem> */items)
    {
        return impl.addAllItems(items);
    }

    /**
     * Set the documentation to this result set. <br>
     * Calling this method is only relevant for tasks created by
     * {@link CompletionProvider#createTask(int, javax.swing.text.JTextComponent)}
     * with {@link CompletionProvider#DOCUMENTATION_QUERY_TYPE} or for
     * {@link CompletionItem#createDocumentationTask()}.
     */
    public void setDocumentation(CompletionDocumentation documentation)
    {
        impl.setDocumentation(documentation);
    }

    /**
     * Set the tooltip to this result set. <br>
     * Calling this method is only relevant for tasks created by
     * {@link CompletionProvider#createTask(int, javax.swing.text.JTextComponent)}
     * with {@link CompletionProvider#TOOLTIP_QUERY_TYPE} or for
     * {@link CompletionItem#createToolTipTask()}.
     */
    public void setToolTip(JToolTip toolTip)
    {
        impl.setToolTip(toolTip);
    }

    /**
     * Mark that this result set is finished and there will be no more
     * modifications done to it.
     */
    public void finish()
    {
        impl.finish();
    }

    /**
     * Check whether this result set is finished.
     * 
     * @return true if the result set is already finished by previous call to
     *         {@link #finish()}.
     */
    public boolean isFinished()
    {
        return impl.isFinished();
    }

    /**
     * Get the sort type currently used by the code completion. <br>
     * It's one of the {@link #PRIORITY_SORT_TYPE} or {@link #TEXT_SORT_TYPE}.
     */
    public int getSortType()
    {
        return impl.getSortType();
    }

    /**
     * Set the explicit value displayed in a label when the completion results
     * do not get computed during a certain timeout (e.g. 250ms). <br>
     * If not set explicitly the completion infrastructure will use the default
     * text.
     * 
     * @param waitText
     *            description of what the query copmutation is currently (doing
     *            or waiting for). <br>
     *            After previous explicit setting <code>null</code> can be
     *            passed to restore using of the default text.
     * 
     * @since 1.5
     */
    public void setWaitText(String waitText)
    {
        impl.setWaitText(waitText);
    }
}
