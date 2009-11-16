package seco.talk;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.query.OrderedLinkCondition;
import org.hypergraphdb.query.impl.DefaultKeyBasedQuery;
import org.hypergraphdb.query.impl.PipeQuery;
import org.hypergraphdb.util.ValueSetter;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.gui.PiccoloCanvas;
import seco.gui.TopFrame;
import seco.gui.VisualAttribs;
import seco.things.CellGroupMember;
import seco.things.CellUtils;


/**
 * 
 * <p>
 * Static top-level methods to manage/establish network connectivity of this
 * peer with a Seco network.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
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
        // The query finds the 1 (expected) panel that is linked with the configuration
        // by a plain link. The PipeQuery used to perform this says "all atoms of type
        // ConnectionPanel that are form an ordered link with the configuration atom".
        // 
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
        
       // for(HGHandle h: L)
      //      ThisNiche.hg.remove(h);
      //  L.clear();
        
        HGHandle panelHandle = null;
        if (L.isEmpty())
        {
            ConnectionPanel panel = new ConnectionPanel();
            panel.initComponents();
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
        PiccoloCanvas canvas = TopFrame.getInstance().getCanvas();
        int width = 200;
        int height = 200;
        int x = Math.max(0, canvas.getWidth() - width - width/5);
        int y = height;
        CellGroupMember cell = ThisNiche.hg.get( 
            GUIHelper.addIfNotThere(ThisNiche.TOP_CELL_GROUP_HANDLE,
                                panelHandle,
                                null, 
                                null, 
                                new Rectangle(x, y, width, height)));
        CellUtils.setName(cell, "Seco Network");
        cell.setAttribute(VisualAttribs.showTitle, true);        
        ConnectionPanel panel = ThisNiche.hg.get(panelHandle);
        panel.connect();
        return panel;
    }
    
    private static Map<HGPeerIdentity, ConnectionPanel> peer_panel_map = 
        new HashMap<HGPeerIdentity, ConnectionPanel>();
    
    public static ConnectionPanel getConnectionPanel(HGPeerIdentity id)
    {
        return peer_panel_map.get(id);
    }
    
    public static void registerConnectionPanel(HGPeerIdentity id, ConnectionPanel pan)
    {
        peer_panel_map.put(id, pan);
    }
    
    public static void unregisterConnectionPanel(HGPeerIdentity id)
    {
        peer_panel_map.remove(id);
    }
    
}
