package seco.talk;

/**
 * <p>
 * A POJO that stores all information needed to connect to a Seco network. This is
 * stored in the niche as is and populates the network dialog.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class ConnectionConfig
{
    private String name; // the name of this connection for when we want multiple configurations saved.    
    private boolean anonymousLogin;
    private boolean autoRegister;
    private String username;
    private String password;
    private String hostname;
    private int port;
    private boolean useProxy;
    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPass;
        
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public boolean isAnonymousLogin()
    {
        return anonymousLogin;
    }
    public void setAnonymousLogin(boolean anonymousLogin)
    {
        this.anonymousLogin = anonymousLogin;
    }
    public boolean isAutoRegister()
    {
        return autoRegister;
    }
    public void setAutoRegister(boolean autoRegister)
    {
        this.autoRegister = autoRegister;
    }
    public String getUsername()
    {
        return username;
    }
    public void setUsername(String username)
    {
        this.username = username;
    }
    public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }
    public String getHostname()
    {
        return hostname;
    }
    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }
    public int getPort()
    {
        return port;
    }
    public void setPort(int port)
    {
        this.port = port;
    }
    public boolean isUseProxy()
    {
        return useProxy;
    }
    public void setUseProxy(boolean userProxy)
    {
        this.useProxy = userProxy;
    }
    public String getProxyHost()
    {
        return proxyHost;
    }
    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
    }
    public int getProxyPort()
    {
        return proxyPort;
    }
    public void setProxyPort(int proxyPort)
    {
        this.proxyPort = proxyPort;
    }
    public String getProxyUser()
    {
        return proxyUser;
    }
    public void setProxyUser(String proxyUser)
    {
        this.proxyUser = proxyUser;
    }
    public String getProxyPass()
    {
        return proxyPass;
    }
    public void setProxyPass(String proxyPass)
    {
        this.proxyPass = proxyPass;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((hostname == null) ? 0 : hostname.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((password == null) ? 0 : password.hashCode());
        result = prime * result + port;
        result = prime * result
                + ((proxyHost == null) ? 0 : proxyHost.hashCode());
        result = prime * result
                + ((proxyPass == null) ? 0 : proxyPass.hashCode());
        result = prime * result + proxyPort;
        result = prime * result
                + ((proxyUser == null) ? 0 : proxyUser.hashCode());
        result = prime * result + (useProxy ? 1231 : 1237);
        result = prime * result
                + ((username == null) ? 0 : username.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ConnectionConfig other = (ConnectionConfig) obj;
        if (hostname == null)
        {
            if (other.hostname != null) return false;
        }
        else if (!hostname.equals(other.hostname)) return false;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        if (password == null)
        {
            if (other.password != null) return false;
        }
        else if (!password.equals(other.password)) return false;
        if (port != other.port) return false;
        if (proxyHost == null)
        {
            if (other.proxyHost != null) return false;
        }
        else if (!proxyHost.equals(other.proxyHost)) return false;
        if (proxyPass == null)
        {
            if (other.proxyPass != null) return false;
        }
        else if (!proxyPass.equals(other.proxyPass)) return false;
        if (proxyPort != other.proxyPort) return false;
        if (proxyUser == null)
        {
            if (other.proxyUser != null) return false;
        }
        else if (!proxyUser.equals(other.proxyUser)) return false;
        if (useProxy != other.useProxy) return false;
        if (username == null)
        {
            if (other.username != null) return false;
        }
        else if (!username.equals(other.username)) return false;
        return true;
    }    
}
