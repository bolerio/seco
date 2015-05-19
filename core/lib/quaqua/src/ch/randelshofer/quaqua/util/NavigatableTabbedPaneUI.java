/*
 * @(#)NavigatableTabbedPaneUI.java  1.0  September 4, 2006
 *
 * Copyright (c) 2006-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.util;

/**
 * NavigatableTabbedPaneUI.
 *
 * @author Werner Randelshofer
 * @version 1.0 September 4, 2006 Created.
 */
public interface NavigatableTabbedPaneUI {
    /** Tab Navigation methods. */
    public void navigateSelectedTab(int direction);
    public boolean requestFocusForVisibleComponent();
    public Integer getIndexForMnemonic(int mnemonic);
}
