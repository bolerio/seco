/*
 * @(#)FileSystemViewFilter.java
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.filechooser;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;
/**
 * This is a filter for the FileSystemView provided by the Java VM.
 * The filter can change some of the behaviour of its target FileSystemView.
 *
 * @author  Werner Randelshofer
 * @version $Id: FileSystemViewFilter.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public abstract class FileSystemViewFilter extends FileSystemView {
    protected FileSystemView target;
    
    /**
     * Creates a new instance.
     */
    public FileSystemViewFilter() {
        target = FileSystemView.getFileSystemView();
    }
    
    /**
     * Creates a new folder with a default folder name.
     */
    public File createNewFolder(File containingDir) throws IOException {
        return target.createNewFolder(containingDir);
    }
    
    /**
     * Determines if the given file is a root in the navigatable tree(s).
     * Examples: Windows 98 has one root, the Desktop folder. DOS has one root
     * per drive letter, <code>C:\</code>, <code>D:\</code>, etc. Unix has one root,
     * the <code>"/"</code> directory.
     *
     * The default implementation gets information from the <code>ShellFolder</code> class.
     *
     * @param f a <code>File</code> object representing a directory
     * @return <code>true</code> if <code>f</code> is a root in the navigatable tree.
     * @see #isFileSystemRoot
     */
    @Override
    public boolean isRoot(File f) {
        return target.isRoot(f);
    }

    /**
     * Returns true if the file (directory) can be visited.
     * Returns false if the directory cannot be traversed.
     *
     * @param f the <code>File</code>
     * @return <code>true</code> if the file/directory can be traversed, otherwise <code>false</code>
     * @see JFileChooser#isTraversable
     * @see FileView#isTraversable
     */
    @Override
    public Boolean isTraversable(File f) {
	return target.isTraversable(f);
    }

    /**
     * Name of a file, directory, or folder as it would be displayed in
     * a system file browser. Example from Windows: the "M:\" directory
     * displays as "CD-ROM (M:)"
     *
     * The default implementation gets information from the ShellFolder class.
     *
     * @param f a <code>File</code> object
     * @return the file name as it would be displayed by a native file chooser
     * @see JFileChooser#getName
     */
    @Override
    public String getSystemDisplayName(File f) {
        return target.getSystemDisplayName(f);
    }

    /**
     * Type description for a file, directory, or folder as it would be displayed in
     * a system file browser. Example from Windows: the "Desktop" folder
     * is desribed as "Desktop".
     *
     * Override for platforms with native ShellFolder implementations.
     *
     * @param f a <code>File</code> object
     * @return the file type description as it would be displayed by a native file chooser
     * or null if no native information is available.
     * @see JFileChooser#getTypeDescription
     */
    @Override
    public String getSystemTypeDescription(File f) {
	return target.getSystemTypeDescription(f);
    }

    /**
     * Icon for a file, directory, or folder as it would be displayed in
     * a system file browser. Example from Windows: the "M:\" directory
     * displays a CD-ROM icon.
     *
     * The default implementation gets information from the ShellFolder class.
     *
     * @param f a <code>File</code> object
     * @return an icon as it would be displayed by a native file chooser
     * @see JFileChooser#getIcon
     */
    @Override
    public Icon getSystemIcon(File f) {
        return target.getSystemIcon(f);
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
    @Override
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
    @Override
    public File getChild(File parent, String fileName) {
        return target.getChild(parent, fileName);
    }


    /**
     * Checks if <code>f</code> represents a real directory or file as opposed to a
     * special folder such as <code>"Desktop"</code>. Used by UI classes to decide if
     * a folder is selectable when doing directory choosing.
     *
     * @param f a <code>File</code> object
     * @return <code>true</code> if <code>f</code> is a real file or directory.
     */
    @Override
    public boolean isFileSystem(File f) {
        return target.isFileSystem(f);
    }

    /**
     * Returns whether a file is hidden or not.
     */
    @Override
    public boolean isHiddenFile(File f) {
	return target.isHiddenFile(f);
    }

    /**
     * Is dir the root of a tree in the file system, such as a drive
     * or partition. Example: Returns true for "C:\" on Windows 98.
     * 
     * @param dir a <code>File</code> object representing a directory
     * @return <code>true</code> if <code>f</code> is a root of a filesystem
     * @see #isRoot
     */
    @Override
    public boolean isFileSystemRoot(File dir) {
	return target.isFileSystemRoot(dir);
    }

    /**
     * Used by UI classes to decide whether to display a special icon
     * for drives or partitions, e.g. a "hard disk" icon.
     *
     * The default implementation has no way of knowing, so always returns false.
     *
     * @param dir a directory
     * @return <code>false</code> always
     */
    @Override
    public boolean isDrive(File dir) {
	return target.isDrive(dir);
    }

    /**
     * Used by UI classes to decide whether to display a special icon
     * for a floppy disk. Implies isDrive(dir).
     *
     * The default implementation has no way of knowing, so always returns false.
     *
     * @param dir a directory
     * @return <code>false</code> always
     */
    @Override
    public boolean isFloppyDrive(File dir) {
	return target.isFloppyDrive(dir);
    }

    /**
     * Used by UI classes to decide whether to display a special icon
     * for a computer node, e.g. "My Computer" or a network server.
     *
     * The default implementation has no way of knowing, so always returns false.
     *
     * @param dir a directory
     * @return <code>false</code> always
     */
    @Override
    public boolean isComputerNode(File dir) {
	return target.isComputerNode(dir);
    }


    /**
     * Returns all root partitions on this system. For example, on
     * Windows, this would be the "Desktop" folder, while on DOS this
     * would be the A: through Z: drives.
     */
    @Override
    public File[] getRoots() {
        return target.getRoots();
    }


    // Providing default implementations for the remaining methods
    // because most OS file systems will likely be able to use this
    // code. If a given OS can't, override these methods in its
    // implementation.

    @Override
    public File getHomeDirectory() {
	return target.getHomeDirectory();
    }

    /**
     * Return the user's default starting directory for the file chooser.
     *
     * @return a <code>File</code> object representing the default
     *         starting folder
     */
    @Override
    public abstract File getDefaultDirectory();
    /*
    public File getDefaultDirectory() {
        return target.getDefaultDirectory();
    }*/

    /**
     * Returns a File object constructed in dir from the given filename.
     */
    @Override
    public File createFileObject(File dir, String filename) {
        return target.createFileObject(dir, filename);
    }

    /**
     * Returns a File object constructed from the given path string.
     */
    @Override
    public File createFileObject(String path) {
        return target.createFileObject(path);
    }


    /**
     * Gets the list of shown (i.e. not hidden) files.
     */
    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        return target.getFiles(dir, useFileHiding);
    }



    /**
     * Returns the parent directory of <code>dir</code>.
     * @param dir the <code>File</code> being queried
     * @return the parent directory of <code>dir</code>, or
     *   <code>null</code> if <code>dir</code> is <code>null</code>
     */
    @Override
    public File getParentDirectory(File dir) {
        return target.getParentDirectory(dir);
    }
}


