/*
 * @(#)WindowsFileSystemView.java 
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.filechooser;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.*;
import java.io.*;
import java.util.*;
/**
 * WindowsFileSystemView provides a Aqua-style view on the windows file system.
 * 
 * 
 * @author Werner Randelshofer
 * @version $Id: WindowsFileSystemView.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class WindowsFileSystemView extends QuaquaFileSystemView {
    private File computer = new File("\\");
    private File volumesFolder;
    private File desktop;
    private File systemVolume = new File("C:\\");
    private final static boolean DEBUG = false;
    
    /**
     * Creates a new instance.
     */
    public WindowsFileSystemView() {
        volumesFolder = getParentDirectory(systemVolume);
        desktop = new File(systemVolume,"WINDOWS\\Desktop");
    }
    
    public File getComputer() {
        return computer;
    }
    
    public File getSystemVolume() {
        return volumesFolder;
    }

    public File getDesktop() {
        return desktop;
    }

    /**
     * Returns all root partitions on this system. For example, on
     * Windows, this would be the "Desktop" folder, while on DOS this
     * would be the A: through Z: drives.
     */
    public File[] getRoots() {
        return getFiles(volumesFolder, true);
        /*
        File[] roots1 = getFiles(volumesFolder, true);
        File[] roots2 = new File[roots1.length + 1];
        roots2[0] = volumesFolder;
        System.arraycopy(roots1, 0, roots2, 1, roots1.length);
        return roots2;*/
    }
    
    
    /**
     * On Windows, a file can appear in multiple folders, other than its
     * parent directory in the filesystem. Folder could for example be the
     * "Desktop" folder which is not the same as file.getParentFile().
     *
     * @param folder a <code>File</code> object repesenting a directory or special folder
     * @param file a <code>File</code> object
     * @return <code>true</code> if <code>folder</code> is a directory or special folder and contains <code>file</code>.
     */
    public boolean isParent(File folder, File file) {
        return target.isParent(folder, file);
    }
    
    /**
     *
     * @param parent a <code>File</code> object repesenting a directory or special folder
     * @param fileName a name of a file or folder which exists in <code>parent</code>
     * @return a File object. This is normally constructed with <code>new
     * File(parent, fileName)</code> except when parent and child are both
     * special folders, in which case the <code>File</code> is a wrapper containing
     * a <code>ShellFolder</code> object.
     */
    public File getChild(File parent, String fileName) {
        return target.getChild(parent, fileName);
    }
    
    
    /**
     * Is dir the root of a tree in the file system, such as a drive
     * or partition. Example: Returns true for "C:\" on Windows 98.
     *
     * @param dir a <code>File</code> object representing a directory
     * @return <code>true</code> if <code>f</code> is a root of a filesystem
     * @see #isRoot
     */
    public boolean isFileSystemRoot(File dir) {
        return target.isFileSystemRoot(dir);
    }
    
    // Providing default implementations for the remaining methods
    // because most OS file systems will likely be able to use this
    // code. If a given OS can't, override these methods in its
    // implementation.
    
    public File getHomeDirectory() {
        return target.getHomeDirectory();
    }
    
    /**
     * Return the user's default starting directory for the file chooser.
     *
     * @return a <code>File</code> object representing the default
     *         starting folder
     */
    public File getDefaultDirectory() {
        return target.getDefaultDirectory();
    }
    public boolean isRoot(File f) {
	if (f == null || !f.isAbsolute()) {
	    return false;
	}
        
        if (f.equals(computer)) {
            return true;
        }
        
	File[] roots = getRoots();
	for (int i = 0; i < roots.length; i++) {
	    if (roots[i].equals(f)) {
		return true;
	    }
	}
	return false;
    }
}
