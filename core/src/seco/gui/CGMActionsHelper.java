package seco.gui;

import static seco.U.hget;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.ThisNiche;
import seco.gui.GUIHelper.PiccoloMenu;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.XMLConstants;
import seco.notebook.gui.menu.DynamicMenuProvider;
import seco.notebook.gui.menu.EnhancedMenu;
import seco.rtenv.RuntimeContext;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;

public class CGMActionsHelper
{
    public static enum Scope
    {
        cell, group, book
    }
    
    private static final String LABEL_RUNTIME_CONTEXT = "Set Runtime Context";
    public static final HGPersistentHandle CELL_MENU_HANDLE = UUIDHandleFactory.I
            .makeHandle("b26a4220-ee65-11df-98cf-0800200c9a66");
    public static final HGPersistentHandle CELL_GROUP_MENU_HANDLE = UUIDHandleFactory.I
          .makeHandle("1aee3830-ee78-11df-98cf-0800200c9a66");
    public static final HGPersistentHandle NOTEBOOK_MENU_HANDLE = UUIDHandleFactory.I
           .makeHandle("2f844cd0-ee78-11df-98cf-0800200c9a66");

    public static Element getOwnerElement(Scope cellOrGroup)
    {
        NotebookUI nbui = NotebookUI.getFocusedNotebookUI();
        if (cellOrGroup == Scope.cell) 
        {
            Element el = nbui.getSelectedCellElement();
            return el != null ? el : nbui.getSelectedOutputCellElement();
        }
        else if (cellOrGroup == Scope.group) return nbui
                .getSelectedGroupElement();
        else if (cellOrGroup == Scope.book)
            return nbui.getDoc().getDefaultRootElement();
        return null;
    }

    public static CellGroupMember getOwnerCGM(Scope cellOrGroup)
    {
        Element el = getOwnerElement(cellOrGroup);
        return (el != null) ? NotebookDocument.getNBElement(el) : null;
    }

    public interface SelectableAction
    {
        boolean isSelected();
    }
    
    public static abstract class CellAction extends AbstractAction
    {
        protected Scope scope = Scope.cell;

        public CellAction(String name, Scope cell_or_group)
        {
            super(name);
            this.scope = cell_or_group;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (!isEnabled()) return;
            NotebookUI nbui = NotebookUI.getFocusedNotebookUI();
            final Element el = nbui.getSelectedCellElement();
            final CellGroupMember nb = (enabled) ? NotebookDocument
                    .getNBElement(el) : null;
            do_action(nb);
        }

        public abstract void do_action(CellGroupMember nb);

        @Override
        public boolean isEnabled()
        {
            NotebookUI nbui = NotebookUI.getFocusedNotebookUI();
            if (nbui == null) return false;
            return getOwnerElement() != null;
        }

        protected Element getOwnerElement()
        {
            return CGMActionsHelper.getOwnerElement(scope);
        }

        protected CellGroupMember getOwnerCGM()
        {
            return CGMActionsHelper.getOwnerCGM(scope);
        }

        public Scope getScope()
        {
            return scope;
        }

        public void setScope(Scope cellOrGroup)
        {
            this.scope = cellOrGroup;
        }

    }

    public static class InitCellAction extends CellAction implements
            SelectableAction
    {
        private static final String INIT_CELL_ACTION = "Init Cell";

        public InitCellAction()
        {
            super(INIT_CELL_ACTION, Scope.cell);
        }

        public InitCellAction(Scope cell_or_group)
        {
            super(INIT_CELL_ACTION, cell_or_group);
            this.putValue(Action.NAME, (cell_or_group == Scope.group) ?
                    "Init Group" : "Init Book");
        }

        public void do_action(CellGroupMember nb)
        {
            CellUtils.toggleAttribute(getOwnerCGM(),
                    XMLConstants.ATTR_INIT_CELL);
        }

        public boolean isSelected()
        {
            return getOwnerCGM() != null && CellUtils.isInitCell(getOwnerCGM());
        }
    }

    public static class ReadOnlyCellAction extends CellAction implements
            SelectableAction
    {
        private static final String NAME = "Readonly";

        public ReadOnlyCellAction()
        {
            super(NAME, Scope.cell);
        }

        public ReadOnlyCellAction(Scope cell_or_group)
        {
            super(NAME, cell_or_group);
        }

        public void do_action(CellGroupMember nb)
        {
            CellUtils
                    .toggleAttribute(getOwnerCGM(), XMLConstants.ATTR_READONLY);
        }

        public boolean isSelected()
        {
            return CellUtils.isReadonly(getOwnerCGM());
        }
    }

    public static class HtmlCellAction extends CellAction implements
            SelectableAction
    {
        private static final String NAME = "Html View";

        public HtmlCellAction()
        {
            super(NAME, Scope.cell);
        }

        public void do_action(CellGroupMember nb)
        {
            CellUtils.toggleAttribute(getOwnerCGM(), XMLConstants.ATTR_HTML);
        }

        public boolean isSelected()
        {
            return CellUtils.isHTML(getOwnerCGM());
        }

        @Override
        public boolean isEnabled()
        {
            boolean b = super.isEnabled();
            return (b) ? "html".equals(CellUtils.getEngine(getOwnerCGM()))
                    : false;
        }
    }

    public static class ErrorCellAction extends CellAction implements
            SelectableAction
    {
        private static final String NAME = "Error Cell";

        public ErrorCellAction()
        {
            super(NAME, Scope.cell);
        }

        public void do_action(CellGroupMember nb)
        {
            NotebookUI nbui = NotebookUI.getFocusedNotebookUI();
            Element output_el = nbui.getSelectedOutputCellElement();
            CellGroupMember out = NotebookDocument.getNBElement(output_el);
            CellUtils.toggleAttribute(out, XMLConstants.ATTR_ERROR);
        }

        public boolean isSelected()
        {
            return CellUtils.isError(getOwnerCGM());
        }

        @Override
        public boolean isEnabled()
        {
            NotebookUI nbui = NotebookUI.getFocusedNotebookUI();
            if(nbui == null) return false;
            final Element output_el = nbui.getSelectedOutputCellElement();
            if (output_el != null)
            {
                return  NotebookDocument.getNBElement(output_el) != null;
            }
            return false;
        }
    }

    public static class EvalAction extends CellAction
    {
        private static final String NAME = "Eval";

        public EvalAction()
        {
            super(NAME, Scope.cell);
        }

        public EvalAction(Scope cell_or_group)
        {
            super(NAME, cell_or_group);
        }

        public void do_action(CellGroupMember nb)
        {
            NotebookUI nbui = NotebookUI.getFocusedNotebookUI();
            final Element el = getOwnerElement();
            try
            {
                if(scope == Scope.cell)
                   nbui.getDoc().evalCellInAuxThread(el);
                else
                   nbui.getDoc().evalGroup(
                            (CellGroup) NotebookDocument.getNBElement(el));
            }
            catch (BadLocationException ex)
            {
                ex.printStackTrace();
            }
        }
        
        public boolean isEnabled()
        {
            boolean b = super.isEnabled();
            if(!b) return false;
            if (scope == Scope.cell && !CellUtils.isInputCell(getOwnerCGM())) return false;
            return true;
        }
    }

    public static class RemoveOutputCellsAction extends CellAction
    {
        private static final String NAME = "Remove Output Cells";

        public RemoveOutputCellsAction()
        {
            super(NAME, Scope.cell);
        }

        public void do_action(CellGroupMember nb)
        {
            List<HGHandle> outs = CellUtils.getOutCellHandles(ThisNiche
                    .handleOf(getOwnerCGM()));
            for (HGHandle h : outs)
                ThisNiche.graph.remove(h, true);
        }
        
        public boolean isEnabled()
        {
            boolean b = super.isEnabled();
            if(!b) return false;
            if (scope == Scope.cell && !CellUtils.isInputCell(getOwnerCGM())) return false;
            return true;
        }
    }

    public static class DescriptionAction extends CellAction
    {
        private static final String NAME = "Add/Edit Description";

        public DescriptionAction()
        {
            super(NAME, Scope.cell);
        }

        public DescriptionAction(Scope cell_or_group)
        {
            super(NAME, cell_or_group);
        }

        public void do_action(CellGroupMember nb)
        {
            CommonActions.editCGMDescription(ThisNiche.handleOf(getOwnerCGM()));
        }
        
        public boolean isEnabled()
        {
            boolean b = super.isEnabled();
            return (b) ? CellUtils.isInputCell(getOwnerCGM()): false;
        }
    }

    public static class RCListProvider implements DynamicMenuProvider
    {
        protected Scope scope;

        public RCListProvider()
        {
        }

        public RCListProvider(Scope cell_or_group)
        {
            this.scope = cell_or_group;
        }

        public void update(JMenu m)
        {
            CellGroupMember owner = CGMActionsHelper.getOwnerCGM(scope);
            if (scope == Scope.cell && !CellUtils.isInputCell(owner)) return;
           
            ButtonGroup group = new ButtonGroup();
            final HGHandle bh = ThisNiche.graph.getHandle(owner);
            List<HGHandle> list = hg.findAll(ThisNiche.graph, hg.type(RuntimeContext.class));
            for (final HGHandle rh: list)
            {
                RuntimeContext rc = (RuntimeContext) hget(rh);
                final JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                        rc.getName());
                if (ThisNiche.getContextHandleFor(bh).equals(rh))
                    item.setSelected(true);
                group.add(item);
                Action act = new AbstractAction() {
                    public void actionPerformed(ActionEvent e)
                    {
                        ThisNiche.setContextFor(bh, rh);
                        item.setSelected(true);
                    }
                };
                act.putValue(Action.NAME, rc.getName());
                item.setAction(act);
                m.add(item);
            }
        }
        
        public Scope getScope()
        {
            return scope;
        }

        public void setScope(Scope scope)
        {
            this.scope = scope;
        }

        public boolean updateEveryTime()
        {
            return true;
        }
    }

    public static JMenu getCellMenu()
    {
        JMenu menu = ThisNiche.graph.get(CELL_MENU_HANDLE);
        if (menu == null)
        {
            menu = new Menu("Cell", Scope.cell);
            menu.add(new JCheckBoxMenuItem(new InitCellAction()));
            menu.add(new JCheckBoxMenuItem(new ReadOnlyCellAction()));
            menu.add(new JCheckBoxMenuItem(new HtmlCellAction()));
            //TODO: fix the bug in the action
            //menu.add(new JCheckBoxMenuItem(new ErrorCellAction()));

            menu.add(new JMenuItem(new EvalAction()));
            menu.add(new JMenuItem(new RemoveOutputCellsAction()));
            menu.add(new EnhancedMenu(LABEL_RUNTIME_CONTEXT,
                    new RCListProvider(Scope.cell)));
            menu.add(new JMenuItem(new DescriptionAction()));

            ThisNiche.graph.define(CELL_MENU_HANDLE, menu);
        }
        return menu;
    }
    
    public static JMenu getCellGroupMenu()
    {
        JMenu menu = ThisNiche.graph.get(CELL_GROUP_MENU_HANDLE);
        if (menu == null)
        {
            menu = new Menu("Cell Group", Scope.group);
            menu.add(new JCheckBoxMenuItem(new InitCellAction(Scope.group)));
            menu.add(new JCheckBoxMenuItem(new ReadOnlyCellAction(Scope.group)));
            menu.add(new JMenuItem(new EvalAction(Scope.group)));
            menu.add(new EnhancedMenu(LABEL_RUNTIME_CONTEXT,
                    new RCListProvider(Scope.group)));
            menu.add(new JMenuItem(new DescriptionAction(Scope.group)));
            ThisNiche.graph.define(CELL_GROUP_MENU_HANDLE, menu);
        }
        return menu;
    }
    
    public static JMenu getNotebookMenu()
    {
        JMenu menu = ThisNiche.graph.get(NOTEBOOK_MENU_HANDLE);
        if (menu == null)
        {
            menu = new Menu("Book", Scope.book);
            menu.add(new JCheckBoxMenuItem(new InitCellAction(Scope.book)));
            menu.add(new JCheckBoxMenuItem(new ReadOnlyCellAction(Scope.book)));
            //menu.add(new JMenuItem(new EvalAction(Scope.group)));
            menu.add(new EnhancedMenu(LABEL_RUNTIME_CONTEXT,
                    new RCListProvider(Scope.book)));
            //menu.add(new JMenuItem(new DescriptionAction(Scope.group)));
            
            ThisNiche.graph.define(NOTEBOOK_MENU_HANDLE, menu);
        }
        return menu;
    }
    
    
    public static class Menu extends PiccoloMenu implements MenuListener
    {
        protected Scope scope;
        
        public Menu()
        {
            super();
            addMenuListener(this);
        }

        public Menu(String s, Scope scope)
        {
            super(s);
            this.scope = scope;
            addMenuListener(this);
        }

        public Scope getScope()
        {
            return scope;
        }

        public void setScope(Scope scope)
        {
            this.scope = scope;
        }

        public void menuSelected(MenuEvent e)
        {
            boolean b = CGMActionsHelper.getOwnerCGM(scope) != null;
            for (int i = 0; i < getMenuComponentCount(); i++)
            {
                Component c = getMenuComponent(i);
                if (c instanceof JMenuItem)
                {
                    Action a = ((JMenuItem) c).getAction();
                    if (a != null) 
                        b = a.isEnabled();
                    if(b && c instanceof JCheckBoxMenuItem && a instanceof SelectableAction)
                        ((JCheckBoxMenuItem) c).setSelected(((SelectableAction)a).isSelected());
                }
                c.setEnabled(b);
                c.setVisible(b);
            }
            getPopupMenu().invalidate();
        }

        public void menuCanceled(MenuEvent e)
        {
        }

        public void menuDeselected(MenuEvent e)
        {
        }
    }
}
