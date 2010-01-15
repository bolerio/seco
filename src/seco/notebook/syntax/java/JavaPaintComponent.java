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
package seco.notebook.syntax.java;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.lang.reflect.Modifier;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.UIManager;

import seco.notebook.util.IconManager;


/**
 *
 * @author  Dusan Balek
 */
public class JavaPaintComponent extends JPanel {
    
    static final String PACKAGE = "seco/notebook/syntax/resources/completion/defaultFolder.gif"; // NOI18N
    static final String CLASS = "seco/notebook/syntax/resources/completion/class_16.png"; // NOI18N
    static final String INTERFACE = "seco/notebook/syntax/resources/completion/interface.png"; // NOI18N
    static final String ENUM = "seco/syntax/notebook/resources/completion/enum.png"; // NOI18N
    static final String ANNOTATION = "seco/notebook/syntax/resources/completion/annotation_type.png"; // NOI18N

    static final String LOCAL_VARIABLE = "seco/syntax/notebook/resources/completion/localVariable.gif"; // NOI18N

    static final String FIELD_PUBLIC = "seco/notebook/syntax/resources/completion/field_16.png"; //NOI18N
    static final String FIELD_PROTECTED = "seco/notebook/syntax/resources/completion/field_protected_16.png"; //NOI18N
    static final String FIELD_PACKAGE = "seco/notebook/syntax/resources/completion/field_package_private_16.png"; //NOI18N
    static final String FIELD_PRIVATE = "seco/notebook/syntax/resources/completion/field_private_16.png"; //NOI18N        
    
    static final String FIELD_ST_PUBLIC = "seco/notebook/syntax/resources/completion/field_static_16.png"; //NOI18N
    static final String FIELD_ST_PROTECTED = "seco/notebook/syntax/resources/completion/field_static_protected_16.png"; //NOI18N
    static final String FIELD_ST_PACKAGE = "seco/notebook/syntax/resources/completion/field_static_package_private_16.png"; //NOI18N
    static final String FIELD_ST_PRIVATE = "seco/notebook/syntax/resources/completion/field_static_private_16.png"; //NOI18N

    static final String CONSTRUCTOR_PUBLIC = "seco/notebook/syntax/resources/completion/constructor_16.png"; //NOI18N
    static final String CONSTRUCTOR_PROTECTED = "seco/notebook/syntax/resources/completion/constructor_protected_16.png"; //NOI18N
    static final String CONSTRUCTOR_PACKAGE = "seco/notebook/syntax/resources/completion/constructor_package_private_16.png"; //NOI18N
    static final String CONSTRUCTOR_PRIVATE = "seco/notebook/syntax/resources/completion/constructor_private_16.png"; //NOI18N

    static final String METHOD_PUBLIC = "seco/notebook/syntax/resources/completion/method_16.png"; //NOI18N
    static final String METHOD_PROTECTED = "seco/notebook/syntax/resources/completion/method_protected_16.png"; //NOI18N
    static final String METHOD_PACKAGE = "seco/notebook/syntax/resources/completion/method_package_private_16.png"; //NOI18N
    static final String METHOD_PRIVATE = "seco/notebook/syntax/resources/completion/method_private_16.png"; //NOI18N        

    static final String METHOD_ST_PUBLIC = "seco/notebook/syntax/resources/completion/method_static_16.png"; //NOI18N
    static final String METHOD_ST_PROTECTED = "seco/notebook/syntax/resources/completion/method_static_protected_16.png"; //NOI18N
    static final String METHOD_ST_PRIVATE = "seco/notebook/syntax/resources/completion/method_static_private_16.png"; //NOI18N
    static final String METHOD_ST_PACKAGE = "seco/notebook/syntax/resources/completion/method_static_package_private_16.png"; //NOI18N
    
    private static final int ICON_WIDTH = 16;
    private static final int ICON_TEXT_GAP = 5;
    
    protected int drawX;

    protected int drawY;

    protected int drawHeight;

    private Font drawFont;

    private int fontHeight;

    private int ascent;

    private Map widths;

    private FontMetrics fontMetrics;

    private boolean isSelected;

    private boolean isDeprecated;
    
    private static final String THROWS = " throws "; // NOI18N


    private static final String[] frequentWords = new String[] {
        "", " ", "[]", "(", ")", ", ", "String", THROWS // NOI18N
    };

    public static final Color KEYWORD_COLOR = Color.darkGray;
    public static final Color TYPE_COLOR = Color.black;

    /** When an outer method/constructor is rendered. */
    static final Color ENCLOSING_CALL_COLOR = Color.gray;
    /** When an active parameter gets rendered. */
    static final Color ACTIVE_PARAMETER_COLOR = Color.black;

    public JavaPaintComponent(){
        super();
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    }
    
    protected void setSelected(boolean isSelected){
        this.isSelected = isSelected;
    }
    
    protected void setDeprecated(boolean isDeprecated){
        this.isDeprecated = isDeprecated;
    }

    protected boolean isSelected(){
        return isSelected;
    }

    protected boolean isDeprecated(){
        return isDeprecated;
    }

    public void paintComponent(Graphics g) {
        // clear background
        g.setColor(getBackground());
        java.awt.Rectangle r = g.getClipBounds();
        g.fillRect(r.x, r.y, r.width, r.height);
        draw(g);
    }

    protected void draw(Graphics g){
    }


    /** Draw the icon if it is valid for the given type.
     * Here the initial drawing assignments are also done.
     */
    protected void drawIcon(Graphics g, Icon icon) {
        Insets i = getInsets();
        if (i != null) {
            drawX = i.left;
            drawY = i.top;
        } else {
            drawX = 0;
            drawY = 0;
        }

        if (icon != null) {
            if (g != null) {
                icon.paintIcon(this, g, drawX, drawY);
            }
            drawHeight = Math.max(fontHeight, icon.getIconHeight());
        } else {
            drawHeight = fontHeight;
        }
        drawX += ICON_WIDTH + ICON_TEXT_GAP;
        if (i != null) {
            drawHeight += i.bottom;
        }
        drawHeight += drawY;
        drawY += ascent;
    }

    protected void drawString(Graphics g, String s){
        drawString(g, s, false);
    }

    /** Draw string using the foreground color */
    protected void drawString(Graphics g, String s, boolean strike) {
        if (g != null) {
            g.setColor(getForeground());
        }
        drawStringToGraphics(g, s, null, strike);
    }


    /** Draw string with given color which is first possibly modified
     * by calling getColor() method to care about selection etc.
     */
    protected void drawString(Graphics g, String s, Color c) {
        if (g != null) {
            g.setColor(getColor(s, c));
        }
        drawStringToGraphics(g, s);
    }

    protected void drawString(Graphics g, String s, Color c, Font font, boolean strike) {
        if (g != null) {
            g.setColor(getColor(s, c));
            g.setFont(font);
        }
        drawStringToGraphics(g, s, font,  strike);
        if (g != null) {
            g.setFont(drawFont);
        }

    }
    
    protected void drawTypeName(Graphics g, String s, Color c) {
        if (g == null) {
            drawString(g, "   "); // NOI18N
            drawString(g, s, c);
        } else {
            int w = getWidth() - getWidth(s) - drawX;
            int spaceWidth = getWidth(" "); // NOI18N
            if (w > spaceWidth * 2) {
                drawX = getWidth() - 2 * spaceWidth - getWidth(s);
            } else {
                drawX = getWidth() - 2 * spaceWidth - getWidth(s) - getWidth("...   "); // NOI18N
                g.setColor(getBackground());
                g.fillRect(drawX, 0, getWidth() - drawX, getHeight());
                drawString(g, "...   ", c); // NOI18N
            }
            drawString(g, s, c);
        }
    }

    protected void drawStringToGraphics(Graphics g, String s) {
        drawStringToGraphics(g, s, null, false);
    }

    protected void drawStringToGraphics(Graphics g, String s, Font font, boolean strike) {
        if (g != null) {
            if (!strike){
                g.drawString(s, drawX, drawY);
            }else{
                Graphics2D g2 = ((Graphics2D)g);
                AttributedString strikeText = new AttributedString(s);
                strikeText.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                strikeText.addAttribute(TextAttribute.FONT, g.getFont());
                g2.drawString(strikeText.getIterator(), drawX, drawY);
            }
        }
        drawX += getWidth(s, font);
    }

    protected int getWidth(String s) {
        Integer i = (Integer)widths.get(s);
        if (i != null) {
            return i.intValue();
        } else {
            if (s == null) {
                s = "";
            }
            return fontMetrics.stringWidth(s);
        }
    }

    protected int getWidth(String s, Font font) {
        if (font == null) return getWidth(s);
        return getFontMetrics(font).stringWidth(s);
    }

    protected Color getColor(String s, Color defaultColor) {
        return isSelected ? getForeground()
        : defaultColor;
    }

    private void storeWidth(String s) {
        fontMetrics.stringWidth(s);
    }

    public void setFont(Font font) {
        super.setFont(font);

        fontMetrics = this.getFontMetrics(font);
        fontHeight = fontMetrics.getHeight();
        ascent = fontMetrics.getAscent();
        if (widths != null) {
            widths.clear();
        } else {
            widths = new HashMap();
        }
        for (int i = 0; i < frequentWords.length; i++) {
            storeWidth(frequentWords[i]);
        }
        drawFont = font;
    }

    protected Font getDrawFont(){
        return drawFont;
    }

    public Dimension getPreferredSize() {
        draw(null);
        Insets i = getInsets();
        if (i != null) {
            drawX += i.right;
        }
        if (drawX > getMaximumSize().width)
            drawX = getMaximumSize().width;
        return new Dimension(drawX, drawHeight);
    }


    //.................. INNER CLASSES .......................
    
    public static class PackagePaintComponent extends JavaPaintComponent {
        
        private String pkgName;
        private boolean displayFullPackagePath;
        private Color PACKAGE_COLOR = Color.green.darker().darker().darker();
        private Icon icon;

        public PackagePaintComponent(){
            super();
        }

        public void setPackageName(String pkgName){
            this.pkgName = pkgName;
        }

        public void setDisplayFullPackagePath(boolean displayFullPackagePath){
            this.displayFullPackagePath = displayFullPackagePath;            
        }
        
        
        protected void draw(Graphics g){
            drawIcon(g, getIcon());
            String name = pkgName;
            if (!displayFullPackagePath) {
                name = name.substring(name.lastIndexOf('.') + 1);
            }
            drawString(g, name, PACKAGE_COLOR);
        }

        protected Icon getIcon(){
            if (icon!=null) return icon;
            Icon newIcon = null;
            Object newIconObj = UIManager.get("Nb.Explorer.Folder.icon"); //NOI18N
            if (newIconObj == null) { 
                newIcon = new ImageIcon(IconManager.getIcon(PACKAGE));
            } else if (newIconObj instanceof Image){
                newIcon = new ImageIcon((Image) newIconObj);
            } else if (newIconObj instanceof Icon){
                newIcon = (Icon) newIconObj;
            }
            icon = newIcon;
            return newIcon;            
        }
    }
    
    public static class ClassPaintComponent extends JavaPaintComponent {

        private Color CLASS_COLOR = Color.red.darker().darker().darker();
        private Color PACKAGE_COLOR = Color.gray;
        String simpleClassName;
        String fqName = null;
        private Icon icon;
        Class cls;

        public void setSimpleClassName(String simpleClassName){
            this.simpleClassName = simpleClassName;
        }
        
        public void setFQName(String fqName) {
            this.fqName = fqName;
        }
        
        public void setCls(Class cls){
           this.cls = cls;
        }
        
        protected void draw(Graphics g){
            boolean strike = isDeprecated();
            drawIcon(g, getIcon());
            drawString(g, simpleClassName, getColor(), null, strike);
            if (fqName != null && fqName.length() > 0)
                drawString(g, fqName, PACKAGE_COLOR, null, strike);
        }

        protected Color getColor(){
            return CLASS_COLOR;
        }
        
        protected Icon getIcon(){
            if (icon==null) icon = new ImageIcon(IconManager.getIcon(CLASS));
            return icon;
        }
        
        public String getToolTipText(){
            //if (cls == null) 
            	return ""; //NOI18N
            /*	
            FileObject resourceFO = JavaMetamodel.getManager().getFileObject(cls.getResource());
            if (resourceFO != null) { // Fix of #57032
                // XXX surely there is an easier way to do this?
                // #49737: show location of the source (just the root, not full path which is redundant).
                ClassPath sourcepath = ClassPath.getClassPath(resourceFO, ClassPath.SOURCE);
                if (sourcepath != null) {
                    FileObject[] roots = sourcepath.getRoots();
                    for (int i = 0; i < roots.length; i++) {
                        if (FileUtil.isParentOf(roots[i], resourceFO)) {
                            return FileUtil.getFileDisplayName(roots[i]);
                        }
                    }
                }
                // Fallback:
                return FileUtil.getFileDisplayName(resourceFO);
            } else {
                return ""; //NOI18N
            }
            */
        }
        
    }

    public static class InterfacePaintComponent extends ClassPaintComponent {
                
        private Icon icon;
        private Color INTERFACE_COLOR = Color.darkGray;
        
        protected Color getColor(){
            return INTERFACE_COLOR;
        }
        
        protected Icon getIcon(){
            if (icon == null) icon = new ImageIcon(IconManager.getIcon(INTERFACE));
            return icon;            
        }
        
    }

    public static class EnumPaintComponent extends ClassPaintComponent {

        private Icon icon;

        protected Icon getIcon(){
            if (icon == null) icon = new ImageIcon(IconManager.getIcon(ENUM));
            return icon;
        }

    }
    public static class AnnotationPaintComponent extends ClassPaintComponent {

        private Icon icon;

        protected Icon getIcon(){
            if (icon == null) icon = new ImageIcon(IconManager.getIcon(ANNOTATION));
            return icon;
        }

    }

    public static class FieldPaintComponent extends JavaPaintComponent {
        
        private String typeName;
        private Color typeColor;
        private String fldName;
        private int modifiers;
        private boolean isLocalVar;
        private Icon icon[][] = new Icon[2][4];
        private Icon localIcon;

        private Color FIELD_COLOR = Color.blue.darker();
        private Color VAR_COLOR = Color.blue.darker().darker();

        public FieldPaintComponent(boolean isLocalVar){
            super();
            this.isLocalVar = isLocalVar;
        }
        
        public void setFieldName(String fldName){
            this.fldName= fldName;
        }
        
        public String getFieldName() {
            return fldName;
        }
        
        public void setTypeColor(Color typeColor){
            this.typeColor = typeColor;
        }
        
        public Color getTypeColor() {
            return typeColor;
        }
        
        public void setTypeName(String typeName){
            this.typeName = typeName;
        }
        
        public String getTypeName() {
            return typeName;
        }
        
        public void setModifiers(int modifiers){
            this.modifiers = modifiers;
        }
        
        public int getModifiers() {
            return modifiers;
        }

        protected void draw(Graphics g){
            boolean strike = isDeprecated();
            drawIcon(g, getIcon());
           
            if ((modifiers & JavaCompletion.LOCAL_MEMBER_BIT) != 0){
                // it is local field, draw as bold
//                drawString(g, fldName, isLocalVar ? VAR_COLOR : FIELD_COLOR, getDrawFont().deriveFont(Font.BOLD), strike); // Workaround for issue #55133
                drawString(g, fldName, isLocalVar ? VAR_COLOR : FIELD_COLOR, new Font(getDrawFont().getName(), getDrawFont().getStyle() | Font.BOLD, getDrawFont().getSize()), strike);
            }else{
                drawString(g, fldName, isLocalVar ? VAR_COLOR : FIELD_COLOR , null, strike);
            }
            drawTypeName(g, typeName, typeColor);
        }

        protected Icon getIcon(){
            String iconPath = FIELD_PUBLIC;

            int level = JavaCompletion.getLevel(modifiers);
            if (isLocalVar && level == JavaCompletion.PACKAGE_LEVEL) {
                if (localIcon == null)
                    localIcon = new ImageIcon(IconManager.getIcon(LOCAL_VARIABLE));
                return localIcon;
            }
            boolean isStatic = (modifiers & Modifier.STATIC) != 0;
            Icon cachedIcon = icon[isStatic?1:0][level];
            if (cachedIcon != null) return cachedIcon;
            
            if (isStatic){
                //static field
                switch (level) {
                    case JavaCompletion.PRIVATE_LEVEL:
                        iconPath = FIELD_ST_PRIVATE;
                        break;

                    case JavaCompletion.PACKAGE_LEVEL:
                        iconPath = FIELD_ST_PACKAGE;
                        break;

                    case JavaCompletion.PROTECTED_LEVEL:
                        iconPath = FIELD_ST_PROTECTED;
                        break;

                    case JavaCompletion.PUBLIC_LEVEL:
                        iconPath = FIELD_ST_PUBLIC;
                        break;
                }
            }else{
                switch (level) {
                    case JavaCompletion.PRIVATE_LEVEL:
                        iconPath = FIELD_PRIVATE;
                        break;

                    case JavaCompletion.PACKAGE_LEVEL:
                        iconPath = FIELD_PACKAGE;
                        break;

                    case JavaCompletion.PROTECTED_LEVEL:
                        iconPath = FIELD_PROTECTED;
                        break;

                    case JavaCompletion.PUBLIC_LEVEL:
                        iconPath = FIELD_PUBLIC;
                        break;
                }
            }
            ImageIcon newIcon = new ImageIcon(IconManager.getIcon(iconPath));
            icon[isStatic?1:0][level] = newIcon;
            return newIcon;            
        }
    
    }

    public static class CallableFeaturePaintComponent extends JavaPaintComponent {

//        protected CallableFeature cf;
        private Color PARAMETER_NAME_COLOR = Color.magenta.darker();
        private List params = new ArrayList();
        private List excs = new ArrayList();
        private int modifiers;
        private String cfName, typeName;
        private Color typeColor;
        private int activeParameterIndex = -1;
        private int drawParamIndex;


        public int getCFModifiers(){
            return modifiers;
        }
        
        public String getCFName(){
            return cfName;
        }
        
        public String getTypeName(){
            return typeName;
        }

        public Color getTypeColor(){
            return typeColor;
        }
        
        public void setModifiers(int modifiers){
            this.modifiers = modifiers;
        }
        
        public void setTypeName(String typeName){
            this.typeName = typeName;
        }
        
        public void setTypeColor(Color typeColor){
            this.typeColor = typeColor;
        }
        
        public void setFeatureName(String cfName){
            this.cfName = cfName;
        }
        
        public void setParams(List params){
            this.params = params;
        }
        
        public void setExceptions(List excs){
            this.excs = excs;
        }
        
        
        protected List getParamList(){
            return params;
        }
        
        protected List getExceptionList(){
            return excs;
        }

        int getActiveParameterIndex() {
            return activeParameterIndex;
        }
        
        /**
         * If set to value different than -1 it marks that
         * this component renders an outer enclosing constructor/method
         * and the given index is the index of the active parameter
         * which is being completed as an inner expression.
         */
        void setActiveParameterIndex(int activeParamIndex) {
            this.activeParameterIndex = activeParamIndex;
        }
        
        /**
         * Check whether this paint component renders an outer method/constructor
         * which should be rendered in grey with black active parameter.
         *
         * @return true if this paint component renders outer method/constructor
         *  or false otherwise.
         */
        boolean isEnclosingCall() {
            return (activeParameterIndex != -1);
        }
        
        protected void drawExceptions(Graphics g, List exc, boolean strike) {
            if (exc.size() > 0) {
                Color color = isEnclosingCall() ? ENCLOSING_CALL_COLOR : KEYWORD_COLOR;
                drawString(g, THROWS, color, null, strike);
                for (Iterator it = exc.iterator(); it.hasNext();) {
                    JavaResultItem.ExceptionStr ex = (JavaResultItem.ExceptionStr) it.next();
                    Color exColor = isEnclosingCall() ? ENCLOSING_CALL_COLOR : ex.getTypeColor();
                    drawString(g, ex.getName(), exColor, null, strike);
                    if (it.hasNext()) {
                        Color commaColor = isEnclosingCall() ? ENCLOSING_CALL_COLOR : getForeground();
                        drawString(g, ", ", commaColor, getFont(), strike); // NOI18N
                    }

                }
            }
        }
        
        protected void drawParameter(Graphics g, JavaResultItem.ParamStr prm) {
            drawParameter(g, prm, false);
        }

        protected void drawParameter(Graphics g, JavaResultItem.ParamStr prm, boolean strike) {

            //drawType
            Font prmFont = isEnclosingCall() && (drawParamIndex == activeParameterIndex)
                // Could be deriveFont() instead but it may cause problems on some platforms
                ? new Font(getFont().getName(), Font.BOLD, getFont().getSize())
                : getFont();
            
            Color typeColor = isEnclosingCall()
                ? (drawParamIndex == activeParameterIndex ? ACTIVE_PARAMETER_COLOR : ENCLOSING_CALL_COLOR)
                : prm.getTypeColor();

            drawString(g, prm.getSimpleTypeName(), typeColor, prmFont, strike);
            
            Color foreColor = isEnclosingCall()
                ? (drawParamIndex == activeParameterIndex ? ACTIVE_PARAMETER_COLOR : ENCLOSING_CALL_COLOR)
                : getForeground();

            if (prm.isVarArg()) {
                drawString(g, "...", foreColor, getFont(), strike); // NOI18N
            }
            String name = prm.getName();
            if (name != null && name.length() > 0) {
                drawString(g, " ", foreColor, getFont(), strike); // NOI18N
                Color prmNameColor = isEnclosingCall()
                    ? (drawParamIndex == activeParameterIndex ? ACTIVE_PARAMETER_COLOR : ENCLOSING_CALL_COLOR)
                    : PARAMETER_NAME_COLOR;

                drawString(g, prm.getName(), prmNameColor, prmFont, strike);
            }
        }

        protected void drawParameterList(Graphics g, List prmList) {
            drawParameterList(g, prmList, false);
        }

        protected void drawParameterList(Graphics g, List prmList, boolean strike) {
            Color foreColor = isEnclosingCall() ? ENCLOSING_CALL_COLOR : getForeground();
            drawString(g, "(", foreColor, getFont(), strike); // NOI18N
            drawParamIndex = 0; // maintain for drawParameter()
            for (Iterator it = prmList.iterator(); it.hasNext();) {
                drawParameter(g, (JavaResultItem.ParamStr)it.next(), strike);
                if (it.hasNext()) {
                    drawString(g, ", ", foreColor, getFont(), strike); // NOI18N
                }
                drawParamIndex++;
            }
            drawString(g, ")", foreColor, getFont(), strike); // NOI18N
        }
    }

    public static class MethodPaintComponent extends CallableFeaturePaintComponent {
        
        private Color METHOD_COLOR = Color.red.darker().darker();
        private Icon icon[][] = new Icon[2][4];
        
        protected Icon getIcon(){

            int level = JavaCompletion.getLevel(getCFModifiers());
            boolean isStatic = (getCFModifiers() & Modifier.STATIC) != 0;
            Icon cachedIcon = icon[isStatic?1:0][level];
            if (cachedIcon != null) return cachedIcon;
            
            String iconPath = METHOD_PUBLIC;
            
            if ((getCFModifiers() & Modifier.STATIC) != 0){
                //static method
                switch (level) {
                    case JavaCompletion.PRIVATE_LEVEL:
                        iconPath = METHOD_ST_PRIVATE;
                        break;

                    case JavaCompletion.PACKAGE_LEVEL:
                        iconPath = METHOD_ST_PACKAGE;
                        break;

                    case JavaCompletion.PROTECTED_LEVEL:
                        iconPath = METHOD_ST_PROTECTED;
                        break;

                    case JavaCompletion.PUBLIC_LEVEL:
                        iconPath = METHOD_ST_PUBLIC;
                        break;
                }
            }else{
                switch (level) {
                    case JavaCompletion.PRIVATE_LEVEL:
                        iconPath = METHOD_PRIVATE;
                        break;

                    case JavaCompletion.PACKAGE_LEVEL:
                        iconPath = METHOD_PACKAGE;
                        break;

                    case JavaCompletion.PROTECTED_LEVEL:
                        iconPath = METHOD_PROTECTED;
                        break;

                    case JavaCompletion.PUBLIC_LEVEL:
                        iconPath = METHOD_PUBLIC;
                        break;
                }
            }
            ImageIcon newIcon = new ImageIcon(IconManager.getIcon(iconPath));
            icon[isStatic?1:0][level] = newIcon;
            return newIcon;            
        }

        protected void draw(Graphics g){
            boolean strike = isDeprecated();
            Icon icon = isEnclosingCall() ? null : getIcon();
            drawIcon(g, icon);
     
            //drawType
            Color methodColor = isEnclosingCall() ? ENCLOSING_CALL_COLOR : METHOD_COLOR;
            if ((getCFModifiers() & JavaCompletion.LOCAL_MEMBER_BIT) != 0 && !isEnclosingCall()){
//                drawString(g, getCFName(), METHOD_COLOR , getDrawFont().deriveFont(Font.BOLD), strike); // Workaround for issue #55133
                drawString(g, getCFName(), methodColor, new Font(getDrawFont().getName(), getDrawFont().getStyle() | Font.BOLD, getDrawFont().getSize()), strike);
            }else{
                drawString(g, getCFName(), methodColor, null, strike);
            }
            drawParameterList(g, getParamList());
            Color typeColor = isEnclosingCall() ? ENCLOSING_CALL_COLOR : getTypeColor();
            drawTypeName(g, getTypeName(), typeColor);
        }

    }
    
    public static class ConstructorPaintComponent extends CallableFeaturePaintComponent {

        private Color CONSTRUCTOR_COLOR = Color.orange.darker();
        private Icon icon[] = new Icon[4];
        
        protected Icon getIcon(){
            
            String iconPath = CONSTRUCTOR_PUBLIC;
            int level = JavaCompletion.getLevel(getCFModifiers());
            
            Icon cachedIcon = icon[level];
            if (cachedIcon != null) return cachedIcon;
            
            switch (level) {
                case JavaCompletion.PRIVATE_LEVEL:
                    iconPath = CONSTRUCTOR_PRIVATE;
                    break;

                case JavaCompletion.PACKAGE_LEVEL:
                    iconPath = CONSTRUCTOR_PACKAGE;
                    break;

                case JavaCompletion.PROTECTED_LEVEL:
                    iconPath = CONSTRUCTOR_PROTECTED;
                    break;

                case JavaCompletion.PUBLIC_LEVEL:
                    iconPath = CONSTRUCTOR_PUBLIC;
                    break;
            }
            ImageIcon newIcon = new ImageIcon(IconManager.getIcon(iconPath));
            icon[level] = newIcon;
            return newIcon;            
        }

        protected void draw(Graphics g){
            boolean strike = isDeprecated();
            Icon icon = isEnclosingCall() ? null : getIcon();
            drawIcon(g, icon);
            Color ctrColor = isEnclosingCall() ? ENCLOSING_CALL_COLOR : CONSTRUCTOR_COLOR;
            drawString(g, getCFName(), ctrColor, null, strike);
            drawParameterList(g, getParamList());
        }
    }

    public static class AttributePaintComponent extends JavaPaintComponent {

        private String typeName;
        private Color typeColor;
        private String attrName;
        private String defaultValueText;

        private Color ATTR_COLOR = Color.darkGray;

        public void setAttrName(String attrName){
            this.attrName= attrName;
        }

        public void setTypeColor(Color typeColor){
            this.typeColor = typeColor;
        }

        public void setTypeName(String typeName){
            this.typeName = typeName;
        }

        public void setDefaultValueText(String defaultValueText) {
            this.defaultValueText = defaultValueText;
        }

        protected void draw(Graphics g){
            drawIcon(g, null);
            drawString(g, typeName, typeColor);
            drawString(g, " "); // NOI18N
            if (defaultValueText == null) {
//                drawString(g, attrName, ATTR_COLOR, getDrawFont().deriveFont(Font.BOLD), false); // Workaround for issue #55133
                drawString(g, attrName, ATTR_COLOR, new Font(getDrawFont().getName(), getDrawFont().getStyle() | Font.BOLD, getDrawFont().getSize()), false);
            } else {
                drawString(g, attrName, ATTR_COLOR);
                drawString(g, " = "); // NOI18N
                drawString(g, defaultValueText);
            }
        }
    }

    public static class StringPaintComponent extends JavaPaintComponent {

        private String str;

        public void setString(String str){
            this.str = str;            
        }
        
        protected void draw(Graphics g){
            drawIcon(g, null);
            drawString(g, str, TYPE_COLOR);
        }
    }


}

