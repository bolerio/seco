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

import java.awt.Color;
import java.awt.Dimension;

/**
* Initializer for the extended editor settings.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtSettingsDefaults //extends SettingsDefaults 
{

    // Highlight row with caret coloring
    public static final Color defaultHighlightCaretRowBackColor = new Color(255, 255, 220);
    // Highlight matching brace coloring
    public static final Color defaultHighlightMatchBraceForeColor = Color.white;
    public static final Color defaultHighlightMatchBraceBackColor = new Color(255, 50, 210);
    //public static final Coloring defaultHighlightMatchBraceColoring
    //= new Coloring(null, defaultHighlightMatchBraceForeColor, defaultHighlightMatchBraceBackColor);

    public static final Boolean defaultHighlightCaretRow = Boolean.TRUE;
    public static final Boolean defaultHighlightMatchBrace = Boolean.TRUE;
    public static final Integer defaultHighlightMatchBraceDelay = new Integer(100);
    public static final Boolean defaultCaretSimpleMatchBrace = Boolean.TRUE;

    public static final Boolean defaultCompletionAutoPopup = Boolean.TRUE;
    public static final Boolean defaultCompletionCaseSensitive = Boolean.FALSE;
    public static final Boolean defaultCompletionNaturalSort = Boolean.FALSE;    
    public static final Integer defaultCompletionAutoPopupDelay = new Integer(250);
    public static final Integer defaultCompletionRefreshDelay = new Integer(200);
    public static final Dimension defaultCompletionPaneMaxSize = new Dimension(400, 300);
    public static final Dimension defaultCompletionPaneMinSize = new Dimension(60, 17);
    public static final Boolean defaultFastImportPackage = Boolean.FALSE;    
    public static final Integer defaultFastImportSelection = new Integer(0);
    public static final Boolean defaultShowDeprecatedMembers = Boolean.TRUE;
    public static final Boolean defaultCompletionInstantSubstitution = Boolean.TRUE;    
    
    public static final Color defaultJavaDocBGColor = new Color(247, 247, 255);
    public static final Integer defaultJavaDocAutoPopupDelay = new Integer(200);
    public static final Dimension defaultJavaDocPreferredSize = new Dimension(500, 300);    
    public static final Boolean defaultJavaDocAutoPopup = Boolean.TRUE;
    private static int MENU_MASK = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

}

