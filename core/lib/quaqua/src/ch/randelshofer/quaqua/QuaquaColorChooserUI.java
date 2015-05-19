/*
 * @(#)QuaquaColorChooserUI.java 
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.colorchooser.*;
import ch.randelshofer.quaqua.util.Methods;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.colorchooser.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import java.security.*;
import java.util.*;

/**
 * QuaquaColorChooserUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaColorChooserUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaColorChooserUI extends ColorChooserUI {

    protected ColorChooserMainPanel mainPanel;
    protected JColorChooser chooser;
    protected ChangeListener previewListener;
    protected PropertyChangeListener propertyChangeListener;
    protected AbstractColorChooserPanel[] defaultChoosers;
    protected JComponent previewPanel;
    private static TransferHandler defaultTransferHandler = new ColorTransferHandler();
    private MouseListener previewMouseListener;

    public static ComponentUI createUI(JComponent c) {
        return new QuaquaColorChooserUI();
    }

    @Override
    public void installUI(JComponent c) {
        chooser = (JColorChooser) c;
        AbstractColorChooserPanel[] oldPanels = chooser.getChooserPanels();

        installDefaults();

        chooser.setLayout(new BorderLayout());
        mainPanel = new ColorChooserMainPanel();
        chooser.add(mainPanel);
        defaultChoosers = createDefaultChoosers();
        chooser.setChooserPanels(defaultChoosers);

        installPreviewPanel();
        AbstractColorChooserPanel[] newPanels = (AbstractColorChooserPanel[]) chooser.getChooserPanels();
        updateColorChooserPanels(oldPanels, newPanels);

        // Note: install listeners only after we have fully installed
        //       all chooser panels. If we do it earlier, we send property
        //       events too early.
        installListeners();

        chooser.applyComponentOrientation(c.getComponentOrientation());
    }

    protected AbstractColorChooserPanel[] createDefaultChoosers() {
        String[] defaultChooserNames = (String[]) UIManager.get("ColorChooser.defaultChoosers");
        ArrayList panels = new ArrayList(defaultChooserNames.length);
        for (int i = 0; i < defaultChooserNames.length; i++) {
            try {

                //panels.add(Class.forName(defaultChoosers[i]).newInstance());
                panels.add(Methods.newInstance(defaultChooserNames[i]));
            } catch (AccessControlException e) {
                // suppress
                System.err.println("QuaquaColorChooserUI warning: unable to instantiate " + defaultChooserNames[i]);
                e.printStackTrace();
            } catch (Exception e) {
                // throw new InternalError("Unable to instantiate "+defaultChoosers[i]);
                // suppress
                System.err.println("QuaquaColorChooserUI warning: unable to instantiate " + defaultChooserNames[i]);
                e.printStackTrace();
            } catch (UnsupportedClassVersionError e) {
                // suppress
                System.err.println("QuaquaColorChooserUI warning: unable to instantiate " + defaultChooserNames[i]);
                //e.printStackTrace();
            } catch (Throwable t) {
                System.err.println("QuaquaColorChooserUI warning: unable to instantiate " + defaultChooserNames[i]);
            }
        }
        //AbstractColorChooserPanel[] panels = new AbstractColorChooserPanel[defaultChoosers.length];
        return (AbstractColorChooserPanel[]) panels.toArray(new AbstractColorChooserPanel[panels.size()]);
    }

    @Override
    public void uninstallUI(JComponent c) {
        chooser.remove(mainPanel);

        uninstallListeners();
        uninstallDefaultChoosers();
        uninstallDefaults();

        mainPanel.setPreviewPanel(null);
        if (previewPanel instanceof UIResource) {
            chooser.setPreviewPanel(null);
        }

        mainPanel = null;
        previewPanel = null;
        defaultChoosers = null;
        chooser = null;
    }

    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(chooser, "ColorChooser.background",
                "ColorChooser.foreground",
                "ColorChooser.font");
        TransferHandler th = chooser.getTransferHandler();
        if (th == null || th instanceof UIResource) {
            chooser.setTransferHandler(defaultTransferHandler);
        }
    }

    protected void uninstallDefaults() {
        if (chooser.getTransferHandler() instanceof UIResource) {
            chooser.setTransferHandler(null);
        }
    }

    protected void installListeners() {
        propertyChangeListener = createPropertyChangeListener();
        chooser.addPropertyChangeListener(propertyChangeListener);

        previewListener = new PreviewListener();
        chooser.getSelectionModel().addChangeListener(previewListener);

        previewMouseListener = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (chooser.getDragEnabled()) {
                    TransferHandler th = chooser.getTransferHandler();
                    th.exportAsDrag(chooser, e, TransferHandler.COPY);
                }
            }
        };
    }

    protected void uninstallListeners() {
        chooser.removePropertyChangeListener(propertyChangeListener);
        chooser.getSelectionModel().removeChangeListener(previewListener);

        previewPanel.removeMouseListener(previewMouseListener);
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return new PropertyHandler();
    }

    protected void installPreviewPanel() {
        if (previewPanel != null) {
            previewPanel.removeMouseListener(previewMouseListener);
            mainPanel.setPreviewPanel(null);
        }

        previewPanel = chooser.getPreviewPanel();

        // If the previewPanel has zero size, the user wants to not display
        // a preview at all.
        // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5029286
        Dimension layoutSize; // fix for bug 4759306
        if (previewPanel != null) {
            layoutSize = new BorderLayout().minimumLayoutSize(previewPanel);
            if (layoutSize.getWidth() + layoutSize.getHeight() == 0) {
              return;
            }
        }


        // If the previewPanel is null or an UIResource, the user wants to
        // display the previewPanel from the look and fell.
        if (previewPanel == null || previewPanel instanceof UIResource) {
            //previewPanel = ColorChooserComponentFactory.getPreviewPanel(); // get from table?
            previewPanel = new QuaquaColorPreviewPanel();
            chooser.setPreviewPanel(previewPanel);
        }

        // 
        previewPanel.setForeground(chooser.getColor());
        mainPanel.setPreviewPanel(previewPanel);
        previewPanel.addMouseListener(previewMouseListener);
    }

    class PreviewListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            ColorSelectionModel model = (ColorSelectionModel) e.getSource();
            if (previewPanel != null) {
                previewPanel.setForeground(model.getSelectedColor());
                previewPanel.repaint();
            }
        }
    }

    protected void uninstallDefaultChoosers() {
        for (int i = 0; i < defaultChoosers.length; i++) {
            chooser.removeChooserPanel(defaultChoosers[i]);
        }
    }

    private void updateColorChooserPanels(
            AbstractColorChooserPanel[] oldPanels,
            AbstractColorChooserPanel[] newPanels) {
        for (int i = 0; i < oldPanels.length; i++) {  // remove old panels
            Container wrapper = oldPanels[i].getParent();
            if (wrapper != null) {
                Container parent = wrapper.getParent();
                if (parent != null) {
                    parent.remove(wrapper);  // remove from hierarchy
                }
                oldPanels[i].uninstallChooserPanel(chooser); // uninstall
            }
        }

        mainPanel.removeAllColorChooserPanels();
        for (int i = 0; i < newPanels.length; i++) {
            if (newPanels[i] != null) {
                mainPanel.addColorChooserPanel(newPanels[i]);
            }
        }

        for (int i = 0; i < newPanels.length; i++) {
            if (newPanels[i] != null) {
                newPanels[i].installChooserPanel(chooser);
            }
        }
    }

    public class PropertyHandler implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if (name.equals(JColorChooser.CHOOSER_PANELS_PROPERTY)) {
                AbstractColorChooserPanel[] oldPanels = (AbstractColorChooserPanel[]) e.getOldValue();
                AbstractColorChooserPanel[] newPanels = (AbstractColorChooserPanel[]) e.getNewValue();

                for (int i = 0; i < oldPanels.length; i++) {  // remove old panels
                    if (oldPanels[i] != null) {
                        Container wrapper = oldPanels[i].getParent();
                        if (wrapper != null) {
                            Container parent = wrapper.getParent();
                            if (parent != null) {
                                parent.remove(wrapper);  // remove from hierarchy
                            }
                            oldPanels[i].uninstallChooserPanel(chooser); // uninstall
                        }
                    }
                }

                mainPanel.removeAllColorChooserPanels();
                for (int i = 0; i < newPanels.length; i++) {
                    if (newPanels[i] != null) {
                        mainPanel.addColorChooserPanel(newPanels[i]);
                    }
                }

                chooser.applyComponentOrientation(chooser.getComponentOrientation());
                for (int i = 0; i < newPanels.length; i++) {
                    if (newPanels[i] != null) {
                        newPanels[i].installChooserPanel(chooser);
                    }
                }
            }
            if (name.equals(JColorChooser.PREVIEW_PANEL_PROPERTY)) {
                if (e.getNewValue() != previewPanel) {
                    installPreviewPanel();
                }
            }
            if (name.equals("componentOrientation")) {
                ComponentOrientation o = (ComponentOrientation) e.getNewValue();
                JColorChooser cc = (JColorChooser) e.getSource();
                if (o != (ComponentOrientation) e.getOldValue()) {
                    cc.applyComponentOrientation(o);
                    cc.updateUI();
                }
            }
        }
    }

    static class ColorTransferHandler extends TransferHandler implements UIResource {

        ColorTransferHandler() {
            super("color");
        }
    }
}
