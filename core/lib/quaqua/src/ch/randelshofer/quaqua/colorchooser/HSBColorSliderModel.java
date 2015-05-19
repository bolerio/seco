/*
 * @(#)HSBColorSliderModel.java  1.0  May 22, 2005
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.colorchooser;

import java.awt.*;
import javax.swing.*;
/**
 * ColorSliderModel for the HSB color model (hue, saturation, brightness).
 *
 * @author  Werner Randelshofer
 * @version 1.0 May 22, 2005 Created.
 */
public class HSBColorSliderModel extends ColorSliderModel {
    
    /**
     * Creates a new instance.
     */
    public HSBColorSliderModel() {
        super(new DefaultBoundedRangeModel[] {
            new DefaultBoundedRangeModel(0, 0, 0, 359),
            new DefaultBoundedRangeModel(0, 0, 0, 100),
            new DefaultBoundedRangeModel(0, 0, 0, 100)
        });
    }
    
    public int getRGB() {
        return Color.HSBtoRGB(
        components[0].getValue() / 360f,
        components[1].getValue() / 100f,
        components[2].getValue() / 100f
        );
    }
    
    public void setRGB(int rgb) {
        float[] hsb = Color.RGBtoHSB((rgb & 0xff0000) >>> 16, (rgb & 0xff00) >>> 8, rgb & 0xff, new float[3]);
        components[0].setValue((int) (hsb[0] * 360f));
        components[1].setValue((int) (hsb[1] * 100f));
        components[2].setValue((int) (hsb[2] * 100f));
    }
    
    public int toRGB(int[] values) {
        return Color.HSBtoRGB(
        values[0] / 360f,
        values[1] / 100f,
        values[2] / 100f
        );
    }    
}
