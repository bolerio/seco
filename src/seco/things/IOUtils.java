package seco.things;

import java.awt.Component;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.hypergraphdb.HGHandle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import seco.ThisNiche;
import seco.notebook.NBStyle;
import seco.notebook.StyleType;
import seco.notebook.XMLConstants;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class IOUtils
{

    public static HGHandle importCellGroup(String filename)
    {
        try
        {
            FileReader in = new FileReader(filename);
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(new InputSource(in));
            HGHandle groupH = CellUtils.createGroupHandle(filename);
            loadTopGroup(doc.getDocumentElement(), groupH);
            return groupH;
        }
        catch (Throwable t)
        {
            throw new RuntimeException(t);
        }
    }

    static void loadTopGroup(Element top, HGHandle top_groupH)
    {
        CellGroup top_group = (CellGroup) ThisNiche.hg.get(top_groupH);
        if (!top.getTagName().equals(XMLConstants.NOTEBOOK))
            throw new RuntimeException("The document '" + top_group.getName()
                    + "' is not a notebook.");
        // title = top.getAttribute(XMLConstants.ATTR_TITLE);
        String engine = top.getAttribute(XMLConstants.ATTR_ENGINE);
        if (engine != null && engine.length() > 0)
            CellUtils.defaultEngineName = engine;
        NodeList children = top.getChildNodes();
        int style_num = 0;
        for (int i = 0; i < children.getLength(); i++)
        {
            Node n = children.item(i);
            if (!(n instanceof Element))
                continue;
            Element el = (Element) n;
            // we expect the styles to come first, after
            // populating the desired number, we stop looking for them
            // for performance reasons
            Map s = (Map) top_group.getAttribute(XMLConstants.CELL_STYLE);
            if (processStyleTag(el, top_group)
                    && ((s == null) || (s != null && style_num <= s.size())))
            {
                style_num++;
                continue;
            }
            if (el.getTagName().equals(XMLConstants.CELL_GROUP))
            {
                HGHandle group = loadGroup(el);
                top_group.insert(top_group.getArity(), group);
                continue;
            } else if (el.getTagName().equals(XMLConstants.CELL))
            {
                HGHandle group = loadCell(el);
                top_group.insert(top_group.getArity(), group);
                continue;
            }
            throw new RuntimeException("Unrecognized tag inside notebook: "
                    + el.getTagName());
        }
    }

    private static boolean processStyleTag(Element el, CellGroup top_group)
    {
        for (StyleType t : StyleType.values())
            if (t.tag_name.equals(el.getTagName()))
            {
                NBStyle s = new NBStyle(t);
                s.fromXMLElement(el);
                CellUtils.addStyle(top_group, s);
                return true;
            }
        return false;
    }

    static HGHandle loadGroup(Element el)
    {
        if (el == null || !el.getTagName().equals(XMLConstants.CELL_GROUP))
            throw new RuntimeException("Invalid or null element instead of: "
                    + XMLConstants.CELL_GROUP);
        String name = el.getAttribute(XMLConstants.ATTR_NAME);
        HGHandle top_groupH =
        (name != null && name.length() > 0) ? CellUtils
                .createGroupHandle(name) : CellUtils.createGroupHandle();
        CellGroup top_group = (CellGroup) ThisNiche.hg.get(top_groupH);
        String str_ind = el.getAttribute(XMLConstants.ATTR_INIT_CELL);
        if (str_ind != null && str_ind.length() > 0)
            CellUtils.setInitCell(top_group, Boolean.parseBoolean(str_ind));
        str_ind = el.getAttribute(XMLConstants.ATTR_COLLAPSED);
        if (str_ind != null && str_ind.length() > 0)
            CellUtils.setCollapsed(top_group, Boolean.parseBoolean(str_ind));
        str_ind = el.getAttribute(XMLConstants.ATTR_READONLY);
        // if (str_ind != null && str_ind.length() > 0)
        // readonly = Boolean.parseBoolean(str_ind);
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node n = children.item(i);
            if (!(n instanceof Element))
                continue;
            Element inner = (Element) n;
            if (inner.getTagName().equals(XMLConstants.CELL))
            {
                 top_group.insert(top_group.getArity(), loadCell(inner));
            } else if (inner.getTagName().equals(XMLConstants.CELL_GROUP))
            {
                 top_group.insert(top_group.getArity(),loadGroup(inner));
            } else
                throw new RuntimeException(
                        "Unrecognized tag inside inside cellGroup-group: "
                                + inner.getTagName());
        }
        return top_groupH;
    }

    static HGHandle loadCell(Element el)
    {
        if (el == null || !el.getTagName().equals(XMLConstants.CELL))
            throw new RuntimeException("Invalid or null element instead of: "
                    + XMLConstants.CELL);
        NodeList children = el.getChildNodes();
        boolean initCell = false;
        boolean isHTML = false;
        boolean readonly = false;
        boolean collapsed = false;
        String engine = null;
        String text = null;
        Element outputCellEl = null;
        String str_ind = el.getAttribute(XMLConstants.ATTR_INIT_CELL);
        if (str_ind != null && str_ind.length() > 0)
            initCell = Boolean.parseBoolean(str_ind);
        str_ind = el.getAttribute(XMLConstants.ATTR_HTML);
        if (str_ind != null && str_ind.length() > 0)
            isHTML = Boolean.parseBoolean(str_ind);
        str_ind = el.getAttribute(XMLConstants.ATTR_READONLY);
        if (str_ind != null && str_ind.length() > 0)
            readonly = Boolean.parseBoolean(str_ind);
        str_ind = el.getAttribute(XMLConstants.ATTR_COLLAPSED);
        if (str_ind != null && str_ind.length() > 0)
            collapsed = Boolean.parseBoolean(str_ind);
        str_ind = el.getAttribute(XMLConstants.ATTR_ENGINE);
        if (str_ind != null && str_ind.length() > 0)
            engine = str_ind;
        // System.out.println("Notebook.Cell: " + index +
        // ":" + initCell + ":" +
        // el.getAttribute(XMLConstants.ATTR_INIT_CELL));
        for (int i = 0; i < children.getLength(); i++)
        {
            Node n = children.item(i);
            if (!(n instanceof Element))
                continue;
            Element inner = (Element) n;
            if (inner.getTagName().equals(XMLConstants.OUTPUT_CELL))
            {
                outputCellEl = inner;
            } else if (inner.getTagName().equals(XMLConstants.CELL_DATA))
            {
                text = normalizeText(XMLConstants.concatContents(inner));
            } else
                throw new RuntimeException(
                        "Unrecognized tag inside inside cellGroup-group: "
                                + inner.getTagName());
        }
        HGHandle cellH = CellUtils.makeCellH(text, engine);
        Cell cell = (Cell) ThisNiche.hg.get(cellH);
        if (initCell)
            CellUtils.setInitCell(cell, initCell);
        if (isHTML)
            CellUtils.setHTML(cell, isHTML);
        if (readonly)
            CellUtils.setReadonly(cell, readonly);
        if (collapsed)
            CellUtils.setCollapsed(cell, collapsed);
        if (outputCellEl != null)
            loadOutputCell(outputCellEl, cellH);
        return cellH;
    }

    static String normalizeText(String t)
    {
        if (t == null)
            return null;
        t = t.trim();
        if (!t.endsWith("\n"))
            t += "\n";
        return t;
    }

    static String normalizeText(StringBuffer t)
    {
        if (t == null)
            return null;
        while ((t.length() > 0) && (t.charAt(0) <= ' '))
        {
            t.deleteCharAt(0);
        }
        while ((t.length() > 0) && (t.charAt(t.length() - 1) <= ' '))
        {
            t.deleteCharAt(t.length() - 1);
        }
        if ((t.length() > 0 && t.charAt(t.length() - 1) != '\n')
                || t.length() == 0)
            t.append("\n");
        return t.toString();
    }

    static Cell loadOutputCell(Element el, HGHandle par)
    {
        if (el == null || !el.getTagName().equals(XMLConstants.OUTPUT_CELL))
            throw new RuntimeException("Invalid or null element instead of: "
                    + XMLConstants.OUTPUT_CELL);
        boolean isError = false;
        boolean collapsed = false;
        Component component = null;
        String text = null;
        String err = el.getAttribute(XMLConstants.ATTR_ERROR);
        if (err != null && err.length() > 0)
            isError = "true".equals(err.trim());
        String str = el.getAttribute(XMLConstants.ATTR_COLLAPSED);
        if (str != null && str.length() > 0)
            collapsed = Boolean.parseBoolean(str);
        // Boolean.getBoolean(err) returns false?
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node n = children.item(i);
            if (!(n instanceof Element))
                continue;
            Element inner = (Element) n;
            if (inner.getTagName().equals(XMLConstants.OUTPUT_CELL_COMP))
            {
                component = readComponent(XMLConstants.concatContentsEx(inner));
            } else if (inner.getTagName().equals(XMLConstants.CELL_DATA))
            {
                text = normalizeText(XMLConstants.concatContents(inner));
                // System.out.println("Notebook - outputCell - len: " +
                // text.length());
            } else
                throw new RuntimeException(
                        "Unrecognized tag inside inside cellGroup-group: "
                                + inner.getTagName());
        }
        Cell cell = (Cell) ThisNiche.hg.get(
                CellUtils.createOutputCellH(par, text, component, isError));
        //if (isError)
        //    CellUtils.setError(cell, isError);
        if (collapsed)
            CellUtils.setCollapsed(cell, collapsed);
        return cell;
    }

    public static Component readComponent(String s)
    {
        try
        {
            final BASE64Decoder dec = new BASE64Decoder();
            final byte[] bytes = dec.decodeBuffer(s);
            ByteArrayInputStream bos = new ByteArrayInputStream(bytes);
            ObjectInputStream os = new ObjectInputStream(bos);
            return (Component) os.readObject();
        }
        catch (Exception ex)
        {
            // ex.printStackTrace();
            System.err.println("Can't read out component from: " + s);
        }
        return null;
    }

    public static String writeComponent(Component component)
    {
        try
        {
            component = cloneCellComp(component);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(component);
            final BASE64Encoder b64e = new BASE64Encoder();
            return new String(b64e.encodeBuffer(bos.toByteArray()));
        }
        catch (Exception ex)
        {
            System.err.println("Can't write out component: "
                    + component.getClass() + ". Reason: " + ex);
            // ex.printStackTrace();
        }
        return "";
    }

    private static String TEMP_FILE_NAME = "NotebookTempFileToBeDeleted.xml";
    // we need this hack, because of a bug(or misbehaviour) in
    // JTable.writeObject() which
    // calls installUI()??? and thus disturb the component currently in use in
    // Notebook
    // maybe similiar situation is present in other Swing components too, so ...
    private static ExceptionListener quietExcListener = new ExceptionListener() {
        public void exceptionThrown(Exception e)
        {
            // DO NOTHING:
        }
    };

    public static Component cloneCellComp(Component c) // throws IOException
    {
        try
        {
            XMLEncoder e = new XMLEncoder(new BufferedOutputStream(
                    new FileOutputStream(TEMP_FILE_NAME)));
            e.setExceptionListener(quietExcListener);
            e.writeObject(c);
            e.close();
            XMLDecoder d = new XMLDecoder(new BufferedInputStream(
                    new FileInputStream(TEMP_FILE_NAME)));
            Component result = (Component) d.readObject();
            d.close();
            File fn = new File(TEMP_FILE_NAME);
            fn.delete();
            return result;
        }
        catch (IOException ex)
        {
            return null;
        }
    }

    public static void exportCellGroup(CellGroup gr, String filename)
    {
        try
        {
            FileWriter fw = new FileWriter(filename);
            fw.write("<" + XMLConstants.NOTEBOOK);
            if (gr.getName() != null)
                fw.write(" " + XMLConstants.ATTR_TITLE + "=\"" + gr.getName()
                        + "\"");
            if (CellUtils.getEngine(gr) != null)
                fw.write(" " + XMLConstants.ATTR_ENGINE + "=\""
                        + CellUtils.getEngine(gr) + "\"");
            ;
            fw.write(">\n");
            Map<StyleType, NBStyle> map= (Map<StyleType, NBStyle>) gr
            .getAttribute(XMLConstants.CELL_STYLE);
            if(map != null)
              for(NBStyle s: map.values())
                 fw.write(s.toXML(1));
            for (int i = 0; i < gr.getArity(); i++)
            {
                CellGroupMember m = (CellGroupMember)
                    ThisNiche.hg.get(gr.getTargetAt(i));
                if (m instanceof CellGroup)
                    fw.write(writeGroup((CellGroup) m, 1));
                else
                    fw.write(writeCell((Cell) m, 1));
            }
            fw.write("</" + XMLConstants.NOTEBOOK + ">\n");
            fw.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static String writeGroup(CellGroup gr, int depth){
        String s =  XMLConstants.makeIndent(depth) + "<" + XMLConstants.CELL_GROUP;
        s += " " + XMLConstants.ATTR_NAME + "=\"" + gr.getName()+ "\"";
        if (CellUtils.isInitCell(gr))
           s += " " + XMLConstants.ATTR_INIT_CELL + "=\"true\"";
        if(CellUtils.isCollapsed(gr))
           s += " " + XMLConstants.ATTR_COLLAPSED + "=\"true\"";
        s += ">\n";
        for (int i = 0; i < gr.getArity(); i++)
        {
            CellGroupMember m = (CellGroupMember)
                ThisNiche.hg.get(gr.getTargetAt(i));
            s += (m instanceof CellGroup) ?
                writeGroup((CellGroup) m, depth + 1) : writeCell((Cell) m, depth + 1);
        }
        s += "\n";
        s += XMLConstants.makeIndent(depth) + "</" + XMLConstants.CELL_GROUP + ">\n";
        return s;
    }

    private static String writeCell(Cell gr, int depth)
    {
        //TODO: deal wit output cells
        if(!CellUtils.isInputCell(gr))return "";
        String s = XMLConstants.makeIndent(depth) + "<" + XMLConstants.CELL;
         if (CellUtils.isInitCell(gr))
            s += " " + XMLConstants.ATTR_INIT_CELL + "=\"true\"";
        if (CellUtils.isHTML(gr))
            s += " " + XMLConstants.ATTR_HTML + "=\"true\"";
        if(CellUtils.isReadonly(gr))
            s += " " + XMLConstants.ATTR_READONLY + "=\"true\"";
        if(CellUtils.isCollapsed(gr))
            s += " " + XMLConstants.ATTR_COLLAPSED + "=\"true\"";
       // if (engine != null)
            s += " " + XMLConstants.ATTR_ENGINE + "=\"" + CellUtils.getEngine(gr) + "\"";
        s += ">\n";
        String ind = XMLConstants.makeIndent(depth + 1);
        s += ind + "<" + XMLConstants.CELL_DATA + ">";
        s += XMLConstants.makeCDATA(CellUtils.getText(gr));
        s += ind + "</" + XMLConstants.CELL_DATA + ">\n";
        //TODO:
        //Cell outputCell = CellUtils.getOutCell(gr);
        //if (outputCell != null) s += writeOutputCell(outputCell, depth + 1);
        s += XMLConstants.makeIndent(depth) + "</" + XMLConstants.CELL + ">\n";
        return s;
    }
    
    private static String writeOutputCell(Cell gr, int depth)
    {
        String s = XMLConstants.makeIndent(depth) + "<" + XMLConstants.OUTPUT_CELL;
        if (CellUtils.isError(gr))
            s += " " + XMLConstants.ATTR_ERROR + "=\"true\"";
        if(CellUtils.isCollapsed(gr))
            s += " " + XMLConstants.ATTR_COLLAPSED + "=\"true\"";
        s += ">\n";
        String ind = XMLConstants.makeIndent(depth + 1);
        s += ind + "<" + XMLConstants.CELL_DATA + ">";
        s += XMLConstants.makeCDATA(CellUtils.getText(gr));
        s += ind + "</" + XMLConstants.CELL_DATA + ">\n";
        Object o = gr.getValue();
        if (o instanceof Component)
        {
            String c = writeComponent((Component) o);
            if(c != null && c.length() > 0)
            {
                s += ind + "<" + XMLConstants.OUTPUT_CELL_COMP + " ";
                // Dimension dim = component.getPreferredSize();
                // s += XMLConstants.ATTR_HEIGHT + "=\"" + dim.height + "\" ";
                // s += XMLConstants.ATTR_WIDTH + "=\"" + dim.width + "\" ";
                s += ">\n";
                s += c;
                s += ind + "</" + XMLConstants.OUTPUT_CELL_COMP + ">\n";
            }
        }
        s += XMLConstants.makeIndent(depth) + "</" + XMLConstants.OUTPUT_CELL + ">\n";
        return s;
    }
}
