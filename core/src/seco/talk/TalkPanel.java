package seco.talk;

import static seco.U.hget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.TransferHandler;
import javax.swing.text.Element;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.PeerPresenceListener;

import seco.ThisNiche;
import seco.gui.SecoTransferable;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookTransferHandler;
import seco.things.CellUtils;
import seco.util.task.Callback;

public class TalkPanel extends BaseChatPanel implements PeerPresenceListener
{
    private static final String LABEL_READY = "Ready";
    private static final String LABEL_ACCEPT_TRANSFER = "Accept Transfer";
    private static final long serialVersionUID = -4034875448632992670L;

    private static final ActionListener transferButtonListener = new TransferButtonListener();

    private HGPeerIdentity friend;
    private ChatPane chatPane;
    private TalkInputPane inputPane;
    private JButton transferButton;
    @HGIgnore
    private transient TalkActivity talkActivity;
    private HGHandle transfer;

    public void initComponents()
    {
        setLayout(new BorderLayout());
        inputPane = new TalkInputPane();
        inputPane.initComponents();
        inputPane.inputCallback = new ChatCallback(this);
        chatPane = new ChatPane();
        chatPane.initComponents();
        chatPane.setMe(getConnectionContext().getPeer().getIdentity());
        JPanel outPanel = new JPanel();
        outPanel.setLayout(new BorderLayout());
        outPanel.add(new JScrollPane(chatPane), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                outPanel, new JScrollPane(inputPane));
        splitPane.setResizeWeight(0.8);
        this.add(splitPane, BorderLayout.CENTER);
        transferButton = new JButton(LABEL_ACCEPT_TRANSFER);
        transferButton.setForeground(Color.blue);
        transferButton.setBorderPainted(false);
        transferButton.setContentAreaFilled(false);
        this.add(transferButton, BorderLayout.SOUTH);
        transferButton.addMouseMotionListener(new DragMouseListener());
        transferButton.setVisible(false);
        setDoubleBuffered(false);
        setEnabled0(true);
    }

    public TalkPanel()
    {
        setTransferHandler(new TPTransferHandler(this));
    }

//    public TalkPanel(HGPeerIdentity friend, HGPeerIdentity peerID)
//    {
//        super(peerID);
//        this.friend = friend;
//        setTransferHandler(new TPTransferHandler(this));
//        // initComponents();
//    }

    public HGPeerIdentity getFriend()
    {
        return friend;
    }

    public void setFriend(HGPeerIdentity friend)
    {
        this.friend = friend;
    }

    public ChatPane getChatPane()
    {
        return chatPane;
    }

    public void setChatPane(ChatPane chatPane)
    {
        this.chatPane = chatPane;
    }

    public TalkActivity getTalkActivity()
    {
        return talkActivity;
    }

    private void endTransfer()
    {
        transferButton.setVisible(false);
        transferButton.setText(LABEL_ACCEPT_TRANSFER);
        transfer = null;
    }

    // called by Transfer handler when smth is imported
    public void acceptTransfer(HGHandle h)
    {
        // TODO: send notification to peers and show their "Accept Transfer"
        // button
        // instead of these 2 lines
        // transfer = h;
        // showTransferButton();
        Object atom = hget(h);
        // HGAtomType type = hget(htype(h));
        String label = atom.getClass().getSimpleName() + "("
                + ThisNiche.graph.getPersistentHandle(h).toString() + ")";
        // ThisNiche.hg.getPersistentHandle(h).toString() + ":" + atom + ":" +
        // type;
        String msg = "Offered " + label;
        chatPane.actionableChatFrom(getConnectionContext().getPeer()
                .getIdentity(), msg, "Cancel", new Runnable() {
            public void run()
            {
                System.out.println("Action Cancelled.");
            }
        });
        talkActivity.offerAtom(h, label);
    }

    public void showTransferButton()
    {
        transferButton.setText(LABEL_ACCEPT_TRANSFER);
        transferButton.setVisible(true);
        transferButton.addActionListener(transferButtonListener);
    }

    // to be called when the copy is received and ready to be dragged
    public void transferAccepted(HGHandle h)
    {
        System.out.println("TalkPanel - transferAccepted:");
        transfer = h;
        transferButton.setVisible(true);
        transferButton.removeActionListener(transferButtonListener);
        transferButton.setText(LABEL_READY);
    }

    public JButton getTransferButton()
    {
        return transferButton;
    }

    public void setTransferButton(JButton transferButton)
    {
        this.transferButton = transferButton;
        if (transferButton != null) transferButton.setVisible(false);
    }

    private static class DragMouseListener extends MouseAdapter
    {
        public DragMouseListener()
        {
        }

        private void initDrag(MouseEvent e)
        {
            TalkPanel talkPanel = (TalkPanel) e.getComponent().getParent();
            if (talkPanel.transfer == null) return;
            TransferHandler handler = talkPanel.getTransferHandler();
            int action = //((e.getModifiers() & MouseEvent.CTRL_MASK) == 0) ? 
                    //TransferHandler.MOVE : 
                        TransferHandler.COPY;
            handler.exportAsDrag(talkPanel, e, action);
        }

        public void mouseDragged(MouseEvent e)
        {
            initDrag(e);
        }

    }

    public static class TPTransferHandler extends TransferHandler
    {
        private static final long serialVersionUID = -2286292421264849858L;

        private TalkPanel talkPanel;

        public TPTransferHandler()
        {
        }

        public TPTransferHandler(TalkPanel tp)
        {
            this.talkPanel = tp;
        }

        protected DataFlavor getImportFlavor(DataFlavor[] flavors, JComponent c)
        {
            for (int i = 0; i < flavors.length; i++)
            {
                if (flavors[i].equals(SecoTransferable.FLAVOR)
                        || flavors[i].equals(NotebookTransferHandler.FLAVOR))
                    return flavors[i];
            }
            return null;
        }

        public int getSourceActions(JComponent c)
        {
            return COPY;// _OR_MOVE;
        }

        protected Transferable createTransferable(JComponent comp)
        {
            return new SecoTransferable(talkPanel.transfer);
        }

        protected void exportDone(JComponent source, Transferable data,
                int action)
        {
            super.exportDone(source, data, action);
            talkPanel.endTransfer();
        }

        @SuppressWarnings("unchecked")
        public boolean importData(TransferSupport support)
        {
            if(!support.isDrop()) return false;
            JComponent comp = (JComponent) support.getComponent();
            Transferable t = support.getTransferable();
            // Don't drop on myself.
            if (comp == talkPanel) return false;
            if(talkPanel.getTalkActivity() == null) return false;
            DataFlavor fl = getImportFlavor(t.getTransferDataFlavors(), comp);
            if (fl == null) return false;
            try
            {
                HGHandle data = null;
                if (fl.equals(SecoTransferable.FLAVOR))
                {
                    data = (HGHandle) t.getTransferData(fl);
                }
                else if (fl.equals(NotebookTransferHandler.FLAVOR))
                {
                    Vector<Element> els = (Vector<Element>) t
                            .getTransferData(fl);
                    data = NotebookDocument.getNBElementH(els.get(0));
                }

                if (data != null)
                {
                    boolean move = (support.getDropAction() == MOVE);
                    data = CellUtils.makeCopy(data);
                    support.setDropAction(COPY);
                    talkPanel.acceptTransfer(data);
                    //NO OTHER WAY TO PREVENT NBUI from deleting the cut component
                    if(move && fl.equals(NotebookTransferHandler.FLAVOR)) return false;
                    return true;
                }
            }
            catch (Exception ioe)
            {
                ioe.printStackTrace();
            }
            return false;
        }

        public boolean canImport(JComponent comp, DataFlavor[] flavors)
        {
            return (getImportFlavor(flavors, comp) != null);
        }

        public TalkPanel getTalkPanel()
        {
            return talkPanel;
        }

        public void setTalkPanel(TalkPanel talkPanel)
        {
            this.talkPanel = talkPanel;
        }
    }

    public static class TransferButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            TalkPanel talkPanel = (TalkPanel) ((Component) e.getSource())
                    .getParent();
            // TODO: negotiate with the peer and get the copy here
            talkPanel.transferAccepted(talkPanel.transfer);
        }
    }

    public static class ChatCallback implements Callback<String>
    {
        private TalkPanel panel;

        public ChatCallback(TalkPanel panel)
        {
            this.panel = panel;
        }

        public ChatCallback()
        {

        }

        public void callback(String msg)
        {
            if (panel.talkActivity != null)
            {
                panel.talkActivity.chat(msg);
                panel.chatPane.chatFrom(panel.getConnectionContext().getPeer()
                        .getIdentity(), msg);
            }
        }

        public TalkPanel getPanel()
        {
            return panel;
        }

        public void setPanel(TalkPanel panel)
        {
            this.panel = panel;
        }
    }

    @Override
    public void connected(ConnectionContext ctx)
    {
        setEnabled0(true);
        ctx.getPeer().addPeerPresenceListener(this);
        initTalkActivity(ctx);
    }

    @Override
    public void disconnected(ConnectionContext ctx)
    {
        ctx.getPeer().removePeerPresenceListener(this);
        talkActivity = null;
        setEnabled0(false);
    }

    @Override
    public void workStarted(ConnectionContext ctx, boolean connect_or_disconnect)
    {
        setEnabled0(false);
    }

    public void initTalkActivity(ConnectionContext ctx)
    {
        if (talkActivity != null) return;
        if (ctx.talks.containsKey(friend))
        {
            talkActivity = ctx.talks.get(friend);
            setEnabled0(true);
            return;
        }
        if (ctx.getPeer().getNetworkTarget(friend) == null)
        {
            setEnabled0(false);
            return;
        }

        talkActivity = new TalkActivity(ctx.getPeer(), friend, this);
        ctx.talks.put(friend, talkActivity);
        System.out.println("initTalkActivity: " + talkActivity);
        ctx.getPeer().getActivityManager().initiateActivity(talkActivity);
        setEnabled0(true);
    }

    public void peerJoined(HGPeerIdentity target)
    {
        setEnabled0(true);
        if (isConnected()) return;
        // System.out.println("TalkPanel - peerJoined: " + target + ":" +
        // friend);
        if (target.equals(friend))
        {
            initTalkActivity(getConnectionContext());
            chatPane.setMe(getConnectionContext().getPeer().getIdentity());
        }
    }

    public void peerLeft(HGPeerIdentity target)
    {
        if (target.equals(friend)) setEnabled0(false);
    }

    @Override
    public boolean isConnected()
    {
        return talkActivity != null;
    }

    void setEnabled0(boolean enabled)
    {
        setEnabled(enabled);
        if (inputPane != null) inputPane.setEnabled(enabled);
        if (chatPane != null) chatPane.setEnabled(enabled);
    }

}