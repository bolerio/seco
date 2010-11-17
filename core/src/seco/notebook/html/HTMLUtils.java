/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.html;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import seco.util.IconManager;

public class HTMLUtils
{
	public static ImageIcon resolveIcon(String img)
	{
		return IconManager.resolveIcon(img);
	}

	static void removeTag(HTMLEditor parent, Element element)
			throws BadLocationException
	{
		if (element == null) return;
		parent.getDoc().remove(element.getStartOffset(),
				element.getEndOffset() - element.getStartOffset());
	}

	// Generate <tag attr=el_attribs>element text</tag>
	static String putInHTML(StyledDocument doc, Element el, HTML.Tag tag)
			throws IOException, BadLocationException
	{
		String text = doc.getText(el.getStartOffset(), el.getEndOffset()
				- el.getStartOffset());
		StringWriter sw = new StringWriter();
		MyHTMLWriter w = new MyHTMLWriter(sw);
		MutableAttributeSet set = new SimpleAttributeSet(doc.getCharacterElement(el.getStartOffset())
				.getAttributes());
		AttributeSet el_set = el.getAttributes();
		Enumeration en = el_set.getAttributeNames();
		while(en.hasMoreElements()){
			Object key = en.nextElement();
			if(key instanceof HTML.Tag) continue;
			set.addAttribute(key, el_set.getAttribute(key));
		}
			
		w.startTag(tag, set);
		sw.write(text);
		w.endTag(tag);
		return sw.getBuffer().toString();
	}

	static String putInHTML(String text, AttributeSet set, HTML.Tag tag)
			throws IOException, BadLocationException
	{
		StringWriter sw = new StringWriter();
		MyHTMLWriter w = new MyHTMLWriter(sw);
		w.startTag(tag, set);
		sw.write(text);
		w.endTag(tag);
		return sw.getBuffer().toString();
	}

	public static Element getListTag(HTMLEditor editor)
	{
		int offset = editor.getCaretPosition();
		Element el = HTMLUtils.getTag(editor, HTML.Tag.OL, offset);
		if (el == null) el = HTMLUtils.getTag(editor, HTML.Tag.UL, offset);
		return el;
	}

	public static Element getTag(HTMLEditor editor, HTML.Tag tag)
	{
		return getTag(editor, tag, editor.getCaretPosition());
	}

	public static Element getTag(HTMLEditor editor, HTML.Tag tag, int offset)
	{
		Element e = editor.getDoc().getParagraphElement(offset);
		if (getName(e) == tag) return e;
		do
		{
			e = e.getParentElement();
			if (getName(e) == tag) return e;
		} while (e != null && getName(e) != HTML.Tag.HTML);
		return null;
	}

	// returns top enclosing tag of a given type
	public static Element getTopTag(HTMLEditor editor, HTML.Tag tag, int offset)
	{
		Element e = getTag(editor, tag, offset);
		if (e == null) return null;
		Element found = null;
		for (Element up = e.getParentElement(); up != null
				&& getName(e) != HTML.Tag.HTML; up = up.getParentElement())
		{
			if (getName(up) == tag)
				found = up;
			else if (found != null) return found;
		}
		// System.out.println(" getTopTag: " + e + ":" + found);
		return (found != null) ? found : e;
	}

	public static boolean isInTag(HTMLEditor editor, HTML.Tag tag)
	{
		return getTag(editor, tag, editor.getCaretPosition()) != null;
	}

	static HTML.Tag getName(Element e)
	{
		return (e == null) ? null : (HTML.Tag) e.getAttributes().getAttribute(
				StyleConstants.NameAttribute);
	}

	static boolean isNBSP(char[] temp)
	{
		return temp.length == 2 && temp[0] == 160 && temp[1] == 10;
	}

	static String getContent(Element el) throws BadLocationException
	{
		Document doc = el.getDocument();
		return doc.getText(el.getStartOffset(), el.getEndOffset()
				- el.getStartOffset());
	}

	static void listOff(HTMLEditor editor, HTML.Tag tag)
	{
		int start = editor.getSelectionStart();
		int end = editor.getSelectionEnd();
		Element list = getTag(editor, tag, start);
		Element li = getTag(editor, HTML.Tag.LI, start);
		if (list == null || li == null) return;
		int index = list.getElementIndex(li.getStartOffset());
		try
		{
			StringWriter first = new StringWriter();
			MyHTMLWriter w = new MyHTMLWriter(first);
			for (int i = 0; i < index; i++)
				w.write(list.getElement(i));
			String nolist = "";
			// String next = null;
			StringWriter next = new StringWriter();
			MyHTMLWriter nw = new MyHTMLWriter(next);
			int nolistLen = 0;
			if (index < list.getElementCount())
			{
				boolean selection = true;
				while (selection)
				{
					Element elem = list.getElement(index);
					nolistLen += elem.getEndOffset() - elem.getStartOffset();
					nolist += putInHTML(editor.getDoc(), elem, HTML.Tag.P);
					selection = elem.getEndOffset() < end
							&& list.getElementCount() > index + 1;
					if (selection) index++;
				}
				if (index + 1 < list.getElementCount())
				{
					nw.startTag(tag, list.getAttributes());
					for (int i = index + 1; i < list.getElementCount(); i++)
						nw.write(list.getElement(i));
					nw.endTag(tag.toString());
				}
			}
			// System.out.println("First: " + first);
			//System.out.println("NoList: " + nolist);
			// System.out.println("Next: " + next);
			Element td = getTag(editor, HTML.Tag.TD, li.getStartOffset());
			// System.out.println("TD: " + td);
			if (first.getBuffer().length() > 0)
				editor.getDoc()
						.setInnerHTML(list, first.getBuffer().toString());
			int off = list.getEndOffset();
			if (nolist != null && nolist.length() > 0)
			{
				if (td == null)
				{
					HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
					kit.read(new StringReader(nolist), editor.getDoc(), off);
					if (next != null)
						kit.read(new StringReader(next.getBuffer().toString()),
								editor.getDoc(), off + nolistLen);
					editor.setCaretPosition(off + nolistLen);
				} else
				{
					// System.out.println("Td: " + td.getEndOffset() + ":" +
					// "list:" + off);
					if (td.getEndOffset() == off)
					{
						editor.getDoc().insertBeforeEnd(td, nolist);
						if (next.getBuffer().length() > 0)
							editor.getDoc().insertBeforeEnd(td,
									next.getBuffer().toString());
					} else
					{
						editor.getDoc().insertAfterEnd(list, nolist);
						if (next.getBuffer().length() > 0)
						{
							Element el = editor.getDoc().getParagraphElement(
									off + nolistLen - 1);
							editor.getDoc().insertAfterEnd(el,
									next.getBuffer().toString());
						}
					}
				}
			}
			if (first.getBuffer().length() == 0)
				editor.getDoc().remove(list.getStartOffset(),
						list.getEndOffset() - list.getStartOffset());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void listOn(final HTMLEditor editor, HTML.Tag tag) throws IOException,
			BadLocationException
	{
		String sel = editor.getSelectedText();
		final int iStart = editor.getSelectionStart();
		int iEnd = editor.getSelectionEnd();
		StringBuffer sbNew = new StringBuffer();
		sbNew.append("<" + tag + ">");
		if (iStart != iEnd)
		{
			String sToken = ((sel.indexOf("\r") > -1) ? "\r" : "\n");
			StringTokenizer stTokenizer = new StringTokenizer(sel, sToken);
			int pos = iStart;
			while (stTokenizer.hasMoreTokens())
			{
				//sbNew.append("<li>");
				//sbNew.append(stTokenizer.nextToken());
				//sbNew.append("</li>");
				String text = stTokenizer.nextToken();
				AttributeSet set = editor.getDoc().getCharacterElement(pos).getAttributes();
				sbNew.append(putInHTML(text, set, HTML.Tag.LI));
				pos += text.length();
			}
		}else
			sbNew.append("<li> </li>");
			
		sbNew.append("</" + tag + ">"); // <p>&nbsp;</p>");
		Element td = HTMLUtils.getTag(editor, HTML.Tag.TD, editor
				.getCaretPosition());
		// System.out.println("insertList: " + td + ":" + sbNew.toString());
		if (td != null)
		{
			editor.getDoc().setInnerHTML(td, sbNew.toString());
		} else
		{
			editor.getDoc().remove(iStart, iEnd - iStart);
			editor.getMyEditorKit().insertHTML(editor.getDoc(), iStart,
					sbNew.toString(), 1, 1, null);
		}
		if(iStart == iEnd)
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					editor.setCaretPosition(iStart + 1);
				}
			});
	}

	static void setCaretPosInAWT(final HTMLEditor editor, final int pos){
		
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				editor.setCaretPosition(pos);
			}
		});
	}
}
