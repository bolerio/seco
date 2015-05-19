/*
 * @(#)VisualMarginBorder.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.border;

import ch.randelshofer.quaqua.VisualMargin;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;

/**
 * The VisualMarginBorder is used to visually align components using bounds
 * based on other criterias than the clip bounds of the component.
 *
 * For example: The clip bounds of a JButton includes its cast shadow and its
 * focus ring. When we align the JButton with a JLabel, we want to align the
 * baseline of the Text of the JButton with the text in the JLabel.
 *
 * The visual margin may be quite large. We allow to programmatically set a
 * smaller margin using the client property "Quaqua.Component.margin".
 *
 * @author  Werner Randelshofer
 * @version $Id: VisualMarginBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class VisualMarginBorder extends AbstractBorder implements UIResource,VisualMargin,PressedCueBorder {
    /**
     * Defines the margin from the clip bounds of the
     * component to its visually perceived borderline.
     */
    private Insets layoutMargin;
    
    /**
     * The UIManager Property to be used for the default margin.
     */
    private String uiManagerPropertyName = "Component.visualMargin";
    /**
     * The Client Property to be used for the default margin.
     */
    private String propertyName = "Quaqua.Component.visualMargin";
    
    private boolean isTopFixed, isLeftFixed, isBottomFixed, isRightFixed;
    
    /**
     * Creates a new VisualMarginBorder.
     */
    public VisualMarginBorder() {
        layoutMargin = new Insets(0, 0, 0, 0);
    }
    
    /**
     * Creates a new VisualMarginBorder.
     *
     * @param top Defines the margin from the clip bounds of the
     * component to its visual bounds.
     * @param left Defines the margin from the clip bounds of the
     * component to its visual bounds.
     * @param bottom Defines the margin from the clip bounds of the
     * component to its visual bounds.
     * @param right Defines the margin from the clip bounds of the
     * component to its visual bounds.
     */
    public VisualMarginBorder(int top, int left, int bottom, int right) {
        layoutMargin = new Insets(top, left, bottom, right);
    }
    public VisualMarginBorder(int top, int left, int bottom, int right, boolean ftop, boolean fleft, boolean fbottom, boolean fright) {
        layoutMargin = new Insets(top, left, bottom, right);
        isTopFixed = ftop;
        isLeftFixed = fleft;
        isBottomFixed = fbottom;
        isRightFixed = fright;
    }
    public VisualMarginBorder(boolean ftop, boolean fleft, boolean fbottom, boolean fright) {
        layoutMargin = new Insets(0, 0, 0, 0);
        isTopFixed = ftop;
        isLeftFixed = fleft;
        isBottomFixed = fbottom;
        isRightFixed = fright;
    }
    /**
     * Creates a new VisualMarginBorder.
     *
     * @param layoutMargin Defines the margin from the clip bounds of the
     * component to its visual bounds. The margin has usually negative values!
     */
    public VisualMarginBorder(Insets layoutMargin) {
        this.layoutMargin = layoutMargin;
    }
    /**
     * The UIManager Property to be used for the default margin.
     */
    public void setPropertyName(String propertyName) {
        //  this.propertyName = propertyName;
    }
    
    /*
     * Specifies SwingConstants.TOP, LEFT, BOTTOM, RIGHT to be fixed.
     * Set to false to unfix.
     */
    public void setFixed(boolean top, boolean left, boolean bottom, boolean right) {
        isTopFixed = top;
        isLeftFixed = left;
        isBottomFixed = bottom;
        isRightFixed = right;
    }
    
    public final Insets getVisualMargin(Component c) {
        return getVisualMargin(c, new Insets(0, 0, 0, 0));
    }
    
    @Override
    public final Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }
    
    /**
     * Reinitializes the insets parameter with this Border's current Insets.
     * @param c the component for which this border insets value applies
     * @param insets the object to be reinitialized
     * @return the <code>insets</code> object
     */
    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        return getVisualMargin(c, insets);
    }
    /**
     * Reinitializes the insets parameter with this Border's current Insets.
     * @param c the component for which this border insets value applies
     * @param insets the object to be reinitialized
     * @return the <code>insets</code> object
     */
    protected Insets getVisualMargin(Component c, Insets insets) {
        insets.top = -layoutMargin.top;
        insets.left = -layoutMargin.left;
        insets.bottom = -layoutMargin.bottom;
        insets.right = -layoutMargin.right;
        
        boolean isUIResource=false;
        
        if (c instanceof JComponent) {
            Insets componentMargin = (Insets) ((JComponent) c).getClientProperty(propertyName);
            if (componentMargin == null && propertyName != null) {
                componentMargin = UIManager.getInsets(uiManagerPropertyName);
                isUIResource=true;
            } else {
                isUIResource=componentMargin instanceof UIResource;
            }
            if (componentMargin != null) {
                if (! isTopFixed) insets.top += componentMargin.top;
                if (! isLeftFixed) insets.left += componentMargin.left;
                if (! isBottomFixed) insets.bottom += componentMargin.bottom;
                if (! isRightFixed) insets.right += componentMargin.right;
            }
        }
        if (isUIResource) {
            return new InsetsUIResource(insets.top, insets.left, insets.bottom, insets.right);
        }
        
        return insets;
    }

    public boolean hasPressedCue(JComponent c) {
        return false;
    }
}
