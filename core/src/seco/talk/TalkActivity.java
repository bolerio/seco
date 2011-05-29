package seco.talk;

import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;
import static org.hypergraphdb.peer.Messages.makeReply;
import static org.hypergraphdb.peer.Structs.combine;
import static org.hypergraphdb.peer.Structs.getPart;
import static org.hypergraphdb.peer.Structs.struct;

import java.awt.Rectangle;
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
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.SubgraphManager;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.WorkflowState;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;

import seco.ThisNiche;
import seco.events.EvalCellEvent;
import seco.events.EventPubSub;
import seco.gui.GUIHelper;
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

    private String friendId;
    private TalkPanel talkPanel;

    private void transferAtom(Message msg, 
                              HGHandle atom, 
                              Set<HGHandle> done,
                              boolean mainAtom)
    {
        if (done.contains(atom))
            return;
        System.out.println("");
        done.add(atom);
        Object x = ThisNiche.graph.get(atom);
        if (x instanceof Cell)
        {
            transferAtom(msg, ((Cell) x).getAtomHandle(), done, false);
            // List<HGHandle> events = hg.findAll(ThisNiche.hg,
            // hg.and(hg.type(EventPubSub.class),
            // hg.orderedLink(EvalCellEvent.HANDLE,
            // atom,
            // hg.anyHandle(),
            // hg.anyHandle())));
            // for (HGHandle ev : events)
            // {
            // EventPubSub pubsub = ThisNiche.hg.get(ev);
            // // Transfer only output cells, not the notebook or anybody else.
            // if (pubsub.getSubscriber().equals(pubsub.getEventHandler()) &&
            // ThisNiche.hg.get(pubsub.getSubscriber()) instanceof Cell)
            // {
            // transferAtom(msg, ev, done, false);
            // transferAtom(msg, pubsub.getSubscriber(), done, false);
            // //transferAtom(msg, pubsub.getEventHandler(), done, false);
            // }
            // }
            List<EventPubSub> subscriptions = hg.getAll(ThisNiche.graph,
                                                        hg.and(hg.type(EventPubSub.class),
                                                               hg.incident(EvalCellEvent.HANDLE),
                                                               hg.incident(atom),
                                                               hg.orderedLink(new HGHandle[] {
                                                                       EvalCellEvent.HANDLE,
                                                                       atom,
                                                                       ThisNiche.graph.getHandleFactory()
                                                                               .anyHandle(),
                                                                       ThisNiche.graph.getHandleFactory()
                                                                               .anyHandle() })));
            for (EventPubSub s : subscriptions)
            {
                Object handler = ThisNiche.graph.get(s.getEventHandler());
                if (s.getEventHandler().equals(s.getSubscriber())
                        && handler instanceof Cell)
                {
                    // System.out.println("Transfering EventPubSub: " + s);
                    transferAtom(msg, s.getSubscriber(), done, false);
                    transferAtom(msg, ThisNiche.handleOf(s), done, false);
                }
            }
        }
        else if (x instanceof CellGroup)
        {
            CellGroup group = (CellGroup) x;
            for (int i = 0; i < group.getArity(); i++)
                transferAtom(msg, group.getTargetAt(i), done, false);

        }
        Message reply = getReply(msg, Performative.InformRef);
        combine(reply,
                struct(CONTENT,
                       struct("type",
                              mainAtom ? "atom" : "auxiliary-atom",
                              "atom",
                              SubgraphManager.getTransferAtomRepresentation(getThisPeer().getGraph(),
                                                                            atom))));

        // should block on posting to retain the order and send the main
        // atom last
        try
        {
            post(getSender(msg), reply).get();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    void openPanel()
    {
        if (!getConnectionContext().talks.containsKey(friendId))
            getConnectionContext().talks.put(friendId, this);
        if (talkPanel != null)
            ctx.openTalkPanel(talkPanel);
        else
            talkPanel = getConnectionContext().openTalkPanel(friendId);
    }

    private void initFriend(Message msg)
    {
        HGPeerIdentity id = getThisPeer().getIdentity(getSender(msg));
        if (friendId == null)
        {
            if (id != null)
            {
                friendId = (String)getThisPeer().getNetworkTarget(id);
                openPanel();
            }
            else
                throw new RuntimeException("Unknown peer " + getSender(msg)
                        + " attempting to talk.");
        }
        else if (!friendId.equals((String)getThisPeer().getNetworkTarget(id)))
            throw new RuntimeException(
                    "Wrong activity for Talk msg, received from " + id
                            + ", but expecting " + friendId);
    }

    public TalkActivity(HyperGraphPeer thisPeer)
    {
        super(thisPeer);
    }

    public TalkActivity(HyperGraphPeer thisPeer, 
                        String friendId,
                        TalkPanel panel)
    {
        this(thisPeer);
        this.friendId = friendId;
        this.talkPanel = panel;
        openPanel();

    }

    public TalkActivity(HyperGraphPeer thisPeer, UUID id)
    {
        super(thisPeer, id);
    }

    public TalkActivity(HyperGraphPeer thisPeer, UUID id, String friendId)
    {
        super(thisPeer, id);
        this.friendId = friendId;
        openPanel();
    }

    public TalkPanel getPanel()
    {
        return talkPanel;
    }

    // void setPanel(TalkPanel panel)
    // {
    // this.talkPanel = panel;
    // }

    public void chat(String text)
    {
        assert friendId == null : new RuntimeException(
                "No destination for TalkActivity.");
        final Message msg = makeReply(this, Performative.Inform, null);
        combine(msg,
                struct(Messages.CONTENT, struct("type", "chat", "text", text)));
        post(getThisPeer().getIdentity(friendId), msg);
    }

    public void sendAtom(HGHandle atom)
    {
        assert friendId == null : new RuntimeException(
                "No destination for TalkActivity.");
        Message msg = makeReply(this, Performative.InformRef, null);
        combine(msg,
                struct(Messages.CONTENT, struct("type", "atom", "atom", atom)));
        post(getThisPeer().getIdentity(friendId), msg);
    }

    public void offerAtom(HGHandle atom, String label)
    {
        assert friendId == null : new RuntimeException(
                "No destination for TalkActivity.");
        Message msg = createMessage(Performative.Propose, TalkActivity.this);
        combine(msg,
                struct(Messages.CONTENT,
                       struct("type", "atom", "label", label, "atom", atom)));
        post(getThisPeer().getIdentity(friendId), msg);
    }

    public void close()
    {
        getState().assign(WorkflowState.Completed);
    }

    protected void onPeerFailure(Message msg)
    {
        JOptionPane.showMessageDialog(getPanel(), getPart(msg, CONTENT));
    }

    @FromState("Started")
    @OnMessage(performative = "Propose")
    public WorkflowState onPropose(final Message msg)
    {
        initFriend(msg);
        Map<String, Object> content = getPart(msg, CONTENT);
        String type = (String) content.get("type");
        assert type != null : new RuntimeException(
                "No type in TalkActivity content.");
        if ("start-chat".equals(type))
        {
            Message reply = getReply(msg, Performative.AcceptProposal);
            send(getThisPeer().getIdentity(friendId), reply);
        }
        else if ("atom".equals(type))
        {
            final HGPersistentHandle atomHandle = getPart(content, "atom");
            if (msg.getPerformative() == Performative.Propose)
            {
                getPanel().getChatPane()
                        .actionableChatFrom(getSender(msg).toString(),
                                            (String) getPart(content, "label"),
                                            "Accept",
                                            new Runnable() {
                                                public void run()
                                                {
                                                    System.out.println("Accepting atom "
                                                            + atomHandle);
                                                    Message msg = makeReply(TalkActivity.this,
                                                                            Performative.QueryRef,
                                                                            null);
                                                    combine(msg,
                                                            struct(CONTENT,
                                                                   struct("type",
                                                                          "atom",
                                                                          "atom",
                                                                          atomHandle)));
                                                    post(getThisPeer().getIdentity(friendId), msg);
                                                }
                                            },
                                            "Reject",
                                            new Runnable() {
                                                public void run()
                                                {
                                                    System.out.println("Rejecting atom "
                                                            + atomHandle);
                                                    reply(msg,
                                                          Performative.RejectProposal,
                                                          atomHandle);
                                                }
                                            });
            }
        }
        return null;
    }

    @FromState("Started")
    @OnMessage(performative = "AcceptProposal")
    public WorkflowState onAcceptProposal(final Message msg)
    {
        return null;
    }

    @FromState("Started")
    @OnMessage(performative = "RejectProposal")
    public WorkflowState onRejectProposal(final Message msg)
    {
        getPanel().getChatPane().chatFrom(getSender(msg).toString(),
                                          "Content " + getPart(msg, CONTENT)
                                                  + " rejected.");
        return null;
    }

    @FromState("Started")
    @OnMessage(performative = "Inform")
    public WorkflowState onInform(final Message msg)
    {
        initFriend(msg);
        Map<String, Object> content = getPart(msg, CONTENT);
        String type = (String) content.get("type");
        assert type != null : new RuntimeException(
                "No type in TalkActivity content.");
        if ("chat".equals(type))
        {
            String text = getPart(content, "text");
            assert text != null : new RuntimeException(
                    "No text in TalkActivity chat.");
            // EventDispatcher.dispatch(U.hgType(ChatEvent.class),
            // friend.getId(),
            // new ChatEvent(friend, text));
            getPanel().getChatPane().chatFrom(getSender(msg).toString(), text);
            ThisNiche.guiController.blink("New message received");
        }
        return null;
    }

    @FromState("Started")
    @OnMessage(performative = "QueryRef")
    public WorkflowState onQueryRef(final Message msg)
    {
        try
        {
            initFriend(msg);
            Map<String, Object> content = getPart(msg, CONTENT);
            String type = (String) content.get("type");
            assert type != null : new RuntimeException(
                    "No type in TalkActivity content.");
            if ("atom".equals(type))
            {
                HGPersistentHandle atomHandle = getPart(content, "atom");
                transferAtom(msg, atomHandle, new HashSet<HGHandle>(), true);
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        return null;
    }

    @FromState("Started")
    @OnMessage(performative = "InformRef")
    public WorkflowState onInformRef(final Message msg)
    {
        try
        {
            initFriend(msg);
            Map<String, Object> content = getPart(msg, CONTENT);
            final String type = (String) content.get("type");
            assert type != null : new RuntimeException(
                    "No type in TalkActivity content.");
            if ("atom".equals(type) || "auxiliary-atom".equals(type))
            {
                final Object atom = getPart(msg, CONTENT, "atom");
                if (atom == null)
                {
                    reply(msg, Performative.Failure, "missing atom data");
                    return null;
                }
                else
                    return getThisPeer().getGraph()
                            .getTransactionManager()
                            .transact(new Callable<WorkflowState>() {
                                public WorkflowState call() throws Exception
                                {
                                    // Store the atom itself locally,
                                    // overwriting any previous version
                                    HGHandle atomHandle = SubgraphManager.writeTransferedGraph(atom,
                                                                                               getThisPeer().getGraph())
                                            .iterator()
                                            .next();
                                    if ("atom".equals(type))
                                        GUIHelper.addIfNotThere(ThisNiche.TOP_CELL_GROUP_HANDLE,
                                                                atomHandle,
                                                                null, // NBUIVisual.getHandle(),
                                                                null,
                                                                new Rectangle(
                                                                        300,
                                                                        200,
                                                                        200,
                                                                        150));
                                    return null;
                                }
                            });
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        return null;
    }

    @Override
    public void initiate()
    {
        assert friendId == null : new RuntimeException(
                "No destination for TalkActivity.");
        Message msg = createMessage(Performative.Propose, TalkActivity.this);
        combine(msg, struct(Messages.CONTENT, struct("type", "start-chat")));
        post(getThisPeer().getIdentity(friendId), msg);
    }

    @Override
    public String getType()
    {
        return TYPENAME;
    }

    public String toString()
    {
        return "activity[" + getId() + "]:" + getType() + ":" + friendId + ":"
                + getThisPeer();
    }

    ConnectionContext ctx;

    public ConnectionContext getConnectionContext()
    {
        if (ctx == null)
        {
            XMPPPeerInterface xmpp = (XMPPPeerInterface)getThisPeer().getPeerInterface();            
            ConnectionConfig config = new ConnectionConfig();
            config.setHostname(xmpp.getServerName());
            config.setPort(xmpp.getPort().intValue());
            config.setUsername(xmpp.getUser());            
            ctx = hg.getOne(getThisPeer().getGraph(), hg.and(hg.type(ConnectionContext.class), 
                                                             hg.eq("config", config)));
        }
        return ctx;

    }
}