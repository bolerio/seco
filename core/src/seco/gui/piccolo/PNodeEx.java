package seco.gui.piccolo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Rowan Christmas
 */
public class PNodeEx extends PPath
{
    public static final int TRIANGLE = 0;
    public static final int DIAMOND = 1;
    public static final int ELLIPSE = 2;
    public static final int HEXAGON = 3;
    public static final int OCTAGON = 4;
    public static final int PARALELLOGRAM = 5;
    public static final int RECTANGLE = 6;
    public static final int ROUNDED_RECTANGLE = 7;
   
    /**
     * Our label
     */
    protected PLabel label;
    /**
     * Our Selection toggle
     */
    protected boolean selected;
    /**
     * Our Visibility
     */
    protected boolean visible;
    /**
     * A boolean that tells us if we are updated to the current position, i.e.
     * after a layout
     */
    protected boolean sandboxed;
    
    protected Image image;
    
    int NODE_SHAPE = ELLIPSE;
    Paint NODE_PAINT = Color.WHITE;
    Paint NODE_SELECTION_PAINT;
    Paint NODE_BORDER_PAINT = Color.BLACK;
    float NODE_BORDER_WIDTH = 0; 
    double NODE_WIDTH = 30.0;
    double NODE_HEIGHT = 30.0;
    String NODE_LABEL;
    
    // ----------------------------------------//
    // Constructors and Initialization
    // ----------------------------------------//
    public PNodeEx()
    {
        this(Integer.MAX_VALUE, (Paint) null, (Paint) null, (Paint) null,
                Float.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE,
                (String) null);
    }

    /**
     * Create a new PNodeView with the given physical attributes.
     * @param node_index The RootGraph Index of this node
     * @param view the PGraphVIew that we belong to
     * @param x_positon the x_positon desired for this node
     * @param y_positon the y_positon desired for this node
     * @param shape the shape type
     * @param paint the Paint for this node
     * @param selection_paint the Paint when this node is selected
     * @param border_paint the boder Paint
     * @param border_width the width of the border
     * @param width the width of the node
     * @param height the height of the node
     * @param label the String to display on the label
     */
    public PNodeEx(int shape, Paint paint, Paint selection_paint,
            Paint border_paint, float border_width, double width,
            double height, String label)
    {
        
        if (shape != Integer.MAX_VALUE)
           NODE_SHAPE = shape;
        if (paint != null)
           NODE_PAINT = paint;
        if (paint != null)
           NODE_SELECTION_PAINT = selection_paint;
        if (border_paint != null)
          NODE_BORDER_PAINT = border_paint;
        if (border_width != Float.MAX_VALUE)
          NODE_BORDER_WIDTH = border_width;
        if (width != Double.MAX_VALUE)
           NODE_WIDTH = width;
        if (height != Double.MAX_VALUE)
           NODE_HEIGHT = height;
        if (label != null)
           NODE_LABEL = label;
        initializeNodeView();
    }

    /**
     * This does a default paint and positioning of the PNodeView based on the
     * values passed on its initial construction.
     */
    protected void initializeNodeView()
    {
        // all w/h is done in the nodes local coordinate system
        setHeight(NODE_HEIGHT);
        setWidth(NODE_WIDTH);
        setStrokePaint(Color.black);
        setPaint(Color.white);
        this.visible = true;
        this.selected = false;
        setShape(NODE_SHAPE);
        setPickable(true);
        invalidatePaint();
    }

  
    // ------------------------------------------------------//
    // Get and Set Methods for all Common Viewable Elements
    // ------------------------------------------------------//
    /**
     * Shape is currently defined via predefined variables in the PNodeView
     * interface. To get the actual java.awt.Shape use getPathReference()
     * @return the current int-tpye shape
     */
    public int getShape()
    {
        return NODE_SHAPE;
    }

    /**
     * This sets the Paint that will be used by this node when it is painted as
     * selected.
     * @param paint The Paint to be used
     */
    public void setSelectedPaint(Paint paint)
    {
        NODE_SELECTION_PAINT = paint;
        if (selected)
        {
            setPaint(paint);
        }
    }

    /**
     * @return the currently set selection Paint
     */
    public Paint getSelectedPaint()
    {
        return NODE_SELECTION_PAINT;
    }

    public void setUnselectedPaint(Paint paint)
    {
       NODE_PAINT = paint;
        if (!selected)
            setPaint(paint);
    }

    /**
     * @return the currently set paint
     */
    public Paint getUnselectedPaint()
    {
        return NODE_PAINT;
    }

    /**
     * @param b_paint the paint the border will use
     */
    public void setBorderPaint(Paint b_paint)
    {
        NODE_BORDER_PAINT  = b_paint;
        super.setStrokePaint(b_paint);
    }

    /**
     * @return the currently set BOrder Paint
     */
    public Paint getBorderPaint()
    {
        return NODE_BORDER_PAINT;
    }

    /**
     * @param border_width The width of the border.
     */
    public void setBorderWidth(float border_width)
    {
        NODE_BORDER_WIDTH = border_width;
        super.setStroke(new BasicStroke(border_width));
    }

    /**
     * @return the currently set Border width
     */
    public float getBorderWidth()
    {
        return NODE_BORDER_WIDTH;
    }

    /**
     * @param stroke the new stroke for the border
     */
    public void setBorder(Stroke stroke)
    {
        super.setStroke(stroke);
    }

    /**
     * @return the current border
     */
    public Stroke getBorder()
    {
        return super.getStroke();
    }

    /**
     * Width is a property in the nodes local coordinate system.
     * @param width the currently set width of this node
     */
    public boolean setWidth(double width)
    {
        double old_width = getWidth();
        NODE_WIDTH = width;
        super.setWidth(width);
        // keep the node centered
        offset(old_width / 2 - width / 2, 0);
        return true;
    }

    /**
     * Width is a property in the nodes local coordinate system.
     * @return the currently set width of this node
     */
    public double getWidth()
    {
        return super.getWidth();
    }

    /**
     * Height is a property in the nodes local coordinate system.
     * @param height the currently set height of this node
     */
    public boolean setHeight(double height)
    {
        double old_height = getHeight();
        NODE_HEIGHT = height;
        super.setHeight(height);
        // keep the node centered
        offset(0, old_height / 2 - height / 2);
        return true;
    }

    /**
     * Height is a property in the nodes local coordinate system.
     * @return the currently set height of this node
     */
    public double getHeight()
    {
        return super.getHeight();
    }

    /**
     * @param label_text the new value to be displayed by the Label
     */
    public void setLabelText(String label_text)
    {
        getLabel().setText(label_text);
    }

    /**
     * @return The label that is also a Child of this node
     */
    public PLabel getLabel()
    {
        if (label == null)
        {
            label = new PLabel(null, this);
            label.setPickable(false);
            addChild(label);
            label.updatePosition();
        }
        return label;
    }


    /**
     * X and Y are a Global coordinate system property of this node, and affect
     * the nodes children
     * 
     * setOffset moves the node to a specified location, offset increments the
     * node by a specified amount
     */
    public void setOffset(double x, double y)
    {
        // setOffset automatically centers the node based on its width
        x -= getWidth() / 2;
        y -= getHeight() / 2;
        super.setOffset(x, y);
    }

    /**
     * X and Y are a Global coordinate system property of this node, and affect
     * the nodes children
     * 
     * setOffset moves the node to a specified location, offset increments the
     * node by a specified amount
     */
    public void offset(double dx, double dy)
    {
        double new_x_position = getXOffset() + getWidth() / 2 + dx;
        new_x_position -= getWidth() / 2;
        double new_y_position = getYOffset() + getHeight() / 2 + dy;
        new_y_position -= getHeight() / 2;
        getTransformReference(true).setOffset(new_x_position, new_y_position);
        invalidatePaint();
        invalidateFullBounds();
        super.firePropertyChange(PNode.PROPERTY_CODE_TRANSFORM,
                PNode.PROPERTY_TRANSFORM, null, getTransformReference(true));
   }

   
     /**
     * This draws us as selected
     */
    public void select()
    {
        selected = true;
        super.setPaint(NODE_SELECTION_PAINT);
    }

    /**
     * This draws us as unselected
     */
    public void unselect()
    {
        selected = false;
        super.setPaint(NODE_PAINT);
    }

    /**
     * 
     */
    public boolean isSelected()
    {
        return selected;
    }

    /**
     * 
     */
    public boolean setSelected(boolean selected)
    {
        if (selected)
        {
            select();
        } else
        {
           unselect();
        }
        return this.selected;
    }

    // ****************************************************************
    // Painting
    // ****************************************************************
    /**
     * 
     */
    protected void paint(PPaintContext paintContext)
    {
        if(image != null)
          paintImage(paintContext);
        else
          super.paint(paintContext);
    }
    
    
    protected void paintImage(PPaintContext paintContext) {
        if (getImage() != null) {
            
            double iw = image.getWidth(null);
            double ih = image.getHeight(null);
            PBounds b = getBoundsReference();
            Graphics2D g2 = paintContext.getGraphics();
            Paint p = getPaint();
            if (p != null) {
                g2.setPaint(p);
                g2.fill(getPathReference());
            }

            if (b.x != 0 || b.y != 0 || b.width != iw || b.height != ih) {
                g2.translate(b.x, b.y);
                g2.scale(b.width / iw, b.height / ih);
                g2.drawImage(image, 0, 0, null);
                g2.scale(iw / b.width, ih / b.height);
                g2.translate(-b.x, -b.y);
            }
            else {
                g2.drawImage(image, 0, 0, null);
            }
        }
    }

    /**
     * Overridden method so that this node is aware of its bounds being changed
     * so that it can tell its label and edges to change their position
     * accordingly.
     */
    public boolean setBounds(double x, double y, double width, double height)
    {
        boolean b = super.setBounds(x, y, width, height);
        // System.out.println( "Bounds Changed for: "+rootGraphIndex );
        // try {
        // int[] i = new int[0];
        // i[2] = 1;
        // } catch ( Exception e ) {
        // e.printStackTrace();
        // }
        firePropertyChange(PNode.PROPERTY_CODE_BOUNDS, "BoundsChanged", null,
                this);
        if (label != null) label.updatePosition();
        return b;
    }

    /**
     * Set a new shape for the FNode, based on one of the pre-defined shapes
     * <B>Note:</B> calling setPathTo( Shape ), allows one to define their own
     * java.awt.Shape ( i.e. A picture of Johnny Cash )
     */
    public void setShape(int shape)
    {
        PBounds bounds = getBounds();
        float x = (float) getWidth();
        float y = (float) getHeight();
        java.awt.geom.Point2D offset = getOffset();
        NODE_SHAPE = shape;
        if (shape == TRIANGLE)
        {
            // make a trianlge
            setPathTo((PPath.createPolyline(new float[] { 0f * x, 2f * x,
                    1f * x, 0f * x }, new float[] { 2f * y, 2f * y, 0f * y,
                    2f * y })).getPathReference());
        } else if (shape == ROUNDED_RECTANGLE)
        {
            GeneralPath path = new GeneralPath();
            path.moveTo(1, 0);
            path.lineTo(2, 0);
            path.quadTo(3, 0, 3, 1);
            path.lineTo(3, 2);
            path.quadTo(3, 3, 2, 3);
            path.lineTo(1, 3);
            path.quadTo(0, 3, 0, 2);
            path.lineTo(0, 1);
            path.quadTo(0, 0, 1, 0);
            path.closePath();
            setPathTo(path);
        } else if (shape == DIAMOND)
        {
            setPathTo((PPath.createPolyline(new float[] { 1f * x, 2f * x,
                    1f * x, 0f * x, 1f * x }, new float[] { 0f * y, 1f * y,
                    2f * y, 1f * y, 0f * y })).getPathReference());
        } else if (shape == ELLIPSE)
        {
            setPathTo((PPath.createEllipse((float) getBounds().getX(),
                    (float) getBounds().getY(), (float) getBounds().getWidth(),
                    (float) getBounds().getHeight())).getPathReference());
        } else if (shape == HEXAGON)
        {
            setPathTo((PPath.createPolyline(new float[] { 0f * x, 1f * x,
                    2f * x, 3f * x, 2f * x, 1f * x, 0f * x }, new float[] {
                    1f * y, 2f * y, 2f * y, 1f * y, 0f * y, 0f * y, 1f * y }))
                    .getPathReference());
        } else if (shape == OCTAGON)
        {
            setPathTo((PPath.createPolyline(new float[] { 0f * x, 0f * x,
                    1f * x, 2f * x, 3f * x, 3f * x, 2f * x, 1f * x, 0f * x },
                    new float[] { 1f * y, 2f * y, 3f * y, 3f * y, 2f * y,
                            1f * y, 0f * y, 0f * y, 1f * y }))
                    .getPathReference());
        } else if (shape == PARALELLOGRAM)
        {
            setPathTo((PPath.createPolyline(new float[] { 0f * x, 1f * x,
                    3f * x, 2f * x, 0f * x }, new float[] { 0f * y, 1f * y,
                    1f * y, 0f * y, 0f * y })).getPathReference());
        } else if (shape == RECTANGLE)
        {
            setPathToRectangle((float) getX(), (float) getY(), x, y);
        }
        // setOffset( offset );
        // setHeight( x );
        // setWidth( y );
        setX(bounds.getX());
        setY(bounds.getY());
        setWidth(bounds.getWidth());
        setHeight(bounds.getHeight());
        if (label != null) label.updatePosition();
        firePropertyChange(0, "Offset", null, this);
    }

    /**
     * Set the new shape of the node, with a given new height and width
     * 
     * @param shape the shape type
     * @param width the new width
     * @param height the new height
     */
    public void setShape(int shape, double width, double height)
    {
        setWidth(width);
        setHeight(height);
        setShape(shape);
        firePropertyChange(0, "Offset", null, this);
    }

    public void firePropertyChange(java.lang.String propertyName,
            java.lang.Object oldValue, java.lang.Object newValue)
    {
        super.firePropertyChange(0, propertyName, oldValue, newValue);
    }

    /**
     * @see PNodeView#setLabel( String ) setLabel <B>Note:</B> this replaces:
     * <I>NodeLabel nl = nr.getLabel(); nl.setFont(na.getFont());</I>
     */
    public void setFont(Font font)
    {
        label.setFont(font);
    }

    public void setToolTip(String tip)
    {
        addAttribute("tooltip", tip);
    }

    /**
     * 
     */
    public void setCenter(double x, double y)
    {
        setOffset(x - getWidth() / 2, y - getHeight() / 2);
    }

    public Point2D getCenter()
    {
        return new Point2D.Double(getXOffset() + getWidth() / 2, getYOffset()
                + getHeight() / 2);
    }

    /**
     * 
     */
    public void setLocation(double x, double y)
    {
        setOffset(x, y);
    }

    /**
     * 
     */
    public void setSize(double w, double h)
    {
        setHeight(h);
        setWidth(w);
    }

    /**
     * 
     */
    public String getLabelText()
    {
        return label.getText();
    }

    public Image getImage()
    {
        return image;
    }

    public void setImage(Image image)
    {
        this.image = image;
    }
} // class PNodeEx

