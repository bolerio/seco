/*
 * @(#)OSXJaguarFileSystemView.java 
 *
 * Copyright (c) 2001-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.jaguar.filechooser;

import ch.randelshofer.quaqua.filechooser.*;
import java.io.*;
import java.util.*;
/**
 * A file system view for Mac OS X 10.2 (Jaguar).
 * 
 * @author Werner Randelshofer
 * @version $Id: OSXJaguarFileSystemView.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class OSXJaguarFileSystemView extends QuaquaFileSystemView {
    private File volumesFolder = new File("/Volumes");
    private File networkFolder = new File("/Network");
    private static final File computer = new File("/");
    /**
     * On Jaguar, we can't determine the system volume.
     * We use the file system root instead.
     */
    private static final File systemVolume = computer;
    
    /**
     * This is a list of file names that are treated as invisible by the AWT
     * FileDialog when they are at the top directory level of a volume.
     * The file names are wrongly treated as visible by
     * Apple's implementation FileSystemView, so we use this HashSet here, to
     * hide them 'manually'.
     */
    private final static HashSet hiddenTopLevelNames = new HashSet();
    private final static HashSet hiddenDirectoryNames = new HashSet();
    static {
        String[] names = {
            "automount",
            "bin",
            "Cleanup At Startup",
            "cores",
            "Desktop DB",
            "Desktop DF",
            "Desktop Folder",
            "dev",
            "etc",
            "mach",
            "mach_kernel",
            "mach.sym",
            "private",
            "sbin",
            "Temporary Items",
            "TheVolumeSettingsFolder",
            "tmp",
            "Trash",
            "usr",
            "var",
            "Volumes",
        };
        
        hiddenTopLevelNames.addAll(Arrays.asList(names));

        names = new String[]{
                    "$RECYCLE.BIN",
                    "Thumbs.db",
                    "desktop.ini",};

        hiddenDirectoryNames.addAll(Arrays.asList(names));
    };
    
    
    public File getSystemVolume() {
        return systemVolume;
    }
    public File getComputer() {
        return computer;
    }
    
    /**
     * Returns the parent directory of dir.
     */
    public File getParentDirectory(File dir) {
        return (isRoot(dir)) ? null : super.getParentDirectory(dir);
    }
    
    /**
     * Returns all root partitions on this system. For example, on Windows,
     * this would be the A: through Z: drives.
     */
    public File[] getRoots() {
        File[] fileArray;
        ArrayList roots = new ArrayList();
        
        fileArray = super.getRoots();
        if (fileArray != null) {
            roots.addAll(Arrays.asList(fileArray));
        }
        
        fileArray = volumesFolder.listFiles();
        if (fileArray != null) {
            roots.addAll(Arrays.asList(fileArray));
        }
        roots.add(networkFolder);
        
        return (File[]) roots.toArray(new File[roots.size()]);
        
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
            } else if (name.charAt(name.length() - 1) == (char) 0x0d) {
                // File names ending with 0x0d are considered as
                // hidden
                return true;
            } else if (name.charAt(0) == '.') {
                // File names starting with '.' are considered as
                // hidden
                return true;
            } else if (hiddenTopLevelNames.contains(name)
            && (f.getParent() == null || isRoot(f.getParentFile()))) {
                return true;
            } else if (hiddenDirectoryNames.contains(name)) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Determines if the given file is a root partition or drive.
     */
    public boolean isRoot(File aFile) {
        return aFile.equals(computer)
        || aFile.equals(networkFolder)
        || aFile.getParentFile() != null && aFile.getParentFile().equals(volumesFolder);
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
}
