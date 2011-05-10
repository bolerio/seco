package seco.gui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.gui.PSwingNode;
import seco.gui.PiccoloCanvas;
import seco.talk.ConnectionConfig;
import seco.talk.ConnectionContext;
import seco.talk.ConnectionManager;
import seco.talk.ConnectionPanel;
import seco.things.CellGroup;
import seco.things.CellUtils;
import seco.util.GUIUtil;

/*
 * NetworkConnectionDlg.java
 *
 * Created on 2010-12-8, 11:48:49
 */
public class NetworkConnectionDlg extends JDialog
{
    private static final long serialVersionUID = 1L;
    private NetworkConnectionPanel networkPanel;
    private JComboBox contextList;

    private void resetConnectionPanel()
    {
        if (this.contextList.getSelectedItem() == null)
        {
            ConnectionConfig cfg = new ConnectionConfig();
            ConnectionContext ctx = new ConnectionContext(cfg);
            ctx.setActive(true);
            ThisNiche.graph.add(ctx);                
            NetworkConnectionDlg.this.contextList.addItem(ctx);
            NetworkConnectionDlg.this.contextList.setSelectedItem(ctx);
        }
        networkPanel.setConnectionContext((ConnectionContext)this.contextList.getSelectedItem());        
    }
    
    /** Creates new form NetworkConnectionDlg */
    public NetworkConnectionDlg()
    {
        super(GUIUtil.getFrame(), "Network Connection");
        if (GUIUtil.getFrame() == null)
            setIconImage(GUIHelper.LOGO_IMAGE);
        initComponents();
        resetConnectionPanel();
        GUIUtil.addEscapeListener(this);
    }

    public void open()
    {
        ConnectionManager.openConnectionPanel(networkPanel.getConnectionContext());
//        setVisible(false);
    }

    public void save()
    {
        ThisNiche.graph.update(networkPanel.getConnectionContext());
    }

    private void initComponents()
    {
        contextList = new JComboBox(hg.getAll(ThisNiche.graph, hg.type(ConnectionContext.class)).toArray());
        //contextList.setRenderer(new ConnectionListCellRenderer());
        contextList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (contextList.isFocusOwner())
                    resetConnectionPanel();
            }
        });
        JButton butOpen = new JButton("Open");
        butOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                save();
                open();
            }
        });
        JButton butSave = new JButton("Save");
        butSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                save();
            }
        });
        JButton butClose = new JButton("Close");
        butClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                NetworkConnectionDlg.this.setVisible(false);
            }
        });
        JButton butDelete = new JButton("Delete");
        butDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                ConnectionContext ctx = (ConnectionContext)NetworkConnectionDlg.this.contextList.getSelectedItem();
                if (ctx == null)
                    return;
                if (ctx.isConnected())
                    ctx.disconnect(true);
                HGHandle h = ThisNiche.handleOf(ctx);
                if (h != null)
                {                    
                    ConnectionPanel panel = ctx.getConnectionPanel();
                    if (panel != null)
                    {
                        HGHandle panelHandle = ThisNiche.handleOf(panel);
                        HGHandle cellHandle = GUIHelper.getCellHandleByValueHandle(
                                    ThisNiche.TOP_CELL_GROUP_HANDLE, panelHandle);
                        // Delete the panel from its canvas. The canvas is the visual 
                        // element of the containing CellGroup
                        CellGroup group = CellUtils.getParentGroup(cellHandle);
                        PiccoloCanvas canvas = (PiccoloCanvas)group.getVisualInstance();
                        PSwingNode node = canvas.getPSwingNodeForHandle(panelHandle);
                        canvas.removeNode(node);
                        ThisNiche.graph.remove(panelHandle);
                    }
                    ThisNiche.graph.remove(h);
                    ctx = null;
                }
                NetworkConnectionDlg.this.contextList.removeItem(ctx);                
            }
        });
        JButton butNew = new JButton("New");
        butNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                ConnectionConfig cfg = new ConnectionConfig();
                ConnectionContext ctx = new ConnectionContext(cfg);
                ctx.setActive(true);
                ThisNiche.graph.add(ctx);                
                NetworkConnectionDlg.this.contextList.addItem(ctx);
                NetworkConnectionDlg.this.contextList.setSelectedItem(ctx);
            }
        });
         
        networkPanel = new NetworkConnectionPanel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        GroupLayout layout = new GroupLayout(getContentPane());
        JSeparator separator = new JSeparator();
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                          layout.createSequentialGroup()
                                  .addContainerGap(85, Short.MAX_VALUE)
                                  .addComponent(butOpen)
                                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                  .addComponent(butSave)
                                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                  .addComponent(butClose))
                .addComponent(separator)
                .addComponent(contextList)                
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING,
                          layout.createSequentialGroup()
                                  .addComponent(butNew)
                                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                  .addComponent(butDelete))                
                .addComponent(networkPanel,
                              GroupLayout.Alignment.TRAILING,
                              GroupLayout.DEFAULT_SIZE,
                              GroupLayout.DEFAULT_SIZE,
                              Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING,
                          layout.createSequentialGroup()
                                  .addComponent(contextList)
                                  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                          .addComponent(butNew)
                                          .addComponent(butDelete))  
                                  .addComponent(separator)
                                  .addComponent(networkPanel,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                          .addComponent(butClose)
                                          .addComponent(butSave)
                                          .addComponent(butOpen))));
        pack();
    }

    public class NetworkConnectionPanel extends JPanel
    {
        private static final long serialVersionUID = 1L;
        private JCheckBox chLogAnon;
        private JCheckBox chProxy;
        private JCheckBox chRegAuto;
        private JLabel labProxyHost;
        private JLabel labProxyPort;
        private JLabel labProxyUsername;
        private JLabel labProxyPassword;
        private JTextField txtHost;
        private JPasswordField txtPassword;
        private JTextField txtPort;
        private JTextField txtProxyHost;
        private JPasswordField txtProxyPassword;
        private JTextField txtProxyPort;
        private JTextField txtProxyUsername;
        private JTextField txtUsername;
        private JCheckBox chkEnabled;
        
        protected ConnectionContext ctx;

        /** Creates new form NetworkConnectionPanel */
        public NetworkConnectionPanel()
        {
            initComponents();
            enable_proxy_stuff(false);
        }

        private void enable_proxy_stuff(boolean enabled)
        {
            labProxyHost.setEnabled(enabled);
            labProxyPort.setEnabled(enabled);
            labProxyUsername.setEnabled(enabled);
            labProxyPassword.setEnabled(enabled);
            txtProxyHost.setEnabled(enabled);
            txtProxyPassword.setEnabled(enabled);
            txtProxyPort.setEnabled(enabled);
            txtProxyUsername.setEnabled(enabled);
        }

        public void setConnectionContext(ConnectionContext ctx)
        {
            this.ctx = ctx;
            ConnectionConfig cfg = ctx.getConfig();
            chLogAnon.setSelected(cfg.isAnonymousLogin());
            chProxy.setSelected(cfg.isUseProxy());
            chRegAuto.setSelected(cfg.isAutoRegister());
            txtHost.setText(cfg.getHostname());
            txtUsername.setText(cfg.getUsername());
            txtPassword.setText(cfg.getPassword());
            txtPort.setText("" + cfg.getPort());
            txtProxyHost.setText(cfg.getProxyHost());
            txtProxyPassword.setText(cfg.getProxyPass());
            txtProxyPort.setText("" + cfg.getProxyPort());
            txtProxyUsername.setText(cfg.getProxyUser());
        }

        public ConnectionContext getConnectionContext()
        {
            ConnectionConfig cfg = ctx.getConfig();
            cfg.setAnonymousLogin(chLogAnon.isSelected());
            cfg.setAutoRegister(chRegAuto.isSelected());
            cfg.setHostname(txtHost.getText());
            cfg.setUsername(txtUsername.getText());
            cfg.setPassword(new String(txtPassword.getPassword()));
            cfg.setPort(new Integer(txtPort.getText()));
            if (chProxy.isSelected())
            {
                cfg.setUseProxy(true);
                cfg.setProxyHost(txtProxyHost.getText());
                cfg.setProxyPass(new String(txtProxyPassword.getPassword()));
                cfg.setProxyPort(new Integer(txtProxyPort.getText()));
                cfg.setProxyUser(txtProxyUsername.getText());
            }
            return ctx;
        }

        private void initComponents()
        {
            JLabel jLabel1 = new JLabel("Host");
            txtHost = new JTextField();
            JLabel jLabel2 = new JLabel("Port");
            txtPort = new JTextField();
            chLogAnon = new JCheckBox("Login Anonymously");
            JLabel jLabel3 = new JLabel("Username:");
            txtUsername = new JTextField();
            JLabel jLabel4 = new JLabel("Password:");
            txtPassword = new JPasswordField();
            chRegAuto = new JCheckBox("Register Automatically ");
            chProxy = new JCheckBox("Use Proxy");
            chProxy.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    enable_proxy_stuff(chProxy.isSelected());
                }
            });
            labProxyHost = new JLabel("Proxy Host");
            txtProxyHost = new JTextField();
            labProxyPort = new JLabel("Proxy Port");
            txtProxyPort = new JTextField();
            labProxyUsername = new JLabel("Proxy Username:");
            txtProxyUsername = new JTextField();
            labProxyPassword = new JLabel("Proxy Password:");
            txtProxyPassword = new JPasswordField();
            txtHost.setColumns(25);
            txtPort.setColumns(6);
            txtUsername.setColumns(15);
            txtPassword.setColumns(15);
            txtProxyHost.setColumns(25);
            txtProxyPort.setColumns(6);
            txtProxyUsername.setColumns(15);
            txtProxyPassword.setColumns(15);

            txtHost.getDocument().addDocumentListener(new DocumentListener()
            {
                public void changedUpdate(DocumentEvent ev) { }
                public void insertUpdate(DocumentEvent arg0) { removeUpdate(arg0); }
                public void removeUpdate(DocumentEvent ev)
                {
                    try
                    {
                        ConnectionContext ctx = (ConnectionContext)contextList.getSelectedItem();
                        ctx.getConfig().setHostname(ev.getDocument().getText(0, ev.getDocument().getLength()));
                        contextList.repaint();                 
                    }
                    catch (BadLocationException e)
                    {
                        e.printStackTrace();
                    }
                }                
            });
            txtUsername.getDocument().addDocumentListener(new DocumentListener()
            {
                public void changedUpdate(DocumentEvent ev) { }
                public void insertUpdate(DocumentEvent arg0) { removeUpdate(arg0); }
                public void removeUpdate(DocumentEvent ev)
                {
                    try
                    {
                        ConnectionContext ctx = (ConnectionContext)contextList.getSelectedItem();
                        ctx.getConfig().setUsername(ev.getDocument().getText(0, ev.getDocument().getLength()));
                        contextList.repaint();                 
                    }
                    catch (BadLocationException e)
                    {
                        e.printStackTrace();
                    }
                }                
            });
            txtPort.getDocument().addDocumentListener(new DocumentListener()
            {
                public void changedUpdate(DocumentEvent ev) { }
                public void insertUpdate(DocumentEvent arg0) { removeUpdate(arg0); }
                public void removeUpdate(DocumentEvent ev)
                {
                    try
                    {
                        ConnectionContext ctx = (ConnectionContext)contextList.getSelectedItem();
                        String portstring = ev.getDocument().getText(0, ev.getDocument().getLength());
                        int port = ctx.getConfig().getPort();
                        try {
                            port = Integer.parseInt(portstring);
                        }
                        catch (Exception ex) 
                        {
                            
                        }
                        ctx.getConfig().setPort(port);
                        contextList.repaint();                 
                    }
                    catch (BadLocationException e)
                    {
                        e.printStackTrace();
                    }
                }
                
            });
            
            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                            .addGap(10, 10, 10)
                                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(labProxyUsername)
                                                    .addComponent(labProxyPassword))
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtProxyUsername,
                                                                  GroupLayout.PREFERRED_SIZE,
                                                                  GroupLayout.DEFAULT_SIZE,
                                                                  GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(txtProxyPassword,
                                                                  GroupLayout.PREFERRED_SIZE,
                                                                  GroupLayout.DEFAULT_SIZE,
                                                                  GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(layout.createSequentialGroup()
                                            .addContainerGap()
                                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel3)
                                                    .addComponent(jLabel4))
                                            .addGap(4, 4, 4)
                                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtUsername,
                                                                  GroupLayout.PREFERRED_SIZE,
                                                                  GroupLayout.DEFAULT_SIZE,
                                                                  GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(txtPassword,
                                                                  GroupLayout.PREFERRED_SIZE,
                                                                  GroupLayout.DEFAULT_SIZE,
                                                                  GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(layout.createSequentialGroup()
                                            .addGap(10, 10, 10)
                                            .addComponent(chLogAnon))
                                    .addGroup(layout.createSequentialGroup()
                                            .addGap(14, 14, 14)
                                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel1)
                                                    .addComponent(jLabel2))
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtHost,
                                                                  GroupLayout.PREFERRED_SIZE,
                                                                  GroupLayout.DEFAULT_SIZE,
                                                                  GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(txtPort,
                                                                  GroupLayout.PREFERRED_SIZE,
                                                                  GroupLayout.DEFAULT_SIZE,
                                                                  GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(layout.createSequentialGroup()
                                            .addGap(10, 10, 10)
                                            .addComponent(chProxy))
                                    .addGroup(layout.createSequentialGroup()
                                            .addGap(10, 10, 10)
                                            .addComponent(chRegAuto))
                                    .addGroup(layout.createSequentialGroup()
                                            .addGap(10, 10, 10)
                                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(labProxyHost)
                                                    .addComponent(labProxyPort))
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(txtProxyHost,
                                                                  GroupLayout.PREFERRED_SIZE,
                                                                  GroupLayout.DEFAULT_SIZE,
                                                                  GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(txtProxyPort,
                                                                  GroupLayout.PREFERRED_SIZE,
                                                                  GroupLayout.DEFAULT_SIZE,
                                                                  GroupLayout.PREFERRED_SIZE))))
                            .addContainerGap(33, Short.MAX_VALUE)));
            layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel1)
                                    .addComponent(txtHost,
                                                  GroupLayout.PREFERRED_SIZE,
                                                  GroupLayout.DEFAULT_SIZE,
                                                  GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2)
                                    .addComponent(txtPort,
                                                  GroupLayout.PREFERRED_SIZE,
                                                  GroupLayout.DEFAULT_SIZE,
                                                  GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(chLogAnon)
                            .addGap(7, 7, 7)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(txtUsername,
                                                  GroupLayout.PREFERRED_SIZE,
                                                  GroupLayout.DEFAULT_SIZE,
                                                  GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4)
                                    .addComponent(txtPassword,
                                                  GroupLayout.PREFERRED_SIZE,
                                                  GroupLayout.DEFAULT_SIZE,
                                                  GroupLayout.PREFERRED_SIZE))
                            .addGap(7, 7, 7)
                            .addComponent(chRegAuto)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(chProxy)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(labProxyHost)
                                    .addComponent(txtProxyHost,
                                                  GroupLayout.PREFERRED_SIZE,
                                                  GroupLayout.DEFAULT_SIZE,
                                                  GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(labProxyPort)
                                    .addComponent(txtProxyPort,
                                                  GroupLayout.PREFERRED_SIZE,
                                                  20,
                                                  GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(labProxyUsername)
                                    .addComponent(txtProxyUsername,
                                                  GroupLayout.PREFERRED_SIZE,
                                                  GroupLayout.DEFAULT_SIZE,
                                                  GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(labProxyPassword)
                                    .addComponent(txtProxyPassword,
                                                  GroupLayout.PREFERRED_SIZE,
                                                  GroupLayout.DEFAULT_SIZE,
                                                  GroupLayout.PREFERRED_SIZE))
                            .addContainerGap(20, Short.MAX_VALUE)));
        }
    }

    class ConnectionListCellRenderer extends JLabel implements ListCellRenderer
    {
        private static final long serialVersionUID = 1L;

        public ConnectionListCellRenderer()
        {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            ConnectionContext ctx = (ConnectionContext) value;
            setText(ctx.getConfig().getName());

            Color background;
            Color foreground;
            
            if (isSelected)
            {
                background = Color.gray;
                foreground = Color.WHITE;
            }
            else
            {
                background = Color.WHITE;
                foreground = Color.BLACK;
            }
            setBackground(background);
            setForeground(foreground);

            return this;
        }
    }
}
