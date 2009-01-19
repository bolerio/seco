package seco.notebook;

import static seco.notebook.ElementType.notebook;

import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.EvalCellEvent;
import seco.events.EventHandler;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellUtils;


public class OutputCellDocument extends NotebookDocument
{

    public OutputCellDocument(HGHandle h)
    {
        super(h);
    }

    private void reinit()
    {
        try
        {
            if (getLength() != 0) super.superRemove(0, getLength());
            init();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void init()
    {
        Cell book = (Cell) ThisNiche.hg.get(bookH);
        Map<StyleType, NBStyle> map = (Map<StyleType, NBStyle>) book
                .getAttribute(XMLConstants.CELL_STYLE);
        if (map != null) for (NBStyle s : map.values())
            addStyle(s);
        Vector<ElementSpec> parseBuffer = new Vector<ElementSpec>();
        SimpleAttributeSet attr = new SimpleAttributeSet();
        attr.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_LEFT);
        attr.addAttribute(ATTR_CELL, bookH);
        DocUtil.startTag(notebook, attr, 0, parseBuffer);
        DocUtil.createOutputCell(this, bookH, attr, parseBuffer);
        DocUtil.endTag(parseBuffer);
        create(parseBuffer.toArray(new ElementSpec[parseBuffer.size()]));
    }

    public HGHandle getHandle()
    {
        if (handle != null) return handle;

        Set<HGHandle> list = CellUtils.findAll(ThisNiche.hg, hg
                .type(OutputCellDocument.class));
        for (HGHandle h : list)
            if (bookH.equals(((OutputCellDocument) ThisNiche.hg.get(h)).bookH)) return handle = h;
        // System.out.println("Adding DOC: " + getTitle());
        handle = ThisNiche.hg.add(this);

        return handle;
    }
    
    public String getTitle()
    {
        return "";
    }

    public void setTitle(String t)
    {
    }

    public static class CopyEvalCellHandler implements EventHandler
    {
        private static HGHandle instance = null;

        public static HGHandle getInstance()
        {
            if (instance == null)
            {
                instance = hg.findOne(ThisNiche.hg, hg.and(hg
                        .type(CopyEvalCellHandler.class)));
                if (instance == null) instance = ThisNiche.hg
                        .add(new CopyEvalCellHandler());
            }
            return instance;
        }

        public void handle(HGHandle eventType, Object event,
                HGHandle publisher, HGHandle subscriber)
        {
            if (eventType.equals(EvalCellEvent.HANDLE))
            {
                EvalCellEvent e = (EvalCellEvent) event;
                Object sub = ThisNiche.hg.get(subscriber);
                Object pub = ThisNiche.hg.get(publisher);
                if (pub instanceof CellGroupMember
                        && sub instanceof OutputCellDocument)
                {
                     System.out.println("CellDocument - CopyEvalCellHandler: "
                            + e.getValue());
                    ((OutputCellDocument) sub).reinit();
                }
            }
        }
    }
}
