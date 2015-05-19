/*
 * @(#)SidebarListModel.java  3.0.3  2008-04-17
 *
 * Copyright (c) 2004-2013 Werner Randelshofer, Switzerland.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package ch.randelshofer.quaqua.panther.filechooser;

import ch.randelshofer.quaqua.osx.OSXFile;
import ch.randelshofer.quaqua.filechooser.*;
import ch.randelshofer.quaqua.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.io.*;
import java.util.*;
import ch.randelshofer.quaqua.*;
import ch.randelshofer.quaqua.ext.base64.*;
import ch.randelshofer.quaqua.ext.nanoxml.*;

/**
 * This is the list model used to display a sidebar in the PantherFileChooserUI.
 * The list consists of two parts: the system items and the user items.
 * The user items are read from the file "~/Library/Preferences/com.apple.sidebarlists.plist".
 * The system items is the contents of the "/Volumes" directory plus the
 * "/Networks" directory.
 * <p>
 * Each element of the SidebarListModel implements the interface FileInfo.
 *
 *
 * @author  Werner Randelshofer
 * @version $Id: SidebarListModel.java 464 2014-03-22 12:32:00Z wrandelshofer $
 */
public class SidebarListModel
        extends AbstractListModel
        implements TreeModelListener {

    private final static boolean DEBUG = false;
    /**
     * This file contains information about the system list and holds the aliases
     * for the user list.
     */
    private final static File sidebarFile = new File(QuaquaManager.getProperty("user.home"), "Library/Preferences/com.apple.sidebarlists.plist");
    /**
     * Holds the tree path to the /Volumes folder.
     */
    private TreePath path;
    /**
     * Holds the FileSystemTreeModel.
     */
    private TreeModel model;
    /**
     * The JFileChooser.
     */
    private JFileChooser fileChooser;
    /**
     * Sequential dispatcher for the lazy creation of icons.
     */
    private SequentialDispatcher dispatcher = new SequentialDispatcher();
    /**
     * Set this to true, if the computer shall be listed in the sidebar.
     */
    private boolean isComputerVisible;
    private final static File[] defaultUserItems;

    static {
        if (QuaquaManager.getProperty("os.name").equals("Mac OS X")) {
            defaultUserItems = new File[]{
                        null, // null is used to specify a divider
                        new File(QuaquaManager.getProperty("user.home"), "Desktop"),
                        new File(QuaquaManager.getProperty("user.home"), "Documents"),
                        new File(QuaquaManager.getProperty("user.home"))
                    };
        } else if (QuaquaManager.getProperty("os.name").startsWith("Windows")) {
            defaultUserItems = new File[]{
                        null, // null is used to specify a divider
                        new File(QuaquaManager.getProperty("user.home"), "Desktop"),
                        // Japanese ideographs for Desktop:
                        new File(QuaquaManager.getProperty("user.home"), "\u684c\u9762"),
                        new File(QuaquaManager.getProperty("user.home"), "My Documents"),
                        new File(QuaquaManager.getProperty("user.home"))
                    };
        } else {
            defaultUserItems = new File[]{
                        null, // null is used to specify a divider
                        new File(QuaquaManager.getProperty("user.home"))
                    };
        }
    }
    /**
     * This array list holds the user items.
     */
    private ArrayList userItems = new ArrayList();
    /**
     * This array holds the view to model mapping of the system items.
     */
    private Row[] viewToModel = new Row[0];
    /**
     * This array holds the model to view mapping of the system items.
     */
    private int[] modelToView = new int[0];
    /**
     * This hash map is used to determine the sequence and visibility of the
     * items in the system list.
     * HashMap&lt;String,SystemItemInfo&gt;
     */
    private HashMap systemItemsMap = new HashMap();

    private static class SystemItemInfo {
        String name = "";
        int sequenceNumber = 0;
        boolean isVisible = true;
    }
    /**
     * Intervals between validations.
     */
    private final static long VALIDATION_TTL = 60000;
    /**
     * Time for next validation of the model.
     */
    private long bestBefore;

    /** Creates a new instance. */
    public SidebarListModel(JFileChooser fileChooser, TreePath path, TreeModel model) {
        this.fileChooser = fileChooser;
        this.path = path;
        this.model = model;
        model.addTreeModelListener(this);
        sortSystemItems();
        validate();
    }

    public void dispose() {
        model.removeTreeModelListener(this);
    }

    public int getSize() {
        return (isComputerVisible)
                ? 1 + viewToModel.length + userItems.size()
                : viewToModel.length + userItems.size();
    }

    private void sortSystemItems() {
        FileSystemTreeModel.Node parent = (FileSystemTreeModel.Node) path.getLastPathComponent();
        if (modelToView.length != parent.getChildCount()) {
            viewToModel = new Row[parent.getChildCount()];
            modelToView = new int[viewToModel.length];
        }
        for (int i = 0; i < viewToModel.length; i++) {
            viewToModel[i] = new Row(i);
        }
        Arrays.sort(viewToModel);
        for (int i = 0; i < viewToModel.length; i++) {
            modelToView[viewToModel[i].modelIndex] = i;
        }

        // remove leaf nodes from system items
        int j = 0;
        for (int i = 0; i < viewToModel.length; i++) {
            FileSystemTreeModel.Node node = (FileSystemTreeModel.Node) parent.getChildAt(viewToModel[i].modelIndex);
            if (!node.isLeaf()) {
                viewToModel[j] = viewToModel[i];
                modelToView[viewToModel[j].modelIndex] = i;
                j++;
            }
        }
        if (j < viewToModel.length) {
            Row[] helper = new Row[j];
            System.arraycopy(viewToModel, 0, helper, 0, j);
            viewToModel = helper;
        }
    }

    public Object getElementAt(int row) {
        if (isComputerVisible) {
            if (row == 0) {
                return path.getPathComponent(0);

            } else if (row <= viewToModel.length) {
                return ((FileSystemTreeModel.Node) model.getChild(path.getLastPathComponent(), viewToModel[row - 1].modelIndex));

            } else {
                return userItems.get(row - viewToModel.length - 1);

            }
        } else {
            return (row < viewToModel.length)
                    ? ((FileSystemTreeModel.Node) model.getChild(path.getLastPathComponent(), viewToModel[row].modelIndex))
                    : userItems.get(row - viewToModel.length);
        }
    }

    public void treeNodesChanged(TreeModelEvent e) {
        if (e.getTreePath().equals(path)) {
            int[] indices = e.getChildIndices();
            fireContentsChanged(this, modelToView[indices[0]], modelToView[indices[indices.length - 1]]);
        }
    }

    public void treeNodesInserted(TreeModelEvent e) {
        if (e.getTreePath().equals(path)) {
            sortSystemItems();

            int[] indices = e.getChildIndices();
            for (int i = 0; i < indices.length; i++) {
                int index = modelToView[indices[i]];
                fireIntervalAdded(this, index, index);
            }
        }
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        if (e.getTreePath().equals(path)) {
            int[] indices = e.getChildIndices();
            int[] oldModelToView = (int[]) modelToView.clone();

            sortSystemItems();

            for (int i = 0; i < indices.length; i++) {
                int index = oldModelToView[indices[i]];
                int offset = 0;
                for (int j = 0; j < i; j++) {
                    if (oldModelToView[indices[i]] < index) {
                        offset++;
                    }
                }
                fireIntervalRemoved(this, index - offset, index - offset);
            }
        }
    }

    public void treeStructureChanged(TreeModelEvent e) {
        if (e.getTreePath().equals(path)) {
            sortSystemItems();
            fireContentsChanged(this, 0, getSize() - 1);
        }
    }

    private class FileItem implements FileInfo {

        private File file;
        private Icon icon;
        private String userName;
        private boolean isTraversable;
        /**
         * Holds a Finder label for the file represented by this node.
         * The label is a value in the interval from 0 through 7.
         * The value -1 is used, if the label could not be determined.
         */
        protected int fileLabel = -1;

        public FileItem(File file) {
            this.file = file;

            userName = fileChooser.getName(file);
            isTraversable = true;
            //isTraversable = file.isDirectory();
        }

        public File lazyGetResolvedFile() {
            return file;
        }

        public File getResolvedFile() {
            return file;
        }

        public File getFile() {
            return file;
        }

        public String getFileKind() {
            return null;
        }

        public int getFileLabel() {
            return -1;
        }

        public long getFileLength() {
            return -1;
        }

        public Icon getIcon() {
            if (icon == null) {
                icon = (isTraversable())
                        ? UIManager.getIcon("FileView.directoryIcon")
                        : UIManager.getIcon("FileView.fileIcon");
                //
                if (!UIManager.getBoolean("FileChooser.speed")) {
                    dispatcher.dispatch(new Worker<Icon>() {

                        public Icon construct() {
                            return fileChooser.getIcon(file);
                        }

                        @Override
                        public void done(Icon value) {
                            icon = value;
                            SidebarListModel.this.fireContentsChanged(SidebarListModel.this, 0, SidebarListModel.this.getSize() - 1);
                        }
                    });
                }
            }
            return icon;
        }

        public String getUserName() {
            /*
            if (userName == null) {
            userName = fileChooser.getName(file);
            }*/
            return userName;
        }

        public boolean isTraversable() {
            return isTraversable;
        }

        public boolean isAcceptable() {
            return true;
        }

        public boolean isValidating() {
            return false;
        }

        public boolean isHidden() {
            return false;
        }
    }

    /**
     * An AliasItem is resolved as late as possible.
     */
    private class AliasItem implements FileInfo {

        private byte[] serializedAlias;
        private File file;
        private Icon icon;
        private String userName;
        private String aliasName;
        private boolean isTraversable;
        /**
         * Holds a Finder label for the file represented by this node.
         * The label is a value in the interval from 0 through 7.
         * The value -1 is used, if the label could not be determined.
         */
        protected int fileLabel = -1;

        public AliasItem(byte[] serializedAlias, String aliasName) {
            this.file = null;
            this.aliasName = aliasName;
            this.serializedAlias = serializedAlias;
            isTraversable = true;
        }

        public File lazyGetResolvedFile() {
            return getResolvedFile();
        }

        public File getResolvedFile() {
            if (file == null) {
                icon = null; // clear cached icon!
                file = OSXFile.resolveAlias(serializedAlias, false);
            }
            return file;
        }

        public File getFile() {
            return file;
        }

        public String getFileKind() {
            return null;
        }

        public int getFileLabel() {
            return -1;
        }

        public long getFileLength() {
            return -1;
        }

        public Icon getIcon() {
            if (icon == null) {
                icon = (isTraversable())
                        ? UIManager.getIcon("FileView.directoryIcon")
                        : UIManager.getIcon("FileView.fileIcon");
                //
                if (file != null && !UIManager.getBoolean("FileChooser.speed")) {
                    dispatcher.dispatch(new Worker<Icon>() {

                        public Icon construct() {
                            return fileChooser.getIcon(file);
                        }

                        @Override
                        public void done(Icon value) {
                            icon = value;
                            SidebarListModel.this.fireContentsChanged(SidebarListModel.this, 0, SidebarListModel.this.getSize() - 1);
                        }
                    });
                }
            }
            return icon;
        }

        public String getUserName() {
            if (userName == null) {
                if (file != null) {
                    userName = fileChooser.getName(file);
                }
            }
            return (userName == null) ? aliasName : userName;
        }

        public boolean isTraversable() {
            return isTraversable;
        }

        public boolean isAcceptable() {
            return true;
        }

        public boolean isValidating() {
            return false;
        }

        public boolean isHidden() {
            return false;
        }
    }

    /**
     * Validates the model if needed.
     */
    public void lazyValidate() {
        if (bestBefore < System.currentTimeMillis()) {
            validate();
        }
    }

    /**
     * Immediately validates the model.
     */
    private void validate() {
        // Prevent multiple invocations of this method by lazyValidate(),
        // while we are validating;
        bestBefore = Long.MAX_VALUE;

        dispatcher.dispatch(
                new Worker<Object[]>() {

                    public Object[] construct() throws IOException {
                        return read();
                    }

                    @Override
                    public void done(Object[] value) {
                        ArrayList freshUserItems;
                        systemItemsMap = (HashMap) value[0];
                        freshUserItems = (ArrayList) value[1];
                        freshUserItems.add(0, null);
                        update(freshUserItems);
                    }

                    @Override
                    public void failed(Throwable value) {
                        ArrayList freshUserItems;
                        freshUserItems = new ArrayList(defaultUserItems.length);
                        for (int i = 0; i < defaultUserItems.length; i++) {
                            if (defaultUserItems[i] == null) {
                                freshUserItems.add(null);
                            } else if (defaultUserItems[i].exists()) {
                                freshUserItems.add(new FileItem(defaultUserItems[i]));
                            }
                        }
                        update(freshUserItems);
                    }

                    private void update(ArrayList freshUserItems) {
                        int systemItemsSize = model.getChildCount(path.getLastPathComponent());
                        int oldUserItemsSize = userItems.size();
                        userItems.clear();
                        if (oldUserItemsSize > 0) {
                            fireIntervalRemoved(
                                    SidebarListModel.this,
                                    systemItemsSize,
                                    systemItemsSize + oldUserItemsSize - 1);
                        }
                        userItems = freshUserItems;
                        if (userItems.size() > 0) {
                            if (DEBUG) {
                                System.out.println("SidebarListModel.fireIntervalAdded " + systemItemsSize + ".." + (systemItemsSize + +userItems.size() - 1) + ", list size=" + getSize());
                            }
                            fireIntervalAdded(
                                    SidebarListModel.this,
                                    systemItemsSize,
                                    systemItemsSize + userItems.size() - 1);
                        }
                        bestBefore = System.currentTimeMillis() + VALIDATION_TTL;
                    }
                });
    }

    /**
     * Reads the sidebar preferences file.
     */
    private Object[] read() throws IOException {
        if (!OSXFile.canWorkWithAliases()) {
            throw new IOException("Unable to work with aliases");
        }

        HashMap sysItemsMap = new HashMap();
        ArrayList usrItems = new ArrayList();

        FileReader reader = null;
        try {
            reader = new FileReader(sidebarFile);
            XMLElement xml = new XMLElement(new HashMap(), false, false);
            try {
                xml.parseFromReader(reader);
            } catch (XMLParseException e) {
                xml = new BinaryPListParser().parse(sidebarFile);
            }
            String key2 = "", key3 = "", key5 = "";
            for (Iterator i0 = xml.iterateChildren(); i0.hasNext();) {
                XMLElement xml1 = (XMLElement) i0.next();

                for (Iterator i1 = xml1.iterateChildren(); i1.hasNext();) {
                    XMLElement xml2 = (XMLElement) i1.next();

                    if (xml2.getName().equals("key")) {
                        key2 = xml2.getContent();
                    }

                    if (xml2.getName().equals("dict") && key2.equals("systemitems")) {
                        for (Iterator i2 = xml2.iterateChildren(); i2.hasNext();) {
                            XMLElement xml3 = (XMLElement) i2.next();
                            if (xml3.getName().equals("key")) {
                                key3 = xml3.getContent();
                            }
                            if (xml3.getName().equals("array") && key3.equals("VolumesList")) {
                                for (Iterator i3 = xml3.iterateChildren(); i3.hasNext();) {
                                    XMLElement xml4 = (XMLElement) i3.next();

                                    if (xml4.getName().equals("dict")) {
                                        SystemItemInfo info = new SystemItemInfo();
                                        for (Iterator i4 = xml4.iterateChildren(); i4.hasNext();) {
                                            XMLElement xml5 = (XMLElement) i4.next();

                                            if (xml5.getName().equals("key")) {
                                                key5 = xml5.getContent();
                                            }

                                            info.sequenceNumber = sysItemsMap.size();
                                            if (xml5.getName().equals("string") && key5.equals("Name")) {
                                                info.name = xml5.getContent();
                                            }
                                            if (xml5.getName().equals("string") && key5.equals("Visibility")) {
                                                info.isVisible = xml5.getContent().equals("AlwaysVisible");
                                            }
                                        }
                                        if (info.name != null) {
                                            sysItemsMap.put(info.name, info);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (xml2.getName().equals("dict") && key2.equals("useritems")) {
                        for (Iterator i2 = xml2.iterateChildren(); i2.hasNext();) {
                            XMLElement xml3 = (XMLElement) i2.next();
                            for (Iterator i3 = xml3.iterateChildren(); i3.hasNext();) {
                                XMLElement xml4 = (XMLElement) i3.next();
                                String aliasName = null;
                                byte[] serializedAlias = null;
                                for (Iterator i4 = xml4.iterateChildren(); i4.hasNext();) {
                                    XMLElement xml5 = (XMLElement) i4.next();

                                    if (xml5.getName().equals("key")) {
                                        key5 = xml5.getContent();
                                    }
                                    if (xml5.getName().equals("string") && key5.equals("Name")) {
                                        aliasName = xml5.getContent();
                                    }
                                    if (!xml5.getName().equals("key") && key5.equals("Alias")) {
                                        serializedAlias = Base64.decode(xml5.getContent());
                                    }
                                }
                                if (serializedAlias != null && aliasName != null) {
                                    // Try to resolve the alias without user interaction
                                    File f = OSXFile.resolveAlias(serializedAlias, true);
                                    if (f != null) {
                                        usrItems.add(new FileItem(f));
                                    } else {
                                        usrItems.add(new AliasItem(serializedAlias, aliasName));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return new Object[]{sysItemsMap, usrItems};
    }

    // Helper classes
    private class Row implements Comparable {

        private int modelIndex;

        public Row(int index) {
            this.modelIndex = index;
        }

        public int compareTo(Object o) {
            int row1 = modelIndex;
            int row2 = ((Row) o).modelIndex;

            FileSystemTreeModel.Node o1 = ((FileSystemTreeModel.Node) model.getChild(path.getLastPathComponent(), row1));
            FileSystemTreeModel.Node o2 = ((FileSystemTreeModel.Node) model.getChild(path.getLastPathComponent(), row2));

            SystemItemInfo i1 = (SystemItemInfo) systemItemsMap.get(o1.getUserName());
            if (i1 == null && o1.getResolvedFile().getName().equals("")) {
                i1 = (SystemItemInfo) systemItemsMap.get("Computer");
            }

            SystemItemInfo i2 = (SystemItemInfo) systemItemsMap.get(o2.getUserName());
            if (i2 == null && o2.getResolvedFile().getName().equals("")) {
                i2 = (SystemItemInfo) systemItemsMap.get("Computer");
            }

            if (i1 != null && i2 != null) {
                return i1.sequenceNumber - i2.sequenceNumber;
            }

            if (i1 != null) {
                return -1;
            }
            if (i2 != null) {
                return 1;
            }

            return row1 - row2;
        }

        @Override
        public int hashCode() {
            int row1 = modelIndex;

            FileSystemTreeModel.Node o1 = ((FileSystemTreeModel.Node) model.getChild(path.getLastPathComponent(), row1));

            SystemItemInfo i1 = (SystemItemInfo) systemItemsMap.get(o1.getUserName());
            if (i1 == null && o1.getResolvedFile().getName().equals("")) {
                i1 = (SystemItemInfo) systemItemsMap.get("Computer");
            }

            if (i1 != null) {
                return i1.sequenceNumber;
            }

            return row1;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Row)//
                    ? compareTo(o)==0
                    : false;
        }
    }
}
