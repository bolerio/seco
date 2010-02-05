package seco.gui.piccolo;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;


public class PToolTipHandler extends PBasicInputEventHandler {

  PCamera camera;
  PToolTip tooltipNode;

  public PToolTipHandler ( PCamera camera ) {
    super();
    this.camera = camera;
    tooltipNode = new PToolTip();
    tooltipNode.setPickable(false);
    //camera.addChild( tooltipNode );
  }

  public void mouseMoved(PInputEvent event) {
    updateToolTip(event);
  }

  public void mouseDragged(PInputEvent event) {
    updateToolTip(event);
  }

  public void updateToolTip(PInputEvent event) {
    try {
      java.awt.geom.Point2D p = event.getCanvasPosition();
      event.getPath().canvasToLocal( p, camera);
      String tip = ( String )event.getInputManager().getMouseOver().getPickedNode().getAttribute("tooltip");
      if ( tip != null ) { 
        camera.addChild( tooltipNode );
        tooltipNode.setText( tip ); 
        tooltipNode.setOffset(p.getX() + 8, 
                              p.getY() - 8);
      } else {
        tooltipNode.setText( "" );
        camera.removeChild(tooltipNode);
      }
    
    } catch ( Exception e ) {}
  }
}



