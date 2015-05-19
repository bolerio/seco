/*
 * @(#)QuaquaViewportUI.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.util.ViewportPainter;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.event.*;

/**
 * The Quaqua user interface delegate for a JViewport.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaViewportUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaViewportUI extends BasicViewportUI {

    private ChangeListener changeListener;
    private ContainerListener containerListener;
    private FocusListener focusListener;
    private PropertyChangeListener propertyChangeListener;
    private JViewport viewport;

    public static ComponentUI createUI(JComponent c) {
        return new QuaquaViewportUI();
    }

    public void paint(Graphics g, JComponent c) {
        if (c.isOpaque()) {
            Component view = viewport.getView();
            Object ui = (view == null) ? null : Methods.invokeGetter(view, "getUI", null);
            if (ui instanceof ViewportPainter) {
                ((ViewportPainter) ui).paintViewport(g, viewport);
            } else {
                if (viewport.getView() != null) {
                    g.setColor(viewport.getView().getBackground());
                    g.fillRect(0, 0, c.getWidth(), c.getHeight());
                }
            }
        }
        Debug.paint(g, c, this);
    }

    public void installUI(JComponent c) {
        viewport = (JViewport) c;
        super.installUI(c);
        //c.setOpaque(QuaquaManager.getBoolean("Viewport.opaque"));
        QuaquaUtilities.installProperty(c, "opaque", UIManager.get("Viewport.opaque"));
        installListeners();
    }

    public void uninstallUI(JComponent c) {
        viewport = (JViewport) c;
        super.uninstallUI(c);
        uninstallListeners();
    }

    /**
     * Attaches listeners to the JTable.
     */
    protected void installListeners() {
        changeListener = createChangeListener();
        viewport.addChangeListener(changeListener);
        containerListener = createContainerListener();
        viewport.addContainerListener(containerListener);
        focusListener = createFocusListener();
        viewport.addFocusListener(focusListener);
        propertyChangeListener = createPropertyChangeListener();
        viewport.addPropertyChangeListener(propertyChangeListener);
        if (viewport.getView() != null) {
            viewport.getView().addFocusListener(focusListener);
        }
    }

    protected void uninstallListeners() {
        viewport.removeChangeListener(changeListener);
        viewport.removeContainerListener(containerListener);
        viewport.removeFocusListener(focusListener);
        viewport.removePropertyChangeListener(propertyChangeListener);
        changeListener = null;
        containerListener = null;

    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return new QuaquaPropertyChangeHandler();
    }

    private ChangeListener createChangeListener() {
        return new ChangeHandler();
    }

    private ContainerListener createContainerListener() {
        return new ContainerHandler();
    }

    private FocusListener createFocusListener() {
        return QuaquaFocusHandler.getInstance();
    }

    /**
     * We need to repaint the viewport if the location of a striped view
     * changes.
     */
    private class ChangeHandler implements ChangeListener {

        private Point previousLocation = new Point();

        public void stateChanged(ChangeEvent e) {

            if (viewport.getView() != null) {
                Component view = viewport.getView();

                Point newLocation = view.getLocation();
                if (!previousLocation.equals(newLocation)) {
                    if (view.getHeight() < viewport.getHeight()) {
                        if (newLocation.x > previousLocation.x) {
                            viewport.repaint(0, view.getHeight(), newLocation.x - previousLocation.x, viewport.getHeight() - view.getHeight());
                        }
                        if (newLocation.x < previousLocation.x) {
                            viewport.repaint(viewport.getWidth() + newLocation.x - previousLocation.x, view.getHeight(), previousLocation.x - newLocation.x, viewport.getHeight() - view.getHeight());
                        }
                    }
                    if (view.getWidth() < viewport.getWidth()) {
                        if (newLocation.y > previousLocation.y) {
                            viewport.repaint(view.getWidth(), 0, viewport.getWidth() - view.getWidth(), Math.min(view.getHeight(), newLocation.y - previousLocation.y));
                        }
                        if (newLocation.y < previousLocation.y) {
                            viewport.repaint(
                                    view.getWidth(),
                                    Math.min(view.getHeight(), viewport.getHeight()) - previousLocation.y + newLocation.y,
                                    viewport.getWidth() - view.getWidth(),
                                    previousLocation.y - newLocation.y);
                        }
                    }
                    previousLocation = newLocation;
                }
            }
        }
    }

    private class ContainerHandler implements ContainerListener {

        public void componentRemoved(ContainerEvent e) {
            e.getChild().removeFocusListener(focusListener);
        }

        public void componentAdded(ContainerEvent e) {
            e.getChild().addFocusListener(focusListener);
        }
    }

    public class QuaquaPropertyChangeHandler implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if ("Frame.active".equals(name)) {
                // we don't need to do anything here yet.
            } else if ("JComponent.sizeVariant".equals(name)) {
                QuaquaUtilities.applySizeVariant(viewport);
            }
        }
    }
}
