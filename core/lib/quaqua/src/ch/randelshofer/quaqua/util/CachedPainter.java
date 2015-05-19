/*
 * @(#)CachedPainter.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.util;

import java.awt.*;
import java.awt.image.*;
import java.lang.ref.SoftReference;
import java.util.*;

/**
 * A base class used for icons or images that are expensive to paint.
 * A subclass will do the following:
 * <ol>
 * <li>Invoke <code>paint</code> when you want to paint the image,
 *     if you are implementing <code>Icon</code> you'll invoke this from
 *     <code>paintIcon</code>.
 *     The args argument is useful when additional state is needed.
 * <li>Override <code>paintToImage</code> to render the image.  The code that
 *     lives here is equivalent to what previously would go in
 *     <code>paintIcon</code>, for an <code>Icon</code>.
 * </ol>
 * This class has been derived from javax.swing.plaf.metal.CachedPainter 1.2 04/02/15 
 * 
 * @author Werner Randelshofer
 * @version $Id: CachedPainter.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public abstract class CachedPainter {
    // CacheMap maps from class to Cache.

    private static final Map cacheMap = new HashMap();
    private int maxCachedImageSize = 20000;

    private static Cache getCache(Object key) {
        synchronized (cacheMap) {
            Cache cache = (Cache) cacheMap.get(key);
            if (cache == null) {
                cache = new Cache(1);
                cacheMap.put(key, cache);
            }
            return cache;
        }
    }

    /**
     * Creates an instance of <code>CachedPainter</code> that will cache up
     * to <code>cacheCount</code> images of this class.
     * 
     * @param cacheCount Max number of images to cache
     */
    public CachedPainter(int cacheCount) {
        getCache(getClass()).setMaxCount(cacheCount);
    }

    /**
     * Renders the cached image to the the passed in <code>Graphic</code>.
     * If there is no cached image <code>paintToImage</code> will be invoked.
     * <code>paintImage</code> is invoked to paint the cached image.
     */
    protected void paint(Component c, Graphics g, int x,
            int y, int w, int h, Object args) {
        if (w <= 0 || h <= 0) {
            return;
        }

        Object key = getClass();
        
        // If the area is larger than 20'000 pixels, render to the passed 
        // in Graphics. 20'000 pixels is a bit larger than a rectangle of 
        // 160*120 points. 
        if (getCache(key).getMaxCount()==0||w * h > maxCachedImageSize) {
            g.translate(x, y);
            paintToImage(c, g, w, h, args);
            g.translate(-x, -y);
            return;
        }

        
        GraphicsConfiguration config = c.getGraphicsConfiguration();
        Cache cache = getCache(key);
        Image image = cache.getImage(key, config, w, h, args);
        int attempts = 0;
        do {
            boolean draw = false;
            if (image instanceof VolatileImage) {
                // See if we need to recreate the image
                switch (((VolatileImage) image).validate(config)) {
                    case VolatileImage.IMAGE_INCOMPATIBLE:
                        ((VolatileImage) image).flush();
                        image = null;
                        break;
                    case VolatileImage.IMAGE_RESTORED:
                        draw = true;
                        break;
                }
            }
            if (image == null) {
                // Recreate the image
                image = createImage(c, w, h, config);
                cache.setImage(key, config, w, h, args, image);
                draw = true;
            }
            if (draw) {
                // Render to the Image
                paintToImage(c, image, w, h, args);
            }

            // Render to the passed in Graphics
            paintImage(c, g, x, y, w, h, image, args);

            // If we did this 3 times and the contents are still lost
            // assume we're painting to a VolatileImage that is bogus and
            // give up.  Presumably we'll be called again to paint.
        } while ((image instanceof VolatileImage)
                && ((VolatileImage) image).contentsLost() && ++attempts < 3);
    }

    /**
     * Paints the representation to cache to the supplied Graphics.
     *
     * @param c Component painting to
     * @param image Image to paint to
     * @param w Width to paint to
     * @param h Height to paint to
     * @param args Arguments supplied to <code>paint</code>
     */
    protected void paintToImage(Component c, Image image,
            int w, int h, Object args) {
        Graphics g2 = image.getGraphics();
        paintToImage(c, g2, w, h, args);
        g2.dispose();
    }

    /**
     * Paints the representation to cache to the supplied Graphics.
     *
     * @param c Component painting to
     * @param g Graphics to paint to
     * @param w Width to paint to
     * @param h Height to paint to
     * @param args Arguments supplied to <code>paint</code>
     */
    protected abstract void paintToImage(Component c, Graphics g,
            int w, int h, Object args);

    /**
     * Paints the image to the specified location.
     *
     * @param c Component painting to
     * @param g Graphics to paint to
     * @param x X coordinate to paint to
     * @param y Y coordinate to paint to
     * @param w Width to paint to
     * @param h Height to paint to
     * @param image Image to paint
     * @param args Arguments supplied to <code>paint</code>
     */
    protected void paintImage(Component c, Graphics g,
            int x, int y, int w, int h, Image image,
            Object args) {
        g.drawImage(image, x, y, null);
    }

    /**
     * Creates the image to cache.  This returns an opaque image, subclasses
     * that require translucency or transparency will need to override this
     * method.
     *
     * @param c Component painting to
     * @param w Width of image to create
     * @param h Height to image to create
     * @param config GraphicsConfiguration that will be
     *        rendered to, this may be null.
     */
    protected Image createImage(Component c, int w, int h,
            GraphicsConfiguration config) {
        if (config == null) {
            return new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        }
        return config.createCompatibleVolatileImage(w, h);
    }

    /**
     * Cache is used to cache an image based on a set of arguments.
     */
    private static class Cache {
        // Maximum number of entries to cache

        private int maxCount;
        // The entries.
        private java.util.List entries;

        Cache(int maxCount) {
            this.maxCount = maxCount;
            entries = new ArrayList(maxCount);
        }

        synchronized void setMaxCount(int maxCount) {
            this.maxCount = maxCount;
        }        
        
        public int getMaxCount() {
            return maxCount; 
        }


        private Entry getEntry(Object key, GraphicsConfiguration config,
                int w, int h, Object args) {
            synchronized (this) {
                Entry entry;
                for (int counter = entries.size() - 1; counter >= 0; counter--) {
                    entry = (Entry) ((SoftReference) entries.get(counter)).get();
                    if (entry == null) {
                        // SoftReference was invalidated, remove the entry
                        entries.remove(counter);
                    } else if (entry.equals(config, w, h, args)) {
                        // Found the entry, return it.
                        return entry;
                    }
                }
                // Entry doesn't exist
                entry = new Entry(config, w, h, args);
                if (entries.size() == maxCount) {
                    entries.remove(0);
                }
                entries.add(new SoftReference(entry));
                return entry;
            }
        }

        /**
         * Returns the cached Image, or null, for the specified arguments.
         */
        public Image getImage(Object key, GraphicsConfiguration config,
                int w, int h, Object args) {
            Entry entry = getEntry(key, config, w, h, args);
            return entry.getImage();
        }

        /**
         * Sets the cached image for the specified constraints.
         */
        public void setImage(Object key, GraphicsConfiguration config,
                int w, int h, Object args, Image image) {
            Entry entry = getEntry(key, config, w, h, args);
            entry.setImage(image);
        }

        /**
         * Caches set of arguments and Image.
         */
        private static class Entry {

            private GraphicsConfiguration config;
            private Object args;
            private Image image;
            private int w;
            private int h;

            Entry(GraphicsConfiguration config, int w, int h, Object args) {
                this.config = config;
                this.args = args;
                this.w = w;
                this.h = h;
            }

            public void setImage(Image image) {
                this.image = image;
            }

            public Image getImage() {
                return image;
            }

            @Override
            public String toString() {
                StringBuilder value = new StringBuilder(super.toString()
                        + "[ graphicsConfig=" + config
                        + ", image=" + image
                        + ", w=" + w + ", h=" + h);
                if (args != null) {
                    value.append(", ");
                    value.append(args);
                }
                value.append("]");
                return value.toString();
            }

            public boolean equals(GraphicsConfiguration config, int w, int h,
                    Object args) {
                if (this.w == w && this.h == h
                        && ((this.config != null && this.config.equals(config))
                        || (this.config == null && config == null))) {
                    if (this.args == null && args == null) {
                        return true;
                    }
                    if (this.args != null && args != null) {

                        if (!this.args.equals(args)) {
                            return false;
                        }


                        return true;
                    }
                }
                return false;
            }
        }
    }

    public int getMaxCachedImageSize() {
        return maxCachedImageSize;
    }

    public void setMaxCachedImageSize(int maxCachedImageSize) {
        this.maxCachedImageSize = maxCachedImageSize;
    }
    
    
}
