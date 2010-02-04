/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/*
 * Mode.java - jEdit editing mode
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1998, 1999, 2000 Slava Pestov
 * Copyright (C) 1999 mike dillon
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
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

package seco.notebook.syntax;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.microstar.xml.XmlException;
import com.microstar.xml.XmlParser;

import seco.ThisNiche;

/**
 * An edit mode defines specific settings for editing some type of file.
 * One instance of this class is created for each supported edit mode.
 *
 * @author Slava Pestov
 * @version $Id: Mode.java,v 1.1 2006/07/14 17:22:44 bizi Exp $
 */
public class Mode
{
	private String name;
	private ScriptSupportFactory supportFactory;
	private Map<String, Object> props = new HashMap<String, Object>();
	//private RE firstlineRE;
	//private RE filenameRE;
	private TokenMarker marker;
	
    private void loadMe()
    {
        final String fileName = (String) getProperty("file");
        System.out.println("Loading edit mode " + fileName);
        final XmlParser parser = new XmlParser();
        XModeHandler xmh = new XModeHandler(getName()) {
            public void error(String what, Object subst)
            {
                int line = parser.getLineNumber();
                int column = parser.getColumnNumber();
                String msg;
                if (subst == null) msg = "xmode-error." + what;
                else
                {
                    msg = subst.toString();
                    if (subst instanceof Throwable)
                        System.out.println("ERROR: " + subst);
                }
                System.err.println("XMode error: " + msg + " file: " + fileName
                        + " line: " + line + " column: " + column);
            }

            public TokenMarker getTokenMarker(String modeName)
            {
                Mode mode = supportFactory.getMode(modeName);
                return (mode == null) ? null : mode.getTokenMarker();
            }
        };
        setTokenMarker(xmh.getTokenMarker());
        parser.setHandler(xmh);
        try
        {
            InputStream is = supportFactory.getClass().getResourceAsStream(fileName);
//            if (is == null)
//                is = Thread.currentThread().getContextClassLoader()
//                        .getResourceAsStream(fileName);
            // System.out.println("NotebookUI - loadMode: " + is);
            parser.parse(null, null, is, null); // grammar);

            setTokenMarker(xmh.getTokenMarker());
            setProperties(xmh.getModeProperties());
        }
        catch (Throwable e)
        {
            System.err.println("ERROR" + e);
            e.printStackTrace();
            if (e instanceof XmlException)
            {
                XmlException xe = (XmlException) e;
                int line = xe.getLine();
                String message = xe.getMessage();
                System.err.println("XMode error: " + message + " file: "
                        + fileName + " line: " + line);
            }
        }
    }
	
	/**
	 * Creates a new edit mode.
	 *
	 * @param name The name used in mode listings and to query mode
	 * properties
	 * @see #getProperty(String)
	 */
	public Mode(String name, String fileName, ScriptSupportFactory supportFactory)
	{
		this.name = name;
		this.supportFactory = supportFactory;
		setProperty("file", fileName);
	} 

	/**
	 * Returns the token marker for this mode.
	 */
	public TokenMarker getTokenMarker()
	{
		loadIfNecessary();
		return marker;
	} 

	/**
	 * Sets the token marker for this mode.
	 * @param marker The new token marker
	 */
	public void setTokenMarker(TokenMarker marker)
	{
		this.marker = marker;
	} 

	/**
	 * Loads the mode from disk if it hasn't been loaded already.
	 * @since jEdit 2.5pre3
	 */
	public void loadIfNecessary()
	{
		 if(marker == null) 
			 loadMe();	       
	} 

	/**
	 * Returns a mode property.
	 * @param key The property name
	 *
	 * @since jEdit 2.2pre1
	 */
	public Object getProperty(String key)
	{
		return props.get(key);
	} 

	/**
	 * Returns the value of a boolean property.
	 * @param key The property name
	 *
	 * @since jEdit 2.5pre3
	 */
	public boolean getBooleanProperty(String key)
	{
		Object value = getProperty(key);
		if("true".equals(value) || "on".equals(value) || "yes".equals(value))
			return true;
		else
			return false;
	} 

	/**
	 * Sets a mode property.
	 * @param key The property name
	 * @param value The property value
	 */
	public void setProperty(String key, Object value)
	{
		props.put(key,value);
	} 

	/**
	 * Unsets a mode property.
	 * @param key The property name
	 * @since jEdit 3.2pre3
	 */
	public void unsetProperty(String key)
	{
		props.remove(key);
	} 

	/**
	 * Should only be called by <code>XModeHandler</code>.
	 * @since jEdit 4.0pre3
	 */
	public void setProperties(Map props)
	{
		// need to carry over file name and first line globs because they are
		// not given to us by the XMode handler, but instead are filled in by
		// the catalog loader.
		//String filenameGlob = (String)this.props.get("filenameGlob");
		//String firstlineGlob = (String)this.props.get("firstlineGlob");
		String filename = (String)this.props.get("file");
		this.props = props;
		/*
		if(filenameGlob != null)
			props.put("filenameGlob",filenameGlob);
		if(firstlineGlob != null)
			props.put("firstlineGlob",firstlineGlob);
			*/
		if(filename != null)
			props.put("file",filename);
			
	} 

	/*
	public boolean accept(String fileName, String firstLine)
	{
		if(filenameRE != null && filenameRE.isMatch(fileName))
			return true;

		if(firstlineRE != null && firstlineRE.isMatch(firstLine))
			return true;

		return false;
	} 
	*/

	/**
	 * Returns the internal name of this edit mode.
	 */
	public String getName()
	{
		return name;
	} 

	/**
	 * Returns a string representation of this edit mode.
	 */
	public String toString()
	{
		return name;
	} 	
}