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

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;

/**
 * <p>Maps HTML and CSS attributes to their equivalents to
 * compensate discrepancies in HTML and CSS rendering of
 * various different view environments.</p>
 *
 * <p>Introduced in stage 5 this class only contains hard wired
 * fixes to certain discrepancies. Should there come up an increased
 * number of necessary fixes in future stages, a more generic way
 * of mapping (such as through a Hashtable of from/to values), etc.
 * will be done.</p>
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

public class AttributeMapper extends SimpleAttributeSet {

  public static final int toHTML = 1;
  public static final int toJava = 2;

  public AttributeMapper() {
    super();
  }

  public AttributeMapper(AttributeSet a) {
    super(a);
  }

  public AttributeSet getMappedAttributes(int direction) {
    switch(direction) {
      case toHTML:
        mapToHTMLAttributes();
        break;
      case toJava:
        mapToJavaAttributes();
        break;
    }
    //System.out.println("AttributeMapper transformed attributes=");
    //de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
    //hd.listAttributes(this, 2);
    return this;
  }

  private void mapToHTMLAttributes() {
    Object cssFontFamily = getAttribute(CSS.Attribute.FONT_FAMILY);
    if(cssFontFamily != null) {
      if(cssFontFamily.toString().equalsIgnoreCase("SansSerif")) {
        addAttribute(CSS.Attribute.FONT_FAMILY, "SansSerif, Sans-Serif");
        //System.out.println("mapToHTMLAttributes SansSerif, Sans-Serif");
      }
      else if(cssFontFamily.toString().indexOf("Monospaced") > -1) {
        addAttribute(CSS.Attribute.FONT_FAMILY, "Monospace, Monospaced");
      }
    }
    /*
    Object cssFontSize = getAttribute(CSS.Attribute.FONT_SIZE);
    if(cssFontSize != null) {
      int size = new Float(new LengthValue(cssFontSize).getValue() / 1.3).intValue();
      addAttribute(CSS.Attribute.FONT_SIZE, Integer.toString(size) + "pt");
    }
    */
  }

  private void mapToJavaAttributes() {
    Object htmlFontFace = getAttribute(HTML.Attribute.FACE);
    Object cssFontFamily = getAttribute(CSS.Attribute.FONT_FAMILY);
    if(htmlFontFace != null) {
      if(cssFontFamily != null) {
        removeAttribute(HTML.Attribute.FACE);
        if(cssFontFamily.toString().indexOf("Sans-Serif") > -1) {
          Util.styleSheet().addCSSAttribute(this, CSS.Attribute.FONT_FAMILY, "SansSerif");
        }
        else if(cssFontFamily.toString().indexOf("Monospace") > -1) {
          Util.styleSheet().addCSSAttribute(this, CSS.Attribute.FONT_FAMILY, "Monospaced");
        }
      }
      else {
        removeAttribute(HTML.Attribute.FACE);
        if(htmlFontFace.toString().indexOf("Sans-Serif") > -1) {
          Util.styleSheet().addCSSAttribute(this, CSS.Attribute.FONT_FAMILY, "SansSerif");
        }
        else if(htmlFontFace.toString().indexOf("Monospace") > -1) {
          Util.styleSheet().addCSSAttribute(this, CSS.Attribute.FONT_FAMILY, "Monospaced");
        }
        else {
          Util.styleSheet().addCSSAttribute(this, CSS.Attribute.FONT_FAMILY, htmlFontFace.toString());
        }
      }
    }
    else {
      if(cssFontFamily != null) {
        if(cssFontFamily.toString().indexOf("Sans-Serif") > -1) {
          Util.styleSheet().addCSSAttribute(this, CSS.Attribute.FONT_FAMILY, "SansSerif");
        }
        else if(cssFontFamily.toString().indexOf("Monospace") > -1) {
          Util.styleSheet().addCSSAttribute(this, CSS.Attribute.FONT_FAMILY, "Monospaced");
        }
      }
    }
  }

}
