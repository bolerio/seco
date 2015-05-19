/*
 * @(#)AnimatedBorder.java 
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.border;

import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.*;
/**
 * AnimatedBorder takes an array of borders and a delay value, to draw an
 * animated border.
 * The border to be drawn is selected based on the current time.
 *
 * @author  Werner Randelshofer
 * @version $Id: AnimatedBorder.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class AnimatedBorder implements Border {
    /**
     * In this HashSet we store all components that are scheduled for repainting.
     */
    private HashSet scheduledComponents = new HashSet();
    /**
     * Animation borders.
     * All borders must have the same insets.
     */
    private Border[] borders;
    /**
     * Delay time between borders.
     */
    private long delay;
    
    /** Creates a new instance.
     * <p>
     * Note: For efficiency reasons this method stores the passed in array
     * internally without copying it. Do not modify the array after
     * invoking this method.
     */
    public AnimatedBorder(Border[] borders, long delay) {
        this.borders = borders;
        this.delay = delay;
    }
    
    public Insets getBorderInsets(Component c) {
        return (Insets) borders[0].getBorderInsets(c).clone();
    }
    
    public boolean isBorderOpaque() {
        return borders[0].isBorderOpaque();
    }
    
    public void paintBorder(final Component c, Graphics g, int x, int y, int width, int height) {
        long animTime = System.currentTimeMillis() % (borders.length * delay);
        int frame = (int) (animTime / delay);

        borders[frame].paintBorder(c, g, x, y, width, height);
    }
}


