package seco.notebook.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import seco.notebook.NotebookUI;
import seco.notebook.XMLConstants;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

public class NotebookPropsProvider extends CellPropsProvider
{
   
    private static final long serialVersionUID = 9182503213726009906L;

    protected void _update(JMenu menu, final NotebookUI nbui, final int off)
    {
        final CellGroupMember nb = nbui.getDoc().getBook();
        if(!(nb instanceof CellGroup))   return;
        
        JCheckBoxMenuItem initCellCheck = new JCheckBoxMenuItem("Init Book");
        initCellCheck.setSelected(CellUtils.isInitCell(nb));
        initCellCheck.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e)
            {
                CellUtils.toggleAttribute(nb, XMLConstants.ATTR_INIT_CELL);
            }
        });
        menu.add(initCellCheck);
        JCheckBoxMenuItem readonlyCellCheck = new JCheckBoxMenuItem("Readonly");
        readonlyCellCheck.setSelected(CellUtils.isReadonly(nb));
        readonlyCellCheck.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e)
            {
                CellUtils.toggleAttribute(nb, XMLConstants.ATTR_READONLY);
            }
        });
        menu.add(readonlyCellCheck);

        final JMenuItem m = new JMenuItem("Eval");
        m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                nbui.getDoc().evalGroup((CellGroup) nb);
            }
        });
        menu.add(m);
    }
}
