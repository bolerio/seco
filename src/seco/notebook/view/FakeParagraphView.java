/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook.view;

import javax.swing.text.Element;


/**
 */
public class FakeParagraphView extends javax.swing.text.ParagraphView 
{
    public FakeParagraphView(Element elem)
    {
        super(elem);
    }

    public int getResizeWeight(int axis)
    {
        return 0;
    }
   
    /*
     * Terrible hack to prevent infinite loop in FlowView.FlowStrategy.layout
     * */
    @Override
    protected int getViewIndexAtPosition(int pos)
    {
        if(pos < 0) return 0;
        return super.getViewIndexAtPosition(pos);
    }
   
    public boolean isVisible()
    {
        return false;
    }

    public float getPreferredSpan(int axis)
    {
        if (!isVisible()) return 0;
        return super.getPreferredSpan(axis);
    }

    public float getMinimumSpan(int axis)
    {
        if (!isVisible()) return 0;
        return super.getMinimumSpan(axis);
    }

    @Override
    public String toString()
    {
        return "PEX: " + getElement() + ":" + super.toString();
    }

    public float getMaximumSpan(int axis)
    {
        if (!isVisible()) return 0;
        return super.getMaximumSpan(axis);
    }

}

