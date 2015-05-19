/*
 * @(#)LookAndFeelProxy.java
 *
 * Copyright (c) 2003-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
/**
 * A proxy for LookAndFeel objects. This class enables us to override
 * the behavior of LookAndFeel objects without subclassing them.
 * <p>
 * <b>Note</b> this class extends BasicLookAndFeel instead of
 * LookAndFeel. This is because some UI classes derived from the Basic LAF
 * don't work if they can't cast the current LookAndFeel to BasicLookAndFeel.
 *
 * @author Werner Randelshofer, Switzerland
 * @version $Id: LookAndFeelProxy.java 464 2014-03-22 12:32:00Z wrandelshofer $
 */
public class LookAndFeelProxy extends BasicLookAndFeel {
    /**
     * The target LookAndFeel.
     */
    protected LookAndFeel target;
    
    /**
     * Creates a new instance which proxies the supplied target.
     * @param target the target
     */
    public LookAndFeelProxy(LookAndFeel target) {
        this.target = target;
    }
    /**
     * Creates a new instance with a null target.
     */
    protected LookAndFeelProxy() {
    }
    
    /**
     * Sets the target of this proxy.
     * @param target the target
     */
    protected void setTarget(LookAndFeel target) {
        this.target = target;
    }
    
    /** 
     * Return a one line description of this look and feel implementation, 
     * e.g. "The CDE/Motif Look and Feel".   This string is intended for 
     * the user, e.g. in the title of a window or in a ToolTip message.
     */
    public String getDescription() {
        return target.getDescription();
    }
    
    /**
     * Return a string that identifies this look and feel.  This string 
     * will be used by applications/services that want to recognize
     * well known look and feel implementations.  Presently
     * the well known names are "Motif", "Windows", "Mac", "Metal".  Note 
     * that a LookAndFeel derived from a well known superclass 
     * that doesn't make any fundamental changes to the look or feel 
     * shouldn't override this method.
     */
    public String getID() {
        return target.getID();
    }
    
    /**
     * Return a short string that identifies this look and feel, e.g.
     * "CDE/Motif".  This string should be appropriate for a menu item.
     * Distinct look and feels should have different names, e.g. 
     * a subclass of MotifLookAndFeel that changes the way a few components
     * are rendered should be called "CDE/Motif My Way"; something
     * that would be useful to a user trying to select a L&amp;F from a list
     * of names.
     */
    public String getName() {
        return target.getName();
    }
    
    /**
     * If the underlying platform has a "native" look and feel, and this
     * is an implementation of it, return true.  For example a CDE/Motif
     * look and implementation would return true when the underlying 
     * platform was Solaris.
     */
    public boolean isNativeLookAndFeel() {
        return target.isNativeLookAndFeel();
    }
    
    /**
     * Return true if the underlying platform supports and or permits
     * this look and feel.  This method returns false if the look 
     * and feel depends on special resources or legal agreements that
     * aren't defined for the current platform.  
     * 
     * @see UIManager#setLookAndFeel
     */
    public boolean isSupportedLookAndFeel() {
        return target.isSupportedLookAndFeel();
    }
    
    /**
     * Invoked when the user attempts an invalid operation, 
     * such as pasting into an uneditable <code>JTextField</code> 
     * that has focus. The default implementation beeps. Subclasses 
     * that wish different behavior should override this and provide 
     * the additional feedback.
     *
     * @param component Component the error occured in, may be null 
     *			indicating the error condition is not directly 
     *			associated with a <code>Component</code>.
     */
    @Override
    public void provideErrorFeedback(Component component) {
        try {
        Methods.invoke(target, "provideErrorFeedback", Component.class, component);
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.getMessage());
        }
    }
    
    /**
     * Returns true if the <code>LookAndFeel</code> returned
     * <code>RootPaneUI</code> instances support providing Window decorations
     * in a <code>JRootPane</code>.
     * <p>
     * The default implementation returns false, subclasses that support
     * Window decorations should override this and return true.
     *
     * @return True if the RootPaneUI instances created support client side
     *              decorations
     * @see JDialog#setDefaultLookAndFeelDecorated
     * @see JFrame#setDefaultLookAndFeelDecorated
     * @see JRootPane#setWindowDecorationStyle
     * @since 1.4
     */
    @Override
    public boolean getSupportsWindowDecorations() {
        try {
        return ((Boolean) Methods.invoke(target, "getSupportsWindowDecorations")).booleanValue();
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.getMessage());
        }
    }
    
    /**
     * UIManager.setLookAndFeel calls this method before the first
     * call (and typically the only call) to getDefaults().  Subclasses
     * should do any one-time setup they need here, rather than 
     * in a static initializer, because look and feel class objects
     * may be loaded just to discover that isSupportedLookAndFeel()
     * returns false.
     *
     * @see #uninitialize
     * @see UIManager#setLookAndFeel
     */
    @Override
    public void initialize() {
        target.initialize();
    }


    /**
     * UIManager.setLookAndFeel calls this method just before we're
     * replaced by a new default look and feel.   Subclasses may 
     * choose to free up some resources here.
     *
     * @see #initialize
     */
    @Override
    public void uninitialize() {
        target.uninitialize();
    }

    /**
     * This method is called once by UIManager.setLookAndFeel to create
     * the look and feel specific defaults table.  Other applications,
     * for example an application builder, may also call this method.
     *
     * @see #initialize
     * @see #uninitialize
     * @see UIManager#setLookAndFeel
     */
    @Override
    public UIDefaults getDefaults() {
        return target.getDefaults();
    }

    @Override
    public LayoutStyle getLayoutStyle() {
        return target.getLayoutStyle();
    }
}    