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

import javax.swing.text.JTextComponent;


/**
 * The basic interface of the code completion querying SPI. Various implementations can
 * be registered per a document mime-type.
 *
 * @author Miloslav Metelka, Dusan Balek
 * @version 1.01
 */

public interface CompletionProvider {

    /**
     * The <code>int</code> value representing the query for a code completion.
     */    
    public static final int COMPLETION_QUERY_TYPE = 1;

    /**
     * The <code>int</code> value representing the query for a documentation.
     */    
    public static final int DOCUMENTATION_QUERY_TYPE = 2;
    
    /**
     * The <code>int</code> value representing the query for a tooltip hint.
     */    
    public static final int TOOLTIP_QUERY_TYPE = 4;

    /**
     * Creates a task that performs a query of the given type on the given component.
     * <br>
     * This method is invoked in AWT thread only and the returned task
     * may either be synchronous (if it's not complex)
     * or it may be asynchonous
     * (see {@link org.netbeans.spi.editor.completion.support.AsyncCompletionTask}).
     * <br>
     * The task usually inspects the component's document, the
     * text up to the caret position and returns the appropriate result.
     * 
     * @param queryType a type ot the query. It can be one of the {@link #COMPLETION_QUERY_TYPE},
     *  {@link #DOCUMENTATION_QUERY_TYPE}, or {@link #TOOLTIP_QUERY_TYPE}
     *  (but not their combination).          
     * @param component a component on which the query is performed
     *
     * @return a task performing the query.
     */
    public CompletionTask createTask(int queryType, JTextComponent component);

    /**
     * Called by the code completion infrastructure to check whether a text just typed
     * into a text component triggers an automatic query invocation.
     * <br>
     * If the particular query type is returned the infrastructure
     * will then call {@link #createTask(int, JTextComponent)}.
     *
     * @param component a component in which typing appeared
     * @param typedText a typed text 
     *
     * @return a combination of the {@link #COMPLETION_QUERY_TYPE},
     *         {@link #DOCUMENTATION_QUERY_TYPE}, and {@link #TOOLTIP_QUERY_TYPE}
     *         values, or zero if no query should be automatically invoked.
     */
    public int getAutoQueryTypes(JTextComponent component, String typedText);

}

