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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import seco.gui.GUIHelper;
import seco.gui.TopFrame;


/**
 * Completion popup - either completion, documentation or tooltip
 * popup implementations.
 *
 *  @author Dusan Balek, Miloslav Metelka
 */
abstract class CompletionLayoutPopup {
    
    private CompletionLayout layout;
    
    private Popup popup;
    
    /** Bounds at which the visible popup has. */
    private Rectangle popupBounds;

    private JComponent contentComponent;
    
    private int anchorOffset;
    
    private Rectangle anchorOffsetBounds;
    
    private boolean displayAboveCaret;
    
    private Rectangle screenBounds;
    
    private boolean preferDisplayAboveCaret;
    
    private boolean showRetainedPreferredSize;
    
    public final boolean isVisible() {
        return (popup != null);
    }
    
    public final boolean isActive() {
        return (contentComponent != null);
    }
    
    public final void hide() {
        if (isVisible()) {
            popup.hide();
            popup = null;
            popupBounds = null;
            contentComponent = null;
            anchorOffset = -1;
            // Reset screen bounds as well to not cache too long
            screenBounds = null;
        }
    }
    
    public final boolean isDisplayAboveCaret() {
        return displayAboveCaret;
    }
    
    public final Rectangle getPopupBounds() {
        return popupBounds;
    }
    
    final void setLayout(CompletionLayout layout) {
        assert (layout != null);
        this.layout = layout;
    }
    
    final void setPreferDisplayAboveCaret(boolean preferDisplayAboveCaret) {
        this.preferDisplayAboveCaret = preferDisplayAboveCaret;
    }
    
    final void setContentComponent(JComponent contentComponent) {
        assert (contentComponent != null);
        this.contentComponent = contentComponent;
    }
    
    final void setAnchorOffset(int anchorOffset) {
        this.anchorOffset = anchorOffset;
        anchorOffsetBounds = null;
    }
    
    final Rectangle getScreenBounds() {
        if (screenBounds == null) {
	    JTextComponent editorComponent = getEditorComponent();
            screenBounds = (editorComponent != null)
		? editorComponent.getGraphicsConfiguration().getBounds()
		: new Rectangle();
        }
        return screenBounds;
    }
    
    final int getAnchorOffset() {
	int offset = anchorOffset;
	if (offset == -1) {
	    // Get caret position
	    JTextComponent editorComponent = getEditorComponent();
	    if (editorComponent != null) {
		offset = editorComponent.getCaretPosition();
	    }
	}
	return offset;
    }
    
    final JComponent getContentComponent() {
        return contentComponent;
    }
    
    final Dimension getPreferredSize() {
        JComponent comp = getContentComponent();
        return (comp == null) ? new Dimension(0,0) : comp.getPreferredSize();
    }
    
    final void resetPreferredSize() {
        JComponent comp = getContentComponent();
        if (comp == null){
            return;
        }
        comp.setPreferredSize(null);
    }
    
    final boolean isShowRetainedPreferredSize() {
        return showRetainedPreferredSize;
    }
    
    final CompletionLayout getLayout() {
        return layout;
    }
    
    final JTextComponent getEditorComponent() {
        return layout.getEditorComponent();
    }
    
    protected int getAnchorHorizontalShift() {
        return 0;
    }

    final Rectangle getAnchorOffsetBounds() {
	JTextComponent editorComponent = getEditorComponent();
	if (editorComponent == null) {
	    return new Rectangle();
	}
        if (anchorOffsetBounds == null){ 
            int anchorOffset = getAnchorOffset();
            try {
                anchorOffsetBounds = editorComponent.modelToView(anchorOffset);
                if (anchorOffsetBounds != null){
                    anchorOffsetBounds.x -= getAnchorHorizontalShift();
                } else {
                    anchorOffsetBounds = new Rectangle(); // use empty rectangle
                }
            } catch (BadLocationException e) {
                anchorOffsetBounds = new Rectangle(); // use empty rectangle
            }
            Point anchorOffsetPoint = anchorOffsetBounds.getLocation();
            SwingUtilities.convertPointToScreen(anchorOffsetPoint, editorComponent);
            anchorOffsetBounds.setLocation(anchorOffsetPoint);
        }
        return anchorOffsetBounds;
    }
    
    final Popup getPopup() {
        return popup;
    }
    
    /**
     * Find bounds of the popup based on knowledge of the preferred size
     * of the content component and the preference of the displaying
     * of the popup either above or below the occupied bounds.
     *
     * @param occupiedBounds bounds of the rectangle above or below which
     *   the bounds should be found.
     * @param aboveOccupiedBounds whether the bounds should be found for position
     *   above or below the occupied bounds.
     * @return rectangle with absolute screen bounds of the popup.
     */
    private Rectangle findPopupBounds(Rectangle occupiedBounds, boolean aboveOccupiedBounds) {
        Dimension prefSize = getPreferredSize();
        Rectangle screen = getScreenBounds();
        Rectangle popupBounds = new Rectangle();
        
        popupBounds.x = Math.min(occupiedBounds.x,
                (screen.x + screen.width) - prefSize.width);
        popupBounds.x = Math.max(popupBounds.x, screen.x);
        popupBounds.width = Math.min(prefSize.width, screen.width);
        
        if (aboveOccupiedBounds) {
            popupBounds.height = Math.min(prefSize.height,
                    occupiedBounds.y - screen.y - CompletionLayout.POPUP_VERTICAL_GAP);
            popupBounds.y = occupiedBounds.y - CompletionLayout.POPUP_VERTICAL_GAP - popupBounds.height;
        } else { // below caret
            popupBounds.y = occupiedBounds.y
                    + occupiedBounds.height + CompletionLayout.POPUP_VERTICAL_GAP;
            popupBounds.height = Math.min(prefSize.height,
                    (screen.y + screen.height) - popupBounds.y);
        }
        
        correctBounds(popupBounds);
        return popupBounds;
    }
    
    protected void correctBounds(Rectangle r)
    {
        if(TopFrame.getInstance().getCanvas() == null) return;
        Point pt = new Point(r.x, r.y);
            //SwingUtilities.convertPoint(getEditorComponent(), r.x,
             //   r.y, TopFrame.getInstance());
        Point corr = GUIHelper.computePoint(getEditorComponent(), pt);
        //System.out.println("correctBounds: " + corr);
        r.x = corr.x; r.y = corr.y;
    }
    
    /**
     * Create and display the popup at the given bounds.
     *
     * @param popupBounds location and size of the popup.
     * @param displayAboveCaret whether the popup is displayed above the anchor
     *  bounds or below them (it does not be right above them).
     */
    private void show(Rectangle popupBounds, boolean displayAboveCaret) {
        // Hide the original popup if exists
        if (popup != null) {
            popup.hide();
            popup = null;
        }
        
        // Explicitly set the preferred size
        Dimension origPrefSize = getPreferredSize();
        Dimension newPrefSize = popupBounds.getSize();
        JComponent contComp = getContentComponent();
        if (contComp == null){
            return;
        }
        contComp.setPreferredSize(newPrefSize);
        showRetainedPreferredSize = newPrefSize.equals(origPrefSize);

        PopupFactory factory = PopupFactory.getSharedInstance();
        // Create popup without explicit parent window
        popup = factory.getPopup(null, contComp,
                popupBounds.x, popupBounds.y);
        popup.show();

        this.popupBounds = popupBounds;
        this.displayAboveCaret = displayAboveCaret;
    }
    
    /**
     * Show the popup along the anchor bounds and take
     * the preferred location (above or below caret) into account.
     */
    void showAlongAnchorBounds() {
        showAlongOccupiedBounds(getAnchorOffsetBounds());
    }
    
    void showAlongAnchorBounds(boolean aboveCaret) {
        showAlongOccupiedBounds(getAnchorOffsetBounds(), aboveCaret);
    }
    
    /**
     * Show the popup along the anchor bounds and take
     * the preferred location (above or below caret) into account.
     */
    void showAlongOccupiedBounds(Rectangle occupiedBounds) {
        boolean aboveCaret;
        if (isEnoughSpace(occupiedBounds, preferDisplayAboveCaret)) {
            aboveCaret = preferDisplayAboveCaret;
        } else { // not enough space at preferred location
            // Choose the location with more space
            aboveCaret = isMoreSpaceAbove(occupiedBounds);
        }
        Rectangle bounds = findPopupBounds(occupiedBounds, aboveCaret);
        show(bounds, aboveCaret);
    }
    
    void showAlongOccupiedBounds(Rectangle occupiedBounds, boolean aboveCaret) {
        Rectangle bounds = findPopupBounds(occupiedBounds, aboveCaret);
        show(bounds, aboveCaret);
    }
    
    boolean isMoreSpaceAbove(Rectangle bounds) {
        Rectangle screen = getScreenBounds();
        int above = bounds.y - screen.y;
        int below = (screen.y + screen.height) - (bounds.y + bounds.height);
        return (above > below);
    }
    
    /**
     * Check whether there is enough space for this popup
     * on its preferred location related to caret.
     */
    boolean isEnoughSpace(Rectangle occupiedBounds) {
        return isEnoughSpace(occupiedBounds, preferDisplayAboveCaret);
    }
    
    /**
     * Check whether there is enough space for this popup above
     * or below the given occupied bounds.
     * 
     * @param occupiedBounds bounds above or below which the available
     *  space should be determined.
     * @param aboveOccupiedBounds whether the space should be checked above
     *  or below the occupiedBounds.
     * @return true if there is enough space for the preferred size of this popup
     *  on the requested side or false if not.
     */
    boolean isEnoughSpace(Rectangle occupiedBounds, boolean aboveOccupiedBounds) {
        Rectangle screen = getScreenBounds();
        int freeHeight = aboveOccupiedBounds
            ? occupiedBounds.y - screen.y
            : (screen.y + screen.height) - (occupiedBounds.y + occupiedBounds.height);
        Dimension prefSize = getPreferredSize();
        return (prefSize.height < freeHeight);
    }
    
    boolean isEnoughSpace(boolean aboveCaret) {
        return isEnoughSpace(getAnchorOffsetBounds(), aboveCaret);
    }
    
    public boolean isOverlapped(Rectangle bounds) {
        return isVisible() ? popupBounds.intersects(bounds) : false;
    }

    public boolean isOverlapped(CompletionLayoutPopup popup) {
        return popup.isVisible() ? isOverlapped(popup.getPopupBounds()) : false;
    }
    
    public Rectangle unionBounds(Rectangle bounds) {
        return isVisible() ? bounds.union(getPopupBounds()) : bounds;
    }

    public abstract void processKeyEvent(KeyEvent evt);
}
