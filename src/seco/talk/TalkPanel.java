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

import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.peer.HGPeerIdentity;

public class TalkPanel extends JPanel
{
    private static final long serialVersionUID = -4034875448632992670L;
    private static final SimpleDateFormat sdf = new SimpleDateFormat(
            "hh:mm:ss a");

    private HGPeerIdentity friend;
    // private JPanel inPanel;
    // private JPanel outPanel;
    // private JTextPane inText;
    private JTextPane outText;
    // private JSplitPane splitPane;
    @HGIgnore
    private TalkActivity talkActivity;

    public void initComponents()
    {
        setLayout(new BorderLayout());

        JTextPane inText = new JTextPane();
        JPanel inPanel = new JPanel();
        inPanel.setLayout(new BorderLayout());
        inPanel.add(inText, BorderLayout.CENTER);
        inText.addKeyListener(new KeyListener(inText, this));
        outText = new JTextPane();
        outText.setEditable(false);
        JPanel outPanel = new JPanel();
        outPanel.setLayout(new BorderLayout());
        outPanel.add(outText, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                outPanel, inPanel);
        this.add(splitPane, BorderLayout.CENTER);
    }

    public TalkPanel()
    {
    }

    public TalkPanel(TalkActivity talkActivity)
    {
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
        private JTextPane inText;
        private TalkPanel panel;

        public KeyListener()
        {
        }

        public KeyListener(JTextPane inText, TalkPanel panel)
        {
            this.inText = inText;
            this.panel = panel;
        }

        public void keyTyped(KeyEvent e)
        {
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

        public JTextPane getInText()
        {
            return inText;
        }

        public void setInText(JTextPane inText)
        {
            this.inText = inText;
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

    public JTextPane getOutText()
    {
        return outText;
    }

    public void setOutText(JTextPane outText)
    {
        this.outText = outText;
    }

}