/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import javax.swing.*;

//import org.openide.util.Utilities;

/** Permits dialogs to be displayed.
 * @author Jesse Glick
 * @since 3.14
 */
public abstract class DialogDisplayer {

    /** Get the default dialog displayer.
     * @return the default instance from lookup
     */
    public static DialogDisplayer getDefault() {
        DialogDisplayer dd = null;
        //(DialogDisplayer)Lookup.getDefault().lookup(DialogDisplayer.class);
        if (dd == null) {
            dd = new Trivial();
        }
        return dd;
    }

    /** Subclass constructor. */
    protected DialogDisplayer() {}

    /** Notify the user of something in a message box, possibly with feedback.
     * <p>To support both GUI and non-GUI use, this method may be called
     * from any thread (providing you are not holding any locks), and
     * will block the caller's thread. In GUI mode, it will be run in the AWT
     * event thread automatically. If you wish to hold locks, or do not
     * need the result object immediately or at all, please make this call
     * asynchronously (e.g. from the request processor).
     * @param descriptor description of the notification
     * @return the option that caused the message box to be closed
     */
    public abstract Object notify(NotifyDescriptor descriptor);
    
    /** Get a new standard dialog.
     * The dialog is designed and created as specified in the parameter.
     * Anyone who wants a dialog with standard buttons and
     * standard behavior should use this method.
     * <p><strong>Do not cache</strong> the resulting dialog if it
     * is modal and try to reuse it! Always create a new dialog
     * using this method if you need to show a dialog again.
     * Otherwise previously closed windows can reappear.
     * @param descriptor general description of the dialog
     * @return the new dialog
     */
    public abstract Dialog createDialog(DialogDescriptor descriptor);
    
    /**
     * Minimal implementation suited for standalone usage.
     * @see "#30031"
     */
    private static final class Trivial extends DialogDisplayer {
        
        public Object notify(NotifyDescriptor nd) {
            JDialog dialog = new StandardDialog(nd.getFrame(), nd.getTitle(), true, nd, null, null);
            dialog.show();
            return nd.getValue() != null ? nd.getValue() : NotifyDescriptor.CLOSED_OPTION;
        }
        
        public Dialog createDialog(final DialogDescriptor dd) {
            final StandardDialog dialog = new StandardDialog(dd.getFrame(), dd.getTitle(), dd.isModal(), dd, dd.getClosingOptions(), dd.getButtonListener());
            dd.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    String pname = ev.getPropertyName();
                    if (NotifyDescriptor.PROP_TITLE.equals(pname)) {
                        dialog.setTitle(dd.getTitle());
                    } else if (NotifyDescriptor.PROP_MESSAGE.equals(pname)) {
                        dialog.updateMessage();
                        dialog.validate();
                        dialog.repaint();
                    } else if (NotifyDescriptor.PROP_OPTIONS.equals(pname) || NotifyDescriptor.PROP_OPTION_TYPE.equals(pname)) {
                        dialog.updateOptions();
                        dialog.validate();
                        dialog.repaint();
                    } else {
                        // Currently other kinds of changes are ignored.
                        // Some may be private undocumented change events anyway.
                        //System.err.println("WARNING - ignoring change " + pname);
                    }
                    // XXX currently PROP_VALID not handled
                }
            });
            return dialog;
        }
        
        private static final class StandardDialog extends JDialog {
            private final NotifyDescriptor nd;
            private Component messageComponent;
            private final JPanel buttonPanel;
            private final Object[] closingOptions;
            private final ActionListener buttonListener;
            private boolean haveFinalValue = false;
            
            public StandardDialog(Frame frame, String title, boolean modal, NotifyDescriptor nd, Object[] closingOptions, ActionListener buttonListener) {
                //we should pass a frame in standalone mode
                super(frame, title, modal);
                this.nd = nd;
                this.closingOptions = closingOptions;
                this.buttonListener = buttonListener;
                getContentPane().setLayout(new BorderLayout());
                setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                updateMessage();
                buttonPanel = new JPanel();
                buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
                updateOptions();
                getContentPane().add(buttonPanel, BorderLayout.SOUTH, 1);
                KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
                Object actionKey = "cancel"; // NOI18N
                getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(k, actionKey);
                Action cancelAction = new AbstractAction() {
                    public void actionPerformed(ActionEvent ev) {
                        cancel();
                    }
                };
                getRootPane().getActionMap().put(actionKey, cancelAction);
                addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent ev) {
                        if (!haveFinalValue) {
                            StandardDialog.this.nd.setValue(NotifyDescriptor.CLOSED_OPTION);
                        }
                    }
                });
                pack();
                Rectangle r = new Rectangle(1028,800);//Utilities.getUsableScreenBounds();
                int maxW = r.width * 9 / 10;
                int maxH = r.height * 9 / 10;
                Dimension d = getPreferredSize();
                d.width = Math.min(d.width, maxW);
                d.height = Math.min(d.height, maxH);
                Rectangle bounds = new Rectangle(r.x + (r.width - d.width) / 2,
                             r.y + (r.height - d.height) / 2,
                             d.width,
                             d.height);
                setBounds(bounds);
                //setBounds(Utilities.findCenterBounds(d));
            }
            
            private void cancel() {
                nd.setValue(NotifyDescriptor.CANCEL_OPTION);
                haveFinalValue = true;
                setVisible(false);
            }
            
            public void updateMessage() {
                if (messageComponent != null) {
                    getContentPane().remove(messageComponent);
                }
                //System.err.println("updateMessage: " + nd.getMessage());
                messageComponent = message2Component(nd.getMessage());
                getContentPane().add(messageComponent, BorderLayout.CENTER);
            }
            
            public void updateOptions() {
                Object[] options = nd.getOptions();
                if (options == null) {
                    switch (nd.getOptionType()) {
                    case NotifyDescriptor.DEFAULT_OPTION:
                    case NotifyDescriptor.OK_CANCEL_OPTION:
                        options = new Object[] {
                            NotifyDescriptor.OK_OPTION,
                            NotifyDescriptor.CANCEL_OPTION,
                        };
                        break;
                    case NotifyDescriptor.YES_NO_OPTION:
                        options = new Object[] {
                            NotifyDescriptor.YES_OPTION,
                            NotifyDescriptor.NO_OPTION,
                        };
                        break;
                    case NotifyDescriptor.YES_NO_CANCEL_OPTION:
                        options = new Object[] {
                            NotifyDescriptor.YES_OPTION,
                            NotifyDescriptor.NO_OPTION,
                            NotifyDescriptor.CANCEL_OPTION,
                        };
                        break;
                    default:
                        throw new IllegalArgumentException();
                    }
                }
                //System.err.println("prep: " + Arrays.asList(options) + " " + Arrays.asList(closingOptions) + " " + buttonListener);
                buttonPanel.removeAll();
                JRootPane rp = getRootPane();
                for (int i = 0; i < options.length; i++) {
                    buttonPanel.add(option2Button(options[i], nd, makeListener(options[i]), rp));
                }
                options = nd.getAdditionalOptions();
                if (options != null) {
                    for (int i = 0; i < options.length; i++) {
                        buttonPanel.add(option2Button(options[i], nd, makeListener(options[i]), rp));
                    }
                }
            }
            
            private ActionListener makeListener(final Object option) {
                return new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //System.err.println("actionPerformed: " + option);
                        nd.setValue(option);
                        if (buttonListener != null) {
                            // #34485: some listeners expect that the action source is the option, not the button
                            ActionEvent e2 = new ActionEvent(option, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers());
                            buttonListener.actionPerformed(e2);
                        }
                        if (closingOptions == null || Arrays.asList(closingOptions).contains(option)) {
                            haveFinalValue = true;
                            setVisible(false);
                        }
                    }
                };
            }

        }
        
        /**
         * Given a message object, create a displayable component from it.
         */
        private static Component message2Component(Object message) {
            if (message instanceof Component) {
                return (Component)message;
            } else if (message instanceof Object[]) {
                Object[] sub = (Object[])message;
                JPanel panel = new JPanel();
                panel.setLayout(new FlowLayout());
                for (int i = 0; i < sub.length; i++) {
                    panel.add(message2Component(sub[i]));
                }
                return panel;
            } else if (message instanceof Icon) {
                return new JLabel((Icon)message);
            } else {
                // bugfix #35742, used JTextArea to correctly word-wrapping
                String text = message.toString();
                JTextArea area = new JTextArea(text);
                Color c = UIManager.getColor("Label.background"); // NOI18N
                if (c != null) {
                    area.setBackground(c);
                }
                area.setLineWrap(true);
                area.setWrapStyleWord (true);
                area.setEditable(false);
                area.setTabSize(4); // looks better for module sys messages than 8
                // XXX not so nice, but if you don't do something, it will be too
                // rectangular even for short messages:
                // +----------------+
                // |Are you sure you|
                // |want to do this |
                // |right now?      |
                // |        Yes  No |
                // +----------------+
                area.setColumns(40);
                if (text.indexOf('\n') != -1) {
                    // Complex multiline message.
                    return new JScrollPane(area);
                } else {
                    // Simple message.
                    return area;
                }
            }
        }
        
        private static Component option2Button(Object option, NotifyDescriptor nd, ActionListener l, JRootPane rp) {
            if (option instanceof AbstractButton) {
                AbstractButton b = (AbstractButton)option;
                b.addActionListener(l);
                return b;
            } else if (option instanceof Component) {
                return (Component)option;
            } else if (option instanceof Icon) {
                return new JLabel((Icon)option);
            } else {
                String text;
                boolean defcap;
                if (option == NotifyDescriptor.OK_OPTION) {
                    text = "OK"; // XXX I18N
                    defcap = true;
                } else if (option == NotifyDescriptor.CANCEL_OPTION) {
                    text = "Cancel"; // XXX I18N
                    defcap = false;
                } else if (option == NotifyDescriptor.YES_OPTION) {
                    text = "Yes"; // XXX I18N
                    defcap = true;
                } else if (option == NotifyDescriptor.NO_OPTION) {
                    text = "No"; // XXX I18N
                    defcap = false;
                } else if (option == NotifyDescriptor.CLOSED_OPTION) {
                    throw new IllegalArgumentException();
                } else {
                    text = option.toString();
                    defcap = false;
                }
                JButton b = new JButton(text);
                if (defcap && rp.getDefaultButton() == null) {
                    rp.setDefaultButton(b);
                }
                b.addActionListener(l);
                return b;
            }
        }
        
    }

}

