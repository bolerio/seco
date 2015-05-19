/*
 * @(#)QuaquaFileSystemView.java 
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Werner Randelshofer. For details see accompanying license terms.
 */

package ch.randelshofer.quaqua.filechooser;

import ch.randelshofer.quaqua.osx.OSXFile;
import ch.randelshofer.quaqua.QuaquaManager;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
/**
 * QuaquaFileSystemView is an enhanced FileSystemView, which provides additional
 * information about a file system required for Aqua file choosers.
 * QuaquaFileSystemView acts as a wrapper on platform specific file system views.
 * The resulting view is an Aqua-style view on the file system.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaFileSystemView.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public abstract class QuaquaFileSystemView extends FileSystemViewFilter {

    /**
     * Creates a new instance.
     */
    public QuaquaFileSystemView() {
    }

    /**
     * Returns the file that represents this computer node.
     */
    public abstract File getComputer();

    /**
     * Returns the file that represents the system (boot) volume of this
     * computer.
     */
    public abstract File getSystemVolume();

    /**
     * Creates a system specific file view for the specified JFileChooser.
     */
    public FileView createFileView(JFileChooser chooser) {
        return new QuaquaFileView(this);
    }

    private static QuaquaFileSystemView fileSystemView;

    /**
     * Returns a FileSystemView that can be cast into QuaquaFileSystemView.
     */
    public static QuaquaFileSystemView getQuaquaFileSystemView() {
        if (fileSystemView == null) {
            String className;
            int os = QuaquaManager.getOS();
                switch (os) {
                    case QuaquaManager.JAGUAR :
                        className = "ch.randelshofer.quaqua.jaguar.filechooser.OSXJaguarFileSystemView";
                        break;
                    case QuaquaManager.PANTHER :
                        className = "ch.randelshofer.quaqua.panther.filechooser.OSXPantherFileSystemView";
                        break;
                    case QuaquaManager.DARWIN :
                        className = "ch.randelshofer.quaqua.leopard.filechooser.DarwinLeopardFileSystemView";
                        break;
                    case QuaquaManager.LEOPARD :
                        className = "ch.randelshofer.quaqua.leopard.filechooser.OSXLeopardFileSystemView";
                        break;
                    case QuaquaManager.SNOW_LEOPARD :
                        className = "ch.randelshofer.quaqua.snowleopard.filechooser.OSX16SnowLeopardFileSystemView";
                        break;
                    case QuaquaManager.LION :
                    case QuaquaManager.MOUNTAIN_LION :
                    case QuaquaManager.MAVERICKS :
                    case QuaquaManager.X :
                    default:
                        className = "ch.randelshofer.quaqua.lion.filechooser.OSXLionFileSystemView";
                        break;
                    case QuaquaManager.TIGER :
                        className = "ch.randelshofer.quaqua.tiger.filechooser.OSXTigerFileSystemView";
                        break;
                    case QuaquaManager.WINDOWS :
                    	className = "ch.randelshofer.quaqua.filechooser.WindowsFileSystemView";
                    	break;
                    case QuaquaManager.LINUX :
                    	className = "ch.randelshofer.quaqua.filechooser.LinuxFileSystemView";
                    	break;
                }
            try {
                fileSystemView = (QuaquaFileSystemView) Class.forName(className).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new InternalError(e.getMessage());
            }
        }
        return fileSystemView;
    }

    /**
     * Sets the QuaquaFileSystemView.
     * Set this to null, if you want Quaqua determine which file system view to use.
     */
    public static void setQuaquaFileSystemView(QuaquaFileSystemView newValue) {
        fileSystemView = newValue;
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
    public Icon getSystemIcon(File f) {
        if (f.equals(getComputer())) {
            return UIManager.getIcon("FileView.computerIcon");
        } else {
            if (OSXFile.canWorkWithAliases()) {
                return OSXFile.getIcon(f, 16);
            } else {
                return target.getSystemIcon(f);
            }
        }
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
    public String getSystemTypeDescription(File f) {
        if (OSXFile.canWorkWithAliases()) {
            return OSXFile.getKindString(f);
        } else {
        return target.getSystemTypeDescription(f);
        }
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
    public Boolean isTraversable(File f) {
        if (OSXFile.canWorkWithAliases()) {
            return Boolean.valueOf(OSXFile.isTraversable(f));
        } else {
            return target.isTraversable(f);
        }
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
    public String getSystemDisplayName(File f) {
        // FIXME - Determine display name
        if (f.equals(getComputer())) {
            return getSystemVolume().getName();
        } else {
            if (OSXFile.canWorkWithAliases()) {
                return OSXFile.getDisplayName(f);
            } else {
                return target.getSystemDisplayName(f);
            }
        }
    }
}