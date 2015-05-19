/*
 * @(#)VisuallyLayoutable.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.awt.*;
import javax.swing.*;

/**
 * This interface is implemented by user interface delegates, which support
 * layouts based on visual criteria.
 * <p>
 * <b>Warning:</b> This is an experimental API. Expect substantial changes on
 * each release.
 * 
 * @author  Werner Randelshofer
 * @version $Id: VisuallyLayoutable.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public interface VisuallyLayoutable {
    /**
     * The clip bounds of a component.
     */
    public final static int CLIP_BOUNDS = 0;
    /**
     * The visually perceived bounds of a component, 
     * e.g. on a JButton with isBorderDrawn = true: the borderline except 
     * cast shadows. 
     * e.g. on a JLabel with an icon: the borderline of the icon plus the
     * TEXT_BOUNDS of the text on the JLabel
     * e.g. on a JLabel without an icon: the TEXT_BOUNDS of the text on 
     * the JLabel
     *
     * If COMPONENT_BOUNDS is not applicable, CLIP_BOUNDS are used instead.
     */
    public final static int COMPONENT_BOUNDS = 1;
    /**
     * The visually perceived bounds of the text on the component.
     * If TEXT_BOUNDS is not applicable, COMPONENT_BOUNDS are used instead.
     */
    public final static int TEXT_BOUNDS = 2;
    
    /**
     * Returns the baseline for the specified component, or -1 if the
     * baseline can not be determined.  The baseline is measured from
     * the top of the component.
     *
     * @param c JComponent to calculate baseline for
     * @param width Width of the component to determine baseline for.
     * @param height Height of the component to determine baseline for.
     * @return baseline for the specified component
     */
    public int getBaseline(JComponent c, int width, int height);

    /**
     * Returns the visual bounds for the specified component, or null if the
     * visual bounds can not be determined.
     *
     * @param c JComponent to calculate visual margin for
     * @param type The type of the visual margin.
     * @param width Width of the component
     * @param height Height of the component
     * @return The visual bounds in component coordinates.
     */
    public Rectangle getVisualBounds(JComponent c, int type, int width, int height);
    
}
