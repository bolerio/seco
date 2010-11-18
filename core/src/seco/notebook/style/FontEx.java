/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.style;

import java.awt.Font;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;

import seco.notebook.html.Util;


public class FontEx extends Font
{
	protected boolean isUnderline;
	protected boolean isStrikethrough;
	protected boolean isSubscript;
	protected boolean isSuperscript;
	
	public FontEx(Map<? extends Attribute, ?> arg0)
	{
		super(arg0);
	}

	public FontEx(String name, int style, int size)
	{
		super(name, style, size);
	}

	public boolean isStrikethrough()
	{
		return isStrikethrough;
	}

	public void setStrikethrough(boolean isStrikethrough)
	{
		this.isStrikethrough = isStrikethrough;
	}

	public boolean isSubscript()
	{
		return isSubscript;
	}

	public void setSubscript(boolean isSubscript)
	{
		this.isSubscript = isSubscript;
	}

	public boolean isSuperscript()
	{
		return isSuperscript;
	}

	public void setSuperscript(boolean isSuperscript)
	{
		this.isSuperscript = isSuperscript;
	}

	public boolean isUnderline()
	{
		return isUnderline;
	}

	public void setUnderline(boolean isUnderline)
	{
		this.isUnderline = isUnderline;
	}
	
	public void populateStyle(MutableAttributeSet doc_style)
	{
		StyleConstants.setFontFamily(doc_style, getFamily());
		//System.out.println("Font Size: " + getSize());
		StyleConstants.setFontSize(doc_style, getSize());
		StyleConstants.setItalic(doc_style, isItalic());
		StyleConstants.setBold(doc_style, isBold());
		StyleConstants.setUnderline(doc_style, isUnderline());
		StyleConstants.setStrikeThrough(doc_style, isStrikethrough());
		StyleConstants.setSubscript(doc_style, isSubscript());
		StyleConstants.setSuperscript(doc_style, isSuperscript());
	}
	
	public void populateCSSStyle(MutableAttributeSet doc_style)
	{
		if(!"Serif".equals(getFamily()))
		  Util.styleSheet().addCSSAttribute(doc_style,
	            CSS.Attribute.FONT_FAMILY,getFamily());
		Util.styleSheet().addCSSAttribute(doc_style, CSS.Attribute.FONT_SIZE,
	            "" + getSize());
		if(isItalic())
			Util.styleSheet().addCSSAttribute(doc_style, CSS.Attribute.FONT_STYLE,
		              StyleConstants.Italic.toString());
		else
			Util.styleSheet().addCSSAttribute(doc_style, CSS.Attribute.FONT_STYLE,
					Util.CSS_ATTRIBUTE_NORMAL);
		if(isBold())
			Util.styleSheet().addCSSAttribute(doc_style, CSS.Attribute.FONT_WEIGHT,
		            StyleConstants.Bold.toString());
		
		StyleConstants.setUnderline(doc_style, isUnderline());
		StyleConstants.setStrikeThrough(doc_style, isStrikethrough());
		StyleConstants.setSubscript(doc_style, isSubscript());
		StyleConstants.setSuperscript(doc_style, isSuperscript());
	}
	   
	
	public static FontEx fromAttribs(AttributeSet attr) 
	{
		String name = StyleConstants.getFontFamily(attr);
		int size = StyleConstants.getFontSize(attr);
		FontEx f = new FontEx(name, Font.PLAIN, size);
		if (StyleConstants.isBold(attr)) 
 	        f.style |= Font.BOLD;
 	     else if (StyleConstants.isItalic(attr)) 
 	    	f.style |= Font.ITALIC;
 	     else if(StyleConstants.isUnderline(attr))
 	    	f.isUnderline = true;
    	 else if(StyleConstants.isStrikeThrough(attr))
 			f.isStrikethrough = true;
    	 else if(StyleConstants.isSubscript(attr))
 			f.isSubscript = true;
    	 else if(StyleConstants.isSuperscript(attr))
 			f.isSuperscript = true;
		return f;
	}
	
	public static FontEx fromCSSAttribs(AttributeSet attr) 
	{
		String name = "Serif";
		if(attr.isDefined(CSS.Attribute.FONT_FAMILY)) 
			name  = attr.getAttribute(CSS.Attribute.FONT_FAMILY).toString();
		
		int size = 12;
		if(attr.isDefined(CSS.Attribute.FONT_SIZE))
			size = (int) Util.getAttrValue(attr.getAttribute(CSS.Attribute.FONT_SIZE));
		
		FontEx f = new FontEx(name, Font.PLAIN, size);
		if (StyleConstants.isBold(attr)) 
 	        f.style |= Font.BOLD;
 	     else if (StyleConstants.isItalic(attr)) 
 	    	f.style |= Font.ITALIC;
 	     else if(StyleConstants.isUnderline(attr))
 	    	f.isUnderline = true;
    	 else if(StyleConstants.isStrikeThrough(attr))
 			f.isStrikethrough = true;
    	 else if(StyleConstants.isSubscript(attr))
 			f.isSubscript = true;
    	 else if(StyleConstants.isSuperscript(attr))
 			f.isSuperscript = true;
		
		if(attr.isDefined(CSS.Attribute.FONT_STYLE))
		{
			String b = attr.getAttribute(CSS.Attribute.FONT_STYLE).toString();
			if(b.equalsIgnoreCase(StyleConstants.Italic.toString()))
			   f.style |= Font.ITALIC;
		}
		
		if(attr.isDefined(CSS.Attribute.FONT_WEIGHT))
		{
			String b = attr.getAttribute(CSS.Attribute.FONT_WEIGHT).toString();
			if(b.equalsIgnoreCase(StyleConstants.Bold.toString()))
					f.style |= Font.BOLD;
		} 
		
		if(attr.isDefined(CSS.Attribute.TEXT_DECORATION))
		{
			String b = attr.getAttribute(CSS.Attribute.TEXT_DECORATION).toString();
			if(b.equalsIgnoreCase("line-through"))
				f.isStrikethrough = true;
			if(b.equalsIgnoreCase("underline"))
				f.isUnderline = true;
		}
		
		if(attr.isDefined(CSS.Attribute.VERTICAL_ALIGN))
		{
			String b = attr.getAttribute(CSS.Attribute.VERTICAL_ALIGN).toString();
			if(b.equalsIgnoreCase("super"))
			 	f.isSuperscript = true;
			if(b.equalsIgnoreCase("sub"))
				f.isSubscript = true;
		} 
		
		return f;
	}
	
	public String toString() {
	    String styleString = "plain";
	    if (style == Font.BOLD) 
	        styleString = "bold";
	    else if (style == Font.ITALIC) 
	        styleString = "italic";
	    else if ( style == (Font.BOLD|Font.ITALIC)) 
	        styleString = "bold|italic";
	    
	    if(isUnderline())
	    	styleString += "|underline";
		if(isStrikethrough())
			styleString += "|strikethrough";
		if(isSubscript())
			styleString += "|subscript";
		if(isSuperscript())
			styleString += "|superscript";
	    	    
	    return getName() + "," + styleString + "," + size;
	}

	public static FontEx fromString(String value) {
	    if (value == null) {return null;}
	    //find index of first comma character
	    int comma1 = value.indexOf(",");
	    //return null if not found, or found at beginning or end of string
	    if (comma1 < 1 || comma1 >= value.length()-1) {return null;}
	    //find the second comma character
	    int comma2 = value.indexOf(",", comma1+1);
	    //return null if not found, or found immediately after the first
	    //comma, or at end of string
	    if (comma2 == -1 || comma2 == comma1+1 ||
	    comma2 >= value.length()-1) {return null;}
	    
	    //extract the fields
	    String name = value.substring(0,comma1);
	    String typeString = value.substring(comma1+1,comma2);
	    String sizeString = value.substring(comma2+1,value.length());
	    
	    int size = 0;
	    try {
	        size = Integer.parseInt(sizeString);
	    } catch (NumberFormatException e) {
	        return null;
	    }
	    FontEx f = new FontEx(name, Font.PLAIN, size);
	    StringTokenizer st = new StringTokenizer(typeString, "|");
	    while(st.hasMoreElements())
	    {
	    	String tok = st.nextToken();
	    	if (tok.equalsIgnoreCase("bold")) 
	 	        f.style |= Font.BOLD;
	 	     else if (tok.equalsIgnoreCase("italic")) 
	 	    	f.style |= Font.ITALIC;
	 	     else if(tok.equalsIgnoreCase("underline"))
	 	    	f.isUnderline = true;
	    	 else if(tok.equalsIgnoreCase("strikethrough"))
	 			f.isStrikethrough = true;
	    	 else if(tok.equalsIgnoreCase("subscript"))
	 			f.isSubscript = true;
	    	 else if(tok.equalsIgnoreCase("superscript"))
	 			f.isSuperscript = true;
	    }
	    return f;
	}
	
	 public int hashCode() 
	 {
		if(name == null) return 0;
		return name.hashCode() ^ style ^ size;
	}
}
