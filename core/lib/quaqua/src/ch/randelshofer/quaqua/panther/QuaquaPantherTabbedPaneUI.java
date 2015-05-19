/*
 * @(#)QuaquaPantherTabbedPaneUI.java
 *
 * Copyright (c) 2006-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.panther;

import ch.randelshofer.quaqua.util.NavigatableTabbedPaneUI;
import ch.randelshofer.quaqua.QuaquaManager;
import ch.randelshofer.quaqua.jaguar.*;
import java.awt.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
/**
 * The QuaquaPantherTabbedPaneUI uses to the QuaquaJaguarTabbedPaneUI for
 * the WRAP_TAB_LAYOUT policy and the QuaquaPantherScrollTabbedPaneUI for
 * the SCROLL_TAB_LAYOUT policy.
 * 
 * @author Werner Randelshofer
 * @version $Id: QuaquaPantherTabbedPaneUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaPantherTabbedPaneUI extends TabbedPaneUI
        implements NavigatableTabbedPaneUI {
    private JTabbedPane tabPane;
    private TabbedPaneUI currentUI;
    private PropertyChangeListener propertyChangeListener;
    //private FocusListener focusListener;
    
    /**
     * Creates a new instance.
     */
    public QuaquaPantherTabbedPaneUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
	return new QuaquaPantherTabbedPaneUI();
    }

    public void installUI(JComponent c) {
        this.tabPane = (JTabbedPane) c;
        
        // Tag the tabbed pane with a client property in order to prevent
        // that we are getting in an endless loop, when the layout policy
        // of the tabbed pane is changed.
        if (tabPane.getClientProperty("Quaqua.TabbedPane.tabLayoutPolicy") == null) {
            tabPane.putClientProperty("Quaqua.TabbedPane.tabLayoutPolicy", UIManager.get("TabbedPane.tabLayoutPolicy"));
            tabPane.setTabLayoutPolicy(UIManager.getInt("TabbedPane.tabLayoutPolicy"));
        }
        
        
        if (tabPane.getTabLayoutPolicy() == JTabbedPane.WRAP_TAB_LAYOUT) {
           QuaquaJaguarTabbedPaneUI qjtpui = (QuaquaJaguarTabbedPaneUI) QuaquaJaguarTabbedPaneUI.createUI(c);
           qjtpui.setPropertyPrefix("TabbedPane.wrap.");
           currentUI = qjtpui;
            
        } else {
           QuaquaPantherScrollTabbedPaneUI qptpui = (QuaquaPantherScrollTabbedPaneUI) QuaquaPantherScrollTabbedPaneUI.createUI(c);
           qptpui.setPropertyPrefix("TabbedPane.scroll.");
           currentUI = qptpui;
        }
        currentUI.installUI(c);
        
        tabPane.setRequestFocusEnabled(UIManager.getBoolean("TabbedPane.requestFocusEnabled"));
        
	//installComponents();
        //installDefaults(); 
        installListeners();
        //installKeyboardActions();
    }

    public void uninstallUI(JComponent c) {
        //uninstallKeyboardActions();
        uninstallListeners();
        //uninstallDefaults();
	//uninstallComponents();

        if (currentUI != null) {
            currentUI.uninstallUI(c);
        }
        
        this.tabPane = null;
    }
    
    protected void installListeners() {
        if ((propertyChangeListener = createPropertyChangeListener()) != null) {
            tabPane.addPropertyChangeListener(propertyChangeListener);
        }
      /*  
        if ((focusListener = createFocusListener()) != null) {
            tabPane.addFocusListener(focusListener);
        }*/
    }

    protected void uninstallListeners() {
        if (propertyChangeListener != null) {
            tabPane.removePropertyChangeListener(propertyChangeListener);
            propertyChangeListener = null;
        }
        /*
        if (focusListener != null) {
            tabPane.removeFocusListener(focusListener);
            focusListener = null;
        }*/
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return new PropertyChangeHandler();
    }
    /*
    protected FocusListener createFocusListener() {
        return new FocusHandler();
    }*/
    

    public Rectangle getTabBounds(JTabbedPane pane, int index) {
        return currentUI.getTabBounds(pane, index);
    }
    
    public int getTabRunCount(JTabbedPane pane) {
        return currentUI.getTabRunCount(pane);
    }
    
    public int tabForCoordinate(JTabbedPane pane, int x, int y) {
        return currentUI.tabForCoordinate(pane, x, y);
    }
    public void paint(Graphics g, JComponent c) {
        currentUI.paint(g, c);
    }
    public void navigateSelectedTab(int direction) {
        ((NavigatableTabbedPaneUI) currentUI).navigateSelectedTab(direction);
    }

    public Integer getIndexForMnemonic(int mnemonic) {
        return ((NavigatableTabbedPaneUI) currentUI).getIndexForMnemonic(mnemonic);
    }

    public boolean requestFocusForVisibleComponent() {
       return ((NavigatableTabbedPaneUI) currentUI).requestFocusForVisibleComponent();
    }
    
    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTabbedPaneUI.
     */  
    public class PropertyChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
	    JTabbedPane pane = (JTabbedPane)e.getSource();
	    String name = e.getPropertyName();
            if (name.equals("tabLayoutPolicy")) {
	        QuaquaPantherTabbedPaneUI.this.uninstallUI(pane);
		QuaquaPantherTabbedPaneUI.this.installUI(pane);
	    }
	}
    }
}
