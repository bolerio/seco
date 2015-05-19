/*
 * @(#)DarwinLeopardFileSystemView.java  
 *
 * Copyright (c) 2007-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.leopard.filechooser;

import ch.randelshofer.quaqua.filechooser.QuaquaFileSystemView;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;

/**
 * DarwinLeopardFileSystemView.
 * 
 * @author Werner Randelshofer
 * @version $Id: DarwinLeopardFileSystemView.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class DarwinLeopardFileSystemView extends QuaquaFileSystemView {

    private static final File volumesFolder = new File("/Volumes");
    private static final File networkFolder = new File("/Network");
    private static final File computer = new File("/");
    private static File systemVolume;
    /**
     * This is a list of file names that are treated as invisible by the AWT
     * FileDialog when they are at the top directory level of a volume.
     * The file names are wrongly treated as visible by
     * Apple's implementation FileSystemView, so we use this HashSet here, to
     * hide them 'manually'.
     */
    private final static HashSet hiddenTopLevelNames = new HashSet();
    

    static {
        String[] names = {
            "AppleShare PDS",
            "automount",
            "bin",
            "Cleanup At Startup",
            "cores",
            "Desktop DB",
            "Desktop DF",
            "dev",
            "etc",
            "home",
            "mach",
            "mach_kernel",
            "mach_kernel.ctfsys",
            "mach.sym",
            "net",
            "opt",
            "private",
            "sbin",
            "Temporary Items",
            "TheVolumeSettingsFolder",
            "TheFindByContentFolder",
            "tmp",
            "Trash",
            "usr",
            "var",
            "Volumes",
            "\u0003\u0002\u0001Move&Rename",
        };

        hiddenTopLevelNames.addAll(Arrays.asList(names));
    }
    ;

    /** Creates a new instance. */
    public DarwinLeopardFileSystemView() {
    }

    public File getComputer() {
        return computer;
    }

    public File getSystemVolume() {
        if (systemVolume == null) {
            File[] volumes = volumesFolder.listFiles();
            File sys = null;
            for (int i = 0; i < volumes.length; i++) {
                try {
                    if (volumes[i].getCanonicalFile().equals(computer)) {
                        sys = volumes[i];
                        break;
                    }
                } catch (IOException e) {
                    // We get here because we can't determine the
                    // canonical path for the volume. We suppress this
                    // exception, in the hope that it did not happen for
                    // the system volume. If it happened for the system
                    // volume, there is fallback code in method
                    // getSystemVolume() that handles this problem.
                    // System.err.println(
                    //   "Unable to canonicalize volume "+volumes[i]
                    // );
                    // e.printStackTrace();
                } catch (SecurityException e) {
                    // We get here because we are not allowed to read the
                    // file. We suppress this exception, in the hope that
                    // it did not happen for the system volume. If it
                    // happened for the system volume, there is fallback
                    // code in method getSystemVolume() that handles this
                    // problem.
                }
            }
            // If we couldn't determine the system volume, we use the
            // root folder instead.
            systemVolume = (sys == null) ? computer : sys;
        }
        return systemVolume;
    }

   public boolean isParent(File folder, File file) {
        if (folder == null || file == null) {
            return false;
        } else {
            return folder.equals(file.getParentFile());
        }
    }

    public File getChild(File parent, String fileName) {
        return new File(parent, fileName);
    }


    public File getDefaultDirectory() {
        return getHomeDirectory();
    }
}
