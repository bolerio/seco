package seco.notebook;

import static seco.notebook.ElementType.notebook;

import java.util.Map;
import java.util.Vector;

import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import seco.ThisNiche;
import seco.events.EvalCellEvent;
import seco.events.EventHandler;
import seco.events.handlers.EvalCellHandler;
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
        DocumentListener[] ls = listenerList.getListeners(DocumentListener.class);
        for(int i = 0; i < ls.length; i++)
            removeDocumentListener(ls[i]);
        if (inited) return;
        Cell book = (Cell) ThisNiche.graph.get(bookH);
        Map<StyleType, NBStyle> map = (Map<StyleType, NBStyle>) book
                .getAttribute(XMLConstants.CELL_STYLE);
        if (map != null) for (NBStyle s : map.values())
            addStyle(s);
        Vector<ElementSpec> parseBuffer = new Vector<ElementSpec>();
        SimpleAttributeSet attr = new SimpleAttributeSet();
        attr.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_LEFT);
        attr.addAttribute(ATTR_CELL, bookH);
        DocUtil.startTag(notebook, attr, 0, parseBuffer);
        DocUtil.createOutputCell(this, bookH, attr, parseBuffer, false);
        DocUtil.endTag(parseBuffer);
        create(parseBuffer.toArray(new ElementSpec[parseBuffer.size()]));
        if(NotebookDocument.DIRECT_EVENTING)
           CellUtils.addEventPubSub(EvalCellEvent.HANDLE, bookH, getHandle(),
                    EvalCellHandler.getHandle()); 
        else
           CellUtils.addMutualEventPubSub(EvalCellEvent.HANDLE, bookH, getHandle(),
                EvalCellHandler.getHandle());
        inited = true;
    }

    public void removeCellBoxElement(Element el) throws BadLocationException
    {
        //DO NOTHING
    }
    
    public String getTitle()
    {
        return "";
    }

    public void setTitle(String t)
    {
    }
    
    @Override
    public void cellEvaled(EvalCellEvent e)
    {
        reinit();
        //super.cellEvaled(e);
    }
    
    public String toString()
    {
        return "OUTCELLDOC: " + getHandle();
    }

    public static class CopyEvalCellHandler implements EventHandler
    {
        private static HGHandle instance = null;

        public static HGHandle getInstance()
        {
            if (instance == null)
            {
                instance = hg.findOne(ThisNiche.graph, hg.and(hg
                        .type(CopyEvalCellHandler.class)));
                if (instance == null) instance = ThisNiche.graph
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
                Object sub = ThisNiche.graph.get(subscriber);
                Object pub = ThisNiche.graph.get(publisher);
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
