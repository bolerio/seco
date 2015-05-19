/*
 * @(#)GrayColorSliderModel.java  1.0  May 22, 2005
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.colorchooser;

import java.awt.Color;
import java.awt.color.ColorSpace;
import javax.swing.*;

/**
 * A ColorSliderModel for a gray color model (brightness).
 *
 * @author  Werner Randelshofer
 * @version $Id: GrayColorSliderModel.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class GrayColorSliderModel extends ColorSliderModel {

    private ColorSpace colorSpace;
    float[] rgb = new float[3];
    float[] gray = new float[1];

    /**
     * Creates a new instance.
     */
    public GrayColorSliderModel() {
        super(new DefaultBoundedRangeModel[]{
                    new DefaultBoundedRangeModel(0, 0, 0, 100)
                });
        colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
    }

    public ColorSpace getColorSpace() {
        return colorSpace;
    }

    public int getRGB() {
        /*
        int br = (int) (components[0].getValue() * 2.55f);
        return 0xff000000 | (br << 16) | (br << 8) | (br);
         */
        rgb = colorSpace.toRGB(new float[]{components[0].getValue() / 100f});
        return 0xff000000 | ((int) (rgb[0] * 255f) << 16) | ((int) (rgb[1] * 255f) << 8) | (int) (rgb[2] * 255f);
    }

    public void setRGB(int newRGB) {
        rgb[0] = ((newRGB & 0xff0000) >>> 16) / 255f;
        rgb[1] = ((newRGB & 0x00ff00) >>> 8) / 255f;
        rgb[2] = (newRGB & 0x0000ff) / 255f;
        gray = colorSpace.fromRGB(rgb);

        components[0].setValue((int) (gray[0] * 100f));
        /*
        components[0].setValue((int)
        (
        (((rgb & 0xff0000) >> 16) + ((rgb & 0x00ff00) >> 8) + (rgb & 0x0000ff))
        / 3f / 2.55f
        )
        );*/
    }

    public int toRGB(int[] values) {
        /*int br = (int) (values[0] * 2.55f);
        return 0xff000000 | (br << 16) | (br << 8) | (br);
         */
        rgb = colorSpace.toRGB(new float[]{values[0] / 100f});
        return 0xff000000 | ((int) (rgb[0] * 255f) << 16) | ((int) (rgb[1] * 255f) << 8) | (int) (rgb[2] * 255f);
    }

    @Override
    public Color getColor() {
        gray[0] = components[0].getValue() / 100f;
        return new Color(colorSpace, gray, 1f);
    }

    @Override
    public void setColor(Color color) {
        if (color.getColorSpace().equals(colorSpace)) {
            gray = color.getColorComponents(gray);
        } else {
            gray = color.getColorComponents(colorSpace, gray);
        }
        rgb = colorSpace.toRGB(gray);
        components[0].setValue((int) (gray[0] * 100f));
    }
}
