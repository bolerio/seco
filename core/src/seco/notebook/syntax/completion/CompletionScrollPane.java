/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package seco.notebook.syntax.completion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.EditorKit;

import seco.notebook.NotebookEditorKit;



/**
* Pane displaying the completion view and accompanying components
* like label for title etc.
*
* @author Miloslav Metelka, Martin Roskanin, Dusan Balek
* @version 1.00
*/

public class CompletionScrollPane extends JScrollPane {
    
    private static final String ESCAPE = "escape"; //NOI18N
    private static final String COMPLETION_UP = "completion-up"; //NOI18N
    private static final String COMPLETION_DOWN = "completion-down"; //NOI18N
    private static final String COMPLETION_PGUP = "completion-pgup"; //NOI18N
    private static final String COMPLETION_PGDN = "completion-pgdn"; //NOI18N
    private static final String COMPLETION_BEGIN = "completion-begin"; //NOI18N
    private static final String COMPLETION_END = "completion-end"; //NOI18N

    private static final int ACTION_ESCAPE = 0;
    private static final int ACTION_COMPLETION_UP = 1;
    private static final int ACTION_COMPLETION_DOWN = 2;
    private static final int ACTION_COMPLETION_PGUP = 3;
    private static final int ACTION_COMPLETION_PGDN = 4;
    private static final int ACTION_COMPLETION_BEGIN = 5;
    private static final int ACTION_COMPLETION_END = 6;

    private CompletionJList view;
    
    private List dataObj;
    
    private JLabel topLabel;
    
    public CompletionScrollPane(JTextComponent editorComponent,
    ListSelectionListener listSelectionListener, MouseListener mouseListener) {
        
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Use maximumSize property to store the limit of the preferred size
        setMaximumSize(CompletionSettings.INSTANCE.completionPopupMaximumSize());
        // At least 2 items; do -1 for title height
        int maxVisibleRowCount = Math.max(2,
            getMaximumSize().height / CompletionLayout.COMPLETION_ITEM_HEIGHT - 1);

        // Add the completion view
        view = new CompletionJList(maxVisibleRowCount, mouseListener);
        if (listSelectionListener != null) {
            view.addListSelectionListener(listSelectionListener);
        }
        setViewportView(view);
        installKeybindings(editorComponent);
    }
    
    public void setData(List data, String title) {
        dataObj = data;
        view.setData(data);
        setTitle(title);
        // Force the viewport preferred size to be taken into account
        // Otherwise the scroll pane attempts to retain its size
        // so e.g. if the number of visible rows increases so the vertical
        // scrollbar would be needed the scrollpane does not increase
        // its preferred size.
        // Resetting of viewport fixes the problem.
        setViewportView(getViewport().getView());
    }

    public CompletionItem getSelectedCompletionItem() {
        Object ret = view.getSelectedValue();
        return ret instanceof CompletionItem ? (CompletionItem) ret : null;
    }
    
    public Dimension getPreferredSize() {
        Dimension prefSize = super.getPreferredSize();
        Dimension labelSize = topLabel != null ? topLabel.getPreferredSize() : new Dimension(0, 0);
        Dimension maxSize = getMaximumSize();
        if (labelSize.width > prefSize.width) {
            prefSize.width = labelSize.width;
        }
        if (prefSize.width > maxSize.width) {
            prefSize.width = maxSize.width;
        }
        // Height is covered by maxVisibleRowCount value
        //System.out.println("CompletionScrollPane-getPrefSize(): " + 
        //		(topLabel != null ? topLabel.getText() : "null") + 
        //		" :" + maxSize.width + ":" + labelSize.width);
        return new Dimension(400, prefSize.height); //???prefSize; 
    }
    
    private void setTitle(String title) {
        if (title == null) {
            if (topLabel != null) {
                setColumnHeader(null);
                topLabel = null;
            }
        } else {
            if (topLabel != null) {
                topLabel.setText(title);
            } else {
                topLabel = new JLabel(title);
                topLabel.setForeground(Color.blue);
                topLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
                setColumnHeaderView(topLabel);
            }
        }
    }

    /** Attempt to find the editor keystroke for the given editor action. */
    private KeyStroke[] findEditorKeys(String editorActionName, KeyStroke defaultKey, JTextComponent component) {
        // This method is implemented due to the issue
        // #25715 - Attempt to search keymap for the keybinding that logically corresponds to the action
        KeyStroke[] ret = new KeyStroke[] { defaultKey };
        /*???
        if (component != null) {
            TextUI ui = component.getUI();
            Keymap km = component.getKeymap();
            if (ui != null && km != null) {
                EditorKit kit = ui.getEditorKit(component);
                if (kit instanceof BaseKit) {
                    Action a = ((BaseKit)kit).getActionByName(editorActionName);
                    if (a != null) {
                        KeyStroke[] keys = km.getKeyStrokesForAction(a);
                        if (keys != null && keys.length > 0) {
                            ret = keys;
                        }
                    }
                }
            }
        }
        */
        return ret;
    }

    private void registerKeybinding(int action, String actionName, KeyStroke stroke, String editorActionName, JTextComponent component){
        KeyStroke[] keys = findEditorKeys(editorActionName, stroke, component);
        for (int i = 0; i < keys.length; i++) {
            getInputMap().put(keys[i], actionName);
        }
        getActionMap().put(actionName, new CompletionPaneAction(action));
    }

    private void installKeybindings(JTextComponent component) {
	   // Register Escape key
    	registerKeybinding(ACTION_ESCAPE, ESCAPE,
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        NotebookEditorKit.escapeAction, component);

        // Register up key
        registerKeybinding(ACTION_COMPLETION_UP, COMPLETION_UP,
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
        NotebookEditorKit.upAction, component);

        // Register down key
        registerKeybinding(ACTION_COMPLETION_DOWN, COMPLETION_DOWN,
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
        NotebookEditorKit.downAction, component);

        // Register PgDn key
        registerKeybinding(ACTION_COMPLETION_PGDN, COMPLETION_PGDN,
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
        NotebookEditorKit.pageDownAction, component);

        // Register PgUp key
        registerKeybinding(ACTION_COMPLETION_PGUP, COMPLETION_PGUP,
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
        NotebookEditorKit.pageUpAction, component);

        // Register home key
        registerKeybinding(ACTION_COMPLETION_BEGIN, COMPLETION_BEGIN,
        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
        NotebookEditorKit.beginLineAction, component);

        // Register end key
        registerKeybinding(ACTION_COMPLETION_END, COMPLETION_END,
        KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
        NotebookEditorKit.endLineAction, component);
    }

    List testGetData() {
        return dataObj;
    }
    
    private class CompletionPaneAction extends AbstractAction {
        private int action;

        private CompletionPaneAction(int action) {
            this.action = action;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            switch (action) {
		case ACTION_ESCAPE:
		    Completion.get().hideCompletion();
		    break;
                case ACTION_COMPLETION_UP:
                    view.up();
                    break;
                case ACTION_COMPLETION_DOWN:
                    view.down();
                    break;
                case ACTION_COMPLETION_PGUP:
                    view.pageUp();
                    break;
                case ACTION_COMPLETION_PGDN:
                        view.pageDown();
                    break;
                case ACTION_COMPLETION_BEGIN:
                        view.begin();
                    break;
                case ACTION_COMPLETION_END:
                        view.end();
                    break;
            }
        }
    }
}

