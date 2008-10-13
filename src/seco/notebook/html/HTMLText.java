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

import javax.swing.text.BadLocationException;
import java.io.IOException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import java.io.StringWriter;
import java.util.Vector;
import javax.swing.text.AttributeSet;
import javax.swing.text.ElementIterator;
import javax.swing.text.Element;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Position;



/**
 * A class to represent a portion of HTML text.
 *
 * <p>In stage 9 copy and paste have been refined to correct bugs that
 * occurred when cut and paste was happening in nested paragraph elements</p>
 *
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the
 *      GNU General Public License,
 *      for details see file gpl.txt in the distribution
 *      package of this software
 *
 * @version stage 11, April 27, 2003
 */

public class HTMLText {

  /** the HTML representation of the text */
  private String htmlText;

  /** the plain text representation of the text */
  private String plainText;

  /** holds the copied plain text chunks */
  private Vector clipText = new Vector(0);

  /** holds the copied character attributes mapping to clipText */
  private Vector clipAttr = new Vector(0);

  /**
   * indicates whether or not the html represented by this
   * <code>HTMLText</code> instance contains more than one paragraph
   */
  private boolean multiPara = false;

  HTMLEditor editor;
  Position start;
  Position end;
  
  /**
   * 
   */
  public HTMLText(HTMLEditor src,  Position p0, Position p1) 
  throws BadLocationException, IOException
  {
	  start = p0;
	  end = p1;
	  editor = src;
	  copyHTML(src, start.getOffset(), end.getOffset() - start.getOffset());
  }

  /**
   * copy an HTML string representation of a content portion from the
   * given editor pane.
   *
   * @param  src  the <code>SHTMLEditorPane</code> to copy from
   * @param  start  the position to start copying at
   * @param  length  the length of the content portion to copy
   *
   * @return an HTML string representation of the copied portion of content
   *
   * @see com.lightdev.app.shtm.SHTMLEditorPane
   */
  public void copyHTML(HTMLEditor src, int start, int length)
        throws BadLocationException, IOException
  {
	  
    HTMLDocument doc = (HTMLDocument) src.getDocument();
    if( doc.getParagraphElement(start).equals(
        doc.getParagraphElement(start + length)))
    {
      multiPara = false;
      clearStyledText();
      copyStyledText(src);
    }
    else {
      multiPara = true;
      StringWriter sw = new StringWriter();
      MyHTMLWriter w = new MyHTMLWriter(sw, doc, start, length);
      Element first = doc.getParagraphElement(start);
      Element last = doc.getCharacterElement(start + length);
      w.write(first, last);
      htmlText = sw.getBuffer().toString();
      plainText = doc.getText(start, length);
    }
  }
  
    void removeText()
	{
		if ((start != null) && (end != null)
				&& (start.getOffset() != end.getOffset()))
		{
			try
			{
				Document doc = editor.getDocument();
				doc.remove(start.getOffset(), end.getOffset() - start.getOffset());
			}
			catch (BadLocationException e)
			{
			}
		}
	}

  /**
   * insert this <code>HTMLText<code> into a <code>Document</code>.
   *
   * @param  doc  the document to insert into
   * @param  pos  the text position to insert at
   */
  public void pasteHTML(Document doc, int pos)
        throws BadLocationException, IOException
  {
    /**
     * if only text within one paragraph is to be inserted,
     * iterate over copied text chunks and insert each
     * chunk with its own set of copied attributes. Else
     * simply read copied HTML code back in.
     */
    if(!multiPara) {
      int contentSize = getClipTextSize();
      String text;
      for(int i=0;i<contentSize;i++) {
        text = getCharactersAt(i);
        doc.insertString(pos, text, getCharacterAttributes(i));
        pos += text.length();
      }
    }
    else {
      MyHTMLDocument sDoc = (MyHTMLDocument) doc;
      Element cElem = sDoc.getCharacterElement(pos);
      Element pElem = cElem.getParentElement();
      if(pos == pElem.getStartOffset()) {
        // we are at the start of the paragraph to insert at
        sDoc.insertBeforeStart(pElem, htmlText);
      }
      else {
        if(pElem.getEndOffset() == pos + 1) {
          // we are at the end of the paragraph to insert at
          sDoc.insertAfterEnd(pElem, htmlText);
        }
        else {
          // we are somewhere else inside the paragraph to insert at
          String newHtml = splitPaste(sDoc, cElem, pElem, pos, htmlText);
          sDoc.setOuterHTML(pElem, newHtml);
        }
      }
    }
  }

  /**
   * paste HTML by splitting a given paragraph element and inserting
   * at the split position
   *
   * @param doc  the document to insert to
   * @param elem  the character element to split
   * @param pElem  the paragraph element to split
   * @param pos  the text position inside the document where to split
   * @param html  the html text to insert at pos
   */
  private String splitPaste(MyHTMLDocument doc, Element elem, Element pElem, int pos, String html) {
    StringWriter sw = new StringWriter();
    MyHTMLWriter w = new MyHTMLWriter(sw, doc);
    StringWriter splitSw = new StringWriter();
    MyHTMLWriter splitW = new MyHTMLWriter(splitSw, doc);
    try {
      int count = pElem.getElementCount();
      Element e;
      w.startTag(pElem.getName(), pElem.getAttributes());
      for(int i = 0; i < count; i++) {
        e = pElem.getElement(i);
        if(e.equals(elem)) {
          int start = e.getStartOffset();
          String textToSplit = doc.getText(start, e.getEndOffset() - start);
          splitW.write(e);
          String splitHtml = splitSw.getBuffer().toString();
          int splitStart = splitSw.getBuffer().toString().indexOf(textToSplit);
          int splitEnd = splitStart + pos - start;
          if(i > 0) {
            w.startTag(pElem.getName(), pElem.getAttributes());
          }
          sw.write(splitHtml.substring(0, splitEnd));
          w.endTag(pElem.getName());
          sw.write(html);
          w.startTag(pElem.getName(), pElem.getAttributes());
          sw.write(splitHtml.substring(splitEnd));
          if(i > 0) {
            w.endTag(pElem.getName());
          }
        }
        else {
          w.write(e);
        }
      }
      w.endTag(pElem.getName());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    return sw.getBuffer().toString();
  }

  /**
   * Copy the selected portion of an SHTMLEditorPane as styled text,
   * i.e. chunks of plain text strings with an AttributeSet associated
   * to each of them.
   *
   * @param src  the SHTMLEditorPane to copy from
   */
  private void copyStyledText(HTMLEditor src) throws BadLocationException {
    Document doc = src.getDocument();
    int selStart = src.getSelectionStart();
    int selEnd = src.getSelectionEnd();
    int eStart;
    int eEnd;
    ElementIterator eli = new ElementIterator(doc);
    Element elem = eli.first();
    while(elem != null) {
      eStart = elem.getStartOffset();
      eEnd = elem.getEndOffset();
      if(elem.getName().equalsIgnoreCase(AbstractDocument.ContentElementName)) {
        if((eEnd >= selStart) && (eStart <= selEnd)) {
          clipAttr.addElement(elem.getAttributes());
          if(eStart < selStart) {
            if(eEnd > selEnd) { // both ends of elem outside selection
              clipText.addElement(src.getText(selStart, selEnd - selStart));
            }
            else { // only first part of elem outside selection
              clipText.addElement(src.getText(selStart, eEnd - selStart));
            }
          }
          else if(eEnd > selEnd) { // only last part of elem outside selection
            clipText.addElement(src.getText(eStart, selEnd - eStart));
          }
          else { // whole element inside selection
            clipText.addElement(src.getText(eStart, eEnd - eStart));
          }
        }
      }
      elem = eli.next();
    }
  }

  /** get the number of text chunks in this <code>StyledText</code> object */
  private int getClipTextSize() {
    return clipText.size();
  }

  /**
   * get the attributes of a certain chunk of styled text
   *
   * @param chunkPos - the number of the chunk to get the attributes for
   * @return the attributes for respective character position
   */
  private AttributeSet getCharacterAttributes(int chunkNo) {
    return (AttributeSet) clipAttr.elementAt(chunkNo);
  }

  /**
   * get the characters of a certain chunk of styled text
   *
   * @param chunkNo - the number of the chunk to get the characters for
   * @return the characters for respective chunk as String
   */
  private String getCharactersAt(int chunkNo) {
    return (String) clipText.elementAt(chunkNo);
  }

  /** clear all styled text contents of this <code>HTMLText</code> object */
  private void clearStyledText() {
    clipText.clear();
    clipAttr.clear();
  }

  /**
   * get a String containing all chunks of text contained in this object
   *
   * @return string of all chunks in this object
   */
  public String toString() {
    StringBuffer text = new StringBuffer();
    if(multiPara) {
      text.append(plainText);
    }
    else {
      int i;
      for(i=0;i<clipText.size();i++) {
        text.append((String) clipText.elementAt(i));
      }
    }
    return text.toString();
  }

}
