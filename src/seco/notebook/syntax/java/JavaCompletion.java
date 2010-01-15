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

package seco.notebook.syntax.java;

import java.lang.reflect.Modifier;

import seco.notebook.syntax.completion.Completion;


/**
* Java completion query specifications
*
* @author Miloslav Metelka
* @version 1.00
*/

abstract public class JavaCompletion extends Completion {

    public static final int PUBLIC_LEVEL = 3;
    public static final int PROTECTED_LEVEL = 2;
    public static final int PACKAGE_LEVEL = 1;
    public static final int PRIVATE_LEVEL = 0;
    
//  the bit for local member. the modificator is not saved within this bit.
    public static final int LOCAL_MEMBER_BIT = (1 << 29);

    // the bit for deprecated flag. it is saved to copde completion  DB
    public static final int DEPRECATED_BIT = (1 << 20);

    public static int getLevel(int modifiers) {
        if ((modifiers & Modifier.PUBLIC) != 0) {
            return PUBLIC_LEVEL;
        } else if ((modifiers & Modifier.PROTECTED) != 0) {
            return PROTECTED_LEVEL;
        } else if ((modifiers & Modifier.PRIVATE) == 0) {
            return PACKAGE_LEVEL;
        } else {
            return PRIVATE_LEVEL;
        }
    }
}
