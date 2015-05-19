/*
 * @(#)FloatingPaletteHandler.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package test;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
/**
 * Hides all registered floating palettes, if none of the registered project
 * windows has focus anymore.
 * <p>
 * Usage: Register all project windows using method add(Window) with
 * the palette handler, and all palette windows using method addPalette(Dialog).  
 *
 * <p>FIXME - Remove WindowFocusListener from public API of this class.
 *
 * @author Werner Randelshofer
 * @version $Id: FloatingPaletteHandler.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class FloatingPaletteHandler implements WindowFocusListener {
    private HashSet palettes = new HashSet();
    private HashSet windows = new HashSet();
    private HashSet hiddenPalettes = new HashSet();
    private static FloatingPaletteHandler instance;
    private javax.swing.Timer timer;
    private Window currentWindow;
    
    /**
     * Returns FloatingPaletteHandler singleton.
     */
    public static FloatingPaletteHandler getInstance() {
        if (instance == null) {
            instance = new FloatingPaletteHandler();
        }
        return instance;
    }
    
    
    /** Creates a new instance. */
    public FloatingPaletteHandler() {
        timer = new javax.swing.Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                hidePalettes();
            }
        });
        timer.setRepeats(false);
    }
    
    
    /**
     * Registers a project window with the FloatingPaletteHandler.
     * <p>
     * When none of the registered windows has focus, the FloatingPaletteHandler
     * hides all registered palette windows.
     * When at least one of the registered windows has focus, the
     * FloatingPaletteHandler shows all registered palette windows.
     */
    public void add(Window window) {
        window.addWindowFocusListener(this);
        windows.add(window);
    }
    
    /**
     * Unregisters a project window with the FloatingPaletteHandler.
     */
    public void remove(Window window) {
        windows.remove(window);
        window.removeWindowFocusListener(this);
    }
    
    /**
     * Registers a palette window with the FloatingPaletteHandler.
     * <p>
     * The FloatingPaletteHandler shows and hides the palette windows depending
     * on the focused state of the registered project windows.
     */
    public void addPalette(JDialog palette) {
        palette.addWindowFocusListener(this);
        palettes.add(palette);
    }
    
    /**
     * Removes a project window from the FloatingPaletteHandler.
     */
    public void removePalette(JDialog palette) {
        palettes.remove(palette);
        palette.removeWindowFocusListener(this);
    }
    
    private void setCurrentWindow(Window newValue) {
        // XXX - firePropertyChangeEvent, so that the floating palettes can
        // register with FloatingPaletteHandler as a PropertyChangeListener
        // and update their contents.
        currentWindow = newValue;
    }
    /**
     * Returns the current applicatin window (the window which was the last to
     * gain focus). Floating palettes may use this method to determine on which
     * window they need to act on.
     */
    public Window getCurrentWindow() {
        return currentWindow;
    }
    
    /**
     * Invoked when the Window is set to be the focused Window, which means
     * that the Window, or one of its subcomponents, will receive keyboard
     * events.
     */
    public void windowGainedFocus(WindowEvent e) {
        timer.stop();
        if (windows.contains(e.getWindow())) {
            setCurrentWindow(e.getWindow());
            showPalettes();
        }
    }
    
    /**
     * Invoked when the Window is no longer the focused Window, which means
     * that keyboard events will no longer be delivered to the Window or any of
     * its subcomponents.
     */
    public void windowLostFocus(WindowEvent e) {
        timer.restart();
    }
    
    private void showPalettes() {
        for (Iterator i=hiddenPalettes.iterator(); i.hasNext(); ) {
            JDialog palette = (JDialog) i.next();
            palette.setVisible(true);
        }
        hiddenPalettes.clear();
    }
    
    private boolean isFocused(Window w) {
        if (w.isFocused()) return true;
        Window[] ownedWindows = w.getOwnedWindows();
        for (int i=0; i < ownedWindows.length; i++) {
            if (isFocused(ownedWindows[i])) {
                return true;
            }
        }
        return false;
    }
    private void hidePalettes() {
        boolean hasFocus = false;
        for (Iterator i=windows.iterator(); i.hasNext(); ) {
            Window window = (Window) i.next();
            if (isFocused(window)) {
                hasFocus = true;
                break;
            }
        }
        if (! hasFocus) {
            for (Iterator i=palettes.iterator(); i.hasNext(); ) {
                Dialog palette = (Dialog) i.next();
                if (isFocused(palette)) {
                    hasFocus = true;
                    break;
                }
            }
        }
        if (! hasFocus) {
            for (Iterator i=palettes.iterator(); i.hasNext(); ) {
            Dialog palette = (Dialog) i.next();
                if (palette.isVisible()) {
                    hiddenPalettes.add(palette);
                    palette.setVisible(false);
                }
            }
        }
    }
    
}
