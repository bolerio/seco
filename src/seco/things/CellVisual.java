package seco.things;

/**
 * 
 * <p>
 * A <code>CellVisual</code> is responsible for creating and initializing
 * a runtime UI component for a given cell or a cell group within a 
 * given parent component.
 * </p>
 * 
 * <p>
 * To be seen on the screen each cell/group must have an associated visual. In general
 * visuals are bound to the type of the atom that the cell contains. Such bindings
 * are represented with the <code>DefaultVisual</code> and <code>AvailableVisual</code>
 * links.
 * </p>
 * 
 * <p>
 * <code>CellVisual</code> are usually singletons since the state needed to properly
 * create a runtime UI component is generally stored in its entirety within the
 * attributes map of the <code>CellGroupMember</code>.
 * </p>
 * 
 * @author Borislav Iordanov
 */
public interface CellVisual
{
    void bind(CellGroupMember element, Object parentVisual);
}
