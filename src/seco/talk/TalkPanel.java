package seco.talk;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

import org.hypergraphdb.peer.HGPeerIdentity;

public class TalkPanel extends JPanel
{
    private static final long serialVersionUID = -4034875448632992670L;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
    
    private HGPeerIdentity friend;
    private JPanel inPanel;
    private JPanel outPanel;
    private JTextPane inText;
    private JTextPane outText;
    private JSplitPane splitPane;
    
    protected void initComponents()
    {
        setLayout(new BorderLayout());
        
        inText = new JTextPane();
        inPanel = new JPanel();
        inPanel.setLayout(new BorderLayout());
        inPanel.add(inText, BorderLayout.CENTER);
        inText.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) 
            {
                if (e.getKeyChar() == '\n')
                    if (!e.isShiftDown())  
                    {
                        String msg = inText.getText();
                        inText.setText("");
                        outText.setText(outText.getText() + msg);
                    }
                    else
                    {
                        inText.setText(inText.getText() + "\n");
                    }
            }        
        });        
        
        outText = new JTextPane();
        outText.setEditable(false);
        outPanel = new JPanel();
        outPanel.setLayout(new BorderLayout());
        outPanel.add(outText, BorderLayout.CENTER);
        
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outPanel, inPanel);
        this.add(splitPane, BorderLayout.CENTER);
    }
    
    public TalkPanel()
    {
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

    public void chatFrom(HGPeerIdentity from, String text)
    {        
        outText.setText(outText.getText() + "(" + sdf.format(new Date()) + ") " + 
                        from.getName() + ":" + text);        
    }
     
    public static void main(String [] argv)
    {
        JFrame frame = new JFrame();
        final TalkPanel talk = new TalkPanel();
        frame.add(talk);
        frame.setSize(500, 500);
        frame.setLocation(300, 200);
        frame.setVisible(true);
    }
}