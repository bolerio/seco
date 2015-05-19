/*
 * @(#)JNILoader.java  1.0  2013-03-21
 * 
 * Copyright (c) 2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with Werner Randelshofer. 
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.osx;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * {@code JNILoader}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2013-03-21 Created.
 */
public class JNILoader {
    public static void loadLibrary(String libName) {
        //Try to load the native library assuming the java.library.path was
        //set correctly at launch.
        //try {
        System.loadLibrary(libName);
        /*} catch (Error e) {
            JFrame f=new JFrame();
            JTextArea l=new JTextArea();
            StringWriter w=new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            l.setText(w.toString());
            f.add(new JScrollPane(l));
            f.pack();
            f.show();
            throw e;
        }*/
    }
}