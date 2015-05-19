/*
 * @(#)Images.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.util;

import ch.randelshofer.quaqua.osx.OSXPreferences;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

/**
 * Image processing methods.
 *
 * @author  Werner Randelshofer, Karl von Randow
 * @version $Id: Images.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class Images {

    /** Prevent instance creation. */
    private Images() {
    }
    private static GraphiteFilter graphiteFilter;

    private static GraphiteFilter getGraphiteFilter() {
        if (graphiteFilter == null) {
            graphiteFilter = new GraphiteFilter();
        }
        return graphiteFilter;
    }

    public static Image createImage(Class baseClass, String location) {
        URL resource=baseClass.getResource(location);
        if (resource==null) throw new IllegalArgumentException("no resource found for location:"+location);
        return createImage(resource);
    }

    public static Image createImage(URL resource) {
        Image image = Toolkit.getDefaultToolkit().createImage(resource);
        if (OSXPreferences.getString(OSXPreferences.GLOBAL_PREFERENCES, "AppleAquaColorVariant", "1").equals("6")) {
            if (canGraphite(resource)) {
                image = toGraphite(image);
            }
        }
        return image;
    }
    private static volatile Properties canGraphite;

    private static boolean canGraphite(URL resource) {
        if (canGraphite == null) {
            synchronized (Images.class) {
                if (canGraphite == null) {
                    Properties p = new Properties();
                    InputStream in = Images.class.getResourceAsStream("graphiteable.properties");
                    try {
                        p.load(in);
                    } catch (IOException e) {
                        System.err.println("Failed to load graphiteable.properties: " + e);
                    } finally {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            System.err.println("Failed to load graphiteable.properties: " + ex);
                        }
                    }
                    canGraphite = p;
                }
            }
        }
        String file = resource.getFile();
        int i = file.lastIndexOf(File.separatorChar);
        if (i != -1) {
            file = file.substring(i + 1);
        }
        return canGraphite.containsKey(file);
    }

    /**
     * This method returns a buffered image with the contents of an image.
     *
     * Code derived from the Java Developers Almanac 1.4
     * http://javaalmanac.com/egs/java.awt.image/Image2Buf.html?l=rel
     */
    private static Image toGraphite(Image image) {
        return Toolkit.getDefaultToolkit().
                createImage(new FilteredImageSource(image.getSource(), getGraphiteFilter()));
    }

    /**
     * The graphite filter converts Mac OS X artwork from "Blue Appearance" to
     * "Graphite Appearance" by desaturing the colors.
     */
    public static class GraphiteFilter extends RGBImageFilter {

        private final static float saturationAdjust = 0.179f;

        public int filterRGB(int x, int y, int rgb) {
            int alpha = rgb & 0xff000000;
            int red = (rgb >>> 16) & 0xff;
            int green = (rgb >>> 8) & 0xff;
            int blue = rgb & 0xff;

            float weight = (1f - saturationAdjust) * 1f / 3f;
            float a = weight + saturationAdjust;
            float b = weight;
            float c = weight;

            int outputRed = (int) (a * red + c * green + b * blue);
            int outputGreen = (int) (b * red + a * green + c * blue);
            int outputBlue = (int) (c * red + b * green + a * blue);
            return alpha | (outputRed << 16) | (outputGreen << 8) | (outputBlue);
        }
    }

    public static BufferedImage toBufferedImage(RenderedImage rImg) {
        BufferedImage image;
        if (rImg instanceof BufferedImage) {
            image = (BufferedImage) rImg;
        } else {
            Raster r = rImg.getData();
            WritableRaster wr = WritableRaster.createWritableRaster(
                    r.getSampleModel(), null);
            rImg.copyData(wr);
            image = new BufferedImage(
                    rImg.getColorModel(),
                    wr,
                    rImg.getColorModel().isAlphaPremultiplied(),
                    null);
        }
        return image;
    }

    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;

        if (System.getProperty("java.version").startsWith("1.4.1_")) {
            // Workaround for Java 1.4.1 on Mac OS X.
            // For this JVM, we always create an ARGB image to prevent a class
            // cast exception in
            // sun.awt.image.BufImgSurfaceData.createData(BufImgSurfaceData.java:434)
            // when we attempt to draw the buffered image.
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        } else {
            // Determine if the image has transparent pixels; for this method's
            // implementation, see e661 Determining If an Image Has Transparent Pixels
            boolean hasAlpha;
            try {
                hasAlpha = hasAlpha(image);
            } catch (IllegalAccessError e) {
                // If we can't determine this, we assume that we have an alpha,
                // in order not to loose data.
                hasAlpha = true;
            }


            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            try {
                // Determine the type of transparency of the new buffered image
                int transparency = Transparency.OPAQUE;
                if (hasAlpha) {
                    transparency = Transparency.TRANSLUCENT;
                }

                // Create the buffered image
                GraphicsDevice gs = ge.getDefaultScreenDevice();
                GraphicsConfiguration gc = gs.getDefaultConfiguration();
                bimage = gc.createCompatibleImage(
                        image.getWidth(null), image.getHeight(null), transparency);
            } catch (Exception e) {
                //} catch (HeadlessException e) {
                // The system does not have a screen
            }

            if (bimage == null) {
                // Create a buffered image using the default color model
                int type = BufferedImage.TYPE_INT_RGB;
                if (hasAlpha) {
                    type = BufferedImage.TYPE_INT_ARGB;
                }
                bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
            }
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;

        // My own implementation:
        /*
        if (image instanceof BufferedImage) {
        return (BufferedImage) image;
        } else {
        BufferedImage bufImg;
        Frame f = new Frame();
        f.pack();
        MediaTracker t = new MediaTracker(f);
        t.addImage(image, 0);
        try { t.waitForAll(); } catch (InterruptedException e) {}

        // Workaround for Java 1.4.1 on Mac OS X.
        if (System.getProperty("java.version").startsWith("1.4.1_")) {
        bufImg = new BufferedImage(image.getWidth(f), image.getHeight(f), BufferedImage.TYPE_INT_ARGB);
        } else {
        bufImg = GraphicsEnvironment
        .getLocalGraphicsEnvironment()
        .getDefaultScreenDevice()
        .getDefaultConfiguration()
        .createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.TRANSLUCENT);
        }
        Graphics2D imgGraphics = bufImg.createGraphics();
        imgGraphics.drawImage(image, 0, 0, f);
        imgGraphics.dispose();
        f.dispose();
        return bufImg;
        }*/
    }

    /**
     * This method returns true if the specified image has transparent pixels
     *
     * Code taken from the Java Developers Almanac 1.4
     * http://javaalmanac.com/egs/java.awt.image/HasAlpha.html
     */
    public static boolean hasAlpha(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }

        // Get the image's color model
        // We must check for null here, because the pixel grabber
        // may not have been able to retrieve the color model.
        ColorModel cm = pg.getColorModel();
        return (cm != null) ? cm.hasAlpha() : false;
    }

    /**
     * Splits an image into count subimages.
     */
    public static BufferedImage[] split(Image image, int count, boolean isHorizontal) {
        BufferedImage src = Images.toBufferedImage(image);
        if (count == 1) {
            return new BufferedImage[]{src};
        }

        BufferedImage[] parts = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            if (isHorizontal) {
                parts[i] = src.getSubimage(
                        src.getWidth() / count * i, 0,
                        src.getWidth() / count, src.getHeight());
            } else {
                parts[i] = src.getSubimage(
                        0, src.getHeight() / count * i,
                        src.getWidth(), src.getHeight() / count);
            }
        }
        return parts;
    }
}
