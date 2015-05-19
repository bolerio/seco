/*
 * @(#)VisualMargin.java  1.0  2011-07-28
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua;

import java.awt.Component;
import java.awt.Insets;
import javax.swing.border.Border;

/**
 * {@code VisualMargin}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-07-28 Created.
 */
public interface VisualMargin extends Border {
    public  Insets getVisualMargin(Component c) ;
}
