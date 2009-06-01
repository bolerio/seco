/**
 * 
 */
package seco.talk;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.hypergraphdb.peer.HGPeerIdentity;
import org.jivesoftware.smackx.muc.HostedRoom;

import seco.notebook.util.IconManager;

class PeerItemRenderer extends JLabel implements ListCellRenderer 
{
    private static final long serialVersionUID = 9045908623545576595L;
    static ImageIcon personIcon = new ImageIcon(IconManager.getIcon("seco/talk/peer-icon.jpg"));
    static ImageIcon roomIcon = new ImageIcon(IconManager.getIcon("seco/talk/chatroom-icon.jpg"));
    
    public Component getListCellRendererComponent(
      JList list,              // the list
      Object value,            // value to display
      int index,               // cell index
      boolean isSelected,      // is the cell selected
      boolean cellHasFocus)    // does the cell have focus
    {
        String label = null;
        ImageIcon image = null;
        if (value instanceof HGPeerIdentity)
        {
            label = ((HGPeerIdentity)value).getName();
            int hostPart = label.lastIndexOf("/");
            if (hostPart > -1)
                label = label.substring(hostPart + 1);
            image = personIcon;
        }
        else if (value instanceof HostedRoom)
        {
            label = ((HostedRoom)value).getName();
            image = roomIcon;
        }        
        setText(label);
        setIcon(image);
        if (isSelected) 
        {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } 
        else 
        {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }
}