/*
 * @(#)CompositeVisualMarginBorder.java  1.0  2011-07-29
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.border;

import ch.randelshofer.quaqua.DefaultColumnCellRenderer.UIResource;
import ch.randelshofer.quaqua.VisualMargin;
import ch.randelshofer.quaqua.util.InsetsUtil;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.InsetsUIResource;

/**
 * {@code CompositeVisualMarginBorder}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-07-29 Created.
 */
public class CompositeVisualMarginBorder implements Border, VisualMargin {
    private Border actualBorder;
    private Insets borderMargin;
     private boolean isTopFixed, isLeftFixed, isBottomFixed, isRightFixed;
  /**
     * The UIManager Property to be used for the default margin.
     */
    private String uiManagerPropertyName = "Component.visualMargin";
    /**
     * The Client Property to be used for the default margin.
     */
    private String propertyName = "Quaqua.Component.visualMargin";
    
    /** Creates a new instance which draws {@code actualBorder} which has
     * a visual margin of {@code  top,left,bottom,right}.
     * 
     * @param actualBorder
     * @param top
     * @param left
     * @param bottom
     * @param right 
     */
    public CompositeVisualMarginBorder(Border actualBorder, int top, int left, int bottom, int right) {
        this(actualBorder,new Insets(top,left,bottom,right));
    }
    public CompositeVisualMarginBorder(Border actualBorder, int top, int left, int bottom, int right,
            boolean isTopFixed,boolean isLeftFixed,boolean isBottomFixed,boolean isRightFixed) {
        this(actualBorder,new Insets(top,left,bottom,right),isTopFixed,isLeftFixed,isBottomFixed,isRightFixed);
    }
    /** Creates a new instance which draws {@code actualBorder} which has
     * a visual margin of {@code visualMargin}.
     * 
     * @param actualBorder
     * @param visualMargin
     */
    public CompositeVisualMarginBorder(Border actualBorder, Insets visualMargin) {
        this(actualBorder,visualMargin,false,false,false,false);
    }
    public CompositeVisualMarginBorder(Border actualBorder, Insets visualMargin, boolean isTopFixed,boolean isLeftFixed,boolean isBottomFixed,boolean isRightFixed) {
        this.actualBorder=actualBorder;this.borderMargin=visualMargin;
        this.isTopFixed=isTopFixed;
        this.isLeftFixed=isLeftFixed;
        this.isBottomFixed=isBottomFixed;
        this.isRightFixed=isRightFixed;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Insets vm=getVisualMargin(c, new Insets(0,0,0,0));
        actualBorder.paintBorder(c,g,//
                x+vm.left-borderMargin.left,//
                y+vm.top-borderMargin.top,//
                width-vm.left-vm.right+borderMargin.left+borderMargin.right,//
                height-vm.top-vm.bottom+borderMargin.top+borderMargin.bottom);
        //actualBorder.paintBorder(c,g,x,y,width,height);
    }   
    public boolean isBorderOpaque() {
        return false;
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
   
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets ins= getVisualMargin(c, insets);
        Insets bi=actualBorder.getBorderInsets(c);
        
        InsetsUtil.addTo(bi,ins);
        return ins;
    }
    
      public final Insets getVisualMargin(Component c) {
        return getVisualMargin(c, new Insets(0, 0, 0, 0));
    }
    
    /**
     * Reinitializes the insets parameter with this Border's current Insets.
     * @param c the component for which this border insets value applies
     * @param insets the object to be reinitialized
     * @return the <code>insets</code> object
     */
    protected Insets getVisualMargin(Component c, Insets insets) {
        /*insets.top = -visualMargin.top;
        insets.left = -visualMargin.left;
        insets.bottom = -visualMargin.bottom;
        insets.right = -visualMargin.right;
        */
        InsetsUtil.clear(insets);
        if (c instanceof JComponent) {
            Insets componentMargin = (Insets) ((JComponent) c).getClientProperty(propertyName);
            if (componentMargin == null && propertyName != null) {
                componentMargin = UIManager.getInsets(uiManagerPropertyName);
            }
            if (componentMargin != null) {
                if (! isTopFixed) insets.top += componentMargin.top;
                if (! isLeftFixed) insets.left += componentMargin.left;
                if (! isBottomFixed) insets.bottom += componentMargin.bottom;
                if (! isRightFixed) insets.right += componentMargin.right;
            }
        }
        return insets;
    }
}
