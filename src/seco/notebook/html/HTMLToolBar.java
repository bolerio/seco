/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.html;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;

import seco.notebook.AppForm;
import seco.notebook.gui.ToolbarButton;

import static javax.swing.text.html.HTML.Tag.*;

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

    public HTMLToolBar()
    {
        super(HTMLEditor.TOOLBAR_NAME);
        //init();
    }

    public void init()
    {
        final MyHTMLEditorKit htmlKit = new MyHTMLEditorKit();
        add(new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.fontAction), "Font"));
        Action act = htmlKit.getActionByName("font-bold");
        act.putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("Bold16.gif"));
        bBold = new MyToggleButton(act, "Bold font");
        add(bBold);
        act = htmlKit.getActionByName("font-italic");
        act.putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("Italic16.gif"));
        bItalic = new MyToggleButton(act, "Italic font");
        add(bItalic);
        act = htmlKit.getActionByName("font-underline");
        act.putValue(Action.SMALL_ICON, HTMLUtils.resolveIcon("Underline16.gif"));
        bUnderline = new MyToggleButton(act, "Underline font");
        add(bUnderline);
        bSubscript = new MyToggleButton(htmlKit
                .getActionByName(MyHTMLEditorKit.subscriptAction), "Subscript");
        add(bSubscript);
        bSuperscript = new MyToggleButton(htmlKit
                .getActionByName(MyHTMLEditorKit.superscriptAction),
                "Superscript");
        add(bSuperscript);
        bStrike = new MyToggleButton(htmlKit
                .getActionByName(MyHTMLEditorKit.strikeAction), "Strike");
        add(bStrike);

        linkButton = new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.linkAction), "Link");
        add(linkButton);

        imageButton = new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.imageAction), "Image");
        add(imageButton);
        add(new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.insertSymbolAction),
                "Insert Special Symbol"));

        cbStyles = new MyComboBox(STYLES);
        cbStyles.setMaximumSize(new Dimension(45, 23));
        cbStyles.setRequestFocusEnabled(false);
        add(cbStyles);
        AbstractAction lst = new StylesAction();  
        cbStyles.setAction(lst);
        bUnorderedList = new MyToggleButton(htmlKit
                .getActionByName(MyHTMLEditorKit.unorderedListAction),
                "Unordered List");
        add(bUnorderedList);
        bOrderedList = new MyToggleButton(htmlKit
                .getActionByName(MyHTMLEditorKit.orderedListAction),
                "Ordered List");
        add(bOrderedList);
        addSeparator();
        ButtonGroup alignGroup = new ButtonGroup();
        bAlignLeft = new MyToggleButton(htmlKit.getActionByName("left"));
        add(bAlignLeft);
        alignGroup.add(bAlignLeft);
        bAlignCenter = new MyToggleButton(htmlKit.getActionByName("center"));
        add(bAlignCenter);
        alignGroup.add(bAlignCenter);
        bAlignRight = new MyToggleButton(htmlKit.getActionByName("right"));
        add(bAlignRight);
        alignGroup.add(bAlignRight);
        bAlignJustify = new MyToggleButton(htmlKit.getActionByName("justify"));
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
        add(fmtTableButton);
        insRowButton = new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.insertTableRowAction),
                "Insert Table Row");
        add(insRowButton);
        insColButton = new ToolbarButton(htmlKit
                .getActionByName(MyHTMLEditorKit.insertTableColAction),
                "Insert Table Column");
        add(insColButton);

        // add( new ToolbarButton(htmlKit.getActionByName(
        // MyHTMLEditorKit.sourceAction), "Go to Source"););
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
        return HTMLEditor.editor;
    }

    public void showAttributes(HTMLEditor editor, int p)
    {
        //TODO: change this method, because buttons are not persistent
        if(AppForm.PICCOLO) return;
            
        AttributeSet attr = editor.getDoc().getCharacterElement(p)
                .getAttributes();
        bBold.setSelectedEx(StyleConstants.isBold(attr));
        bItalic.setSelectedEx(StyleConstants.isItalic(attr));
        bUnderline.setSelectedEx(StyleConstants.isUnderline(attr));
        bStrike.setSelectedEx(StyleConstants.isStrikeThrough(attr));
        bSubscript.setSelectedEx(StyleConstants.isSubscript(attr));
        bSuperscript.setSelectedEx(StyleConstants.isSuperscript(attr));

        boolean link = ((editor.getSelectionEnd() > editor.getSelectionStart()));
        linkButton.setEnabled(link);

        String align = (String) attr
                .getAttribute(javax.swing.text.html.HTML.Attribute.ALIGN);
        if (align == null) align = ""
                + attr.getAttribute(CSS.Attribute.TEXT_ALIGN);
        if (align != null)
        {// left, right, center, justify
            bAlignCenter.setSelectedEx(align.equalsIgnoreCase("center"));
            bAlignLeft.setSelectedEx(align.equalsIgnoreCase("left"));
            bAlignRight.setSelectedEx(align.equalsIgnoreCase("right"));
            bAlignJustify.setSelectedEx(align.equalsIgnoreCase("justify"));
        } else
        {
            bAlignCenter.setSelectedEx(false);
            bAlignLeft.setSelectedEx(false);
            bAlignRight.setSelectedEx(false);
            bAlignJustify.setSelectedEx(false);
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
        cbStyles.setSelectedIndexEx(index);
        bUnorderedList.setSelectedEx(HTMLUtils.isInTag(editor, UL));
        bOrderedList.setSelectedEx(HTMLUtils.isInTag(editor, OL));
        MyHTMLEditorKit htmlKit = new MyHTMLEditorKit();
        htmlKit.getActionByName(MyHTMLEditorKit.listFormatAction).setEnabled(
                bUnorderedList.isSelected() || bOrderedList.isSelected());

        boolean inTable = HTMLUtils.isInTag(editor, TABLE);
        fmtTableButton.setEnabled(inTable);
        insRowButton.setEnabled(inTable);
        insColButton.setEnabled(inTable);

        htmlKit.getActionByName(MyHTMLEditorKit.deleteTableRowAction)
                .setEnabled(inTable);
        htmlKit.getActionByName(MyHTMLEditorKit.deleteTableColAction)
                .setEnabled(inTable);
    }

    public static class MyToggleButton extends JToggleButton implements MouseListener
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

        public MyToggleButton(Action a)
        {
            this(a, (String) a.getValue(Action.SHORT_DESCRIPTION));
        }

        public MyToggleButton(Action a, String tip)
        {
            super(a);
            setToolTipText(tip);
            init();
            addMouseListener(this);
        }

        public MyToggleButton(Action a, String tip, ImageIcon img)
        {
            this(a, tip);
            a.putValue(Action.SMALL_ICON, img);
        }

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
        }

        public MyComboBox(ComboBoxModel aModel)
        {
            super(aModel);
        }

        public MyComboBox(Object[] items)
        {
            super(items);
        }

        public MyComboBox(Vector<?> items)
        {
            super(items);
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

    public static class StylesAction extends AbstractAction
    {

        public void actionPerformed(ActionEvent e)
        {
            if (!isEnabled()) return;
            if(!(e.getSource() instanceof MyComboBox)) return;
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
