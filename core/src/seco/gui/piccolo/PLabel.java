package seco.gui.piccolo;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.util.PBoundsLocator;
import edu.umd.cs.piccolox.util.PNodeLocator;


/**
 * The Label class for nodes and edges
 */
public class PLabel extends PrintingFixTextNode
    implements PropertyChangeListener {
    static PNodeLocator pbl;

    public static int CENTER = 0;
    public static int NORTH = 1;
    public static int SOUTH = 2;
    public static int WEST = 3;
    public static int EAST = 4;
    public static int NORTHWEST = 5;
    public static int NORTHEAST = 6;
    public static int SOUTHEAST = 7;
    public static int SOUTHWEST = 8;
    public static int SOURCE_BOUND = 9;
    public static int TARGET_BOUND = 10;
     
   

    // The FEdge or FNode to which we are bound
    PNode boundObject;

    // The direction and amount of offset from the bound Object
    Point2D offsetAmount;

    // The text displayed by this node
    String text;
    protected int labelLocation = 0;
    boolean smartColor = false;

    /**
     * Creates a new PLabel object.
     *
     * @param text DOCUMENT ME!
     * @param node DOCUMENT ME!
     */
    public PLabel(
        String text,
        PNode node) {
        super(text);
        this.text = text;
        this.boundObject = node;
        boundObject.addPropertyChangeListener(this);
        updatePosition();
    }

  public void setWordWrap ( boolean wrap ) {}


    /**
     * DOCUMENT ME!
     */
    public void updatePosition() {
        if (labelLocation == NORTH) {
            pbl = PBoundsLocator.createNorthLocator(boundObject);
            setOffset(pbl.locateX() - (0.5 * getBounds()
                                       .getWidth()),
                      pbl.locateY() + ( getBounds()
                                       .getHeight()));
        } else if (labelLocation == NORTHWEST) {
            pbl = PBoundsLocator.createNorthWestLocator(boundObject);
            setOffset(pbl.locateX() - ( getBounds()
                                       .getWidth()),
                      pbl.locateY() );
        
        } else if (labelLocation == NORTHEAST) {
            pbl = PBoundsLocator.createNorthEastLocator(boundObject);
            setOffset(pbl.locateX() ,
                      pbl.locateY() );
        } else if (labelLocation == SOUTH) {
            pbl = PBoundsLocator.createSouthLocator(boundObject);
            setOffset(pbl.locateX() ,
                      pbl.locateY() - ( getBounds()
                                       .getHeight()));
        } else if (labelLocation == SOUTHWEST) {
            pbl = PBoundsLocator.createSouthWestLocator(boundObject);
        } else if (labelLocation == SOUTHEAST) {
            pbl = PBoundsLocator.createSouthEastLocator(boundObject);
        } else if (labelLocation == EAST) {
            pbl = PBoundsLocator.createEastLocator(boundObject);
        } else if (labelLocation == WEST) {
            pbl = PBoundsLocator.createWestLocator(boundObject);
        } else {
            pbl = new PNodeLocator(boundObject);
            setOffset(pbl.locateX() - (0.5 * getBounds()
                                       .getWidth()),
                  pbl.locateY() - (0.5 * getBounds()
                                   .getHeight()));
        }
  

        if ( smartColor ) {
          if ( labelLocation == CENTER ) {
            Paint paint = boundObject.getPaint();
            if ( paint instanceof Color ) {
              Color color = ( Color )paint;
              int rgb = 0;
              rgb += color.getRed();
              rgb += color.getGreen();
              rgb += color.getBlue();
              if ( rgb < 382 ) {
                setTextPaint( Color.white );
              } else {
                setTextPaint( color.black );
              }
          }

          }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param loc DOCUMENT ME!
     */
    public void setLabelLocation(int loc) {
        labelLocation = loc;
        updatePosition();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getLabelLocation() {
        return labelLocation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName()
                   .equals("identifier")) {
            setText((String) evt.getNewValue());
        }
        updatePosition();
    }

    public void paint(PPaintContext paintContext) {
      // double s = paintContext.getScale();
      //if (s > .2 ) {
            super.paint(paintContext);
            // }
    }

    /**
     * DOCUMENT ME!
     *
     * @param i DOCUMENT ME!
     */
    public void setPositionHint(int i) {
        // TODO
    }

    /**
     * DOCUMENT ME!
     *
     * @param i DOCUMENT ME!
     */
    public void setSize(int i) {
        //TODO 
    }
}
