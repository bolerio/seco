/*
 * @(#)QuaquaTreeUI.java  
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua;

import ch.randelshofer.quaqua.color.InactivatableColorUIResource;
import ch.randelshofer.quaqua.color.PaintableColor;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * QuaquaTreeUI.
 *
 * XXX - Without copying a substantial amount of code from BasicTreeUI,
 * we can't implement the proper selection behavior for a JTree.
 *
 * @author  Werner Randelshofer
 * @version $Id: QuaquaTreeUI.java 462 2014-03-22 09:23:12Z wrandelshofer $
 */
public class QuaquaTreeUI extends BasicTreeUI {
    // Old actions forward to an instance of this. ??

    static private final Actions SHARED_ACTION = new Actions();
    static private final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
    /** Last width the tree was at when painted. This is used when
     * !leftToRigth to notice the bounds have changed so that we can instruct
     * the TreeState to relayout. */
    private int lastWidth;
    /**
     * The time factor to treate the series of typed alphanumeric key
     * as prefix for first letter navigation.
     */
    private long timeFactor = 1000L;
    private Handler handler;
    /**
     * A temporary variable for communication between startEditingOnRelease
     * and startEditing.
     */
    private MouseEvent releaseEvent;
    /** If true, the property change event for LEAD_SELECTION_PATH_PROPERTY,
     * or ANCHOR_SELECTION_PATH_PROPERTY will not generate a repaint. */
    private boolean ignoreLAChange;
    /** Row correspondin to lead leadPath. */
    private int leadRow;
    private static DropTargetListener defaultDropTargetListener = null;
    /** This is set to true, if the editor may start editing. */
    private boolean isMouseReleaseStartsEditing;
    private boolean isDragRecognitionOngoing;
    private final static Color TRANSPARENT_COLOR = new Color(0, true);

    /** Creates a new instance. */
    public QuaquaTreeUI() {
    }

    public static ComponentUI createUI(JComponent c) {
        return new QuaquaTreeUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();

        // By default, we commit an edit when it is closed.
        tree.setInvokesStopCellEditing(true);
    }

    @Override
    protected void installKeyboardActions() {
        InputMap km = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        SwingUtilities.replaceUIInputMap(tree, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                km);
        km = getInputMap(JComponent.WHEN_FOCUSED);
        SwingUtilities.replaceUIInputMap(tree, JComponent.WHEN_FOCUSED, km);

        QuaquaLazyActionMap.installLazyActionMap(tree, QuaquaTreeUI.class,
                "Tree.actionMap");
    }

    static void loadActionMap(QuaquaLazyActionMap map) {
        map.put(new Actions(Actions.SELECT_PREVIOUS));
        map.put(new Actions(Actions.SELECT_PREVIOUS_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_PREVIOUS_EXTEND_SELECTION));

        map.put(new Actions(Actions.SELECT_NEXT));
        map.put(new Actions(Actions.SELECT_NEXT_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_NEXT_EXTEND_SELECTION));

        map.put(new Actions(Actions.SELECT_CHILD));
        map.put(new Actions(Actions.SELECT_CHILD_CHANGE_LEAD));

        map.put(new Actions(Actions.SELECT_PARENT));
        map.put(new Actions(Actions.SELECT_PARENT_CHANGE_LEAD));

        map.put(new Actions(Actions.SCROLL_UP_CHANGE_SELECTION));
        map.put(new Actions(Actions.SCROLL_UP_CHANGE_LEAD));
        map.put(new Actions(Actions.SCROLL_UP_EXTEND_SELECTION));

        map.put(new Actions(Actions.SCROLL_DOWN_CHANGE_SELECTION));
        map.put(new Actions(Actions.SCROLL_DOWN_EXTEND_SELECTION));
        map.put(new Actions(Actions.SCROLL_DOWN_CHANGE_LEAD));

        map.put(new Actions(Actions.SELECT_FIRST));
        map.put(new Actions(Actions.SELECT_FIRST_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_FIRST_EXTEND_SELECTION));

        map.put(new Actions(Actions.SELECT_LAST));
        map.put(new Actions(Actions.SELECT_LAST_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_LAST_EXTEND_SELECTION));

        map.put(new Actions(Actions.TOGGLE));

        map.put(new Actions(Actions.CANCEL_EDITING));

        map.put(new Actions(Actions.START_EDITING));

        map.put(new Actions(Actions.SELECT_ALL));

        map.put(new Actions(Actions.CLEAR_SELECTION));

        map.put(new Actions(Actions.SCROLL_LEFT));
        map.put(new Actions(Actions.SCROLL_RIGHT));

        map.put(new Actions(Actions.SCROLL_LEFT_EXTEND_SELECTION));
        map.put(new Actions(Actions.SCROLL_RIGHT_EXTEND_SELECTION));

        map.put(new Actions(Actions.SCROLL_RIGHT_CHANGE_LEAD));
        map.put(new Actions(Actions.SCROLL_LEFT_CHANGE_LEAD));

        map.put(new Actions(Actions.EXPAND));
        map.put(new Actions(Actions.COLLAPSE));
        map.put(new Actions(Actions.MOVE_SELECTION_TO_PARENT));

        map.put(new Actions(Actions.ADD_TO_SELECTION));
        map.put(new Actions(Actions.TOGGLE_AND_ANCHOR));
        map.put(new Actions(Actions.EXTEND_TO));
        map.put(new Actions(Actions.MOVE_SELECTION_TO));

        map.put(TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction());
    }

    /**
     * Invoked after the <code>tree</code> instance variable has been
     * set, but before any defaults/listeners have been installed.
     */
    @Override
    protected void prepareForUIInstall() {
        super.prepareForUIInstall();
        leadRow = -1;
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();

        handler = null;
    }

    private static class QuaquaTreeCellEditor extends DefaultTreeCellEditor implements UIResource {

        public QuaquaTreeCellEditor(JTree tree,
                DefaultTreeCellRenderer renderer) {
            super(tree, renderer);
        }
        // FIXME - We should explicitly turn the real editing component
        // opaque.

        @Override
        protected Container createContainer() {
            return new DefaultTreeCellEditor.EditorContainer() {

                @Override
                public void paint(Graphics gr) {
                    Graphics2D g = (Graphics2D) gr;
                    g.setColor(UIManager.getColor("TextField.background"));
                    Component[] c = getComponents();
                    for (int i = 0; i < c.length; i++) {
                        g.fill(c[i].getBounds());
                    }
                    super.paint(g);
                }
            };
        }
    }

    private static class QuaquaTreeCellRenderer extends DefaultTreeCellRenderer implements UIResource {

        public QuaquaTreeCellRenderer() {
            setBorder(null);
        }
    }

    /**
     * Creates a default cell editor.
     */
    @Override
    protected TreeCellEditor createDefaultCellEditor() {
        if (currentCellRenderer != null
                && (currentCellRenderer instanceof DefaultTreeCellRenderer)) {
            DefaultTreeCellEditor editor = new QuaquaTreeCellEditor(
                    tree, (DefaultTreeCellRenderer) currentCellRenderer);
            return editor;
        }
        return new DefaultTreeCellEditor(tree, null);
    }

    /**
     * Returns the default cell renderer that is used to do the
     * stamping of each node.
     */
    @Override
    protected TreeCellRenderer createDefaultCellRenderer() {
        return new QuaquaTreeCellRenderer();
    }

    /**
     * Creates the listener reponsible for getting key events from
     * the tree.
     */
    @Override
    protected KeyListener createKeyListener() {
        return getHandler();
    }

    /**
     * Creates the listener responsible for getting property change
     * events from the selection model.
     */
    @Override
    protected PropertyChangeListener createSelectionModelPropertyChangeListener() {
        return getHandler();
    }

    /**
     * Creates the listener that updates the display based on selection change
     * methods.
     */
    @Override
    protected TreeSelectionListener createTreeSelectionListener() {
        return getHandler();
    }

    /**
     * Creates a listener to handle events from the current editor.
     */
    @Override
    protected CellEditorListener createCellEditorListener() {
        return getHandler();
    }

    /**
     * Creates and returns the object responsible for updating the treestate
     * when nodes expanded state changes.
     */
    @Override
    protected TreeExpansionListener createTreeExpansionListener() {
        return getHandler();
    }

    /**
     * Creates a listener that is responsible that updates the UI based on
     * how the tree changes.
     */
    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    /**
     * Returns a listener that can update the tree when the model changes.
     */
    protected TreeModelListener createTreeModelListener() {
        return getHandler();
    }

    /**
     * Returning true signifies a mouse event on the node should toggle
     * the selection of only the row under mouse.
     */
    @Override
    protected boolean isToggleSelectionEvent(MouseEvent event) {
        return event.getID() == MouseEvent.MOUSE_PRESSED
                && SwingUtilities.isLeftMouseButton(event)
                && event.isMetaDown();
    }

    @Override
    protected boolean isToggleEvent(MouseEvent event) {
        if (event.getID() != MouseEvent.MOUSE_PRESSED
                || !SwingUtilities.isLeftMouseButton(event)) {
            return false;
        }
        int clickCount = tree.getToggleClickCount();

        if (clickCount <= 0) {
            return false;
        }
        return ((event.getClickCount() % clickCount) == 0);
    }

    /**
     * Returning true signifies a mouse event on the node should select
     * from the anchor point.
     */
    @Override
    protected boolean isMultiSelectEvent(MouseEvent event) {
        return (SwingUtilities.isLeftMouseButton(event)
                && event.isShiftDown());
    }

    private TreePath getMouseClickedClosestPathForLocation(JTree tree, int x, int y) {
        final TreePath path = getClosestPathForLocation(tree, x, y);
        if (path == null) {
            return null;
        }

        final Rectangle pathBounds = getPathBounds(tree, path);
        if (y > pathBounds.y + pathBounds.height) {
            return null;
        }

        return path;
    }

    //
    // The following selection methods (lead/anchor) are covers for the
    // methods in JTree.
    //
    private void setAnchorSelectionPath(TreePath newPath) {
        ignoreLAChange = true;
        try {
            tree.setAnchorSelectionPath(newPath);
        } finally {
            ignoreLAChange = false;
        }
    }

    private TreePath getAnchorSelectionPath() {
        return tree.getAnchorSelectionPath();
    }

    private void setLeadSelectionPath(TreePath newPath, boolean repaint) {
        Rectangle bounds = repaint ? getPathBounds(tree, getLeadSelectionPath()) : null;

        ignoreLAChange = true;
        try {
            tree.setLeadSelectionPath(newPath);
        } finally {
            ignoreLAChange = false;
        }
        leadRow = getRowForPath(tree, newPath);

        if (repaint) {
            if (bounds != null) {
                tree.repaint(bounds);
            }
            bounds = getPathBounds(tree, newPath);
            if (bounds != null) {
                tree.repaint(bounds);
            }
        }
    }

    private TreePath getLeadSelectionPath() {
        return tree.getLeadSelectionPath();
    }

    private void setLeadSelectionPath(TreePath newPath) {
        setLeadSelectionPath(newPath, false);
    }

    private void updateLeadRow() {
        leadRow = getRowForPath(tree, getLeadSelectionPath());
    }

    private void updateSize0() {
        validCachedPreferredSize = false;
        tree.revalidate();
    }

    private int getLeadSelectionRow() {
        return tree.getLeadSelectionRow();
//        return leadRow;
    }

    /**
     * Determines whether the node handles are to be displayed.
     * Regardless of what value you specify here, the Quaqua look and feel
     * always shows the root handles.
     */
    @Override
    protected void setShowsRootHandles(boolean newValue) {
        super.setShowsRootHandles(true);
    }

    @Override
    protected boolean getShowsRootHandles() {
        return true;
    }
    /*
    private int getLeadSelectionRow() {
    TreePath leadPath = tree.getLeadSelectionPath();
    return (leadPath == null) ? -1 : getRowForPath(tree, leadPath);
    }*/

    /**
     * Invokes <code>repaint</code> on the JTree for the passed in TreePath,
     * <code>leadPath</code>.
     */
    private void repaintPath(TreePath path) {
        if (path != null) {
            Rectangle bounds = getPathBounds(tree, path);
            if (bounds != null) {
                tree.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
    }

    /**
     * Paints the expand (toggle) part of a row. The receiver should
     * NOT modify <code>clipBounds</code>, or <code>insets</code>.
     */
    @Override
    protected void paintExpandControl(Graphics g,
            Rectangle clipBounds, Insets insets,
            Rectangle bounds, TreePath path,
            int row, boolean isExpanded,
            boolean hasBeenExpanded,
            boolean isLeaf) {
        Object value = path.getLastPathComponent();

        // Draw icons if not a leaf and either hasn't been loaded,
        // or the model child count is > 0.
        if (!isLeaf && (!hasBeenExpanded
                || treeModel.getChildCount(value) > 0)) {
            int middleXOfKnob;
            if (QuaquaUtilities.isLeftToRight(tree)) {
                middleXOfKnob = bounds.x - (getRightChildIndent() - 1);
            } else {
                middleXOfKnob = bounds.x + bounds.width + getRightChildIndent();
            }
            int middleYOfKnob = bounds.y + (bounds.height / 2);

            Icon treeIcon = getTreeIcon(isExpanded, tree.isRowSelected(row));
            if (treeIcon != null) {
                drawCentered(tree, g, treeIcon, middleXOfKnob,
                        middleYOfKnob);
            }
        }
    }

    private Icon getTreeIcon(boolean isExpanded, boolean isSelected) {
        boolean isSideBar = isSideBar();

        Icon[] icons = (Icon[]) UIManager.get(isSideBar ? "Tree.sideBar.icons" : "Tree.icons");
        if (icons == null) {
            return UIManager.getIcon((isExpanded) ? "Tree.expandedIcon" : "Tree.collapsedIcon");
        } else {

            int index = (isExpanded) ? 6 : 0;
            if (isSelected) {
                if (QuaquaUtilities.isFocused(tree)) {
                    index += 2;
                } else if (QuaquaUtilities.isOnActiveWindow(tree)) {
                    index++;
                } else {
                    index += 4;
                }
            } else {
                if (!tree.isEnabled()) {
                    index += 5;
                } else if (!QuaquaUtilities.isOnActiveWindow(tree)) {
                    index += 3;
                }
            }

            /*
            if (!isSideBar && !QuaquaUtilities.isOnActiveWindow(tree)) {
            index += 2;
            } else {
            if (isSelected && QuaquaUtilities.isFocused(tree)) {
            index++;
            } else if (!tree.isEnabled()) {
            index += 2;
            }
            }*/
            return icons[index];
        }
    }

    /**
     * Returns the location, along the x-axis, to render a particular row
     * at. The return value does not include any Insets specified on the JTree.
     * This does not check for the validity of the row or depth, it is assumed
     * to be correct and will not throw an Exception if the row or depth
     * doesn't match that of the tree.
     *
     * @param row Row to return x location for
     * @param depth Depth of the row
     * @return amount to indent the given row.
     * @since 1.5
     */
    @Override
    protected int getRowX(int row, int depth) {
        boolean isSideBar = isSideBar();

        if (isSideBar) {
            return totalChildIndent * (Math.max(1, depth - (tree.isRootVisible()?2:1)) + depthOffset);
        } else {
            return totalChildIndent * (depth + depthOffset);
        }
    }

    // cover method for startEditing that allows us to pass extra
    // information into that method via a class variable
    private boolean startEditingOnRelease(TreePath path,
            MouseEvent event,
            MouseEvent releaseEvent) {
        this.releaseEvent = releaseEvent;
        try {
            if (isMouseReleaseStartsEditing) {
                return startEditing(path, event);
            } else {
                return false;
            }
        } finally {
            this.releaseEvent = null;
        }
    }

    InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            return (InputMap) UIManager.get("Tree.ancestorInputMap");
        } else if (condition == JComponent.WHEN_FOCUSED) {
            InputMap keyMap = (InputMap) UIManager.get("Tree.focusInputMap");
            InputMap rtlKeyMap;

            if (tree.getComponentOrientation().isLeftToRight()
                    || ((rtlKeyMap = (InputMap) UIManager.get(
                    "Tree.focusInputMap.RightToLeft")) == null)) {
                return keyMap;
            } else {
                rtlKeyMap.setParent(keyMap);
                return rtlKeyMap;
            }
        }
        return null;
    }

    /**
     * Creates the focus listener for handling keyboard navigation in the JTable.
     */
    @Override
    protected FocusListener createFocusListener() {
        return new FocusHandler();
    }

    @Override
    protected MouseListener createMouseListener() {
        return getHandler();
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTableUI.
     */
    public class FocusHandler extends BasicTreeUI.FocusHandler {

        @Override
        public void focusGained(FocusEvent event) {
            if (tree != null) {
                Rectangle pBounds = null;

                TreePath[] selectionPaths = tree.getSelectionPaths();
                if (selectionPaths != null) {
                    for (int i = 0; i < selectionPaths.length; i++) {
                        if (i == 0) {
                            pBounds = getPathBounds(tree, selectionPaths[i]);
                        } else {
                            pBounds.add(getPathBounds(tree, selectionPaths[i]));
                        }
                    }
                    if (pBounds != null) {
                        tree.repaint(0, pBounds.y, tree.getWidth(), pBounds.height);
                    }
                }
            }
        }

        @Override
        public void focusLost(FocusEvent event) {
            focusGained(event);
        }
    }
//
// Painting routines.
//

    @Override
    public void paint(Graphics gr, JComponent c) {
        if (tree != c) {
            throw new InternalError("incorrect component");
        }

        Graphics2D g = (Graphics2D) gr;
        Object property = tree.getClientProperty("Quaqua.Tree.style");
        boolean isStriped = property != null && property.equals("striped");
        boolean isSideBar = isSideBar();
        Color[] stripes = {UIManager.getColor("Tree.alternateBackground.0"), UIManager.getColor("Tree.alternateBackground.1")};
        boolean isEnabled = c.isEnabled();
        boolean isFocused = QuaquaUtilities.isFocused(c);
        boolean isActive = QuaquaUtilities.isOnActiveWindow(c);
        Color selectionBackground = UIManager.getColor("Tree.selectionBackground");
        Color selectionForeground = UIManager.getColor("Tree.selectionForeground");
        if (selectionBackground instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) selectionBackground).setActive(isFocused && isEnabled);
        }

        if (selectionForeground instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) selectionForeground).setActive(isFocused && isEnabled);
        }

// Should never happen if installed for a UI
        if (treeState == null) {
            return;
        }

        boolean leftToRight = QuaquaUtilities.isLeftToRight(tree);
        // Update the lastWidth if necessary.
        // This should really come from a ComponentListener installed on
        // the JTree, but for the time being it is here.
        int width = tree.getWidth();
        int height = tree.getHeight();

        if (width != lastWidth) {
            lastWidth = width;
            if (!leftToRight) {
                // For RTL when the size changes, we have to refresh the
                // cache as the X position is based off the width.
                redoTheLayout();
                updateSize();
            }

        }

        Rectangle paintBounds = g.getClipBounds();
        Insets insets = tree.getInsets();

        if (insets == null) {
            insets = EMPTY_INSETS;
        }

        TreePath initialPath = getClosestPathForLocation(tree, 0, paintBounds.y);
        Enumeration paintingEnumerator = treeState.getVisiblePathsFrom(initialPath);
        int row = treeState.getRowForPath(initialPath);
        int endY = paintBounds.y + paintBounds.height;

        drawingCache.clear();

        Color background;

        Border selectionBorder;

        if (isSideBar) {
            background = UIManager.getColor("Tree.sideBar.background");
            selectionBorder =
                    UIManager.getBorder("Tree.sideBar.selectionBorder");
            if (selectionBackground instanceof InactivatableColorUIResource) {
                ((InactivatableColorUIResource) selectionBackground).setTransparent(true);
            }
           
        } else {
            background = tree.getBackground();
            selectionBorder = null;
        }

        if (tree.isOpaque()) {
            if (background instanceof InactivatableColorUIResource) {
                if (isSideBar) {
                    ((InactivatableColorUIResource) background).setActive(QuaquaUtilities.isOnActiveWindow(c, true));
                } else {
                    ((InactivatableColorUIResource) background).setActive(isActive);
                }
            }
            g.setPaint(PaintableColor.getPaint(background,tree,0,0,width,height));
            g.fillRect(0, 0, width, height);
        }

        if (initialPath != null && paintingEnumerator != null) {
            TreePath parentPath = initialPath;
            boolean hasBeenExpanded;
            boolean done = false;
            boolean isLeaf;
            boolean isExpanded;
            TreePath path;

            Rectangle bounds = null;
            Rectangle boundsBuffer = new Rectangle();
            boolean rootVisible = isRootVisible();
            int rwidth = width - insets.right - insets.left;

            // Draw the alternating row colors
            //*****
            if (isStriped) {
                while (!done && paintingEnumerator.hasMoreElements()) {
                    path = (TreePath) paintingEnumerator.nextElement();
                    if (path != null) {
                        isLeaf = treeModel.isLeaf(path.getLastPathComponent());
                        if (isLeaf) {
                            isExpanded = hasBeenExpanded = false;
                        } else {
                            isExpanded = treeState.getExpandedState(path);
                            hasBeenExpanded =
                                    tree.hasBeenExpanded(path);
                        }

                        bounds = treeState.getBounds(path, boundsBuffer);
                        if (bounds == null) {
                            // This will only happen if the model changes out
                            // from under us (usually in another thread).
                            // Swing isn't multithreaded, but I'll put this
                            // check in anyway.
                            return;
                        }

                        bounds.x += insets.left;
                        bounds.y += insets.top;

                        if (tree.isRowSelected(row) /*&& !tree.isEditing()*/) {
                            if (selectionBorder == null) {
                                g.setColor(selectionBackground);
                                g.fillRect(insets.left, bounds.y, rwidth, bounds.height);
                            } else {
                                selectionBorder.paintBorder(tree, g, insets.left, bounds.y, rwidth, bounds.height);
                            }

                        } else {
                            g.setColor(stripes[row % 2]);
                            g.fillRect(insets.left, bounds.y, rwidth, bounds.height);
                        }

                        if ((bounds.y + bounds.height) >= endY) {
                            done = true;
                        }

                    } else {
                        done = true;
                    }

                    row++;
                }

                int rheight = tree.getRowHeight();
                if (rheight <= 0) {
                    // FIXME - Use the cell renderer to determine the height
                    rheight = tree.getFont().getSize() + 4;
                }

                int startY = (bounds != null) ? bounds.y + bounds.height : 0;

                for (int y = startY; y < height; y += rheight) {
                    g.setColor(stripes[row % 2]);
                    g.fillRect(insets.left, y, rwidth, rheight);
                    row++;

                }


            } else {
                g.setColor(selectionBackground);
                while (!done && paintingEnumerator.hasMoreElements()) {
                    path = (TreePath) paintingEnumerator.nextElement();
                    if (path != null) {
                        isLeaf = treeModel.isLeaf(path.getLastPathComponent());
                        if (isLeaf) {
                            isExpanded = hasBeenExpanded = false;
                        } else {
                            isExpanded = treeState.getExpandedState(path);
                            hasBeenExpanded =
                                    tree.hasBeenExpanded(path);
                        }

                        bounds = treeState.getBounds(path, boundsBuffer);
                        if (bounds == null) {
                            // This will only happen if the model changes out
                            // from under us (usually in another thread).
                            // Swing isn't multithreaded, but I'll put this
                            // check in anyway.
                            return;
                        }

                        bounds.x += insets.left;
                        bounds.y += insets.top;

                        if (tree.isRowSelected(row) /*&& !tree.isEditing()*/) {
                            if (selectionBorder == null) {
                                g.fillRect(insets.left, bounds.y, rwidth, bounds.height);
                            } else {
                                selectionBorder.paintBorder(tree, g, insets.left, bounds.y, rwidth, bounds.height);
                            }

                        }
                        if ((bounds.y + bounds.height) >= endY) {
                            done = true;
                        }

                    } else {
                        done = true;
                    }

                    row++;
                }

            }
            //********
            paintingEnumerator = treeState.getVisiblePathsFrom(initialPath);
            row =
                    treeState.getRowForPath(initialPath);
            // Draw the lines, knobs, and rows
            // Find each parent and have them draw a line to their last child
            parentPath =
                    parentPath.getParentPath();
            while (parentPath != null) {
                paintVerticalPartOfLeg(g, paintBounds, insets, parentPath);
                drawingCache.put(parentPath, Boolean.TRUE);
                parentPath =
                        parentPath.getParentPath();
            }

// Information for the node being rendered.
            done = false;

            while (!done && paintingEnumerator.hasMoreElements()) {
                path = (TreePath) paintingEnumerator.nextElement();
                if (path != null) {
                    isLeaf = treeModel.isLeaf(path.getLastPathComponent());
                    if (isLeaf) {
                        isExpanded = hasBeenExpanded = false;
                    } else {
                        isExpanded = treeState.getExpandedState(path);
                        hasBeenExpanded =
                                tree.hasBeenExpanded(path);
                    }

                    bounds = treeState.getBounds(path, boundsBuffer);
                    if (bounds == null) // This will only happen if the model changes out
                    // from under us (usually in another thread).
                    // Swing isn't multithreaded, but I'll put this
                    // check in anyway.
                    {
                        return;
                    }

                    bounds.x += insets.left;
                    bounds.y += insets.top;

                    // See if the vertical line to the parent has been drawn.
                    parentPath =
                            path.getParentPath();
                    if (parentPath != null) {
                        if (drawingCache.get(parentPath) == null) {
                            paintVerticalPartOfLeg(g, paintBounds,
                                    insets, parentPath);
                            drawingCache.put(parentPath, Boolean.TRUE);
                        }

                        paintHorizontalPartOfLeg(g, paintBounds, insets,
                                bounds, path, row,
                                isExpanded,
                                hasBeenExpanded, isLeaf);
                    } else if (rootVisible && row == 0) {
                        paintHorizontalPartOfLeg(g, paintBounds, insets,
                                bounds, path, row,
                                isExpanded,
                                hasBeenExpanded, isLeaf);
                    }

                    if (shouldPaintExpandControl(path, row, isExpanded,
                            hasBeenExpanded, isLeaf)) {
                        paintExpandControl(g, paintBounds, insets, bounds,
                                path, row, isExpanded,
                                hasBeenExpanded, isLeaf);
                    }
//This is the quick fix for bug 4259260.  Somewhere we
//are out by 4 pixels in the RTL layout.  Its probably
//due to built in right-side padding in some icons.  Rather
//than ferret out problem at the source, this compensates.
                    if (!leftToRight) {
                        bounds.x += 4;
                    }
                    paintRow(g, paintBounds, insets, bounds, path,
                            row, isExpanded, hasBeenExpanded, isLeaf, isEnabled, isFocused, isActive);
                    if ((bounds.y + bounds.height) >= endY) {
                        done = true;
                    }

                } else {
                    done = true;
                }

                row++;
            }

        } else {
            if (isStriped) {
                // Draw stripes on empty tree
                int rwidth = width - insets.left - insets.left;
                int rheight = tree.getRowHeight();
                if (rheight <= 0) {
                    // FIXME - Use the cell renderer to determine the height
                    rheight = tree.getFont().getSize() + 4;
                }

                row = 0;
                for (int y = 0; y < height; y += rheight) {
                    g.setColor(stripes[row % 2]);
                    g.fillRect(insets.left, y, rwidth, rheight);
                    row++;

                }


            }
        }

        // Empty out the renderer pane, allowing renderers to be gc'ed.
        rendererPane.removeAll();
        if (selectionBackground instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) selectionBackground).setActive(true);
            ((InactivatableColorUIResource) selectionBackground).setTransparent(false);
        }

        if (selectionForeground instanceof InactivatableColorUIResource) {
            ((InactivatableColorUIResource) selectionForeground).setActive(true);
        }

    }

    /**
     * Recomputes the right margin, and invalidates any tree states
     */
    private void redoTheLayout() {
        if (treeState != null) {
            treeState.invalidateSizes();
        }

    }

    /**
     * Paints the vertical part of the leg. The receiver should
     * NOT modify <code>clipBounds</code>, <code>insets</code>.<p>
     */
    @Override
    protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds,
            Insets insets, TreePath path) {
        /* Never draw lines
        if (QuaquaManager.getBoolean("Tree.paintLines")) {
        super.paintVerticalPartOfLeg(g, clipBounds, insets, leadPath);
        }
         */
    }

    /**
     * Paints the horizontal part of the leg. The receiver should
     * NOT modify <code>clipBounds</code>, or <code>insets</code>.<p>
     * NOTE: <code>parentRow</code> can be -1 if the root is not visible.
     */
    @Override
    protected void paintHorizontalPartOfLeg(Graphics g, Rectangle clipBounds,
            Insets insets, Rectangle bounds,
            TreePath path, int row,
            boolean isExpanded,
            boolean hasBeenExpanded, boolean isLeaf) {
        /* Never draw lines
        if (QuaquaManager.getBoolean("Tree.paintLines")) {
        super.paintHorizontalPartOfLeg(g, clipBounds, insets, bounds, leadPath, row, isExpanded, hasBeenExpanded, isLeaf);
        }
         */
    }

    /**
     * Paints a vertical line.
     */
    @Override
    protected void paintVerticalLine(Graphics g, JComponent c, int x, int top,
            int bottom) {
        /*
        if (QuaquaManager.getBoolean("Tree.paintLines")) {
        super.paintVerticalLine(g, c, x, top, bottom);
        }*/
    }

    /**
     * Paints a horizontal line.
     */
    @Override
    protected void paintHorizontalLine(Graphics g, JComponent c, int y,
            int left, int right) {
        /*
        if (QuaquaManager.getBoolean("Tree.paintLines")) {
        super.paintHorizontalLine(g, c, y, left, right);
        }*/
    }

    /**
     * Paints the renderer part of a row. The receiver should
     * NOT modify <code>clipBounds</code>, or <code>insets</code>.
     */
    protected void paintRow(Graphics g, Rectangle clipBounds,
            Insets insets, Rectangle bounds, TreePath path,
            int row, boolean isExpanded,
            boolean hasBeenExpanded, boolean isLeaf, boolean isEnabled, boolean isFocused, boolean isActive) {
        // Don't paint the renderer if editing this row.
        if (editingComponent != null && editingRow == row) {
            return;
        }

        int leadIndex;

        if (tree.hasFocus()) {
            leadIndex = getLeadSelectionRow();
        } else {
            leadIndex = -1;
        }

        Component component;

        boolean isRowSelected = tree.isRowSelected(row);

        component =
                currentCellRenderer.getTreeCellRendererComponent(tree, path.getLastPathComponent(),
                isRowSelected, isExpanded, isLeaf, row,
                (leadIndex == row));

        // CHANGE Set appropriate client property when component is a JLabel
        boolean isSideBar = isSideBar();
        if (isSideBar && component instanceof JLabel) {
            JLabel label = (JLabel) component;
            boolean isTopLevel = path.getPathCount() == (isRootVisible() ? 1 : 2);

            label.putClientProperty("Quaqua.Label.style",
                    isTopLevel ? isRowSelected ? (isActive ? "categorySelected" : "categoryInactiveSelected")
                    : (isActive ? "category" : "categoryInactive") : isRowSelected ? (isActive ? "rowSelected" : "rowInactiveSelected")
                    : (isActive ? "row" : "rowInactive"));

            // We need to do some (very ugly) modifications because
            // DefaultTreeCellRenderers have their own paint-method
            // and paint a border around each item
            if (label instanceof DefaultTreeCellRenderer) {
                DefaultTreeCellRenderer treeCellRenderer = (DefaultTreeCellRenderer) label;
                treeCellRenderer.setBackgroundNonSelectionColor(TRANSPARENT_COLOR);
                treeCellRenderer.setBackgroundSelectionColor(TRANSPARENT_COLOR);
                treeCellRenderer.setBorderSelectionColor(TRANSPARENT_COLOR);
                treeCellRenderer.setBorder(new EmptyBorder(0, 0, 0, 0));
            }

            if (isTopLevel) {
                label.setIcon(null);
                label.setDisabledIcon(null);
            }
        }

        rendererPane.paintComponent(g, component, tree, bounds.x, bounds.y,
                bounds.width, bounds.height, true);
    }

    private boolean isSideBar() {
        Object property = tree.getClientProperty("Quaqua.Tree.style");
        return property != null && (property.equals("sideBar")
                || property.equals("sourceList"));
    }

    /**
     * Extends the selection from the anchor to make <code>newLead</code>
     * the lead of the selection. This does not scroll.
     */
    private void extendSelection(TreePath newLead) {
        TreePath aPath = getAnchorSelectionPath();
        int aRow = (aPath == null) ? -1 : getRowForPath(tree, aPath);
        int newIndex = getRowForPath(tree, newLead);

        if (aRow == -1) {
            tree.setSelectionRow(newIndex);
        } else {
            if (aRow < newIndex) {
                tree.setSelectionInterval(aRow, newIndex);
            } else {
                tree.setSelectionInterval(newIndex, aRow);
            }

            setAnchorSelectionPath(aPath);
            setLeadSelectionPath(newLead);
        }



    }

    private class Handler implements CellEditorListener, FocusListener,
            KeyListener, MouseListener, MouseMotionListener, PropertyChangeListener,
            TreeExpansionListener, TreeModelListener,
            TreeSelectionListener, QuaquaDragRecognitionSupport.BeforeDrag {
        //
        // KeyListener
        //

        private String prefix = "";
        private String typedString = "";
        private long lastTime = 0L;        // MouseListener & MouseMotionListener
        private boolean mouseReleaseDeselects;
        private boolean mouseDragSelects;

        /**
         * Invoked when a key has been typed.
         *
         * Moves the keyboard focus to the first element whose prefix matches the
         * sequence of alphanumeric keys pressed by the user with delay less
         * than value of <code>timeFactor</code> property (or 1000 milliseconds
         * if it is not defined). Subsequent same key presses move the keyboard
         * focus to the next object that starts with the same letter until another
         * key is pressed, then it is treated as the prefix with appropriate number
         * of the same letters followed by first typed another letter.
         */
        public void keyTyped(KeyEvent e) {
            // handle first letter navigation
            if (tree != null && tree.getRowCount() > 0 && tree.hasFocus()
                    && tree.isEnabled()) {
                if (e.isAltDown() || e.isControlDown() || e.isMetaDown()
                        || isNavigationKey(e)) {
                    return;
                }
                boolean startingFromSelection = true;

                char c = e.getKeyChar();

                long time = e.getWhen();
                int startingRow = getLeadSelectionRow();
                if (time - lastTime < timeFactor) {
                    typedString += c;
                    if ((prefix.length() == 1) && (c == prefix.charAt(0))) {
                        // Subsequent same key presses move the keyboard focus to the next
                        // object that starts with the same letter.
                        startingRow++;
                    } else {
                        prefix = typedString;
                    }
                } else {
                    startingRow++;
                    typedString = "" + c;
                    prefix = typedString;
                }
                lastTime = time;

                if (startingRow < 0 || startingRow >= tree.getRowCount()) {
                    startingFromSelection = false;
                    startingRow = 0;
                }
                TreePath path = tree.getNextMatch(prefix, startingRow,
                        Position.Bias.Forward);
                if (path != null) {
                    tree.setSelectionPath(path);
                    int row = getRowForPath(tree, path);
                    ensureRowsAreVisible(row, row);
                } else if (startingFromSelection) {
                    path = tree.getNextMatch(prefix, 0,
                            Position.Bias.Forward);
                    if (path != null) {
                        tree.setSelectionPath(path);
                        int row = getRowForPath(tree, path);
                        ensureRowsAreVisible(row, row);
                    }
                }
            }
        }

        /**
         * Invoked when a key has been pressed.
         *
         * Checks to see if the key event is a navigation key to prevent
         * dispatching these keys for the first letter navigation.
         */
        public void keyPressed(KeyEvent e) {
            if (isNavigationKey(e)) {
                prefix = "";
                typedString = "";
                lastTime = 0L;
            }
        }

        public void keyReleased(KeyEvent e) {
        }

        /**
         * Returns whether or not the supplied key event maps to a key that is used for
         * navigation.  This is used for optimizing key input by only passing non-
         * navigation keys to the first letter navigation mechanism.
         */
        private boolean isNavigationKey(KeyEvent event) {
            InputMap inputMap = tree.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            KeyStroke key = KeyStroke.getKeyStrokeForEvent(event);

            if (inputMap != null && inputMap.get(key) != null) {
                return true;
            }
            return false;
        }

        //
        // PropertyChangeListener
        //
        public void propertyChange(PropertyChangeEvent event) {
            String name = event.getPropertyName();
            if (event.getSource() == treeSelectionModel) {
                treeSelectionModel.resetRowSelection();

                // Update the lead row in the JTree
            } else if (event.getSource() == tree) {
                if (name != null && name.equals("Frame.active")) {
                    if (tree.getClientProperty("Quaqua.Tree.style") != null //
                            && (tree.getClientProperty("Quaqua.Tree.style").equals("sideBar") || tree.getClientProperty("Quaqua.Tree.style").equals("sourceList"))) {
                        tree.repaint();
                    }
                } else if (name != null && name.equals(JTree.LEAD_SELECTION_PATH_PROPERTY)) {
                    if (!ignoreLAChange) {
                        updateLeadRow();
                        repaintPath((TreePath) event.getOldValue());
                        repaintPath((TreePath) event.getNewValue());
                    }
                } else if (name != null && name.equals(JTree.ANCHOR_SELECTION_PATH_PROPERTY)) {
                    if (!ignoreLAChange) {
                        repaintPath((TreePath) event.getOldValue());
                        repaintPath((TreePath) event.getNewValue());
                    }
                }
                if (name != null && name.equals(JTree.CELL_RENDERER_PROPERTY)) {
                    setCellRenderer((TreeCellRenderer) event.getNewValue());
                    redoTheLayout();
                } else if (name != null && name.equals(JTree.TREE_MODEL_PROPERTY)) {
                    setModel((TreeModel) event.getNewValue());
                } else if (name != null && name.equals(JTree.ROOT_VISIBLE_PROPERTY)) {
                    setRootVisible(((Boolean) event.getNewValue()).booleanValue());
                } else if (name != null && name.equals(JTree.SHOWS_ROOT_HANDLES_PROPERTY)) {
                    setShowsRootHandles(((Boolean) event.getNewValue()).booleanValue());
                } else if (name != null && name.equals(JTree.ROW_HEIGHT_PROPERTY)) {
                    setRowHeight(((Integer) event.getNewValue()).intValue());
                } else if (name != null && name.equals(JTree.CELL_EDITOR_PROPERTY)) {
                    setCellEditor((TreeCellEditor) event.getNewValue());
                } else if (name != null && name.equals(JTree.EDITABLE_PROPERTY)) {
                    setEditable(((Boolean) event.getNewValue()).booleanValue());
                } else if (name != null && name.equals(JTree.LARGE_MODEL_PROPERTY)) {
                    setLargeModel(tree.isLargeModel());
                } else if (name != null && name.equals(JTree.SELECTION_MODEL_PROPERTY)) {
                    setSelectionModel(tree.getSelectionModel());
                } else if (name != null && name.equals("font")) {
                    completeEditing();
                    if (treeState != null) {
                        treeState.invalidateSizes();
                    }
                    updateSize();
                } else if (name != null && name.equals("componentOrientation")) {
                    if (tree != null) {
                        //leftToRight = QuaquaUtilities.isLeftToRight(tree);
                        redoTheLayout();
                        tree.treeDidChange();

                        InputMap km = getInputMap(JComponent.WHEN_FOCUSED);
                        SwingUtilities.replaceUIInputMap(tree,
                                JComponent.WHEN_FOCUSED, km);
                    }
                } else if (name != null && name.equals("transferHandler")) {
                    DropTarget dropTarget = tree.getDropTarget();
                    if (dropTarget instanceof UIResource) {
                        if (defaultDropTargetListener == null) {
                            defaultDropTargetListener = new TreeDropTargetListener();
                        }
                        try {
                            dropTarget.addDropTargetListener(defaultDropTargetListener);
                        } catch (TooManyListenersException tmle) {
                            // should not happen... swing drop target is multicast
                        }
                    }
                } else if (name != null && name.equals("JComponent.sizeVariant")) {
                    QuaquaUtilities.applySizeVariant(tree);
                } else if (name != null && name.equals("Quaqua.Tree.style")) {
                    QuaquaUtilities.applySizeVariant(tree);
                }
            }
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
            isMouseReleaseStartsEditing = false;
        }

        public void dragStarting(MouseEvent me) {
        }

        /**
         * Invoked when a mouse button has been pressed on a component.
         */
        public void mousePressed(MouseEvent e) {
            if (tree.isEnabled()) {
                // if we can't stop any ongoing editing, do nothing
                if (isEditing(tree) && tree.getInvokesStopCellEditing() && !stopEditing(tree)) {
                    return;
                }

                completeEditing();

                // Note: Some applications depend on selection changes only occuring
                // on focused components. Maybe we must not do any changes to the
                // selection changes at all, when the compnent is not focused?
                if (tree.isRequestFocusEnabled()) {
                    tree.requestFocusInWindow();
                }


                TreePath path = getMouseClickedClosestPathForLocation(tree, e.getX(), e.getY());

                // Check for clicks in expand control
                if (isLocationInExpandControl(path, e.getX(), e.getY())) {
                    checkForClickInExpandControl(path, e.getX(), e.getY());
                    return;
                }

                int index = tree.getRowForPath(path);

                mouseDragSelects = false;
                mouseReleaseDeselects = false;
                isMouseReleaseStartsEditing = true;
                isDragRecognitionOngoing = false;
                if (index != -1) {
                    boolean isRowAtIndexSelected = tree.isRowSelected(index);
                    if (isRowAtIndexSelected && e.isPopupTrigger()) {
                        // Do not change the selection, if the item is already
                        // selected, and the user triggers the popup menu.
                    } else {
                        int anchorIndex = tree.getRowForPath(tree.getAnchorSelectionPath());

                        if ((e.getModifiersEx() & (MouseEvent.META_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK)) == MouseEvent.META_DOWN_MASK) {
                            if (isRowAtIndexSelected) {
                                tree.removeSelectionInterval(index, index);
                            } else {
                                tree.addSelectionInterval(index, index);
                                mouseDragSelects = true;
                                isMouseReleaseStartsEditing = false;
                            }
                        } else if ((e.getModifiersEx() & (MouseEvent.SHIFT_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK)) == MouseEvent.SHIFT_DOWN_MASK
                                && anchorIndex != -1) {
                            tree.setSelectionInterval(anchorIndex, index);
                            setLeadSelectionPath(path);
                            mouseDragSelects = true;
                            isMouseReleaseStartsEditing = false;
                        } else if ((e.getModifiersEx() & (MouseEvent.SHIFT_DOWN_MASK | MouseEvent.META_DOWN_MASK)) == 0) {
                            if (isRowAtIndexSelected) {
                                if (tree.getDragEnabled()) {
                                    isDragRecognitionOngoing = QuaquaDragRecognitionSupport.mousePressed(e);
                                    mouseDragSelects = mouseReleaseDeselects = false;
                                } else {
                                    mouseReleaseDeselects = tree.isFocusOwner();
                                }
                            } else {
                                tree.setSelectionInterval(index, index);
                                if (tree.getDragEnabled()
                                        && getPathBounds(tree, path).contains(e.getPoint())) {
                                    isDragRecognitionOngoing = QuaquaDragRecognitionSupport.mousePressed(e);
                                    mouseDragSelects = mouseReleaseDeselects = false;
                                    isMouseReleaseStartsEditing = false;
                                } else {
                                    mouseDragSelects = true;
                                    isMouseReleaseStartsEditing = false;
                                }
                            }
                            setAnchorSelectionPath(path);
                            setLeadSelectionPath(path);
                        }
                    }
                }
            }
        }

        public void mouseDragged(MouseEvent e) {
            if (tree.isEnabled()) {
                if (tree.getDragEnabled() && isDragRecognitionOngoing) {
                    QuaquaDragRecognitionSupport.mouseDragged(e, this);
                }

                // Do nothing if we can't stop editing.
                if (isEditing(tree) && tree.getInvokesStopCellEditing()
                        && !stopEditing(tree)) {
                    return;
                }

                TreePath leadPath = getClosestPathForLocation(tree, e.getX(),
                        e.getY());

                // this is a dirty trick to reset the timer of the cell editor.
                if (tree.getCellEditor() != null) {
                    tree.getCellEditor().isCellEditable(new EventObject(this));
                }

                mouseReleaseDeselects = false;
                isMouseReleaseStartsEditing = false;
                if (mouseDragSelects) {
                    int index = tree.getRowForPath(leadPath);
                    if (index != -1) {
                        Rectangle cellBounds = tree.getRowBounds(index);
                        tree.scrollRectToVisible(cellBounds);
                        TreePath anchorPath = tree.getAnchorSelectionPath();
                        int anchorIndex = tree.getRowForPath(anchorPath);
                        if (tree.getSelectionModel().getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION) {
                            tree.setSelectionInterval(index, index);
                        } else {
                            if (anchorIndex < index) {
                                tree.setSelectionInterval(anchorIndex, index);
                            } else {
                                tree.setSelectionInterval(index, anchorIndex);
                            }
                            setAnchorSelectionPath(anchorPath);
                            setLeadSelectionPath(leadPath);
                        }
                    }
                }
            }
        }

        /**
         * Invoked when the mouse button has been moved on a component
         * (with no buttons down).
         */
        public void mouseMoved(MouseEvent e) {
            isMouseReleaseStartsEditing = false;
            // this is a dirty trick to reset the timer of the cell editor.
            if (tree.getCellEditor() != null) {
                tree.getCellEditor().isCellEditable(new EventObject(this));
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (tree.isEnabled()) {
                if (isEditing(tree) && tree.getInvokesStopCellEditing()
                        && !stopEditing(tree)) {
                    return;
                }
                TreePath path = getMouseClickedClosestPathForLocation(tree, e.getX(),
                        e.getY());
                if (startEditingOnRelease(path, e, e)) {
                    return;
                }

                mouseDragSelects = false;
                if (mouseReleaseDeselects) {
                    int index = tree.getRowForPath(path);
                    tree.setSelectionInterval(index, index);
                }
                //tree.getSelectionModel().setValueIsAdjusting(false);
            }
            if (tree.isRequestFocusEnabled()) {
                tree.requestFocus();
            }
        }

        //
        // FocusListener
        //
        public void focusGained(FocusEvent e) {
            if (tree != null) {
                Rectangle pBounds;

                pBounds = getPathBounds(tree, tree.getLeadSelectionPath());
                if (pBounds != null) {
                    tree.repaint(pBounds);
                }
                pBounds = getPathBounds(tree, getLeadSelectionPath());
                if (pBounds != null) {
                    tree.repaint(pBounds);
                }
            }
        }

        public void focusLost(FocusEvent e) {
            focusGained(e);
        }

        //
        // CellEditorListener
        //
        public void editingStopped(ChangeEvent e) {
            completeEditing(false, false, true);
        }

        /** Messaged when editing has been canceled in the tree. */
        public void editingCanceled(ChangeEvent e) {
            completeEditing(false, false, false);
        }

        //
        // TreeSelectionListener
        // 
        public void valueChanged(TreeSelectionEvent event) {
            // Stop editing
            completeEditing();
            // Make sure all the paths are visible, if necessary.
            // PENDING: This should be tweaked when isAdjusting is added
            if (tree.getExpandsSelectedPaths() && treeSelectionModel != null) {
                TreePath[] paths = treeSelectionModel.getSelectionPaths();

                if (paths != null) {
                    for (int counter = paths.length - 1; counter >= 0;
                            counter--) {
                        TreePath path = paths[counter].getParentPath();
                        boolean expand = true;

                        while (path != null) {
                            // Indicates this leadPath isn't valid anymore,
                            // we shouldn't attempt to expand it then.
                            if (treeModel.isLeaf(path.getLastPathComponent())) {
                                expand = false;
                                path = null;
                            } else {
                                path = path.getParentPath();
                            }
                        }
                        if (expand) {
                            tree.makeVisible(paths[counter]);
                        }
                    }
                }
            }

            TreePath oldLead = getLeadSelectionPath();
            lastSelectedRow = tree.getMinSelectionRow();
            TreePath lead = tree.getSelectionModel().getLeadSelectionPath();
            setAnchorSelectionPath(lead);
            setLeadSelectionPath(lead);

            TreePath[] changedPaths = event.getPaths();
            Rectangle nodeBounds;
            Rectangle visRect = tree.getVisibleRect();
            boolean paintPaths = true;
            int nWidth = tree.getWidth();

            if (changedPaths != null) {
                int counter, maxCounter = changedPaths.length;

                if (maxCounter > 4) {
                    tree.repaint();
                    paintPaths = false;
                } else {
                    for (counter = 0; counter < maxCounter; counter++) {
                        nodeBounds = getPathBounds(tree,
                                changedPaths[counter]);
                        if (nodeBounds != null
                                && visRect.intersects(nodeBounds)) {
                            tree.repaint(0, nodeBounds.y, nWidth,
                                    nodeBounds.height);
                        }
                    }
                }
            }
            if (paintPaths) {
                oldLead = event.getOldLeadSelectionPath();
                nodeBounds = getPathBounds(tree, oldLead);
                if (nodeBounds != null && visRect.intersects(nodeBounds)) {
                    tree.repaint(0, nodeBounds.y, nWidth, nodeBounds.height);
                }
                nodeBounds = getPathBounds(tree, lead);
                if (nodeBounds != null && visRect.intersects(nodeBounds)) {
                    tree.repaint(0, nodeBounds.y, nWidth, nodeBounds.height);
                }
            }
        }

        //
        // TreeExpansionListener
        //
        public void treeExpanded(TreeExpansionEvent event) {
            if (event != null && tree != null) {
                TreePath path = event.getPath();

                updateExpandedDescendants(path);
            }
        }

        public void treeCollapsed(TreeExpansionEvent event) {
            if (event != null && tree != null) {
                TreePath path = event.getPath();

                completeEditing();
                if (path != null && tree.isVisible(path)) {
                    treeState.setExpandedState(path, false);
                    updateLeadRow();
                    updateSize();
                }
            }
        }

        //
        // TreeModelListener
        //
        public void treeNodesChanged(TreeModelEvent e) {
            if (treeState != null && e != null) {
                TreePath parentPath = e.getTreePath();
                int[] indices = e.getChildIndices();
                if (indices == null || indices.length == 0) {
                    // The root has changed
                    treeState.treeNodesChanged(e);
                    updateSize();
                } else if (treeState.isExpanded(parentPath)) {
                    // Changed nodes are visible
                    // Find the minimum index, we only need paint from there
                    // down.
                    int minIndex = indices[0];
                    for (int i = indices.length - 1; i > 0; i--) {
                        minIndex = Math.min(indices[i], minIndex);
                    }
                    Object minChild = treeModel.getChild(
                            parentPath.getLastPathComponent(), minIndex);
                    TreePath minPath = parentPath.pathByAddingChild(minChild);
                    Rectangle minBounds = getPathBounds(tree, minPath);

                    // Forward to the treestate
                    treeState.treeNodesChanged(e);

                    // Mark preferred size as bogus.
                    updateSize0();

                    // And repaint
                    Rectangle newMinBounds = getPathBounds(tree, minPath);
                    if (newMinBounds != null) { // prevent NPE when tree is not showing
                        if (indices.length == 1
                                && newMinBounds.height == minBounds.height) {
                            tree.repaint(0, minBounds.y, tree.getWidth(),
                                    minBounds.height);
                        } else {
                            tree.repaint(0, minBounds.y, tree.getWidth(),
                                    tree.getHeight() - minBounds.y);
                        }
                    }
                } else {
                    // Nodes that changed aren't visible.  No need to paint
                    treeState.treeNodesChanged(e);
                }
            }
        }

        public void treeNodesInserted(TreeModelEvent e) {
            if (treeState != null && e != null) {
                treeState.treeNodesInserted(e);

                updateLeadRow();

                TreePath path = e.getTreePath();

                if (treeState.isExpanded(path)) {
                    updateSize();
                } else {
                    // PENDING(sky): Need a method in TreeModelEvent
                    // that can return the count, getChildIndices allocs
                    // a new array!
                    int[] indices = e.getChildIndices();
                    int childCount = treeModel.getChildCount(path.getLastPathComponent());

                    if (indices != null && (childCount - indices.length) == 0) {
                        updateSize();
                    }
                }
            }
        }

        public void treeNodesRemoved(TreeModelEvent e) {
            if (treeState != null && e != null) {
                treeState.treeNodesRemoved(e);

                updateLeadRow();

                TreePath path = e.getTreePath();

                if (treeState.isExpanded(path)
                        || treeModel.getChildCount(path.getLastPathComponent()) == 0) {
                    updateSize();
                }
            }
        }

        public void treeStructureChanged(TreeModelEvent e) {
            if (treeState != null && e != null) {
                treeState.treeStructureChanged(e);

                updateLeadRow();

                TreePath pPath = e.getTreePath();

                if (pPath != null) {
                    pPath = pPath.getParentPath();
                }
                if (pPath == null || treeState.isExpanded(pPath)) {
                    updateSize();
                }
            }
        }
    }

    /**
     * A DropTargetListener to extend the default Swing handling of drop operations
     * by moving the tree selection to the nearest location to the mouse pointer.
     * Also adds autoscroll capability.
     */
    static class TreeDropTargetListener extends QuaquaDropTargetListener {

        /**
         * called to save the state of a component in case it needs to
         * be restored because a drop is not performed.
         */
        @Override
        protected void saveComponentState(JComponent comp) {
            JTree tree = (JTree) comp;
            selectedIndices = tree.getSelectionRows();
        }

        /**
         * called to restore the state of a component
         * because a drop was not performed.
         */
        @Override
        protected void restoreComponentState(JComponent comp) {
            JTree tree = (JTree) comp;
            tree.setSelectionRows(selectedIndices);
        }

        /**
         * called to set the insertion location to match the current
         * mouse pointer coordinates.
         */
        @Override
        protected void updateInsertionLocation(JComponent comp, Point p) {
            JTree tree = (JTree) comp;
            BasicTreeUI ui = (BasicTreeUI) tree.getUI();
            TreePath path = ui.getClosestPathForLocation(tree, p.x, p.y);
            if (path != null) {
                tree.setSelectionPath(path);
            }
        }
        private int[] selectedIndices;
    }

    private static class Actions extends QuaquaUIAction {

        private static final String SELECT_PREVIOUS = "selectPrevious";
        private static final String SELECT_PREVIOUS_CHANGE_LEAD =
                "selectPreviousChangeLead";
        private static final String SELECT_PREVIOUS_EXTEND_SELECTION =
                "selectPreviousExtendSelection";
        private static final String SELECT_NEXT = "selectNext";
        private static final String SELECT_NEXT_CHANGE_LEAD =
                "selectNextChangeLead";
        private static final String SELECT_NEXT_EXTEND_SELECTION =
                "selectNextExtendSelection";
        private static final String SELECT_CHILD = "selectChild";
        private static final String SELECT_CHILD_CHANGE_LEAD =
                "selectChildChangeLead";
        private static final String SELECT_PARENT = "selectParent";
        private static final String SELECT_PARENT_CHANGE_LEAD =
                "selectParentChangeLead";
        private static final String SCROLL_UP_CHANGE_SELECTION =
                "scrollUpChangeSelection";
        private static final String SCROLL_UP_CHANGE_LEAD =
                "scrollUpChangeLead";
        private static final String SCROLL_UP_EXTEND_SELECTION =
                "scrollUpExtendSelection";
        private static final String SCROLL_DOWN_CHANGE_SELECTION =
                "scrollDownChangeSelection";
        private static final String SCROLL_DOWN_EXTEND_SELECTION =
                "scrollDownExtendSelection";
        private static final String SCROLL_DOWN_CHANGE_LEAD =
                "scrollDownChangeLead";
        private static final String SELECT_FIRST = "selectFirst";
        private static final String SELECT_FIRST_CHANGE_LEAD =
                "selectFirstChangeLead";
        private static final String SELECT_FIRST_EXTEND_SELECTION =
                "selectFirstExtendSelection";
        private static final String SELECT_LAST = "selectLast";
        private static final String SELECT_LAST_CHANGE_LEAD =
                "selectLastChangeLead";
        private static final String SELECT_LAST_EXTEND_SELECTION =
                "selectLastExtendSelection";
        private static final String TOGGLE = "toggle";
        private static final String CANCEL_EDITING = "cancel";
        private static final String START_EDITING = "startEditing";
        private static final String SELECT_ALL = "selectAll";
        private static final String CLEAR_SELECTION = "clearSelection";
        private static final String SCROLL_LEFT = "scrollLeft";
        private static final String SCROLL_RIGHT = "scrollRight";
        private static final String SCROLL_LEFT_EXTEND_SELECTION =
                "scrollLeftExtendSelection";
        private static final String SCROLL_RIGHT_EXTEND_SELECTION =
                "scrollRightExtendSelection";
        private static final String SCROLL_RIGHT_CHANGE_LEAD =
                "scrollRightChangeLead";
        private static final String SCROLL_LEFT_CHANGE_LEAD =
                "scrollLeftChangeLead";
        private static final String EXPAND = "expand";
        private static final String COLLAPSE = "collapse";
        private static final String MOVE_SELECTION_TO_PARENT =
                "moveSelectionToParent";        // add the lead item to the selection without changing lead or anchor
        private static final String ADD_TO_SELECTION = "addToSelection";        // toggle the selected state of the lead item and move the anchor to it
        private static final String TOGGLE_AND_ANCHOR = "toggleAndAnchor";        // extend the selection to the lead item
        private static final String EXTEND_TO = "extendTo";        // move the anchor to the lead and ensure only that item is selected
        private static final String MOVE_SELECTION_TO = "moveSelectionTo";

        Actions() {
            super(null);
        }

        Actions(String key) {
            super(key);
        }

        @Override
        public boolean isEnabled(Object o) {
            if (o instanceof JTree) {
                if (getName() != null && getName().equals(CANCEL_EDITING)) {
                    return ((JTree) o).isEditing();
                }
            }
            return true;
        }

        public void actionPerformed(ActionEvent e) {
            JTree tree = (JTree) e.getSource();
            QuaquaTreeUI ui = (QuaquaTreeUI) QuaquaUtilities.getUIOfType(
                    tree.getUI(), QuaquaTreeUI.class);
            if (ui == null) {
                return;
            }
            String key = getName();
            if (key != null && key.equals(SELECT_PREVIOUS)) {
                increment(tree, ui, -1, false, true);
            } else if (key != null && key.equals(SELECT_PREVIOUS_CHANGE_LEAD)) {
                increment(tree, ui, -1, false, false);
            } else if (key != null && key.equals(SELECT_PREVIOUS_EXTEND_SELECTION)) {
                increment(tree, ui, -1, true, true);
            } else if (key != null && key.equals(SELECT_NEXT)) {
                increment(tree, ui, 1, false, true);
            } else if (key != null && key.equals(SELECT_NEXT_CHANGE_LEAD)) {
                increment(tree, ui, 1, false, false);
            } else if (key != null && key.equals(SELECT_NEXT_EXTEND_SELECTION)) {
                increment(tree, ui, 1, true, true);
            } else if (key != null && key.equals(SELECT_CHILD)) {
                traverse(tree, ui, 1, true);
            } else if (key != null && key.equals(SELECT_CHILD_CHANGE_LEAD)) {
                traverse(tree, ui, 1, false);
            } else if (key != null && key.equals(SELECT_PARENT)) {
                traverse(tree, ui, -1, true);
            } else if (key != null && key.equals(SELECT_PARENT_CHANGE_LEAD)) {
                traverse(tree, ui, -1, false);
            } else if (key != null && key.equals(SCROLL_UP_CHANGE_SELECTION)) {
                page(tree, ui, -1, false, true);
            } else if (key != null && key.equals(SCROLL_UP_CHANGE_LEAD)) {
                page(tree, ui, -1, false, false);
            } else if (key != null && key.equals(SCROLL_UP_EXTEND_SELECTION)) {
                page(tree, ui, -1, true, true);
            } else if (key != null && key.equals(SCROLL_DOWN_CHANGE_SELECTION)) {
                page(tree, ui, 1, false, true);
            } else if (key != null && key.equals(SCROLL_DOWN_EXTEND_SELECTION)) {
                page(tree, ui, 1, true, true);
            } else if (key != null && key.equals(SCROLL_DOWN_CHANGE_LEAD)) {
                page(tree, ui, 1, false, false);
            } else if (key != null && key.equals(SELECT_FIRST)) {
                home(tree, ui, -1, false, true);
            } else if (key != null && key.equals(SELECT_FIRST_CHANGE_LEAD)) {
                home(tree, ui, -1, false, false);
            } else if (key != null && key.equals(SELECT_FIRST_EXTEND_SELECTION)) {
                home(tree, ui, -1, true, true);
            } else if (key != null && key.equals(SELECT_LAST)) {
                home(tree, ui, 1, false, true);
            } else if (key != null && key.equals(SELECT_LAST_CHANGE_LEAD)) {
                home(tree, ui, 1, false, false);
            } else if (key != null && key.equals(SELECT_LAST_EXTEND_SELECTION)) {
                home(tree, ui, 1, true, true);
            } else if (key != null && key.equals(TOGGLE)) {
                toggle(tree, ui);
            } else if (key != null && key.equals(CANCEL_EDITING)) {
                cancelEditing(tree, ui);
            } else if (key != null && key.equals(START_EDITING)) {
                startEditing(tree, ui);
            } else if (key != null && key.equals(SELECT_ALL)) {
                selectAll(tree, ui, true);
            } else if (key != null && key.equals(CLEAR_SELECTION)) {
                selectAll(tree, ui, false);
            } else if (key != null && key.equals(ADD_TO_SELECTION)) {
                if (ui.getRowCount(tree) > 0) {
                    int lead = ui.getLeadSelectionRow();
                    if (!tree.isRowSelected(lead)) {
                        TreePath aPath = ui.getAnchorSelectionPath();
                        tree.addSelectionRow(lead);
                        ui.setAnchorSelectionPath(aPath);
                    }
                }
            } else if (key != null && key.equals(TOGGLE_AND_ANCHOR)) {
                if (ui.getRowCount(tree) > 0) {
                    int lead = ui.getLeadSelectionRow();
                    TreePath lPath = ui.getLeadSelectionPath();
                    if (!tree.isRowSelected(lead)) {
                        tree.addSelectionRow(lead);
                    } else {
                        tree.removeSelectionRow(lead);
                        ui.setLeadSelectionPath(lPath);
                    }
                    ui.setAnchorSelectionPath(lPath);
                }
            } else if (key != null && key.equals(EXTEND_TO)) {
                extendSelection(tree, ui);
            } else if (key != null && key.equals(MOVE_SELECTION_TO)) {
                if (ui.getRowCount(tree) > 0) {
                    int lead = ui.getLeadSelectionRow();
                    tree.setSelectionInterval(lead, lead);
                }
            } else if (key != null && key.equals(SCROLL_LEFT)) {
                scroll(tree, ui, SwingConstants.HORIZONTAL, -10);
            } else if (key != null && key.equals(SCROLL_RIGHT)) {
                scroll(tree, ui, SwingConstants.HORIZONTAL, 10);
            } else if (key != null && key.equals(SCROLL_LEFT_EXTEND_SELECTION)) {
                scrollChangeSelection(tree, ui, -1, true, true);
            } else if (key != null && key.equals(SCROLL_RIGHT_EXTEND_SELECTION)) {
                scrollChangeSelection(tree, ui, 1, true, true);
            } else if (key != null && key.equals(SCROLL_RIGHT_CHANGE_LEAD)) {
                scrollChangeSelection(tree, ui, 1, false, false);
            } else if (key != null && key.equals(SCROLL_LEFT_CHANGE_LEAD)) {
                scrollChangeSelection(tree, ui, -1, false, false);
            } else if (key != null && key.equals(EXPAND)) {
                expand(tree, ui);
            } else if (key != null && key.equals(COLLAPSE)) {
                collapse(tree, ui);
            } else if (key != null && key.equals(MOVE_SELECTION_TO_PARENT)) {
                moveSelectionToParent(tree, ui);
            }
        }

        private void scrollChangeSelection(JTree tree, QuaquaTreeUI ui,
                int direction, boolean addToSelection,
                boolean changeSelection) {

            if (ui.getRowCount(tree) > 0
                    && ui.treeSelectionModel != null) {
                TreePath newPath;
                Rectangle visRect = tree.getVisibleRect();

                if (direction == -1) {
                    newPath = ui.getClosestPathForLocation(tree, visRect.x,
                            visRect.y);
                    visRect.x = Math.max(0, visRect.x - visRect.width);
                } else {
                    visRect.x = Math.min(Math.max(0, tree.getWidth()
                            - visRect.width), visRect.x + visRect.width);
                    newPath = ui.getClosestPathForLocation(tree, visRect.x,
                            visRect.y + visRect.height);
                }
                // Scroll
                tree.scrollRectToVisible(visRect);
                // select
                if (addToSelection) {
                    ui.extendSelection(newPath);
                } else if (changeSelection) {
                    tree.setSelectionPath(newPath);
                } else {
                    ui.setLeadSelectionPath(newPath, true);
                }
            }
        }

        private void scroll(JTree component, QuaquaTreeUI ui, int direction,
                int amount) {
            Rectangle visRect = component.getVisibleRect();
            Dimension size = component.getSize();
            if (direction == SwingConstants.HORIZONTAL) {
                visRect.x += amount;
                visRect.x = Math.max(0, visRect.x);
                visRect.x = Math.min(Math.max(0, size.width - visRect.width),
                        visRect.x);
            } else {
                visRect.y += amount;
                visRect.y = Math.max(0, visRect.y);
                visRect.y = Math.min(Math.max(0, size.width - visRect.height),
                        visRect.y);
            }
            component.scrollRectToVisible(visRect);
        }

        private void extendSelection(JTree tree, QuaquaTreeUI ui) {
            if (ui.getRowCount(tree) > 0) {
                int lead = ui.getLeadSelectionRow();

                if (lead != -1) {
                    TreePath leadP = ui.getLeadSelectionPath();
                    TreePath aPath = ui.getAnchorSelectionPath();
                    int aRow = ui.getRowForPath(tree, aPath);

                    if (aRow == -1) {
                        aRow = 0;
                    }
                    tree.setSelectionInterval(aRow, lead);
                    ui.setLeadSelectionPath(leadP);
                    ui.setAnchorSelectionPath(aPath);
                }
            }
        }

        private void selectAll(JTree tree, QuaquaTreeUI ui, boolean selectAll) {
            int rowCount = ui.getRowCount(tree);

            if (rowCount > 0) {
                if (selectAll) {
                    if (tree.getSelectionModel().getSelectionMode()
                            == TreeSelectionModel.SINGLE_TREE_SELECTION) {

                        int lead = ui.getLeadSelectionRow();
                        if (lead != -1) {
                            tree.setSelectionRow(lead);
                        } else if (tree.getMinSelectionRow() == -1) {
                            tree.setSelectionRow(0);
                            ui.ensureRowsAreVisible(0, 0);
                        }
                        return;
                    }

                    TreePath lastPath = ui.getLeadSelectionPath();
                    TreePath aPath = ui.getAnchorSelectionPath();

                    if (lastPath != null && !tree.isVisible(lastPath)) {
                        lastPath = null;
                    }
                    tree.setSelectionInterval(0, rowCount - 1);
                    if (lastPath != null) {
                        ui.setLeadSelectionPath(lastPath);
                    }
                    if (aPath != null && tree.isVisible(aPath)) {
                        ui.setAnchorSelectionPath(aPath);
                    }
                } else {
                    TreePath lastPath = ui.getLeadSelectionPath();
                    TreePath aPath = ui.getAnchorSelectionPath();

                    tree.clearSelection();
                    ui.setAnchorSelectionPath(aPath);
                    ui.setLeadSelectionPath(lastPath);
                }
            }
        }

        private void startEditing(JTree tree, QuaquaTreeUI ui) {
            new Throwable().printStackTrace();
            TreePath lead = ui.getLeadSelectionPath();
            int editRow = (lead != null) ? ui.getRowForPath(tree, lead) : -1;

            if (editRow != -1) {
                tree.startEditingAtPath(lead);
            }
        }

        private void cancelEditing(JTree tree, QuaquaTreeUI ui) {
            tree.cancelEditing();
        }

        private void toggle(JTree tree, QuaquaTreeUI ui) {
            int selRow = ui.getLeadSelectionRow();

            if (selRow != -1 && !ui.isLeaf(selRow)) {
                TreePath aPath = ui.getAnchorSelectionPath();
                TreePath lPath = ui.getLeadSelectionPath();

                ui.toggleExpandState(ui.getPathForRow(tree, selRow));
                ui.setAnchorSelectionPath(aPath);
                ui.setLeadSelectionPath(lPath);
            }
        }

        private void expand(JTree tree, QuaquaTreeUI ui) {
            int selRow = ui.getLeadSelectionRow();
            tree.expandRow(selRow);
        }

        private void collapse(JTree tree, QuaquaTreeUI ui) {
            int selRow = ui.getLeadSelectionRow();
            tree.collapseRow(selRow);
        }

        private void increment(JTree tree, QuaquaTreeUI ui, int direction,
                boolean addToSelection,
                boolean changeSelection) {

            // disable moving of lead unless in discontiguous mode
            if (!addToSelection && !changeSelection
                    && tree.getSelectionModel().getSelectionMode()
                    != TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
                changeSelection = true;
            }

            int rowCount;

            if (ui.treeSelectionModel != null
                    && (rowCount = tree.getRowCount()) > 0) {
                int selIndex = ui.getLeadSelectionRow();
                int newIndex;

                if (selIndex == -1) {
                    if (direction == 1) {
                        newIndex = 0;
                    } else {
                        newIndex = rowCount - 1;
                    }
                } else /* Aparently people don't like wrapping;( */ {
                    newIndex = Math.min(rowCount - 1, Math.max(0, (selIndex + direction)));
                }
                if (addToSelection && ui.treeSelectionModel.getSelectionMode() != TreeSelectionModel.SINGLE_TREE_SELECTION) {
                    ui.extendSelection(tree.getPathForRow(newIndex));
                } else if (changeSelection) {
                    tree.setSelectionInterval(newIndex, newIndex);
                } else {
                    ui.setLeadSelectionPath(tree.getPathForRow(newIndex), true);
                }
                ui.ensureRowsAreVisible(newIndex, newIndex);
                ui.lastSelectedRow = newIndex;
            }
        }

        private void traverse(JTree tree, QuaquaTreeUI ui, int direction,
                boolean changeSelection) {
            // disable moving of lead unless in discontiguous mode
            if (!changeSelection
                    && tree.getSelectionModel().getSelectionMode()
                    != TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
                changeSelection = true;
            }

            int rowCount;

            if ((rowCount = tree.getRowCount()) > 0) {
                int minSelIndex = ui.getLeadSelectionRow();
                int newIndex;

                if (minSelIndex == -1) {
                    newIndex = 0;
                } else {
                    /* Try and expand the node, otherwise go to next
                    node. */
                    if (direction == 1) {
                        if (!ui.isLeaf(minSelIndex)
                                && !tree.isExpanded(minSelIndex)) {
                            ui.toggleExpandState(ui.getPathForRow(tree, minSelIndex));
                            newIndex = -1;
                        } else {
                            newIndex = Math.min(minSelIndex + 1, rowCount - 1);
                        }
                    } /* Try to collapse node. */ else {
                        if (!ui.isLeaf(minSelIndex)
                                && tree.isExpanded(minSelIndex)) {
                            ui.toggleExpandState(ui.getPathForRow(tree, minSelIndex));
                            newIndex = -1;
                        } else {
                            TreePath path = ui.getPathForRow(tree,
                                    minSelIndex);

                            if (path != null && path.getPathCount() > 1) {
                                newIndex = ui.getRowForPath(tree, path.getParentPath());
                            } else {
                                newIndex = -1;
                            }
                        }
                    }
                }
                if (newIndex != -1) {
                    if (changeSelection) {
                        tree.setSelectionInterval(newIndex, newIndex);
                    } else {
                        ui.setLeadSelectionPath(ui.getPathForRow(
                                tree, newIndex), true);
                    }
                    ui.ensureRowsAreVisible(newIndex, newIndex);
                }
            }
        }

        private void moveSelectionToParent(JTree tree, QuaquaTreeUI ui) {
            int selRow = ui.getLeadSelectionRow();
            TreePath path = ui.getPathForRow(tree, selRow);
            if (path != null && path.getPathCount() > 1) {
                int newIndex = ui.getRowForPath(tree, path.getParentPath());
                if (newIndex != -1) {
                    tree.setSelectionInterval(newIndex, newIndex);
                    ui.ensureRowsAreVisible(newIndex, newIndex);
                }
            }
        }

        private void page(JTree tree, QuaquaTreeUI ui, int direction,
                boolean addToSelection, boolean changeSelection) {

            // disable moving of lead unless in discontiguous mode
            if (!addToSelection && !changeSelection
                    && tree.getSelectionModel().getSelectionMode()
                    != TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
                changeSelection = true;
            }

            if (ui.getRowCount(tree) > 0
                    && ui.treeSelectionModel != null) {
                Dimension maxSize = tree.getSize();
                TreePath lead = ui.getLeadSelectionPath();
                TreePath newPath;
                Rectangle visRect = tree.getVisibleRect();

                if (direction == -1) {
                    // up.
                    newPath = ui.getClosestPathForLocation(tree, visRect.x,
                            visRect.y);
                    if (newPath.equals(lead)) {
                        visRect.y = Math.max(0, visRect.y - visRect.height);
                        newPath = tree.getClosestPathForLocation(visRect.x,
                                visRect.y);
                    }
                } else {
                    // down
                    visRect.y = Math.min(maxSize.height, visRect.y
                            + visRect.height - 1);
                    newPath = tree.getClosestPathForLocation(visRect.x,
                            visRect.y);
                    if (newPath.equals(lead)) {
                        visRect.y = Math.min(maxSize.height, visRect.y
                                + visRect.height - 1);
                        newPath = tree.getClosestPathForLocation(visRect.x,
                                visRect.y);
                    }
                }
                Rectangle newRect = ui.getPathBounds(tree, newPath);

                newRect.x = visRect.x;
                newRect.width = visRect.width;
                if (direction == -1) {
                    newRect.height = visRect.height;
                } else {
                    newRect.y -= (visRect.height - newRect.height);
                    newRect.height = visRect.height;
                }

                if (addToSelection) {
                    ui.extendSelection(newPath);
                } else if (changeSelection) {
                    tree.setSelectionPath(newPath);
                } else {
                    ui.setLeadSelectionPath(newPath, true);
                }
                tree.scrollRectToVisible(newRect);
            }
        }

        private void home(JTree tree, QuaquaTreeUI ui, int direction,
                boolean addToSelection, boolean changeSelection) {

            // disable moving of lead unless in discontiguous mode
            if (!addToSelection && !changeSelection
                    && tree.getSelectionModel().getSelectionMode()
                    != TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
                changeSelection = true;
            }

            int rowCount = ui.getRowCount(tree);

            if (rowCount > 0) {
                if (direction == -1) {
                    ui.ensureRowsAreVisible(0, 0);
                    if (addToSelection) {
                        TreePath aPath = ui.getAnchorSelectionPath();
                        int aRow = (aPath == null) ? -1 : ui.getRowForPath(tree, aPath);

                        if (aRow == -1) {
                            tree.setSelectionInterval(0, 0);
                        } else {
                            tree.setSelectionInterval(0, aRow);
                            ui.setAnchorSelectionPath(aPath);
                            ui.setLeadSelectionPath(ui.getPathForRow(tree, 0));
                        }
                    } else if (changeSelection) {
                        tree.setSelectionInterval(0, 0);
                    } else {
                        ui.setLeadSelectionPath(ui.getPathForRow(tree, 0),
                                true);
                    }
                } else {
                    ui.ensureRowsAreVisible(rowCount - 1, rowCount - 1);
                    if (addToSelection) {
                        TreePath aPath = ui.getAnchorSelectionPath();
                        int aRow = (aPath == null) ? -1 : ui.getRowForPath(tree, aPath);

                        if (aRow == -1) {
                            tree.setSelectionInterval(rowCount - 1,
                                    rowCount - 1);
                        } else {
                            tree.setSelectionInterval(aRow, rowCount - 1);
                            ui.setAnchorSelectionPath(aPath);
                            ui.setLeadSelectionPath(ui.getPathForRow(tree,
                                    rowCount - 1));
                        }
                    } else if (changeSelection) {
                        tree.setSelectionInterval(rowCount - 1, rowCount - 1);
                    } else {
                        ui.setLeadSelectionPath(ui.getPathForRow(tree,
                                rowCount - 1), true);
                    }
                }
            }
        }
    }

    /**
     * <code>TreeTraverseAction</code> is the action used for left/right keys.
     * Will toggle the expandedness of a node, as well as potentially
     * incrementing the selection.
     */
    public class TreeTraverseAction extends AbstractAction {

        /** Determines direction to traverse, 1 means expand, -1 means
         * collapse. */
        protected int direction;
        /** True if the selection is reset, false means only the lead leadPath
         * changes. */
        private boolean changeSelection;

        public TreeTraverseAction(int direction, String name) {
            this(direction, name, true);
        }

        private TreeTraverseAction(int direction, String name,
                boolean changeSelection) {
            this.direction = direction;
            this.changeSelection = changeSelection;
        }

        public void actionPerformed(ActionEvent e) {
            if (tree != null) {
                SHARED_ACTION.traverse(tree, QuaquaTreeUI.this, direction,
                        changeSelection);
            }
        }

        @Override
        public boolean isEnabled() {
            return (tree != null
                    && tree.isEnabled());
        }
    } // BasicTreeUI.TreeTraverseAction

    /** TreePageAction handles page up and page down events.
     */
    public class TreePageAction extends AbstractAction {

        /** Specifies the direction to adjust the selection by. */
        protected int direction;
        /** True indicates should set selection from anchor leadPath. */
        private boolean addToSelection;
        private boolean changeSelection;

        public TreePageAction(int direction, String name) {
            this(direction, name, false, true);
        }

        private TreePageAction(int direction, String name,
                boolean addToSelection,
                boolean changeSelection) {
            this.direction = direction;
            this.addToSelection = addToSelection;
            this.changeSelection = changeSelection;
        }

        public void actionPerformed(ActionEvent e) {
            if (tree != null) {
                SHARED_ACTION.page(tree, QuaquaTreeUI.this, direction,
                        addToSelection, changeSelection);
            }
        }

        @Override
        public boolean isEnabled() {
            return (tree != null
                    && tree.isEnabled());
        }
    } // BasicTreeUI.TreePageAction

    /** TreeIncrementAction is used to handle up/down actions.  Selection
     * is moved up or down based on direction.
     */
    public class TreeIncrementAction extends AbstractAction {

        /** Specifies the direction to adjust the selection by. */
        protected int direction;
        /** If true the new item is added to the selection, if false the
         * selection is reset. */
        private boolean addToSelection;
        private boolean changeSelection;

        public TreeIncrementAction(int direction, String name) {
            this(direction, name, false, true);
        }

        private TreeIncrementAction(int direction, String name,
                boolean addToSelection,
                boolean changeSelection) {
            this.direction = direction;
            this.addToSelection = addToSelection;
            this.changeSelection = changeSelection;
        }

        public void actionPerformed(ActionEvent e) {
            if (tree != null) {
                SHARED_ACTION.increment(tree, QuaquaTreeUI.this, direction,
                        addToSelection, changeSelection);
            }
        }

        @Override
        public boolean isEnabled() {
            return (tree != null
                    && tree.isEnabled());
        }
    } // End of class BasicTreeUI.TreeIncrementAction

    /**
     * TreeHomeAction is used to handle end/home actions.
     * Scrolls either the first or last cell to be visible based on
     * direction.
     */
    public class TreeHomeAction extends AbstractAction {

        protected int direction;
        /** Set to true if append to selection. */
        private boolean addToSelection;
        private boolean changeSelection;

        public TreeHomeAction(int direction, String name) {
            this(direction, name, false, true);
        }

        private TreeHomeAction(int direction, String name,
                boolean addToSelection,
                boolean changeSelection) {
            this.direction = direction;
            this.changeSelection = changeSelection;
            this.addToSelection = addToSelection;
        }

        public void actionPerformed(ActionEvent e) {
            if (tree != null) {
                SHARED_ACTION.home(tree, QuaquaTreeUI.this, direction,
                        addToSelection, changeSelection);
            }
        }

        @Override
        public boolean isEnabled() {
            return (tree != null
                    && tree.isEnabled());
        }
    } // End of class BasicTreeUI.TreeHomeAction

    /**
     * For the first selected row expandedness will be toggled.
     */
    public class TreeToggleAction extends AbstractAction {

        public TreeToggleAction(String name) {
        }

        public void actionPerformed(ActionEvent e) {
            if (tree != null) {
                SHARED_ACTION.toggle(tree, QuaquaTreeUI.this);
            }
        }

        @Override
        public boolean isEnabled() {
            return (tree != null
                    && tree.isEnabled());
        }
    } // End of class BasicTreeUI.TreeToggleAction

    /**
     * ActionListener that invokes cancelEditing when action performed.
     */
    public class TreeCancelEditingAction extends AbstractAction {

        public TreeCancelEditingAction(String name) {
        }

        public void actionPerformed(ActionEvent e) {
            if (tree != null) {
                SHARED_ACTION.cancelEditing(tree, QuaquaTreeUI.this);
            }
        }

        @Override
        public boolean isEnabled() {
            return (tree != null
                    && tree.isEnabled()
                    && isEditing(tree));
        }
    } // End of class BasicTreeUI.TreeCancelEditingAction
}
