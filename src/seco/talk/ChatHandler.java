package seco.talk;

import org.hypergraphdb.HGHandle;

import seco.U;
import seco.events.EventHandler;

public class ChatHandler implements EventHandler
{

    @Override
    public void handle(HGHandle eventType, 
                       Object event, 
                       HGHandle publisher,
                       HGHandle subscriber)
    {
        ChatEvent chat = (ChatEvent)event;
        TalkPanel talk = U.hget(subscriber);
        talk.getChatPane().chatFrom(chat.getFrom(), chat.getText());
    }
}
