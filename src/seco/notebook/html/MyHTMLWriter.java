/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.html;

/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import java.io.*;
import javax.swing.text.*;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

/**
 * A writer for documents of application SimplyHTML.
 * 
 * <p>
 * <code>MyHTMLWriter</code> implements an own approach to clean writing of
 * HTML produced by application SimplyHTML. To keep it simple,
 * <code>MyHTMLWriter</code> only writes HTML and CSS content application
 * SimplyHTML 'understands'. Documents not produced by SimplyHTML might or might
 * not work with this writer.
 * </p>
 * 
 * By passing MODE_HTML, font-size attribute contents are written as is, with
 * MODE_JAVA, values of attribute font-size are adjusted to (wrong) sizes
 * expected in Java.
 * </p>
 * 
 * 
 * @todo add text wrap for content
 * 
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the GNU General Public
 * License, for details see file gpl.txt in the distribution package of this
 * software
 * 
 * @version stage 11, April 27, 2003
 */
public class MyHTMLWriter
{
	/** the document this writer is to generate HTML for */
	private HTMLDocument doc;
	/** the writer for HTML output */
	private Writer w;
	/**
	 * constant for new line character (handled differnetly on Windows,
	 * Unix/Linux and Mac)
	 */
	private String newline = System.getProperty("line.separator");
	/** start position in document for writing HTML */
	private int startOffset;
	/** end position in document for writing HTML */
	private int endOffset;
	/** indicates if the writer currently works on an implied tag */
	private boolean inImpliedTag = false;
	/** indicates if the writer currently works on a link */
	private boolean inLink = false;
	/** indicates if the wrtier currenty is inside of content */
	private boolean inContent = false;
	/**
	 * indicates if the writer has already worked on the first tag inside of any
	 * content
	 */
	private boolean firstContent = true;
	/**
	 * table of CSS attributes which shall not be written out when encountered
	 * inside a document.
	 */
	private Vector<CSS.Attribute> unwantedCssAttributes = new Vector<CSS.Attribute>();

	/**
	 * construct a new MyHTMLWriter to generate HTML for a given part of a
	 * document
	 * 
	 * @param w the writer for HTML output
	 * @param doc the document to generate HTML from
	 * @param pos the start position of the part of the document to generate
	 * HTML for
	 * @param len the length of the part of the document to generate HTML for
	 */
	public MyHTMLWriter(Writer w, HTMLDocument doc, int pos, int len)
	{
		this.doc = doc;
		this.w = w;
		this.startOffset = pos;
		this.endOffset = pos + len;
		initUnwantedCssAttributes();
	}

	/**
	 * construct a new MyHTMLWriter to generate HTML for a given document
	 * 
	 * @param w the writer for HTML output
	 * @param doc the document to generate HTML from
	 */
	public MyHTMLWriter(Writer w, HTMLDocument doc)
	{
		this(w, doc, 0, doc.getLength());
	}

	/**
	 * construct an uninitialized MyHTMLWriter
	 * 
	 * <p>
	 * This can be used for snythesizing HTML instead of reading it from the
	 * element structure of a document.
	 * </p>
	 * 
	 * <p>
	 * If a MyHTMLWriter was constructed with this constructor which shall be
	 * used for writing a document, the document and writeSegment properties of
	 * the MyHTMLWriter need to be set before calling its write() methods.
	 * </p>
	 * 
	 * @param w the writer to write to
	 */
	public MyHTMLWriter(Writer w)
	{
		this(w, null, 0, 0);
	}

	/**
	 * initialize the table of CSS attributes which shall not be written out
	 * when encountered inside a document.
	 */
	private void initUnwantedCssAttributes()
	{
		unwantedCssAttributes.addElement(CSS.Attribute.BORDER_TOP_WIDTH);
		unwantedCssAttributes.addElement(CSS.Attribute.BORDER_RIGHT_WIDTH);
		unwantedCssAttributes.addElement(CSS.Attribute.BORDER_BOTTOM_WIDTH);
		unwantedCssAttributes.addElement(CSS.Attribute.BORDER_LEFT_WIDTH);
		unwantedCssAttributes.addElement(CSS.Attribute.MARGIN_TOP);
		unwantedCssAttributes.addElement(CSS.Attribute.MARGIN_RIGHT);
		unwantedCssAttributes.addElement(CSS.Attribute.MARGIN_BOTTOM);
		unwantedCssAttributes.addElement(CSS.Attribute.MARGIN_LEFT);
		unwantedCssAttributes.addElement(CSS.Attribute.PADDING_TOP);
		unwantedCssAttributes.addElement(CSS.Attribute.PADDING_RIGHT);
		unwantedCssAttributes.addElement(CSS.Attribute.PADDING_BOTTOM);
		unwantedCssAttributes.addElement(CSS.Attribute.PADDING_LEFT);
	}

	/**
	 * set the document to write from
	 * 
	 * @param doc the document to write from
	 */
	public void setDocument(MyHTMLDocument doc)
	{
		this.doc = doc;
	}

	/**
	 * specify the text segment of the document to be written
	 * 
	 * @param pos the start position of the text segment
	 * @param len the length of the text segment
	 */
	public void setWriteSegment(int pos, int len)
	{
		this.startOffset = pos;
		this.endOffset = pos + len;
	}

	/**
	 * invoke HTML creation for the document or part of document given in the
	 * constructor of this MyHTMLWriter
	 * 
	 * @exception IOException on any i/o error
	 * @exception BadLocationException if text retrieval via doc.getText is
	 * passed an invalid location within the document
	 */
	public void write() throws IOException, BadLocationException
	{
		if ((doc != null) && (endOffset > 0))
		{
			if (startOffset == 0)
			{
				write(doc.getDefaultRootElement());
			} else
			{
				write(doc.getParagraphElement(startOffset).getParentElement());
			}
		} else
		{
			throw new RuntimeException("No Document");
		}
	}

	/**
	 * invoke HTML creation for a given element and all its children
	 * 
	 * <p>
	 * This iterates through the element structure below the given element by
	 * calling itself recursively for each branch element found.
	 * </p>
	 * 
	 * @param e the element to generate HTML for
	 * 
	 * @exception IOException on any i/o error
	 * @exception BadLocationException if text retrieval via doc.getText is
	 * passed an invalid location within the document
	 */
	public void write(Element e) throws IOException, BadLocationException
	{
		//System.out.println("write: " + e + ":" + e.isLeaf() + ":"
		//		+ e.getElementCount());
		if(specialImplied(e)) return;
		if (e.isLeaf())
		{
			inContent = true;
			leafTag(e);
			inContent = false;
		} else if(!inFakeHead(e))
		{
			startTag(e);
			for (int i = 0; i < e.getElementCount(); i++)
				write(e.getElement(i)); 
			endTag(e);
		}
	}
	
	private boolean specialImplied(Element el)throws IOException, BadLocationException
	{
		if(!isImpliedElement(el)||
				el.getElementCount() == 1) return false;
		Element par = el.getParentElement();
		if(par != null && par.getElementCount() == 1) return false;
		for (int i = 0; i < el.getElementCount(); i++)
			if(HTMLUtils.getName(el.getElement(i)) != HTML.Tag.CONTENT)
				return false;
		startTag("p", el.getAttributes());
		for (int i = 0; i < el.getElementCount(); i++)
			write(el.getElement(i)); 
		endTag("p");
		return true;
	}

	/**
	 * write an element and all its children. If a given element is reached,
	 * writing stops with this element. If the end element is a leaf, it is
	 * written as the last element, otherwise it is not written.
	 * 
	 * @param e the element to write including its children (if any)
	 * @param end the last leaf element to write or the branch element to stop
	 * writing at (whatever applies)
	 */
	public void writeElementsUntil(Element e, Element end) throws IOException,
			BadLocationException
	{
		if(specialImplied(e)) return;
		
		if (e.isLeaf())
		{
			inContent = true;
			leafTag(e);
			inContent = false;
		} else
		{
			if (e != end && !inFakeHead(e))
			{
				startTag(e);
				int childCount = e.getElementCount();
				int index = 0;
				while (index < childCount)
				{
					writeElementsUntil(e.getElement(index), end); 
					index++;
				}
				endTag(e);
			}
		}
	}

	/**
	 * write elements and their children starting at a given element until a
	 * given element is reached. The end element is written as the last element,
	 * if it is a leaf element.
	 * 
	 * @param start the element to start writing with
	 * @param end the last element to write
	 */
	public void write(Element start, Element end) throws IOException,
			BadLocationException
	{
		Element parent = start.getParentElement();
		int count = parent.getElementCount();
		int i = 0;
		Element e = parent.getElement(i);
		while (i < count && e != start)
		{
			e = parent.getElement(i++);
		}
		while (i < count)
		{
			writeElementsUntil(e, end);
			e = parent.getElement(i++);
		}
	}

	/**
	 * invoke HTML creation for all children of a given element.
	 * 
	 * @param elem the element which children are to be written as HTML
	 */
	public void writeChildElements(Element elem) throws IOException,
			BadLocationException
	{
		Element para;
		for (int i = 0; i < elem.getElementCount(); i++)
		{
			para = elem.getElement(i);
			System.out.println("writeChildElements: " + para + ":"
					+ para.isLeaf());
			write(para);
		}
	}
	/* HTML syntax constants */
	public static final char htmlTagStart = '<';
	public static final char htmlTagEnd = '>';
	public static final char htmlEndTagIndicator = '/';
	public static final char htmlAttributeSeparator = '=';
	public static final char htmlAttributeTerminator = ' ';
	public static final char htmlAttributeStartEnd = '\"';
	/* CSS syntax constants */
	public static final char cssAttributeTerminator = ';';
	public static final char cssAttributeSeparator = ':';

	/**
	 * write out a start tag including tag attributes for a given element
	 * 
	 * <p>
	 * This will not write start tags for implied elements. Indicator
	 * inImpliedTag is set instead to signal subsequent methods that we are
	 * inside an implied tag. Indicator inImpliedTag is cleared by method
	 * endTag.
	 * </p>
	 * 
	 * <p>
	 * CAUTION: This will not work for nested implied tags! Not sure yet, if
	 * these can happen, watch out accordingly.
	 * </p>
	 * 
	 * @param e the element to generate a start tag for
	 * 
	 * @exception IOException on any i/o error
	 */
	private void startTag(Element e) throws IOException
	{
		if (filter(e)) return;
		AttributeSet a = e.getAttributes();
		if (isImpliedElement(e))
		{
			inImpliedTag = true;
		} else
		{
			startTag(e.getName(), a);
		}
	}

	/**
	 * write out a start tag for a given tag name including tag attributes (if
	 * any)
	 * 
	 * @param name the name of the tag to generate
	 * @param a the set of attributes to generate in tag or null if none
	 * 
	 * @exception IOException on any i/o error
	 */
	public void startTag(String name, AttributeSet a) throws IOException
	{
		if (!name.equalsIgnoreCase(HTML.Tag.A.toString()))
		{
			w.write(newline);
			indent();
		}
		w.write(htmlTagStart);
		//if("p-implied".equals(name))
		//	name = "p";
		w.write(name);
		if (a != null)
		{
			writeTagAttributes(a, false);
		}
		w.write(htmlTagEnd);
		increaseIndent();
	}

	public void startTag(HTML.Tag name, AttributeSet a) throws IOException
	{
		startTag(name.toString(), a);
	}

	/**
	 * write a leaf tag
	 * 
	 * leaf tags can have different content in the element structure of a
	 * document
	 * 
	 * 1. 'normal' leaf: the element name is CONTENT.
	 * 
	 * a) element has no attributes: only the text from the document is written.
	 * 
	 * b) element has attributes: a SPAN start tag is synthesized holding the
	 * attributes, then the text from the document is written and a SPAN end tag
	 * is written finally.
	 * 
	 * c) the leaf is the CONTENT element of an implied paragraph: nothing is
	 * written.
	 * 
	 * 2. leaf holding a tag name for which no end tag is needed: the element
	 * name is other than CONTENT.
	 * 
	 * a start tag is generated for the element (endTag does not write an end
	 * tag for this startTag in this case).
	 * 
	 * @exception IOException on any i/o error
	 * @exception BadLocationException if text retrieval via doc.getText is
	 * passed an invalid location within the document.
	 */
	private void leafTag(Element e) throws IOException, BadLocationException
	{
		//System.out.println("MyHTMLWriter.leafTag e= " + e.getName() + 
		//		":" + e.getStartOffset() + ":" + e.getEndOffset());
		// get the element attributes
		AttributeSet a = e.getAttributes();
		// get value of name attribute and see if we are in content
		if (HTMLUtils.getName(e) == HTML.Tag.CONTENT)
		{
			// get start and end position of text to write
			int start = Math.max(startOffset, e.getStartOffset());
			int end = (endOffset != 0) ? Math.min(endOffset, e.getEndOffset()) : e.getEndOffset();
			//System.out.println("leafTag: " + start + ":" + end + "//" + startOffset + ":" + endOffset);
			if (end > start)
			{
				// element is inside the part of document to write, get the text
				String text = e.getDocument().getText(start, end - start);
				//System.out.println("MyHTMLWriter.leafTag text: " + text);
				if (text != null)
				{
					// we have text, write attributes, if any
					extractLink(a);
					if (writeTagAttributes(a, true))
					{
						// attributes written, write content text and...
						writeContentText(text);
						// ..write SPAN end tag
						decreaseIndent();
						w.write(htmlTagStart);
						w.write(htmlEndTagIndicator);
						w.write(HTML.Tag.SPAN.toString());
						w.write(htmlTagEnd);
						if (inLink)
						{
							// a link tag was found in attributes and written
							// out, write end tag for it
							decreaseIndent();
							endTag(HTML.Tag.A.toString());
							inLink = false;
						}
					} else
					{
						if(isCodeOrCite(e))
						{
							String t = getCodeOrCite(e);
							startTag(t , null);
							writeContentText(text);
							endTag(t);
							return;
						}
//						 no style attributes written, just write content text
						if(/*text.charAt(0) == '\n'*/!inLink && isImpliedInBody(e))
						{
							startTag(HTML.Tag.P, null);
							writeContentText(text);
							endTag(HTML.Tag.P);
							return;
						}
						writeContentText(text);
						if (inLink)
						{
							// a link tag was found in attributes and written
							// out, write end tag for it
							decreaseIndent();
							endTag(HTML.Tag.A.toString());
							inLink = false;
						}
					} // writeTagAttributes
				} // text != null
			} // end > start
		} // nameEquals content
		else
		{
			// not in content, write an implied startTag or endTag (if any)
			if (!isEndtag(a))
			{
				extractLink(a);
				startTag(e.getName(), a);
				decreaseIndent();
			} else
			{
				indent();
				endTag(e.getName());
			}
		}
	}
	
	//a <p-implied> with single leaf in <body> 
	private boolean isImpliedInBody(Element e){
		Element parent = e.getParentElement();
		Element body = parent.getParentElement();
		return isImpliedElement(parent) && parent.getElementCount() == 1 &&
		   body != null && HTMLUtils.getName(body) == HTML.Tag.BODY;
	}
	
	private boolean isCodeOrCite(Element e){
		AttributeSet a = e.getAttributes();
		return a.getAttribute(HTML.Tag.CITE) !=null || a.getAttribute(HTML.Tag.CODE)!=null;
	}
	
	private String getCodeOrCite(Element e){
		AttributeSet a = e.getAttributes();
		if(a.getAttribute(HTML.Tag.CITE) !=null)
			return "cite";
		if(a.getAttribute(HTML.Tag.CODE)!=null);
		  return "code";
	}

	/**
	 * test, if an attribute set contains another attribute set identifying a
	 * link if yes, write a link tag with respective attributes
	 * 
	 * @param a the attribute set to look for a link attribute set
	 * 
	 * @return true, when a link was written, false, if not
	 */
	private boolean extractLink(AttributeSet a) throws IOException
	{
		Object key = a.getAttribute(HTML.Tag.A);
		if (key != null)
		{
			AttributeSet linkAttrs = (AttributeSet) key;
			startTag(HTML.Tag.A, linkAttrs);
			inLink = true;
		}
		return inLink;
	}

	/**
	 * determine by a given set of attributes whether or not the tag the
	 * attributes belong to is an implied endtag.
	 */
	private boolean isEndtag(AttributeSet a)
	{
		boolean hasEndtag = false;
		if (a != null)
		{
			Object endtag = a.getAttribute(HTML.Attribute.ENDTAG);
			if (endtag != null)
			{
				hasEndtag = true;
			}
		}
		return hasEndtag;
	}

	/**
	 * write out an end tag for a given element
	 * 
	 * <p>
	 * This will not write an end tag if we are inside an implied tag. Indicator
	 * inImpliedTag will be cleared in this case instead.
	 * </p>
	 * 
	 * <p>
	 * CAUTION: This will not work for nested implied tags! Not sure yet, if
	 * these can happen, watch out accordingly.
	 * </p>
	 * 
	 * @param e the element to generate an end tag for
	 * 
	 * @exception IOException on any i/o error
	 */
	private void endTag(Element e) throws IOException
	{
		if (filter(e)) return;
		if (inImpliedTag)
		{
			inImpliedTag = false;
		} else
		{
			decreaseIndent();
			indent();
			endTag(e.getName());
		}
	}

	public void endTag(HTML.Tag tag) throws IOException
	{
		endTag(tag.toString());
	}

	/**
	 * write out an end tag for a given tag name
	 * 
	 * @param name the name of the tag to generate an end tag for
	 */
	public void endTag(String name) throws IOException
	{
		if (!inLink)
		{
			w.write(newline);
			indent();
		}
		w.write(htmlTagStart);
		w.write(htmlEndTagIndicator);
		//if("p-implied".equals(name))
		//	name = "p";
		w.write(name);
		w.write(htmlTagEnd);
	}

	/**
	 * write attributes of a tag
	 * 
	 * this first collects all attributes and generates HTML or CSS code
	 * according to the type of attribute.
	 * 
	 * Single members of CSS shorthand properties are filtered out using method
	 * filterAttributes and Vector unwantedCssAttributes. For these attributes
	 * CSS shorthand properties are generated instead.
	 * 
	 * It then writes out the attributes according to their type:
	 * 
	 * a) if HTML attributes were generated, they are written as key/value pairs
	 * in HTML syntax following the HTML tag name.
	 * 
	 * b) if CSS attributes were generated, they are written as key/value pairs
	 * in CSS syntax inside a STYLE attribute follwing the HTML tag name or a)
	 * above, whatever applies.
	 * 
	 * If attributes were generated for a CONTENT tag, a) and b) above are
	 * generated inside a synthesized SPAN start tag. Method leafTag generates
	 * the SPAN end tag after writing the content accordingly in this case.
	 * 
	 * In stage 9 link handling is added: Links (tag A) are attributes in Java
	 * instead of tags.
	 * 
	 * @param a set of attributes to write
	 * @param inContent true if we are in content (SPAN), false if not
	 * 
	 * @exception IOException on any i/o error
	 */
	private boolean writeTagAttributes(AttributeSet a, boolean inContent)
			throws IOException
	{
		boolean wroteAttributes = false;
		a = new AttributeMapper(a).getMappedAttributes(AttributeMapper.toHTML);
		StringBuffer style = new StringBuffer();
		StringBuffer html = new StringBuffer();
		splitToAttrBuffers(a, html, style);
		writeShortHandProperty(style, CSS.Attribute.BORDER_WIDTH, a);
		writeShortHandProperty(style, CSS.Attribute.MARGIN, a);
		writeShortHandProperty(style, CSS.Attribute.PADDING, a);
		//System.out.println("writeTagAttributes " + style.length() + ":"
		//		+ html.length());
		if (((html.length() > 0) || (style.length() > 0)) && (inContent))
		{
			if (!inLink && firstContent)
			{
				w.write(newline);
				indent();
				firstContent = false;
			}
			indent();
			w.write(htmlTagStart);
			w.write(HTML.Tag.SPAN.toString());
		}
		if (html.length() > 0)
		{
			w.write(html.toString());
			wroteAttributes = true;
		}
		if (style.length() > 0)
		{
			w.write(htmlAttributeTerminator);
			w.write(HTML.Attribute.STYLE.toString());
			w.write(htmlAttributeSeparator);
			w.write(htmlAttributeStartEnd);
			w.write(style.toString());
			w.write(htmlAttributeStartEnd);
			wroteAttributes = true;
		}
		if (((html.length() > 0) || (style.length() > 0)) && (inContent))
		{
			w.write(htmlTagEnd);
			increaseIndent();
		}
		return wroteAttributes;
	}

	/**
	 * split an attribute set into CSS and HTML attributes
	 */
	private void splitToAttrBuffers(AttributeSet a, StringBuffer html,
			StringBuffer style) throws IOException
	{
		Enumeration keys = a.getAttributeNames();
		while (keys.hasMoreElements())
		{
			Object key = keys.nextElement();
			if (key != null)
			{
				Object val = a.getAttribute(key);
				if (val != null)
				{
					if (key instanceof CSS.Attribute)
					{
						filterCSSAttributes(style, key, val);
					} else if (key instanceof HTML.Attribute)
					{
						html.append(htmlAttributeTerminator);
						html.append(key.toString());
						html.append(htmlAttributeSeparator);
						html.append(htmlAttributeStartEnd);
						html.append(val.toString());
						html.append(htmlAttributeStartEnd);
					}
				}
			}
		}
	}

	/**
	 * write out a given CSS shorthand property to a given output buffer
	 * 
	 * <p>
	 * This only writes a CSS shorthand property if the given set of attributes
	 * contains members for the given CSS shorthand property
	 * </p>
	 * 
	 * @param buf the output buffer to write to
	 * @param key the CSS shorthand property key to write
	 * @param a the set of attributes to get the members for the given CSS
	 * shorthand property
	 */
	private void writeShortHandProperty(StringBuffer buf, Object key,
			AttributeSet a)
	{
		CombinedAttribute ca = new CombinedAttribute(key, a, false);
		if (!ca.isEmpty())
		{
			writeCssAttribute(buf, key, ca.getAttribute());
		}
	}

	/**
	 * only add CSS attributes to a given output buffer if they are not part of
	 * the table of CSS attributes which shall not be written out when
	 * encountered inside a document.
	 * 
	 * @param buf the output buffer to write attributes to
	 * @param key the attribute key to filter
	 * @param val the attribute value to write
	 */
	private void filterCSSAttributes(StringBuffer buf, Object key, Object val)
	{
		if (!isUnwanted(key))
		{
			if (isFontSizeAttr(key))
			{
				Object newVal = getFontSizeValue(val);
				//System.out.println("getFontValue: " + val + ":" + newVal);
				writeCssAttribute(buf, key, newVal);
			} else
			{
				writeCssAttribute(buf, key, val);
			}
		}
	}

	private boolean isFontSizeAttr(Object key)
	{
		return CSS.Attribute.FONT_SIZE == key;
	}

	private Object getFontSizeValue(Object val)
	{
		int i = -1;
		i = Integer.parseInt(val.toString());
		if (i > 0 && i <= fontSizes.length) 
			return new Integer(fontSizes[i - 1]);
		return val;
	}
	static int fontSizes[] = { 8, 10, 12, 14, 18, 24, 36 };

	/**
	 * write a given CSS attribute to a given output buffer
	 * 
	 * @param buf the output buffer to write to
	 * @param key the CSS attribute key to write
	 * @param val the value of the attribute to write
	 */
	private void writeCssAttribute(StringBuffer buf, Object key, Object val)
	{
		if(val == null || val.toString().length() == 0) return;
		
		if (buf.length() > 0)
			buf.append(htmlAttributeTerminator);
		
		buf.append(key.toString());
		buf.append(cssAttributeSeparator);
		buf.append(val.toString());
		buf.append(cssAttributeTerminator);
	}

	/**
	 * find out whether or not a given attribute key is part of the table of CSS
	 * attributes which shall not be written out.
	 * 
	 * @param key the attribute key to check
	 * 
	 * @return true if the attribute key is unwanted, false if not
	 */
	private boolean isUnwanted(Object key)
	{
		boolean unwanted = false;
		int i = 0;
		int count = unwantedCssAttributes.size();
		while (!unwanted && i < count)
		{
			unwanted = key.equals(unwantedCssAttributes.elementAt(i));
			i++;
		}
		return unwanted;
	}

	/* -------- tag writing end ------------------- */
	/* -------- tag writing help methods start ------------------- */
	/**
	 * write the text found as content and append a new line if none is found in
	 * the content text.
	 * 
	 * @param text the content text to write
	 */
	private void writeContentText(String text) throws IOException
	{
		//System.out.println("writeContentText: " + text + ":" + text.length());
		transformSpecialChars(text.toCharArray());
	}

	/**
	 * transform special characters in content text by replacing certain
	 * characters with their HTML equivalents before actually writing them out.
	 * 
	 * @param text the uncleaned content text to write
	 * 
	 * @exception IOException on any i/o error
	 */
	private void transformSpecialChars(char[] text) throws IOException
	{
		int last = 0;
		int length = text.length;
		for (int i = 0; i < text.length; i++)
		{
			switch (text[i])
			{
			case '<':
				if (i > last)
				{
					w.write(text, last, i - last);
				}
				last = i + 1;
				w.write("&lt;");
				break;
			case '>':
				if (i > last)
				{
					w.write(text, last, i - last);
				}
				last = i + 1;
				w.write("&gt;");
				break;
			case '&':
				if (i > last)
				{
					w.write(text, last, i - last);
				}
				last = i + 1;
				w.write("&amp;");
				break;
			case '"':
				if (i > last)
				{
					w.write(text, last, i - last);
				}
				last = i + 1;
				w.write("&quot;");
				break;
			case '\n':
				if (i > last)
				{
					w.write(text, last, i - last);
				}
				last = i + 1;
				break;
			case '\t':
			case '\r':
				break;
			default:
				if (text[i] < ' ' || text[i] > 127)
				{
					if (i > last)
					{
						w.write(text, last, i - last);
					}
					last = i + 1;
					w.write("&#");
					w.write(String.valueOf((int) text[i]));
					w.write(";");
				}
				break;
			}
		}
		if (last < length)
		{
			w.write(text, last, length - last);
		}
	}
	private char[] indentChars;
	private int indentLevel = 0;
	private int indentSpace = 2;

	/**
	 * decrement the indent level.
	 */
	private void decreaseIndent()
	{
		--indentLevel;
	}

	/**
	 * increment the indent level.
	 */
	private void increaseIndent()
	{
		++indentLevel;
	}

	/**
	 * write spaces according to the current indentLevel.
	 * 
	 * The number of spaces written is determined by the amount of spaces per
	 * indent step multiplied by the level of indentation.
	 * 
	 * Indent spaces are generated in a char[] as needed.
	 * 
	 * @exception IOException on any i/o error
	 */
	private void indent() throws IOException
	{
		if (!inContent || firstContent)
		{
			int max = indentLevel * indentSpace;
			if (indentChars == null || max > indentChars.length)
			{
				indentChars = new char[max];
				for (int i = 0; i < max; i++)
				{
					indentChars[i] = ' ';
				}
			}
			w.write(indentChars, 0, max /* Math.max(0, max) */);
		}
	}

	boolean filter(Element elem)
	{
		if (isImpliedElement(elem)) return true;
		HTML.Tag name = HTMLUtils.getName(elem);
		return (elem.getDocument().getProperty(name) != null);
	}
	
	private boolean inFakeHead(Element elem){
		HTML.Tag name = HTMLUtils.getName(elem);
		return (name == HTML.Tag.HEAD &&
				(elem.getDocument().getProperty(name) != null));
	}

	protected boolean isImpliedElement(Element elem)
	{
		return (HTMLUtils.getName(elem) == HTML.Tag.IMPLIED);
	}
}
