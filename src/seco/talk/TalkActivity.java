package seco.talk;

import java.util.Map;
import java.util.UUID;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Message;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.protocol.Performative;
import org.hypergraphdb.peer.workflow.Activity;
import org.hypergraphdb.peer.workflow.WorkflowState;

import seco.U;
import seco.events.EventDispatcher;
import static org.hypergraphdb.peer.Messages.*;
import static org.hypergraphdb.peer.Structs.*;

/**
 * <p>
 * This activity handles communication b/w 
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class TalkActivity extends Activity
{
    public static final String TYPENAME = "seco-talk";
    
    private HGPeerIdentity friend;
    
    private void initFriend(Message msg)
    {
        HGPeerIdentity id = getThisPeer().getIdentity(getSender(msg));
        if (friend == null)
            friend = id;
        else if (!friend.equals(id))
            throw new RuntimeException("Wrong activity for Talk msg, received from " + 
                                       id + ", but expecting " + friend);
    }
    
    public TalkActivity(HyperGraphPeer thisPeer)
    {
        super(thisPeer);
    }

    public TalkActivity(HyperGraphPeer thisPeer, UUID id)
    {
        super(thisPeer, id);
    }

    public TalkActivity(HyperGraphPeer thisPeer, UUID id, HGPeerIdentity friend)
    {
        super(thisPeer, id);
        this.friend = friend;
    }

    public void chat(String text)
    {
        assert friend == null : new RuntimeException("No destination for TalkActivity.");
        Message msg = createMessage(Performative.QueryRef, TalkActivity.this);
        combine(msg, struct(Messages.CONTENT, 
          struct("type", "chat", "text", text)));
        send(friend, msg);
    }
    
    public void sendAtom(HGHandle atom)
    {
        assert friend == null : new RuntimeException("No destination for TalkActivity.");
        Message msg = createMessage(Performative.QueryRef, TalkActivity.this);
        combine(msg, struct(Messages.CONTENT, 
          struct("type", "atom", "atom", atom)));
        send(friend, msg);
    }
    
    public void close()
    {
        getState().assign(WorkflowState.Completed);
    }
    
    @Override
    public void handleMessage(Message msg)
    {
        initFriend(msg);
        Map<String, Object> content = getPart(msg, CONTENT);
        String type = (String)content.get("type");
        assert type != null : new RuntimeException("No type in TalkActivity content.");
        if ("chat".equals(type))
        {
            String text = getPart(content, "text");
            assert text != null : new RuntimeException("No text in TalkActivity chat.");            
            EventDispatcher.dispatch(U.hgType(ChatEvent.class), 
                                     friend.getId(), 
                                     new ChatEvent(friend, text));
        }
        else if ("atom".equals(type))
        {
            HGPersistentHandle atomHandle = getPart(content, "atom");
            EventDispatcher.dispatch(U.hgType(ChatEvent.class), 
                                     friend.getId(), 
                                     new AtomProposedEvent(friend, atomHandle));            
        }
        else
            throw new RuntimeException("Unreadable TalkActivity message content " + content);
    }

    @Override
    public void initiate()
    {
        // nothing to do...
    }
    
    @Override
    public String getType()
    {
        return TYPENAME;
    }
}