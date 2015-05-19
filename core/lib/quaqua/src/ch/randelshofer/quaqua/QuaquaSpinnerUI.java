/*
 * @(#)QuaquaSpinnerUI.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */

package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.util.Debug;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.*;
/**
 * QuaquaSpinnerUI.
 * 
 * @author Werner Randelshofer
 * @version $Id: QuaquaSpinnerUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaSpinnerUI extends BasicSpinnerUI implements VisuallyLayoutable {
    /**
     * The <code>PropertyChangeListener</code> that's added to the
     * <code>JSpinner</code> itself. This listener is created by the
     * <code>createPropertyChangeListener</code> method, added by the
     * <code>installListeners</code> method, and removed by the
     * <code>uninstallListeners</code> method.
     * <p>
     * One instance of this listener is shared by all JSpinners.
     *
     * @see #createPropertyChangeListener
     * @see #installListeners
     * @see #uninstallListeners
     */
    private static final PropertyChangeListener propertyChangeListener = new PropertyChangeHandler();
    /**
     * The mouse/action listeners that are added to the spinner's
     * arrow buttons.  These listeners are shared by all
     * spinner arrow buttons.
     *
     * @see #createNextButton
     * @see #createPreviousButton
     */
    private static final ArrowButtonHandler nextButtonHandler = new ArrowButtonHandler("increment", true);
    private static final ArrowButtonHandler previousButtonHandler = new ArrowButtonHandler("decrement", false);
    
    /**
     * Used by the default LayoutManager class - SpinnerLayout for
     * missing (null) editor/nextButton/previousButton children.
     */
    private static final Dimension zeroSize = new Dimension(0, 0);
    
    public static ComponentUI createUI(JComponent c) {
        return new QuaquaSpinnerUI();
    }
    /**
     * Create a component that will replace the spinner models value
     * with the object returned by <code>spinner.getPreviousValue</code>.
     * By default the <code>previousButton</code> is a JButton
     * who's <code>ActionListener</code> updates it's <code>JSpinner</code>
     * ancestors model.  If a previousButton isn't needed (in a subclass)
     * then override this method to return null.
     *
     * @return a component that will replace the spinners model with the
     *     next value in the sequence, or null
     * @see #installUI
     * @see #createNextButton
     */
    @Override
    protected Component createPreviousButton() {
        JButton button = createArrowButton(SwingConstants.NORTH, previousButtonHandler);
        button.setIcon(UIManager.getIcon("Spinner.south"));
        return button;
    }
    
    
    /**
     * Create a component that will replace the spinner models value
     * with the object returned by <code>spinner.getNextValue</code>.
     * By default the <code>nextButton</code> is a JButton
     * who's <code>ActionListener</code> updates it's <code>JSpinner</code>
     * ancestors model.  If a nextButton isn't needed (in a subclass)
     * then override this method to return null.
     *
     * @return a component that will replace the spinners model with the
     *     next value in the sequence, or null
     * @see #installUI
     * @see #createPreviousButton
     */
    @Override
    protected Component createNextButton() {
        JButton button = createArrowButton(SwingConstants.NORTH, nextButtonHandler);
        button.setIcon(UIManager.getIcon("Spinner.north"));
        return button;
    }
    
    private JButton createArrowButton(int direction, ArrowButtonHandler handler) {
        JButton b = new JButton();
        if (! (b.getUI() instanceof QuaquaButtonUI)) {
            b.setUI((ButtonUI) QuaquaButtonUI.createUI(b));
        }
        b.setBorderPainted(false);
        b.setMargin(new Insets(0,0,0,0));
        b.addActionListener(handler);
        b.addMouseListener(handler);
        b.setFocusable(false);
        Border buttonBorder = UIManager.getBorder("Spinner.arrowButtonBorder");
        if (buttonBorder instanceof UIResource) {
            // Wrap the border to avoid having the UIResource be replaced by
            // the ButtonUI. This is the opposite of using BorderUIResource.
            b.setBorder(new CompoundBorder(buttonBorder, null));
        } else {
            b.setBorder(buttonBorder);
        }
        b.putClientProperty("Quaqua.Component.visualMargin", new Insets(0,0,0,3));
        return b;
    }
    /**
     * Create a <code>LayoutManager</code> that manages the <code>editor</code>,
     * <code>nextButton</code>, and <code>previousButton</code>
     * children of the JSpinner.  These three children must be
     * added with a constraint that identifies their role:
     * "Editor", "Next", and "Previous". The default layout manager
     * can handle the absence of any of these children.
     *
     * @return a LayoutManager for the editor, next button, and previous button.
     * @see #createNextButton
     * @see #createPreviousButton
     * @see #createEditor
     */
    @Override
    protected LayoutManager createLayout() {
        return new SpinnerLayout();
    }
    /**
     * Create a <code>PropertyChangeListener</code> that can be
     * added to the JSpinner itself.  Typically, this listener
     * will call replaceEditor when the "editor" property changes,
     * since it's the <code>SpinnerUI's</code> responsibility to
     * add the editor to the JSpinner (and remove the old one).
     * This method is called by <code>installListeners</code>.
     *
     * @return A PropertyChangeListener for the JSpinner itself
     * @see #installListeners
     */
    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return new PropertyChangeHandler();
    }
    /**
     * Updates the enabled state of the children Components based on the
     * enabled state of the <code>JSpinner</code>.
     */
    private void updateEnabledState() {
        updateEnabledState(spinner, spinner.isEnabled());
    }
    
    
    /**
     * Recursively updates the enabled state of the child
     * <code>Component</code>s of <code>c</code>.
     */
    private void updateEnabledState(Container c, boolean enabled) {
        for (int counter = c.getComponentCount() - 1; counter >= 0;counter--) {
            Component child = c.getComponent(counter);
            
            child.setEnabled(enabled);
            if (child instanceof Container) {
                updateEnabledState((Container)child, enabled);
            }
        }
    }
    /**
     * Updates the font of the children Components based on the
     * font of the <code>JSpinner</code>.
     */
    private void updateFont() {
        Font f = spinner.getFont();
        JComponent editor = spinner.getEditor();
        editor.setFont(f);
        for (int i=0; i < editor.getComponentCount(); i++) {
            editor.getComponent(i).setFont(f);
        }
        if (f.getSize() <= 11) {
            getNextButton().setIcon(UIManager.getIcon("Spinner.smallNorth"));
            getPreviousButton().setIcon(UIManager.getIcon("Spinner.smallSouth"));
        } else {
            getNextButton().setIcon(UIManager.getIcon("Spinner.north"));
            getPreviousButton().setIcon(UIManager.getIcon("Spinner.south"));
        }
    }
    
    /**
     * This method is called by installUI to get the editor component
     * of the <code>JSpinner</code>.  By default it just returns
     * <code>JSpinner.getEditor()</code>.  Subclasses can override
     * <code>createEditor</code> to return a component that contains
     * the spinner's editor or null, if they're going to handle adding
     * the editor to the <code>JSpinner</code> in an
     * <code>installUI</code> override.
     * <p>
     * Typically this method would be overridden to wrap the editor
     * with a container with a custom border, since one can't assume
     * that the editors border can be set directly.
     * <p>
     * The <code>replaceEditor</code> method is called when the spinners
     * editor is changed with <code>JSpinner.setEditor</code>.  If you've
     * overriden this method, then you'll probably want to override
     * <code>replaceEditor</code> as well.
     *
     * @return the JSpinners editor JComponent, spinner.getEditor() by default
     * @see #installUI
     * @see #replaceEditor
     * @see JSpinner#getEditor
     */
    @Override
    protected JComponent createEditor() {
        JComponent editor = spinner.getEditor();
        maybeRemoveEditorBorder(editor);
        installEditorBorderListener(editor);
        installEditorFocusInputMap(editor);
        return editor;
    }
    /**
     * Called by the <code>PropertyChangeListener</code> when the
     * <code>JSpinner</code> editor property changes.  It's the responsibility
     * of this method to remove the old editor and add the new one.  By
     * default this operation is just:
     * <pre>
     * spinner.remove(oldEditor);
     * spinner.add(newEditor, "Editor");
     * </pre>
     * The implementation of <code>replaceEditor</code> should be coordinated
     * with the <code>createEditor</code> method.
     *
     * @see #createEditor
     * @see #createPropertyChangeListener
     */
    @Override
    protected void replaceEditor(JComponent oldEditor, JComponent newEditor) {
        spinner.remove(oldEditor);
        maybeRemoveEditorBorder(newEditor);
        installEditorBorderListener(newEditor);
        installEditorFocusInputMap(newEditor);
        spinner.add(newEditor, "Editor");
    }
    /**
     * Remove the border around the inner editor component for LaFs
     * that install an outside border around the spinner,
     */
    private void installEditorBorderListener(JComponent editor) {
        if (!UIManager.getBoolean("Spinner.editorBorderPainted")) {
            if (editor instanceof JPanel &&
            editor.getBorder() == null &&
            editor.getComponentCount() > 0) {
                
                editor = (JComponent)editor.getComponent(0);
            }
            if (editor != null &&
            (editor.getBorder() == null ||
            editor.getBorder() instanceof UIResource)) {
                editor.addPropertyChangeListener(propertyChangeListener);
            }
        }
    }
    
    private void removeEditorBorderListener(JComponent editor) {
        if (!UIManager.getBoolean("Spinner.editorBorderPainted")) {
            if (editor instanceof JPanel &&
            editor.getComponentCount() > 0) {
                
                editor = (JComponent)editor.getComponent(0);
            }
            if (editor != null) {
                editor.removePropertyChangeListener(propertyChangeListener);
            }
        }
    }
    /**
     * Installs the KeyboardActions onto the JSpinner.
     */
    private void installEditorFocusInputMap(JComponent editor) {
        if (editor instanceof JPanel &&
        editor.getComponentCount() > 0) {
            
            editor = (JComponent)editor.getComponent(0);
        }
        if (editor != null) {
            InputMap iMap = getInputMap(JComponent.
            WHEN_FOCUSED);
            
            SwingUtilities.replaceUIInputMap(editor, JComponent.
            WHEN_FOCUSED,
            iMap);
            
            //SwingUtilities.replaceUIActionMap(editor, getActionMap());
        }
    }
    /**
     * Returns the InputMap to install for <code>condition</code>.
     */
    private InputMap getInputMap(int condition) {
        switch (condition) {
            case JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT :
                return (InputMap)UIManager.get("Spinner.ancestorInputMap");
            case JComponent.WHEN_FOCUSED :
                return (InputMap)UIManager.get("Spinner.focusInputMap");
            default :
                return null;
        }
    }
    
    private JButton getNextButton() {
        return (JButton) ((SpinnerLayout) spinner.getLayout()).nextButton;
    }
    private JButton getPreviousButton() {
        return (JButton) ((SpinnerLayout) spinner.getLayout()).previousButton;
    }
    private JComponent getEditor() {
        return (JComponent) ((SpinnerLayout) spinner.getLayout()).editor;
    }
    @Override
    public void paint( Graphics g, JComponent c ) {
        super.paint(g, c);
        Debug.paint(g, c, this);
    }
    
    /**
     * Remove the border around the inner editor component for LaFs
     * that install an outside border around the spinner,
     */
    private void maybeRemoveEditorBorder(JComponent editor) {
        if (!UIManager.getBoolean("Spinner.editorBorderPainted")) {
            if (editor instanceof JPanel &&
            editor.getBorder() == null &&
            editor.getComponentCount() > 0) {
                
                editor = (JComponent)editor.getComponent(0);
            }
            
            if (editor != null && editor.getBorder() instanceof UIResource) {
                editor.setBorder(null);
            }
        }
    }
    /**
     * Calls <code>installDefaults</code>, <code>installListeners</code>,
     * and then adds the components returned by <code>createNextButton</code>,
     * <code>createPreviousButton</code>, and <code>createEditor</code>.
     *
     * @param c the JSpinner
     * @see #installDefaults
     * @see #installListeners
     * @see #createNextButton
     * @see #createPreviousButton
     * @see #createEditor
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
	QuaquaUtilities.installProperty(c, "opaque", UIManager.get("Spinner.opaque"));
        //c.setOpaque(UIManager.getBoolean("Spinner.opaque"));
        updateFont();
    }
    /**
     * Initializes <code>propertyChangeListener</code> with
     * a shared object that delegates interesting PropertyChangeEvents
     * to protected methods.
     * <p>
     * This method is called by <code>installUI</code>.
     *
     * @see #replaceEditor
     * @see #uninstallListeners
     */
    @Override
    protected void installListeners() {
        spinner.addPropertyChangeListener(propertyChangeListener);
    }
    
    
    /**
     * Removes the <code>propertyChangeListener</code> added
     * by installListeners.
     * <p>
     * This method is called by <code>uninstallUI</code>.
     *
     * @see #installListeners
     */
    @Override
    protected void uninstallListeners() {
        spinner.removePropertyChangeListener(propertyChangeListener);
        removeEditorBorderListener(spinner.getEditor());
    }
    
    /**
     * A handler for spinner arrow button mouse and action events.  When
     * a left mouse pressed event occurs we look up the (enabled) spinner
     * that's the source of the event and start the autorepeat timer.  The
     * timer fires action events until any button is released at which
     * point the timer is stopped and the reference to the spinner cleared.
     * The timer doesn't start until after a 300ms delay, so often the
     * source of the initial (and final) action event is just the button
     * logic for mouse released - which means that we're relying on the fact
     * that our mouse listener runs after the buttons mouse listener.
     * <p>
     * Note that one instance of this handler is shared by all slider previous
     * arrow buttons and likewise for all of the next buttons,
     * so it doesn't have any state that persists beyond the limits
     * of a single button pressed/released gesture.
     */
    private static class ArrowButtonHandler extends AbstractAction
    implements MouseListener, UIResource {
        final javax.swing.Timer autoRepeatTimer;
        final boolean isNext;
        JSpinner spinner = null;
        
        ArrowButtonHandler(String name, boolean isNext) {
            super(name);
            this.isNext = isNext;
            autoRepeatTimer = new javax.swing.Timer(60, this);
            autoRepeatTimer.setInitialDelay(300);
        }
        
        private JSpinner eventToSpinner(AWTEvent e) {
            Object src = e.getSource();
            while ((src instanceof Component) && !(src instanceof JSpinner)) {
                src = ((Component)src).getParent();
            }
            return (src instanceof JSpinner) ? (JSpinner)src : null;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (!(e.getSource() instanceof javax.swing.Timer)) {
                // Most likely resulting from being in ActionMap.
                spinner = eventToSpinner(e);
            }
            if (spinner != null) {
                try {
                    int calendarField = getCalendarField(spinner);
                    spinner.commitEdit();
                    if (calendarField != -1) {
                        ((SpinnerDateModel)spinner.getModel()).
                        setCalendarField(calendarField);
                    }
                    Object value = (isNext) ? spinner.getNextValue() :
                        spinner.getPreviousValue();
                        if (value != null) {
                            spinner.setValue(value);
                            select(spinner);
                        }
                } catch (IllegalArgumentException iae) {
                    UIManager.getLookAndFeel().provideErrorFeedback(spinner);
                } catch (ParseException pe) {
                    UIManager.getLookAndFeel().provideErrorFeedback(spinner);
                }
            }
        }
        
        /**
         * If the spinner's editor is a DateEditor, this selects the field
         * associated with the value that is being incremented.
         */
        private void select(JSpinner spinner) {
            JComponent editor = spinner.getEditor();
            
            if (editor instanceof JSpinner.DateEditor) {
                JSpinner.DateEditor dateEditor = (JSpinner.DateEditor)editor;
                JFormattedTextField ftf = dateEditor.getTextField();
                Format format = dateEditor.getFormat();
                Object value;
                
                if (format != null && (value = spinner.getValue()) != null) {
                    SpinnerDateModel model = dateEditor.getModel();
                    DateFormat.Field field = DateFormat.Field.ofCalendarField(
                    model.getCalendarField());
                    
                    if (field != null) {
                        try {
                            AttributedCharacterIterator iterator = format.
                            formatToCharacterIterator(value);
                            if (!select(ftf, iterator, field) &&
                            field == DateFormat.Field.HOUR0) {
                                select(ftf, iterator, DateFormat.Field.HOUR1);
                            }
                        }
                        catch (IllegalArgumentException iae) {}
                    }
                }
            }
        }
        
        /**
         * Selects the passed in field, returning true if it is found,
         * false otherwise.
         */
        private boolean select(JFormattedTextField ftf,
        AttributedCharacterIterator iterator,
        DateFormat.Field field) {
            int max = ftf.getDocument().getLength();
            
            iterator.first();
            do {
                Map attrs = iterator.getAttributes();
                
                if (attrs != null && attrs.containsKey(field)){
                    int start = iterator.getRunStart(field);
                    int end = iterator.getRunLimit(field);
                    
                    if (start != -1 && end != -1 && start <= max &&
                    end <= max) {
                        ftf.select(start, end);
                    }
                    return true;
                }
            } while (iterator.next() != CharacterIterator.DONE);
            return false;
        }
        
        /**
         * Returns the calendarField under the start of the selection, or
         * -1 if there is no valid calendar field under the selection (or
         * the spinner isn't editing dates.
         */
        private int getCalendarField(JSpinner spinner) {
            JComponent editor = spinner.getEditor();
            
            if (editor instanceof JSpinner.DateEditor) {
                JSpinner.DateEditor dateEditor = (JSpinner.DateEditor)editor;
                JFormattedTextField ftf = dateEditor.getTextField();
                int start = ftf.getSelectionStart();
                JFormattedTextField.AbstractFormatter formatter =
                ftf.getFormatter();
                
                if (formatter instanceof InternationalFormatter) {
                    Format.Field[] fields = ((InternationalFormatter)
                    formatter).getFields(start);
                    
                    for (int counter = 0; counter < fields.length; counter++) {
                        if (fields[counter] instanceof DateFormat.Field) {
                            int calendarField;
                            
                            if (fields[counter] == DateFormat.Field.HOUR1) {
                                calendarField = Calendar.HOUR;
                            }
                            else {
                                calendarField = ((DateFormat.Field)
                                fields[counter]).getCalendarField();
                            }
                            if (calendarField != -1) {
                                return calendarField;
                            }
                        }
                    }
                }
            }
            return -1;
        }
        
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && e.getComponent().isEnabled()) {
                spinner = eventToSpinner(e);
                autoRepeatTimer.start();
                
                focusSpinnerIfNecessary();
            }
        }
        
        public void mouseReleased(MouseEvent e) {
            autoRepeatTimer.stop();
            spinner = null;
        }
        
        public void mouseClicked(MouseEvent e) {
        }
        
        public void mouseEntered(MouseEvent e) {
        }
        
        public void mouseExited(MouseEvent e) {
        }
        
        /**
         * Requests focus on a child of the spinner if the spinner doesn't
         * have focus.
         */
        private void focusSpinnerIfNecessary() {
            Component fo = KeyboardFocusManager.
            getCurrentKeyboardFocusManager().getFocusOwner();
            if (spinner.isRequestFocusEnabled() && (
            fo == null ||
            !SwingUtilities.isDescendingFrom(fo, spinner))) {
                Container root = spinner;
                
                if (!root.isFocusCycleRoot()) {
                    root = root.getFocusCycleRootAncestor();
                }
                if (root != null) {
                    FocusTraversalPolicy ftp = root.getFocusTraversalPolicy();
                    Component child = ftp.getComponentAfter(root, spinner);
                    
                    if (child != null && SwingUtilities.isDescendingFrom(
                    child, spinner)) {
                        child.requestFocus();
                    }
                }
            }
        }
    }
    /**
     * A simple layout manager for the editor and the next/previous buttons.
     * See the BasicSpinnerUI javadoc for more information about exactly
     * how the components are arranged.
     */
    private static class SpinnerLayout implements LayoutManager {
        /*private*/ Component nextButton = null;
        /*private*/ Component previousButton = null;
        /*private*/ Component editor = null;
        
        public void addLayoutComponent(String name, Component c) {
            if ("Next".equals(name)) {
                nextButton = c;
            }
            else if ("Previous".equals(name)) {
                previousButton = c;
            }
            else if ("Editor".equals(name)) {
                editor = c;
            }
        }
        
        public void removeLayoutComponent(Component c) {
            if (c == nextButton) {
                c = null;
            }
            else if (c == previousButton) {
                previousButton = null;
            }
            else if (c == editor) {
                editor = null;
            }
        }
        
        private Dimension preferredSize(Component c) {
            return (c == null) ? zeroSize : c.getPreferredSize();
        }
        
        public Dimension preferredLayoutSize(Container parent) {
            Dimension nextD = preferredSize(nextButton);
            Dimension previousD = preferredSize(previousButton);
            Dimension editorD = preferredSize(editor);
            
            // Don't...
            /* Force the editors height to be a multiple of 2
             */
            //editorD.height = ((editorD.height + 1) / 2) * 2;
            
            Dimension size = new Dimension(editorD.width, editorD.height);
            
            // Subtract -1 because we let the buttons overlap the editor field.
            size.width += Math.max(nextD.width, previousD.width) -1;
            Insets insets = parent.getInsets();
            size.width += insets.left + insets.right;
            size.height += insets.top + insets.bottom;
            return size;
        }
        
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }
        
        private void setBounds(Component c, int x, int y, int width, int height) {
            if (c != null) {
                c.setBounds(x, y, width, height);
            }
        }
        
        public void layoutContainer(Container parent) {
            int width  = parent.getWidth();
            int height = parent.getHeight();
            
            Insets insets = parent.getInsets();
            Dimension nextD = preferredSize(nextButton);
            Dimension previousD = preferredSize(previousButton);
            int buttonsWidth = Math.max(nextD.width, previousD.width);
            int editorHeight = height - (insets.top + insets.bottom);
            
            // The arrowButtonInsets value is used instead of the JSpinner's
            // insets if not null. Defining this to be (0, 0, 0, 0) causes the
            // buttons to be aligned with the outer edge of the spinner's
            // border, and leaving it as "null" places the buttons completely
            // inside the spinner's border.
            Insets buttonInsets = UIManager.getInsets("Spinner.arrowButtonInsets");
            if (buttonInsets == null) {
                buttonInsets = insets;
            }
            
            /* Deal with the spinner's componentOrientation property.
             */
            int editorX, editorWidth, buttonsX;
            if (parent.getComponentOrientation().isLeftToRight()) {
                editorX = insets.left;
                editorWidth = width - insets.left - buttonsWidth - buttonInsets.right;
                buttonsX = width - buttonsWidth - buttonInsets.right;
            } else {
                buttonsX = buttonInsets.left;
                editorX = buttonsX + buttonsWidth;
                editorWidth = width - buttonInsets.left - buttonsWidth - insets.right;
            }
            
            //int nextY = buttonInsets.top;
            //int nextHeight = (height / 2) + (height % 2) - nextY;
            //int previousY = buttonInsets.top + nextHeight;
            //int previousHeight = height - previousY - buttonInsets.bottom;
            int previousY = insets.top + editorHeight / 2;
            int nextY = previousY - nextD.height;
            
            // Add 1 because we let the editor overlap with the buttons.
            setBounds(editor,         editorX,  insets.top, editorWidth + 1, editorHeight);
            //setBounds(nextButton,     buttonsX, nextY,      buttonsWidth, nextHeight);
            //setBounds(previousButton, buttonsX, previousY,  buttonsWidth, previousHeight);
            setBounds(nextButton,     buttonsX, nextY,      nextD.width, nextD.height);
            setBounds(previousButton, buttonsX, previousY,  previousD.width, previousD.height);
        }
    }
    /**
     * Detect JSpinner property changes we're interested in and delegate.  Subclasses
     * shouldn't need to replace the default propertyChangeListener (although they
     * can by overriding createPropertyChangeListener) since all of the interesting
     * property changes are delegated to protected methods.
     */
    private static class PropertyChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if (e.getSource() instanceof JSpinner) {
                JSpinner spinner = (JSpinner)(e.getSource());
                SpinnerUI spinnerUI = spinner.getUI();
                if (spinnerUI instanceof QuaquaSpinnerUI) {
                    QuaquaSpinnerUI ui = (QuaquaSpinnerUI)spinnerUI;
                    
                    if ("editor".equals(name)) {
                        JComponent oldEditor = (JComponent)e.getOldValue();
                        JComponent newEditor = (JComponent)e.getNewValue();
                        ui.replaceEditor(oldEditor, newEditor);
                        ui.updateEnabledState();
                        ui.updateFont();
                    }
                    else if ("enabled".equals(name)) {
                        ui.updateEnabledState();
                    }
                    else if ("font".equals(name)) {
                        ui.updateFont();
       } else if (name.equals("JComponent.sizeVariant")) {
            QuaquaUtilities.applySizeVariant(spinner);
                    }
                }
            } else if (e.getSource() instanceof JComponent) {
                JComponent c = (JComponent)e.getSource();
                if ((c.getParent() instanceof JPanel) &&
                (c.getParent().getParent() instanceof JSpinner) &&
                "border".equals(name)) {
                    
                    JSpinner spinner = (JSpinner)c.getParent().getParent();
                    SpinnerUI spinnerUI = spinner.getUI();
                    if (spinnerUI instanceof BasicSpinnerUI) {
                        QuaquaSpinnerUI ui = (QuaquaSpinnerUI)spinnerUI;
                        ui.maybeRemoveEditorBorder(c);
                    }
                }
            }
        }
    }
    
    protected Insets getMargin() {
        Insets margin = (Insets) spinner.getClientProperty("Quaqua.Component.visualMargin");
        if (margin == null) margin = UIManager.getInsets("Component.visualMargin");
        return (Insets) margin.clone();
    }
    @Override
    public int getBaseline(JComponent c, int width, int height) {
        Rectangle vb = getVisualBounds(c, VisuallyLayoutable.TEXT_BOUNDS, width, height);
        return (vb == null) ? -1 : vb.y + vb.height;
    }
    public Rectangle getVisualBounds(JComponent c, int layoutType, int width, int height) {
        Rectangle bounds = new Rectangle(0,0,width,height);
        if (layoutType == VisuallyLayoutable.CLIP_BOUNDS) {
            return bounds;
        }
        
        switch (layoutType) {
            case VisuallyLayoutable.COMPONENT_BOUNDS :
                Insets margin = getMargin();
                bounds.x += margin.left;
                bounds.y += margin.top;
                bounds.width -= margin.left + margin.right;
                bounds.height -= margin.top + margin.bottom;
                break;
            case VisuallyLayoutable.TEXT_BOUNDS :
                JComponent editor = getEditor();
                if (editor instanceof JPanel) {
                    editor = (JComponent) editor.getComponent(0);
                }
                Object ui = Methods.invokeGetter(editor, "getUI", null);
                Insets insets = spinner.getInsets();
                int editorHeight = height - (insets.top + insets.bottom);
                int editorWidth;
                Dimension nextD = getNextButton().getPreferredSize();
                Dimension previousD = getPreviousButton().getPreferredSize();
                int buttonsWidth = Math.max(nextD.width, previousD.width);
                Insets buttonInsets = new Insets(0,0,0,0); // XXX - BAD VALUES
                if (spinner.getComponentOrientation().isLeftToRight()) {
                    ///int editorX = insets.left;
                    editorWidth = width - insets.left - buttonsWidth - buttonInsets.right;
                } else {
                    //int editorX = buttonInsets.left + buttonsWidth;
                    editorWidth = width - buttonInsets.left - buttonsWidth - insets.right;
                }
                
                if (ui instanceof VisuallyLayoutable) {
                    Rectangle editorBounds = ((VisuallyLayoutable) ui).getVisualBounds(editor, layoutType, editorWidth, editorHeight);
                    bounds.x += editorBounds.x;
                    bounds.y += editorBounds.y;
                    bounds.width = editorBounds.width;
                    bounds.height = editorBounds.height;
                }
                break;
        }
        return bounds;
    }
}
