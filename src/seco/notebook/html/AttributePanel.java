/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
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

package seco.notebook.html;

import javax.swing.JPanel;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.util.*;
import java.awt.event.*;

/**
 * Panel set a group of attributes.
 *
 * <p>Abstract base class for other panels such as margin or style panel.</p>
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

public abstract class AttributePanel extends JPanel
    implements AttributeComponent, ContainerListener
{

  /** container for all AttributeComponents shown on this AttributePanel */
  private Vector components = new Vector();

  /**
   * construct a new AttributePanel
   */
  public AttributePanel() {
    super();
    this.addContainerListener(this);
  }

  /**
   * set the value of this <code>AttributeComponent</code>
   *
   * @param a  the set of attributes possibly having an
   *          attribute this component can display
   *
   * @return true, if the set of attributes had a matching attribute,
   *            false if not
   */
  public boolean setValue(AttributeSet a) {
    /*
    System.out.println("AttributePanel setValue");
    de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
    hd.listAttributes(a, 4);
    System.out.println("\r\n");
    */
    boolean result = true;
    Enumeration elements = components.elements();
    AttributeComponent ac;
    while(elements.hasMoreElements()) {
      ac = (AttributeComponent) elements.nextElement();
      if(!ac.setValue(a)) {
        result = false;
      }
    }
    return result;
  }

  /**
   * get the value of this <code>AttributeComponent</code>
   *
   * @return the value selected from this component
   */
  public AttributeSet getValue() {
    SimpleAttributeSet attributes = new SimpleAttributeSet();
    Enumeration elements = components.elements();
    AttributeComponent ac;
    while(elements.hasMoreElements()) {
      ac = (AttributeComponent) elements.nextElement();
      attributes.addAttributes(ac.getValue());
    }
    return attributes;
  }

  public AttributeSet getValue(boolean includeUnchanged) {
    if(includeUnchanged) {
      SimpleAttributeSet attributes = new SimpleAttributeSet();
      Enumeration elements = components.elements();
      AttributeComponent ac;
      while(elements.hasMoreElements()) {
        ac = (AttributeComponent) elements.nextElement();
        attributes.addAttributes(ac.getValue(includeUnchanged));
      }
      return attributes;
    }
    else {
      return getValue();
    }
  }

  public void componentAdded(ContainerEvent e) {
    Object component = e.getChild();
    if(component instanceof AttributeComponent) {
      components.add(component);
    }
  }

  public void componentRemoved(ContainerEvent e) {
    Object component = e.getChild();
    if(component instanceof AttributeComponent) {
      components.remove(component);
    }
  }
}
