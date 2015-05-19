/*
 * @(#)FlickerTest.java
 *
 * Copyright (c) 2009 Werner Randelshofer, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Werner Randelshofer. For details see accompanying license terms.
 */
package test;

import javax.swing.*;
/**
 * FlickerTest demonstrates the flickering that occurs, when a look and feel
 * decorated frame is resized.
 * <p>
 * Opens two JFrames. One with native frame decoration, and one with look and 
 * feel decoration. Resizing the frame with native decoration does not flicker.
 * Resizing the frame with look and feel decoration causes its content to flicker.
 * <p>
 * This problem affects: J2SE 1.4, J2SE 5 and J2SE 6 on Mac OS X.
 * 
 * @author Werner Randelshofer
 * @version $Id: FlickerTest.java 458 2013-05-29 15:44:33Z wrandelshofer $
 */
public class FlickerTest {
    
    /** Creates a new instance. */
    public FlickerTest() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                JFrame f = new JFrame("Frame with native decoration");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.getContentPane().add(new JLabel("Resize me, I don't flicker"));
                f.pack();
                f.setVisible(true);
                
                f = new JFrame("Frame with look and feel decoration");
                f.setUndecorated(true);
                f.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.getContentPane().add(new JLabel("Resize me, and see me flicker"));
                f.pack();
                f.setLocation(0,80);
                f.setVisible(true);
                
                
            }
        });
        // TODO code application logic here
    }
    
}
