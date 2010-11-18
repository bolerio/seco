/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.FontMetrics;
import java.text.BreakIterator;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import javax.swing.text.Position.Bias;

import seco.notebook.ElementType;
import seco.notebook.NotebookDocument;
import seco.notebook.style.StyleType;
import seco.notebook.syntax.ScriptSupport;
import seco.util.SegmentCache;

/**
 */
public class InlineView extends LabelView // implements HidableView
{
    private boolean visible = true;
    boolean isOutputCell;
    Element tokenElement;
    int lineIndex = -1;

    /**
     * Constructs a new view wrapped on an element.
     * 
     * @param elem
     *            the element
     */
    public InlineView(Element elem)
    {
        super(elem);
        isOutputCell = NotebookDocument.getUpperElement(elem,
                ElementType.outputCellBox) != null;
        tokenElement = NotebookDocument.getUpperElement(elem,
                ElementType.commonCell);
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public boolean isVisible()
    {
        return visible;
    }

    ScriptSupport getScriptSupport()
    {
        if (tokenElement == null)
            return null;
        return (ScriptSupport) tokenElement.getAttributes().getAttribute(
                NotebookDocument.ATTR_SCRIPT_SUPPORT);
    }

    @Override
    public GlyphPainter getGlyphPainter()
    {
        return getCustomPainter();
    }

    protected void checkPainter()
    {
        if (super.getGlyphPainter() == null)
            setGlyphPainter(getGlyphPainter());
    }

    static GlyphPainter0 customPainter;

    public GlyphPainter getCustomPainter()
    {
        if (customPainter == null)
            customPainter = new GlyphPainter0();
        return customPainter;
    }

    // SegmentCache is package private so we need to use our own
    // to be able to release segments in GlyphPainter0
    public Segment getText(int p0, int p1)
    {
        Segment text = SegmentCache.getSharedSegment();
        //happens occasionally with unclear reason...
        if (p1 >= p0)
            try
            {
                Document doc = getDocument();
                doc.getText(p0, p1 - p0, text);
            }
            catch (BadLocationException bl)
            {
                throw new Error("InlineView: Stale view: " + bl);
            }
        return text;
    }

    public Font getFont()
    {
        return (isOutputCell) ? ((NotebookDocument) getDocument())
                .getOutputCellFont() : ((NotebookDocument) getDocument())
                .getInputCellFont();
    }

    public Color getForeground()
    {
        return (isOutputCell) ? StyleConstants.getForeground(getAttributes())
                : super.getForeground();
    }

    // copied from GlyphView, removing all the ugly selection related stuff
    public void paint(Graphics g, Shape a)
    {
        checkPainter();
        boolean paintedText = false;
        Component c = getContainer();
        int p0 = getStartOffset();
        int p1 = getEndOffset();
        Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a
                .getBounds();
        Color bg = getBackground();
        Color fg = getForeground();
        if (c instanceof JTextComponent)
        {
            JTextComponent tc = (JTextComponent) c;
            if (!tc.isEnabled())
            {
                fg = tc.getDisabledTextColor();
            }
        }
        if (bg != null)
        {
            g.setColor(bg);
            g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
        }
        if (c instanceof JTextComponent)
        {
            JTextComponent tc = (JTextComponent) c;
            Highlighter h = tc.getHighlighter();
            if (h instanceof LayeredHighlighter)
            {
                ((LayeredHighlighter) h).paintLayeredHighlights(g, p0, p1, a,
                        tc, this);
            }
        }
        // if (Utilities.isComposedTextElement(getElement()))
        // {
        // Utilities.paintComposedText(g, a.getBounds(), this);
        // / paintedText = true;
        // } else

        if (!paintedText)
            paintTextUsingColor0(g, a, fg, p0, p1);
    }

    /**
     * Paints the specified region of text in the specified color.
     */
    final void paintTextUsingColor0(Graphics g, Shape a, Color c, int p0, int p1)
    {
        // render the glyphs
        g.setColor(c);
        customPainter.paint(this, g, a, p0, p1);
        // render underline or strikethrough if set.
        boolean underline = isUnderline();
        boolean strike = isStrikeThrough();
        if (underline || strike)
        {
            // calculate x coordinates
            Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a
                    .getBounds();
            View parent = getParent();
            if ((parent != null) && (parent.getEndOffset() == p1))
            {
                // strip whitespace on end
                Segment s = getText(p0, p1);
                while ((s.count > 0)
                        && (Character.isWhitespace(s.array[s.count - 1])))
                {
                    p1 -= 1;
                    s.count -= 1;
                }
                SegmentCache.releaseSharedSegment(s);
            }
            int x0 = alloc.x;
            int p = getStartOffset();
            if (p != p0)
            {
                x0 += (int) customPainter.getSpan(this, p, p0,
                        getTabExpander(), x0);
            }
            int x1 = x0
                    + (int) customPainter.getSpan(this, p0, p1,
                            getTabExpander(), x0);
            // calculate y coordinate
            // int d = (int) customPainter.getDescent(this);
            int y = alloc.y + alloc.height
                    - (int) customPainter.getDescent(this);
            if (underline)
            {
                int yTmp = y;
                yTmp += 1;
                g.drawLine(x0, yTmp, x1, yTmp);
            }
            if (strike)
            {
                int yTmp = y;
                // move y coordinate above baseline
                yTmp -= (int) (customPainter.getAscent(this) * 0.3f);
                g.drawLine(x0, yTmp, x1, yTmp);
            }
        }
    }

}
