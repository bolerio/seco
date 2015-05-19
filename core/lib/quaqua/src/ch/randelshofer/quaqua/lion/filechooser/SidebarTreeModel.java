/*
 * @(#)SidebarTreeModel.java  
 *
 * Copyright (c) 2011-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Werner Randelshofer. For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.lion.filechooser;

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
 * SidebarTreeModel.
 *
 * @author Werner Randelshofer
 * @version $Id: SidebarTreeModel.java 464 2014-03-22 12:32:00Z wrandelshofer $
 */
public class SidebarTreeModel extends DefaultTreeModel implements TreeModelListener {

    /**
     * This file contains information about the system list and holds the aliases
     * for the user list.
     */
    private final static File sidebarFile = new File(QuaquaManager.getProperty("user.home"), "Library/Preferences/com.apple.sidebarlists.plist");
    /**
     * Holds the tree volumesPath to the /Volumes folder.
     */
    private TreePath volumesPath;
    /**
     * Holds the FileSystemTreeModel.
     */
    private TreeModel model;
    /**
     * Represents the "Devices" node in the sidebar.
     */
    private DefaultMutableTreeNode devicesNode;
    /**
     * Represents the "Places" node in the sidebar.
     */
    private DefaultMutableTreeNode favoritesNode;
    /**
     * Intervals between validations.
     */
    private final static long VALIDATION_TTL = 60000;
    /**
     * Time for next validation of the model.
     */
    private long bestBefore;
    /**
     * The JFileChooser.
     */
    private JFileChooser fileChooser;
    /**
     * Sequential dispatcher for the lazy creation of icons.
     */
    private SequentialDispatcher dispatcher = new SequentialDispatcher();
    /**
     * This hash map is used to determine the sequence and visibility of the
     * items in the system list.
     * HashMap&lt;String,SystemItemInfo&gt;
     */
    private HashMap systemItemsMap = new HashMap();
    /**
     * The defaultUserItems are used when we fail to read the user items from
     * the sidebarFile.
     */
    private final static File[] defaultUserItems;

    static {
        if (QuaquaManager.isOSX()
                || QuaquaManager.getOS() == QuaquaManager.DARWIN) {
            defaultUserItems = new File[]{
                        new File(QuaquaManager.getProperty("user.home"), "Desktop"),
                        new File(QuaquaManager.getProperty("user.home"), "Documents"),
                        new File(QuaquaManager.getProperty("user.home"))
                    };
        } else if (QuaquaManager.getOS() == QuaquaManager.WINDOWS) {
            defaultUserItems = new File[]{
                        new File(QuaquaManager.getProperty("user.home"), "Desktop"),
                        // Japanese ideographs for Desktop:
                        new File(QuaquaManager.getProperty("user.home"), "\u684c\u9762"),
                        new File(QuaquaManager.getProperty("user.home"), "My Documents"),
                        new File(QuaquaManager.getProperty("user.home"))
                    };
        } else if (QuaquaManager.getOS() == QuaquaManager.LINUX) {
            defaultUserItems = new File[]{
                        new File(QuaquaManager.getProperty("user.home"), "Desktop"),
                        new File("/media"),
                        new File(QuaquaManager.getProperty("user.home"), "Documents"),
                        new File(QuaquaManager.getProperty("user.home"))
                    };
        } else {
            defaultUserItems = new File[]{
                        new File(QuaquaManager.getProperty("user.home"))
                    };
        }
    }

    /** Creates a new instance. */
    public SidebarTreeModel(JFileChooser fileChooser, TreePath path, TreeModel model) {
        super(new DefaultMutableTreeNode(), true);

        this.fileChooser = fileChooser;
        this.volumesPath = path;
        this.model = model;

        devicesNode = new DefaultMutableTreeNode(UIManager.getString("FileChooser.devices"));
        devicesNode.setAllowsChildren(true);
        favoritesNode = new DefaultMutableTreeNode(UIManager.getString("FileChooser.favorites"));
        favoritesNode.setAllowsChildren(true);

        DefaultMutableTreeNode r = (DefaultMutableTreeNode) getRoot();
        r.add(favoritesNode);
        r.add(devicesNode);

        validate();
        updateDevicesNode();

        model.addTreeModelListener(this);
    }

    public void lazyValidate() {
        // throw new UnsupportedOperationException("Not yet implemented");
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
                        update(freshUserItems);
                    }

                    @Override
                    public void failed(Throwable value) {
                        ArrayList freshUserItems;

                        System.err.println("Warning: SidebarTreeModel uses default user items.");
                        freshUserItems = new ArrayList(defaultUserItems.length);
                        for (int i = 0; i < defaultUserItems.length; i++) {
                            if (defaultUserItems[i] == null) {
                                freshUserItems.add(null);
                            } else if (defaultUserItems[i].exists()) {
                                freshUserItems.add(new FileNode(defaultUserItems[i]));
                            }
                        }
                        update(freshUserItems);
                    }

                    private void update(ArrayList freshUserItems) {
                        int oldUserItemsSize = favoritesNode.getChildCount();
                        if (oldUserItemsSize > 0) {
                            int[] removedIndices = new int[oldUserItemsSize];
                            Object[] removedChildren = new Object[oldUserItemsSize];
                            for (int i = 0; i < oldUserItemsSize; i++) {
                                removedIndices[i] = i;
                                removedChildren[i] = favoritesNode.getChildAt(i);
                            }
                            favoritesNode.removeAllChildren();
                            fireTreeNodesRemoved(
                                    SidebarTreeModel.this,
                                    favoritesNode.getPath(),
                                    removedIndices,
                                    removedChildren);
                        }
                        if (freshUserItems.size() > 0) {
                            int[] insertedIndices = new int[freshUserItems.size()];
                            Object[] insertedChildren = new Object[freshUserItems.size()];
                            for (int i = 0; i < freshUserItems.size(); i++) {
                                insertedIndices[i] = i;
                                insertedChildren[i] = freshUserItems.get(i);
                                if (freshUserItems.get(i) == null) {
                                    favoritesNode.add(new DefaultMutableTreeNode("null?"));
                                } else {
                                    favoritesNode.add((DefaultMutableTreeNode) freshUserItems.get(i));
                                }
                            }
                            fireTreeNodesInserted(
                                    SidebarTreeModel.this,
                                    favoritesNode.getPath(),
                                    insertedIndices,
                                    insertedChildren);
                        }
                        bestBefore = System.currentTimeMillis() + VALIDATION_TTL;
                    }
                });
    }

    private void updateDevicesNode() {
        FileSystemTreeModel.Node modelDevicesNode = (FileSystemTreeModel.Node) volumesPath.getLastPathComponent();

        // Remove nodes from the view which are not present in the model
        for (int i = devicesNode.getChildCount() - 1; i >= 0; i--) {
            SidebarViewToModelNode viewNode = (SidebarViewToModelNode) devicesNode.getChildAt(i);
            if (viewNode.getTarget().getParent() != modelDevicesNode) {
                removeNodeFromParent(viewNode);
            }
        }

        // Add nodes to the view, wich are present in the model, but not
        // in the view. Only add non-leaf nodes
        for (int i = 0, n = modelDevicesNode.getChildCount(); i < n; i++) {
            FileSystemTreeModel.Node modelNode = (FileSystemTreeModel.Node) modelDevicesNode.getChildAt(i);
            if (!modelNode.isLeaf()) {
                boolean isInView = false;
                for (int j = 0, m = devicesNode.getChildCount(); j < m; j++) {
                    SidebarViewToModelNode viewNode = (SidebarViewToModelNode) devicesNode.getChildAt(j);
                    if (viewNode.getTarget() == modelNode) {
                        isInView = true;
                        break;
                    }
                }
                if (!isInView) {
                    SidebarViewToModelNode newNode = new SidebarViewToModelNode(modelNode);
                    int insertionIndex = 0;
                   SideBarViewToModelNodeComparator comparator=new SideBarViewToModelNodeComparator();
                    while (insertionIndex < devicesNode.getChildCount()
                            && comparator.compare((SidebarViewToModelNode) devicesNode.getChildAt(insertionIndex),newNode) < 0) {
                        insertionIndex++;
                    }
                    insertNodeInto(newNode, devicesNode, insertionIndex);
                }
            }
        }

        // Update the view
        if (devicesNode.getChildCount() > 0) {
            int[] childIndices = new int[devicesNode.getChildCount()];
            Object[] childNodes = new Object[devicesNode.getChildCount()];
            for (int i = 0; i < childIndices.length; i++) {
                childIndices[i] = i;
                childNodes[i] = devicesNode.getChildAt(i);
            }
            fireTreeNodesChanged(this, devicesNode.getPath(), childIndices, childNodes);
        }
    }

    /**
     * Reads the sidebar preferences file.
     */
    private Object[] read() throws IOException {
        if (!OSXFile.canWorkWithAliases()) {
            throw new IOException("Unable to work with aliases");
        }

        HashMap sysItemsMap = new HashMap();
        ArrayList userItems = new ArrayList();

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
                    if (xml2.getName().equals("dict") && key2.equals("favorites")) {
                        for (Iterator i2 = xml2.iterateChildren(); i2.hasNext();) {
                            XMLElement xml3 = (XMLElement) i2.next();
                            for (Iterator i3 = xml3.iterateChildren(); i3.hasNext();) {
                                XMLElement xml4 = (XMLElement) i3.next();
                                String aliasName = null;
                                int entryType=0;
                                byte[] serializedAlias = null;
                                boolean isVisible=true;
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
                                    if (key5.equals("EntryType")) {
                                        // EntryType marks items which have been added
                                        // by the System.
                                        try {
                                        entryType=Integer.parseInt(xml5.getContent());
                                        } catch (NumberFormatException e) {
                                            entryType=1;
                                        }
                                    }
                                    if (key5.equals("Visibility")) {
                                        if (xml5.getContent()!=null&&xml5.getContent().equals("NeverVisible")) {
                                            isVisible=false;
                                        }
                                        
                                    }
                                }
                                
                                if (serializedAlias != null && aliasName != null && entryType==0 && isVisible) {
                                    // Suppress the "All My Files" folder.
                                    if (aliasName.equals("All My Files")) continue;

                                    // Try to resolve the alias without user interaction
                                    File f = OSXFile.resolveAlias(serializedAlias, true);
                                    if (f != null) {
                                        userItems.add(new FileNode(f));
                                    } else {
                                        userItems.add(new AliasNode(serializedAlias, aliasName));
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
        return new Object[]{sysItemsMap, userItems};
    }

    public void treeNodesChanged(TreeModelEvent e) {
        if (e.getTreePath().equals(volumesPath)) {
            updateDevicesNode();
        }
    }

    public void treeNodesInserted(TreeModelEvent e) {
        if (e.getTreePath().equals(volumesPath)) {
            updateDevicesNode();
        }
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        if (e.getTreePath().equals(volumesPath)) {
            updateDevicesNode();
        }
    }

    public void treeStructureChanged(TreeModelEvent e) {
        if (e.getTreePath().equals(volumesPath)) {
            updateDevicesNode();
        }
    }

    private class FileNode extends Node {

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

        public FileNode(File file) {
            this.file = file;
            // userName = fileChooser.getName(file);
            isTraversable = true;
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

        public boolean allowsChildren() {
            return false;
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
                            int[] changedIndices = {getParent().getIndex(FileNode.this)};
                            Object[] changedChildren = {FileNode.this};
                            SidebarTreeModel.this.fireTreeNodesChanged(
                                    SidebarTreeModel.this,
                                    favoritesNode.getPath(),
                                    changedIndices, changedChildren);
                        }
                    });
                }
            }
            return icon;
        }

        public String getUserName() {
            if (userName == null) {
                userName = fileChooser.getName(file);
            }
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
    }

    /**
     * An AliasNode is resolved as late as possible.
     */
    private abstract class Node extends DefaultMutableTreeNode implements FileInfo {

        @Override
        public boolean getAllowsChildren() {
            return false;
        }

        public boolean isHidden() {
            return false;
        }
    }

    /**
     * An AliasNode is resolved as late as possible.
     */
    private class AliasNode extends Node {

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

        public AliasNode(byte[] serializedAlias, String aliasName) {
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
                // Note: We clear this icon, when we resolve the alias
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

                            int[] changedIndices = new int[]{getParent().getIndex(AliasNode.this)};
                            Object[] changedChildren = new Object[]{AliasNode.this};
                            SidebarTreeModel.this.fireTreeNodesChanged(
                                    SidebarTreeModel.this,
                                    ((DefaultMutableTreeNode) AliasNode.this.getParent()).getPath(),
                                    changedIndices, changedChildren);
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
    }

    private static class SystemItemInfo {

        String name = "";
        int sequenceNumber = 0;
        boolean isVisible = true;
    }



    /** Note: SidebaViewToModelNode must not implement Comparable and must
     * not override equals()/hashCode(), because this confuses the layout algorithm
     * in JTree.
     */
    private class SidebarViewToModelNode extends Node /*implements Comparable*/ {

        private FileSystemTreeModel.Node target;

        public SidebarViewToModelNode(FileSystemTreeModel.Node target) {
            this.target = target;
        }

        public File getFile() {
            return target.getFile();
        }

        public File getResolvedFile() {
            return target.getResolvedFile();
        }

        public File lazyGetResolvedFile() {
            return target.lazyGetResolvedFile();
        }

        public boolean isTraversable() {
            return target.isTraversable();
        }

        public boolean isAcceptable() {
            return target.isAcceptable();
        }

        public int getFileLabel() {
            return target.getFileLabel();
        }

        public String getUserName() {
            return target.getUserName();
        }

        public Icon getIcon() {
            return target.getIcon();
        }

        public long getFileLength() {
            return target.getFileLength();
        }

        public String getFileKind() {
            return target.getFileKind();
        }

        public boolean isValidating() {
            return target.isValidating();
        }

        public FileSystemTreeModel.Node getTarget() {
            return target;
        }

        @Override
        public String toString() {
            return target.toString();
        }
/*
        public int compareTo(Object o) {
            return compareTo((SidebarViewToModelNode) o);
        }

        public int compareTo(SidebarViewToModelNode that) {
            FileSystemTreeModel.Node o1 = this.getTarget();
            FileSystemTreeModel.Node o2 = that.getTarget();

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

            return 0;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof SidebarViewToModelNode) //
                    ? compareTo((SidebarViewToModelNode) o) == 0 //
                    : false;
        }

        @Override
        public int hashCode() {
            return getTarget() == null ? 0 : getTarget().getUserName().hashCode();
        }*/
    }
     private class SideBarViewToModelNodeComparator implements Comparator<SidebarViewToModelNode> {

        public int compare(SidebarViewToModelNode n1, SidebarViewToModelNode n2) {
            FileSystemTreeModel.Node o1 = n1.getTarget();
            FileSystemTreeModel.Node o2 = n2.getTarget();

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

            return 0;
        }

    }
}
