package seco.talk;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.dyno.visual.swing.layouts.Constraints;
import org.dyno.visual.swing.layouts.GroupLayout;
import org.dyno.visual.swing.layouts.Leading;
import org.dyno.visual.swing.layouts.Trailing;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

public class RoasterDlg extends JDialog
{

    private static final long serialVersionUID = 1L;
    private JLabel lblSearch;
    private JCheckBox chkUsername;
    private JCheckBox chkEmail;
    private JTextField txtSearch;
    private JButton buttSearch;
    private JPanel includePanel;
    private JCheckBox chkName;
    private JTable resultTable;
    private JScrollPane scrollPane;
    private JButton butAddOrRemove;

    ConnectionContext ctx;
    boolean add_or_remove;

    public RoasterDlg()
    {
        initComponents();
    }

    public RoasterDlg(ConnectionContext conn, boolean add_or_remove)
    {
        this();
        ctx = conn;
        this.add_or_remove = add_or_remove;
        if (!add_or_remove) getButAddOrRemove().setText("Remove From Roaster");
    }

    private void initComponents()
    {
        setLayout(new GroupLayout());
        add(getLblSearch(), new Constraints(new Leading(24, 10, 10),
                new Leading(25, 10, 10)));
        add(getIncludePanel(), new Constraints(new Leading(15, 287, 12, 12),
                new Leading(59, 53, 10, 10)));
        add(getButtSearch(), new Constraints(new Leading(323, 10, 10),
                new Leading(74, 10, 10)));
        add(getTxtSearch(), new Constraints(new Leading(85, 313, 12, 12),
                new Leading(25, 12, 12)));
        add(getScrollPane(), new Constraints(new Leading(15, 383, 12, 12),
                new Leading(124, 199, 10, 10)));
        add(getButAddOrRemove(), new Constraints(new Trailing(12, 12, 12),
                new Trailing(12, 162, 335)));
        setSize(412, 363);
    }

    private JButton getButAddOrRemove()
    {
        if (butAddOrRemove == null)
        {
            butAddOrRemove = new JButton();
            butAddOrRemove.setText("Add To Roaster");
            butAddOrRemove.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event)
                {
                    butAddOrRemoveActionActionPerformed(event);
                }
            });
        }
        return butAddOrRemove;
    }

    private JLabel getLblSearch()
    {
        if (lblSearch == null)
        {
            lblSearch = new JLabel();
            lblSearch.setText("Pattern:");
        }
        return lblSearch;
    }

    private JScrollPane getScrollPane()
    {
        if (scrollPane == null)
        {
            scrollPane = new JScrollPane();
            scrollPane.setViewportView(getResultTable());
        }
        return scrollPane;
    }

    private JTable getResultTable()
    {
        if (resultTable == null)
        {
            resultTable = new JTable();
            resultTable.setModel(new TableModel());
            // TableColumn tc =
            // resultTable.getColumnModel().getColumn(0);
            // tc.setCellRenderer(new ActionsRenderer());
        }
        return resultTable;
    }

    private JCheckBox getChkName()
    {
        if (chkName == null)
        {
            chkName = new JCheckBox();
            chkName.setSelected(true);
            chkName.setText("Name");
        }
        return chkName;
    }

    private JPanel getIncludePanel()
    {
        if (includePanel == null)
        {
            includePanel = new JPanel();
            includePanel.setBorder(BorderFactory.createTitledBorder(null,
                    "Search In", TitledBorder.LEADING,
                    TitledBorder.DEFAULT_POSITION, new Font("Dialog",
                            Font.BOLD, 12), new Color(51, 51, 51)));
            includePanel.setLayout(new GroupLayout());
            includePanel.add(getChkUsername(), new Constraints(new Leading(12,
                    10, 10), new Leading(0, 8, 8)));
            includePanel.add(getChkName(), new Constraints(new Leading(111, 10,
                    10), new Leading(0, 8, 8)));
            includePanel.add(getChkEmail(), new Constraints(new Leading(187,
                    10, 10), new Leading(0, 8, 8)));
        }
        return includePanel;
    }

    private JButton getButtSearch()
    {
        if (buttSearch == null)
        {
            buttSearch = new JButton();
            buttSearch.setText("Search");
            buttSearch.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event)
                {
                    buttSearchActionActionPerformed(event);
                }
            });
        }
        return buttSearch;
    }

    private JTextField getTxtSearch()
    {
        if (txtSearch == null)
        {
            txtSearch = new JTextField();
            txtSearch.setText("*");
        }
        return txtSearch;
    }

    private JCheckBox getChkEmail()
    {
        if (chkEmail == null)
        {
            chkEmail = new JCheckBox();
            chkEmail.setText("Email");
        }
        return chkEmail;
    }

    private JCheckBox getChkUsername()
    {
        if (chkUsername == null)
        {
            chkUsername = new JCheckBox();
            chkUsername.setSelected(true);
            chkUsername.setText("Username");
        }
        return chkUsername;
    }

    static class TableModel extends DefaultTableModel
    {
        static final String[] COL_NAMES = new String[] { "JID", "Username",
                "Name", "Email" };
        private ReportedData data;
        private ConnectionContext ctx;
        boolean add_or_remove;

        public TableModel()
        {

        }

        public TableModel(ReportedData data, ConnectionContext ctx,
                boolean add_or_remove)
        {
            super();
            this.data = data;
            this.ctx = ctx;
            this.add_or_remove = add_or_remove;
            init();
        }

        private void init()
        {
            XMPPPeerInterface interf = (XMPPPeerInterface) ctx.getPeer()
                    .getPeerInterface();
            Roster r = interf.getConnection().getRoster();
            for (Iterator<Row> it = data.getRows(); it.hasNext();)
            {
                Row row = it.next();
                String jid = (String) row.getValues(COL_NAMES[0]).next();
                //System.out.println("Filtering: " + jid + " :me: " + interf.getConnection().getUser().getUser());
                //filter "me" 
                if(interf.getConnection().getUser().startsWith(jid))
                    continue;
                // filter the users already in the roaster
                if (add_or_remove && r.getEntry(jid) != null)
                    continue;
                // filter the users that are not in the roaster
                if (!add_or_remove && r.getEntry(jid) == null)
                    continue;
                Object[] res = new Object[getColumnCount()];
                res[0] = jid;
                for (int i = 1; i < getColumnCount(); i++)
                {
                    Iterator vals = row.getValues(COL_NAMES[i]);
                    if (vals.hasNext()) res[i] = vals.next();
                }
                addRow(res);
            }
        }

        @Override
        public int getColumnCount()
        {
            return COL_NAMES.length;
        }

        @Override
        public String getColumnName(int column)
        {
            return COL_NAMES[column];
        }

        @Override
        public int getRowCount()
        {
            return super.getRowCount();
        }

    }

    private void buttSearchActionActionPerformed(ActionEvent event)
    {
        if (ctx == null) return;
        XMPPPeerInterface i = (XMPPPeerInterface) ctx.getPeer()
                .getPeerInterface();
        UserSearchManager search = new UserSearchManager(i.getConnection());
        try
        {
            Collection srs = search.getSearchServices();
            String searchService = (String) srs.iterator().next();
            Form searchForm = search.getSearchForm(searchService);
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", chkUsername.isSelected());
            answerForm.setAnswer("Name", chkName.isSelected());
            answerForm.setAnswer("Email", chkEmail.isSelected());
            answerForm.setAnswer("search", txtSearch.getText());
            ReportedData data = search.getSearchResults(answerForm,
                    searchService);
            getResultTable().setModel(new TableModel(data, ctx, add_or_remove));
            // TableColumn tc =
            // resultTable.getColumnModel().getColumn(0);
            // tc.setCellRenderer(new ActionsRenderer());
        }
        catch (XMPPException ex)
        {
            ex.printStackTrace();
        }
    }

    private void butAddOrRemoveActionActionPerformed(ActionEvent event)
    {
        int row = getResultTable().getSelectedRow();
        if (row < 0) return;
        XMPPPeerInterface i = (XMPPPeerInterface) ctx.getPeer()
                .getPeerInterface();
        String jid = (String) getResultTable().getValueAt(row, 0);
        Roster r = i.getConnection().getRoster();
        try
        {
            if (add_or_remove)
            {
                r.createEntry(jid,
                        (String) getResultTable().getValueAt(row, 1), null);
            }
            else
            {
                RosterEntry entry = r.getEntry(jid);
                r.removeEntry(entry);
            }
           TableModel model = (TableModel) getResultTable().getModel();
           model.removeRow(row);
        }
        catch (Exception ex)
        {
           ex.printStackTrace();  
        }
    }

    // static class ActionsRenderer extends JPanel implements TableCellRenderer
    // {
    // JButton buttAdd;
    // JButton buttRemove;
    // public ActionsRenderer()
    // {
    // this.setLayout(new FlowLayout());
    // buttAdd = new JButton("+");
    // buttRemove = new JButton("x");
    // this.add(buttAdd);
    // this.add(buttRemove);
    // }
    //
    // @Override
    // public Component getTableCellRendererComponent(JTable table,
    // Object value, boolean isSelected, boolean hasFocus, int row,
    // int column)
    // {
    // return this;
    // }
    // }

}
