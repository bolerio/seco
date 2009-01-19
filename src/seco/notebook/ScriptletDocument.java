package seco.notebook;

import static seco.notebook.ElementType.commonCell;
import static seco.notebook.ElementType.htmlCell;
import static seco.notebook.ElementType.inputCellBox;
import static seco.notebook.ElementType.notebook;
import static seco.notebook.ElementType.wholeCell;

import java.util.Map;
import java.util.Vector;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.hypergraphdb.HGHandle;

import seco.ThisNiche;
import seco.events.AttributeChangeEvent;
import seco.events.CellTextChangeEvent;
import seco.events.handlers.AttributeChangeHandler;
import seco.events.handlers.CellTextChangeHandler;
import seco.notebook.NotebookDocument.UpdateAction;
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
        createCell(this, bookH, attr, parseBuffer); 
        DocUtil.endTag(parseBuffer);
        create(parseBuffer.toArray(new ElementSpec[parseBuffer.size()]));
        update(UpdateAction.tokenize);
        update(UpdateAction.evalInitCells);
        setModified(false);
        CellUtils.addMutualEventPubSub(AttributeChangeEvent.HANDLE, bookH,
                getHandle(), AttributeChangeHandler.getInstance());
    }
    
    static void createCell(NotebookDocument doc, HGHandle cellH, 
            MutableAttributeSet attr, Vector<ElementSpec> vec)
    {
        Cell cell = (Cell) ThisNiche.hg.get(cellH);
        // System.out.println("createCell: " + cellH + ":" + cell);
        attr.addAttribute(ATTR_CELL, cellH);
        DocUtil.startTag(wholeCell, attr, 0, vec);
        attr.removeAttribute(ATTR_CELL);
        attr = DocUtil.getDocStyle(doc, StyleType.inputCell);
        DocUtil.startTag(inputCellBox, attr, 0, vec);
        if (!CellUtils.isHTML(cell)) DocUtil.startTag(commonCell, attr, 0, vec);
        else
            DocUtil.startTag(htmlCell, attr, 0, vec);
        String text = CellUtils.getText(cell);
        if (!text.endsWith("\n")) text += "\n";
        // System.out.println("NDOC-createCell: " + text);
        DocUtil.addContent(text.toCharArray(), 0, vec, 0);
        DocUtil.endTag(vec);
        //attr.addAttribute(ATTR_CELL, cellH);
       // DocUtil.createCellHandle(attr, vec);
       // attr.removeAttribute(ATTR_CELL);
        DocUtil.endTag(vec);
//        boolean eval = CellUtils.isInitCell(cell);
//        HGHandle out = CellUtils.getOutCellHandle(cellH);
//        //System.out.println("createOutputCell: " + ((Cell)ThisNiche.hg.get(out)).getValue());
//        if (out != null && !eval) createOutputCell(doc, out, attr, vec);
//        else if (eval)
//        {
//            EvalResult res = eval_result(doc, cell);
//            createOutputCell(doc, CellUtils.createOutputCellH(cellH, res.getText(),
//                    res.getComponent()), attr, vec);
//        }
        DocUtil.endTag(vec);
        //if (gen_insP) createInsertionPoint(attr, vec);
        CellUtils.addEventPubSub(AttributeChangeEvent.HANDLE, cellH,
                doc.getHandle(), AttributeChangeHandler.getInstance());
        CellUtils.addMutualEventPubSub(CellTextChangeEvent.HANDLE, cellH,
                doc.getHandle(), CellTextChangeHandler.getInstance());
//        CellUtils.addMutualEventPubSub(EvalCellEvent.HANDLE, cellH,
//                doc.getHandle(), EvalCellHandler.getInstance());

    }
}
