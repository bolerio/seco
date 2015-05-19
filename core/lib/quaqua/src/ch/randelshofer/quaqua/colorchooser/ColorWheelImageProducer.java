/*
 * @(#)ColorWheelImageProducer.java 
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua.colorchooser;

import java.awt.*;
import java.awt.image.*;

/**
 * Produces the image of a ColorWheel.
 *
 * @author  Werner Randelshofer
 * @version $Id: ColorWheelImageProducer.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class ColorWheelImageProducer extends MemoryImageSource {
    protected int[] pixels;
    protected int w, h;
    protected float brightness = 1f;
    protected boolean isDirty = true;
    
    /** Lookup table for hues. */
    protected float[] hues;
    /** Lookup table for saturations. */
    protected float[] saturations;
    /** Lookup table for alphas. 
     * The alpha value is used for antialiasing the
     * color wheel.
     */
    protected int[] alphas;
    
    /** Creates a new instance. */
    public ColorWheelImageProducer(int w, int h) {
        super(w, h, null, 0, w);
        pixels = new int[w*h];
        this.w = w;
        this.h = h;
        generateLookupTables();
        newPixels(pixels, ColorModel.getRGBdefault(), 0, w);
        setAnimated(true);
        generateColorWheel();
    }
    
    public int getRadius() {
        return Math.min(w, h) / 2 - 2;
    }
    
    protected void generateLookupTables() {
        saturations = new float[w*h];
        hues = new float[w*h];
        alphas = new int[w*h];
        float radius = getRadius();
        
        // blend is used to create a linear alpha gradient of two extra pixels
        float blend = (radius + 2f) / radius - 1f;

        // Center of the color wheel circle
        int cx = w / 2;
        int cy = h / 2;
        
        for (int x=0; x < w; x++) {
            int kx = x - cx; // Kartesian coordinates of x
            int squarekx = kx * kx; // Square of kartesian x
            
            for (int y=0; y < h; y++) {
                int ky = cy - y; // Kartesian coordinates of y
                
                int index = x + y * w;
                saturations[index] = (float) Math.sqrt(squarekx + ky*ky) / radius;
                if (saturations[index] <= 1f) {
                    alphas[index] = 0xff000000;
                } else {
                    alphas[index] = (int) ((blend - Math.min(blend,saturations[index] - 1f)) * 255 / blend) << 24;
                    saturations[index] = 1f;
                }
                if (alphas[index] != 0) {
                    hues[index] = (float) (Math.atan2(ky, kx) / Math.PI / 2d);
                }
            }
        }
    }
    
    public void setBrightness(float newValue) {
        isDirty = isDirty || brightness != newValue;
        brightness = newValue;
    }
    
    public boolean needsGeneration() {
        return isDirty;
    }
    
    public void regenerateColorWheel() {
        if (isDirty) {
            generateColorWheel();
        }
    }
    
    public void generateColorWheel() {
        for (int index=0; index < pixels.length; index++) {
            if (alphas[index] != 0) {
                pixels[index] = alphas[index]
                | 0xffffff & Color.HSBtoRGB(hues[index], saturations[index], brightness);
            }
        }
        newPixels();
        isDirty = false;
    }
    protected Point getColorLocation(Color c, int width, int height) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
        return getColorLocation(hsb[0], hsb[1], hsb[2], width, height);
    }

    protected Point getColorLocation(float hue, float saturation, float brightness, int width, int height) {
        float radius = Math.min(width, height) / 2f;
        return new Point(
                width / 2 + (int) (radius * saturation * Math.cos(hue * Math.PI * 2d)),
                height / 2 - (int) (radius * saturation * Math.sin(hue * Math.PI * 2d)));
    }

    protected float[] getColorAt(int x, int y, int width, int height) {
        x -= width / 2;
        y -= height / 2;
        float r = (float) Math.sqrt(x * x + y * y);
        float theta = (float) Math.atan2(y, -x);

        float[] hsb = {
            (float) (0.5 + (theta / Math.PI / 2d)),
            Math.min(1f, (float) r / getRadius()),
            brightness
        };
        return hsb;
    }

}
