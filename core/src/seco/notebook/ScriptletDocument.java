package seco.notebook;

import static seco.notebook.ElementType.commonCell;
import static seco.notebook.ElementType.htmlCell;
import static seco.notebook.ElementType.inputCellBox;
import static seco.notebook.ElementType.notebook;

import java.util.Map;
import java.util.Vector;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.CellTextChangeEvent;
import seco.events.handlers.AttributeChangeHandler;
import seco.events.handlers.CellTextChangeHandler;
import seco.things.Cell;
import seco.things.CellUtils;

public class ScriptletDocument extends NotebookDocument
{
    public ScriptletDocument(HGHandle h)
    {
        super(h);
    }
    
    public void init()
    {
//        DocumentListener[] ls = listenerList.getListeners(DocumentListener.class);
//        for(int i = 0; i < ls.length; i++)
//            removeDocumentListener(ls[i]);
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
        createCell(this, bookH, attr, parseBuffer); 
        DocUtil.endTag(parseBuffer);
        create(parseBuffer.toArray(new ElementSpec[parseBuffer.size()]));
        update(UpdateAction.tokenize);
        setModified(false);
        if(NotebookDocument.DIRECT_EVENTING)
           CellUtils.addMutualEventPubSub(AttributeChangeEvent.HANDLE, bookH,
                    getHandle(), AttributeChangeHandler.getHandle());
        else
           CellUtils.addMutualEventPubSub(AttributeChangeEvent.HANDLE, bookH,
                getHandle(), AttributeChangeHandler.getHandle());
        inited = true;
    }
    
    static void createCell(NotebookDocument doc, HGHandle cellH, 
            MutableAttributeSet attr, Vector<ElementSpec> vec)
    {
        Cell cell = (Cell) ThisNiche.graph.get(cellH);
        // System.out.println("createCell: " + cellH + ":" + cell);
        attr = DocUtil.getDocStyle(doc, StyleType.inputCell);
        DocUtil.startTag(inputCellBox, attr, 0, vec);
        if (!CellUtils.isHTML(cell)) DocUtil.startTag(commonCell, attr, 0, vec);
        else
            DocUtil.startTag(htmlCell, attr, 0, vec);
        String text = CellUtils.getText(cell);
        if (!text.endsWith("\n")) text += "\n";
        DocUtil.addContent(text.toCharArray(), 0, vec, 0);
        DocUtil.endTag(vec);
        DocUtil.endTag(vec);
        CellUtils.addEventPubSub(AttributeChangeEvent.HANDLE, cellH,
                doc.getHandle(), AttributeChangeHandler.getHandle());
        if(NotebookDocument.DIRECT_EVENTING)
           CellUtils.addEventPubSub(CellTextChangeEvent.HANDLE, cellH,
                    doc.getHandle(), CellTextChangeHandler.getHandle());
        else
           CellUtils.addMutualEventPubSub(CellTextChangeEvent.HANDLE, cellH,
                doc.getHandle(), CellTextChangeHandler.getHandle());
    }
    
    public boolean evalCell(Element el) throws BadLocationException
    {
        return false;
    }
    
    public void removeCellBoxElement(Element el) throws BadLocationException
    {
        //DO NOTHING
    }
    
    public String toString()
    {
        return "SCRIPTLET_DOC: " + getHandle();
    }
}
