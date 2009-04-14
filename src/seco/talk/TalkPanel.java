package seco.talk;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.text.Element;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.peer.HGPeerIdentity;

import seco.gui.SecoTabbedPane;
import seco.gui.SecoTransferable;
import seco.gui.TabbedPaneU;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookTransferHandler;

public class TalkPanel extends JPanel
{
    private static final String LABEL_READY = "Ready";
    private static final String LABEL_ACCEPT_TRANSFER = "Accept Transfer";
    private static final long serialVersionUID = -4034875448632992670L;
    private static final SimpleDateFormat sdf = new SimpleDateFormat(
            "hh:mm:ss a");
    private static final ActionListener transferButtonListener
     = new TransferButtonListener();

    private HGPeerIdentity friend;
    private JTextPane outText;
    private JButton transferButton;
    @HGIgnore
    private TalkActivity talkActivity;
    private HGHandle transfer;

    public void initComponents()
    {
        setLayout(new BorderLayout());

        JTextPane inText = new JTextPane();
        JPanel inPanel = new JPanel();
        inPanel.setLayout(new BorderLayout());
        inPanel.add(inText, BorderLayout.CENTER);
        inText.addKeyListener(new KeyListener());
        outText = new JTextPane();
        outText.setEditable(false);
        JPanel outPanel = new JPanel();
        outPanel.setLayout(new BorderLayout());
        outPanel.add(outText, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                outPanel, inPanel);
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
        initComponents();
        this.talkActivity = talkActivity;
    }

    public HGPeerIdentity getFriend()
    {
        return friend;
    }

    public void setFriend(HGPeerIdentity friend)
    {
        this.friend = friend;
    }

    public void chatFrom(HGPeerIdentity from, String text)
    {
        String s = "(" + sdf.format(new Date()) + ") ";
        s += (!from.equals(friend) ? "me" : from.getName()) + ":" + text;
        outText.setText(outText.getText() + s);
    }

    public TalkActivity getTalkActivity()
    {
        return talkActivity;
    }

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
        transfer = h;
        showTransferButton();
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

    public static class KeyListener extends KeyAdapter
    {
        //private JTextPane inText;
        //private TalkPanel panel;

        public KeyListener()
        {
        }

//        public KeyListener(JTextPane inText, TalkPanel panel)
//        {
//            this.inText = inText;
//            this.panel = panel;
//        }

        public void keyTyped(KeyEvent e)
        {
            TalkPanel panel = (TalkPanel) e.getComponent().getParent().getParent().getParent();
            JTextPane inText = (JTextPane) e.getComponent();
            if (e.getKeyChar() == '\n')
                if (!e.isShiftDown())
                {
                    String msg = inText.getText();
                    inText.setText("");
                    if (panel.talkActivity != null)
                    {
                        panel.talkActivity.chat(msg);
                        panel.chatFrom(panel.talkActivity.getThisPeer()
                                .getIdentity(), msg);
                    }
                }
                else
                {
                    inText.setText(inText.getText() + "\n");
                }
        }

//        public JTextPane getInText()
//        {
//            return inText;
//        }
//
//        public void setInText(JTextPane inText)
//        {
//            this.inText = inText;
//        }
//
//        public TalkPanel getPanel()
//        {
//            return panel;
//        }
//
//        public void setPanel(TalkPanel panel)
//        {
//            this.panel = panel;
//        }
    }

    public JTextPane getOutText()
    {
        return outText;
    }

    public void setOutText(JTextPane outText)
    {
        this.outText = outText;
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
                }else if(fl.equals(NotebookTransferHandler.FLAVOR))
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
        @Override
        public void actionPerformed(ActionEvent e)
        {
            TalkPanel talkPanel = (TalkPanel) 
                ((Component)e.getSource()).getParent();
            //TODO: negotiate with the peer and get the copy here
            talkPanel.transferAccepted(talkPanel.transfer);
        }
    } 
}