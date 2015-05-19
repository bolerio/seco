/*
 * @(#)QuaquaLionScrollBarThumbBorder.java  1.0  2011-08-05
 * 
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.lion;

import ch.randelshofer.quaqua.QuaquaUtilities;
import ch.randelshofer.quaqua.color.PaintableColor;
import ch.randelshofer.quaqua.ext.batik.ext.awt.LinearGradientPaint;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;

/**
 * {@code QuaquaLionScrollBarThumbBorder}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-08-05 Created.
 */
public class QuaquaLionScrollBarTrackBorder implements Border, UIResource {

    private final static Color rrectColor = new Color(0xdbdbdbdb, true);
    private final static Color[] rrectGradientColors = {
        new Color(0xdbe3e3e3, true),
        new Color(0xdbe7e7e7, true),
        new Color(0xdbeaeaea, true),};
    private final static float[] rrectGradientFractions = {
        0f, 0.5f, 1f,};
    private final static Color topColor = new Color(0xe4e4e4);
    private final static Color bottomColor = new Color(0xefefef);
    private final static Color[] gradientColors = {
        new Color(0xf2f2f2),
        new Color(0xfcfcfc),
        new Color(0xf8f8f8),};
    private final static float[] gradientFractions = {
        0f, 0.5f, 1f,};

    public void paintBorder(Component c, Graphics gr, int x, int y, int width, int height) {
        JScrollBar sb = (JScrollBar) c;
        Container parent = sb.getParent();
        JScrollPane sp = (parent instanceof JScrollPane) ? (JScrollPane) parent : null;
        Dimension ps = sb.getUI().getPreferredSize(sb);
        Graphics2D g = (Graphics2D) gr;
        Object oldHints = QuaquaUtilities.beginGraphics(g);
        
        // Draw filled gradient track if not in scroll pane
         if (sb.getOrientation() == SwingConstants.HORIZONTAL) {
            height = Math.min(ps.height, height);
            g.setColor(topColor);
            g.drawLine(x, y, x + width - 1, y);
            g.setColor(bottomColor);
            g.drawLine(x, y + height - 2, x + width - 1, y + height - 2);
            int tx = x + 1;
            int ty = y + 1;
            int tw = width - 2;
            int th = height - 2;
            g.setPaint(new LinearGradientPaint(tx, ty, tx, ty + th - 1, gradientFractions, gradientColors));
            g.fillRect(tx, ty, tw, th);
        } else {
            width = Math.min(ps.width, width);
            g.setColor(topColor);
            g.drawLine(x, y, x, y + height - 1);
            g.setColor(bottomColor);
            g.drawLine(x + width - 2, y, x + width - 2, y + height - 1);
            int tx = x + 1;
            int ty = y + 1;
            int tw = width - 2;
            int th = height - 2;
            g.setPaint(new LinearGradientPaint(tx, ty, tx + width - 1, ty, gradientFractions, gradientColors));
            g.fillRect(tx, ty, tw, th);
        }

        // Draw round rect track
        if (false &&sp != null) {
            if (sb.getValueIsAdjusting()) {
                if (sb.getOrientation() == SwingConstants.HORIZONTAL) {
                    height = Math.min(ps.height, height);
                    int tx = x + 2;
                    int ty = y + 2;
                    int tw = width - 4;
                    int th = height - 4;
                    g.setPaint(new LinearGradientPaint(tx, ty, tx, ty + th - 1, rrectGradientFractions, rrectGradientColors));
                    g.fillRoundRect(tx, ty, tw, th, th, th);
                    g.setColor(rrectColor);
                    g.drawRoundRect(tx, ty, tw, th - 1, th - 1, th - 1);
                } else {
                    width = Math.min(ps.width, width);
                    int tx = x + 2;
                    int ty = y + 2;
                    int tw = width - 4;
                    int th = height - 4;
                    g.setPaint(new LinearGradientPaint(tx, ty, tx + width - 1, ty, rrectGradientFractions, rrectGradientColors));
                    g.fillRoundRect(tx, ty, tw, th, tw, tw);
                    g.setColor(rrectColor);
                    g.drawRoundRect(tx, ty, tw - 1, th, tw - 1, tw - 1);
                }
            }
        }
if (false && sp != null && sp.getViewport().getView()!=null) {
            Color bg= sp.getViewport().getView().getBackground();
            g.setPaint(PaintableColor.getPaint(bg,sp.getViewport().getView()));
            g.fillRect(x,y,width,height);
        } 
        QuaquaUtilities.endGraphics(g, oldHints);
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(2, 2, 2, 2);
    }

    public boolean isBorderOpaque() {
        return false;
    }
}
