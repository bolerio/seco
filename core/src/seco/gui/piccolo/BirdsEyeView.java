package seco.gui.piccolo;

import java.awt.geom.Rectangle2D;
import java.awt.Color;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * The Birds Eye View Class
 */
public class BirdsEyeView extends PCanvas implements PropertyChangeListener
{

    /**
     * This is the node that shows the viewed area.
     */
    PPath areaVisiblePNode;

    /**
     * This is the canvas that is being viewed
     */
    PCanvas viewedCanvas;

    /**
     * The change listener to know when to update the birds eye view.
     */
    PropertyChangeListener changeListener;

    int layerCount;

    /**
     * Creates a new instance of a BirdsEyeView
     */
    public BirdsEyeView()
    {

        // create the PropertyChangeListener for listening to the viewed canvas
        changeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt)
            {
                updateFromViewed();
            }
        };

        areaVisiblePNode = PPath.createRectangle(0, 0, 100, 100);
        areaVisiblePNode.setPaint(new Color(.2f, .3f, .8f, .3f));
        areaVisiblePNode.setStroke(new PFixedWidthStroke(2));
        areaVisiblePNode.setStrokePaint(Color.red);

        getCamera().addChild(areaVisiblePNode);

        // add the drag event handler
        getCamera().addInputEventListener(new PDragSequenceEventHandler() {
            protected void startDrag(PInputEvent e)
            {
                if (e.getPickedNode() == areaVisiblePNode) super.startDrag(e);
            }

            protected void drag(PInputEvent e)
            {
                PDimension dim = e.getDelta();
                viewedCanvas.getCamera().translateView(0 - dim.getWidth(),
                        0 - dim.getHeight());
            }

        });

        // remove Pan and Zoom
        removeInputEventListener(getPanEventHandler());
        removeInputEventListener(getZoomEventHandler());

        // setDefaultRenderQuality( PPaintContext.LOW_QUALITY_RENDERING );

    }

    public void connect(PCanvas canvas, PLayer[] viewed_layers)
    {

        this.viewedCanvas = canvas;
        layerCount = 0;

        viewedCanvas.getCamera().addPropertyChangeListener(changeListener);

        for (layerCount = 0; layerCount < viewed_layers.length; ++layerCount)
        {
            getCamera().addLayer(layerCount, viewed_layers[layerCount]);
        }

    }

    /**
     * Add a layer to list of viewed layers
     */
    public void addLayer(PLayer new_layer)
    {
        getCamera().addLayer(new_layer);
        layerCount++;
    }

    /**
     * Remove the layer from the viewed layers
     */
    public void removeLayer(PLayer old_layer)
    {
        getCamera().removeLayer(old_layer);
        layerCount--;
    }

    /**
     * Stop the birds eye view from receiving events from the viewed canvas and
     * remove all layers
     */
    public void disconnect()
    {
        viewedCanvas.getCamera().removePropertyChangeListener(changeListener);

        for (int i = 0; i < getCamera().getLayerCount(); ++i)
        {
            getCamera().removeLayer(i);
        }

    }

    /**
     * This method will get called when the viewed canvas changes
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        updateFromViewed();
    }

    /**
     * This method gets the state of the viewed canvas and updates the
     * BirdsEyeViewer This can be called from outside code
     */
    public void updateFromViewed()
    {

        double viewedX;
        double viewedY;
        double viewedHeight;
        double viewedWidth;

        double ul_camera_x = viewedCanvas.getCamera().getViewBounds().getX();
        double ul_camera_y = viewedCanvas.getCamera().getViewBounds().getY();
        double lr_camera_x = ul_camera_x
                + viewedCanvas.getCamera().getViewBounds().getWidth();
        double lr_camera_y = ul_camera_y
                + viewedCanvas.getCamera().getViewBounds().getHeight();

        Rectangle2D drag_bounds = getCamera().getUnionOfLayerFullBounds();

        double ul_layer_x = drag_bounds.getX();
        double ul_layer_y = drag_bounds.getY();
        double lr_layer_x = drag_bounds.getX() + drag_bounds.getWidth();
        double lr_layer_y = drag_bounds.getY() + drag_bounds.getHeight();

        // find the upper left corner

        // set to the lesser value
        if (ul_camera_x < ul_layer_x) viewedX = ul_layer_x;
        else
            viewedX = ul_camera_x;

        // same for y
        if (ul_camera_y < ul_layer_y) viewedY = ul_layer_y;
        else
            viewedY = ul_camera_y;

        // find the lower right corner

        // set to the greater value
        if (lr_camera_x < lr_layer_x) viewedWidth = lr_camera_x - viewedX;
        else
            viewedWidth = lr_layer_x - viewedX;

        // same for height
        if (lr_camera_y < lr_layer_y) viewedHeight = lr_camera_y - viewedY;
        else
            viewedHeight = lr_layer_y - viewedY;

        Rectangle2D bounds = new Rectangle2D.Double(viewedX, viewedY,
                viewedWidth, viewedHeight);
        bounds = getCamera().viewToLocal(bounds);
        areaVisiblePNode.setBounds(bounds);

        // keep the birds eye view centered
        getCamera().animateViewToCenterBounds(drag_bounds, true, 0);

    }

} // class BirdsEyeView
