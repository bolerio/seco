package seco.talk;

import org.jivesoftware.smackx.muc.Occupant;

/*
 * Workaround class that wraps the <code>org.jivesoftware.smackx.muc.Occupant</code> class,
 * Its hashCode() and equals() methods throw NPE when jid is missing, which is a common case
 * for semi or full-anonymous rooms. 
 */
public class OccupantEx
{
    private String affiliation;
    private String role;
    // Fields that may have a value
    private String jid;
    private String nick;
    public OccupantEx(Occupant item) 
    {
        
        this.jid = item.getJid();
        this.affiliation = item.getAffiliation();
        this.role = item.getRole();
        this.nick = item.getNick();
    }
    
    public String getAffiliation()
    {
        return affiliation;
    }
    public void setAffiliation(String affiliation)
    {
        this.affiliation = affiliation;
    }
    public String getRole()
    {
        return role;
    }
    public void setRole(String role)
    {
        this.role = role;
    }
    public String getJid()
    {
        return jid;
    }
    public void setJid(String jid)
    {
        this.jid = jid;
    }
    public String getNick()
    {
        return nick;
    }
    public void setNick(String nick)
    {
        this.nick = nick;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((affiliation == null) ? 0 : affiliation.hashCode());
        result = prime * result + ((jid == null) ? 0 : jid.hashCode());
        result = prime * result + ((nick == null) ? 0 : nick.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        OccupantEx other = (OccupantEx) obj;
        if (affiliation == null)
        {
            if (other.affiliation != null) return false;
        }
        else if (!affiliation.equals(other.affiliation)) return false;
        if (jid == null)
        {
            if (other.jid != null) return false;
        }
        else if (!jid.equals(other.jid)) return false;
        if (nick == null)
        {
            if (other.nick != null) return false;
        }
        else if (!nick.equals(other.nick)) return false;
        if (role == null)
        {
            if (other.role != null) return false;
        }
        else if (!role.equals(other.role)) return false;
        return true;
    }
}
