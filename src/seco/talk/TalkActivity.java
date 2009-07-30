package seco.talk;

import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.createMessage;
import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;
import static org.hypergraphdb.peer.Messages.makeReply;
import static org.hypergraphdb.peer.Structs.combine;
import static org.hypergraphdb.peer.Structs.getPart;
import static org.hypergraphdb.peer.Structs.struct;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.swing.JOptionPane;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Message;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.SubgraphManager;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.WorkflowState;

import seco.ThisNiche;
import seco.events.EvalCellEvent;
import seco.events.EventPubSub;
import seco.gui.GUIHelper;
import seco.gui.NBUIVisual;
import seco.gui.VisualAttribs;
import seco.things.Cell;
import seco.things.CellGroup;


/**
 * <p>
 * This activity handles communication through a chat window b/w two people. 
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class TalkActivity extends FSMActivity
{
    public static final String TYPENAME = "seco-talk";
    
    private HGPeerIdentity friend;
    private TalkPanel talkPanel;
    
    private void transferAtom(Message msg, HGHandle atom, Set<HGHandle> done, boolean mainAtom)
    {
        if (done.contains(atom))
            return;
        done.add(atom);
        Object x = ThisNiche.hg.get(atom);
        if (x instanceof Cell)
        {
            transferAtom(msg, ((Cell)x).getAtomHandle(), done, false);
            List<HGHandle> events = hg.findAll(ThisNiche.hg, 
                                               hg.and(hg.type(EventPubSub.class),
                                               hg.orderedLink(EvalCellEvent.HANDLE, 
                                                              atom,
                                                              hg.anyHandle(),
                                                              hg.anyHandle())));
            for (HGHandle ev : events)
            {
                EventPubSub pubsub = ThisNiche.hg.get(ev);
                // Transfer only output cells, not the notebook or anybody else.
                if (ThisNiche.hg.get(pubsub.getSubscriber()) instanceof Cell)
                {
                    transferAtom(msg, ev, done, false);
                    transferAtom(msg, pubsub.getTargetAt(2), done, false);
                    transferAtom(msg, pubsub.getTargetAt(3), done, false);
                }
            }
        }
        else if (x instanceof CellGroup)
        {
            CellGroup group = (CellGroup)x;
            for (int i = 0; i < group.getArity(); i++)
                transferAtom(msg, group.getTargetAt(i), done, false);
        }
        Message reply = getReply(msg, Performative.InformRef);
        combine(reply, struct(CONTENT, 
                         struct("type", mainAtom ? "atom" : "auxiliary-atom", 
                                "atom", SubgraphManager.getTransferAtomRepresentation(getThisPeer().getGraph(), 
                                                                                      atom))));
        post(getSender(msg), reply);
    }
    
    void openPanel()
    {
        ConnectionPanel connectionPanel = 
            (ConnectionPanel)getThisPeer().getObjectContext().get(ConnectionPanel.class.getName());
        if (!connectionPanel.talks.containsKey(friend))
            connectionPanel.talks.put(friend, this);
        if (talkPanel == null)
        {
            talkPanel = hg.getOne(ThisNiche.hg, hg.and(hg.type(TalkPanel.class), hg.eq("friend", friend)));
            if (talkPanel == null)
            {
                talkPanel = new TalkPanel(this);
                talkPanel.setFriend(friend);
                ThisNiche.hg.add(talkPanel);
            }
        }
        talkPanel.setTalkActivity(this);
        Map<Object, Object> attribs = new HashMap<Object, Object>();
        //TODO: some sort of naming 
        attribs.put(VisualAttribs.name, "Connection Panel");
        attribs.put(VisualAttribs.showTitle, true);
        GUIHelper.addIfNotThere(ThisNiche.TOP_CELL_GROUP_HANDLE, 
                                ThisNiche.hg.getHandle(talkPanel), 
                                null, 
                                null, 
                                new Rectangle(500, 200, 200, 100),
                                attribs);            
    }
    
    private void initFriend(Message msg)
    {
        HGPeerIdentity id = getThisPeer().getIdentity(getSender(msg));
        if (friend == null)
        {
            if (id != null)
            {
                friend = id;
                openPanel();
            }
            else
                throw new RuntimeException("Unknown peer " + getSender(msg) + " attempting to talk.");
        }
        else if (!friend.equals(id))
            throw new RuntimeException("Wrong activity for Talk msg, received from " + 
                                       id + ", but expecting " + friend);
    }
    
    public TalkActivity(HyperGraphPeer thisPeer)
    {
        super(thisPeer);
    }

    public TalkActivity(HyperGraphPeer thisPeer, HGPeerIdentity friend)
    {
        this(thisPeer);
        this.friend = friend;
        openPanel();
    }
    
    public TalkActivity(HyperGraphPeer thisPeer, UUID id)
    {
        super(thisPeer, id);
    }

    public TalkActivity(HyperGraphPeer thisPeer, UUID id, HGPeerIdentity friend)
    {
        super(thisPeer, id);
        this.friend = friend;
        openPanel();
    }

    public TalkPanel getPanel()
    {
        return talkPanel;
    }
    
    public void chat(String text)
    {
        assert friend == null : new RuntimeException("No destination for TalkActivity.");
        final Message msg = makeReply(this, Performative.Inform, null);
        combine(msg, struct(Messages.CONTENT, 
          struct("type", "chat", "text", text)));
        post(friend, msg);
    }
    
    public void sendAtom(HGHandle atom)
    {
        assert friend == null : new RuntimeException("No destination for TalkActivity.");
        Message msg = makeReply(this, Performative.InformRef, null);
        combine(msg, struct(Messages.CONTENT, 
                            struct("type", "atom", "atom", atom)));
        post(friend, msg);
    }
    
    public void offerAtom(HGHandle atom, String label)
    {
        assert friend == null : new RuntimeException("No destination for TalkActivity.");
        Message msg = createMessage(Performative.Propose, TalkActivity.this);
        combine(msg, struct(Messages.CONTENT,
                             struct("type", "atom", 
                                    "label", label,
                                    "atom", atom)));        
        post(friend, msg);
    }

    public void close()
    {
        getState().assign(WorkflowState.Completed);
    }
    
    protected void onPeerFailure(Message msg)
    {
        JOptionPane.showMessageDialog(talkPanel, getPart(msg, CONTENT));
    }
    
    @FromState("Started")
    @OnMessage(performative="Propose")
    public WorkflowState onPropose(final Message msg)
    {
        initFriend(msg);
        Map<String, Object> content = getPart(msg, CONTENT);
        String type = (String)content.get("type");
        assert type != null : new RuntimeException("No type in TalkActivity content.");
        if ("start-chat".equals(type))
        {
            Message reply = getReply(msg, Performative.AcceptProposal);
            send(friend, reply);            
        }       
        else if ("atom".equals(type))
        {
            final HGPersistentHandle atomHandle = getPart(content, "atom");
            if (msg.getPerformative() == Performative.Propose)
            {
                talkPanel.getChatPane().actionableChatFrom(friend, (String)getPart(content, "label"), 
                "Accept", new Runnable() {
                    public void run()
                    {
                        System.out.println("Accepting atom " + atomHandle);
                        Message msg = makeReply(TalkActivity.this, Performative.QueryRef, null);
                        combine(msg, struct(CONTENT, 
                                            struct("type", "atom", "atom", atomHandle)));
                        post(friend, msg);                        
                    }
                },
                "Reject", new Runnable() {
                    public void run()
                    {
                        System.out.println("Rejecting atom " + atomHandle);
                        reply(msg, Performative.RejectProposal, atomHandle);
                    }
                }
                );
            }            
        }
        return null;
    }

    @FromState("Started")
    @OnMessage(performative="AcceptProposal")
    public WorkflowState onAcceptProposal(final Message msg)
    {
        return null;
    }

    @FromState("Started")
    @OnMessage(performative="RejectProposal")
    public WorkflowState onRejectProposal(final Message msg)
    {
        talkPanel.getChatPane().chatFrom(friend, "Content " + getPart(msg, CONTENT) + " rejected.");
        return null;
    }
    
    @FromState("Started")
    @OnMessage(performative="Inform")
    public WorkflowState onInform(final Message msg)
    {
        initFriend(msg);
        Map<String, Object> content = getPart(msg, CONTENT);
        String type = (String)content.get("type");
        assert type != null : new RuntimeException("No type in TalkActivity content.");
        if ("chat".equals(type))
        {
            String text = getPart(content, "text");
            assert text != null : new RuntimeException("No text in TalkActivity chat.");            
//            EventDispatcher.dispatch(U.hgType(ChatEvent.class), 
//                                     friend.getId(), 
//                                     new ChatEvent(friend, text));
            talkPanel.getChatPane().chatFrom(friend, text);
        }        
        return null;
    }

    @FromState("Started")
    @OnMessage(performative="QueryRef")
    public WorkflowState onQueryRef(final Message msg)
    {
        try
        {
        initFriend(msg);
        Map<String, Object> content = getPart(msg, CONTENT);
        String type = (String)content.get("type");
        assert type != null : new RuntimeException("No type in TalkActivity content.");
        if ("atom".equals(type))
        {
            HGPersistentHandle atomHandle = getPart(content, "atom");
            transferAtom(msg, atomHandle, new HashSet<HGHandle>(), true);
        }
        }catch (Throwable t) { t.printStackTrace(); }
        return null;        
    }
    
    @FromState("Started")
    @OnMessage(performative="InformRef")
    public WorkflowState onInformRef(final Message msg)
    {
        try
        {
        initFriend(msg);
        Map<String, Object> content = getPart(msg, CONTENT);
        final String type = (String)content.get("type");
        assert type != null : new RuntimeException("No type in TalkActivity content.");
        if ("atom".equals(type) || "auxiliary-atom".equals(type))
        {
            final Object atom = getPart(msg, CONTENT, "atom");
            if (atom == null)
            {
                reply(msg, Performative.Failure, "missing atom data");
                return null;
            }
            else return
             getThisPeer().getGraph().getTransactionManager().transact(new Callable<WorkflowState>() {
                 public WorkflowState call() throws Exception
                 {
                     // Store the atom itself locally, overwriting any previous version
                     HGHandle atomHandle = SubgraphManager.writeTransferedGraph(
                                   atom, 
                                   getThisPeer().getGraph()).iterator().next();
                     if ("atom".equals(type))
                         GUIHelper.addIfNotThere(ThisNiche.TOP_CELL_GROUP_HANDLE, 
                                                 atomHandle, 
                                                 NBUIVisual.getHandle(), 
                                                 null, 
                                                 new Rectangle(300, 200, 100, 100));
                     return null;
                 }
             });            
        }
        }catch (Throwable t) { t.printStackTrace(); }
        return null;
    }    

    @Override
    public void initiate()
    {
        assert friend == null : new RuntimeException("No destination for TalkActivity.");
        Message msg = createMessage(Performative.Propose, TalkActivity.this);
        combine(msg, struct(Messages.CONTENT, struct("type", "start-chat")));
        post(friend, msg);
    }
     
    @Override
    public String getType()
    {
        return TYPENAME;
    }    
}