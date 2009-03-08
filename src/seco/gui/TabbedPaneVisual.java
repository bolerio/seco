package seco.gui;

import javax.swing.JTabbedPane;
import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.notebook.GUIHelper;
import seco.notebook.NotebookUI;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellVisual;

public class TabbedPaneVisual implements CellVisual
{
    public void bind(CellGroupMember element, Object parentVisual)
    {
        final CellGroup group = (CellGroup) element;
        HGHandle groupH = ThisNiche.handleOf(element);
        final JTabbedPane tp = (ThisNiche.TABBED_PANE_GROUP_HANDLE
                .equals(groupH)) ? GUIHelper.getJTabbedPane() : TabbedPaneU
                .createTabbedPane();
        for (int i = 0; i < group.getArity(); i++)
        {
            HGHandle h = group.getTargetAt(i);
            Cell cell = (Cell) ThisNiche.hg.get(h);
            NotebookUI ui = new NotebookUI(cell.getAtomHandle());
            TabbedPaneU.addNotebookTab(tp, ui, false);
        }

        PiccoloCanvas canvas = (PiccoloCanvas) parentVisual;
        if (canvas != null) canvas.addComponent(tp, group);
    }

}
