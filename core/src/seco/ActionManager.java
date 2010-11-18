/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco;

import static seco.notebook.Actions.COPY;
import static seco.notebook.Actions.CUT;
import static seco.notebook.Actions.PASTE;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDHandleFactory;

import seco.gui.GUIHelper.ExportAction;
import seco.gui.GUIHelper.ImportAction;
import seco.gui.GUIHelper.NewAction;
import seco.gui.GUIHelper.OpenAction;
import seco.notebook.NotebookEditorKit;
import seco.util.IconManager;

public class ActionManager
{
    public static final HGPersistentHandle HANDLE = UUIDHandleFactory.I.makeHandle("73723e60-043c-11df-8a39-0800200c9a66");
    private static ActionManager instance;
    private Map<String, Action> actionMap = new HashMap<String, Action>();

    public static ActionManager getInstance()
    {
        if (instance == null)
        {
            instance = ThisNiche.graph.get(HANDLE);
            if (instance == null)
            {
                instance = new ActionManager(true);
                ThisNiche.graph.define(HANDLE, instance);
            }
        }
        return instance;
    }

    public Collection<Action> getActions()
    {
        return actionMap.values();
    }

    public Map<String, Action> getActionMap()
    {
        return actionMap;
    }

    public void setActionMap(Map<String, Action> actions)
    {
        this.actionMap = actions;
    }

    public Action getAction(String name)
    {
        return actionMap.get(name);
    }

    public Action putAction(Action a)
    {
        return putAction(a, true);
    }

    public Action putAction(Action a, boolean persist)
    {
        actionMap.put((String) a.getValue(Action.NAME), a);
        if (persist) update();
        return a;
    }

    public Action putAction(Action a, KeyStroke k)
    {
        return putAction(a, k, true);
    }

    public Action putAction(Action a, KeyStroke k, boolean persist)
    {
        a.putValue(Action.ACCELERATOR_KEY, k);
        actionMap.put((String) a.getValue(Action.NAME), a);
        if (persist) update();
        return a;
    }

    public Action putAction(Action a, KeyStroke k, Icon icon)
    {
        return putAction(a, k, icon, true);
    }

    public Action putAction(Action a, KeyStroke k, Icon icon, boolean persist)
    {
        a.putValue(Action.SMALL_ICON, icon);
        return putAction(a, k, persist);
    }

    private void update()
    {
        ThisNiche.graph.update(this);
    }

    public ActionManager()
    {
    }

    private ActionManager(boolean init)
    {
        init();
    }

    private void init()
    {
        // Notebook
        putAction(new NewAction(), KeyStroke.getKeyStroke(KeyEvent.VK_N,
                ActionEvent.CTRL_MASK), false);
        putAction(new OpenAction(), KeyStroke.getKeyStroke(KeyEvent.VK_O,
                ActionEvent.CTRL_MASK), false);
        putAction(new ImportAction(), KeyStroke.getKeyStroke(KeyEvent.VK_I,
                ActionEvent.CTRL_MASK), false);
        putAction(new ExportAction(), false);
        // Edit
        putAction(NotebookEditorKit.undo, KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                ActionEvent.CTRL_MASK), IconManager.resolveIcon("Undo16.gif"),
                false);
        putAction(NotebookEditorKit.redo, KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                ActionEvent.CTRL_MASK), IconManager.resolveIcon("Redo16.gif"),
                false);
        NotebookEditorKit kit = new NotebookEditorKit();
        Action act = kit.getActionByName(DefaultEditorKit.cutAction);
        if(act !=  null){
        act.putValue(Action.NAME, CUT);
        act.putValue(Action.SHORT_DESCRIPTION, "Cut");
        putAction(act, KeyStroke.getKeyStroke(KeyEvent.VK_X,
                ActionEvent.CTRL_MASK), IconManager.resolveIcon("Cut16.gif"),
                false);}
        act = kit.getActionByName(DefaultEditorKit.copyAction);
        if(act !=  null){
        act.putValue(Action.SHORT_DESCRIPTION, "Copy");
        act.putValue(Action.NAME, COPY);
        putAction(act, KeyStroke.getKeyStroke(KeyEvent.VK_C,
                ActionEvent.CTRL_MASK), IconManager.resolveIcon("Copy16.gif"),
                false);
        }
        act = kit.getActionByName(DefaultEditorKit.pasteAction);
        if(act !=  null){
        act.putValue(Action.NAME, PASTE);
        act.putValue(Action.SHORT_DESCRIPTION, "Paste");
        putAction(act, KeyStroke.getKeyStroke(KeyEvent.VK_V,
                ActionEvent.CTRL_MASK), IconManager.resolveIcon("Paste16.gif"),
                false);
        }

        act = kit.getActionByName(NotebookEditorKit.selectAllAction);
        act.putValue(Action.NAME, "Select All");
        putAction(act, KeyStroke.getKeyStroke(KeyEvent.VK_A,
                ActionEvent.CTRL_MASK), false);
        putAction(kit.getActionByName(NotebookEditorKit.findAction), KeyStroke
                .getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK), false);
        putAction(kit.getActionByName(NotebookEditorKit.replaceAction),
                KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK),
                false);

        //Format
        putAction(kit.getActionByName(NotebookEditorKit.shortcutInspectorAction),
                false);
        putAction(kit.getActionByName(NotebookEditorKit.abbreviationManagerAction),
                false);
        // Tools
        putAction(kit.getActionByName(NotebookEditorKit.evalAction), false);
        putAction(kit.getActionByName(NotebookEditorKit.mergeCellsAction),
                KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK),
                false);
        putAction(kit.getActionByName(NotebookEditorKit.evalCellGroupAction),
                false);
        putAction(kit
                .getActionByName(NotebookEditorKit.reEvalOutputCellsAction),
                false);
        putAction(kit
                .getActionByName(NotebookEditorKit.removeOutputCellsAction),
                false);
        putAction(kit
                .getActionByName(NotebookEditorKit.clearEngineContextAction),
                false);
        putAction(kit.getActionByName(NotebookEditorKit.javaDocManagerAction),
                false);
        putAction(kit.getActionByName(NotebookEditorKit.ctxInspectorAction),
                false);
        // NBUI
        putAction(kit.getActionByName(NotebookEditorKit.removeTabAction),
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                        InputEvent.SHIFT_DOWN_MASK), false);
        putAction(kit.getActionByName(NotebookEditorKit.addRemoveCommentsAction),
                KeyStroke.getKeyStroke(KeyEvent.VK_C,
                        InputEvent.SHIFT_MASK
                        | InputEvent.CTRL_MASK), false);
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,
                InputEvent.SHIFT_DOWN_MASK);
        putAction(kit.getActionByName(NotebookEditorKit.deleteCellAction), key,
                false);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK);
        putAction(
                kit.getActionByName(NotebookEditorKit.selectCellHandleAction),
                key, false);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.SHIFT_MASK
                | InputEvent.CTRL_MASK);
        putAction(kit.getActionByName(NotebookEditorKit.importAction), key,
                false);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
                InputEvent.CTRL_DOWN_MASK);
        putAction(kit.getActionByName(NotebookEditorKit.setCellLanguageAction),
                key, false);
        
        putAction(kit.getActionByName(NotebookEditorKit.openObjectInspectorAction), false);

    }
}
