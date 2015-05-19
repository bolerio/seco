/*
 * @(#)QuaquaOptionPaneUI.java  
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.util.*;
import ch.randelshofer.quaqua.color.PaintableColor;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

/**
 * QuaquaOptionPaneUI.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaOptionPaneUI.java 464 2014-03-22 12:32:00Z wrandelshofer $
 */
public class QuaquaOptionPaneUI extends BasicOptionPaneUI {

    private final static int MIN_BUTTON_WIDTH = 68 + 6;
    private final static int HORIZONTAL_BUTTON_PADDING = 8;
    private QuaquaButtonAreaLayout buttonAreaLayout;
    private static String newline;

    static {
        java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {

                    public Object run() {
                        newline = System.getProperty("line.separator");
                        if (newline == null) {
                            newline = "\n";
                        }
                        return null;
                    }
                });
    }
    private Handler handler;

    /**
     * Creates a new QuaquaOptionPaneUI instance.
     */
    public static ComponentUI createUI(JComponent x) {
        return new QuaquaOptionPaneUI();
    }

    @Override
    public void paint(Graphics gr, JComponent c) {
        if (c.isOpaque()) {
            Graphics2D g = (Graphics2D) gr;
            g.setPaint(PaintableColor.getPaint(c.getBackground(), c));
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
    }

    @Override
    protected LayoutManager createLayoutManager() {
        return new GridBagLayout();
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    @Override
    protected void installComponents() {
        GridBagConstraints c;

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        optionPane.add(createMessageArea(), c);

        Container separator = createSeparator();
        if (separator != null) {
            c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            optionPane.add(separator, c);
        }
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        optionPane.add(createButtonArea(), c);
        Methods.invokeIfExists(
                optionPane, "applyComponentOrientation", ComponentOrientation.class,
                optionPane.getComponentOrientation());
    }
    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        handler = null;
    }

    /**
     * Messaged from installComponents to create a Container containing the
     * body of the message. The icon is the created by calling
     * <code>addIcon</code>.
     */
    @Override
    protected Container createMessageArea() {
        JPanel top = new JPanel();
        top.setBorder(UIManager.getBorder("OptionPane.messageAreaBorder"));
        top.setLayout(new BorderLayout());

        /* Fill the body. */
        Container body = new JPanel();
        Container realBody = new JPanel();

        realBody.setLayout(new BorderLayout());
        realBody.add(body, BorderLayout.CENTER);

        body.setLayout(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridx = cons.gridy = 0;
        cons.gridwidth = GridBagConstraints.REMAINDER;
        cons.gridheight = 1;
        cons.anchor = GridBagConstraints.WEST;
        cons.insets = new Insets(0, 0, 0, 0);

        addMessageComponents(body, cons, getMessage(),
                getMaxCharactersPerLineCount(), false);
        top.add(realBody, BorderLayout.CENTER);

        //addIcon(top);
        addIcon(optionPane);
        return top;
    }

    /**
     * Returns the maximum number of characters to place on a line.
     */
    @Override
    protected int getMaxCharactersPerLineCount() {
        return Math.min(
                optionPane.getMaxCharactersPerLineCount(),
                UIManager.getInt("OptionPane.maxCharactersPerLineCount"));
    }

    /**
     * Creates and adds a JLabel representing the icon returned from
     * <code>getIcon</code> to <code>top</code>. This is messaged from
     * <code>createMessageArea</code>
     */
    @Override
    protected void addIcon(Container top) {
        /* Create the icon. */
        Icon sideIcon = getIcon();

        if (sideIcon != null) {
            JLabel iconLabel = new JLabel(sideIcon);

            iconLabel.setVerticalAlignment(SwingConstants.TOP);
            GridBagConstraints cons;

            cons = new GridBagConstraints();
            cons.gridx = 0;
            cons.gridy = 0;
            cons.anchor = GridBagConstraints.NORTH;
            cons.gridheight = GridBagConstraints.REMAINDER;
            cons.insets = new Insets(0, 0, 0, 16 - 6); // -6 is the visual margin of icon and text
            top.add(iconLabel, cons);
        }
    }

    /**
     * Creates the appropriate object to represent each of the objects in
     * <code>buttons</code> and adds it to <code>container</code>. This
     * differs from addMessageComponents in that it will recurse on
     * <code>buttons</code> and that if button is not a Component
     * it will create an instance of JButton.
     */
    @Override
    protected void addButtonComponents(Container container, Object[] buttons,
            int initialIndex) {
        if (!(container.getLayout() instanceof QuaquaButtonAreaLayout)) {
            return;
        }
        if (buttons != null && buttons.length > 0) {
            boolean sizeButtonsToSame = getSizeButtonsToSameWidth();
            boolean createdAll = true;
            int numButtons = buttons.length;
            JButton[] createdButtons = null;
            int maxWidth = 0;

            // Suppress mnemonics
            int[] mnemonics = null;

            if (sizeButtonsToSame) {
                createdButtons = new JButton[numButtons];
            }

            for (int counter = 0; counter < numButtons; counter++) {
                Object button = buttons[counter];
                Component newComponent;

                if (button instanceof Component) {
                    createdAll = false;
                    newComponent = (Component) button;
                    container.add(newComponent);
                    hasCustomComponents = true;
                } else {
                    JButton aButton;

                    if (button instanceof ButtonFactory) {
                        aButton = ((ButtonFactory) button).createButton();
                    } else if (button instanceof Icon) {
                        aButton = new JButton((Icon) button);
                    } else {
                        aButton = new JButton(button.toString());
                    }
                    Methods.invokeIfExists(aButton, "setMultiClickThreshhold",
                            UIManager.getInt("OptionPane.buttonClickThreshhold"));
                    configureButton(aButton);

                    container.add(aButton);

                    ActionListener buttonListener = createButtonActionListener(counter);
                    if (buttonListener != null) {
                        aButton.addActionListener(buttonListener);
                    }
                    newComponent = aButton;
                }
                if (sizeButtonsToSame && createdAll
                        && (newComponent instanceof JButton)) {
                    createdButtons[counter] = (JButton) newComponent;
                    maxWidth = Math.max(maxWidth,
                            newComponent.getMinimumSize().width);
                }
                if (counter == initialIndex) {
                    initialFocusComponent = newComponent;
                    if (initialFocusComponent instanceof JButton) {
                        JButton defaultB = (JButton) initialFocusComponent;
                        // For some strange reason, the default button must be
                        // focusable.
                        defaultB.setFocusable(true);
                        defaultB.addAncestorListener(new AncestorListener() {

                            public void ancestorAdded(AncestorEvent e) {
                                JButton defaultButton = (JButton) e.getComponent();
                                JRootPane root = SwingUtilities.getRootPane(defaultButton);
                                if (root != null) {
                                    root.setDefaultButton(defaultButton);
                                }
                            }

                            public void ancestorRemoved(AncestorEvent event) {
                            }

                            public void ancestorMoved(AncestorEvent event) {
                            }
                        });
                    }
                }
            }
            ((ButtonAreaLayout) container.getLayout()).setSyncAllWidths((sizeButtonsToSame && createdAll));

            /* Set the padding, windows seems to use 8 if <= 2 components,
            otherwise 4 is used. It may actually just be the size of the
            buttons is always the same, not sure. */
            if (sizeButtonsToSame && createdAll) {
                JButton aButton;
                int padSize;

                padSize = (numButtons <= 2 ? 8 : 4);

                for (int counter = 0; counter < numButtons; counter++) {
                    aButton = createdButtons[counter];
                    aButton.setMargin(new Insets(2, padSize, 2, padSize));
                }
            }
        }
    }

    /**
     * Configures any necessary colors/fonts for the specified button
     * used representing the button portion of the optionpane.
     */
    private void configureButton(JButton button) {
        Font buttonFont = UIManager.getFont("OptionPane.buttonFont");
        if (buttonFont != null) {
            button.setFont(buttonFont);
        }
        // As of Mac OS X Tiger, native buttons are focusable
        //button.setFocusPainted(false);
        // Methods.invokeIfExists(button, "setFocusable", false);

        if (button.getText() == null || button.getText().length() == 0) {
            new Throwable().printStackTrace();
            button.setText("QuaquaOptionPaneUI.hal");
        }
    }

    /**
     * Creates the appropriate object to represent <code>msg</code> and
     * places it into <code>container</code>. If <code>msg</code> is an
     * instance of Component, it is added directly, if it is an Icon,
     * a JLabel is created to represent it, otherwise a JLabel is
     * created for the string, if <code>d</code> is an Object[], this
     * method will be recursively invoked for the children.
     * <code>internallyCreated</code> is true if Objc is an instance
     * of Component and was created internally by this method (this is
     * used to correctly set hasCustomComponents only if !internallyCreated).
     */
    @Override
    protected void addMessageComponents(Container container,
            GridBagConstraints cons,
            Object msg, int maxll,
            boolean internallyCreated) {
        if (msg == null) {
            return;
        }
        if (msg instanceof Component) {
            cons.weightx = 1.0;
            cons.weighty = 1.0;
            cons.fill = GridBagConstraints.BOTH;
            container.add((Component) msg, cons);
            cons.gridy++;
            if (!internallyCreated) {
                hasCustomComponents = true;
            }

        } else if (msg instanceof Object[]) {
            cons.weightx = 0.0;
            cons.weighty = 0.0;
            cons.fill = GridBagConstraints.NONE;

            Object[] msgs = (Object[]) msg;
            for (int i = 0; i < msgs.length; i++) {
                addMessageComponents(container, cons, msgs[i], maxll, false);
            }

        } else if (msg instanceof Icon) {
            cons.weightx = 0.0;
            cons.weighty = 0.0;
            cons.fill = GridBagConstraints.NONE;
            JLabel label = new JLabel((Icon) msg, SwingConstants.CENTER);
            configureMessageLabel(label);
            addMessageComponents(container, cons, label, maxll, true);

        } else {
            cons.weightx = 0.0;
            cons.weighty = 0.0;
            cons.fill = GridBagConstraints.NONE;
            String s = msg.toString();
            int len = s.length();
            if (len <= 0) {
                return;
            }
            int nl = -1;
            int nll = 0;

            if ((nl = s.indexOf(newline)) >= 0) {
                nll = newline.length();
            } else if ((nl = s.indexOf("\r\n")) >= 0) {
                nll = 2;
            } else if ((nl = s.indexOf('\n')) >= 0) {
                nll = 1;
            }
            boolean isHTML = BasicHTML.isHTMLString(s);
            if (nl >= 0 && !isHTML) {
                // break up newlines
                if (nl == 0) {
                    addMessageComponents(container, cons, new Component() {

                        @Override
                        public Dimension getPreferredSize() {
                            Font f = getFont();

                            if (f != null) {
                                return new Dimension(1, f.getSize() + 2);
                            }
                            return new Dimension(0, 0);
                        }
                    }, maxll, true);
                } else {
                    addMessageComponents(container, cons, s.substring(0, nl),
                            maxll, false);
                }
                addMessageComponents(container, cons, s.substring(nl + nll), maxll,
                        false);

            } else if (len > maxll && !isHTML) {
                Container c = Box.createVerticalBox();
                burstStringInto(c, s, maxll);
                addMessageComponents(container, cons, c, maxll, true);

            } else {
                JLabel label;
                label = new JLabel(s, JLabel.LEADING);
                configureMessageLabel(label);
                addMessageComponents(container, cons, label, maxll, true);
            }
        }
    }

    /**
     * Configures any necessary colors/fonts for the specified label
     * used representing the message.
     */
    private void configureMessageLabel(JLabel label) {
        label.setForeground(UIManager.getColor(
                "OptionPane.messageForeground"));
// We use a plain font for HTML messages to make the examples in the
        // Java Look and Feel Guidelines work.
        boolean isHTML = BasicHTML.isHTMLString(label.getText());
        Font messageFont = (isHTML)
                ? UIManager.getFont("OptionPane.htmlMessageFont")
                : UIManager.getFont("OptionPane.messageFont");

        if (isHTML) {
            View htmlView = (View) label.getClientProperty(BasicHTML.propertyKey);
            if (htmlView != null && UIManager.getInt("OptionPane.messageLabelWidth") != 0) {
                htmlView.setSize(UIManager.getInt("OptionPane.messageLabelWidth"), 0);
            }
        }

        if (messageFont != null) {
            label.setFont(messageFont);
        }
    }

    /**
     * Returns the buttons to display from the JOptionPane the receiver is
     * providing the look and feel for. If the JOptionPane has options
     * set, they will be provided, otherwise if the optionType is
     * YES_NO_OPTION, yesNoOptions is returned, if the type is
     * YES_NO_CANCEL_OPTION yesNoCancelOptions is returned, otherwise
     * defaultButtons are returned.
     */
    @Override
    protected Object[] getButtons() {
        if (optionPane != null) {
            Object[] suppliedOptions = optionPane.getOptions();

            if (suppliedOptions == null) {
                Object[] defaultOptions;
                int type = optionPane.getOptionType();

                // With java 1.3 we can only determine the locale if we have
                // a parent.
                Locale l;
                try {
                    l = optionPane.getLocale();
                } catch (IllegalComponentStateException e) {
                    l = Locale.getDefault();
                }

                // FIXME - The following code only works when the locale of
                // the option pane is the same as the default locale.
                if (type == JOptionPane.YES_NO_OPTION) {
                    defaultOptions = new ButtonFactory[2];
                    defaultOptions[0] = new ButtonFactory(
                            UIManager.getString("OptionPane.yesButtonText"),
                            getMnemonic("OptionPane.yesButtonMnemonic", l),
                            (Icon) UIManager.get(
                            "OptionPane.yesIcon"));
                    defaultOptions[1] = new ButtonFactory(
                            UIManager.getString("OptionPane.noButtonText"),
                            getMnemonic("OptionPane.noButtonMnemonic", l),
                            (Icon) UIManager.get(
                            "OptionPane.noIcon"));
                } else if (type == JOptionPane.YES_NO_CANCEL_OPTION) {
                    defaultOptions = new ButtonFactory[3];
                    defaultOptions[0] = new ButtonFactory(
                            UIManager.getString("OptionPane.yesButtonText"),
                            getMnemonic("OptionPane.yesButtonMnemonic", l),
                            (Icon) UIManager.get(
                            "OptionPane.yesIcon"));
                    defaultOptions[1] = new ButtonFactory(
                            UIManager.getString("OptionPane.noButtonText"),
                            getMnemonic("OptionPane.noButtonMnemonic", l),
                            (Icon) UIManager.get(
                            "OptionPane.noIcon"));
                    defaultOptions[2] = new ButtonFactory(
                            UIManager.getString("OptionPane.cancelButtonText"),
                            getMnemonic("OptionPane.cancelButtonMnemonic", l),
                            (Icon) UIManager.get(
                            "OptionPane.cancelIcon"));
                } else if (type == JOptionPane.OK_CANCEL_OPTION) {
                    defaultOptions = new ButtonFactory[2];
                    defaultOptions[0] = new ButtonFactory(
                            UIManager.getString("OptionPane.okButtonText"),
                            getMnemonic("OptionPane.okButtonMnemonic", l),
                            (Icon) UIManager.get(
                            "OptionPane.okIcon"));
                    defaultOptions[1] = new ButtonFactory(
                            UIManager.getString("OptionPane.cancelButtonText"),
                            getMnemonic("OptionPane.cancelButtonMnemonic", l),
                            (Icon) UIManager.get(
                            "OptionPane.cancelIcon"));
                } else {
                    defaultOptions = new ButtonFactory[1];
                    defaultOptions[0] = new ButtonFactory(
                            UIManager.getString("OptionPane.okButtonText"),
                            getMnemonic("OptionPane.okButtonMnemonic", l),
                            (Icon) UIManager.get(
                            "OptionPane.okIcon"));
                }
                return defaultOptions;

            }
            return suppliedOptions;
        }
        return null;
    }

    /**
     * Returns the message to display from the JOptionPane the receiver is
     * providing the look and feel for.
     */
    @Override
    protected Object getMessage() {
        inputComponent = null;
        if (optionPane != null) {
            if (optionPane.getWantsInput()) {
                /* Create a user component to capture the input. If the
                selectionValues are non null the component and there
                are < 20 values it'll be a combobox, if non null and
                >= 20, it'll be a list, otherwise it'll be a textfield. */
                Object message = optionPane.getMessage();
                Object[] sValues = optionPane.getSelectionValues();
                Object inputValue = optionPane.getInitialSelectionValue();
                JComponent toAdd;

                if (sValues != null) {
                    if (sValues.length < 20) {
                        JComboBox cBox = new JComboBox();

                        cBox.setName("OptionPane.comboBox");
                        for (int counter = 0, maxCounter = sValues.length;
                                counter < maxCounter; counter++) {
                            cBox.addItem(sValues[counter]);
                        }
                        if (inputValue != null) {
                            cBox.setSelectedItem(inputValue);
                        }
                        inputComponent = cBox;
                        toAdd = cBox;

                    } else {
                        JList list = new JList(sValues);
                        JScrollPane sp = new JScrollPane(list);

                        sp.setName("OptionPane.scrollPane");
                        list.setName("OptionPane.list");
                        list.setVisibleRowCount(10);
                        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        if (inputValue != null) {
                            list.setSelectedValue(inputValue, true);
                        }
                        list.addMouseListener(getHandler());
                        toAdd = sp;
                        inputComponent = list;
                    }

                } else {
                    MultiplexingTextField tf = new MultiplexingTextField(20);

                    tf.setName("OptionPane.textField");
                    tf.setKeyStrokes(new KeyStroke[]{
                                KeyStroke.getKeyStroke("ENTER")});

                    if (optionPane.getClientProperty("PrivateQuaqua.OptionPane.InputFieldDocument") instanceof Document) {
                        tf.setDocument((Document)optionPane.getClientProperty("PrivateQuaqua.OptionPane.InputFieldDocument"));
                    }

                    if (inputValue != null) {
                        String inputString = inputValue.toString();
                        tf.setText(inputString);
                        tf.setSelectionStart(0);
                        tf.setSelectionEnd(inputString.length());
                    }
                    tf.addActionListener(getHandler());
                    toAdd = inputComponent = tf;
                }

                Object[] newMessage;

                if (message == null) {
                    newMessage = new Object[1];
                    newMessage[0] = toAdd;

                } else {
                    newMessage = new Object[2];
                    newMessage[0] = message;
                    newMessage[1] = toAdd;
                }
                return newMessage;
            }
            return optionPane.getMessage();
        }
        return null;
    }

    private int getMnemonic(String key, Locale l) {
        // FIXME - We should use get with local on Java 1.4 and above
        //String value = (String)UIManager.get(key, l);
        String value = (String) UIManager.get(key);

        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
        }
        return 0;
    }

    /**
     * Returns true, basic L&amp;F wants all the buttons to have the same
     * width.
     */
    @Override
    protected boolean getSizeButtonsToSameWidth() {
        //return true;
        return false;
    }

    /**
     * Creates and returns a Container containing the buttons. The buttons
     * are created by calling <code>getButtons</code>.
     */
    @Override
    protected Container createButtonArea() {
        // we need to call super, because the super method sets some private
        // variables in the super class.
        super.createButtonArea();

        JPanel bottom = new JPanel();
        bottom.setBorder(UIManager.getBorder("OptionPane.buttonAreaBorder"));

        Object[] buttons = getButtons();
        buttonAreaLayout = new QuaquaButtonAreaLayout(false, 12 - 6); // -6 is the visual margin
        Integer destructiveOption = (Integer) optionPane.getClientProperty("Quaqua.OptionPane.destructiveOption");
        if (destructiveOption != null) {
            buttonAreaLayout.setDestructiveOption(destructiveOption.intValue());
        }
        bottom.setLayout(buttonAreaLayout);
        addButtonComponents(bottom, buttons, getInitialValueIndex());
        //mnemonics = null;
        return bottom;
    }

    /**
     * <code>ButtonAreaLayout</code> behaves in a similar manner to
     * <code>FlowLayout</code>. It lays out all components from left to
     * right. If <code>syncAllWidths</code> is true, the widths of each
     * component will be set to the largest preferred size width.
     *
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicOptionPaneUI.
     */
    public static class QuaquaButtonAreaLayout extends BasicOptionPaneUI.ButtonAreaLayout {

        /**
         * The destructive option is placed at least 24 pixels further away
         * from the non-destructive options.
         * Set this to -1 to specify that there is no destructive option.
         */
        protected int destructiveOption = -1;
        /*
        protected boolean           syncAllWidths;
        protected int               padding;
        /** If true, children are lumped together in parent. * /
        protected boolean           centersChildren;
         */

        public QuaquaButtonAreaLayout(boolean syncAllWidths, int padding) {
            super(syncAllWidths, padding);
            /*
            this.syncAllWidths = syncAllWidths;
            this.padding = padding;
            centersChildren = true;
             */
            centersChildren = false;
        }

        public void setDestructiveOption(int newValue) {
            destructiveOption = newValue;
        }

        public int getDestructiveOption() {
            return destructiveOption;
        }

        @Override
        public void setSyncAllWidths(boolean newValue) {
            syncAllWidths = newValue;
        }

        @Override
        public boolean getSyncAllWidths() {
            return syncAllWidths;
        }

        @Override
        public void setPadding(int newPadding) {
            this.padding = newPadding;
        }

        @Override
        public int getPadding() {
            return padding;
        }

        @Override
        public void setCentersChildren(boolean newValue) {
            centersChildren = newValue;
        }

        @Override
        public boolean getCentersChildren() {
            return centersChildren;
        }

        @Override
        public void addLayoutComponent(String string, Component comp) {
        }

        @Override
        public void layoutContainer(Container container) {
            Component[] children = container.getComponents();

            if (children != null && children.length > 0) {
                int numChildren = children.length;
                Dimension[] sizes = new Dimension[numChildren];
                Insets insets = container.getInsets();
                int counter;
                int yLocation = insets.top;
                boolean ltr = container.getComponentOrientation().isLeftToRight();

                if (syncAllWidths) {
                    int maxWidth = MIN_BUTTON_WIDTH;

                    for (counter = 0; counter < numChildren; counter++) {
                        sizes[counter] = children[counter].getPreferredSize();
                        maxWidth = Math.max(maxWidth, sizes[counter].width + HORIZONTAL_BUTTON_PADDING);
                    }

                    int xLocation;
                    int xOffset = padding + maxWidth;

                    if (getCentersChildren()) {
                        xLocation = (container.getSize().width - insets.left - insets.right
                                - (maxWidth * numChildren
                                + (numChildren - 1) * padding)) / 2;
                    } else {
                        if (ltr) {
                            xLocation = container.getSize().width
                                    - insets.right
                                    - (maxWidth * numChildren
                                    + (numChildren - 1) * padding);
                        } else {
                            xLocation = insets.left;
                        }
                    }

                    // If left to right layout then adjust xLocation and
                    // xOffset to start at the right side of the container
                    // and move left.
                    if (ltr) {
                        if (numChildren > 1) {
                            xLocation += maxWidth * (numChildren - 1)
                                    + (numChildren - 1) * padding;
                        }
                        xOffset = -xOffset;
                    }

                    for (counter = 0; counter < numChildren; counter++) {
                        children[counter].setBounds(xLocation, yLocation,
                                maxWidth,
                                sizes[counter].height);
                        xLocation += xOffset;
                        if (counter == destructiveOption - 1) {
                            if (xOffset > 0) {
                                xLocation += 14;
                            } else {
                                xLocation -= 14;
                            }
                        }
                    }
                } else {
                    int totalWidth = 0;

                    for (counter = 0; counter < numChildren; counter++) {
                        sizes[counter] = children[counter].getPreferredSize();
                        sizes[counter].width = Math.max(MIN_BUTTON_WIDTH, sizes[counter].width + HORIZONTAL_BUTTON_PADDING);
                        totalWidth += sizes[counter].width;
                    }
                    totalWidth += ((numChildren - 1) * padding);

                    boolean cc = getCentersChildren();
                    int xLocation;

                    if (cc) {
                        xLocation = insets.left
                                + (container.getSize().width - insets.left - insets.right
                                - totalWidth) / 2;
                    } else {
                        if (ltr) {
                            xLocation = container.getSize().width - insets.right - sizes[0].width;
                        } else {
                            xLocation = insets.left;
                        }
                    }

                    if (ltr) {
                        // If left to right layout then adjust xLocation to
                        // start at the right side of the container.
                        for (counter = 0; counter < numChildren; counter++) {
                            children[counter].setBounds(xLocation, yLocation,
                                    sizes[counter].width, sizes[counter].height);
                            if (counter < numChildren - 1) {
                                xLocation -= padding + sizes[counter + 1].width;
                            }
                            if (counter == destructiveOption - 1) {
                                if (destructiveOption == numChildren - 1) {
                                    xLocation = insets.left;
                                } else {
                                    xLocation -= 14;
                                }
                            }
                        }
                    } else {
                        for (counter = 0; counter < numChildren; counter++) {
                            children[counter].setBounds(xLocation, yLocation,
                                    sizes[counter].width, sizes[counter].height);
                            xLocation += padding + sizes[counter].width;
                            if (counter == destructiveOption - 1) {
                                xLocation += 14;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container c) {
            if (c != null) {
                Component[] children = c.getComponents();

                if (children != null && children.length > 0) {
                    Dimension aSize;
                    int numChildren = children.length;
                    int height = 0;
                    Insets cInsets = c.getInsets();
                    int extraHeight = cInsets.top + cInsets.bottom;
                    int extraWidth = cInsets.left + cInsets.right;

                    if (syncAllWidths) {
                        int maxWidth = MIN_BUTTON_WIDTH;

                        for (int counter = 0; counter < numChildren; counter++) {
                            aSize = children[counter].getPreferredSize();
                            height = Math.max(height, aSize.height);
                            maxWidth = Math.max(maxWidth, aSize.width + HORIZONTAL_BUTTON_PADDING);
                        }
                        return new Dimension(extraWidth + (maxWidth * numChildren)
                                + (numChildren - 1) * padding
                                + ((destructiveOption != -1) ? 14 : 0),
                                extraHeight + height);
                    } else {
                        int totalWidth = 0;

                        for (int counter = 0; counter < numChildren; counter++) {
                            aSize = children[counter].getPreferredSize();
                            height = Math.max(height, aSize.height);
                            totalWidth += Math.max(MIN_BUTTON_WIDTH, aSize.width + HORIZONTAL_BUTTON_PADDING);
                        }
                        totalWidth += ((numChildren - 1) * padding);
                        return new Dimension(
                                extraWidth + totalWidth
                                + ((destructiveOption != -1) ? 14 : 0),
                                extraHeight + height);
                    }
                }
            }
            return new Dimension(0, 0);
        }

        @Override
        public Dimension preferredLayoutSize(Container c) {
            return minimumLayoutSize(c);
        }

        @Override
        public void removeLayoutComponent(Component c) {
        }
    }

    /**
     * This class is used to create the default buttons. This indirection is
     * used so that addButtonComponents can tell which Buttons were created
     * by us vs subclassers or from the JOptionPane itself.
     */
    private static class ButtonFactory {

        private String text;
        private int mnemonic;
        private Icon icon;

        ButtonFactory(String text, int mnemonic, Icon icon) {
            this.text = text;
            this.mnemonic = mnemonic;
            this.icon = icon;
        }

        JButton createButton() {
            JButton button = new JButton(text);
            if (icon != null) {
                button.setIcon(icon);
            }
            if (mnemonic != 0) {
                button.setMnemonic(mnemonic);
            }
            return button;
        }
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicOptionPaneUI.
     */
    private class Handler implements ActionListener, MouseListener,
            PropertyChangeListener {

        //
        // ActionListener
        //
        public void actionPerformed(ActionEvent e) {
            optionPane.setInputValue(((JTextField) e.getSource()).getText());
        }

        //
        // MouseListener
        //
        public void mouseClicked(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() == 2) {
                JList list = (JList) e.getSource();
                int index = list.locationToIndex(e.getPoint());

                optionPane.setInputValue(list.getModel().getElementAt(index));
            }
        }

        //
        // PropertyChangeListener
        //
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getSource() == optionPane) {
                // Option Pane Auditory Cue Activation
                // only respond to "ancestor" changes
                // the idea being that a JOptionPane gets a JDialog when it is
                // set to appear and loses it's JDialog when it is dismissed.
                if ("ancestor" == e.getPropertyName()) {
                    JOptionPane op = (JOptionPane) e.getSource();
                    boolean isComingUp;

                    // if the old value is null, then the JOptionPane is being
                    // created since it didn't previously have an ancestor.
                    if (e.getOldValue() == null) {
                        isComingUp = true;
                    } else {
                        isComingUp = false;
                    }

                    // figure out what to do based on the message type
                    /*
                    switch (op.getMessageType()) {
                    case JOptionPane.PLAIN_MESSAGE:
                    if (isComingUp) {
                    QuaquaLookAndFeel.playSound(optionPane,
                    "OptionPane.informationSound");
                    }
                    break;
                    case JOptionPane.QUESTION_MESSAGE:
                    if (isComingUp) {
                    QuaquaLookAndFeel.playSound(optionPane,
                    "OptionPane.questionSound");
                    }
                    break;
                    case JOptionPane.INFORMATION_MESSAGE:
                    if (isComingUp) {
                    QuaquaLookAndFeel.playSound(optionPane,
                    "OptionPane.informationSound");
                    }
                    break;
                    case JOptionPane.WARNING_MESSAGE:
                    if (isComingUp) {
                    QuaquaLookAndFeel.playSound(optionPane,
                    "OptionPane.warningSound");
                    }
                    break;
                    case JOptionPane.ERROR_MESSAGE:
                    if (isComingUp) {
                    QuaquaLookAndFeel.playSound(optionPane,
                    "OptionPane.errorSound");
                    }
                    break;
                    default:
                    System.err.println("Undefined JOptionPane type: " +
                    op.getMessageType());
                    break;
                    }*/
                }
                // Visual activity
                String changeName = e.getPropertyName();

                if (changeName == JOptionPane.OPTIONS_PROPERTY
                        || changeName == JOptionPane.INITIAL_VALUE_PROPERTY
                        || changeName == JOptionPane.ICON_PROPERTY
                        || changeName == JOptionPane.MESSAGE_TYPE_PROPERTY
                        || changeName == JOptionPane.OPTION_TYPE_PROPERTY
                        || changeName == JOptionPane.MESSAGE_PROPERTY
                        || changeName == JOptionPane.SELECTION_VALUES_PROPERTY
                        || changeName == JOptionPane.INITIAL_SELECTION_VALUE_PROPERTY
                        || changeName == JOptionPane.WANTS_INPUT_PROPERTY) {
                    uninstallComponents();
                    installComponents();
                    optionPane.validate();
                } else if (changeName == "componentOrientation") {
                    ComponentOrientation o = (ComponentOrientation) e.getNewValue();
                    JOptionPane op = (JOptionPane) e.getSource();
                    if (o != (ComponentOrientation) e.getOldValue()) {
                        op.applyComponentOrientation(o);
                    }
                } // Client Property
                else if ("Quaqua.OptionPane.destructiveOption" == changeName) {
                    Integer value = (Integer) e.getNewValue();
                    if (buttonAreaLayout != null) {
                        buttonAreaLayout.setDestructiveOption(
                                (value == null)
                                ? -1
                                : value.intValue());
                    }
                }
            }
        }
    }

    private static class MultiplexingTextField extends JTextField {

        private KeyStroke[] strokes;

        public MultiplexingTextField(Document doc, String text, int columns) {
            super(doc, text, columns);
        }

        public MultiplexingTextField(int cols) {
            super(cols);
        }

        /**
         * Sets the KeyStrokes that will be additional processed for
         * ancestor bindings.
         */
        public void setKeyStrokes(KeyStroke[] strokes) {
            this.strokes = strokes;
        }

        @Override
        protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                int condition, boolean pressed) {
            boolean processed = super.processKeyBinding(ks, e, condition,
                    pressed);

            if (processed && condition != JComponent.WHEN_IN_FOCUSED_WINDOW) {
                for (int counter = strokes.length - 1; counter >= 0;
                        counter--) {
                    if (strokes[counter].equals(ks)) {
                        // Returning false will allow further processing
                        // of the bindings, eg our parent Containers will get a
                        // crack at them.
                        return false;
                    }
                }
            }
            return processed;
        }
    }
}
