package seco.gui.rtctx;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.rtenv.ClassPath;
import seco.rtenv.ClassPathEntry;
import seco.rtenv.RuntimeContext;
import seco.util.GUIUtil;

/**
 * 
 * @author bizi
 */
public class ClassPathPanel extends JPanel
{
    private static final long serialVersionUID = -5665321328192942382L;
    private JList cpList;
    private RuntimeContext runtimeContext;
    boolean autoUpdateCtx = true;

    /** Creates new form ClassPathPanel */
    public ClassPathPanel()
    {
        initComponents();
    }

    public RuntimeContext getRuntimeContext()
    {
        return runtimeContext;
    }

    public void setRuntimeContext(RuntimeContext _runtimeContext)
    {
        if(runtimeContext != null && runtimeContext.equals(_runtimeContext)) return;
        this.runtimeContext = _runtimeContext;
        cpList.setModel(new CPListModel(runtimeContext.getClassPath()));
    }

    private void initComponents()
    {
        JLabel label = new JLabel("Class Path");
        setLayout(new java.awt.GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        add(label, gridBagConstraints);
        cpList = new JList();
        cpList.setModel(new CPListModel(new ClassPath()));
        JScrollPane scroll = new JScrollPane(cpList);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 176;
        gridBagConstraints.ipady = 228;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(scroll, gridBagConstraints);
        
        JButton butAddCp = new JButton();
        JButton butRemCp = new JButton();
        

        butAddCp.setText("Add ClassPath Entry");
        butAddCp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            {
                addCPEntry(evt);
                if(autoUpdateCtx) updateCtx();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(butAddCp, gridBagConstraints);

        butRemCp.setText("Remove Selected");
        butRemCp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            {
                removeCpEntry(evt);
                if(autoUpdateCtx) updateCtx();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(butRemCp, gridBagConstraints);
        
   }

    private void addCPEntry(ActionEvent evt)
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select JAR or Directory");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (chooser.showDialog(GUIUtil.getFrame(evt), "Select") == JFileChooser.APPROVE_OPTION)
        {
            CPListModel model = (CPListModel) cpList.getModel();
            for (File f : chooser.getSelectedFiles())
                model.addEntry(new ClassPathEntry(f));
        }
    }

    private void removeCpEntry(ActionEvent evt)
    {
        if(!(cpList.getModel() instanceof CPListModel)) return;
        try
        {
            CPListModel model = (CPListModel) cpList.getModel();
            model.removeEntries(cpList.getSelectedIndices());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    private void updateCtx()
    {
        if(runtimeContext == null) return;
        HGHandle h = ThisNiche.handleOf(runtimeContext);
        if(h == null) return;
        runtimeContext.setClassPath(getClassPath());
        ThisNiche.graph.replace(h, runtimeContext);
    }

    public ClassPath getClassPath()
    {
        return ((CPListModel) cpList.getModel()).getClassPath();
    }

}
