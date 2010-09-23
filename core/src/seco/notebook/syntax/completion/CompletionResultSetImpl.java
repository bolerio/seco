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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JToolTip;

/**
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CompletionResultSetImpl
{
    private final Completion completionImpl;
    private final Object resultId;
    private final CompletionTask task;
    private final int queryType;
    private CompletionResultSet resultSet;
    private boolean active;
    private String title;
    private String waitText;
    private int anchorOffset;
    private Set<CompletionItem> items;
    private boolean finished;
    private CompletionDocumentation documentation;
    private JToolTip toolTip;
    private int estimatedItemCount;

    CompletionResultSetImpl(Completion completionImpl, Object resultId,
            CompletionTask task, int queryType)
    {
        assert (completionImpl != null);
        assert (resultId != null);
        assert (task != null);
        this.completionImpl = completionImpl;
        this.resultId = resultId;
        this.task = task;
        this.queryType = queryType;
        this.anchorOffset = -1; // not set
        this.estimatedItemCount = -1; // not estimated
        this.active = true;

        createCompletionResultSet(this);
    }

    public CompletionResultSet createCompletionResultSet(
            CompletionResultSetImpl impl)
    {
        return new CompletionResultSet(impl);
    }

    /**
     * Get the result set instance associated with this implementation.
     */
    public synchronized CompletionResultSet getResultSet()
    {
        return resultSet;
    }

    public synchronized void setResultSet(CompletionResultSet resultSet)
    {
        assert (resultSet != null);
        assert (this.resultSet == null);
        this.resultSet = resultSet;
    }

    /**
     * Get the task associated with this result.
     */
    public CompletionTask getTask()
    {
        return task;
    }

    /**
     * Get the query type to which this result set belongs. The results of other
     * query types will be ignored when being set into this result set.
     */
    public int getQueryType()
    {
        return queryType;
    }

    /**
     * Mark that results from this result set should no longer be taken into
     * account.
     */
    public synchronized void markInactive()
    {
        this.active = false;
    }

    public synchronized String getTitle()
    {
        return title;
    }

    public synchronized void setTitle(String title)
    {
        checkNotFinished();
        this.title = title;
    }

    public synchronized int getAnchorOffset()
    {
        return anchorOffset;
    }

    public synchronized void setAnchorOffset(int anchorOffset)
    {
        checkNotFinished();
        this.anchorOffset = anchorOffset;
    }

    public synchronized boolean addItem(CompletionItem item)
    {
        assert (item != null) : "Added item cannot be null";
        checkNotFinished();
        // signal do not add any further results
        if (!active || queryType != CompletionProvider.COMPLETION_QUERY_TYPE) 
            return false;

        if (items == null)
        {
            int estSize = (estimatedItemCount == -1) ? 10 : estimatedItemCount;
            items = new HashSet<CompletionItem>(estSize);
        }
        items.add(item);
        return items.size() < 1000;
    }

    public boolean addAllItems(Collection<CompletionItem> items)
    {
        boolean cont = true;
        for (Iterator<CompletionItem> it = items.iterator(); it.hasNext();)
        {
            cont = addItem(it.next());
        }
        return cont;
    }

    /**
     * @return non-null list of items.
     */
    public synchronized Set<CompletionItem> getItems()
    {
        assert isFinished() : "Adding not finished";
        if (items != null) return items;
        Set<CompletionItem> empty = Collections.emptySet();
        return empty;
    }

    public synchronized void setDocumentation(
            CompletionDocumentation documentation)
    {
        checkNotFinished();
        if (!active || queryType != CompletionProvider.DOCUMENTATION_QUERY_TYPE) { return; }
        this.documentation = documentation;
    }

    public synchronized CompletionDocumentation getDocumentation()
    {
        return documentation;
    }

    public synchronized JToolTip getToolTip()
    {
        return toolTip;
    }

    public synchronized void setToolTip(JToolTip toolTip)
    {
        checkNotFinished();
        if (!active || queryType != CompletionProvider.TOOLTIP_QUERY_TYPE) { return; }
        this.toolTip = toolTip;
    }

    public synchronized boolean isFinished()
    {
        return finished;
    }

    public void finish()
    {
        synchronized (this)
        {
            if (finished) { throw new IllegalStateException(
                    "finish() already called"); // NOI18N
            }
            finished = true;
        }

        completionImpl.finishNotify(this);
    }

    public int getSortType()
    {
        return completionImpl.getSortType();
    }

    Completion getCompletionImpl()
    {
        return completionImpl;
    }

    Object getResultId()
    {
        return resultId;
    }

    private void checkNotFinished()
    {
        if (isFinished()) { throw new IllegalStateException(
                "Result set already finished"); // NOI18N
        }
    }

    public synchronized String getWaitText()
    {
        return waitText;
    }

    public synchronized void setWaitText(String waitText)
    {
        this.waitText = waitText;
    }

}
