package seco.talk;

import javax.swing.JPanel;

import org.hypergraphdb.annotation.AtomReference;
import org.hypergraphdb.peer.HGPeerIdentity;

import seco.ThisNiche;

public abstract class BaseChatPanel extends JPanel 
                                    implements ConnectionContext.ConnectionContextListener
{
    private static final long serialVersionUID = 5298624293094960295L;

    public BaseChatPanel()
    {
    }

    @AtomReference("symbolic")
    protected ConnectionContext connectionContext;

    public abstract boolean isConnected();

    public HGPeerIdentity getPeerID()
    {
        return connectionContext.getPeer().getIdentity();
    }

    public void addNotify()
    {
        super.addNotify();
        getConnectionContext();
    }

    public void setConnectionContext(ConnectionContext ctx)
    {
        if (this.connectionContext != null)
            this.connectionContext.removeConnectionListener(this);
        this.connectionContext = ctx;
        ctx.addConnectionListener(this);
    }
    
    public ConnectionContext getConnectionContext()
    {
//        if (ctx == null)
//        {
//            ctx = ConnectionManager.getConnectionContext(getPeerID());
//            if (ctx != null)
//            {
//                ctx.addConnectionListener(this);
//                if (ctx.isConnected() && !isConnected())
//                    connected(ctx);
//                else if (!ctx.isConnected() && isConnected())
//                    disconnected(ctx);
//            }
//        }
        return connectionContext;
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
