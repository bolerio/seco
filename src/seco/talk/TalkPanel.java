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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.TransferHandler;
import javax.swing.text.Element;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.peer.HGPeerIdentity;

import seco.ThisNiche;
import seco.api.Callback;
import seco.gui.SecoTransferable;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookTransferHandler;

public class TalkPanel extends JPanel
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
        chatPane.setMe(this.talkActivity.getThisPeer().getIdentity());
        JPanel outPanel = new JPanel();
        outPanel.setLayout(new BorderLayout());
        outPanel.add(new JScrollPane(chatPane), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              outPanel, 
                                              new JScrollPane(inputPane));
        splitPane.setResizeWeight(0.8);
        this.add(splitPane, BorderLayout.CENTER);
        transferButton = new JButton(LABEL_ACCEPT_TRANSFER);
        transferButton.setForeground(Color.blue);
        transferButton.setBorderPainted(false);
        transferButton.setContentAreaFilled(false);
        this.add(transferButton, BorderLayout.SOUTH);
        transferButton.addMouseMotionListener(new DragMouseListener());
        transferButton.setVisible(false);
    }

    public TalkPanel()
    {
        this.setTransferHandler(new TPTransferHandler(this));
    }

    public TalkPanel(TalkActivity talkActivity)
    {
        this();
        this.talkActivity = talkActivity;        
        initComponents();
    }

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

    @HGIgnore
    public void setTalkActivity(TalkActivity talkActivity)
    {
        this.talkActivity = talkActivity;
    }
    
    private void endTransfer()
    {
        transferButton.setVisible(false);
        transferButton.setText(LABEL_ACCEPT_TRANSFER);
        transfer = null;
    }
    
    //called by Transfer handler when smth is imported  
    public void acceptTransfer(HGHandle h)
    {
        //TODO: send notification to peers and show their "Accept Transfer" button
        //instead of these 2 lines
//        transfer = h;
//        showTransferButton();
        Object atom = hget(h);
//        HGAtomType type = hget(htype(h));        
        String label = atom.getClass().getSimpleName() + "(" +ThisNiche.hg.getPersistentHandle(h).toString() + ")";              
            //ThisNiche.hg.getPersistentHandle(h).toString() + ":" + atom + ":" + type;
        String msg = "Offered " + label;
        chatPane.actionableChatFrom(this.talkActivity.getThisPeer().getIdentity(), msg, "Cancel",
        new Runnable() {
            public void run()
            {
                System.out.println("Action Cancelled.");
            }
        }
        );
        talkActivity.offerAtom(h, label);
    }
    
    public void showTransferButton()
    {
        transferButton.setText(LABEL_ACCEPT_TRANSFER);
        transferButton.setVisible(true);
        transferButton.addActionListener(transferButtonListener);
    }

    //to be called when the copy is received and ready to be dragged   
    public void transferAccepted(HGHandle h)
    {
        System.out.println("TalkPanel - transferAccepted:" );
        transfer = h;
        transferButton.setVisible(true);
        transferButton.removeActionListener(transferButtonListener);
        transferButton.setText(LABEL_READY);
    }
    
    public static void main(String[] argv)
    {
        JFrame frame = new JFrame();
        final TalkPanel talk = new TalkPanel(null);
        frame.add(talk);
        frame.setSize(500, 500);
        frame.setLocation(300, 200);
        frame.setVisible(true);
    }

    
    public JButton getTransferButton()
    {
        return transferButton;
    }

    public void setTransferButton(JButton transferButton)
    {
        this.transferButton = transferButton;
        if(transferButton != null)
           transferButton.setVisible(false);
    }
    
    private static class DragMouseListener extends MouseAdapter
    {
        public DragMouseListener()
        {
        }
        private void initDrag(MouseEvent e)
        {
            TalkPanel talkPanel = (TalkPanel) e.getComponent().getParent();
            if(talkPanel.transfer == null) return;
            TransferHandler handler = talkPanel.getTransferHandler();
            int action = ((e.getModifiers() & MouseEvent.CTRL_MASK) == 0) ? TransferHandler.MOVE
                    : TransferHandler.COPY;
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
                if (flavors[i].equals(SecoTransferable.FLAVOR) ||
                        flavors[i].equals(NotebookTransferHandler.FLAVOR))
                    return flavors[i];
            }
            return null;
        }
        
      
        public int getSourceActions(JComponent c)
        {
            return COPY_OR_MOVE;
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
            JComponent comp = (JComponent) support.getComponent();
            Transferable t = support.getTransferable();
            // Don't drop on myself.
            if (comp == talkPanel)  return false;
            DataFlavor fl = getImportFlavor(t.getTransferDataFlavors(), comp);
            if (fl == null) return false;
            try
            {
                HGHandle data = null;
                if (fl.equals(SecoTransferable.FLAVOR))
                {
                    data = (HGHandle) t.getTransferData(fl);
                }
                else if(fl.equals(NotebookTransferHandler.FLAVOR))
                {
                    Vector<Element> els = (Vector<Element>) t.getTransferData(fl);
                    data = NotebookDocument.getNBElementH(els.get(0));
                }
                if(data != null)
                {
                    talkPanel.acceptTransfer(data);
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
            TalkPanel talkPanel = (TalkPanel) 
                ((Component)e.getSource()).getParent();
            //TODO: negotiate with the peer and get the copy here
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
                panel.chatPane.chatFrom(panel.talkActivity.getThisPeer().getIdentity(), msg);
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
}