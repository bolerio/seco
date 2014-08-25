package seco.gui.visual;

import javax.swing.JComponent;
import javax.swing.JLabel;

import seco.gui.GUIHelper;
import seco.things.Cell;
import seco.things.CellGroupMember;
import seco.things.CellUtils;
import seco.things.CellVisual;
import seco.things.NotSerializableValue;

public class DelayedVisual implements CellVisual
{

    @Override
    public JComponent bind(CellGroupMember element)
    {
        if(CellUtils.isMinimized(element))
            return GUIHelper.getMinimizedUI(element);
        
        Cell cell = (Cell) element;
        
        NotSerializableValue notser = (NotSerializableValue)cell.getValue();
        if (notser.initialized())
            return CellUtils.getVisual(element).bind(element);
        else
        	return new JLabel("Not available, please evaluate source cell.");
    }
}