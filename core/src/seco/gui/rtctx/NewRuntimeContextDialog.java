package seco.gui.rtctx;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.gui.GUIHelper;
import seco.rtenv.RuntimeContext;
import seco.util.GUIUtil;

/**
 * 
 * @author Bizi
 */
public class NewRuntimeContextDialog extends JDialog
{

    private ClassPathPanel cpPanel;
    private JTextField textRtCtx;

    /** Creates new form NewRuntimeContextPanel */
    public NewRuntimeContextDialog()
    {
        super(GUIUtil.getFrame(), "Add New Runtime Context");
        if(GUIUtil.getFrame() == null) setIconImage(GUIHelper.LOGO_IMAGE);
        getContentPane().add(new MyPanel());
        setSize(350, 350);
    };

    private void save()
    {
        String name = textRtCtx.getText();
        if (name == null || name.length() == 0)
        {
            JOptionPane.showMessageDialog(GUIUtil.getFrame(),
                    "Context name cannot be empty.");
            return;
        }
        else
        {
            if (!hg.findAll(ThisNiche.graph,
                    hg.and(hg.type(RuntimeContext.class), hg.eq("name", name)))
                    .isEmpty())
            {
                JOptionPane.showMessageDialog(
                        GUIUtil.getFrame(),
                        "Context with this name is already defined.");
                return;
            }
        }
        RuntimeContext ctx = new RuntimeContext(textRtCtx.getText());
        ctx.setClassPath(cpPanel.getClassPath());
        ThisNiche.graph.add(ctx);
        setVisible(false);
    }

    class MyPanel extends JPanel
    {
        public MyPanel()
        {
            initComponents();
        }

        private void initComponents()
        {
            JPanel buttPanel = new JPanel();
            JLabel label = new JLabel("Context Name:");
            JButton buttCancel = new JButton("Cancel");
            buttCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    NewRuntimeContextDialog.this.setVisible(false);
                }
            });
            JButton buttApply = new JButton("Apply");
            buttApply.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    save();
                }
            });
            textRtCtx = new JTextField();
            cpPanel = new ClassPathPanel();

            setLayout(new java.awt.GridBagLayout());

            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            add(label, gridBagConstraints);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 7;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            add(textRtCtx, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.gridheight = 7;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
            add(cpPanel, gridBagConstraints);

            buttPanel.setLayout(new java.awt.GridBagLayout());

            buttApply.setText("Apply");
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
            buttPanel.add(buttApply, gridBagConstraints);

            buttCancel.setText("Cancel");
            buttPanel.add(buttCancel, new java.awt.GridBagConstraints());

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 7;
            gridBagConstraints.gridy = 8;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
            add(buttPanel, gridBagConstraints);
        }
    }

}
