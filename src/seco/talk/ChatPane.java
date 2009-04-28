package seco.talk;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.peer.HGPeerIdentity;

public class ChatPane extends JTextPane
{
    private static final long serialVersionUID = 8264519627985265257L;
    private static SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
    private static final HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    
    private HGPeerIdentity me;
    // tracking user actions
    @HGIgnore
    AtomicInteger actionGroupId = new AtomicInteger(0);
    @HGIgnore
    Map<Integer, List<Runnable>> actions = 
        Collections.synchronizedMap(new HashMap<Integer, List<Runnable>>());
    
    public ChatPane()
    {
        setEditorKit(htmlEditorKit);
        setEditable(false);
        addHyperlinkListener(new LinkActionListener(this));        
    }
    
    public void chatFrom(HGPeerIdentity from, String text)
    {
        String s = "(" + sdf.format(new Date()) + ") ";
        s += (from.equals(me) ? "me" : from.getName()) + ":" + text;
        //outText.setText(outText.getText() + s);
        try
        {
            getDocument().insertString(getDocument().getLength(), s, null);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }    
    
    /**
     * <p>
     * Like 'chatFrom', but with some actions appended. The 'actions' argument
     * is a flattened list of (String, Runnable) pairs. That is, an argument at an 
     * even position is a string label of the action and an argument at an odd position
     * is the Runnable the will execute it. Actions are presented as hyperlinks and
     * are exclusive: once a user click on one of the action, all of them become
     * disabled.
     * </p>
     */
    public void actionableChatFrom(HGPeerIdentity from, 
                                   String text, 
                                   Object...actions)
    {
        try 
        {
            String s = "(" + sdf.format(new Date()) + ") ";
            s += (from.equals(me) ? "me" : from.getName()) + ":";
            String indent = s.replaceAll(".", " ");
            s += text + "\n" + indent;            
            getDocument().insertString(getDocument().getLength(), s, null);            
            if (actions.length % 2 != 0)
                throw new RuntimeException("Wrong number of arguments: actions must be (label, Runnable) pairs.");
            if (actions.length > 0)
            {
                getDocument().insertString(getDocument().getLength(), "[", null);
                List<Runnable> actionList = new ArrayList<Runnable>();
                int groupId = actionGroupId.incrementAndGet();
                this.actions.put(groupId, actionList);
                for (int i = 0; i < actions.length; i+=2)
                {
                    int actionId = actionList.size();
                    actionList.add((Runnable)actions[i+1]);
                    
                    SimpleAttributeSet hrefAttr = new SimpleAttributeSet();
                    hrefAttr.addAttribute(HTML.Attribute.HREF, "" + groupId + ":" + actionId);
                    
                    SimpleAttributeSet attrs = new SimpleAttributeSet();
                    StyleConstants.setUnderline(attrs, true);
                    StyleConstants.setForeground(attrs, Color.blue);
                    StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_LEFT);
                    attrs.addAttribute(HTML.Tag.A, hrefAttr);                    
                    getDocument().insertString(getDocument().getLength(), 
                                                       actions[i].toString(), 
                                                       attrs);
                    if (i < actions.length - 2)
                        getDocument().insertString(getDocument().getLength(), ",", null);                
                }
                getDocument().insertString(getDocument().getLength(), "]\n", null);                
            }
        }
        catch (BadLocationException e) 
        {
            e.printStackTrace(System.err);
        }              
    }
    
    public static class LinkActionListener implements HyperlinkListener
    {
        ChatPane pane;
        
        public LinkActionListener()
        {            
        }
        
        public LinkActionListener(ChatPane pane)
        {
            this.pane = pane;
        }
        
        public void hyperlinkUpdate(HyperlinkEvent e)
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            {                
                System.out.println("Link clicked: " + e.getDescription());
                String [] A = e.getDescription().split(":");
                int groupId = Integer.parseInt(A[0]);
                int actionId = Integer.parseInt(A[1]);
                List<Runnable> actions = pane.actions.get(groupId);
                if (actions != null)
                {
                    Runnable r = actions.get(actionId);
                    pane.actions.remove(groupId);
                    r.run();
                }
            }
        }

        public ChatPane getPane()
        {
            return pane;
        }

        public void setPane(ChatPane pane)
        {
            this.pane = pane;
        }        
    }    
}