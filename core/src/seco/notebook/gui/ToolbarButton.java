/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.gui;

import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import seco.gui.GUIHelper;
import seco.gui.TopFrame;

public class ToolbarButton extends JButton implements MouseListener
{
    protected Border m_raised = new SoftBevelBorder(BevelBorder.RAISED);
    protected Border m_lowered = new SoftBevelBorder(BevelBorder.LOWERED);
    protected Border m_inactive = new EmptyBorder(3, 3, 3, 3);
    protected Border m_border = m_inactive;
    protected Insets m_ins = new Insets(4, 4, 4, 4);

    public ToolbarButton()
    {

    }

    public ToolbarButton(Action act, String tip)
    {
        super((Icon) act.getValue(Action.SMALL_ICON));
        setBorder(m_inactive);
        setMargin(m_ins);
        setToolTipText(tip);
        setRequestFocusEnabled(false);
        setAction(act);
        setText("");
        addMouseListener(this);
    }

    public float getAlignmentY()
    {
        return 0.5f;
    }

    // Overridden for 1.4 bug fix
    public Border getBorder()
    {
        return m_border;
    }

    // Overridden for 1.4 bug fix
    public Insets getInsets()
    {
        return m_ins;
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

    public void setEnabled(boolean b)
    {
        super.setEnabled(b);
        if (getAction() != null)
        {
            getAction().setEnabled(b);
        }
    }

    @Override
    public void setText(String text)
    {
        super.setText("");
    }

    @Override
    public Point getToolTipLocation(MouseEvent e)
    {
        return (TopFrame.PICCOLO) ? GUIHelper.adjustPointInPicollo(this, e.getPoint())
                : super.getToolTipLocation(e);
    }

}
