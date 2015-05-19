/**
 * @(#)NonEDTTest.java  1.0  May 1, 2008
 *
 * Copyright (c) 2008 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package test;

import ch.randelshofer.quaqua.QuaquaManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 * NonEDTTest.
 *
 * @author Werner Randelshofer
 * @version 1.0 May 1, 2008 Created.
 */
public class NonEDTTest {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(QuaquaManager.getLookAndFeel());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(NonEDTTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        JFileChooser fc = new JFileChooser();
        
        JFrame f = new JFrame("NonEDTest");
        f.getContentPane().add(fc);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }
}
