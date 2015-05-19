/*
 * @(#)QuaquaResourceBundleUtil.java  1.3.3  2005-11-07
 *
 * Copyright (c) 2000-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.util;

import java.util.*;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.ImageIcon;
import java.text.MessageFormat;
import java.net.URL;

/**
 * This is a convenience wrapper for accessing resources stored in a ResourceBundle.
 *
 * @author  Werner Randelshofer, Switzerland
 * @version 1,3.3 2005-11-07 Method getLocale added.
 * <br>1.3.2 2004-05-02 Method getLAFBundle without Locale as parameter
 * added.
 * <br>1.3.1 2002-07-30 Method getLAFBundle now takes a Locale as
 * an additional parameter.
 * <br>1.3 2001-10-10   The default resource name changed from 'name_Metal'
 *                            to 'name'.
 * <br>      1.2 2001-07-23   Adaptation to JDK 1.3 in progress.
 * <br>      1.0 2000-06-10   Created.
 */
public class QuaquaResourceBundleUtil {
    /** The wrapped resource bundle. */
    private ResourceBundle resource;
    
    /**
     * Creates a new ResouceBundleUtil which wraps
     * the provided resource bundle.
     */
    public QuaquaResourceBundleUtil(ResourceBundle r) {
        resource = r;
    }
    
    
    /**
     * Get a String from the ResourceBundle.
     * <br>Convenience method to save casting.
     *
     * @param key The key of the property.
     * @return The value of the property. Returns the key
     *          if the property is missing.
     */
    public String getString(String key) {
        try {
            return resource.getString(key);
        } catch (MissingResourceException e) {
            return '-'+key+'-';
        }
    }
    /**
     * Get an image icon from the ResourceBundle.
     * <br>Convenience method .
     *
     * @param key The key of the property.
     * @return The value of the property. Returns null
     *          if the property is missing.
     */
    public ImageIcon getImageIcon(String key, Class baseClass) {
        try {
            String rsrcName = resource.getString(key);
            if (rsrcName.equals("")) {
                return null;
            }
            URL url = baseClass.getResource(rsrcName);
            return (url == null) ? null : new ImageIcon(url);
        } catch (MissingResourceException e) {
            return null;
        }
    }
    
    /**
     * Get a Mnemonic from the ResourceBundle.
     * <br>Convenience method.
     *
     * @param key The key of the property.
     * @return The first char of the value of the property.
     *          Returns '\0' if the property is missing.
     */
    public char getMnemonic(String key) {
        String s = resource.getString(key);
        return (s == null || s.length() == 0) ? '\0' : s.charAt(0);
    }
    /**
     * Get a Mnemonic from the ResourceBundle.
     * <br>Convenience method.
     *
     * @param key The key of the property. This method appends "Mnem" to the key.
     * @return The first char of the value of the property.
     *          Returns '\0' if the property is missing.
     */
    public char getMnem(String key) {
        String s = resource.getString(key+"Mnem");
        return (s == null || s.length() == 0) ? '\0' : s.charAt(0);
    }
    
    /**
     * Get a KeyStroke from the ResourceBundle.
     * <BR>Convenience method.
     *
     * @param key The key of the property.
     * @return <code>javax.swing.KeyStroke.getKeyStroke(value)</code>.
     *          Returns null if the property is missing.
     */
    public KeyStroke getKeyStroke(String key) {
        KeyStroke ks = null;
        try {
            String s = resource.getString(key);
            ks = (s == null) ? (KeyStroke) null : KeyStroke.getKeyStroke(s);
        } catch (NoSuchElementException e) {
        }
        return ks;
    }
    /**
     * Get a KeyStroke from the ResourceBundle.
     * <BR>Convenience method.
     *
     * @param key The key of the property. This method adds "Acc" to the key.
     * @return <code>javax.swing.KeyStroke.getKeyStroke(value)</code>.
     *          Returns null if the property is missing.
     */
    public KeyStroke getAcc(String key) {
        KeyStroke ks = null;
        try {
            String s = resource.getString(key+"Acc");
            ks = (s == null) ? (KeyStroke) null : KeyStroke.getKeyStroke(s);
        } catch (NoSuchElementException e) {
        }
        return ks;
    }
    
    public String getFormatted(String key, Object argument) {
        return MessageFormat.format(resource.getString(key), new Object[] {argument});
    }
    public String getFormatted(String key, Object[] arguments) {
        return MessageFormat.format(resource.getString(key), arguments);
    }

    public Locale getLocale() {
        return resource.getLocale();
    }
    
    /**
     * Get the appropriate ResourceBundle subclass.
     *
     * @see java.util.ResourceBundle
     */
    public static QuaquaResourceBundleUtil getBundle(String baseName)
    throws MissingResourceException {
        return new QuaquaResourceBundleUtil(ResourceBundle.getBundle(baseName, Locale.getDefault()));
    }
    /**
     * Get the appropriate ResourceBundle subclass.
     * The baseName is extended by the Swing Look and Feel ID
     * and by the Locale code returned by Locale.getDefault().
     *
     * The default Look and Feel ID is Metal.
     *
     * @see java.util.ResourceBundle
     */
    public static QuaquaResourceBundleUtil getLAFBundle(String baseName)
    throws MissingResourceException {
        return getLAFBundle(baseName, Locale.getDefault());
    }
    /**
     * Get the appropriate ResourceBundle subclass.
     * The baseName is extended by the Swing Look and Feel ID
     * and by the Locale code.
     *
     * The default Look and Feel ID is Metal.
     *
     * @see java.util.ResourceBundle
     */
    public static QuaquaResourceBundleUtil getLAFBundle(String baseName, Locale locale)
    throws MissingResourceException {
        QuaquaResourceBundleUtil r;
        try {
            r = new QuaquaResourceBundleUtil(
            ResourceBundle.getBundle(
            baseName + "_" + UIManager.getLookAndFeel().getID(), locale
            )
            );
        } catch (MissingResourceException e) {
            r = new QuaquaResourceBundleUtil(
            ResourceBundle.getBundle(baseName, locale)
            );
        }
        return r;
    }
}
