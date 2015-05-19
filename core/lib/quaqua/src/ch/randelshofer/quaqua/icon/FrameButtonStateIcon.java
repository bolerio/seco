/*
 * @(#)ButtonStateIcon.java  1.0  2006-02-13
 *
 * Copyright (c) 2006-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.icon;

import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
/**
 * An Icon with different visuals reflecting the state of the AbstractButton
 * on which it draws on.
 *
 * @author  Werner Randelshofer
 * @version 1.0 2006-02-13 Created.
 */
public class FrameButtonStateIcon extends MultiIcon {
    private final static int E = 0;
    private final static int EP = 1;
    private final static int ES = 2;
    private final static int EPS = 3;
    private final static int D = 4;
    private final static int DS = 5;
    private final static int I = 6;
    private final static int IS = 7;
    private final static int DI = 8;
    private final static int DIS = 9;
    private final static int R = 10;
    private final static int RS = 11;
    
    /**
     * Creates a new instance.
     * All icons must have the same dimensions.
     *
     * The array indices are used to represente the following states:
     * [0] Enabled
     * [1] Armed
     * [2] Pressed
     * [3] Disabled
     * [4] Enabled Selected
     * [5] Armed Selected
     * [6] Pressend Selected
     * [7] Disabled Selected
     *
     * If an array element is null, an icon is derived for the state from the
     * other icons.
     */
    public FrameButtonStateIcon(Image[] images) {
        super(images);
    }
    /**
     * Creates a new instance.
     * All icons must have the same dimensions.
     * If an icon is null, nothing is drawn for this state.
     */
    public FrameButtonStateIcon(Icon[] icons) {
        super(icons);
    }
    
    /**
     * Creates a new instance.
     * The icon representations are created lazily from the image.
     */
    public FrameButtonStateIcon(Image tiledImage, int tileCount, boolean isTiledHorizontally) {
        super(tiledImage, tileCount, isTiledHorizontally);
    }
    
    private boolean isRollover(Component c) {
        if (c instanceof JComponent) {
            return ((JComponent) c).getClientProperty("paintRollover") == Boolean.TRUE;
        }
        return false;
    }
    
    
    protected Icon getIcon(Component c) {
        Icon icon;
        boolean isRollover = isRollover(c);
        boolean isActive = QuaquaUtilities.isOnActiveWindow(c);
        
        if (c instanceof AbstractButton) {
            ButtonModel model = ((AbstractButton) c).getModel();
            if (isActive) {
                if (model.isEnabled()) {
                    if (/*model.isPressed() && */model.isArmed()) {
                        if (model.isSelected()) {
                            icon = icons[EPS];
                        } else {
                            icon = icons[EP];
                        }
                    } else if (model.isSelected()) {
                        icon =  (isRollover) ? icons[RS] : icons[ES];
                    } else {
                        icon = (isRollover) ? icons[R] : icons[E];
                    }
                } else {
                    if (model.isSelected()) {
                        icon = icons[DS];
                    } else {
                        icon = icons[D];
                    }
                }
            } else {
                if (model.isEnabled()) {
                    if (model.isSelected()) {
                        icon = (isRollover) ? icons[RS] : icons[IS];
                    } else {
                        icon = (isRollover) ? icons[R] : icons[I];
                    }
                } else {
                    if (model.isSelected()) {
                        icon = icons[DIS];
                    } else {
                        icon = icons[DI];
                    }
                }
            }
        } else {
            if (isActive) {
                if (c.isEnabled()) {
                    icon = (isRollover) ? icons[R] : icons[E];
                } else {
                    icon = icons[D];
                }
            } else {
                if (c.isEnabled()) {
                    icon = icons[I];
                } else {
                    icon = icons[DI];
                }
            }
        }
        return icon;
    }
    
    protected void generateMissingIcons() {        
        if (icons.length != 12) {
            Icon[] helper = icons;
            icons = new Icon[12];
            System.arraycopy(helper, 0, icons, 0, Math.min(helper.length, icons.length));
        }
        
        if (icons[EP] == null) {
            icons[EP] = icons[E];
        }
        if (icons[ES] == null) {
            icons[ES] = icons[EP];
        }
        if (icons[EPS] == null) {
            icons[EPS] = icons[EP];
        }
        if (icons[D] == null) {
            icons[D] = icons[E];
        }
        if (icons[DS] == null) {
            icons[DS] = icons[ES];
        }
        if (icons[I] == null) {
            icons[I] = icons[E];
        }
        if (icons[IS] == null) {
            icons[IS] = icons[ES];
        }
        if (icons[DI] == null) {
            icons[DI] = icons[D];
        }
        if (icons[DIS] == null) {
            icons[DIS] = icons[DS];
        }
        if (icons[R] == null) {
            icons[R] = icons[E];
        }
        if (icons[RS] == null) {
            icons[RS] = icons[ES];
        }
    }
}
