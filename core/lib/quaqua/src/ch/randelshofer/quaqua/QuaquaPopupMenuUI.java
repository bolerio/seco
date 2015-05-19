/*
 * @(#)QuaquaPopupMenuUI.java
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.color.PaintableColor;
import ch.randelshofer.quaqua.util.Methods;
import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import sun.awt.AppContext;
import sun.swing.UIAction;

/**
 * QuaquaPopupMenuUI.
 *
 * @author Werner Randelshofer
 * @version $Id: QuaquaPopupMenuUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaPopupMenuUI extends BasicPopupMenuUI implements QuaquaMenuPainterClient {

    public final static String WINDOW_ALPHA_PROPERTY = "Quaqua.PopupMenu.windowAlpha";
    static final StringBuilder MOUSE_GRABBER_KEY = new StringBuilder(
            "javax.swing.plaf.basic.BasicPopupMenuUI.MouseGrabber");
    static final StringBuilder MENU_KEYBOARD_HELPER_KEY = new StringBuilder(
            "javax.swing.plaf.basic.BasicPopupMenuUI.MenuKeyboardHelper");
    private transient PopupMenuListener popupMenuListener = null;
    private MenuKeyListener menuKeyListener = null;
    /**
     * Special mask for the UngrabEvent events, in addition to the
     * public masks defined in AWTEvent.  Should be used as the mask
     * value for Toolkit.addAWTEventListener.
     * Copied here, because sun.awt.SunToolkit.GRAB_EVENT_MASK is only available
     * from J2SE6 onwards.
     */
    public static final int GRAB_EVENT_MASK = 0x80000000;

    public static ComponentUI createUI(JComponent x) {
        return new QuaquaPopupMenuUI();
    }

    public QuaquaPopupMenuUI() {
    }

    @Override
    protected void installListeners() {
        if (popupMenuListener == null) {
            popupMenuListener = new QuaquaPopupMenuListener();
        }
        popupMenu.addPopupMenuListener(popupMenuListener);

        if (menuKeyListener == null) {
            menuKeyListener = new QuaquaMenuKeyListener();
        }
        popupMenu.addMenuKeyListener(menuKeyListener);

    }

    @Override
    protected void uninstallListeners() {
        if (popupMenuListener != null) {
            popupMenu.removePopupMenuListener(popupMenuListener);
        }
        if (menuKeyListener != null) {
            popupMenu.removeMenuKeyListener(menuKeyListener);
        }
    }

    public void paintBackground(Graphics g, JComponent component, int menuWidth, int menuHeight) {
        Color bgColor = UIManager.getColor("PopupMenu.selectionBackground");
        AbstractButton menuItem = (AbstractButton) component;
        ButtonModel model = menuItem.getModel();
        Color oldColor = g.getColor();

        if (menuItem.isOpaque()) {
            if (model.isArmed() || model.isSelected()) {
                ((Graphics2D) g).setPaint(PaintableColor.getPaint(bgColor, component));
                g.fillRect(0, 0, menuWidth, menuHeight);
            } else {
                ((Graphics2D) g).setPaint(PaintableColor.getPaint(menuItem.getBackground(), component));
                g.fillRect(0, 0, menuWidth, menuHeight);
            }
            g.setColor(oldColor);
        }
    }

    static ActionMap getActionMap() {
        return QuaquaLazyActionMap.getActionMap(QuaquaPopupMenuUI.class,
                "PopupMenu.actionMap");
    }

    static void loadActionMap(QuaquaLazyActionMap map) {
        map.put(new Actions(Actions.CANCEL));
        map.put(new Actions(Actions.SELECT_NEXT));
        map.put(new Actions(Actions.SELECT_PREVIOUS));
        map.put(new Actions(Actions.SELECT_PARENT));
        map.put(new Actions(Actions.SELECT_CHILD));
        map.put(new Actions(Actions.RETURN));
        //BasicLookAndFeel.installAudioActionMap(map);
    }

    static InputMap getInputMap(JPopupMenu popup, JComponent c) {
        InputMap windowInputMap = null;
        Object[] bindings = (Object[]) UIManager.get("PopupMenu.selectedWindowInputMapBindings");
        if (bindings != null) {
            windowInputMap = LookAndFeel.makeComponentInputMap(c, bindings);
            if (!popup.getComponentOrientation().isLeftToRight()) {
                Object[] km = (Object[]) UIManager.get("PopupMenu.selectedWindowInputMapBindings.RightToLeft");
                if (km != null) {
                    InputMap rightToLeftInputMap = LookAndFeel.makeComponentInputMap(c, km);
                    rightToLeftInputMap.setParent(windowInputMap);
                    windowInputMap = rightToLeftInputMap;
                }
            }
        }
        return windowInputMap;
    }

    static MenuElement getFirstPopup() {
        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        MenuElement[] p = msm.getSelectedPath();
        MenuElement me = null;

        for (int i = 0; me == null && i < p.length; i++) {
            if (p[i] instanceof JPopupMenu) {
                me = p[i];
            }
        }

        return me;
    }

    static JPopupMenu getLastPopup() {
        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        MenuElement[] p = msm.getSelectedPath();
        JPopupMenu popup = null;

        for (int i = p.length - 1; popup == null && i >= 0; i--) {
            if (p[i] instanceof JPopupMenu) {
                popup = (JPopupMenu) p[i];
            }
        }
        return popup;
    }

    static List getPopups() {
        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        MenuElement[] p = msm.getSelectedPath();

        List list = new ArrayList(p.length);
        for (int i = 0; i < p.length; i++) {
            if (p[i] instanceof JPopupMenu) {
                list.add((JPopupMenu) p[i]);
            }
        }
        return list;
    }

    private static MenuElement nextEnabledChild(MenuElement e[],
            int fromIndex, int toIndex) {
        for (int i = fromIndex; i <= toIndex; i++) {
            if (e[i] != null) {
                Component comp = e[i].getComponent();
                if (comp != null
                        && (comp.isEnabled() || UIManager.getBoolean("MenuItem.disabledAreNavigable"))
                        && comp.isVisible()) {
                    return e[i];
                }
            }
        }
        return null;
    }

    private static MenuElement previousEnabledChild(MenuElement e[],
            int fromIndex, int toIndex) {
        for (int i = fromIndex; i >= toIndex; i--) {
            if (e[i] != null) {
                Component comp = e[i].getComponent();
                if (comp != null
                        && (comp.isEnabled() || UIManager.getBoolean("MenuItem.disabledAreNavigable"))
                        && comp.isVisible()) {
                    return e[i];
                }
            }
        }
        return null;
    }

    static MenuElement findEnabledChild(MenuElement e[], int fromIndex,
            boolean forward) {
        MenuElement result = null;
        if (forward) {
            result = nextEnabledChild(e, fromIndex + 1, e.length - 1);
            if (result == null) {
                result = nextEnabledChild(e, 0, fromIndex - 1);
            }
        } else {
            result = previousEnabledChild(e, fromIndex - 1, 0);
            if (result == null) {
                result = previousEnabledChild(e, e.length - 1,
                        fromIndex + 1);
            }
        }
        return result;
    }

    static MenuElement findEnabledChild(MenuElement e[],
            MenuElement elem, boolean forward) {
        for (int i = 0; i < e.length; i++) {
            if (e[i] == elem) {
                return findEnabledChild(e, i, forward);
            }
        }
        return null;
    }

    private static boolean checkInvokerEqual(MenuElement present, MenuElement last) {
        Component invokerPresent = present.getComponent();
        Component invokerLast = last.getComponent();

        if (invokerPresent instanceof JPopupMenu) {
            invokerPresent = ((JPopupMenu) invokerPresent).getInvoker();
        }
        if (invokerLast instanceof JPopupMenu) {
            invokerLast = ((JPopupMenu) invokerLast).getInvoker();
        }
        return (invokerPresent == invokerLast);
    }

    /**
     * Returns the <code>Popup</code> that will be responsible for
     * displaying the <code>JPopupMenu</code>.
     *
     * @param popup JPopupMenu requesting Popup
     * @param x     Screen x location Popup is to be shown at
     * @param y     Screen y location Popup is to be shown at.
     * @return Popup that will show the JPopupMenu
     * @since 1.4
     *//*
    @Override
    public Popup getPopup(JPopupMenu popup, int x, int y) {
    PopupFactory popupFactory = PopupFactory.getSharedInstance();
    return popupFactory.getPopup(popup.getInvoker(), popup, x, y);
    }*/

    static class MouseGrabber implements ChangeListener,
            AWTEventListener, ComponentListener, WindowListener {

        Window grabbedWindow;
        MenuElement[] lastPathSelected;

        public MouseGrabber() {
            MenuSelectionManager msm = MenuSelectionManager.defaultManager();
            msm.addChangeListener(this);
            this.lastPathSelected = msm.getSelectedPath();
            if (this.lastPathSelected.length != 0) {
                grabWindow(this.lastPathSelected);
            }
        }

        public void uninstall() {
            synchronized (QuaquaPopupMenuUI.MOUSE_GRABBER_KEY) {
                MenuSelectionManager.defaultManager().removeChangeListener(this);
                ungrabWindow();
                AppContext.getAppContext().remove(MOUSE_GRABBER_KEY);
            }
        }

        void grabWindow(MenuElement[] newPath) {
            // A grab needs to be added
            final Toolkit tk = Toolkit.getDefaultToolkit();
            java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {

                        public Object run() {
                            tk.addAWTEventListener(MouseGrabber.this,
                                    AWTEvent.MOUSE_EVENT_MASK
                                    | AWTEvent.MOUSE_MOTION_EVENT_MASK
                                    | AWTEvent.MOUSE_WHEEL_EVENT_MASK
                                    | AWTEvent.WINDOW_EVENT_MASK | GRAB_EVENT_MASK);
                            return null;
                        }
                    });

            Component invoker = newPath[0].getComponent();
            if (invoker instanceof JPopupMenu) {
                invoker = ((JPopupMenu) invoker).getInvoker();
            }
            grabbedWindow = invoker instanceof Window
                    ? (Window) invoker
                    : SwingUtilities.getWindowAncestor(invoker);
            if (grabbedWindow != null) {
                if (tk instanceof sun.awt.SunToolkit) {
                    try {
                        //((sun.awt.SunToolkit) tk).grab(grabbedWindow);
                        Methods.invoke(tk, "grab", new Class[]{Window.class}, new Object[]{grabbedWindow});
                    } catch (NoSuchMethodException ex) {
                        grabbedWindow.addComponentListener(this);
                        grabbedWindow.addWindowListener(this);
                    }
                } else {
                    grabbedWindow.addComponentListener(this);
                    grabbedWindow.addWindowListener(this);
                }
            }
        }

        void ungrabWindow() {
            final Toolkit tk = Toolkit.getDefaultToolkit();
            // The grab should be removed
            java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {

                        public Object run() {
                            tk.removeAWTEventListener(MouseGrabber.this);
                            return null;
                        }
                    });
            if (grabbedWindow != null) {
                if (tk instanceof sun.awt.SunToolkit) {
                    try {
                        //((sun.awt.SunToolkit) tk).ungrab(grabbedWindow);
                        Methods.invoke(tk, "ungrab", new Class[]{Window.class}, new Object[]{grabbedWindow});
                    } catch (NoSuchMethodException ex) {
                        grabbedWindow.removeComponentListener(this);
                        grabbedWindow.removeWindowListener(this);
                    }
                } else {
                    grabbedWindow.removeComponentListener(this);
                    grabbedWindow.removeWindowListener(this);
                }
                grabbedWindow = null;
            }
        }

        public void stateChanged(ChangeEvent e) {
            MenuSelectionManager msm = MenuSelectionManager.defaultManager();
            MenuElement[] p = msm.getSelectedPath();

            if (lastPathSelected.length == 0 && p.length != 0) {
                grabWindow(p);
            }

            if (lastPathSelected.length != 0 && p.length == 0) {
                ungrabWindow();
            }

            lastPathSelected = p;
        }

        public void eventDispatched(AWTEvent ev) {
            if (Methods.instanceOf(ev, "sun.awt.UngrabEvent")) {
                // Popup should be canceled in case of ungrab event
                cancelPopupMenu();
                return;
            }
            if (!(ev instanceof MouseEvent)) {
                // We are interested in MouseEvents only
                return;
            }
            MouseEvent me = (MouseEvent) ev;
            Component src = me.getComponent();
            switch (me.getID()) {
                case MouseEvent.MOUSE_PRESSED:
                    if (isInPopup(src)
                            || (src instanceof JMenu && ((JMenu) src).isSelected())) {
                        return;
                    }
                    if (!(src instanceof JComponent)
                            || !(((JComponent) src).getClientProperty("doNotCancelPopup") != null
                            && ((JComponent) src).getClientProperty("doNotCancelPopup").toString().equals(QuaquaComboBoxUI.HIDE_POPUP_KEY.toString()))//
                            ) {
                        // Cancel popup only if this property was not set.
                        // If this property is set to TRUE component wants
                        // to deal with this event by himself.
                        cancelPopupMenu();
                        // Ask UIManager about should we consume event that closes
                        // popup. This made to match native apps behaviour.
                        boolean consumeEvent =
                                UIManager.getBoolean("PopupMenu.consumeEventOnClose");
                        // Consume the event so that normal processing stops.
                        if (consumeEvent && !(src instanceof MenuElement)) {
                            me.consume();
                        }
                    }
                    break;

                case MouseEvent.MOUSE_RELEASED:
                    if (!(src instanceof MenuElement)) {
                        // Do not forward event to MSM, let component handle it
                        if (isInPopup(src)) {
                            break;
                        }
                    }
                    if (src instanceof JMenu || !(src instanceof JMenuItem)) {
                        MenuSelectionManager.defaultManager().
                                processMouseEvent(me);
                    }
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    if (!(src instanceof MenuElement)) {
                        // For the MOUSE_DRAGGED event the src is
                        // the Component in which mouse button was pressed.
                        // If the src is in popupMenu,
                        // do not forward event to MSM, let component handle it.
                        if (isInPopup(src)) {
                            break;
                        }
                    }
                    MenuSelectionManager.defaultManager().
                            processMouseEvent(me);
                    break;
                case MouseEvent.MOUSE_WHEEL:
                    if (isInPopup(src)) {
                        MouseWheelEvent mwe = (MouseWheelEvent) ev;
                        if (mwe.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                            int totalScrollAmount = mwe.getUnitsToScroll();
                            if (totalScrollAmount > 0) {
                                // TODO select next menu item
                            } else if (totalScrollAmount < 0) {
                                // TODO select previous menu item
                            }
                        }
                        return;
                    }
                    //cancelPopupMenu(); don't cancel the popup 
                    break;
            }
        }

        boolean isInPopup(Component src) {
            for (Component c = src; c != null; c = c.getParent()) {
                if (c instanceof JWindow) {
                    JWindow jw = (JWindow) c;
                    if (jw.getName() != null && jw.getName().equals("###focusableSwingPopup###")) {
                        return true;
                    } else {
                        break;
                    }
                } else if (c instanceof Applet || c instanceof Window) {
                    break;
                } else if (c instanceof JPopupMenu) {
                    return true;
                }
            }
            return false;
        }

        void cancelPopupMenu() {
            JPopupMenu firstPopup = (JPopupMenu) getFirstPopup();
            // 4234793: This action should call firePopupMenuCanceled but it's
            // a protected method. The real solution could be to make
            // firePopupMenuCanceled public and call it directly.
            List popups = getPopups();
            Iterator iter = popups.iterator();
            while (iter.hasNext()) {
                JPopupMenu popup = (JPopupMenu) iter.next();
                popup.putClientProperty("JPopupMenu.firePopupMenuCanceled", Boolean.TRUE);
            }
            MenuSelectionManager.defaultManager().clearSelectedPath();
        }

        public void componentResized(ComponentEvent e) {
            cancelPopupMenu();
        }

        public void componentMoved(ComponentEvent e) {
            cancelPopupMenu();
        }

        public void componentShown(ComponentEvent e) {
            cancelPopupMenu();
        }

        public void componentHidden(ComponentEvent e) {
            cancelPopupMenu();
        }

        public void windowClosing(WindowEvent e) {
            cancelPopupMenu();
        }

        public void windowClosed(WindowEvent e) {
            cancelPopupMenu();
        }

        public void windowIconified(WindowEvent e) {
            cancelPopupMenu();
        }

        public void windowDeactivated(WindowEvent e) {
            cancelPopupMenu();
        }

        public void windowOpened(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowActivated(WindowEvent e) {
        }
    }

    /**
     * This Listener fires the Action that provides the correct auditory
     * feedback.
     *
     * @since 1.4
     */
    private class QuaquaPopupMenuListener implements PopupMenuListener {

        public void popupMenuCanceled(PopupMenuEvent e) {
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            /* QuaquaLookAndFeel.playSound((JPopupMenu)e.getSource(),
            "PopupMenu.popupSound");*/
        }
    }

    /**
     * Handles mnemonic for children JMenuItems.
     * @since 1.5
     */
    private class QuaquaMenuKeyListener implements MenuKeyListener {

        MenuElement menuToOpen = null;

        public void menuKeyTyped(MenuKeyEvent e) {
            if (menuToOpen != null) {
                // we have a submenu to open
                JPopupMenu subpopup = ((JMenu) menuToOpen).getPopupMenu();
                MenuElement subitem = findEnabledChild(
                        subpopup.getSubElements(), -1, true);

                ArrayList lst = new ArrayList(Arrays.asList(e.getPath()));
                lst.add(menuToOpen);
                lst.add(subpopup);
                if (subitem != null) {
                    lst.add(subitem);
                }
                MenuElement newPath[] = new MenuElement[0];
                //
                newPath = (MenuElement[]) lst.toArray(newPath);
                MenuSelectionManager.defaultManager().setSelectedPath(newPath);
                e.consume();
            }
            menuToOpen = null;
        }

        public void menuKeyPressed(MenuKeyEvent e) {
            char keyChar = e.getKeyChar();

            // Handle the case for Escape or Enter...
            if (!Character.isLetterOrDigit(keyChar)) {
                return;
            }

            MenuSelectionManager manager = e.getMenuSelectionManager();
            MenuElement path[] = e.getPath();
            MenuElement items[] = popupMenu.getSubElements();
            int currentIndex = -1;
            int matches = 0;
            int firstMatch = -1;
            int indexes[] = null;

            for (int j = 0; j < items.length; j++) {
                if (!(items[j] instanceof JMenuItem)) {
                    continue;
                }
                JMenuItem item = (JMenuItem) items[j];
                int mnemonic = item.getMnemonic();
                if (item.isEnabled()
                        && item.isVisible() && lower(keyChar) == lower(mnemonic)) {
                    if (matches == 0) {
                        firstMatch = j;
                        matches++;
                    } else {
                        if (indexes == null) {
                            indexes = new int[items.length];
                            indexes[0] = firstMatch;
                        }
                        indexes[matches++] = j;
                    }
                }
                if (item.isArmed()) {
                    currentIndex = matches - 1;
                }
            }

            if (matches == 0) {
                // no op
            } else if (matches == 1) {
                // Invoke the menu action
                JMenuItem item = (JMenuItem) items[firstMatch];
                if (item instanceof JMenu) {
                    // submenus are handled in menuKeyTyped
                    menuToOpen = item;
                } else if (item.isEnabled()) {
                    // we have a menu item
                    manager.clearSelectedPath();
                    item.doClick();
                }
                e.consume();
            } else {
                // Select the menu item with the matching mnemonic. If
                // the same mnemonic has been invoked then select the next
                // menu item in the cycle.
                MenuElement newItem = null;

                newItem = items[indexes[(currentIndex + 1) % matches]];

                MenuElement newPath[] = new MenuElement[path.length + 1];
                System.arraycopy(path, 0, newPath, 0, path.length);
                newPath[path.length] = newItem;
                manager.setSelectedPath(newPath);
                e.consume();
            }
            return;
        }

        public void menuKeyReleased(MenuKeyEvent e) {
        }

        private char lower(char keyChar) {
            return Character.toLowerCase(keyChar);
        }

        private char lower(int mnemonic) {
            return Character.toLowerCase((char) mnemonic);
        }
    }

    /**
     * This helper is added to MenuSelectionManager as a ChangeListener to
     * listen to menu selection changes. When a menu is activated, it passes
     * focus to its parent JRootPane, and installs an ActionMap/InputMap pair
     * on that JRootPane. Those maps are necessary in order for menu
     * navigation to work. When menu is being deactivated, it restores focus
     * to the component that has had it before menu activation, and uninstalls
     * the maps.
     * This helper is also installed as a KeyListener on root pane when menu
     * is active. It forwards key events to MenuSelectionManager for mnemonic
     * keys handling.
     */
    static class MenuKeyboardHelper
            implements ChangeListener, KeyListener {

        private Component lastFocused = null;
        private MenuElement[] lastPathSelected = new MenuElement[0];
        private JPopupMenu lastPopup;
        private JRootPane invokerRootPane;
        private ActionMap menuActionMap = getActionMap();
        private InputMap menuInputMap;
        private boolean focusTraversalKeysEnabled;

        /*
         * Fix for 4213634
         * If this is false, KEY_TYPED and KEY_RELEASED events are NOT
         * processed. This is needed to avoid activating a menuitem when
         * the menu and menuitem share the same mnemonic.
         */
        private boolean receivedKeyPressed = false;

        void removeItems() {
            if (lastFocused != null) {
                if (!lastFocused.requestFocusInWindow()) {
                    // Workarounr for 4810575.
                    // If lastFocused is not in currently focused window
                    // requestFocusInWindow will fail. In this case we must
                    // request focus by requestFocus() if it was not
                    // transferred from our popup.
                    Window cfw = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
                    if (cfw != null
                            && "###focusableSwingPopup###".equals(cfw.getName())) {
                        lastFocused.requestFocus();
                    }

                }
                lastFocused = null;
            }
            if (invokerRootPane != null) {
                invokerRootPane.removeKeyListener(this);
                invokerRootPane.setFocusTraversalKeysEnabled(focusTraversalKeysEnabled);
                removeUIInputMap(invokerRootPane, menuInputMap);
                removeUIActionMap(invokerRootPane, menuActionMap);
                invokerRootPane = null;
            }
            receivedKeyPressed = false;
        }
        private FocusListener rootPaneFocusListener = new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent ev) {
                Component opposite = ev.getOppositeComponent();
                if (opposite != null) {
                    lastFocused = opposite;
                }
                ev.getComponent().removeFocusListener(this);
            }
        };

        /**
         * Return the last JPopupMenu in <code>path</code>,
         * or <code>null</code> if none found
         */
        JPopupMenu getActivePopup(MenuElement[] path) {
            for (int i = path.length - 1; i >= 0; i--) {
                MenuElement elem = path[i];
                if (elem instanceof JPopupMenu) {
                    return (JPopupMenu) elem;
                }
            }
            return null;
        }

        void addUIInputMap(JComponent c, InputMap map) {
            InputMap lastNonUI = null;
            InputMap parent = c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

            while (parent != null && !(parent instanceof UIResource)) {
                lastNonUI = parent;
                parent = parent.getParent();
            }

            if (lastNonUI == null) {
                c.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, map);
            } else {
                lastNonUI.setParent(map);
            }
            map.setParent(parent);
        }

        void addUIActionMap(JComponent c, ActionMap map) {
            ActionMap lastNonUI = null;
            ActionMap parent = c.getActionMap();

            while (parent != null && !(parent instanceof UIResource)) {
                lastNonUI = parent;
                parent = parent.getParent();
            }

            if (lastNonUI == null) {
                c.setActionMap(map);
            } else {
                lastNonUI.setParent(map);
            }
            map.setParent(parent);
        }

        void removeUIInputMap(JComponent c, InputMap map) {
            InputMap im = null;
            InputMap parent = c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

            while (parent != null) {
                if (parent == map) {
                    if (im == null) {
                        c.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW,
                                map.getParent());
                    } else {
                        im.setParent(map.getParent());
                    }
                    break;
                }
                im = parent;
                parent = parent.getParent();
            }
        }

        void removeUIActionMap(JComponent c, ActionMap map) {
            ActionMap im = null;
            ActionMap parent = c.getActionMap();

            while (parent != null) {
                if (parent == map) {
                    if (im == null) {
                        c.setActionMap(map.getParent());
                    } else {
                        im.setParent(map.getParent());
                    }
                    break;
                }
                im = parent;
                parent = parent.getParent();
            }
        }

        public void stateChanged(ChangeEvent ev) {
            if (!(UIManager.getLookAndFeel() instanceof BasicLookAndFeel)) {
                uninstall();
                return;
            }
            MenuSelectionManager msm = (MenuSelectionManager) ev.getSource();
            MenuElement[] p = msm.getSelectedPath();
            JPopupMenu popup = getActivePopup(p);
            if (popup != null && !popup.isFocusable()) {
                // Do nothing for non-focusable popups
                return;
            }

            if (lastPathSelected.length != 0 && p.length != 0) {
                if (!checkInvokerEqual(p[0], lastPathSelected[0])) {
                    removeItems();
                    lastPathSelected = new MenuElement[0];
                }
            }

            if (lastPathSelected.length == 0 && p.length > 0) {
                // menu posted
                JComponent invoker;

                if (popup == null) {
                    if (p.length == 2 && p[0] instanceof JMenuBar
                            && p[1] instanceof JMenu) {
                        // a menu has been selected but not open
                        invoker = (JComponent) p[1];
                        popup = ((JMenu) invoker).getPopupMenu();
                    } else {
                        return;
                    }
                } else {
                    Component c = popup.getInvoker();
                    if (c instanceof JFrame) {
                        invoker = ((JFrame) c).getRootPane();
                    } else if (c instanceof JDialog) {
                        invoker = ((JDialog) c).getRootPane();
                    } else if (c instanceof JApplet) {
                        invoker = ((JApplet) c).getRootPane();
                    } else {
                        while (!(c instanceof JComponent)) {
                            if (c == null) {
                                return;
                            }
                            c = c.getParent();
                        }
                        invoker = (JComponent) c;
                    }
                }

                // remember current focus owner
                lastFocused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                // request focus on root pane and install keybindings
                // used for menu navigation
                invokerRootPane = SwingUtilities.getRootPane(invoker);
                if (invokerRootPane != null) {
                    invokerRootPane.addFocusListener(rootPaneFocusListener);
                    invokerRootPane.requestFocus(true);
                    invokerRootPane.addKeyListener(this);
                    focusTraversalKeysEnabled = invokerRootPane.getFocusTraversalKeysEnabled();
                    invokerRootPane.setFocusTraversalKeysEnabled(false);

                    menuInputMap = getInputMap(popup, invokerRootPane);
                    addUIInputMap(invokerRootPane, menuInputMap);
                    addUIActionMap(invokerRootPane, menuActionMap);
                }
            } else if (lastPathSelected.length != 0 && p.length == 0) {
                // menu hidden -- return focus to where it had been before
                // and uninstall menu keybindings
                removeItems();
            } else {
                if (popup != lastPopup) {
                    receivedKeyPressed = false;
                }
            }

            // Remember the last path selected
            lastPathSelected = p;
            lastPopup = popup;
        }

        public void keyPressed(KeyEvent ev) {
            receivedKeyPressed = true;
            MenuSelectionManager.defaultManager().processKeyEvent(ev);
        }

        public void keyReleased(KeyEvent ev) {
            if (receivedKeyPressed) {
                receivedKeyPressed = false;
                MenuSelectionManager.defaultManager().processKeyEvent(ev);
            }
        }

        public void keyTyped(KeyEvent ev) {
            if (receivedKeyPressed) {
                MenuSelectionManager.defaultManager().processKeyEvent(ev);
            }
        }

        void uninstall() {
            synchronized (MENU_KEYBOARD_HELPER_KEY) {
                MenuSelectionManager.defaultManager().removeChangeListener(this);
                AppContext.getAppContext().remove(MENU_KEYBOARD_HELPER_KEY);
            }
        }
    }

    private static class Actions extends UIAction {
        // Types of actions

        private static final String CANCEL = "cancel";
        private static final String SELECT_NEXT = "selectNext";
        private static final String SELECT_PREVIOUS = "selectPrevious";
        private static final String SELECT_PARENT = "selectParent";
        private static final String SELECT_CHILD = "selectChild";
        private static final String RETURN = "return";
        // Used for next/previous actions
        private static final boolean FORWARD = true;
        private static final boolean BACKWARD = false;
        // Used for parent/child actions
        private static final boolean PARENT = false;
        private static final boolean CHILD = true;

        Actions(String key) {
            super(key);
        }

        public void actionPerformed(ActionEvent e) {
            String key = getName();
            if (key == CANCEL) {
                cancel();
            } else if (key == SELECT_NEXT) {
                selectItem(FORWARD);
            } else if (key == SELECT_PREVIOUS) {
                selectItem(BACKWARD);
            } else if (key == SELECT_PARENT) {
                selectParentChild(PARENT);
            } else if (key == SELECT_CHILD) {
                selectParentChild(CHILD);
            } else if (key == RETURN) {
                doReturn();
            }
        }

        private void doReturn() {
            KeyboardFocusManager fmgr =
                    KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Component focusOwner = fmgr.getFocusOwner();
            if (focusOwner != null && !(focusOwner instanceof JRootPane)) {
                return;
            }

            MenuSelectionManager msm = MenuSelectionManager.defaultManager();
            MenuElement path[] = msm.getSelectedPath();
            MenuElement lastElement;
            if (path.length > 0) {
                lastElement = path[path.length - 1];
                if (lastElement instanceof JMenu) {
                    MenuElement newPath[] = new MenuElement[path.length + 1];
                    System.arraycopy(path, 0, newPath, 0, path.length);
                    newPath[path.length] = ((JMenu) lastElement).getPopupMenu();
                    msm.setSelectedPath(newPath);
                } else if (lastElement instanceof JMenuItem) {
                    JMenuItem mi = (JMenuItem) lastElement;

                    //  if (mi.getUI() instanceof BasicMenuItemUI) {
                    //      ((BasicMenuItemUI)mi.getUI()).doClick(msm);
                    //  }
                    //  else {
                    msm.clearSelectedPath();
                    mi.doClick(0);
                    // }
                }
            }
        }

        private void selectParentChild(boolean direction) {
            MenuSelectionManager msm = MenuSelectionManager.defaultManager();
            MenuElement path[] = msm.getSelectedPath();
            int len = path.length;

            if (direction == PARENT) {
                // selecting parent
                int popupIndex = len - 1;

                if (len > 2
                        && // check if we have an open submenu. A submenu item may or
                        // may not be selected, so submenu popup can be either the
                        // last or next to the last item.
                        (path[popupIndex] instanceof JPopupMenu
                        || path[--popupIndex] instanceof JPopupMenu)
                        && !((JMenu) path[popupIndex - 1]).isTopLevelMenu()) {

                    // we have a submenu, just close it
                    MenuElement newPath[] = new MenuElement[popupIndex];
                    System.arraycopy(path, 0, newPath, 0, popupIndex);
                    msm.setSelectedPath(newPath);
                    return;
                }
            } else {
                // selecting child
                if (len > 0 && path[len - 1] instanceof JMenu
                        && !((JMenu) path[len - 1]).isTopLevelMenu()) {

                    // we have a submenu, open it
                    JMenu menu = (JMenu) path[len - 1];
                    JPopupMenu popup = menu.getPopupMenu();
                    MenuElement[] subs = popup.getSubElements();
                    MenuElement item = findEnabledChild(subs, -1, true);
                    MenuElement[] newPath;

                    if (item == null) {
                        newPath = new MenuElement[len + 1];
                    } else {
                        newPath = new MenuElement[len + 2];
                        newPath[len + 1] = item;
                    }
                    System.arraycopy(path, 0, newPath, 0, len);
                    newPath[len] = popup;
                    msm.setSelectedPath(newPath);
                    return;
                }
            }

            // check if we have a toplevel menu selected.
            // If this is the case, we select another toplevel menu
            if (len > 1 && path[0] instanceof JMenuBar) {
                MenuElement currentMenu = path[1];
                MenuElement nextMenu = findEnabledChild(
                        path[0].getSubElements(), currentMenu, direction);

                if (nextMenu != null && nextMenu != currentMenu) {
                    MenuElement newSelection[];
                    if (len == 2) {
                        // menu is selected but its popup not shown
                        newSelection = new MenuElement[2];
                        newSelection[0] = path[0];
                        newSelection[1] = nextMenu;
                    } else {
                        // menu is selected and its popup is shown
                        newSelection = new MenuElement[3];
                        newSelection[0] = path[0];
                        newSelection[1] = nextMenu;
                        newSelection[2] = ((JMenu) nextMenu).getPopupMenu();
                    }
                    msm.setSelectedPath(newSelection);
                }
            }
        }

        private void selectItem(boolean direction) {
            MenuSelectionManager msm = MenuSelectionManager.defaultManager();
            MenuElement path[] = msm.getSelectedPath();
            if (path.length == 0) {
                return;
            }
            int len = path.length;
            if (len == 1 && path[0] instanceof JPopupMenu) {

                JPopupMenu popup = (JPopupMenu) path[0];
                MenuElement[] newPath = new MenuElement[2];
                newPath[0] = popup;
                newPath[1] = findEnabledChild(popup.getSubElements(), -1, direction);
                msm.setSelectedPath(newPath);
            } else if (len == 2
                    && path[0] instanceof JMenuBar && path[1] instanceof JMenu) {

                // a toplevel menu is selected, but its popup not shown.
                // Show the popup and select the first item
                JPopupMenu popup = ((JMenu) path[1]).getPopupMenu();
                MenuElement next =
                        findEnabledChild(popup.getSubElements(), -1, FORWARD);
                MenuElement[] newPath;

                if (next != null) {
                    // an enabled item found -- include it in newPath
                    newPath = new MenuElement[4];
                    newPath[3] = next;
                } else {
                    // menu has no enabled items -- still must show the popup
                    newPath = new MenuElement[3];
                }
                System.arraycopy(path, 0, newPath, 0, 2);
                newPath[2] = popup;
                msm.setSelectedPath(newPath);

            } else if (path[len - 1] instanceof JPopupMenu
                    && path[len - 2] instanceof JMenu) {

                // a menu (not necessarily toplevel) is open and its popup
                // shown. Select the appropriate menu item
                JMenu menu = (JMenu) path[len - 2];
                JPopupMenu popup = menu.getPopupMenu();
                MenuElement next =
                        findEnabledChild(popup.getSubElements(), -1, direction);

                if (next != null) {
                    MenuElement[] newPath = new MenuElement[len + 1];
                    System.arraycopy(path, 0, newPath, 0, len);
                    newPath[len] = next;
                    msm.setSelectedPath(newPath);
                } else {
                    // all items in the popup are disabled.
                    // We're going to find the parent popup menu and select
                    // its next item. If there's no parent popup menu (i.e.
                    // current menu is toplevel), do nothing
                    if (len > 2 && path[len - 3] instanceof JPopupMenu) {
                        popup = ((JPopupMenu) path[len - 3]);
                        next = findEnabledChild(popup.getSubElements(),
                                menu, direction);

                        if (next != null && next != menu) {
                            MenuElement[] newPath = new MenuElement[len - 1];
                            System.arraycopy(path, 0, newPath, 0, len - 2);
                            newPath[len - 2] = next;
                            msm.setSelectedPath(newPath);
                        }
                    }
                }

            } else {
                // just select the next item, no path expansion needed
                MenuElement subs[] = path[len - 2].getSubElements();
                MenuElement nextChild =
                        findEnabledChild(subs, path[len - 1], direction);
                if (nextChild == null) {
                    nextChild = findEnabledChild(subs, -1, direction);
                }
                if (nextChild != null) {
                    path[len - 1] = nextChild;
                    msm.setSelectedPath(path);
                }
            }
        }

        private void cancel() {
            // 4234793: This action should call JPopupMenu.firePopupMenuCanceled but it's
            // a protected method. The real solution could be to make
            // firePopupMenuCanceled public and call it directly.
            JPopupMenu lastPopup = (JPopupMenu) getLastPopup();
            if (lastPopup != null) {
                lastPopup.putClientProperty("JPopupMenu.firePopupMenuCanceled", Boolean.TRUE);
            }

            MenuElement path[] = MenuSelectionManager.defaultManager().getSelectedPath();
            if (path.length > 4) { /* PENDING(arnaud) Change this to 2 when a mouse grabber is available for MenuBar */
                MenuElement newPath[] = new MenuElement[path.length - 2];
                System.arraycopy(path, 0, newPath, 0, path.length - 2);
                MenuSelectionManager.defaultManager().setSelectedPath(newPath);
            } else {
                MenuSelectionManager.defaultManager().clearSelectedPath();
            }
        }
    }
}
