/*
 * @(#)QuaquaBorders.java  
 *
 * Copyright (c) 2006-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.border.*;
/**
 * QuaquaBorders.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaBorders.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaBorders {
    
    /**
     * Prevent instance creation.
     */
    private QuaquaBorders() {
    }
    

    /**
     * Border for a Frame.
     * @since 1.4
     */
    static class FrameBorder extends AbstractBorder implements UIResource {

        private static final Insets insets = new Insets(5, 5, 5, 5);

        private static final int corner = 14;

        public void paintBorder(Component c, Graphics g, int x, int y,
            int w, int h) {

            Color background;
            Color highlight;
            Color shadow;

            Window window = SwingUtilities.getWindowAncestor(c);
            if (window != null && QuaquaUtilities.isOnActiveWindow(window)) {
                background = Color.gray;//QuaquaLookAndFeel.getPrimaryControlDarkShadow();
                highlight = Color.white; //QuaquaLookAndFeel.getPrimaryControlShadow();
                shadow = Color.black; //QuaquaLookAndFeel.getPrimaryControlInfo();
            } else {
                background = Color.darkGray;//QuaquaLookAndFeel.getControlDarkShadow();
                highlight = Color.white;//QuaquaLookAndFeel.getControlShadow();
                shadow = Color.black;//QuaquaLookAndFeel.getControlInfo();
            }

            g.setColor(background);
            // Draw outermost lines
            g.drawLine( x+1, y+0, x+w-2, y+0);
            g.drawLine( x+0, y+1, x+0, y +h-2);
            g.drawLine( x+w-1, y+1, x+w-1, y+h-2);
            g.drawLine( x+1, y+h-1, x+w-2, y+h-1);

            // Draw the bulk of the border
            for (int i = 1; i < 5; i++) {
                g.drawRect(x+i,y+i,w-(i*2)-1, h-(i*2)-1);
            }

            if ((window instanceof Frame) && ((Frame) window).isResizable()) {
                g.setColor(highlight);
                // Draw the Long highlight lines
                g.drawLine( corner+1, 3, w-corner, 3);
                g.drawLine( 3, corner+1, 3, h-corner);
                g.drawLine( w-2, corner+1, w-2, h-corner);
                g.drawLine( corner+1, h-2, w-corner, h-2);

                g.setColor(shadow);
                // Draw the Long shadow lines
                g.drawLine( corner, 2, w-corner-1, 2);
                g.drawLine( 2, corner, 2, h-corner-1);
                g.drawLine( w-3, corner, w-3, h-corner-1);
                g.drawLine( corner, h-3, w-corner-1, h-3);
            }

        }

        public Insets getBorderInsets(Component c)       {
            return insets;
        }
    	  
        public Insets getBorderInsets(Component c, Insets newInsets) 
        {
            newInsets.top = insets.top;
            newInsets.left = insets.left;
            newInsets.bottom = insets.bottom;
            newInsets.right = insets.right;
            return newInsets;
        }
    }

    /**
     * Border for a Frame.
     * @since 1.4
     */
    static class DialogBorder extends AbstractBorder implements UIResource 
    {		
        private static final Insets insets = new Insets(5, 5, 5, 5);
        private static final int corner = 14;

        protected Color getActiveBackground()
        {
            return Color.gray;//QuaquaLookAndFeel.getPrimaryControlDarkShadow();
        }

        protected Color getActiveHighlight()
        {
            return Color.white;//QuaquaLookAndFeel.getPrimaryControlShadow();
        }

        protected Color getActiveShadow()
        {
            return Color.black;//QuaquaLookAndFeel.getPrimaryControlInfo();
        }

        protected Color getInactiveBackground()
        {
            return Color.darkGray;//QuaquaLookAndFeel.getControlDarkShadow();
        }

        protected Color getInactiveHighlight()
        {
            return Color.white;//QuaquaLookAndFeel.getControlShadow();
        }

        protected Color getInactiveShadow()
        {
            return Color.black;//QuaquaLookAndFeel.getControlInfo();
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) 
        {
            Color background;
            Color highlight;
            Color shadow;

            Window window = SwingUtilities.getWindowAncestor(c);
            if (window != null && QuaquaUtilities.isOnActiveWindow(window)) {
                background = getActiveBackground();
                highlight = getActiveHighlight();
                shadow = getActiveShadow();
            } else {
                background = getInactiveBackground();
                highlight = getInactiveHighlight();
                shadow = getInactiveShadow();
            }

            g.setColor(background);
            // Draw outermost lines
            g.drawLine( x + 1, y + 0, x + w-2, y + 0);
            g.drawLine( x + 0, y + 1, x + 0, y + h - 2);
            g.drawLine( x + w - 1, y + 1, x + w - 1, y + h - 2);
            g.drawLine( x + 1, y + h - 1, x + w - 2, y + h - 1);

            // Draw the bulk of the border
            for (int i = 1; i < 5; i++) {
                g.drawRect(x+i,y+i,w-(i*2)-1, h-(i*2)-1);
            }


            if ((window instanceof Dialog) && ((Dialog) window).isResizable()) {
                g.setColor(highlight);
                // Draw the Long highlight lines
                g.drawLine( corner+1, 3, w-corner, 3);
                g.drawLine( 3, corner+1, 3, h-corner);
                g.drawLine( w-2, corner+1, w-2, h-corner);
                g.drawLine( corner+1, h-2, w-corner, h-2);

                g.setColor(shadow);
                // Draw the Long shadow lines
                g.drawLine( corner, 2, w-corner-1, 2);
                g.drawLine( 2, corner, 2, h-corner-1);
                g.drawLine( w-3, corner, w-3, h-corner-1);
                g.drawLine( corner, h-3, w-corner-1, h-3);
            }
            
        }

        public Insets getBorderInsets(Component c)       {
            return insets;
        }

        public Insets getBorderInsets(Component c, Insets newInsets) 
        {
            newInsets.top = insets.top;
            newInsets.left = insets.left;
            newInsets.bottom = insets.bottom;
            newInsets.right = insets.right;
            return newInsets;
        }
    }

    /**
     * Border for an Error Dialog.
     * @since 1.4
     */
    static class ErrorDialogBorder extends DialogBorder implements UIResource
    {
        protected Color getActiveBackground() {
            return UIManager.getColor("OptionPane.errorDialog.border.background");
        }
    }
    

    /**
     * Border for a QuestionDialog.  Also used for a JFileChooser and a 
     * JColorChooser..
     * @since 1.4
     */
    static class QuestionDialogBorder extends DialogBorder implements UIResource
    {
        protected Color getActiveBackground() {
            return UIManager.getColor("OptionPane.questionDialog.border.background");
        }
    }
    

    /**
     * Border for a Warning Dialog.
     * @since 1.4
     */
    static class WarningDialogBorder extends DialogBorder implements UIResource
    {
        protected Color getActiveBackground() {
            return UIManager.getColor("OptionPane.warningDialog.border.background");
        }
    }
    

    /**
     * Border for a Palette.
     * @since 1.3
     */
    public static class PaletteBorder extends AbstractBorder implements UIResource {
        private static final Insets insets = new Insets(1, 1, 1, 1);
        int titleHeight = 0;

        public void paintBorder( Component c, Graphics g, int x, int y, int w, int h ) {  

	    g.translate(x,y);  
	    g.setColor(Color.black);//QuaquaLookAndFeel.getPrimaryControlDarkShadow());
	    g.drawLine(0, 1, 0, h-2);
	    g.drawLine(1, h-1, w-2, h-1);
	    g.drawLine(w-1,  1, w-1, h-2);
	    g.drawLine( 1, 0, w-2, 0);
	    g.drawRect(1,1, w-3, h-3);
	    g.translate(-x,-y);
      
	}

        public Insets getBorderInsets(Component c)       {
            return insets;
        }

        public Insets getBorderInsets(Component c, Insets newInsets) {
	    newInsets.top = insets.top;
	    newInsets.left = insets.left;
	    newInsets.bottom = insets.bottom;
	    newInsets.right = insets.right;
	    return newInsets;
	}
    }

    public static class OptionDialogBorder extends AbstractBorder implements UIResource {
        private static final Insets insets = new Insets(3, 3, 3, 3);
        int titleHeight = 0;

        public void paintBorder( Component c, Graphics g, int x, int y, int w, int h ) {  

	    g.translate(x,y);  

            int messageType = JOptionPane.PLAIN_MESSAGE;
            if (c instanceof JInternalFrame) {
                Object obj = ((JInternalFrame) c).getClientProperty(
                              "JInternalFrame.messageType"); 
                if (obj != null && (obj instanceof Integer)) {
                    messageType = ((Integer) obj).intValue();
                }
            }

            Color borderColor;

	    switch (messageType) {
            case(JOptionPane.ERROR_MESSAGE):
                borderColor = UIManager.getColor(
                    "OptionPane.errorDialog.border.background");
                break;
            case(JOptionPane.QUESTION_MESSAGE):
                borderColor = UIManager.getColor(
                    "OptionPane.questionDialog.border.background");
                break;
            case(JOptionPane.WARNING_MESSAGE):
                borderColor = UIManager.getColor(
                    "OptionPane.warningDialog.border.background");
                break;
            case(JOptionPane.INFORMATION_MESSAGE):
            case(JOptionPane.PLAIN_MESSAGE):
            default:
                borderColor = Color.black;//QuaquaLookAndFeel.getPrimaryControlDarkShadow();
                break;
	    }

	    g.setColor(borderColor);

              // Draw outermost lines
              g.drawLine( 1, 0, w-2, 0);
              g.drawLine( 0, 1, 0, h-2);
              g.drawLine( w-1, 1, w-1, h-2);
              g.drawLine( 1, h-1, w-2, h-1);

              // Draw the bulk of the border
              for (int i = 1; i < 3; i++) {
	          g.drawRect(i, i, w-(i*2)-1, h-(i*2)-1);
              }

	    g.translate(-x,-y);
      
	}

        public Insets getBorderInsets(Component c)       {
            return insets;
        }

        public Insets getBorderInsets(Component c, Insets newInsets) {
	    newInsets.top = insets.top;
	    newInsets.left = insets.left;
	    newInsets.bottom = insets.bottom;
	    newInsets.right = insets.right;
	    return newInsets;
	}
    }

}
