package seco.talk;

import java.awt.Rectangle;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.query.OrderedLinkCondition;
import org.hypergraphdb.query.impl.DefaultKeyBasedQuery;
import org.hypergraphdb.query.impl.PipeQuery;
import org.hypergraphdb.util.ValueSetter;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.things.CellGroup;

public class ConnectionManager
{
    public static ConnectionPanel openConnectionPanel(ConnectionConfig config)
    {
        //
        // First, we need to find if there's a ConnectionPanel for that configuration
        // already open.
        //
        
        HGHandle configHandle = ThisNiche.hg.getHandle(config);
        
        if (configHandle == null)
            configHandle = ThisNiche.hg.add(config);
        
        // Hmm, it's almost easier to rewrite the following to directly use result sets.
        HGQuery<HGHandle> inquery = HGQuery.make(ThisNiche.hg, hg.type(ConnectionPanel.class));
        final OrderedLinkCondition cond = hg.orderedLink(hg.anyHandle(), configHandle);        
        HGQuery<HGHandle> query = new PipeQuery<HGHandle, HGHandle>
            (inquery,
            new DefaultKeyBasedQuery<HGHandle, HGHandle>(ThisNiche.hg,
                                                        hg.apply(hg.targetAt(ThisNiche.hg, 0), cond),
                                                        new ValueSetter<HGHandle>()
                                                        {
                                                            public void set(HGHandle h)
                                                            {
                                                                cond.getTargets()[0] = h;
                                                            }
                                                        }
            ));
        query.setHyperGraph(ThisNiche.hg);
        List<HGHandle> L = hg.findAll(query);
        HGHandle panelHandle = null;
        if (L.isEmpty())
        {
            ConnectionPanel panel = new ConnectionPanel();
//            panel.setConnectionConfig(config);
            panelHandle = ThisNiche.hg.add(panel);           
            ThisNiche.hg.add(new HGPlainLink(panelHandle, configHandle));            
        }
        else if (L.size() == 1)
        {
            panelHandle = L.get(0);
        }
        else
            throw new RuntimeException("More than 1 ConnectionPanel associated with configuration " + 
                                       configHandle);
        
        // Find an existing cell with that panel:
        // hmm, not sure what needs to be done here....
        GUIHelper.addIfNotThere(ThisNiche.TOP_CELL_GROUP_HANDLE,
                                panelHandle,
                                null, 
                                null, 
                                new Rectangle(800, 100, 200, 100));        
        ConnectionPanel panel = ThisNiche.hg.get(panelHandle);
        panel.initComponents();
        panel.connect();
        return panel;
    }
}
