/*
 * @(#)QuaquaButtonListener.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.basic.*;
import java.beans.*;
import java.util.Enumeration;

/**
 * QuaquaButtonListener.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaButtonListener.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaButtonListener extends BasicButtonListener {

    transient long lastPressedTimestamp = -1;
    transient boolean shouldDiscardRelease = false;

    /** Creates a new instance. */
    public QuaquaButtonListener(AbstractButton button) {
        super(button);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (e.getSource() instanceof AbstractButton) {
            AbstractButton btn = ((AbstractButton) e.getSource());
            if (prop==null||
                    prop.equals("Frame.active")||
                    prop.equals("Quaqua.Button.type")||
                    prop.equals("JButton.buttonType")||
                    prop.equals("JButton.segmentPosition")||
                    prop.equals("JComponent.sizeVariant")) {
                btn.repaint();
            }
        }
        super.propertyChange(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            AbstractButton b = (AbstractButton) e.getSource();

            if (b.contains(e.getX(), e.getY())) {
                long multiClickThreshhold = Methods.invokeGetter(b, "getMultiClickThreshhold", (long) 0);
                long lastTime = lastPressedTimestamp;
                long currentTime = lastPressedTimestamp = e.getWhen();
                if (lastTime != -1 && currentTime - lastTime < multiClickThreshhold) {
                    shouldDiscardRelease = true;
                    return;
                }

                ButtonModel model = b.getModel();
                if (!model.isEnabled()) {
                    // Disabled buttons ignore all input...
                    return;
                }
                if (!model.isArmed()) {
                    // button not armed, should be
                    model.setArmed(true);
                }
                if (!b.hasFocus()) {
                    if (b.isRequestFocusEnabled()) {
                        b.requestFocus();
                    } else {
                        // request focus if one of the buttons in the button group
                        // has focus
                        if (model instanceof DefaultButtonModel) {
                            ButtonGroup grp = ((DefaultButtonModel) model).getGroup();
                            if (grp != null) {
                                for (Enumeration i = grp.getElements(); i.hasMoreElements();) {
                                    AbstractButton grpButton = (AbstractButton) i.nextElement();
                                    if (grpButton.hasFocus()) {
                                        b.setFocusable(true);
                                        b.requestFocus();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                // Update model after focus changes have been requested, so that
                // model can request focus changes of its own.
                model.setPressed(true);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            // Support for multiClickThreshhold
            if (shouldDiscardRelease) {
                shouldDiscardRelease = false;
                return;
            }
            AbstractButton b = (AbstractButton) e.getSource();
            ButtonModel model = b.getModel();
            model.setPressed(false);
            model.setArmed(false);

            // restore focus in the button group
            if (!model.isSelected() && b.hasFocus()) {
                if (model instanceof DefaultButtonModel) {
                    ButtonGroup grp = ((DefaultButtonModel) model).getGroup();
                    if (grp != null) {
                        boolean groupHasFocus = false;
                        for (Enumeration i = grp.getElements(); i.hasMoreElements();) {
                            AbstractButton grpButton = (AbstractButton) i.nextElement();
                            if (grpButton.isSelected()) {
                                grpButton.requestFocus();
                                break;
                            }
                        }
                        b.setFocusable(false);
                    }
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        ButtonModel model = b.getModel();

        if (b.isRolloverEnabled()) {
            model.setRollover(true);
        }
        if (model.isPressed()) {
            model.setArmed(true);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        ButtonModel model = b.getModel();

        if (b.isRolloverEnabled()) {
            model.setRollover(false);
        }
        model.setArmed(false);
    }

    @Override
    public void focusGained(FocusEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();

        if (b instanceof JButton && ((JButton) b).isDefaultCapable()) {
            JRootPane root = b.getRootPane();

            if (root != null) {
                QuaquaButtonUI ui = (QuaquaButtonUI) QuaquaUtilities.getUIOfType(
                        ((AbstractButton) b).getUI(), QuaquaButtonUI.class);

                if (ui != null
                        && UIManager.get(ui.getPropertyPrefix() + "defaultButtonFollowsFocus") != Boolean.FALSE) {
                    root.putClientProperty("temporaryDefaultButton", b);
                    root.setDefaultButton((JButton) b);
                    root.putClientProperty("temporaryDefaultButton", null);
                }
            }
        }
        b.repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        JRootPane root = b.getRootPane();

        if (root != null) {
            JButton initialDefault = (JButton) root.getClientProperty("initialDefaultButton");

            if (b != initialDefault) {
                QuaquaButtonUI ui = (QuaquaButtonUI) QuaquaUtilities.getUIOfType(
                        ((AbstractButton) b).getUI(), QuaquaButtonUI.class);

                if (ui != null
                        && UIManager.get(ui.getPropertyPrefix() + "defaultButtonFollowsFocus") != Boolean.FALSE) {
                    root.setDefaultButton(initialDefault);
                }
            }
        }

        b.getModel().setArmed(false);
        b.repaint();
    }

    /**
     * Populates Buttons actions.
     */
    static void loadActionMap(QuaquaLazyActionMap map) {
        map.put(new Actions(Actions.PRESS));
        map.put(new Actions(Actions.RELEASE));
        map.put(new Actions(Actions.SELECT_NEXT_BUTTON));
        map.put(new Actions(Actions.SELECT_PREVIOUS_BUTTON));
    }

    /** Keyboard action for selecting the next/previous button in a radio
     * button group.
     */
    private static class Actions extends AbstractAction {

        private static final String PRESS = "pressed";
        private static final String RELEASE = "released";
        private static final String SELECT_NEXT_BUTTON = "selectNextButton";
        private static final String SELECT_PREVIOUS_BUTTON = "selectPreviousButton";

        public Actions(String name) {
            super(name);
        }

        public String getName() {
            return (String) getValue(Action.NAME);
        }

        public void actionPerformed(ActionEvent e) {
            AbstractButton b = (AbstractButton) e.getSource();
            String key = getName();
            if (key == PRESS) {
                ButtonModel model = b.getModel();
                model.setArmed(true);
                model.setPressed(true);
                if (!b.hasFocus()) {
                    b.requestFocus();
                }
            } else if (key == RELEASE) {
                ButtonModel model = b.getModel();
                model.setPressed(false);
                model.setArmed(false);
            } else if (key == SELECT_NEXT_BUTTON) {
                ButtonModel model = b.getModel();
                if (model instanceof DefaultButtonModel) {
                    DefaultButtonModel defaultButtonModel = (DefaultButtonModel) model;
                    ButtonGroup group = defaultButtonModel.getGroup();
                    if (group != null) {
                        AbstractButton btn = getSiblingButton(group, true);
                        if (btn != null) {
                            // Invoke doClick before requesting focus in window,
                            // so that focus stays on button regardless of what
                            // listeners do.
                            btn.doClick();
                            btn.requestFocusInWindow();
                        }
                    }
                }
            } else if (key == SELECT_PREVIOUS_BUTTON) {
                ButtonModel model = b.getModel();
                if (model instanceof DefaultButtonModel) {
                    DefaultButtonModel defaultButtonModel = (DefaultButtonModel) model;
                    ButtonGroup group = defaultButtonModel.getGroup();
                    if (group != null) {
                        AbstractButton btn = getSiblingButton(group, false);
                        if (btn != null) {
                            // Invoke doClick before requesting focus in window,
                            // so that focus stays on button regardless of what
                            // listeners do.
                            btn.doClick();
                            btn.requestFocusInWindow();
                        }
                    }
                }
            }
        }

        public boolean isEnabled(Object sender) {
            if (sender != null && (sender instanceof AbstractButton)
                    && !((AbstractButton) sender).getModel().isEnabled()) {
                return false;
            } else {
                return true;
            }
        }

        private AbstractButton getSiblingButton(ButtonGroup group, boolean isSelectNext) {
            AbstractButton adjacentToSelected = null;
            AbstractButton adjacentToFocused = null;
            if (isSelectNext) {
                boolean takeNextSelected = false;
                boolean takeNextFocused = false;
                for (Enumeration i = group.getElements(); i.hasMoreElements();) {
                    final AbstractButton buttonInGroup = (AbstractButton) i.nextElement();
                    if (takeNextSelected && buttonInGroup.isEnabled()) {
                        adjacentToSelected = buttonInGroup;
                        takeNextSelected = false;
                    }
                    if (takeNextFocused && buttonInGroup.isEnabled()) {
                        adjacentToFocused = buttonInGroup;
                        takeNextFocused = false;
                    }

                    if (buttonInGroup.isSelected()) {
                        takeNextSelected = true;
                    }
                    if (buttonInGroup.isFocusOwner()) {
                        takeNextFocused = true;
                    }
                }
            } else {
                AbstractButton previousButton = null;
                for (Enumeration i = group.getElements(); i.hasMoreElements();) {
                    final AbstractButton buttonInGroup = (AbstractButton) i.nextElement();
                    if (buttonInGroup.isSelected()) {
                        adjacentToSelected = previousButton;
                    }
                    if (buttonInGroup.isFocusOwner()) {
                        adjacentToFocused = previousButton;
                    }

                    if (buttonInGroup.isEnabled()) {
                        previousButton = buttonInGroup;
                    }
                }
            }
            return (adjacentToFocused == null) ? adjacentToSelected : adjacentToFocused;
        }
    }
}
