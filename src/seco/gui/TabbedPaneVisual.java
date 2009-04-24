package seco.gui;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;

import seco.ThisNiche;
import seco.events.CellGroupChangeEvent;
import seco.events.EventHandler;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.CellVisual;

public class TabbedPaneVisual implements CellVisual, EventHandler
{
    private static final HGPersistentHandle handle = HGHandleFactory
            .makeHandle("55ddbdf0-149d-11de-8c30-0800200c9a66");

    public static HGPersistentHandle getHandle()
    {
        return handle;
    }

    public JComponent bind(CellGroupMember element)
    {
        if(!(element instanceof CellGroup)) 
        {
            System.err.println("TabbedPaneVisual - Unable to create visual for: " + element);
            return null;
        }
        if(CellUtils.isMinimized(element))
            return GUIHelper.getMinimizedUI(element);
        final CellGroup group = //element instanceof CellGroup ? 
                (CellGroup) element; // : (CellGroup) ((Cell) element).getValue();
        HGHandle groupH = ThisNiche.handleOf(element);
        final JTabbedPane tp = (ThisNiche.TABBED_PANE_GROUP_HANDLE
                .equals(groupH)) ? GUIHelper.getJTabbedPane() : TabbedPaneU
                .createTabbedPane(group);
        tp.removeAll();        
        for (int i = 0; i < group.getArity(); i++)
            addChild(tp, group.getTargetAt(i));
        group.setVisualInstance(tp);

        CellUtils.addEventPubSub(CellGroupChangeEvent.HANDLE, groupH,
                getHandle(), getHandle());
        return tp;
    }

    public void handle(HGHandle eventType, Object event, HGHandle publisher,
            HGHandle subscriber)
    {
        if (eventType.equals(CellGroupChangeEvent.HANDLE)
                && subscriber.equals(ThisNiche.handleOf(this)))
        {
            handleEvent((CellGroupChangeEvent) event);
        }
    }

    private void addChild(JTabbedPane tp, HGHandle childH)
    {
        CellGroupMember cell = ThisNiche.hg.get(childH);
        CellVisual visual = CellUtils.getVisual(cell);
        JComponent comp = visual.bind(cell);
        if(comp == null) return;
        comp.putClientProperty(TabbedPaneU.CHILD_HANDLE_KEY, childH);
        String title = (comp.getName() != null) ? comp.getName(): "Untitled";
        tp.addTab(title, comp);
    }

    private void handleEvent(CellGroupChangeEvent e)
    {
        CellGroup group = (CellGroup) ThisNiche.hg.get(e.getCellGroup());
        if (!(group.getVisualInstance() instanceof JTabbedPane)) return;
        JTabbedPane tp = (JTabbedPane) group.getVisualInstance();
        HGHandle[] added = e.getChildrenAdded();
        HGHandle[] removed = e.getChildrenRemoved();
        // int index = e.getIndex();
        if (removed != null && removed.length > 0)
            for (int i = 0; i < removed.length; i++)
                removeTab(tp, removed[i]);
        if (added != null && added.length > 0)
            for (int i = 0; i < added.length; i++)
                addChild(tp, added[i]);
        tp.setSelectedIndex(tp.getTabCount() - 1);
    }

    private static void removeTab(JTabbedPane tp, HGHandle h)
    {
        for (int i = 0; i < tp.getTabCount(); i++)
        {
            HGHandle inH = TabbedPaneU.getHandleAt(tp, i);
            if (!h.equals(inH)) continue;
            tp.remove(i);
            return;
        }
    }

}
