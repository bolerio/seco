package seco.gui;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.notebook.AppForm;
import seco.notebook.GUIHelper;
import seco.notebook.NotebookUI;
import seco.notebook.PiccoloCanvas;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellVisual;

public class TabbedPaneVisual implements CellVisual
{
    public void bind(CellGroupMember element, Object parentVisual)
    {
        CellGroup group = (CellGroup)element;
        PiccoloCanvas canvas = (PiccoloCanvas) parentVisual;
        for (int i = 0; i < group.getArity(); i++)                
        {
            HGHandle h = group.getTargetAt(i);
            Cell cell = (Cell) ThisNiche.hg.get(h);
            NotebookUI ui = new NotebookUI(cell.getAtomHandle());
            AppForm.getInstance().addNotebookTab(ui);
        } 
        canvas.addComponent(GUIHelper.getJTabbedPane(), group);
    }

}
