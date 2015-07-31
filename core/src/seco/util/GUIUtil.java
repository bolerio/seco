package seco.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.gui.PSwingNode;
import seco.gui.PiccoloCanvas;
import seco.gui.TopFrame;
import seco.gui.common.DialogDescriptor;
import seco.gui.common.DialogDisplayer;
import seco.gui.common.NotifyDescriptor;
import seco.gui.common.NotifyDescriptor.Confirmation;
import seco.notebook.html.ElementTreePanel;
import edu.umd.cs.piccolo.util.PBounds;

public class GUIUtil
{
   private GUIUtil(){}

/**
 * Helper method that performs the necessary transformations to adjust coordinates 
 * of a point given the component to which it belongs  
 * @param c The component displayed in canvas
 * @param pt The point
 * @return adjusted point
 */
public static Point adjustPointInPicollo(JComponent c, Point pt)
{
    PSwingNode ps = GUIHelper.getPSwingNode(c);
    if (ps == null) return pt;
    PiccoloCanvas canvas = ps.getCanvas();
    PBounds r1c = ps.getBounds();
    ps.localToGlobal(r1c);
    canvas.getCamera().globalToLocal(r1c);
    Rectangle2D r = canvas.getCamera().getViewTransform()
            .createTransformedShape(r1c).getBounds2D();
    return new Point((int) (r.getX() + pt.x), (int) (r.getY() + pt.y));
}

public static Frame getFrame(Component c)
{
    Component p = c.getParent();
    while (p != null && !(p instanceof Frame))
    	p = p.getParent();
    
    return (p instanceof Frame) ? (Frame) p : null;
}

public static Frame getFrame(ActionEvent e)
{
    if(e.getSource() instanceof Component) 
        return getFrame((Component) e.getSource());
    return  getFrame();   
}

public static TopFrame getFrame()
{
    return (TopFrame)ThisNiche.guiController.getFrame();
}

public static boolean showConfirmDlg(String message)
{
    NotifyDescriptor d = new NotifyDescriptor.Confirmation(
            getFrame(), message,
            NotifyDescriptor.OK_CANCEL_OPTION);
    return DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION;
}

public static void centerOnScreen(Component c)
{
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); 
	int x = (screenSize.width - c.getWidth()) / 2;
	int y = (screenSize.height - c.getHeight()) / 2;
	c.setLocation(x, y);    	
}

public static void createAndShowDlg(Frame f, String title, Component c, Dimension dim)
{
    DialogDescriptor dd = new DialogDescriptor(f, c, title);
    DialogDisplayer.getDefault().notify(dd);
//    
//    JDialog dialog = new JDialog(f, title);
//    if(dim != null)  dialog.setSize(dim);
//    dialog.add(c);
//    if(f == null) dialog.setIconImage(GUIHelper.LOGO_IMAGE);
//    centerOnScreen(dialog);
//    dialog.setVisible(true);
}

public static void createAndShowDlg(String title, Component c, Dimension dim)
{
    createAndShowDlg(getFrame(), title, c, dim);
}

public static void showDlg(JDialog dialog)
{
    if(dialog.getIconImages() == null || dialog.getIconImages().isEmpty()) 
        dialog.setIconImage(GUIHelper.LOGO_IMAGE);
    centerOnScreen(dialog);
    dialog.setVisible(true);
}

/**
 * Traverses the given component's parent tree looking for an
 * instance of JDialog, and return it. If not found, return null.
 * @param c The component
 */
public static JDialog getParentDialog(Component c)
{
    Component p = c.getParent();
    while (p != null && !(p instanceof JDialog))
        p = p.getParent();
    
    return (p instanceof JDialog) ? (JDialog) p : null;
}

public static void addEscapeListener(final JDialog dialog) {
    ActionListener escListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            dialog.setVisible(false);
        }
    };

    dialog.getRootPane().registerKeyboardAction(escListener,
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

}
}
