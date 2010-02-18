/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.html;

import static javax.swing.text.html.HTML.Tag.CITE;
import static javax.swing.text.html.HTML.Tag.CODE;
import static javax.swing.text.html.HTML.Tag.H1;
import static javax.swing.text.html.HTML.Tag.H2;
import static javax.swing.text.html.HTML.Tag.H3;
import static javax.swing.text.html.HTML.Tag.H4;
import static javax.swing.text.html.HTML.Tag.H5;
import static javax.swing.text.html.HTML.Tag.H6;
import static javax.swing.text.html.HTML.Tag.OL;
import static javax.swing.text.html.HTML.Tag.P;
import static javax.swing.text.html.HTML.Tag.PRE;
import static javax.swing.text.html.HTML.Tag.TABLE;
import static javax.swing.text.html.HTML.Tag.UL;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;

import edu.umd.cs.piccolox.pswing.PSwing;

import seco.gui.GUIHelper;
import seco.notebook.gui.ToolbarButton;

public class HTMLToolBar extends JToolBar
{
    public static HTML.Tag[] STYLES = { P, /* BLOCKQUOTE, */
    /* javax.swing.text.html.HTML.Tag.CENTER, */
    CITE, CODE, H1, H2, H3, H4, H5, H6, PRE };

    private MyToggleButton bBold;
    private MyToggleButton bItalic;
    private MyToggleButton bUnderline;

    private MyToggleButton bUnorderedList;
    private MyToggleButton bOrderedList;
    private MyToggleButton bSubscript;
    private MyToggleButton bSuperscript;
    private MyToggleButton bStrike;

    private MyToggleButton bAlignLeft;
    private MyToggleButton bAlignCenter;
    private MyToggleButton bAlignRight;
    private MyToggleButton bAlignJustify;

    private MyComboBox cbStyles;

    private ToolbarButton linkButton;
    private ToolbarButton imageButton;

    private ToolbarButton tableButton;
    private ToolbarButton fmtTableButton;
    private ToolbarButton insRowButton;
    private ToolbarButton insColButton;

    // ////////////////////////

    public HTMLToolBar()
    {
        super(HTMLEditor.TOOLBAR_NAME);
        // init();

    }

    public void init()
    {
        final MyHTMLEditorKit htmlKit = new MyHTMLEditorKit();
        add(new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.fontAction), "Font"));
        Action act = htmlKit.getActionByName("font-bold");
        act.putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("Bold16.gif"));
        bBold = new MyToggleButton("bBold", act, "Bold font");
        add(bBold);
        act = htmlKit.getActionByName("font-italic");
        act.putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("Italic16.gif"));
        bItalic = new MyToggleButton("bItalic", act, "Italic font");
        add(bItalic);
        act = htmlKit.getActionByName("font-underline");
        act.putValue(Action.SMALL_ICON, HTMLUtils
                .resolveIcon("Underline16.gif"));
        bUnderline = new MyToggleButton("bUnderline", act, "Underline font");
        add(bUnderline);
        bSubscript = new MyToggleButton("bSubscript", htmlKit
                .getActionByName(MyHTMLEditorKit.subscriptAction), "Subscript");
        add(bSubscript);
        bSuperscript = new MyToggleButton("bSuperscript", htmlKit
                .getActionByName(MyHTMLEditorKit.superscriptAction),
                "Superscript");
        add(bSuperscript);
        bStrike = new MyToggleButton("bStrike", htmlKit
                .getActionByName(MyHTMLEditorKit.strikeAction), "Strike");
        add(bStrike);

        linkButton = new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.linkAction), "Link");
        linkButton.setName("linkButton");
        add(linkButton);

        imageButton = new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.imageAction), "Image");
        imageButton.setName("imageButton");
        add(imageButton);
        add(new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.insertSymbolAction),
                "Insert Special Symbol"));

        cbStyles = new MyComboBox(STYLES);
        cbStyles.setMaximumSize(new Dimension(45, 23));
        cbStyles.setRequestFocusEnabled(false);
        cbStyles.setName("cbStyles");
        add(cbStyles);
        AbstractAction lst = new StylesAction();
        cbStyles.setAction(lst);
        bUnorderedList = new MyToggleButton("bUnorderedList", htmlKit
                .getActionByName(MyHTMLEditorKit.unorderedListAction),
                "Unordered List");
        add(bUnorderedList);
        bOrderedList = new MyToggleButton("bOrderedList", htmlKit
                .getActionByName(MyHTMLEditorKit.orderedListAction),
                "Ordered List");
        add(bOrderedList);
        addSeparator();
        ButtonGroup alignGroup = new ButtonGroup();
        bAlignLeft = new MyToggleButton("bAlignLeft", htmlKit
                .getActionByName("left"), "Align Left");
        add(bAlignLeft);
        alignGroup.add(bAlignLeft);
        bAlignCenter = new MyToggleButton("bAlignCenter", htmlKit
                .getActionByName("center"), "Align Center");
        add(bAlignCenter);
        alignGroup.add(bAlignCenter);
        bAlignRight = new MyToggleButton("bAlignRight", htmlKit
                .getActionByName("right"), "bAlign Right");
        add(bAlignRight);
        alignGroup.add(bAlignRight);
        bAlignJustify = new MyToggleButton("bAlignJustify", htmlKit
                .getActionByName("justify"), "Align Justify");
        add(bAlignJustify);
        alignGroup.add(bAlignJustify);

        addSeparator();
        add(new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.indentLeftAction),
                "Indent Left"));
        add(new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.indentRightAction),
                "Indent Right"));
        add(new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.clearFormatAction),
                "Clear Format"));

        tableButton = new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.tableAction), "Table");
        add(tableButton);

        fmtTableButton = new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.formatTableAction),
                "Format Table");
        fmtTableButton.setName("fmtTableButton");
        add(fmtTableButton);
        insRowButton = new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.insertTableRowAction),
                "Insert Table Row");
        insRowButton.setName("insRowButton");
        add(insRowButton);
        insColButton = new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.insertTableColAction),
                "Insert Table Column");
        insColButton.setName("insColButton"); 
        add(insColButton);

        // add( new ToolbarButton(htmlKit.getActionByName(
        // MyHTMLEditorKit.sourceAction), "Go to Source"););
    }
    
    public void showAttributes(HTMLEditor editor, int p)
    {
        // TODO: change this method, because buttons are not persistent
        //if (TopFrame.PICCOLO) return;
        if(editor == null) return;
        AttributeSet attr = editor.getDoc().getCharacterElement(p)
                .getAttributes();
        updateToggle(bBold, "bBold", StyleConstants.isBold(attr));
        updateToggle(bItalic, "bItalic", StyleConstants.isItalic(attr));
        updateToggle(bUnderline, "bUnderline",StyleConstants.isUnderline(attr));
        updateToggle(bStrike, "bStrike",StyleConstants.isStrikeThrough(attr));
        updateToggle(bSubscript, "bSubscript",StyleConstants.isSubscript(attr));
        updateToggle(bSuperscript, "bSuperscript",StyleConstants.isSuperscript(attr));

        boolean link = ((editor.getSelectionEnd() > editor.getSelectionStart()));
        updateButton(linkButton, "linkButton", link);

        String align = (String) attr
                .getAttribute(javax.swing.text.html.HTML.Attribute.ALIGN);
        if (align == null)
            align = "" + attr.getAttribute(CSS.Attribute.TEXT_ALIGN);
        if (align != null)
        {// left, right, center, justify
            updateToggle(bAlignCenter, "bAlignCenter",align.equalsIgnoreCase("center"));
            updateToggle(bAlignLeft, "bAlignLeft",align.equalsIgnoreCase("left"));
            updateToggle(bAlignRight, "bAlignRight",align.equalsIgnoreCase("right"));
            updateToggle(bAlignJustify, "bAlignJustify",align.equalsIgnoreCase("justify"));
        }
        else
        {
            updateToggle(bAlignCenter,"bAlignCenter",false);
            updateToggle(bAlignLeft,"bAlignLeft",false);
            updateToggle(bAlignRight,"bAlignRight",false);
            updateToggle(bAlignJustify,"bAlignJustify",false);
        }

        Element ep = editor.getDoc().getParagraphElement(p);
        HTML.Tag tag = HTMLUtils.getName(ep);
        int index = -1;
        if (tag != null)
        {
            for (int k = 0; k < STYLES.length; k++)
            {
                if (STYLES[k].equals(tag))
                {
                    index = k;
                    break;
                }
            }
        }
        cbStyles =(cbStyles != null) ? cbStyles : 
            (MyComboBox) getComponent("cbStyles");
        cbStyles.setSelectedIndexEx(index);
        updateToggle(bUnorderedList, "bUnorderedList", HTMLUtils.isInTag(editor, UL));
        updateToggle(bOrderedList, "bOrderedList", HTMLUtils.isInTag(editor, OL));
        MyHTMLEditorKit htmlKit = new MyHTMLEditorKit();
        bUnorderedList = (bUnorderedList == null) ?
                getMyToggleButton("bUnorderedList"): bUnorderedList;
        bOrderedList = (bOrderedList == null) ?
                        getMyToggleButton("bOrderedList"): bOrderedList;        
        if(bUnorderedList != null)
           htmlKit.getActionByName(MyHTMLEditorKit.listFormatAction).setEnabled(
                bUnorderedList.isSelected() 
                || bOrderedList.isSelected());

        boolean inTable = HTMLUtils.isInTag(editor, TABLE);
        updateButton(fmtTableButton, "fmtTableButton", inTable);
        updateButton(insRowButton, "insRowButton", inTable);
        updateButton(insColButton, "insColButton", inTable);

        htmlKit.getActionByName(MyHTMLEditorKit.deleteTableRowAction)
                .setEnabled(inTable);
        htmlKit.getActionByName(MyHTMLEditorKit.deleteTableColAction)
                .setEnabled(inTable);
    }
    
    private void updateToggle(MyToggleButton but, String name, boolean b)
    {
        if(but == null)
            but = getMyToggleButton(name);
        if(but != null)
            but.setSelectedEx(b);
    }
    
    private void updateButton(ToolbarButton but, String name, boolean b)
    {
        if(but == null)
            but = (ToolbarButton) getComponent(name);
        if(but != null)
            but.setEnabled(b);
    }
    
    private MyToggleButton getMyToggleButton(String name)
    {
       return (MyToggleButton) getComponent(name);
        
    }
    
    private Component getComponent(String name)
    {
        for(int i = 0; i < getComponentCount(); i++)
            if(name.equals(getComponent(i).getName()))
                return getComponent(i);
        return null;
    }

    private static HTMLEditor getEditor(ActionEvent e)
    {
        JTextComponent tcomp = getTextComponent(e);
        if (tcomp instanceof JEditorPane) { return (HTMLEditor) tcomp; }
        return null;
    }

    private static final HTMLEditor getTextComponent(ActionEvent e)
    {
        if (e != null)
        {
            Object o = e.getSource();
            if (o instanceof HTMLEditor) { return (HTMLEditor) o; }
        }
        return HTMLEditor.getFocusedEditor();
    }


    public static class MyToggleButton extends JToggleButton implements
            MouseListener
    {
        private final static Dimension buttonSize = new Dimension(24, 24);

        protected Border m_raised = new SoftBevelBorder(BevelBorder.RAISED);
        protected Border m_lowered = new SoftBevelBorder(BevelBorder.LOWERED);
        protected Border m_inactive = new EmptyBorder(3, 3, 3, 3);
        protected Border m_border = m_inactive;
        protected Insets m_ins = new Insets(4, 4, 4, 4);

        public MyToggleButton()
        {
            super();
            init();
            addMouseListener(this);
        }

        public MyToggleButton(ImageIcon img, String tip)
        {
            super("", img);
            setToolTipText(tip);
            init();
            addMouseListener(this);
        }

        public MyToggleButton(String name, Action a, String tip)
        {
            super(a);
            this.setName(name);
            setToolTipText(tip);
            init();
            addMouseListener(this);
        }

        // public MyToggleButton(Action a, String tip, ImageIcon img)
        // {
        // this(a, tip);
        // a.putValue(Action.SMALL_ICON, img);
        // }

        public boolean isFocusable()
        {
            return false;
        }

        public void setSelectedEx(boolean selected)
        {
            getAction().setEnabled(false);
            setSelected(selected);
            getAction().setEnabled(true);
            itemStateChanged(null);
        }

        private void init()
        {
            setText("");
            // setBorderPainted(false);
            setMargin(m_ins); // new Insets(0, 0, 0, 0));
            setIconTextGap(0);
            setContentAreaFilled(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setMinimumSize(buttonSize);
            setPreferredSize(buttonSize);
            setMaximumSize(buttonSize);
            setFocusPainted(false);
            setRequestFocusEnabled(false);
        }

        public float getAlignmentY()
        {
            return 0.5f;
        }

        // Overridden for 1.4 bug fix
        public Insets getInsets()
        {
            return m_ins;
        }

        public Border getBorder()
        {
            return isSelected() ? m_lowered : m_border; // m_raised;
        }

        public void itemStateChanged(ItemEvent e)
        {
            setBorder(isSelected() ? m_lowered : m_raised);
        }

        public void mousePressed(MouseEvent e)
        {
            m_border = m_lowered;
            setBorder(m_lowered);
        }

        public void mouseReleased(MouseEvent e)
        {
            m_border = m_inactive;
            setBorder(m_inactive);
        }

        public void mouseClicked(MouseEvent e)
        {
        }

        public void mouseEntered(MouseEvent e)
        {
            m_border = m_raised;
            setBorder(m_raised);
        }

        public void mouseExited(MouseEvent e)
        {
            m_border = m_inactive;
            setBorder(m_inactive);
        }

        public void setText(String text)
        {
            super.setText("");
        }
    }

    public static class MyComboBox extends JComboBox
    {
        public MyComboBox()
        {
            super();
            setUI(new ComboUI());
        }

        public MyComboBox(ComboBoxModel aModel)
        {
            super(aModel);
            setUI(new ComboUI());
        }

        public MyComboBox(Object[] items)
        {
            super(items);
            setUI(new ComboUI());
        }

        public MyComboBox(Vector<?> items)
        {
            super(items);
            setUI(new ComboUI());
        }

        public void setSelectedItemEx(Object item)
        {
            getAction().setEnabled(false);
            setSelectedItem(item);
            getAction().setEnabled(true);
        }

        public void setSelectedIndexEx(int index)
        {
            getAction().setEnabled(false);
            setSelectedIndex(index);
            getAction().setEnabled(true);
        }

        public boolean isFocusable()
        {
            return false;
        }
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        for (int i = 0; i < getComponentCount(); i++)
            this.getComponent(i).setEnabled(enabled);
    }

    private static class ComboUI extends MetalComboBoxUI
    {
        @Override
        protected ComboPopup createPopup() {
            PBasicComboPopup popup = new PBasicComboPopup(comboBox);
            popup.getAccessibleContext().setAccessibleParent(comboBox);
            return popup;
        }
    }
    
    static class PBasicComboPopup extends BasicComboPopup {

        public PBasicComboPopup(JComboBox combo) {
            super(combo);
        }

       protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
            Point r = getPopupMenuOrigin();
            Rectangle sup = super.computePopupBounds(px, py, pw, ph);
            if(r == null) return sup;
            return new Rectangle((int) r.getX(), (int) r.getY(), (int) sup.getWidth(), (int) sup.getHeight());
        }
       
       private Point getPopupMenuOrigin()
       {
           Point pt = null;
           //System.out.println("PBasicComboPopup: " + comboBox.getParent());
           if (comboBox.getParent() != null && comboBox.getParent() instanceof JComponent)
           {
               return GUIHelper.adjustPointInPicollo(
                       (JComponent) comboBox.getParent(), new Point(0,0));
           }
           return pt;
       }
    }
    public static class StylesAction extends AbstractAction
    {

        public void actionPerformed(ActionEvent e)
        {
            if (!isEnabled()) return;
            if (!(e.getSource() instanceof MyComboBox)) return;
            MyComboBox cbStyles = (MyComboBox) e.getSource();
            HTML.Tag style = (HTML.Tag) cbStyles.getSelectedItem();
            if (style == null) return;
            MutableAttributeSet attr = new SimpleAttributeSet();
            attr.addAttribute(StyleConstants.NameAttribute, style);
            HTMLEditor editor = getEditor(e);
            if (editor != null) editor.setAttributeSet(attr, true);
            // editor.grabFocus();
        }

    }

}
