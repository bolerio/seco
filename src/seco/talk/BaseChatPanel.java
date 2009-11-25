package seco.talk;

import javax.swing.JPanel;

import org.hypergraphdb.peer.HGPeerIdentity;

public abstract class BaseChatPanel extends JPanel implements
ConnectionContext.ConnectionListener
{
    public BaseChatPanel()
    {
    }
    
    public BaseChatPanel(HGPeerIdentity peerID)
    {
       this.peerID = peerID;
    }

    protected HGPeerIdentity peerID;
    protected ConnectionContext ctx;
 
    
    
    public abstract boolean isConnected();
    
    public HGPeerIdentity getPeerID()
    {
        return peerID;
    }

    public void setPeerID(HGPeerIdentity peerID)
    {
        this.peerID = peerID;
    }
    
    public void addNotify()
    {
        super.addNotify();
        getConnectionContext();
    }
    
    public ConnectionContext getConnectionContext()
    {
        if(ctx == null)
        {
          ctx = ConnectionManager.getConnectionContext(getPeerID());
          if(ctx != null)
          {
             ctx.addConnectionListener(this);
             if(ctx.isConnected() && !isConnected())
                connected(ctx);
             else if(!ctx.isConnected() && isConnected())
                disconnected(ctx);
          }
        }
        return ctx;
    }
    
   

    @Override
    public void connected(ConnectionContext ctx)
    {
    }
    
    @Override
    public void disconnected(ConnectionContext ctx)
    {
    }
    
   

}
