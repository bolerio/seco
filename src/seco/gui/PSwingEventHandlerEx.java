package seco.gui;


import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.pswing.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

/**
 * Event handler to send MousePressed, MouseReleased, MouseMoved, MouseClicked,
 * and MouseDragged events on Swing components within a PCanvas.
 * 
 * @author Ben Bederson
 * @author Lance Good
 * @author Sam Reid
 */
public class PSwingEventHandlerEx implements PInputEventListener {

    private PNode listenNode = null; // used to listen to for events
    private boolean active = false; // True when event handlers are set active.

    // The previous component - used to generate mouseEntered and
    // mouseExited events
    private Component prevComponent = null;

    // Previous points used in generating mouseEntered and mouseExited events
    private Point2D prevPoint = null;
    private Point2D prevOff = null;

    private boolean recursing = false;// to avoid accidental recursive handling

    private ButtonData leftButtonData = new ButtonData();
    private ButtonData rightButtonData = new ButtonData();
    private ButtonData middleButtonData = new ButtonData();

    private PSwingCanvas canvas;

    /**
     * Constructs a new PSwingEventHandler for the given canvas, and a node that
     * will recieve the mouse events.
     * 
     * @param canvas the canvas associated with this PSwingEventHandler.
     * @param node the node the mouse listeners will be attached to.
     */
    public PSwingEventHandlerEx(PSwingCanvas canvas, PNode node) {
        this.canvas = canvas;
        listenNode = node;
    }

    /**
     * Constructs a new PSwingEventHandler for the given canvas.
     */
    public PSwingEventHandlerEx(PSwingCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Sets whether this event handler can fire events.
     * 
     * @param active
     */
    void setActive(boolean active) {
        if (this.active && !active) {
            if (listenNode != null) {
                this.active = false;
                listenNode.removeInputEventListener(this);
            }
        }
        else if (!this.active && active) {
            if (listenNode != null) {
                this.active = true;
                listenNode.addInputEventListener(this);
            }
        }
    }

    /**
     * Determines if this event handler is active.
     * 
     * @return True if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Finds the component at the specified location (must be showing).
     * 
     * @param c
     * @param x
     * @param y
     * @return the component at the specified location.
     */
    private Component findShowingComponentAt(Component c, int x, int y) {
        if (!c.contains(x, y)) {
            return null;
        }

        if (c instanceof Container) {
            Container contain = ((Container) c);
            int ncomponents = contain.getComponentCount();
            Component component[] = contain.getComponents();
            //MINE: otherwise some buttons from inactive tabs receive events
            //which leads to lot of unexpected bugs and strange behavior
//            if(contain instanceof JTabbedPane)
//            {
//                ncomponents = 1;
//                component = new Component[]{
//                        ((JTabbedPane) contain).getSelectedComponent()
//                };
//                //System.out.println("PSwingEventHandlerEx: " + component[0]);
//            }
            //but the addition of comp.isVisible() will have the same effect
            //isEnabled() chek probably will speed up things too 
            for (int i = 0; i < ncomponents; i++) {
                Component comp = component[i];
                if (comp != null && comp.isVisible() && comp.isEnabled()) {
                    Point p = comp.getLocation();
                    if (comp instanceof Container) {
                        comp = findShowingComponentAt(comp, x - (int) p.getX(), y - (int) p.getY());
                    }
                    else {
                        comp = comp.getComponentAt(x - (int) p.getX(), y - (int) p.getY());
                    }
                    if (comp != null && comp.isShowing()) {
                        return comp;
                    }
                }
            }
        }
        return c;
    }

    Point2D innerPt(PSwingNode swing, Point2D pt)
    {
        PiccoloCanvas canvas = swing.getCanvas();
        PSwingNode canv_node = GUIHelper.getPSwingNode(canvas);
        if(canv_node == null) return pt;
        // PBounds fb = canv_node.getFullBounds();
        // Point2D pt_out = new Point2D.Double(pt.getX() - fb.x, pt.getY() - fb.y);
        cameraToLocal(canvas.getCamera(), pt, canv_node);
        //System.out.println("innerPt: " + pt + ":" + pt_out);
        return pt; //pt_out;
    }
    /**
     * Determines if any Swing components in Piccolo should receive the given
     * MouseEvent and forwards the event to that component. However,
     * mouseEntered and mouseExited are independent of the buttons. Also, notice
     * the notes on mouseEntered and mouseExited.
     * 
     * @param pSwingMouseEvent
     * @param aEvent
     */
    void dispatchEvent(PSwingEvent pSwingMouseEvent, PInputEvent aEvent)
    {
        MouseEvent mEvent = pSwingMouseEvent.asMouseEvent();
        Component comp = null;
        Point2D pt = null;
        PNode pickedNode = pSwingMouseEvent.getPath().getPickedNode();

        // The offsets to put the event in the correct context
        int offX = 0;
        int offY = 0;

        PNode currentNode = pSwingMouseEvent.getCurrentNode();

        if (currentNode instanceof PSwing)
        {
            PSwingNode swing = (PSwingNode) currentNode;
            PNode grabNode = pickedNode;

            if (true)//grabNode.isDescendentOf(canvas.getRoot()))
            {
                //pt = new Point2D.Double(mEvent.getX(), mEvent.getY());
                //cameraToLocal(pSwingMouseEvent.getPath().getTopCamera(), pt,
                //        grabNode);
                boolean inner = ! grabNode.isDescendentOf(canvas.getRoot());
                pt = new Point2D.Double(mEvent.getX(), mEvent.getY());
                cameraToLocal(pSwingMouseEvent.getPath().getTopCamera(), pt, grabNode);  
                if(inner) pt = innerPt(swing,pt);
                prevPoint = new Point2D.Double(pt.getX(), pt.getY());

                // This is only partially fixed to find the deepest
                // component at pt. It needs to do something like
                // package private method:
                // Container.getMouseEventTarget(int,int,boolean)
                comp = //(inner) ?  swing.getComponent() :
                    findShowingComponentAt(swing.getComponent(), (int) pt
                        .getX(), (int) pt.getY());
                //if(pSwingMouseEvent.getID() != MouseEvent.MOUSE_MOVED)
               //    System.out.println("PSwingEventhandlerEx: " + inner +
               //         ":" + (comp != null && comp != swing.getComponent()) +
               //         ":" + pSwingMouseEvent + ":" + comp + ":" + currentNode); 
                // We found the right component - but we need to
                // get the offset to put the event in the component's
                // coordinates
                if (comp != null && comp != swing.getComponent())
                {
                    for (Component c = comp; c != swing.getComponent(); c = c
                            .getParent())
                    {
                        offX += c.getLocation().getX();
                        offY += c.getLocation().getY();
                    }
                }

                // Mouse Pressed gives focus - effects Mouse Drags and
                // Mouse Releases
                if (comp != null
                        && pSwingMouseEvent.getID() == MouseEvent.MOUSE_PRESSED)
                {
                    if (SwingUtilities.isLeftMouseButton(mEvent))
                    {
                        leftButtonData.setState(swing, pickedNode, comp, 
                                    offX, offY);
                    }
                    else if (SwingUtilities.isMiddleMouseButton(mEvent))
                    {
                        middleButtonData.setState(swing, pickedNode, comp,
                                offX, offY);
                    }
                    else if (SwingUtilities.isRightMouseButton(mEvent))
                    {
                        rightButtonData.setState(swing, pickedNode, comp, offX,
                                offY);
                    }
                }
            }
        }

        // This first case we don't want to give events to just
        // any Swing component - but to the one that got the
        // original mousePressed
        if (pSwingMouseEvent.getID() == MouseEvent.MOUSE_DRAGGED
                || pSwingMouseEvent.getID() == MouseEvent.MOUSE_RELEASED)
        {

            // LEFT MOUSE BUTTON
            if (SwingUtilities.isLeftMouseButton(mEvent)
                    && leftButtonData.getFocusedComponent() != null)
            {
                handleButton(pSwingMouseEvent, aEvent, leftButtonData);
            }

            // MIDDLE MOUSE BUTTON
            if (SwingUtilities.isMiddleMouseButton(mEvent)
                    && middleButtonData.getFocusedComponent() != null)
            {
                handleButton(pSwingMouseEvent, aEvent, middleButtonData);
            }

            // RIGHT MOUSE BUTTON
            if (SwingUtilities.isRightMouseButton(mEvent)
                    && rightButtonData.getFocusedComponent() != null)
            {
                handleButton(pSwingMouseEvent, aEvent, rightButtonData);
            }
        }
        // This case covers the cases mousePressed, mouseClicked,
        // and mouseMoved events
        else if ((pSwingMouseEvent.getID() == MouseEvent.MOUSE_PRESSED
                || pSwingMouseEvent.getID() == MouseEvent.MOUSE_CLICKED || pSwingMouseEvent
                .getID() == MouseEvent.MOUSE_MOVED)
                && (comp != null))
        {

            MouseEvent e_temp = new MouseEvent(comp, pSwingMouseEvent.getID(),
                    mEvent.getWhen(), mEvent.getModifiers(), (int) pt.getX()
                            - offX, (int) pt.getY() - offY, mEvent
                            .getClickCount(), mEvent.isPopupTrigger());

            PSwingEvent e2 = PSwingMouseEvent.createMouseEvent(e_temp.getID(),
                    e_temp, aEvent);
            dispatchEvent(comp, e2);
        }
        else if (pSwingMouseEvent.getID() == MouseEvent.MOUSE_WHEEL
                && (comp != null))
        {
            MouseWheelEvent mWEvent = (MouseWheelEvent) mEvent;
            MouseWheelEvent e_temp = new MouseWheelEvent(comp, pSwingMouseEvent
                    .getID(), mEvent.getWhen(), mEvent.getModifiers(), (int) pt
                    .getX()
                    - offX, (int) pt.getY() - offY, mEvent.getClickCount(),
                    mEvent.isPopupTrigger(), mWEvent.getScrollType(), mWEvent
                            .getScrollAmount(), mWEvent.getWheelRotation());

            PSwingMouseWheelEvent e2 = new PSwingMouseWheelEvent(
                    e_temp.getID(), e_temp, aEvent);
            dispatchEvent(comp, e2);
        }

        // Now we need to check if an exit or enter event needs to
        // be dispatched - this code is independent of the mouseButtons.
        // I tested in normal Swing to see the correct behavior.
        if (prevComponent != null)
        {
            // This means mouseExited

            // This shouldn't happen - since we're only getting node events
            if (comp == null
                    || pSwingMouseEvent.getID() == MouseEvent.MOUSE_EXITED)
            {
                MouseEvent e_temp = createExitEvent(mEvent);

                PSwingEvent e2 = PSwingMouseEvent.createMouseEvent(e_temp
                        .getID(), e_temp, aEvent);

                dispatchEvent(prevComponent, e2);
                prevComponent = null;
            }

            // This means mouseExited prevComponent and mouseEntered comp
            else if (prevComponent != comp)
            {
                MouseEvent e_temp = createExitEvent(mEvent);
                PSwingEvent e2 = PSwingMouseEvent.createMouseEvent(e_temp
                        .getID(), e_temp, aEvent);
                dispatchEvent(prevComponent, e2);

                e_temp = createEnterEvent(comp, mEvent, offX, offY);
                e2 = PSwingMouseEvent.createMouseEvent(e_temp.getID(), e_temp,
                        aEvent);
                comp.dispatchEvent(e2.asMouseEvent());
            }
        }
        else
        {
            // This means mouseEntered
            if (comp != null)
            {
                MouseEvent e_temp = createEnterEvent(comp, mEvent, offX, offY);
                PSwingEvent e2 = PSwingMouseEvent.createMouseEvent(e_temp
                        .getID(), e_temp, aEvent);
                dispatchEvent(comp, e2);
            }
        }

         // Set the previous variables for next time
        prevComponent = comp;

        if (comp != null)
        {
            prevOff = new Point2D.Double(offX, offY);
        }
    }
    
    private MouseEvent createEnterEvent(Component comp, MouseEvent e1,
            int offX, int offY)
    {
        return new MouseEvent(comp, MouseEvent.MOUSE_ENTERED, e1.getWhen(), 0,
                (int) prevPoint.getX() - offX, (int) prevPoint.getY() - offY,
                e1.getClickCount(), e1.isPopupTrigger());
    }

    private MouseEvent createExitEvent(MouseEvent e1)
    {
        return new MouseEvent(prevComponent, MouseEvent.MOUSE_EXITED, e1
                .getWhen(), 0, (int) prevPoint.getX() - (int) prevOff.getX(),
                (int) prevPoint.getY() - (int) prevOff.getY(), e1
                        .getClickCount(), e1.isPopupTrigger());
    }

    private void handleButton(PSwingEvent e1, PInputEvent aEvent, ButtonData buttonData) {
        Point2D pt;
         MouseEvent m1 = e1.asMouseEvent();
        if (true)//buttonData.getPNode().isDescendentOf(canvas.getRoot()))
        {
            pt = new Point2D.Double(m1.getX(), m1.getY());
            boolean inner = ! buttonData.getPNode().isDescendentOf(canvas.getRoot());
            cameraToLocal(e1.getPath().getTopCamera(), pt, buttonData
                    .getPNode());
            if(inner) pt = innerPt((PSwingNode)buttonData.getPNode(),pt);
            // todo this probably won't handle viewing through multiple cameras.
            MouseEvent e_temp = new MouseEvent(
                    buttonData.getFocusedComponent(), e1.getID(), m1.getWhen(),
                    m1.getModifiers(), (int) pt.getX()
                            - buttonData.getOffsetX(), (int) pt.getY()
                            - buttonData.getOffsetY(), m1.getClickCount(), m1
                            .isPopupTrigger());

            PSwingEvent e2 = PSwingMouseEvent.createMouseEvent(e_temp.getID(),
                    e_temp, aEvent);
            dispatchEvent(buttonData.getFocusedComponent(), e2);
        }
        else
        {
            dispatchEvent(buttonData.getFocusedComponent(), e1);
        }
        // buttonData.getPSwing().repaint(); //Experiment with SliderExample
        // (from Martin) suggests this line is unnecessary, and a serious
        // problem in performance.
        m1.consume();
        if (e1.getID() == MouseEvent.MOUSE_RELEASED)
        {
            buttonData.mouseReleased();
        }
    }

    private void dispatchEvent(final Component target, final PSwingEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                target.dispatchEvent(event.asMouseEvent());
            }
        });
    }

    private void cameraToLocal(PCamera topCamera, Point2D pt, PNode node) {
        AffineTransform inverse = null;
        try {
            inverse = topCamera.getViewTransform().createInverse();
        }
        catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
         /*
         * Only apply the camera's view transform when this node is a descendant
         * of PLayer
         */
        PNode searchNode = node;
        do {
            searchNode = searchNode.getParent();
            if (searchNode instanceof PLayer) {
                inverse.transform(pt, pt);
                break;
            }
        } while (searchNode != null);
        if (node != null)
            node.globalToLocal(pt);
    }

    /**
     * Process a piccolo event and (if active) dispatch the corresponding Swing
     * event.
     * 
     * @param aEvent
     * @param type
     */
    public void processEvent(PInputEvent aEvent, int type)
    {
        if (aEvent.isMouseEvent())
        {
            InputEvent sourceSwingEvent = aEvent.getSourceSwingEvent();
            if (sourceSwingEvent instanceof MouseEvent)
            {
                if (sourceSwingEvent instanceof MouseWheelEvent)
                {

                }
                MouseEvent swingMouseEvent = (MouseEvent) sourceSwingEvent;
                PSwingEvent pSwingMouseEvent = PSwingMouseEvent
                        .createMouseEvent(swingMouseEvent.getID(),
                                swingMouseEvent, aEvent);
                if (!recursing)
                {
                    recursing = true;
                    dispatchEvent(pSwingMouseEvent, aEvent);
                    if (pSwingMouseEvent.asMouseEvent().isConsumed())
                    {
                        aEvent.setHandled(true);
                    }
                    recursing = false;
                }
            }
            else
            {
                new Exception(
                        "PInputEvent.getSourceSwingEvent was not a MouseEvent.  Actual event: "
                                + sourceSwingEvent + ", class="
                                + sourceSwingEvent.getClass().getName())
                        .printStackTrace();
            }
        }

   }


    /**
     * Internal Utility class for handling button interactivity.
     */
    private static class ButtonData {
        private PSwing focusPSwing = null;
        private PNode focusNode = null;
        private Component focusComponent = null;
        private int focusOffX = 0;
        private int focusOffY = 0;

        public void setState(PSwing swing, PNode visualNode, Component comp, int offX, int offY) {
            focusPSwing = swing;
            focusComponent = comp;
            focusNode = visualNode;
            focusOffX = offX;
            focusOffY = offY;
        }

        public Component getFocusedComponent() {
            return focusComponent;
        }

        public PNode getPNode() {
            return focusNode;
        }

        public int getOffsetX() {
            return focusOffX;
        }

        public int getOffsetY() {
            return focusOffY;
        }

        public PSwing getPSwing() {
            return focusPSwing;
        }

        public void mouseReleased() {
            focusComponent = null;
            focusNode = null;
        }
    }
}
