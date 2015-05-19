package ch.randelshofer.quaqua.colorchooser;

/*
 * @(#)PaletteEntry.java  1.0  19 septembre 2005
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

import java.awt.*;
/**
 * PaletteEntry.
 *
 * @author  Werner Randelshofer
 * @version 1.0 19 septembre 2005 Created.
 */
public class PaletteEntry {
    private String name;
    private Color color;
    
    /**
     * Creates a new instance.
     */
    public PaletteEntry(String name, Color color) {
        this.name = name;
        this.color = color;
    }
    public String getName() {
        return name;
    }
    public String toString() {
        return name;
    }
    public Color getColor() {
        return color;
    }
}
