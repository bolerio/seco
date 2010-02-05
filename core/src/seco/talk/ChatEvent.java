package seco.talk;

import org.hypergraphdb.peer.HGPeerIdentity;

public class ChatEvent
{
    private String text;
    private HGPeerIdentity from;
    
    public ChatEvent(HGPeerIdentity from, String text)
    {
        this.from = from;
        this.text = text;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public HGPeerIdentity getFrom()
    {
        return from;
    }

    public void setFrom(HGPeerIdentity from)
    {
        this.from = from;
    }    
}