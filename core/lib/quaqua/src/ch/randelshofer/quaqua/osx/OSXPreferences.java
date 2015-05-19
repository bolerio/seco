/*
 * @(#)OSXPreferences.java
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.osx;

import ch.randelshofer.quaqua.*;
import ch.randelshofer.quaqua.ext.nanoxml.*;
import ch.randelshofer.quaqua.util.BinaryPListParser;
import java.io.*;
import java.util.*;

/**
 * Utility class for accessing Mac OS X Preferences.
 *
 * @author  Werner Randelshofer
 * @version $Id: OSXPreferences.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class OSXPreferences {

    /** Path to global preferences. */
    public final static File GLOBAL_PREFERENCES = new File(QuaquaManager.getProperty("user.home"), "Library/Preferences/.GlobalPreferences.plist");
    /** Path to finder preferences. */
    public final static File FINDER_PREFERENCES = new File(QuaquaManager.getProperty("user.home"), "Library/Preferences/com.apple.finder.plist");
    /** Each entry in this hash map represents a cached preferences file. */
    private static HashMap<File, HashMap<String, Object>> cachedFiles;

    /**
     * Creates a new instance.
     */
    public OSXPreferences() {
    }

    public static String getString(File file, String key) {
        return (String) get(file, key);
    }

    public static String getString(File file, String key, String defaultValue) {
        return (String) get(file, key, defaultValue);
    }

    public static boolean isStringEqualTo(File file, String key, String defaultValue, String compareWithThisValue) {
        return ((String) get(file, key, defaultValue)).equals(compareWithThisValue);
    }
    
    public static Object get(File file, String key) {
        ensureCached(file);
        return cachedFiles.get(file).get(key);
    }

    /** Returns all known keys for the specified preferences file. */
    public static Set<String> getKeySet(File file) {
        ensureCached(file);
        return cachedFiles.get(file).keySet();
    }

    /** Clears all caches. */
    public static void clearAllCaches() {
        cachedFiles.clear();

    }

    /** Clears the cache for the specified preference file. */
    public static void clearCache(File f) {
        cachedFiles.remove(f);
    }

    /**
     * Get a value from a Mac OS X preferences file.
     * 
     * @param file The preferences file.
     * @param key Hierarchical keys are separated by \t characters.
     * @param defaultValue This value is returned when the key does not exist.
     * @return Returns the preferences value.
     */
    public static Object get(File file, String key, Object defaultValue) {
        ensureCached(file);
        return (cachedFiles.get(file).containsKey(key)) ? cachedFiles.get(file).get(key) : defaultValue;
    }

    private static void ensureCached(File file) {
        if (cachedFiles == null) {
            cachedFiles = new HashMap<File, HashMap<String, Object>>();
        }
        if (!cachedFiles.containsKey(file)) {
            HashMap<String, Object> cache = new HashMap<String, Object>();
            cachedFiles.put(file, cache);
            updateCache(file, cache);
        }
    }

    private static void updateCache(File file, HashMap<String, Object> cache) {
        cache.clear();

        if (QuaquaManager.isOSX()) {
            try {
                XMLElement plist = readPList(file);

                Stack<String> keyPath = new Stack<String>();
                readNode(plist, keyPath, cache);
            } catch (Throwable e) {
                System.err.println("Warning: ch.randelshofer.quaqua.util.OSXPreferences failed to load " + file);
                e.printStackTrace();
            }
        }
    }

    private static void readNode(XMLElement node, Stack<String> keyPath, HashMap<String, Object> cache) throws IOException {
        String name = node.getName();
        if (name.equals("plist")) {
            readPList(node, keyPath, cache);
        } else if (name.equals("dict")) {
            readDict(node, keyPath, cache);
        } else if (name.equals("array")) {
            readArray(node, keyPath, cache);
        } else {
            readValue(node, keyPath, cache);
        }
    }

    private static void readPList(XMLElement plist, Stack<String> keyPath, HashMap<String, Object> cache) throws IOException {
        ArrayList<XMLElement> children = plist.getChildren();
        for (int i = 0, n = children.size(); i < n; i++) {
            readNode(children.get(i), keyPath, cache);
        }
    }

    private static void readDict(XMLElement dict, Stack<String> keyPath, HashMap<String, Object> cache) throws IOException {
        ArrayList<XMLElement> children = dict.getChildren();
        for (int i = 0, n = children.size(); i < n; i += 2) {
            XMLElement keyElem = children.get(i);
            if (!keyElem.getName().equals("key")) {
                throw new IOException("missing dictionary key at" + keyPath);
            }
            keyPath.push(keyElem.getContent());
            readNode(children.get(i + 1), keyPath, cache);
            keyPath.pop();
        }
    }

    private static void readArray(XMLElement array, Stack<String> keyPath, HashMap<String, Object> cache) throws IOException {
        ArrayList<XMLElement> children = array.getChildren();
        for (int i = 0, n = children.size(); i < n; i++) {
            keyPath.push(Integer.toString(i));
            readNode(children.get(i), keyPath, cache);
            keyPath.pop();
        }
    }

    private static void readValue(XMLElement value, Stack<String> keyPath, HashMap<String, Object> cache) throws IOException {
        StringBuffer key = new StringBuffer();
        for (Iterator<String> i = keyPath.iterator(); i.hasNext();) {
            key.append(i.next());
            if (i.hasNext()) {
                key.append('\t');
            }
        }
        cache.put(key.toString(), value.getContent());
    }

    /**
     * Reads the specified PList file and returns it as an XMLElement.
     * This method can deal with XML encoded and binary encoded PList files.
     */
    private static XMLElement readPList(File plistFile) throws IOException {
        FileReader reader = null;
        XMLElement xml = null;
        try {
            reader = new FileReader(plistFile);
            xml = new XMLElement(new HashMap(), false, false);
            try {
                xml.parseFromReader(reader);
            } catch (XMLParseException e) {
                xml = new BinaryPListParser().parse(plistFile);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return xml;
    }
}

