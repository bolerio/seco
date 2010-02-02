/*
 * This file is part of the Seco source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2010 Kobrix Software, Inc.
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
package seco.notebook.syntax.java;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;

import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.storage.PackageInfo;
import seco.notebook.syntax.completion.AsyncCompletionTask;
import seco.notebook.syntax.completion.Completion;
import seco.notebook.syntax.completion.CompletionItem;
import seco.notebook.syntax.completion.CompletionTask;
import seco.notebook.syntax.util.JMIUtils;
import seco.notebook.util.CharSequenceUtilities;
import seco.notebook.util.DocumentUtilities;
import seco.notebook.util.RequestProcessor;
import bsh.BshCompletionProvider;

/**
 * 
 * @author Dusan Balek
 */
public abstract class JavaResultItem implements CompletionItem
{
    protected int selectionStartOffset = -1;
    protected int selectionEndOffset = -1;
    protected int substituteOffset = -1;

    /** Says what text would this Element use if substituteText is called.
     * @return the substitution text, usable e.g. for finding common text/its' length
     */
    public abstract String getItemText();

    public abstract Object getAssociatedObject();

    protected static Color getTypeColor(Object typ)
    {
        return JavaPaintComponent.TYPE_COLOR;
    }

    public void setSubstituteOffset(int substituteOffset)
    {
        this.substituteOffset = substituteOffset;
    }

    /** Update the text in response to pressing ENTER on this element.
     * @param c the text component to operate on, enables implementation to
     *        do things like movement of caret.
     * @param offset the offset where the item should be placed
     * @param len the length of recognized text which should be replaced
     * @param shift the flag that instructs completion to behave somehow
     *        differently - enables more kinds of invocation of substituteText
     * @return whether the text was successfully updated
     */
    public boolean substituteCommonText(JTextComponent c, int offset, int len,
            int subLen)
    {
        // [PENDING] not enough info in parameters...
        // commonText
        // substituteExp
        return false;
    }

    /** Update the text in response to pressing TAB key (or any key mapped to
     * this function) on this element
     * @param c the text component to operate on, enables implementation to
     *        do things like movement of caret.
     * @param offset the offset where the item should be placed
     * @param len the length of recognized text which should be replaced
     * @param subLen the length of common part - the length of text that should
     *        be inserted after removal of recognized text
     * @return whether the text was successfully updated
     */
    public boolean substituteText(JTextComponent c, int offset, int len,
            boolean shift)
    {
        NotebookDocument doc = (NotebookDocument) c.getDocument();
        String text = getItemText();
        if (text != null)
        {
            if (toAdd != null && !toAdd.equals("\n")) // NOI18N
                text += toAdd;
            // Update the text
            doc.atomicLock();
            try
            {
                CharSequence textToReplace = DocumentUtilities.getText(doc,
                        offset, len);
                if (CharSequenceUtilities.textEquals(text, textToReplace))
                    return false;
                EditorKit kit = c.getUI().getEditorKit(c);
                if (len > 0) doc.remove(offset, len);
                kit.read(new StringReader(text), doc, offset);
                // System.out.println("JavaResultItem - substituteText: " + text
                // + ":" + kit);
                if (selectionStartOffset >= 0)
                {
                    c.select(offset + selectionStartOffset, offset
                            + selectionEndOffset);
                }
            }
            catch (Exception e)
            {
                // Can't update
            }
            finally
            {
                doc.atomicUnlock();
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    public Component getPaintComponent(javax.swing.JList list,
            boolean isSelected, boolean cellHasFocus)
    {
        Component ret = getPaintComponent(isSelected);
        if (ret == null) return null;
        if (isSelected)
        {
            ret.setBackground(list.getSelectionBackground());
            ret.setForeground(list.getSelectionForeground());
        }
        else
        {
            ret.setBackground(list.getBackground());
            ret.setForeground(list.getForeground());
        }
        ret.getAccessibleContext().setAccessibleName(getItemText());
        ret.getAccessibleContext().setAccessibleDescription(getItemText());
        return ret;
    }

    /** Prepare proper component for painting value of <CODE>this</CODE>.
     * @param JList the list this item will be drawn into, usefull e.g. for 
     *        obtaining preferred colors.
     * @param isSelected tells if this item is just selected, for using
     *        proper color scheme.
     * @param cellHasFocus tells it this item is just focused.
     * @return the component usable for painting this value
     */
    public abstract Component getPaintComponent(boolean isSelected);

    public int getPreferredWidth(Graphics g, Font defaultFont)
    {
        Component renderComponent = getPaintComponent(false);
        return renderComponent.getPreferredSize().width;
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected)
    {
        Component renderComponent = getPaintComponent(selected);
        renderComponent.setFont(defaultFont);
        renderComponent.setForeground(defaultColor);
        renderComponent.setBackground(backgroundColor);
        renderComponent.setBounds(0, 0, width, height);
        ((JavaPaintComponent) renderComponent).paintComponent(g);
    }

    public String toString()
    {
        return getItemText();
    }

    // CompletionItem implementation
    public static final String COMPLETION_SUBSTITUTE_TEXT = "completion-substitute-text"; // NOI18N
    static String toAdd;

    public void processKeyEvent(KeyEvent evt)
    {
        if (evt.getID() == KeyEvent.KEY_TYPED)
        {
            Completion completion = Completion.get();
            switch (evt.getKeyChar())
            {
            case ' ':
                if (evt.getModifiers() == 0)
                {
                    completion.hideCompletion();
                    completion.hideDocumentation();
                }
                break;
            case ';':
            case ',':
                completion.hideCompletion();
                completion.hideDocumentation();
            case '.':
                if (defaultAction((JTextComponent) evt.getSource(), Character
                        .toString(evt.getKeyChar())))
                {
                    evt.consume();
                    break;
                }
            }
        }
    }

    public CharSequence getSortText()
    {
        return getItemText();
    }

    public CharSequence getInsertPrefix()
    {
        return getItemText();
    }

    public CompletionTask createDocumentationTask()
    {
        return new AsyncCompletionTask(
                new BshCompletionProvider.DocQuery(this), NotebookUI
                        .getFocusedNotebookUI());

    }

    public CompletionTask createToolTipTask()
    {
        return null;
    }

    public boolean instantSubstitution(JTextComponent c)
    {
        Completion completion = Completion.get();
        completion.hideCompletion();
        completion.hideDocumentation();
        defaultAction(c);
        return true;
    }

    public void defaultAction(JTextComponent component)
    {
        Completion completion = Completion.get();
        completion.hideCompletion();
        completion.hideDocumentation();
        defaultAction(component, "");
    }

    boolean defaultAction(JTextComponent component, String addText)
    {
        int substOffset = substituteOffset;
        if (substOffset == -1) substOffset = component.getCaret().getDot();
        JavaResultItem.toAdd = addText;
        return substituteText(component, substOffset, component.getCaret()
                .getDot()
                - substOffset, false);
    }

    public static class VarResultItem extends JavaResultItem
    {
        private Class<?> type;
        private String typeName;
        private Color typeColor;
        private String varName;
        private int modifiers;
        private static JavaPaintComponent.FieldPaintComponent fieldComponent = null;

        public VarResultItem(String varName, Class<?> type, int modifiers)
        {
            this.type = type;
            this.varName = varName;
            this.modifiers = modifiers | JavaCompletion.LOCAL_MEMBER_BIT;
            this.typeName = JMIUtils.getTypeName(type, false, false);
            this.typeColor = getTypeColor(type);
        }

        public String getItemText()
        {
            return varName;
        }

        public Component getPaintComponent(boolean isSelected)
        {
            if (fieldComponent == null)
            {
                fieldComponent = new JavaPaintComponent.FieldPaintComponent(
                        true);
            }
            fieldComponent.setTypeName(typeName);
            fieldComponent.setTypeColor(typeColor);
            fieldComponent.setFieldName(varName);
            fieldComponent.setModifiers(modifiers);
            fieldComponent.setSelected(isSelected);
            return fieldComponent;
        }

        public Object getAssociatedObject()
        {
            return this;
        }

        public Type getType()
        {
            return type;
        }

        public int getSortPriority()
        {
            return 200;
        }

        public String toString()
        {
            String mods = Modifier.toString(modifiers) + " "; // NOI18N
            return (mods.length() > 1 ? mods : "") + typeName + " " + varName; // NOI18N
        }
    }

    public static class FieldResultItem extends JavaResultItem
    {
        private Field fld;
        private String typeName;
        private Color typeColor;
        private String fldName;
        private int modifiers;
        private boolean isDeprecated;
        private Class<?> context;
        private static JavaPaintComponent.FieldPaintComponent fieldComponent = null;

        public FieldResultItem(String name, Type type, int modifiers) 
        {
            this.fldName = name;
            this.modifiers = modifiers;
            this.typeName = JMIUtils.getTypeName(type, false, false);
            this.typeColor = getTypeColor(type);
        }
        
        public FieldResultItem(String name, String type, int modifiers) 
        {
            this.fldName = name;
            this.modifiers = modifiers;
            this.typeName = type;
            this.typeColor = getTypeColor(type);
        }

        public FieldResultItem(Field fld, Class<?> context)
        {
            this.fld = fld;
            this.fldName = fld.getName();
            this.modifiers = fld.getModifiers();
            if (fld.getDeclaringClass() == context)
            {
                this.modifiers |= JavaCompletion.LOCAL_MEMBER_BIT;
            }
            Type type = fld.getType();
            this.typeName = JMIUtils.getTypeName(type, false, false);
            this.typeColor = getTypeColor(type);
            this.isDeprecated = fld.getAnnotation(Deprecated.class) != null;
            this.context = context;
        }

        public String getItemText()
        {
            return fldName;
        }

        public String getTypeName()
        {
            return typeName;
        }

        public int getModifiers()
        {
            return modifiers;
        }

        public String getFieldName()
        {
            return fldName;
        }

        public Component getPaintComponent(boolean isSelected)
        {
            if (fieldComponent == null)
            {
                fieldComponent = new JavaPaintComponent.FieldPaintComponent(
                        false);
            }
            fieldComponent.setTypeName(typeName);
            fieldComponent.setFieldName(fldName);
            fieldComponent.setTypeColor(typeColor);
            fieldComponent.setModifiers(modifiers);
            fieldComponent.setSelected(isSelected);
            fieldComponent.setDeprecated(isDeprecated);
            return fieldComponent;
        }

        public boolean substituteText(final JTextComponent c, final int offset,
                final int len, final boolean shift)
        {
            if (context == null)
                return super.substituteText(c, offset, len, shift);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run()
                {
                    final NotebookDocument doc = (NotebookDocument) c
                            .getDocument();
                    final StringBuffer sb = new StringBuffer();
                    sb.append(fldName);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {
                            String text = sb.toString();
                            if (text != null && text.length() > 0)
                            {
                                int offset = c.getCaret().getDot() - len;
                                doc.atomicLock();
                                try
                                {
                                    EditorKit kit = c.getUI().getEditorKit(c);
                                    if (len > 0) doc.remove(offset, len);
                                    kit.read(new StringReader(text), doc,
                                            offset);
                                }
                                catch (Exception e)
                                {
                                    // Can't update
                                }
                                finally
                                {
                                    doc.atomicUnlock();
                                }
                            }
                        }
                    });
                }
            });
            return true;
        }

        public Object getAssociatedObject()
        {
            return fld;
        }

        public int getSortPriority()
        {
            return 300;
        }

        public String toString()
        {
            String mods = Modifier.toString(modifiers) + " "; // NOI18N
            return (mods.length() > 1 ? mods : "") + typeName + " " + fldName; // NOI18N
        }
        
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((fldName == null) ? 0 : fldName.hashCode());
            result = prime * result + modifiers;
            result = prime * result
                    + ((typeName == null) ? 0 : typeName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            FieldResultItem other = (FieldResultItem) obj;
            if (fldName == null)
            {
                if (other.fldName != null) return false;
            }
            else if (!fldName.equals(other.fldName)) return false;
            if (modifiers != other.modifiers) return false;
            if (typeName == null)
            {
                if (other.typeName != null) return false;
            }
            else if (!typeName.equals(other.typeName)) return false;
            return true;
        }
    }

    public static class MethodItem extends CallableFeatureResultItem
    {
        private static JavaPaintComponent.MethodPaintComponent mtdComponent = null;

        public MethodItem(Method mtd)
        {
            super(mtd);
            modifiers = mtd.getModifiers(); // Modifier.PUBLIC;
            cfName = mtd.getName();// : (String) mtd;
            Object obj = mtd.getReturnType(); // : "void";
            typeName = JMIUtils.getTypeName(obj, false, false);
            typeColor = getTypeColor(cf);
            isDeprecated = mtd.getAnnotation(Deprecated.class) != null;
            populateParamsAndExceptions(mtd.getParameterTypes(), mtd
                    .getExceptionTypes());
        }

        public MethodItem(String mtdName, Class<?> type,
                Class<?>[] params, Class<?>[] exc)
        {
            super(mtdName);
            modifiers = Modifier.PUBLIC;
            cfName = mtdName;
            typeName = JMIUtils.getTypeName(type, false, false);
            typeColor = getTypeColor(mtdName);
            populateParamsAndExceptions(params, exc);
        }

        public MethodItem(String mtdName, String type)
        {
            super(mtdName);
            modifiers = Modifier.PUBLIC;
            cfName = mtdName;
            typeName = type;
            typeColor = getTypeColor(mtdName);
        }
        
        public MethodItem(String mtdName, String type, int modifiers)
        {
            this(mtdName, type);
            this.modifiers = modifiers;
        }

        public MethodItem(String mtdName, String type, String[] types,
                String[] names, int modifiers)
        {
            this(mtdName, type);
            this.modifiers = modifiers;
            populateParams(types, names);
        }

        public MethodItem(String mtdName, String type, String[] types,
                String[] names)
        {
            this(mtdName, type, types, names, Modifier.PUBLIC);
        }

        void populateParams(String[] prms, String names[])
        {
            for (int i = 0; i < prms.length; i++)
                params.add(new ParamStr(prms[i], prms[i], names[i], false,
                        getTypeColor(prms[i])));
        }

        public Component getPaintComponent(boolean isSelected)
        {
            if (mtdComponent == null)
            {
                mtdComponent = new JavaPaintComponent.MethodPaintComponent();
            }
            mtdComponent.setFeatureName(getName());
            mtdComponent.setModifiers(getModifiers());
            mtdComponent.setTypeName(getTypeName());
            mtdComponent.setTypeColor(getTypeColor());
            mtdComponent.setParams(getParams());
            mtdComponent.setExceptions(getExceptions());
            mtdComponent.setDeprecated(isDeprecated());
            mtdComponent.setSelected(isSelected);
            mtdComponent.setActiveParameterIndex(getActiveParameterIndex());
            return mtdComponent;
        }

        public int getSortPriority()
        {
            return isEnclosingCall() ? 10 : 500;
        }

        public String toString()
        {
            String mods = Modifier.toString(getModifiers()) + " "; // NOI18N
            return (mods.length() > 1 ? mods : "") + getTypeName() + " "
                    + getName() + printParams(true) + printExceptions(); // NOI18N
        }
        
       
    }

    public static class ConstructorResultItem extends CallableFeatureResultItem
    {
        private static JavaPaintComponent.ConstructorPaintComponent ctrComponent = null;

        public ConstructorResultItem(Constructor<?> con)
        {
            super(con);
            this.modifiers = con.getModifiers();
            cfName = con.getName();
            typeName = "void";
            typeColor = getTypeColor(cf);
            isDeprecated = con.getAnnotation(Deprecated.class) != null;
            populateParamsAndExceptions(con.getParameterTypes(), con
                    .getExceptionTypes());
        }

        public String getName()
        {
            return getTypeName();
        }

        public Component getPaintComponent(boolean isSelected)
        {
            if (ctrComponent == null)
            {
                ctrComponent = new JavaPaintComponent.ConstructorPaintComponent();
            }
            ctrComponent.setFeatureName(getName());
            ctrComponent.setModifiers(getModifiers());
            ctrComponent.setParams(getParams());
            ctrComponent.setExceptions(getExceptions());
            ctrComponent.setDeprecated(isDeprecated());
            ctrComponent.setSelected(isSelected);
            ctrComponent.setActiveParameterIndex(getActiveParameterIndex());
            return ctrComponent;
        }

        public int getSortPriority()
        {
            return isEnclosingCall() ? 5 : 400;
        }

        public String toString()
        {
            String mods = Modifier.toString(getModifiers()) + " "; // NOI18N
            return (mods.length() > 1 ? mods : "") + getName()
                    + printParams(true) + printExceptions();
        }
    }

    public abstract static class CallableFeatureResultItem extends
            JavaResultItem
    {
        protected Object cf;
        protected List<ParamStr> params = new ArrayList<ParamStr>();
        protected List<ExceptionStr> excs = new ArrayList<ExceptionStr>();
        protected int modifiers;
        protected String cfName, typeName;
        protected Color typeColor;
        protected boolean isDeprecated;
        protected int activeParameterIndex = -1;

        public CallableFeatureResultItem(Object cf)
        {
            this.cf = cf;
        }

        public void processKeyEvent(KeyEvent evt)
        {
            super.processKeyEvent(evt);
            if (!evt.isConsumed() && evt.getID() == KeyEvent.KEY_TYPED)
            {
                Completion completion = Completion.get();
                switch (evt.getKeyChar())
                {
                case '(':
                    completion.hideCompletion();
                    completion.hideDocumentation();
                    if (defaultAction((JTextComponent) evt.getSource(),
                            Character.toString(evt.getKeyChar())))
                    {
                        evt.consume();
                        break;
                    }
                }
            }
        }

        public String getItemText()
        {
            return getName();
        }

        public String getTypeName()
        {
            return typeName;
        }

        public Color getTypeColor()
        {
            return typeColor;
        }

        public int getModifiers()
        {
            return modifiers;
        }

        public boolean isDeprecated()
        {
            return isDeprecated;
        }

        public String getName()
        {
            return cfName;
        }

        public List<ParamStr> getParams()
        {
            return params;
        }

        public List<ExceptionStr> getExceptions()
        {
            return excs;
        }

        public CharSequence getSortText()
        {
            return getName() + "#" + getParamsCountString() + "#"
                    + printParams(false); // NOI18N
        }

        protected void populateParamsAndExceptions(Class<?>[] prms,
                Class<?>[] exceps)
        {
            if (prms != null)
                for (int i = 0; i < prms.length; i++)
                    if (params != null)
                    {
                        String arg_name = (prms.length == 1) ? "arg" : "arg" + (i + 1);
                        params.add(new ParamStr(prms[i].getSimpleName(),
                                JMIUtils.getTypeName(prms[i], false, false),
                                arg_name, false, getTypeColor(prms[i])));
                    }
            if (exceps != null)
                for (int i = 0; i < exceps.length; i++)
                    excs.add(new ExceptionStr(exceps[i].getSimpleName(),
                            getTypeColor(exceps[i])));
        }

        private String getParamsCountString()
        {
            int size = params.size();
            return (size < 10 ? "0" : "") + size; // NOI18N
        }

        public Object getAssociatedObject()
        {
            return cf;
        }

        public int getActiveParameterIndex()
        {
            return activeParameterIndex;
        }

        /**
         * If set to value different than -1 it marks that this component
         * renders an outer enclosing constructor/method and the given index is
         * the index of the active parameter which is being completed as an
         * inner expression.
         */
        public void setActiveParameterIndex(int activeParamIndex)
        {
            this.activeParameterIndex = activeParamIndex;
        }

        /**
         * Check whether this paint component renders an outer
         * method/constructor which should be rendered in grey with black active
         * parameter.
         * 
         * @return true if this paint component renders outer method/constructor
         *         or false otherwise.
         */
        boolean isEnclosingCall()
        {
            return (activeParameterIndex != -1);
        }

        boolean addParams = true;

        public void addParams(boolean addParams)
        {
            this.addParams = addParams;
        }

        protected boolean isAddParams()
        {
            return addParams;
        }

        public boolean substituteText(JTextComponent c, int offset, int len,
                boolean shift)
        {
            NotebookDocument doc = (NotebookDocument) c.getDocument();
            String text = null;
            boolean addSpace = false;
            text = cfName;

            if (isAddParams())
            {
                if (addSpace) text += ' '; // NOI18N
                text += '('; // NOI18N
                if (params.size() > 0)
                {
                    selectionStartOffset = selectionEndOffset = text.length();
                    Completion completion = Completion.get();
                    completion.hideCompletion();
                    completion.hideDocumentation();
                    completion.showToolTip();
                }
                text += ")"; // NOI18N
            }
            if (text != null)
            {
                if (toAdd != null && !toAdd.equals("\n") && !"(".equals(toAdd)) // NOI18N
                    text += toAdd;
                // Update the text
                doc.atomicLock();
                try
                {
                    CharSequence textToReplace = DocumentUtilities.getText(doc,
                            offset, len);
                    if (CharSequenceUtilities.textEquals(text, textToReplace))
                    {
                        c.setCaretPosition(offset + len);
                        return false;
                    }
                    EditorKit kit = c.getUI().getEditorKit(c);
                    if (len > 0) doc.remove(offset, len);
                    kit.read(new StringReader(text), doc, offset);
                    if (selectionStartOffset >= 0)
                    {
                        c.select(offset + selectionStartOffset, offset
                                + selectionEndOffset);
                    }
                    else if ("(".equals(toAdd))
                    { // NOI18N
                        int index = text.lastIndexOf(')');
                        if (index > -1)
                        {
                            c.setCaretPosition(offset + index);
                        }
                    }
                }
                catch (Exception e)
                {
                    // Can't update
                }
                finally
                {
                    doc.atomicUnlock();
                }
                return true;
            }
            else
            {
                return false;
            }
        }

        protected List<String> createParamsList()
        {
            List<String> ret = new ArrayList<String>();
            for (Iterator<ParamStr> it = params.iterator(); it.hasNext();)
            {
                StringBuffer sb = new StringBuffer();
                ParamStr ps = it.next();
                sb.append(ps.getSimpleTypeName());
                if (ps.isVarArg())
                {
                    sb.append("..."); // NOI18N
                }
                String name = ps.getName();
                if (name != null && name.length() > 0)
                {
                    sb.append(" "); // NOI18N
                    sb.append(name);
                }
                if (it.hasNext())
                {
                    sb.append(", "); // NOI18N
                }
                ret.add(sb.toString());
            }
            return ret;
        }

        public int getCurrentParamIndex()
        {
            int idx = 0;
            // if (substituteExp != null && substituteExp.getExpID() ==
            // JCExpression.METHOD_OPEN)
            // idx = substituteExp.getParameterCount() - 1;
            // if (varArgIndex > -1 && varArgIndex < idx)
            // idx = varArgIndex;
            return idx;
        }

        protected String printParams(boolean includeParamNames)
        {
            StringBuffer sb = new StringBuffer();
            sb.append("("); // NOI18N
            for (Iterator<ParamStr> it = params.iterator(); it.hasNext();)
            {
                ParamStr ps = it.next();
                sb.append(ps.getSimpleTypeName());
                if (ps.isVarArg())
                {
                    sb.append("..."); // NOI18N
                }
                if (includeParamNames)
                {
                    String name = ps.getName();
                    if (name != null && name.length() > 0)
                    {
                        sb.append(" "); // NOI18N
                        sb.append(name);
                    }
                }
                if (it.hasNext())
                {
                    sb.append(", "); // NOI18N
                }
            }
            sb.append(")"); // NOI18N
            return sb.toString();
        }

        protected String printExceptions()
        {
            StringBuffer sb = new StringBuffer();
            if (excs.size() > 0)
            {
                sb.append(" throws "); // NOI18N
                for (Iterator<ExceptionStr> it = excs.iterator(); it.hasNext();)
                {
                    ExceptionStr ex = (ExceptionStr) it.next();
                    sb.append(ex.getName());
                    if (it.hasNext())
                    {
                        sb.append(", "); // NOI18N
                    }
                }
            }
            return sb.toString();
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((cfName == null) ? 0 : cfName.hashCode());
            result = prime * result + modifiers;
            result = prime * result
                    + ((params == null) ? 0 : params.hashCode());
            result = prime * result
                    + ((typeName == null) ? 0 : typeName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            CallableFeatureResultItem other = (CallableFeatureResultItem) obj;
            if (cfName == null)
            {
                if (other.cfName != null) return false;
            }
            else if (!cfName.equals(other.cfName)) return false;
            if (params == null)
            {
                if (other.params != null) return false;
            }
            else if (params.size() != other.params.size()) return false;
            if (typeName == null)
            {
                if (other.typeName != null) return false;
            }
            else if (!typeName.equals(other.typeName)) return false;
            return true;
        }
        
        
    }

    public static class PackageResultItem extends JavaResultItem
    {
        private boolean displayFullPackagePath;
        private PackageInfo pkg;
        private String pkgName;
        private static JavaPaintComponent.PackagePaintComponent pkgComponent = null;

        public PackageResultItem(PackageInfo pkg, boolean displayFullPackagePath)
        {
            this.pkg = pkg;
            this.displayFullPackagePath = displayFullPackagePath;
            this.pkgName = pkg.getName();
        }

        public String getItemText()
        {
            return displayFullPackagePath ? pkgName : pkgName.substring(pkgName
                    .lastIndexOf('.') + 1);
        }

        public Component getPaintComponent(boolean isSelected)
        {
            if (pkgComponent == null)
            {
                pkgComponent = new JavaPaintComponent.PackagePaintComponent();
            }
            pkgComponent.setSelected(isSelected);
            pkgComponent.setPackageName(pkgName);
            pkgComponent.setDisplayFullPackagePath(displayFullPackagePath);
            return pkgComponent;
        }

        public int getSortPriority()
        {
            return 100;
        }

        public Object getAssociatedObject()
        {
            return pkg;
        }

        public boolean substituteText(final JTextComponent c, final int offset,
                final int len, final boolean shift)
        {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run()
                {
                    final NotebookDocument doc = (NotebookDocument) c
                            .getDocument();
                    final StringBuffer sb = new StringBuffer();
                    sb.append(pkgName);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {
                            String text = sb.toString();
                            if (text != null && text.length() > 0)
                            {
                                int offset = c.getCaret().getDot() - len;
                                doc.atomicLock();
                                try
                                {
                                    EditorKit kit = c.getUI().getEditorKit(c);
                                    if (len > 0) doc.remove(offset, len);
                                    kit.read(new StringReader(text), doc,
                                            offset);
                                }
                                catch (Exception e)
                                {
                                    // Can't update
                                }
                                finally
                                {
                                    doc.atomicUnlock();
                                }
                            }
                        }
                    });
                }
            });
            return true;
        }
    }

    public static class ClassResultItem extends JavaResultItem
    {
        private Class<?> cls;
        private boolean isInterface;
        private boolean isDeprecated;
        boolean addImport;
        boolean generateClassSkeleton;
        private String fqName = null;
        private String name = null;
        private static JavaPaintComponent.InterfacePaintComponent interfaceComponent = null;
        private static JavaPaintComponent.ClassPaintComponent classComponent = null;
        private static final boolean autoImportDisabled = Boolean
                .getBoolean("org.netbeans.java.editor.disableAutoImport"); // NOI18N
        private static final boolean autoGenerationDisabled = Boolean
                .getBoolean("org.netbeans.java.editor.disableAutoClassSkeletonGeneration"); // NOI18N
        private boolean displayFQN;

        public ClassResultItem(Class<?> cls, boolean displayFQN,
                boolean addImport, boolean generateClassSkeleton)
        {
            this.cls = cls;
            this.addImport = addImport && !autoImportDisabled;
            this.name = cls.getSimpleName();
            this.displayFQN = displayFQN;
            if (displayFQN || this.addImport)
            {
                this.fqName = cls.getName();
                int idx = this.fqName.indexOf('<'); // NOI18N
                if (idx >= 0) this.fqName = this.fqName.substring(0, idx);
                idx = this.fqName.lastIndexOf('.'); // NOI18N
                this.fqName = idx >= 0 ? " (" + this.fqName.substring(0, idx)
                        + ")" : ""; // NOI18N
            }
            else
            {
                this.fqName = ""; // NOI18N
            }
            this.isInterface = cls.isInterface();
            this.isDeprecated = cls.getAnnotation(Deprecated.class) != null;
            this.addImport = addImport && !autoImportDisabled;
            this.generateClassSkeleton = generateClassSkeleton
                    && !autoGenerationDisabled;
        }

        public boolean substituteText(final JTextComponent c, final int offset,
                final int len, final boolean shift)
        {
            final NotebookDocument doc = (NotebookDocument) c.getDocument();
            String text = // ???generateClassSkeleton && cls instanceof
            // ParameterizedType ? null :
            getItemText();
            //int toAddDelta = 0;
            final boolean makeConstructor = "(".equals(toAdd); // NOI18N
            boolean ret = true;
            if (text != null)
            {
                if (toAdd != null && !toAdd.equals("\n"))
                { // NOI18N
                    text += toAdd;
                    if (makeConstructor)
                    {
                        text += ")"; // NOI18N
                    }
                   //toAddDelta = toAdd.length();
                }
                // Update the text
                doc.atomicLock();
                try
                {
                    CharSequence textToReplace = DocumentUtilities.getText(doc,
                            offset, len);
                    if (CharSequenceUtilities.textEquals(text, textToReplace))
                    {
                        ret = false;
                    }
                    else
                    {
                        EditorKit kit = c.getUI().getEditorKit(c);
                        if (len > 0) doc.remove(offset, len);
                        kit.read(new StringReader(text), doc, offset);
                        if (makeConstructor)
                            c.setCaretPosition(c.getCaretPosition() - 1);
                    }
                }
                catch (Exception e)
                {
                    // Can't update
                }
                finally
                {
                    doc.atomicUnlock();
                }
            }
           // final int toAddDeltaResult = toAddDelta;
            RequestProcessor.getDefault().post(new Runnable() {
                public void run()
                {
                    final StringBuffer sb = new StringBuffer();
                    // JMIUtils jmiUtils = JMIUtils.get(doc);
                    // jmiUtils.beginTrans(true);
                    try
                    {
                        // if (cls.isValid()) {
                        //Map cache = new HashMap();
                        // NbJavaJMISyntaxSupport ssup =
                        // (NbJavaJMISyntaxSupport)doc.getSyntaxSupport().get(NbJavaJMISyntaxSupport.class);
                        // JavaClass ctx =
                        // ssup.getJavaClass(c.getCaretPosition());
                        /*
                         * if (addImport && ctx != null && !cls.isInner()) {
                         * Class jc = cls instanceof ParameterizedType ?
                         * ((ParameterizedType)cls).getDefinition() : cls;
                         * MultipartId mpid =
                         * JavaModelUtil.resolveImportsForClass(ctx, jc); if
                         * (!jc.getSimpleName().equals(mpid.getName())) {
                         * doc.atomicLock(); try { int pos =
                         * c.getCaretPosition() - toAddDeltaResult -
                         * name.length(); doc.remove(pos, name.length());
                         * doc.insertString(pos, mpid.getName(), null); } catch
                         * (BadLocationException ble) { } finally {
                         * doc.atomicUnlock(); } cache.put(jc, Boolean.TRUE); }
                         * else { cache.put(jc, Boolean.FALSE); } }
                         */
                        if (!makeConstructor
                                && checkClassSkeletonAutoGeneration())
                        {
                            sb.append("() {\n"); // NOI18N
                            // ???List methods = jmiUtils.findMethods(cls, "",
                            // false, false, null, false, false, null, false,
                            // true); //NOI18N
                            // for (Iterator it = methods.iterator();
                            // it.hasNext();) {
                            // Method mtd = (Method)it.next();
                            Method[] mtds = cls.getMethods();
                            for (int j = 0; j < mtds.length; j++)
                            {
                                Method mtd = mtds[j];
                                int mods = mtd.getModifiers();
                                if (Modifier.isAbstract(mods))
                                {
                                    sb
                                            .append(Modifier
                                                    .toString(mods
                                                            & ~(Modifier.NATIVE
                                                                    | Modifier.ABSTRACT | Modifier.SYNCHRONIZED)));
                                    sb.append(' '); // NOI18N
                                    // Type typ = mtd.getType();
                                    sb.append(mtd.getReturnType().getName());// JMIUtils.getTypeName(typ,
                                    // typ
                                    // instanceof
                                    // Class
                                    // &&
                                    // useFQN((Class)typ,
                                    // ctx,
                                    // cache),
                                    // true));
                                    sb.append(' '); // NOI18N
                                    sb.append(mtd.getName());
                                    sb.append('('); // NOI18N
                                    Class<?>[] params = mtd.getParameterTypes();
                                    for (int i = 0; i < params.length; i++)
                                    {
                                        // Parameter prm = (Parameter)
                                        // itt.next();
                                        // typ = prm.getType();
                                        sb.append(params[i].getName());// JMIUtils.getTypeName(typ,
                                        // typ
                                        // instanceof
                                        // Class
                                        // &&
                                        // useFQN((Class)typ,
                                        // ctx,
                                        // cache),
                                        // true));
                                        sb.append(' '); // NOI18N
                                        sb.append("arg" + i);// prm.getName());
                                        // if (prm.isVarArg())
                                        // sb.append("..."); //NOI18N
                                        if (i < params.length - 1)
                                            sb.append(", "); // NOI18N
                                    }
                                    sb.append(')'); // NOI18N
                                    Class<?>[] exs = mtd.getExceptionTypes();
                                    if (exs.length > 0) sb.append(" throws "); // NOI18N
                                    for (int i = 0; i < exs.length; i++)
                                    {
                                        sb.append(exs[i].getName()); // JMIUtils.getTypeName(ex,
                                        // useFQN(ex,
                                        // ctx,
                                        // cache),
                                        // true));
                                        if (i < exs.length - 1) sb.append(','); // NOI18N
                                    }
                                    sb.append(" {\n}\n"); // NOI18N
                                }
                            }
                            sb.append('}'); // NOI18N
                        }
                        // }
                    }
                    finally
                    {
                        // jmiUtils.endTrans(false);
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {
                            String skeleton = sb.toString();
                            if (skeleton != null && skeleton.length() > 0)
                            {
                                doc.atomicLock();
                                try
                                {
                                    int startOffset = c.getCaret().getDot();
                                    doc.insertString(startOffset, skeleton,
                                            null);
                                    //int endOffset = c.getCaret().getDot();
                                    // ???doc.getFormatter().reformat(doc,
                                    // startOffset, endOffset);
                                }
                                catch (BadLocationException e)
                                {
                                    // Can't update
                                }
                                finally
                                {
                                    doc.atomicUnlock();
                                }
                            }
                        }
                    });
                }
            });
            return ret;
        }

        public boolean substituteTextSimple(final JTextComponent c, int offset,
                int len, boolean shift)
        {
            return super.substituteText(c, offset, len, shift);
        }

        public void processKeyEvent(KeyEvent evt)
        {
            if (evt.getID() == KeyEvent.KEY_PRESSED
                    && evt.getKeyCode() == KeyEvent.VK_ENTER
                    && evt.getModifiers() == InputEvent.CTRL_MASK)
            {
                substituteTextSimple((JTextComponent) evt.getSource());
                evt.consume();
            }
            if (!evt.isConsumed())
            {
                super.processKeyEvent(evt);
            }
        }

        private void substituteTextSimple(JTextComponent component)
        {
            int substOffset = substituteOffset;
            if (substOffset == -1) substOffset = component.getCaretPosition();
            substituteTextSimple(component, substOffset, component
                    .getCaretPosition()
                    - substOffset, false);
            Completion.get().hideCompletion();
        }

        public boolean checkAutoImport(final JTextComponent c)
        {
            // ??? if (addImport)
            // return new
            // NbJavaJMIFastImport(c).checkAutoImport(ClassResultItem.this);
            return false;
        }

        private boolean checkClassSkeletonAutoGeneration()
        {
            return generateClassSkeleton
                    && (Modifier.isAbstract(cls.getModifiers()) || Modifier
                            .isInterface(cls.getModifiers()));
        }

        public String getItemText()
        {
            return name;
        }

        public boolean instantSubstitution(JTextComponent c)
        {
            boolean ret = !(checkAutoImport(c) || checkClassSkeletonAutoGeneration());
            if (ret) super.instantSubstitution(c);
            return ret;
        }

        public CharSequence getSortText()
        {
            return name + fqName;
        }

        public Component getPaintComponent(boolean isSelected)
        {
            if (isInterface)
            {
                if (interfaceComponent == null)
                {
                    interfaceComponent = new JavaPaintComponent.InterfacePaintComponent();
                }
                interfaceComponent.setSelected(isSelected);
                interfaceComponent.setDeprecated(isDeprecated);
                interfaceComponent.setSimpleClassName(name);
                interfaceComponent.setFQName(fqName);
                if (displayFQN)
                {
                    interfaceComponent.setCls(cls);
                }
                return interfaceComponent;
            }
            else
            {
                if (classComponent == null)
                {
                    classComponent = new JavaPaintComponent.ClassPaintComponent();
                }
                classComponent.setSelected(isSelected);
                classComponent.setDeprecated(isDeprecated);
                classComponent.setSimpleClassName(name);
                classComponent.setFQName(fqName);
                if (displayFQN)
                {
                    classComponent.setCls(cls);
                }
                return classComponent;
            }
        }

        public Object getAssociatedObject()
        {
            return cls;
        }

        public int getSortPriority()
        {
            return 200;
        }
    }

    public static class StringResultItem extends JavaResultItem
    {
        private String str;
        private static JavaPaintComponent.StringPaintComponent stringComponent = null;

        public StringResultItem(String str)
        {
            this.str = str;
        }

        public String getItemText()
        {
            return str;
        }

        public Component getPaintComponent(boolean isSelected)
        {
            if (stringComponent == null)
            {
                stringComponent = new JavaPaintComponent.StringPaintComponent();
            }
            stringComponent.setSelected(isSelected);
            stringComponent.setString(str);
            return stringComponent;
        }

        public int getSortPriority()
        {
            return 50;
        }

        public Object getAssociatedObject()
        {
            return str;
        }
    }

    public static class ParamStr
    {
        private String type, simpleType, prm;
        private boolean isVarArg;
        private Color typeColor;

        public ParamStr(String type, String simpleType, String prm,
                boolean isVarArg, Color typeColor)
        {
            this.type = type;
            this.simpleType = simpleType;
            this.prm = prm;
            this.isVarArg = isVarArg;
            this.typeColor = typeColor;
        }

        public String getTypeName()
        {
            return type;
        }

        public String getSimpleTypeName()
        {
            return simpleType;
        }

        public String getName()
        {
            return prm;
        }

        public boolean isVarArg()
        {
            return isVarArg;
        }

        public Color getTypeColor()
        {
            return typeColor;
        }
    }

    public static class ExceptionStr
    {
        private String name;
        private Color typeColor;

        public ExceptionStr(String name, Color typeColor)
        {
            this.name = name;
            this.typeColor = typeColor;
        }

        public String getName()
        {
            return name;
        }

        public Color getTypeColor()
        {
            return typeColor;
        }
    }
}
