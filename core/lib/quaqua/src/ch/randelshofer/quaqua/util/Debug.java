/*
 * @(#)Debug.java 
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.util;

import ch.randelshofer.quaqua.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
/**
 * Debug.
 *
 * @author  Werner Randelshofer
 * @version $Id: Debug.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class Debug {
    private final static Stroke defaultStroke = new BasicStroke();
    /**
     * Private instance creation.
     */
    private Debug() {
    }
    static int rainbow=0;
    /**
     * This method is called from UI delegates at the end of its paint method.
     */
    public static void paint(Graphics gr, JComponent c, ComponentUI ui) {
        Graphics2D g = (Graphics2D) gr;
        g.setStroke(defaultStroke);
        
        if (UIManager.getBoolean("Quaqua.Debug.showVisualBounds")
        && ui instanceof VisuallyLayoutable) {
            
            VisuallyLayoutable layoutable = (VisuallyLayoutable) ui;
            g.setColor(UIManager.getColor("Quaqua.Debug.componentBoundsForeground"));
            Rectangle rect = layoutable.getVisualBounds(c,VisuallyLayoutable.COMPONENT_BOUNDS,c.getWidth(),c.getHeight());
            g.drawRect(rect.x,rect.y,rect.width - 1,rect.height - 1);
            
            g.setColor(UIManager.getColor("Quaqua.Debug.textBoundsForeground"));
            rect = layoutable.getVisualBounds(c,VisuallyLayoutable.TEXT_BOUNDS,c.getWidth(),c.getHeight());
            g.drawRect(rect.x,rect.y,rect.width - 1,rect.height - 1);
        }
        if (UIManager.getBoolean("Quaqua.Debug.showClipBounds")) {
            g.setColor(UIManager.getColor("Quaqua.Debug.clipBoundsForeground"));
            g.drawRect(0,0,c.getWidth() - 1,c.getHeight() - 1);
        }
        /*
        if (QuaquaManager.getBoolean("Quaqua.Debug.colorizePaintEvents")) {
            if (EventQueue.getCurrentEvent() != previousEvent) {
                previousEvent = EventQueue.getCurrentEvent();
            rainbow = (rainbow + 30) % 360;
            }
            g.setColor(
                    new Color(0x22ffffff & Color.HSBtoRGB((float) (rainbow / 360f), 0.4f, 1.0f),true)
                    );
            g.fillRect(0,0,c.getWidth(),c.getHeight());
        }*/
        
    }
}
