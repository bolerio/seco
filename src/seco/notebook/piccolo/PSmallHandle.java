package seco.notebook.piccolo;

/* 
 * Copyright (C) 2002-@year@ by University of Maryland, College Park, MD 20742, USA 
 * All rights reserved. 
 * 
 * Piccolo was written at the Human-Computer Interaction Laboratory 
 * www.cs.umd.edu/hcil by Jesse Grosjean under the supervision of Ben Bederson. 
 * The Piccolo website is www.cs.umd.edu/hcil/piccolo 
 */ 
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.GradientPaint;
import java.awt.event.InputEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.util.PLocator;
import edu.umd.cs.piccolox.util.PNodeLocator;

/**
 * <b>PSmallHandle</b> is used to modify some aspect of Piccolo when it
 * is dragged. Each handle has a PLocator that it uses to automatically position
 * itself. See PBoundsHandle for an example of a handle that resizes the bounds
 * of another node.
 * <P>
 * @version 1.0
 * @author Jesse Grosjean
 */
public class PSmallHandle extends PNode {

	public static float DEFAULT_HANDLE_SIZE = 4;
	//public static Paint DEFAULT_PAINT = new GradientPaint( 0, 0, Color.white, 2, 2, Color.black, true );
  public static Paint DEFAULT_PAINT = Color.lightGray;

	private PLocator locator;
	private PDragSequenceEventHandler handleDragger;


	/**
	 * Construct a new handle that will use the given locator
	 * to locate itself on its parent node.
	 */
	public PSmallHandle(PLocator aLocator) {
		super();
		locator = aLocator;
		setPaint(DEFAULT_PAINT);
    setWidth( DEFAULT_HANDLE_SIZE );
    setHeight( DEFAULT_HANDLE_SIZE );
		installHandleEventHandlers();
	}

	protected void installHandleEventHandlers() {
		handleDragger = new PDragSequenceEventHandler() {
			protected void startDrag(PInputEvent event) {
				super.startDrag(event);
				startHandleDrag(event.getPositionRelativeTo(PSmallHandle.this), event);
			}
			protected void drag(PInputEvent event) {
				super.drag(event);
				PDimension aDelta = event.getDeltaRelativeTo(PSmallHandle.this); 	
				if (aDelta.getWidth() != 0 || aDelta.getHeight() != 0) {
					dragHandle(aDelta, event);
				}
			}
			protected void endDrag(PInputEvent event) {
				super.endDrag(event);
				endHandleDrag(event.getPositionRelativeTo(PSmallHandle.this), event);
			}
		};

		addPropertyChangeListener(PNode.PROPERTY_TRANSFORM, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				relocateHandle();
			}
		}); 	
		
		handleDragger.setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK));
		handleDragger.getEventFilter().setMarksAcceptedEventsAsHandled(true);
		handleDragger.getEventFilter().setAcceptsMouseEntered(false);
		handleDragger.getEventFilter().setAcceptsMouseExited(false);
		handleDragger.getEventFilter().setAcceptsMouseMoved(false); 	// no need for moved events for handle interaction, 
																		// so reject them so we don't consume them
		addInputEventListener(handleDragger);
	}
	
	/**
	 * Return the event handler that is responsible for the drag handle
	 * interaction.
	 */
	public PDragSequenceEventHandler getHandleDraggerHandler() {
		return handleDragger;
	}	

	/**
	 * Get the locator that this handle uses to position itself on its
	 * parent node.
	 */
	public PLocator getLocator() {
		return locator;
	}
	
	/**
	 * Set the locator that this handle uses to position itself on its
	 * parent node.
	 */
	public void setLocator(PLocator aLocator) {
		locator = aLocator;
		invalidatePaint();
		relocateHandle();
	}
	
	//****************************************************************
	// Handle Dragging - These are the methods the subclasses should
	// normally override to give a handle unique behavior.
	//****************************************************************
	
	/**
	 * Override this method to get notified when the handle starts to get dragged.
	 */
	public void startHandleDrag(Point2D aLocalPoint, PInputEvent aEvent) {
	}
	
	/**
	 * Override this method to get notified as the handle is dragged.
	 */
	public void dragHandle(PDimension aLocalDimension, PInputEvent aEvent) {
	}
	
	/**
	 * Override this method to get notified when the handle stops getting dragged.
	 */
	public void endHandleDrag(Point2D aLocalPoint, PInputEvent aEvent) {
	}
	  

	//****************************************************************
	// Layout - When a handle's parent's layout changes the handle
	// invalidates its own layout and then repositions itself on its
	// parents bounds using its locator to determine that new
	// position.
	//****************************************************************
	
	public void setParent(PNode newParent) {
		super.setParent(newParent);
		relocateHandle();
	}
	
	public void parentBoundsChanged() {
		relocateHandle();
	}
		
	/**
	 * Force this handle to relocate itself using its locator.
	 */
	public void relocateHandle() {
		if (locator != null) {
			PBounds b = getBoundsReference();
			Point2D aPoint = locator.locatePoint(null);
			
			if (locator instanceof PNodeLocator) {
				PNode located = ((PNodeLocator)locator).getNode();
				PNode parent = getParent();
				
				located.localToGlobal(aPoint);
				globalToLocal(aPoint);
				
				if (parent != located && parent instanceof PCamera) {
					((PCamera)parent).viewToLocal(aPoint);
				}
			}
			
			double newCenterX = aPoint.getX();
			double newCenterY = aPoint.getY();

			if (newCenterX != b.getCenterX() ||
				newCenterY != b.getCenterY()) {
				
				centerBoundsOnPoint(newCenterX, newCenterY);
			}
		}
	}
		
	//****************************************************************
	// Serialization
	//****************************************************************
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		installHandleEventHandlers();
	}
}

