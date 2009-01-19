/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import static seco.notebook.ElementType.inputCellBox;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SizeRequirements;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.View;
import javax.swing.text.Position.Bias;
import javax.swing.text.html.CSS;

import seco.gui.StandaloneFrame;
import seco.gui.TopFrame;
import seco.notebook.ElementType;
import seco.notebook.GUIHelper;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookEditorKit;
import seco.notebook.NotebookUI;
import seco.notebook.gui.GUIUtilities;
import seco.notebook.html.HTMLEditor;
import seco.notebook.html.HTMLToolBar;
import seco.notebook.html.HTMLUtils;
import seco.notebook.html.MyHTMLEditorKit;
import seco.notebook.html.Util;
import seco.notebook.html.MyHTMLEditorKit.BaseAction;
import seco.notebook.syntax.completion.HTMLDocView;
import seco.things.Cell;
import seco.things.CellUtils;


public class HtmlView extends HidableComponentView
{
    private HTMLEditor view = null;

    public HtmlView(Element element)
    {
        super(element);
    }

    @Override
    public float getAlignment(int axis)
    {
        if (view != null) return 0.0f;
        return super.getAlignment(axis);
    }

    @Override
    public int viewToModel(float x, float y, Shape a, Bias[] biasReturn)
    {
        return getElement().getStartOffset() + 1;
    }

    @Override
    public void setVisible(boolean visible)
    {
        if (view != null) view.setVisible(visible);
        super.setVisible(visible);
    }

    protected Component createComponent()
    {
        if (view == null)
        {
            final NotebookUI ui = (NotebookUI) getContainer();
            String fn = ui.getDoc().getTitle();
            view = new InnerHTMLEditor(fn); // HTMLEditor(fn);
            Cell cell = (Cell) NotebookDocument.getNBElement(getElement());
            view.setContent(CellUtils.getText(cell));
            view.setEditable(!CellUtils.isReadonly(cell));

            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    ui.getDoc().addAttribute(getElement(),
                            NotebookDocument.ATTR_HTML_EDITOR, view);
                    view.getDoc().addUndoableEditListener(
                            new UndoableEditListener() {
                                public void undoableEditHappened(
                                        UndoableEditEvent e)
                                {
                                    ((NotebookDocument) getElement()
                                            .getDocument()).setModified(true);
                                }
                            });
                }
            });
        }
        return view;
    }

    public class InnerHTMLEditor extends HTMLEditor
    {
        public InnerHTMLEditor(String filename)
        {
            super(filename);
            addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e)
                {
                    NotebookUI ui = getNotebookUI();
                    if (ui == null) return;
                    ui.getSelectionManager().clearSelections();
                    // .setCaretPosition(getElement().getStartOffset());
                    TopFrame.getInstance().showHTMLToolBar(true);
                    editor = InnerHTMLEditor.this;
                }

                public void focusLost(FocusEvent e)
                {
                    TopFrame.getInstance().showHTMLToolBar(false);
                }
            });

            addCaretListener(new CaretListener() {
                public void caretUpdate(CaretEvent e)
                {
                    GUIHelper.getHTMLToolBar().showAttributes(
                            InnerHTMLEditor.this, getCaretPosition());
                }
            });

            setNavigationFilter(new CustomNavigationFilter());
        }

        protected PopupListener getPopupListener()
        {
            if (popupListener == null) popupListener = new MyPopupListener();
            return popupListener;
        }

        public Element getElement()
        {
            return HtmlView.this.getElement();
        }

        public NotebookUI getNotebookUI()
        {
            return (NotebookUI) HtmlView.this.getContainer();
        }

        protected class MyPopupListener extends PopupListener
        {
            public void mouseClicked(MouseEvent e)
            {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e))
                {
                    NotebookUI ui = ((InnerHTMLEditor) e.getComponent())
                            .getNotebookUI();
                    if (popupMenu.isVisible())
                    {
                        popupMenu.setVisible(false);
                    } else
                    {
                        int off = ui.viewToModel(e.getPoint());
                        if (off != -1) ui.setCaretPosition(off);
                        popupMenu.update();
                        Frame f = GUIUtilities.getFrame(e.getComponent());
                        Point pt = SwingUtilities.convertPoint(
                                e.getComponent(), e.getX(), e.getY(), f);
                        popupMenu.show(f, pt.x, pt.y);
                    }
                }
            }
        }

        public class CustomNavigationFilter extends NavigationFilter
        {
            private int lastDot;
            private int realDirection;

            public void setDot(NavigationFilter.FilterBypass fb, int dot,
                    Position.Bias bias)
            {
                // special case when caret is on the last line, but not on
                // endpos and down arrow is pressed
                boolean spec_down = realDirection == SwingConstants.SOUTH
                        && lastDot == dot;
                // System.out.println("InnerHTMLNavigationFilter-setDot: " + dot
                // + ":" + lastDot
                // + ":" + getDocument().getLength() + ":" + fb + ":" + bias +
                // ":" + realDirection);

                InnerHTMLEditor ed = InnerHTMLEditor.this;
                if ((dot == ed.getDocument().getLength() && lastDot == dot)
                        || (dot == 1 && bias == Position.Bias.Backward)
                        || spec_down)
                {
                    boolean up = bias == Position.Bias.Backward;
                    if (spec_down) up = false;
                    int off = (up) ? ed.getElement().getStartOffset() : ed
                            .getElement().getEndOffset();
                    final NotebookUI ui = (NotebookUI) getContainer();
                    ui.setCaretPosition(off + ((up) ? -1 : 1));
                    ui.requestFocus();
                    lastDot = -1;
                    realDirection = 0;
                    return;

                }

                lastDot = dot;
                fb.setDot(dot, bias);
            }

            public int getNextVisualPositionFrom(JTextComponent text, int pos,
                    Position.Bias bias, int direction, Position.Bias[] biasRet)
                    throws BadLocationException
            {
                Position.Bias realBias = (direction == SwingConstants.NORTH || direction == SwingConstants.WEST) ? Position.Bias.Backward
                        : Position.Bias.Forward;
                realDirection = direction;
                biasRet[0] = realBias;
                return super.getNextVisualPositionFrom(text, pos, realBias,
                        direction, biasRet);
            }
        }
    }

}
