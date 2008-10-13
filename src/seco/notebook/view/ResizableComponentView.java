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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;

import seco.notebook.AppForm;
import seco.notebook.NotebookUI;
import seco.notebook.SelectionManager;
import seco.notebook.SelectionManager.Selection;


public class ResizableComponentView extends HidableComponentView implements
        SelectionManager.Selection
{
    private ResizableComponent panel = null;

    public ResizableComponentView(Element element)
    {
        super(element);
    }

    @Override
    public float getAlignment(int axis)
    {
        if (panel != null)
        {
            switch (axis)
            {
            case View.X_AXIS:
                return 0.0f;
            case View.Y_AXIS:
                return 0.0f;
            }
        }
        return super.getAlignment(axis);
    }

    public float getPreferredSpan(int axis)
    {
        float m = super.getPreferredSpan(axis);
        if (axis == X_AXIS)
        {
            float l = getContainer().getWidth() - 20
                    - InputCellView.WHITE_GAP_SPAN;
            if (m > l || m == 0)
                return l;
        }
        return m;
    }

    public float getMinimumSpan(int axis)
    {
        return getPreferredSpan(axis);
    }

    public float getMaximumSpan(int axis)
    {
        return getPreferredSpan(axis);
    }

    protected Component createComponent()
    {
        if (panel != null) return panel;
        AttributeSet attr = getElement().getAttributes();
        Component comp = (Component) attr
                .getAttribute(StyleConstants.ComponentAttribute);
        panel = new ResizableComponent(comp);
        panel.setDoubleBuffered(!AppForm.PICCOLO);
       
        final NotebookUI ui = (NotebookUI) getContainer();
        ui.getSelectionManager().put(getElement(), this);
        ui.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e)
            {
                panel.setSelected(false);
            }
        });
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt)
            {
                // System.out.println("ResizableComponentView - propertyChange:
                // " + evt.getPropertyName());
                if (ResizableComponent.PROP_TO_BE_SELECTED.equals(evt
                        .getPropertyName()))
                    ui.getSelectionManager().clearSelections();
                else if (ResizableComponent.PROP_RESIZED.equals(evt
                        .getPropertyName()))
                {
                    ;// TODO: maybe we could write the new Dimension in
                    // attribs for further saving in a file
                }
            }
        });

        return panel;
    }

    public void setSelected(boolean selected)
    {
        // System.out.println("ResizableComponentView - setSelected: " +
        // selected);
        panel.setSelected(selected);
    }

    public void requestFocus()
    {
        panel.requestFocus();
    }

    @Override
    public void setVisible(boolean visible)
    {
        if (panel != null)
            panel.setVisible(visible);
        super.setVisible(visible);
    }

}
