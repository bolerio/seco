/*
 * @(#)PressedCueBorder.java  1.0  2011-07-31
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.border;

import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * {@code PressedCueBorder}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-07-31 Created.
 */
public interface PressedCueBorder extends Border {
    /**
     * Returns true, if this border has a visual cue for the pressed
     * state of the button.
     * If the border has no visual cue, then the ButtonUI has to provide
     * it by some other means.
     */
    public boolean hasPressedCue(JComponent c);
}
