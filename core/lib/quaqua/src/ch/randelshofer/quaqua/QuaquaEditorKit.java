/*
 * @(#)QuaquaEditorKit.java 
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 *
 * Part of this software (as marked) has been derived from software by
 * Dustin Sacks. These parts are used under license.
 */
package ch.randelshofer.quaqua;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;

/**
 * The QuaquaEditorKit extends the Swing DefaultEditorKit with Mac OS X specific
 * text editing actions.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaEditorKit.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaEditorKit extends DefaultEditorKit {
    // FIXME - Maybe we should rename this class OSXEditorKit and move it into
    // a separate package. Because it is not a LAF class.
    // Maybe we should move all non-LAF classes out of the quaqua package.

    /**
     * Default actions of the QuaquaEditorKit.
     */
    private static Action[] actions;

    // TO DO: Use this instead of the code above:
    //actions = TextAction.augmentList(....)
    /**
     * Default constructor.
     */
    public QuaquaEditorKit() {
    }

    /**
     * Fetches the set of commands that can be used
     * on a text component that is using a model and
     * view produced by this kit.
     *
     * @return the command list
     */
    @Override
    public Action[] getActions() {
        if (actions == null) {
            Action[] dekActions = new DefaultEditorKit().getActions();
            HashMap dekActionMap = new HashMap();
            for (int i = 0; i < dekActions.length; i++) {
                dekActionMap.put(dekActions[i].getValue(Action.NAME), dekActions[i]);
            }

            HashMap actionMap = (HashMap) dekActionMap.clone();
            actionMap.put(deleteNextWordAction, new QuaquaEditorKit.DeleteNextWordAction());
            actionMap.put(deletePrevWordAction, new QuaquaEditorKit.DeletePrevWordAction());
            actionMap.put(upAction, new QuaquaEditorKit.VerticalAction(
                    upAction,
                    (TextAction) dekActionMap.get(upAction),
                    (TextAction) dekActionMap.get(beginAction)));
            actionMap.put(downAction, new QuaquaEditorKit.VerticalAction(
                    downAction,
                    (TextAction) dekActionMap.get(downAction),
                    (TextAction) dekActionMap.get(endAction)));
            actionMap.put(selectionUpAction, new QuaquaEditorKit.VerticalAction(
                    selectionUpAction,
                    (TextAction) dekActionMap.get(selectionUpAction),
                    (TextAction) dekActionMap.get(selectionBeginAction)));
            actionMap.put(selectionDownAction, new QuaquaEditorKit.VerticalAction(
                    selectionDownAction,
                    (TextAction) dekActionMap.get(selectionDownAction),
                    (TextAction) dekActionMap.get(selectionEndAction)));

            actions = (Action[]) actionMap.values().toArray(new Action[0]);
        }
        return actions.clone();
    }

    /*
     * Deletes the word that follows the
     * current caret position.
     *
     * Original code of this class by Dustin Sacks.
     *
     * @see QuaquaEditorKit#deleteNextWordAction
     * @see QuaquaEditorKit#getActions
     */
    static class DeleteNextWordAction extends TextAction {

        /**
         * Creates this object with the appropriate identifier.
         */
        DeleteNextWordAction() {
            super(deleteNextWordAction);
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            boolean beep = true;
            if ((target != null) && (target.isEditable())) {
                try {
                    // select the next word
                    int offs = target.getCaretPosition();
                    int endOffs;
                    String s = target.getDocument().getText(offs, 1);
                    if (Character.isWhitespace(s.charAt(0))) {
                        endOffs = Utilities.getNextWord(target, offs);
                        endOffs = Utilities.getWordEnd(target, endOffs);
                    } else {
                        endOffs = Utilities.getWordEnd(target, offs);
                    }
                    target.moveCaretPosition(endOffs);

                    // and then delete it
                    target.replaceSelection("");
                    beep = false;
                } catch (BadLocationException exc) {
                    // nothing to do, because we set beep to true already
                }
            }
            if (beep) {
                QuaquaUtilities.provideErrorFeedback(target);
            }
        }
    }

    /*
     * Deletes the word that precedes the
     * current caret position.
     *
     * Original code of this class by Dustin Sacks.
     *
     * @see QuaquaEditorKit#deletePrevWordAction
     * @see QuaquaEditorKit#getActions
     */
    static class DeletePrevWordAction extends TextAction {

        /**
         * Creates this object with the appropriate identifier.
         */
        DeletePrevWordAction() {
            super(deletePrevWordAction);
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            boolean beep = true;
            if ((target != null) && (target.isEditable())) {
                int offs = target.getCaretPosition();
                boolean failed = false;
                try {
                    offs = Utilities.getPreviousWord(target, offs);
                } catch (BadLocationException bl) {
                    if (offs != 0) {
                        offs = 0;
                    } else {
                        failed = true;
                    }
                }
                if (!failed) {
                    target.moveCaretPosition(offs);
                    // and then delete it
                    target.replaceSelection("");
                    beep = false;
                }
            }
            if (beep) {
                QuaquaUtilities.provideErrorFeedback(target);
            }
        }
    }

    /*
     * Action to move the selection up or down.
     *
     * This is very similar to the NextVisualPositionAction of class
     * DefaultEditorKit. The differences is, that we move the cursor to the
     * beginning of the text, if the user wants to move upwards and is already
     * at the first line of the text. We move the cursor to the
     * end of the text, if the user wants to move downwards and is already
     * at the last line of the text.
     *
     *
     * Note that we delegate actions to DefaultEditorKit actions. We can not
     * implement all the required code by ourself, because method
     * DefaultCaret.getDotBias() is not accessible from outside the
     * javax.swing.text package.
     */
    static class VerticalAction extends TextAction {

        private TextAction verticalAction;
        private TextAction beginEndAction;

        /**
         * Create this action with the appropriate identifier.
         */
        VerticalAction(String name, TextAction verticalAction, TextAction beginEndAction) {
            super(name);
            this.verticalAction = verticalAction;
            this.beginEndAction = beginEndAction;
        }

        /** The operation to perform when this action is triggered. */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                // target.getUI().getNextVisualPositionFrom(t
                Caret caret = target.getCaret();
                int dot = caret.getDot();
                verticalAction.actionPerformed(e);
                if (dot == caret.getDot()) {
                    Point magic = caret.getMagicCaretPosition();
                    beginEndAction.actionPerformed(e);
                    caret.setMagicCaretPosition(magic);
                }
            }
        }
    }
}
