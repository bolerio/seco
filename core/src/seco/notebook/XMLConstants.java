/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XMLConstants
{
	public static final String NOTEBOOK = "notebook";
	public static final String CELL_GROUP = "cell-group";
	public static final String CELL = "cell";
	public static final String CELL_DATA = "data";
	public static final String OUTPUT_CELL = "output-cell";
	public static final String OUTPUT_CELL_COMP = "component";
	public static final String ATTR_TITLE = "title";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_INIT_CELL = "initialization";
	public static final String ATTR_ERROR = "error";
	public static final String ATTR_ENGINE = "engine";
	public static final String ATTR_HTML = "html-preview";
	public static final String ATTR_READONLY = "readonly";
	public static final String ATTR_COLLAPSED = "collapsed";
	
	public static final String GLOBAL_STYLE = "global-style";
	public static final String CELL_STYLE = "cell-style";
	public static final String OUTPUT_CELL_STYLE = "output-cell-style";
	public static final String OUTPUT_ERROR_STYLE = "output-error-style";
	
	private XMLConstants()
	{
	}
	
	public static String concatContentsEx(Element node)
	{
		StringBuffer contents = new StringBuffer();
		NodeList name_content = node.getChildNodes();
		for (int j = 0; j < name_content.getLength(); j++)
		{
			Node content_item = name_content.item(j);
			if (content_item.getNodeType() == Node.TEXT_NODE ||
				content_item.getNodeType() == Node.CDATA_SECTION_NODE)
			{
				if (((Text)content_item).getData() != null)
					contents.append(((Text)content_item).getData());
			}
		}
		return contents.toString();
	}
	
	public static StringBuffer concatContents(Element node)
	{
		StringBuffer contents = new StringBuffer();
		NodeList name_content = node.getChildNodes();
		for (int j = 0; j < name_content.getLength(); j++)
		{
			Node content_item = name_content.item(j);
			if (content_item.getNodeType() == Node.TEXT_NODE ||
				content_item.getNodeType() == Node.CDATA_SECTION_NODE)
			{
				if (((Text)content_item).getData() != null)
					contents.append(((Text)content_item).getData());
			}
		}
		return contents;
	}
	
	 /**
     * Used for indentation in XML files.
     *
     * @param level The level of indentation. When level is 0, the returned String is empty
     * when it is 1 - the string contains 4 spaces, etc.. 
     * @return The indentation string.
     */
    public static String makeIndent(int level)
    {
        String result = "";
        for (int i = 0; i < level; i++)
            result += IND_STRING;
        return result;
    }
    private static final String IND_STRING = "    "; // 4 spaces
    
    public static String makeCDATA(String val)
    {
       return val = "<![CDATA[" + val + "]]>";
    }
}
