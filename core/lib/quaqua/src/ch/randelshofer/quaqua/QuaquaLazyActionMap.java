/**
 * @(#)QuaquaLazyActionMap.java  
 *
 * Copyright (c) 2008-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.plaf.*;

/**
 * QuaquaLazyActionMap.
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaLazyActionMap.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaLazyActionMap extends ActionMapUIResource {
    /**
     * Object to invoke <code>loadActionMap</code> on. This may be
     * a Class object.
     */
    private transient Object _loader;

    /**
     * Installs an ActionMap that will be populated by invoking the
     * <code>loadActionMap</code> method on the specified Class
     * when necessary.
     * <p>
     * This should be used if the ActionMap can be shared.
     *
     * @param c JComponent to install the ActionMap on.
     * @param loaderClass Class object that gets loadActionMap invoked
     *                    on.
     * @param defaultsKey Key to use to defaults table to check for
     *        existing map and what resulting Map will be registered on.
     */
    static void installLazyActionMap(JComponent c, Class loaderClass,
                                     String defaultsKey) {
        ActionMap map = (ActionMap)UIManager.get(defaultsKey);
        if (map == null) {
            map = new QuaquaLazyActionMap(loaderClass);
            UIManager.getLookAndFeelDefaults().put(defaultsKey, map);
        }
        SwingUtilities.replaceUIActionMap(c, map);
    }

    /**
     * Returns an ActionMap that will be populated by invoking the
     * <code>loadActionMap</code> method on the specified Class
     * when necessary.
     * <p>
     * This should be used if the ActionMap can be shared.
     *
     * @param c JComponent to install the ActionMap on.
     * @param loaderClass Class object that gets loadActionMap invoked
     *                    on.
     * @param defaultsKey Key to use to defaults table to check for
     *        existing map and what resulting Map will be registered on.
     */
    static ActionMap getActionMap(Class loaderClass,
                                  String defaultsKey) {
        ActionMap map = (ActionMap)UIManager.get(defaultsKey);
        if (map == null) {
            map = new QuaquaLazyActionMap(loaderClass);
         UIDefaults uide=   UIManager.getLookAndFeelDefaults();
         if (uide!=null) uide.put(defaultsKey, map);
        }
        return map;
    }


    public QuaquaLazyActionMap(Class loader) {
        _loader = loader;
    }

    public void put(Action action) {
        put(action.getValue(Action.NAME), action);
    }

    @Override
    public void put(Object key, Action action) {
        loadIfNecessary();
        super.put(key, action);
    }

    @Override
    public Action get(Object key) {
        loadIfNecessary();
        return super.get(key);
    }

    @Override
    public void remove(Object key) {
        loadIfNecessary();
        super.remove(key);
    }

    @Override
    public void clear() {
        loadIfNecessary();
        super.clear();
    }

    @Override
    public Object[] keys() {
        loadIfNecessary();
        return super.keys();
    }

    @Override
    public int size() {
        loadIfNecessary();
        return super.size();
    }

    @Override
    public Object[] allKeys() {
        loadIfNecessary();
        return super.allKeys();
    }

    @Override
    public void setParent(ActionMap map) {
        loadIfNecessary();
        super.setParent(map);
    }

    private void loadIfNecessary() {
        if (_loader != null) {
            Object loader = _loader;

            _loader = null;
            Class klass = (Class)loader;
            try {
                Method method = klass.getDeclaredMethod("loadActionMap",
                                      new Class[] { QuaquaLazyActionMap.class });
                method.invoke(klass, new Object[] { this });
            } catch (NoSuchMethodException nsme) {
                assert false : "LazyActionMap unable to load actions " +
                        klass;
            } catch (IllegalAccessException iae) {
                assert false : "LazyActionMap unable to load actions " +
                        iae;
            } catch (InvocationTargetException ite) {
                assert false : "LazyActionMap unable to load actions " +
                        ite;
            } catch (IllegalArgumentException iae) {
                assert false : "LazyActionMap unable to load actions " +
                        iae;
            }
        }
    }
}
