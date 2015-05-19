/*
 * @(#)BasicLookAndFeelImpl.java  1.0  2008-11-17
 *
 * Copyright (c) 2008 Werner Randelshofer, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Werner Randelshofer. For details see accompanying license terms.
 */

package test;

import javax.swing.plaf.basic.BasicLookAndFeel;

/**
 * BasicLookAndFeelImpl.
 *
 * Note: This class must be Java 1.4 compatible.
 *
 * @author Werner Randelshofer, 
 * @version 1.0 2008-11-17 Created.
 */
public class BasicLookAndFeelImpl extends BasicLookAndFeel {

    //@Override
    public String getName() {
        return "Basic";
    }

    //@Override
    public String getID() {
        return "Basic";
    }

    //@Override
    public String getDescription() {
        return "A basic look and feel";
    }

    //@Override
    public boolean isNativeLookAndFeel() {
        return false;
    }

    //@Override
    public boolean isSupportedLookAndFeel() {
        return true;
    }

}
