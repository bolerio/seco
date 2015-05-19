/*
 * @(#)LinuxFileSystemView.java  
 *
 * Copyright (c) 2009-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Werner Randelshofer. For details see accompanying license terms
 */

package ch.randelshofer.quaqua.filechooser;

import ch.randelshofer.quaqua.osx.OSXFile;
import java.io.File;

/**
 * LinuxFileSystemView.
 *
 * @author Werner Randelshofer, stefanmd
 * @version $Id: LinuxFileSystemView.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class LinuxFileSystemView extends QuaquaFileSystemView {
    /** XXX - The computer mount does only exist in Gnome */
    private File computer = new File("computer:///");

    /** XXX - There is no volumes folder, only root. */
    private File volumesFolder = new File("/");

    /** XXX - This works on Gnome and KDE */
    private File desktop = new File(System.getProperty("user.home")+"/Desktop");

    /** The system volume is "/" for all Linux distributions. */
    private File systemVolume = new File("/");

    private final static boolean DEBUG = false;

    /**
     * Creates a new instance.
     */
    public LinuxFileSystemView() {
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
     * Returns all root partitions on this system.
     * XXX - This needs more work.
     */
    public File[] getRoots() {
        return new File[] {volumesFolder};
    }




    /**
     * Returns whether a file is hidden or not.
     */
    public boolean isHiddenFile(File f) {
        if (f.isHidden()) {
            return true;
        } else {
            String name = f.getName();
            if (name.length() == 0) {
                return false;
             } else if (name.charAt(0) == '.') {
                // File names starting with '.' are considered as
                // hidden
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Determines if the given file is a root partition or drive.
     */
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
        if (folder == null || file == null) {
            return false;
        } else {
            return folder.equals(file.getParentFile());
        }
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
        return new File(parent, fileName);
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
        File parentFile = dir.getParentFile();
        return parentFile == null || parentFile.equals(volumesFolder);
    }


    // Providing default implementations for the remaining methods
    // because most OS file systems will likely be able to use this
    // code. If a given OS can't, override these methods in its
    // implementation.
    public File getHomeDirectory() {
        return createFileObject(System.getProperty("user.home"));
    }

    /**
     * Return the user's default starting directory for the file chooser.
     *
     * @return a <code>File</code> object representing the default
     *         starting folder
     */
    public File getDefaultDirectory() {
        return getHomeDirectory();
    }
    public String getSystemDisplayName(File f) {
        // FIXME - Determine display name
        if (f.equals(systemVolume)) {
            return "/";
        } else {
            if (OSXFile.canWorkWithAliases()) {
                return OSXFile.getDisplayName(f);
            } else {
                return target.getSystemDisplayName(f);
            }
        }
    }
}