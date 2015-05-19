/**
 * @(#)QuaquaDragGestureRecognizer.java  
 *
 * Copyright (c) 2008-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import java.awt.dnd.DnDConstants;
import java.awt.event.*;
import javax.swing.*;

/**
 * This is a blunt copy of BasicDragGestureRecognizer from J2SE5.
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaDragGestureRecognizer.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaDragGestureRecognizer implements MouseListener, MouseMotionListener {

    private MouseEvent dndArmedEvent = null;

    private static int getMotionThreshold() {
        //return DragSource.getDragThreshold();
        return 5;
    }

    public static boolean exceedsMotionTreshold(MouseEvent a, MouseEvent b) {
        if (a == null || b == null) {
            return true;
        }
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int tr2 = getMotionThreshold() * getMotionThreshold();
        return /*dx > dy &&*/ ((dx * dx > tr2) || (dy * dy > tr2));
    }
    public static boolean exceedsCheckTreshold(MouseEvent a, MouseEvent b) {
        if (a == null || b == null) {
            return true;
        }
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int tr2 = getMotionThreshold() * getMotionThreshold();
        return ((dx * dx > tr2) || (dy * dy > tr2));
    }


    protected int mapDragOperationFromModifiers(MouseEvent e) {
        int mods = e.getModifiersEx();

        if ((mods & InputEvent.BUTTON1_DOWN_MASK) != InputEvent.BUTTON1_DOWN_MASK) {
            return TransferHandler.NONE;
        }

        JComponent c = getComponent(e);
        TransferHandler th = c.getTransferHandler();
        return convertModifiersToDropAction(mods, th.getSourceActions(c));
    }

    private static int convertModifiersToDropAction(int mods, int sourceActions) {
        int modifierMask = InputEvent.ALT_DOWN_MASK | InputEvent.META_DOWN_MASK |
                InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK;
        if (((mods & modifierMask) | InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK) {
            if ((sourceActions & DnDConstants.ACTION_MOVE) != 0) {
                return DnDConstants.ACTION_MOVE;
            } else {
                return sourceActions & DnDConstants.ACTION_COPY;
            }
        } else if ((mods & modifierMask) == InputEvent.ALT_DOWN_MASK) {
            return sourceActions & DnDConstants.ACTION_COPY;
        } else if ((mods & modifierMask) == (InputEvent.ALT_DOWN_MASK | InputEvent.META_DOWN_MASK)) {
            return sourceActions & DnDConstants.ACTION_LINK;
        }
        return 0;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        dndArmedEvent = null;

        if (isDragPossible(e) && mapDragOperationFromModifiers(e) != TransferHandler.NONE) {
            dndArmedEvent = e;
            e.consume();
        }
    }

    public void mouseReleased(MouseEvent e) {
        dndArmedEvent = null;
    }

    public void mouseEntered(MouseEvent e) {
    //dndArmedEvent = null;
    }

    public void mouseExited(MouseEvent e) {
    //if (dndArmedEvent != null && mapDragOperationFromModifiers(e) == TransferHandler.NONE) {
    //    dndArmedEvent = null;
    //}
    }

    public void mouseDragged(MouseEvent e) {
        if (dndArmedEvent != null) {
            e.consume();

            int action = mapDragOperationFromModifiers(e);

            if (action == TransferHandler.NONE) {
                return;
            }

            if (exceedsMotionTreshold(dndArmedEvent, e)) {
                // start transfer... shouldn't be a click at this point
                JComponent c = getComponent(e);
                TransferHandler th = c.getTransferHandler();
                th.exportAsDrag(c, dndArmedEvent, action);
                dndArmedEvent = null;
            }
            if (exceedsCheckTreshold(dndArmedEvent, e)) {
                dndArmedEvent = null;
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
    }

    private TransferHandler getTransferHandler(MouseEvent e) {
        JComponent c = getComponent(e);
        return c == null ? null : c.getTransferHandler();
    }

    /**
     * Determines if the following are true:
     * <ul>
     * <li>the press event is located over a selection
     * <li>the dragEnabled property is true
     * <li>A TranferHandler is installed
     * </ul>
     * <p>
     * This is implemented to check for a TransferHandler.
     * Subclasses should perform the remaining conditions.
     */
    protected boolean isDragPossible(MouseEvent e) {
        JComponent c = getComponent(e);
        return (c == null) ? true : (c.getTransferHandler() != null);
    }

    protected JComponent getComponent(MouseEvent e) {
        Object src = e.getSource();
        if (src instanceof JComponent) {
            JComponent c = (JComponent) src;
            return c;
        }
        return null;
    }
}

