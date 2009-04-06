/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.notebook.view.CellHandleView;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellUtils;


public class SelectionManager extends
        HashMap<Element, SelectionManager.Selection>
{
    private final NotebookUI notebookUI;
    // selected cell elements
    private SortedSet<Element> selection = new TreeSet<Element>(
            new ElementComparator<Element>());

    /**
     * @param notebookUI
     */
    SelectionManager(NotebookUI notebookUI)
    {
        this.notebookUI = notebookUI;
    }

    public void clearSelections()
    {
        for (SelectionManager.Selection s : values())
            s.setSelected(false);
        selection.clear();
    }

    public void addCellSelection(Element el)
    {
        check_remove_nested(el);
        selection.add(el);
        get(el).setSelected(true);
        get(el).requestFocus();
    }

    public void removeCellSelection(Element el)
    {
        get(el).setSelected(false);
        selection.remove(el);
    }

    public SortedSet<Element> getSelection()
    {
        return selection;
    }

    public boolean canUngroup()
    {
        if (selection.isEmpty()) return false;
        for (Element el : selection)
        {
            CellGroupMember nb = NotebookDocument.getNBElement(el);
            if (!(nb instanceof CellGroup)) return false;
            // if(nb.getParent() instanceof Notebook)
            // {
            // CellGroup c =(CellGroup) nb;
            // for(int i = 0; i < c.size(); i++)
            // if(!(c.getElement(i)instanceof CellGroup))
            // return false;
            // }
        }
        return true;
    }

    public boolean canGroup()
    {
        // System.out.println("canGroup: " + selection.size() + ":" +selection);
        if (selection.size() < 2) return false;
        int index = -1;
        Element common_par = null;
        for (Element el : selection)
        {
            HGHandle nb = NotebookDocument.getNBElementH(el);
            if (index < 0)
            {
                common_par = NotebookDocument.getContainerEl(el, false);
                CellGroup par = (common_par == null) ? (CellGroup) notebookUI
                        .getDoc().getBook() : (CellGroup) NotebookDocument
                        .getNBElement(common_par);
                index = par.indexOf(nb);
                continue;
            }
            Element par = NotebookDocument.getContainerEl(el, false);
            if (common_par == null) common_par = el.getDocument()
                    .getRootElements()[0];
            if (!common_par.equals(par)) return false;
            int next_ind = ((CellGroup) NotebookDocument.getNBElement(par))
                    .indexOf(nb);
            if (next_ind - index != 1) return false;
            index = next_ind;
        }
        return true;
    }

    public void group()
    {
        NotebookDocument doc = notebookUI.getDoc();
        try
        {
            Vector<Element> temp = new Vector<Element>(selection);
            selection.clear();
            doc.group(temp);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }

    public void ungroup()
    {
        NotebookDocument doc = notebookUI.getDoc();
        try
        {
            Vector<Element> temp = new Vector<Element>(selection);
            selection.clear();
            for (Element el : temp)
                doc.ungroup(el);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }

    public void up(Element el, boolean select)
    {
        // System.out.println("SelectionManager - up: " + select + ":" + el);
        // CellGroupMember from = NotebookDocument.getNBElement(el);
        CellGroupMember to = prev_nb(el);
        common_sel(el, to, select);
        if (to == null) return;
        adjustScrollBar(getElement(to), true);
    }

    public void down(Element el, boolean select)
    {
        // System.out.println("SelectionManager - down: " + select + ":" + el);
        // CellGroupMember from = NotebookDocument.getNBElement(el);
        CellGroupMember to = next_nb(el);
        common_sel(el, to, select);
        if (to == null) return;
        adjustScrollBar(getElement(to), false);
    }

    private void adjustScrollBar(Element el, boolean up)
    {
        if (el == null) return;
        int off = (up) ? el.getStartOffset() : el.getEndOffset();
        Position.Bias bias = (up) ? Position.Bias.Backward
                : Position.Bias.Forward;
        Utilities.adjustScrollBar(notebookUI, off, bias);
    }

    private void common_sel(Element el, CellGroupMember to, boolean select)
    {
        if (to != null)
        {
            if (!select) clearSelections();
            // if destination is already selected, deselect target and focus
            // dest
            if (isSelected(to))
            {
                removeCellSelection(el);
                get(getElement(to)).requestFocus();
            } else
                select(to, true);
        }
    }

    public void left(Element el, boolean select)
    {
        // System.out.println("SelectionManager - left: " + select + ":" + el);
        CellGroupMember from = NotebookDocument.getNBElement(el);
        if(from instanceof CellGroup && ((CellGroup)from).getArity() > 0)
        {
            if (!select) clearSelections();
            else
                removeCellSelection(el);
            select(((CellGroup)from).getElement(0), true);
        } else
        {
            clearSelections();
            notebookUI.setCaretPosition(el.getStartOffset());
            notebookUI.requestFocus();
        }
    }

    public void right(Element el, boolean select)
    {
        if (notebookUI.entering_sel_mode_in_progress)
        {
            notebookUI.entering_sel_mode_in_progress = false;
            return;
        }
        // System.out.println("SelectionManager - right: " + select + ":" + el);
       // CellGroupMember from = NotebookDocument.getNBElement(el);
        // System.out.println("SelectionManager - parents: " + from.getParent()
        // + ":" + from.getNotebook());
        Element par = NotebookDocument.getContainerEl(el, false);
        if (par != el.getDocument().getRootElements()[0])
        {
            if (!select) clearSelections();
            else
                removeCellSelection(el);
            select(NotebookDocument.getNBElement(par), true);
        }
    }

    public void addCellRangeSelection(Element to)
    {
        if (selection.size() == 0)
        {
            clearSelections();
            addCellSelection(to);
            return;
        }
        // if already selected, just unselect
        if (selection.contains(to))
        {
            removeCellSelection(to);
            return;
        }
        // check if one of the selected handles is contained in the other
        if (check_remove_nested(to)) return;
        Element from = selection.last();
        if (from.getStartOffset() > to.getStartOffset())
        {
            Element temp = from;
            from = to;
            to = temp;
        }
        clearSelections();
        addRange(from, to, true, true);
    }

    private boolean check_remove_nested(Element el)
    {
        boolean result = false;
        //CellGroupMember nb = NotebookDocument.getNBElement(el);
        Vector<Element> removed = new Vector<Element>(selection.size());
        for (Element e : selection)
        {
            if (isNested(e, el) || isNested(el, e))
            {
                result = true;
                // System.out.println("Nested: " + nb + ":" +
                // NotebookDocument.getNBElement(e));
                get(e).setSelected(false);
                removed.add(e);
            }
        }
        for (Element e : removed)
            selection.remove(e);
        return result;
    }

    private boolean isNested(Element inner, Element outer)
    {
        return inner.getStartOffset() >= outer.getStartOffset()
                && inner.getEndOffset() <= outer.getEndOffset();
    }

    private void addRange(Element from, Element to, boolean select_first,
            boolean select_last)
    {
        CellGroupMember nb_from = NotebookDocument.getNBElement(from);
        CellGroupMember nb_to = NotebookDocument.getNBElement(to);
        NotebookDocument doc = notebookUI.getDoc();
        // System.out.println("addRange: " + nb_from + ":" + nb_to +
        // ":" + select_first + ":" + select_last);
        Element par_el = NotebookDocument.getContainerEl(from, false);
        Element to_par = NotebookDocument.getContainerEl(to, false);
        CellGroup parent = (par_el != null) ? (CellGroup) NotebookDocument
                .getNBElement(par_el) : (CellGroup) doc.getBook();
        if (par_el == null) par_el = doc.getRootElements()[0];
        if (par_el.equals(to_par))
        {
            int i_from = parent.indexOf(nb_from);
            int i_to = parent.indexOf(nb_to);
            if (!select_last && i_to != 0) i_to--;
            if (!select_first) i_from++;
            for (int i = i_from; i <= i_to; i++)
                select(parent.getElement(i));
            return;
        } else
        {
            par_el = same_level_el(from, to);
            parent = (par_el != null) ? (CellGroup) NotebookDocument
                    .getNBElement(par_el) : (CellGroup) doc.getBook();
            if (parent != null)
            {
                if (!nb_from.equals(parent)) addRange(getElement(parent),
                        getElement(nb_to), false, true);
                parent = (CellGroup) doc.getContainer(from);
                addRange(getElement(nb_from), getElement(parent
                        .getElement(parent.getArity() - 1)), true, true);
                return;
            }
            par_el = same_level_el(to, from);
            parent = (par_el != null) ? (CellGroup) NotebookDocument
                    .getNBElement(par_el) : (CellGroup) doc.getBook();
            if (parent != null)
            {
                if (!nb_from.equals(parent)) addRange(getElement(nb_from),
                        getElement(parent), true, false);
                addRange(getElement(parent.getElement(0)), getElement(nb_to),
                        true, true);
                return;
            }
            // par_el = NotebookDocument.getContainerEl(from, false);
           // addRange(from,
           //         getElement(parent.getElement(parent.getArity() - 1)), true,
          //          true);
          //  addRange(getElement(next_nb(getElement(parent))), to, false, false);
        }
    }

    private CellGroupMember next_nb(Element el)
    {
        NotebookDocument doc = notebookUI.getDoc();
        Element par_el = NotebookDocument.getContainerEl(el, false);
        CellGroup parent = (par_el == null) ? (CellGroup) doc.getBook()
                : (CellGroup) NotebookDocument.getNBElement(par_el);
        if (parent == null) return null;
        if (par_el == null) par_el = doc.getRootElements()[0];
        int index = parent.indexOf(NotebookDocument.getNBElementH(el));
        if (index < parent.getArity() - 1) return parent.getElement(index + 1);
        return next_nb(par_el);
    }

    private CellGroupMember prev_nb(Element el)
    {
        NotebookDocument doc = notebookUI.getDoc();
        Element par_el = NotebookDocument.getContainerEl(el, false);
        CellGroup parent = (par_el == null) ? (CellGroup) doc.getBook()
                : (CellGroup) NotebookDocument.getNBElement(par_el);

        if (parent == null) return null;
        if (par_el == null) par_el = doc.getRootElements()[0];
        int index = parent.indexOf(NotebookDocument.getNBElementH(el));
        if (index > 0) return parent.getElement(index - 1);
        return prev_nb(par_el);
    }

    private Element same_level_el(Element from, Element to)
    {
        Element parent = NotebookDocument.getContainerEl(from, false);
        while (parent != from.getDocument().getRootElements()[0])
        {
            Element temp = NotebookDocument.getContainerEl(parent, false);
            Element to_el = NotebookDocument.getContainerEl(to, false);
            if (temp != null && temp.equals(to_el)) return parent;
            parent = temp;
        }
        return null;
    }

    private void select(CellGroupMember nb)
    {
        select(nb, false);
    }

    private boolean isSelected(CellGroupMember nb)
    {
        Element el = getElement(nb);
        if (el == null) return false;
        return selection.contains(el);
    }

    private Element getElement(CellGroupMember nb)
    {
        for (Element el : keySet())
            if (nb.equals(NotebookDocument.getNBElement(el))
                    && get(el) instanceof CellHandleView.CustomButton) return el;
        return null;
    }
    
    private Element getOutElement(CellGroupMember nb)
    {
        List<HGHandle> list = 
            CellUtils.getOutCellHandles(ThisNiche.handleOf(nb));
        List<CellGroupMember> mems = new ArrayList(list.size());
        for(HGHandle h: list)
          mems.add((CellGroupMember) ThisNiche.hg.get(h));
        for (Element el : keySet())
            if (get(el) instanceof CellHandleView.CustomButton &&
                mems.contains(NotebookDocument.getNBElement(el)))
                    return el;
        return null;
    }
    
    private Element getElementH(HGHandle nb)
    {
        for (Element el : keySet())
            if (nb.equals(NotebookDocument.getNBElementH(el))
                    && get(el) instanceof CellHandleView.CustomButton) return el;
        return null;
    }

    private void select(CellGroupMember nb, boolean focus)
    {
        Element el = getElement(nb);
        if (el == null) return;
        // System.out.println("Select: " + nb + ":" + selection.contains(el));
        //Element outEl = getOutElement(nb);
        if (!selection.contains(el))
        {
            check_remove_nested(el);
            selection.add(el);
            get(el).setSelected(true);
            if (focus)
            {
                get(el).requestFocus();
            }
            //if (outEl != null) select(NotebookDocument.getNBElement(outEl));
            return;
        } else
        {
            removeCellSelection(el);
            //if (outEl != null) removeCellSelection(outEl);
        }
    }

    @Override
    public void clear()
    {
        selection.clear();
        super.clear();
    }

    @Override
    public SelectionManager.Selection remove(Object key)
    {
        selection.remove((Element) key);
        return super.remove(key);
    }

    // not used now, but maybe it will when some misterious
    // add/removeNotify bug arise again
    public void removeByValue(Selection sel)
    {
        for (Element key : keySet())
        {
            Selection s = get(key);
            if (s == sel)
            {
                remove(key);
                selection.remove(key);
                return;
            }
        }
    }

    private static class ElementComparator<T extends Element> implements
            Comparator<T>
    {
        public int compare(T obj1, T obj2)
        {
            int c = obj1.getStartOffset() - obj2.getStartOffset();
            return (c != 0) ? c : obj1.getEndOffset() - obj2.getEndOffset();
        }
    }

    public interface Selection
    {
        public void setSelected(boolean selected);

        public void requestFocus();
    }
}
