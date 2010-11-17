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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.html.HTMLDocument;

import seco.util.IconManager;

/**
 *
 *  @author  Martin Roskanin, Dusan Balek
 */
public class DocumentationScrollPane extends JScrollPane {

    private static final String BACK = "seco/notebook/syntax/resources/back.png"; //NOI18N
    private static final String FORWARD = "seco/notebook/syntax/resources/forward.png"; //NOI18N
    private static final String GOTO_SOURCE = "seco/notebook/syntax/resources/open_source_in_editor.png"; //NOI18N
    private static final String SHOW_WEB = "seco/notebook/syntax/resources/open_in_external_browser.png"; //NOI18N

    private static final String JAVADOC_ESCAPE = "javadoc-escape"; //NOI18N
    private static final String JAVADOC_BACK = "javadoc-back"; //NOI18N
    private static final String JAVADOC_FORWARD = "javadoc-forward"; //NOI18N    
    private static final String JAVADOC_OPEN_IN_BROWSER = "javadoc-open-in-browser"; //NOI18N    
    private static final String JAVADOC_OPEN_SOURCE = "javadoc-open-source"; //NOI18N    
    
    private static final int ACTION_JAVADOC_ESCAPE = 0;
    private static final int ACTION_JAVADOC_BACK = 1;
    private static final int ACTION_JAVADOC_FORWARD = 2;
    private static final int ACTION_JAVADOC_OPEN_IN_BROWSER = 3;
    private static final int ACTION_JAVADOC_OPEN_SOURCE = 4;

    private JButton bBack, bForward, bGoToSource, bShowWeb;    
    private HTMLDocView view;
    
    // doc browser history
    private List/*<CompletionDocItem>*/ history = new ArrayList(5);
    private int currentHistoryIndex = -1;
    private CompletionDocumentation currentDocumentation = null;
    
    private Dimension documentationPreferredSize;

    /** Creates a new instance of ScrollJavaDocPane */
    public DocumentationScrollPane(JTextComponent editorComponent) {
        super();
 
        // Determine and use fixed preferred size
        documentationPreferredSize = CompletionSettings.INSTANCE.documentationPopupPreferredSize();
        setPreferredSize(null); // Use the documentationPopupPreferredSize
        
        Color bgColor = CompletionSettings.INSTANCE.documentationBackgroundColor();

        // Add the completion doc view
        view = new HTMLDocView(bgColor);
        view.addHyperlinkListener(new HyperlinkAction());
        setViewportView(view);
        
        installTitleComponent();
        installKeybindings(editorComponent);
    }
    
    public void setPreferredSize(Dimension preferredSize) {
        if (preferredSize == null) {
            preferredSize = documentationPreferredSize;
        }
        super.setPreferredSize(preferredSize);
    }
    
    
    public void setData(CompletionDocumentation doc) {
        setDocumentation(doc);
        addToHistory(doc);
    }
    
    private ImageIcon resolveIcon(String res){
    	return new ImageIcon(IconManager.getIcon(res)); 
    }

    private void installTitleComponent() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);        
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("controlDkShadow"))); //NOI18N
        toolbar.setLayout(new GridBagLayout());

        GridBagConstraints gdc = new GridBagConstraints();
        gdc.gridx = 0;
        gdc.gridy = 0;
        gdc.anchor = GridBagConstraints.WEST;        
        ImageIcon icon = resolveIcon(BACK);
        if (icon != null) {            
            bBack = new BrowserButton(icon);
            bBack.addMouseListener(new MouseEventListener(bBack));
            bBack.setEnabled(false);
            bBack.setContentAreaFilled(false);
            bBack.setMargin(new Insets(0, 0, 0, 0));
            bBack.setToolTipText("Back"); //NOI18N
            toolbar.add(bBack, gdc);
        }
        
        gdc.gridx = 1;
        gdc.gridy = 0;
        gdc.anchor = GridBagConstraints.WEST;        
        icon = resolveIcon(FORWARD);
        if (icon != null) {
            bForward = new BrowserButton(icon);
            bForward.addMouseListener(new MouseEventListener(bForward));
            bForward.setEnabled(false);
            bForward.setContentAreaFilled(false);
            bForward.setToolTipText("Forward"); //NOI18N
            bForward.setMargin(new Insets(0, 0, 0, 0));
            toolbar.add(bForward, gdc);
        }
        
        gdc.gridx = 2;
        gdc.gridy = 0;
        gdc.anchor = GridBagConstraints.WEST;        
        icon = resolveIcon(SHOW_WEB);
        if (icon != null) {            
            bShowWeb = new BrowserButton(icon);
            bShowWeb.addMouseListener(new MouseEventListener(bShowWeb));
            bShowWeb.setEnabled(false);
            bShowWeb.setContentAreaFilled(false);
            bShowWeb.setMargin(new Insets(0, 0, 0, 0));
            bShowWeb.setToolTipText("Open A Web Browser"); //NOI18N
            toolbar.add(bShowWeb, gdc);
        }
        
        gdc.gridx = 3;
        gdc.gridy = 0;
        gdc.weightx = 1.0;
        gdc.anchor = GridBagConstraints.WEST;                
        icon = resolveIcon(GOTO_SOURCE);
        if (icon != null) {
            bGoToSource = new BrowserButton(icon);
            bGoToSource.addMouseListener(new MouseEventListener(bGoToSource));
            bGoToSource.setEnabled(false);
            bGoToSource.setContentAreaFilled(false);
            bGoToSource.setMargin(new Insets(0, 0, 0, 0));
            bGoToSource.setToolTipText("Go To Source"); //NOI18N
            toolbar.add(bGoToSource, gdc);
        }
        setColumnHeaderView(toolbar);
    }
    
    private synchronized void setDocumentation(CompletionDocumentation doc) {
        currentDocumentation = doc;
        String text = currentDocumentation.getText();
        URL url = currentDocumentation.getURL();
        if (text != null){
            if (url!=null){
                // fix of issue #58658
                javax.swing.text.Document document = view.getDocument();
                if (document instanceof HTMLDocument){
                    ((HTMLDocument)document).setBase(url);
                }
            }
            view.setContent(text);
        } else if (url != null){
            try{
                view.setPage(url);
            }catch(IOException ioe){
                //StatusDisplayer.getDefault().setStatusText(ioe.toString());
            }
        }
        bShowWeb.setEnabled(url != null);
        bGoToSource.setEnabled(currentDocumentation.getGotoSourceAction() != null);
    }
    
    private synchronized void addToHistory(CompletionDocumentation doc) {
        int histSize = history.size();
        for (int i = currentHistoryIndex + 1; i < histSize; i++){
            history.remove(history.size() - 1);
        }
        history.add(doc);
        currentHistoryIndex = history.size() - 1;
        if (currentHistoryIndex > 0)
            bBack.setEnabled(true);
        bForward.setEnabled(false);
    }
    
    private synchronized void backHistory() {
        if (currentHistoryIndex > 0) {
            currentHistoryIndex--;
            setDocumentation((CompletionDocumentation)history.get(currentHistoryIndex));            
            if (currentHistoryIndex == 0)
                bBack.setEnabled(false);
            bForward.setEnabled(true);
        }
    }
    
    private synchronized void forwardHistory(){
        if (currentHistoryIndex <history.size()-1){
            currentHistoryIndex++;
            setDocumentation((CompletionDocumentation)history.get(currentHistoryIndex));
            if (currentHistoryIndex == history.size() - 1)
                bForward.setEnabled(false); 
            bBack.setEnabled(true);
        }
    }
    
    synchronized void clearHistory(){
        currentHistoryIndex = -1;
        history.clear();
        bBack.setEnabled(false);
        bForward.setEnabled(false);
    }

    private void openInExternalBrowser(){
        //???URL url = currentDocumentation.getURL();
        //if (url != null)
        //    HtmlBrowser.URLDisplayer.getDefault().showURL(url);
    }
    
    private void goToSource() {
        Action action = currentDocumentation.getGotoSourceAction();
        if (action != null)
            action.actionPerformed(new ActionEvent(currentDocumentation, 0, null));        
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
        }*/
        return ret;
    }

    private void registerKeybinding(int action, String actionName, KeyStroke stroke, String editorActionName, JTextComponent component){
        KeyStroke[] keys = findEditorKeys(editorActionName, stroke, component);
        for (int i = 0; i < keys.length; i++) {
            getInputMap().put(keys[i], actionName);
        }
        getActionMap().put(actionName, new DocPaneAction(action));
    }
    
    private void installKeybindings(JTextComponent component) {
	// Register Escape key
       // ???registerKeybinding(ACTION_JAVADOC_ESCAPE, JAVADOC_ESCAPE,
       // KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
       // ExtKit.escapeAction, component);

        // Register javadoc back key
        registerKeybinding(ACTION_JAVADOC_BACK, JAVADOC_BACK,
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_MASK),
        null, component);

        // Register javadoc forward key
        registerKeybinding(ACTION_JAVADOC_FORWARD, JAVADOC_FORWARD,
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK),
        null, component);

        // Register open in external browser key
        registerKeybinding(ACTION_JAVADOC_OPEN_IN_BROWSER, JAVADOC_OPEN_IN_BROWSER,
        KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK),
        null, component);

        // Register open the source in editor key
        registerKeybinding(ACTION_JAVADOC_OPEN_SOURCE, JAVADOC_OPEN_SOURCE,
        KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_MASK | KeyEvent.CTRL_MASK),
        null, component);
        
        // Register movement keystrokes to be reachable through Shift+<orig-keystroke>
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.CTRL_MASK));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.CTRL_MASK));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
        mapWithShift(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
    }        
    
    private void mapWithShift(KeyStroke key) {
        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Object actionKey = inputMap.get(key);
        if (actionKey != null) {
            key = KeyStroke.getKeyStroke(key.getKeyCode(), key.getModifiers() | InputEvent.SHIFT_MASK);
            getInputMap().put(key, actionKey);
        }
    }
    
    private class BrowserButton extends JButton {
        public BrowserButton() {
            setBorderPainted(false);
            setFocusPainted(false);
        }
        
        public BrowserButton(String text){
            super(text);
            setBorderPainted(false);
            setFocusPainted(false);
        }
        
        public BrowserButton(Icon icon){
            super(icon);
            setBorderPainted(false);
            setFocusPainted(false);
        }

        public void setEnabled(boolean b) {
            super.setEnabled(b);
        }
        
        
    }

    private class MouseEventListener extends MouseAdapter {
        
        private JButton button;
        
        MouseEventListener(JButton button) {
            this.button = button;
        }
        
        public void mouseEntered(MouseEvent ev) {
            if (button.isEnabled()){
                button.setContentAreaFilled(true);
                button.setBorderPainted(true);
            }
        }
        public void mouseExited(MouseEvent ev) {
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
        }
        
        public void mouseClicked(MouseEvent evt) {
            if (button.equals(bBack)){
                backHistory();
            }else if(button.equals(bForward)){
                forwardHistory();
            }else if(button.equals(bGoToSource)){
                goToSource();
            }else if (button.equals(bShowWeb)){
                openInExternalBrowser();
            }
        }
    }

    private class HyperlinkAction implements HyperlinkListener {
        
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e != null && HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                final String desc = e.getDescription();
                if (desc != null) {
                    CompletionDocumentation doc = currentDocumentation.resolveLink(desc);
                    if (doc == null) {
                        doc = new CompletionDocumentation() {
                            private URL base = currentDocumentation.getURL();
                            public Action getGotoSourceAction() {
                                return null;
                            }
                            public String getText() {
                                return null;
                            }
                            public URL getURL() {
                                try {
                                    return new URL(base, desc);
                                } catch (MalformedURLException ex) {
                                }
                                return null;
                            }
                            public CompletionDocumentation resolveLink(String link) {
                                return null;
                            }
                        };
                    }
                    setData(doc);
                }                    
            }
        }
    }
    
    private class DocPaneAction extends AbstractAction {
        private int action;
        
        private DocPaneAction(int action) {
            this.action = action;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            switch (action) {
		case ACTION_JAVADOC_ESCAPE:
		    Completion.get().hideDocumentation();
		    break;
                case ACTION_JAVADOC_BACK:
                    backHistory();
                    break;
                case ACTION_JAVADOC_FORWARD:
                    forwardHistory();
                    break;
                case ACTION_JAVADOC_OPEN_IN_BROWSER:
                    openInExternalBrowser();
                    break;
                case ACTION_JAVADOC_OPEN_SOURCE:
                    goToSource();
                    break;
            }
            
        }
    }
}
