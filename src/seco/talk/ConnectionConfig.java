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
}
