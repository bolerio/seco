/*
 * @(#)ButtonFocusIcon.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.icon;


import ch.randelshofer.quaqua.*;
import java.awt.*;
import javax.swing.*;

/**
 * A focus ring icon with different visuals reflecting the selected state of an
 * abstract button.
 *
 * @author  Werner Randelshofer
 * @version $Id: ButtonFocusIcon.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class ButtonFocusIcon extends MultiIcon {
    private final static int E = 0;
    private final static int S = 1;
    
    /**
     * Creates a new instance.
     * All icons must have the same dimensions.
     * If an icon is null, nothing is drawn for this state.
     */
    public ButtonFocusIcon(Icon e, Icon s) {
        super (new Icon[] {e, s});
    }
    
    /**
     * Creates a new instance.
     * All icons must have the same dimensions.
     * If an icon is null, nothing is drawn for this state.
     */
    public ButtonFocusIcon(Image[] images) {
        super(images);
    }
    /**
     * Creates a new instance.
     * All icons must have the same dimensions.
     * If an icon is null, nothing is drawn for this state.
     */
    public ButtonFocusIcon(Icon[] icons) {
        super(icons);
    }
    
    /**
     * Creates a new instance.
     * The icon representations are created lazily from the image.
     */
    public ButtonFocusIcon(Image tiledImage, int tileCount, boolean isTiledHorizontaly) {
        super(tiledImage, tileCount, isTiledHorizontaly);
    }

    protected Icon getIcon(Component c) {
        Icon icon = null;
        if (QuaquaUtilities.isFocused(c) && c.isEnabled()
        && (! (c instanceof AbstractButton) || ((AbstractButton) c).isFocusPainted())) {
            ButtonModel model = ((AbstractButton) c).getModel();
            if (model.isSelected()) {
                icon = icons[S];
            } else {
                icon = icons[E];
            }
        }
        return icon;
    }
    
    protected void generateMissingIcons() {
        if (icons.length != 2) {
       Icon[] newIcons = new Icon[2];
            System.arraycopy(icons, 0, newIcons, 0, Math.min(icons.length, 2));
            icons = newIcons;
        }
        
        if (icons[S] == null) {
            icons[S] = icons[E];
        }
    }
}
