package seco.gui;

import java.awt.Rectangle;

import javax.swing.JComponent;

import seco.ThisNiche;
import seco.notebook.PiccoloCanvas;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellVisual;

public class JComponentVisual implements CellVisual
{
    public void bind(CellGroupMember element, Object parentVisual)
    {
        Cell cell = (Cell)element;
        JComponent comp = (JComponent)ThisNiche.hg.get(cell.getAtomHandle());
        PiccoloCanvas canvas = (PiccoloCanvas)parentVisual;
        canvas.addComponent(comp, (Rectangle) cell.getAttribute(VisualAttribs.rect),
                ThisNiche.handleOf(element));
    }
 
}