/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
// ExtFileFilter.java

/** Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 ** 
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and the
 ** Institute for Systems Biology and the Whitehead Institute 
 ** have no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall the
 ** Institute for Systems Biology and the Whitehead Institute 
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if the
 ** Institute for Systems Biology and the Whitehead Institute 
 ** have been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 ** 
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/


package seco.util;
//---------------------------------------------------------------------------
import java.lang.Runtime;
import java.io.File;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Set;
import java.io.FilenameFilter;
import javax.swing.*;
import javax.swing.filechooser.*;


/**
 * A convenience implementation of FileFilter that filters out
 * all files except for those type extensions that it knows about.
 *
 * Extensions are of the type ".foo", which is typically found on
 * Windows and Unix boxes, but not on Macinthosh. Case is ignored.
 *
 * @version 1.0 05/02/03
 * @author Larissa Kamenkovich
 */
public class ExtFileFilter 
  extends 
    FileFilter
  implements 
    FilenameFilter {

    private static String TYPE_UNKNOWN = "Type Unknown";
    private static String HIDDEN_FILE = "Hidden File";

    private Hashtable filters = null;
    private String description = null;
    private String fullDescription = null;
    private boolean useExtensionsInDescription = true;

    /**
     * Creates a file filter. If no filters are added, then all
     * files are accepted.
     *
     * @see #addExtension
     */
    public ExtFileFilter() {
	this.filters = new Hashtable();
    }

    /**
     * Creates a file filter that accepts files with the given extension.
     * Example: new ExampleFileFilter("jpg");
     *
     * @see #addExtension
     */
    public ExtFileFilter(String extension) {
	this(extension,null);
    }

    /**
     * Creates a file filter that accepts the given file type.
     * Example: new ExampleFileFilter("jpg", "JPEG Image Images");
     *
     * Note that the "." before the extension is not needed. If
     * provided, it will be ignored.
     *
     * @see #addExtension
     */
    public ExtFileFilter(String extension, String description) {
	this();
	if(extension!=null) addExtension(extension);
 	if(description!=null) setDescription(description);
    }

    /**
     * Creates a file filter from the given string array.
     * Example: new ExampleFileFilter(String {"gif", "jpg"});
     *
     * Note that the "." before the extension is not needed adn
     * will be ignored.
     *
     * @see #addExtension
     */
    public ExtFileFilter(String[] filters) {
	this(filters, null);
    }

    /**
     * Creates a file filter from the given string array and description.
     * Example: new ExampleFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     *
     * @see #addExtension
     */
    public ExtFileFilter(String[] filters, String description) {
	this();
	for (int i = 0; i < filters.length; i++) {
	    // add filters one by one
	    addExtension(filters[i]);
	}
 	if(description!=null) setDescription(description);
    }

    /**
     * Return true if this file should be shown in the directory pane,
     * false if it shouldn't.
     *
     * Files that begin with "." are ignored.
     *
     * @see #getExtension
     * @see FileFilter#accepts
     */
    public boolean accept(File f) {

	if(f != null) {
        //  If there are no filters, always accept
        if (filters.size()==0) {
            return true;
        }
	    if(f.isDirectory()) {
    		return true;
	    }
	    String extension = getExtension(f);
	    if(extension != null && filters.get(getExtension(f)) != null) {
		return true;
	    };
	}
	return false;
    }

  /**
   * In order to implement the AWT version of this class 
   * "FileNameFilter", the following method must also be 
   * implemented.
   *
   */
  public boolean accept ( File dir, String name ) {

    return accept( new File( name ) );
  }


    /**
     * Return the extension portion of the file's name .
     *
     * @see #getExtension
     * @see FileFilter#accept
     */
     public String getExtension(File f) {
	if(f != null) {
	    String filename = f.getName();
	    int i = filename.lastIndexOf('.');
	    if(i>0 && i<filename.length()-1) {
		return filename.substring(i+1).toLowerCase();
	    };
	}
	return null;
    }

    /**
     * Adds a filetype "dot" extension to filter against.
     *
     * For example: the following code will create a filter that filters
     * out all files except those that end in ".jpg" and ".tif":
     *
     *   ExampleFileFilter filter = new ExampleFileFilter();
     *   filter.addExtension("jpg");
     *   filter.addExtension("tif");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     */
    public void addExtension(String extension) {
	if(filters == null) {
	    filters = new Hashtable(5);
	}
	filters.put(extension.toLowerCase(), this);
	fullDescription = null;
    }


    /**
     * Returns the human readable description of this filter. For
     * example: "JPEG and GIF Image Files (*.jpg, *.gif)"
     *
     * @see setDescription
     * @see setExtensionListInDescription
     * @see isExtensionListInDescription
     * @see FileFilter#getDescription
     */
    public String getDescription() {
	if(fullDescription == null) {
	    if(description == null || isExtensionListInDescription()) {
 		fullDescription = description==null ? "(" : description + " (";
		// build the description from the extension list
		Enumeration extensions = filters.keys();
		if(extensions != null) {
                  fullDescription += "*." + (String) (extensions.hasMoreElements() ? extensions.nextElement() : "*");
		    while (extensions.hasMoreElements()) {
			fullDescription += ", *." + (String) extensions.nextElement();
		    }
		}
		fullDescription += ")";
	    } else {
		fullDescription = description;
	    }
	}
	return fullDescription;
    }

    /**
     * Sets the human readable description of this filter. For
     * example: filter.setDescription("Gif and JPG Images");
     *
     * @see setDescription
     * @see setExtensionListInDescription
     * @see isExtensionListInDescription
     */
    public void setDescription(String description) {
	this.description = description;
	fullDescription = null;
    }

    /**
     * Determines whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevent if a description was provided in the constructor
     * or using setDescription();
     *
     * @see getDescription
     * @see setDescription
     * @see isExtensionListInDescription
     */
    public void setExtensionListInDescription(boolean b) {
	useExtensionsInDescription = b;
	fullDescription = null;
    }

    /**
     * Returns whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevent if a description was provided in the constructor
     * or using setDescription();
     *
     * @see getDescription
     * @see setDescription
     * @see setExtensionListInDescription
     */
    public boolean isExtensionListInDescription() {
	return useExtensionsInDescription;
    }

    /**
     * Returns the Set of file extension fontNames.
     *
     */
    public Set getExtensionSet() {
      return filters.keySet();
    }
}
