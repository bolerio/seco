/*
 * @(#)QuaquaDragRecognitionSupport.java  
 *
 * Copyright (c) 2007-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.Methods;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.event.*;
import java.awt.dnd.DragSource;
import javax.swing.*;
//import sun.awt.dnd.SunDragSourceContextPeer;

/**
 * QuaquaDragRecognitionSupport is mostly a copy of
 * javax.swing.plaf.basic.DragRecognitionSupport 1.1 05/05/02 by
 * Shannon Hickey.
 * This probably violates some licenses, but I don't know how
 * to change the drag and drop behavior of a TreeUI otherwise. :(
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaDragRecognitionSupport.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */


class QuaquaDragRecognitionSupport {
    private int motionThreshold;
    private MouseEvent dndArmedEvent;
    private JComponent component;
    private static QuaquaDragRecognitionSupport instance;
    
    /**
     * This interface allows us to pass in a handler to mouseDragged,
     * so that we can be notified immediately before a drag begins.
     */
    public static interface BeforeDrag {
        public void dragStarting(MouseEvent me);
    }
    
    /**
     * Returns the QuaquaDragRecognitionSupport for the caller's AppContext.
     */
    private static QuaquaDragRecognitionSupport getDragRecognitionSupport() {
        if (instance == null) {
            instance = new QuaquaDragRecognitionSupport();
        }
        return instance;
    }
    
    /**
     * Returns whether or not the event is potentially part of a drag sequence.
     */
    public static boolean mousePressed(MouseEvent me) {
        return ((QuaquaDragRecognitionSupport)getDragRecognitionSupport()).
                mousePressedImpl(me);
    }
    
    /**
     * If a dnd recognition has been going on, return the MouseEvent
     * that started the recognition. Otherwise, return null.
     */
    public static MouseEvent mouseReleased(MouseEvent me) {
        return ((QuaquaDragRecognitionSupport)getDragRecognitionSupport()).
                mouseReleasedImpl(me);
    }
    
    /**
     * Returns whether or not a drag gesture recognition is ongoing.
     */
    public static boolean mouseDragged(MouseEvent me, BeforeDrag bd) {
        return ((QuaquaDragRecognitionSupport)getDragRecognitionSupport()).
                mouseDraggedImpl(me, bd);
    }
    
    private void clearState() {
        dndArmedEvent = null;
        component = null;
    }
    
    private int mapDragOperationFromModifiers(MouseEvent me,
            TransferHandler th) {
        
        if (th == null || !SwingUtilities.isLeftMouseButton(me)) {
            return TransferHandler.NONE;
        }
        
        return convertModifiersToDropAction(me.getModifiersEx(),
                th.getSourceActions(component));
    }
    
    
    /**
     * Returns whether or not the event is potentially part of a drag sequence.
     */
    private boolean mousePressedImpl(MouseEvent me) {
        component = (JComponent)me.getSource();
        
        if (mapDragOperationFromModifiers(me, component.getTransferHandler())
        != TransferHandler.NONE) {
            try {
                //motionThreshold = DragSource.getDragThreshold();
                motionThreshold = ((Integer) Methods.invokeStatic(DragSource.class,"getDragThreshold")).intValue();
            } catch (NoSuchMethodException ex) {
                Integer td = (Integer)Toolkit.getDefaultToolkit().
                        getDesktopProperty("DnD.gestureMotionThreshold");
                if (td != null) {
                    motionThreshold = td.intValue();
                }
            }
            dndArmedEvent = me;
            return true;
        }
        
        clearState();
        return false;
    }
    
    /**
     * If a dnd recognition has been going on, return the MouseEvent
     * that started the recognition. Otherwise, return null.
     */
    private MouseEvent mouseReleasedImpl(MouseEvent me) {
        /* no recognition has been going on */
        if (dndArmedEvent == null) {
            return null;
        }
        
        MouseEvent retEvent = null;
        
        if (me.getSource() == component) {
            retEvent = dndArmedEvent;
        } // else component has changed unexpectedly, so return null
        
        clearState();
        return retEvent;
    }
    
    /**
     * Returns whether or not a drag gesture recognition is ongoing.
     */
    private boolean mouseDraggedImpl(MouseEvent me, BeforeDrag bd) {
        /* no recognition is in progress */
        if (dndArmedEvent == null) {
            return false;
        }
        
        /* component has changed unexpectedly, so bail */
        if (me.getSource() != component) {
            clearState();
            return false;
        }
        
        int dx = Math.abs(me.getX() - dndArmedEvent.getX());
        int dy = Math.abs(me.getY() - dndArmedEvent.getY());
        if (Math.sqrt(dx*dx+dy*dy) > motionThreshold) {
            TransferHandler th = component.getTransferHandler();
            int action = mapDragOperationFromModifiers(me, th);
            if (action != TransferHandler.NONE) {
                /* notify the BeforeDrag instance */
                if (bd != null) {
                    bd.dragStarting(dndArmedEvent);
                }
                th.exportAsDrag(component, dndArmedEvent, action);
                clearState();
            }
        }
        
        return true;
    }
    
    private int convertModifiersToDropAction(int modifiersEx, int sourceActions) {
        int dropAction = DnDConstants.ACTION_NONE;
        if ( 0 != (modifiersEx & (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK)))  {
            dropAction = DnDConstants.ACTION_COPY & sourceActions;
            if (dropAction == DnDConstants.ACTION_NONE) {
                dropAction = DnDConstants.ACTION_MOVE & sourceActions;
            }
        } else if ( 0 != (modifiersEx & (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK)) &&
                0 != (modifiersEx & (InputEvent.META_DOWN_MASK)))  {
            dropAction = DnDConstants.ACTION_LINK & sourceActions;
            if (dropAction == DnDConstants.ACTION_NONE) {
                dropAction = DnDConstants.ACTION_MOVE & sourceActions;
            }
        } else {
            dropAction = DnDConstants.ACTION_MOVE & sourceActions;
            if (dropAction == DnDConstants.ACTION_NONE) {
            dropAction = DnDConstants.ACTION_COPY & sourceActions;
            }
        }
        return dropAction;
    }
}
