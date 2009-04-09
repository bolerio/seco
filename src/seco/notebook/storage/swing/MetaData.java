package seco.notebook.storage.swing;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import seco.notebook.storage.swing.types.AddOnFactory;

/*
 * Like the <code>Intropector</code>, the <code>MetaData</code> class
 * contains <em>meta</em> objects that describe the way
 * classes should express their state in terms of their
 * own public APIs.
 *
 * @see java.beans.Intropector
 *
 * @version 1.39 05/05/04
 * @author Philip Milne
 * @author Steve Langley
 */
public class MetaData
{

    private static final String PACKAGE_NAME = "seco.notebook.storage.swing.";
    private static Map<String, DefaultConverter> converters = new HashMap<String, DefaultConverter>();
    private static Map<String, Vector<String>> transientProperties = new HashMap<String, Vector<String>>();

    static
    {
        removeProperty("javax.swing.text.AbstractDocument", "documentProperties");
        removeProperty("javax.swing.text.DefaultCaret", "dot"); 
        removeProperty("javax.swing.JEditorPane", "focusTraversalPolicy");
        removeProperty("javax.swing.JEditorPane", "UI");
        removeProperty("javax.swing.JSplitPane", "component");
        removeProperty("javax.swing.JSplitPane", "nextFocusableComponent");
        removeProperty("javax.swing.JSplitPane", "UI");
        // TODO: throws IllegalArgExc if > 0 and set before label
        removeProperty("javax.swing.AbstractButton", "displayedMnemonicIndex");
        removeProperty("javax.swing.JList", "UI");
        removeProperty("javax.swing.JScrollBar", "UI");
        removeProperty("javax.swing.JScrollPane", "UI");
        // Transient properties
        // awt
        // Infinite graphs.
        removeProperty("java.awt.geom.RectangularShape", "frame");
        removeProperty("java.awt.Rectangle2D", "frame");
        removeProperty("java.awt.Rectangle", "frame");
        removeProperty("java.awt.geom.Rectangle2D", "frame");
        removeProperty("java.awt.geom.Rectangle2D.Double", "frame");
        removeProperty("java.awt.geom.Rectangle2D.Float", "frame");
        removeProperty("java.awt.Rectangle", "bounds");
        removeProperty("java.awt.Dimension", "size");
        removeProperty("java.awt.Point", "location");
        // The color and font properties in Component need special treatment,
        // see above.
        removeProperty("java.awt.Component", "foreground");
        // removeProperty("java.awt.Component", "background");
        removeProperty("java.awt.Component", "font");
        // The visible property of Component needs special treatment because of
        // Windows.
        removeProperty("java.awt.Component", "visible");
        // This property throws an exception if accessed when there is no child.
        removeProperty("java.awt.ScrollPane", "scrollPosition");
        // 4917458 this should be removed for XAWT since it may throw
        // an unsupported exception if there isn't any input methods.
        // This shouldn't be a problem since these are added behind
        // the scenes automatically.
        removeProperty("java.awt.im.InputContext", "compositionEnabled");
        // swing
        // The size properties in JComponent need special treatment, see above.
        removeProperty("javax.swing.JComponent", "minimumSize");
        removeProperty("javax.swing.JComponent", "preferredSize");
        removeProperty("javax.swing.JComponent", "maximumSize");
        
        removeProperty("javax.swing.JComponent", "actionMap");
        // These properties have platform specific implementations
        // and should not appear in archives.
        removeProperty("javax.swing.ImageIcon", "image");
        removeProperty("javax.swing.ImageIcon", "imageObserver");
        // This property throws an exception when set in JMenu.
        // PENDING: Note we must delete the property from
        // the superclass even though the superclass's
        // implementation does not throw an error.
        // This needs some more thought.
        removeProperty("javax.swing.JMenu", "accelerator");
        removeProperty("javax.swing.JMenu", "delay");
        // removeProperty("javax.swing.JMenuItem", "accelerator");
        // This property unconditionally throws a "not implemented" exception.
        removeProperty("javax.swing.JMenuBar", "helpMenu");
        // XXX
        removeProperty("javax.swing.JMenu", "UI");
        removeProperty("javax.swing.JMenuBar", "UI");
        removeProperty("javax.swing.JMenuBar", "layout");

        removeProperty("javax.swing.JMenuItem", "UI");
        removeProperty("javax.swing.JMenuItem", "layout");
        removeProperty("javax.swing.JPopupMenu", "UI");
        removeProperty("javax.swing.JSeparator", "UI");
        removeProperty("javax.swing.JPopupMenu", "layout");
        removeProperty("javax.swing.JButton", "UI");
        removeProperty("javax.swing.JToolBar", "UI");
        removeProperty("javax.swing.JComboBox", "UI");
        removeProperty("javax.swing.JToolBar", "layout");
        removeProperty("javax.swing.AbstractAction", "enabled");

        // The scrollBars in a JScrollPane are dynamic and should not
        // be archived. The row and columns headers are changed by
        // components like JTable on "addNotify".
        removeProperty("javax.swing.JScrollPane", "verticalScrollBar");
        removeProperty("javax.swing.JScrollPane", "horizontalScrollBar");
        removeProperty("javax.swing.JScrollPane", "rowHeader");
        removeProperty("javax.swing.JScrollPane", "columnHeader");
        removeProperty("javax.swing.JViewport", "extentSize");
        // Renderers need special treatment, since their properties
        // change during rendering.
        removeProperty("javax.swing.table.JTableHeader", "defaultRenderer");
        removeProperty("javax.swing.JList", "cellRenderer");
        removeProperty("javax.swing.JList", "selectedIndices");
        // The lead and anchor selection indexes are best ignored.
        // Selection is rarely something that should persist from
        // development to deployment.
        removeProperty("javax.swing.DefaultListSelectionModel",
                "leadSelectionIndex");
        removeProperty("javax.swing.DefaultListSelectionModel",
                "anchorSelectionIndex");
        // The selection must come after the text itself.
        removeProperty("javax.swing.JComboBox", "selectedIndex");
        removeProperty("javax.swing.JComboBox", "keySelectionManager");
        removeProperty("javax.swing.JComboBox", "editor");
        removeProperty("javax.swing.JComboBox", "renderer");
        removeProperty("javax.swing.JComboBox", "layout");
        // normally UI-s write here some non-persistent stuff..
        removeProperty("javax.swing.JComboBox", "component");
        // All selection information should come after the JTabbedPane is built
        removeProperty("javax.swing.JTabbedPane", "selectedIndex");
        removeProperty("javax.swing.JTabbedPane", "selectedComponent");
        // PENDING: The "disabledIcon" property is often computed from the icon
        // property.
        removeProperty("javax.swing.AbstractButton", "disabledIcon");
        removeProperty("javax.swing.JLabel", "disabledIcon");
        // The caret property throws errors when it it set beyond
        // the extent of the text. We could just set it after the
        // text, but this is probably not something we want to archive anyway.
        removeProperty("javax.swing.text.JTextComponent", "caret");
        removeProperty("javax.swing.text.JTextComponent", "caretPosition");
        // The selectionStart must come after the text itself.
        removeProperty("javax.swing.text.JTextComponent", "selectionStart");
        removeProperty("javax.swing.text.JTextComponent", "selectionEnd");

        // removeProperty("javax.swing.plaf.basic.LazyActionMap", "parent");

        removeProperty("javax.swing.JRootPane", "layout");
        removeProperty("javax.swing.JFrame", "layout");
        removeProperty("javax.swing.JFrame", "layeredPane");
        removeProperty("javax.swing.JFrame", "glassPane");
        removeProperty("javax.swing.JRootPane", "contentPane");
        removeProperty("javax.swing.JFrame", "menuBar");
        removeProperty("javax.swing.JRootPane", "JMenuBar");
        removeProperty("javax.swing.JRootPane", "layeredPane");
        removeProperty("javax.swing.JRootPane", "glassPane");

        // removeProperty("javax.swing.JFrame", "menuBar");

        registerConstructor("javax.swing.GroupLayout", new String[] { "host" },
                new Class[] { Container.class });

        registerConstructor("javax.swing.DefaultCellEditor",
                new String[] { "editorComponent" },
                new Class[] { JCheckBox.class });

        registerConstructor("javax.swing.Box",
                new String[] { "layoutMgr.axis" }, new Class[] { Integer.TYPE });
        registerFactoryConstructor(KeyStroke.class, KeyStroke.class,
                "getKeyStroke", new String[] { "keyCode", "modifiers" },
                new Class[] { Integer.TYPE, Integer.TYPE });
        registerFactoryConstructor(ToolTipManager.class, ToolTipManager.class,
                "sharedInstance", new String[0], new Class[0]);
        registerConstructor("javax.swing.plaf.basic.LazyActionMap",
                new String[] { "_loader" }, new Class[] { Class.class });

        registerConstructor("javax.swing.plaf.InsetsUIResource", new String[] {
                "top", "left", "bottom", "right" });
        
        registerConstructor("javax.swing.plaf.basic.BasicBorders$ButtonBorder",
                new String[] {"shadow", "darkShadow", "highlight", "lightHighlight"});
        registerConstructor("javax.swing.plaf.basic.BasicBorders$SplitPaneBorder",
                new String[] {"highlight", "shadow"});
        
        registerConstructor("java.awt.MenuShortcut", new String[] { "key",
                "usesShift" });
        registerConstructor("javax.swing.plaf.IconUIResource",
                new String[] { "delegate" });
        // Constructors.
        // util
        registerConstructor("java.util.Date", new String[] { "time" });
        // beans
        // TODO: should write converters fo these two if ever needed
        // registerConstructor("java.beans.Statement", new String[] { "target",
        // "methodName", "arguments" });
        // registerConstructor("java.beans.Expression", new String[] { "target",
        // "methodName", "arguments" });
        //
        registerConstructor("java.beans.EventHandler", new String[] { "target",
                "action", "eventPropertyName", "listenerMethodName" });
        // registerConstructor("java.beans.PropertyChangeListenerProxy",
        // new String[] { "propertyName", "listener"},
        // new Class[] { String.class, PropertyChangeListener.class });
        // awt
        registerConstructor("java.awt.Point", new String[] { "x", "y" });
        registerConstructor("java.awt.Dimension", new String[] { "width",
                "height" });
        registerConstructor("java.awt.Rectangle", new String[] { "x", "y",
                "width", "height" });
        registerConstructor("java.awt.Insets", new String[] { "top", "left",
                "bottom", "right" });
        registerConstructor("java.awt.Color", new String[] { "rGB" });
        // { "red", "green", "blue", "alpha" });
        registerConstructor("java.awt.Font", new String[] { "name", "style",
                "size" });
        registerConstructor("java.awt.Cursor", new String[] { "type" });
        // registerConstructor("java.awt.GridBagConstraints", new String[] {
        // "gridx", "gridy", "gridwidth", "gridheight", "weightx",
        // "weighty", "anchor", "fill", "insets", "ipadx", "ipady" });
        registerConstructor("java.awt.ScrollPane",
                new String[] { "scrollbarDisplayPolicy" });
        // swing
        registerConstructor("javax.swing.plaf.FontUIResource", new String[] {
                "name", "style", "size" });
        registerConstructor("javax.swing.plaf.ColorUIResource", new String[] {
                "red", "green", "blue" });
        registerConstructor("javax.swing.tree.DefaultTreeModel",
                new String[] { "root" }, new Class[] { TreeNode.class });
        registerConstructor("javax.swing.JTree", new String[] { "model" });
        registerConstructor("javax.swing.tree.TreePath",
                new String[] { "path" });
        registerConstructor("javax.swing.OverlayLayout",
                new String[] { "target" });
        registerConstructor("javax.swing.BoxLayout", new String[] { "target",
                "axis" });
        registerConstructor("javax.swing.Box$Filler", new String[] {
                "minimumSize", "preferredSize", "maximumSize" });
        
        // Try to synthesize the ImageIcon from its description.
        // XXX
        // registerConstructor("javax.swing.ImageIcon",
        // new String[] { "description" });
        // JButton's "label" and "actionCommand" properties are related,
        // use the label as a constructor argument to ensure that it is set
        // first.
        // This remove the benign, but unnecessary, manipulation of
        // actionCommand
        // property in the common case.
        registerConstructor("javax.swing.JButton", new String[] { "label" });
        // borders
        registerConstructor("javax.swing.border.BevelBorder", new String[] {
                "bevelType", "highlightOuter", "highlightInner", "shadowOuter",
                "shadowInner" });
        registerConstructor("javax.swing.plaf.BorderUIResource",
                new String[] { "delegate" });
        registerConstructor(
                "javax.swing.plaf.BorderUIResource$BevelBorderUIResource",
                new String[] { "bevelType", "highlightOuter", "highlightInner",
                        "shadowOuter", "shadowInner" });
        registerConstructor("javax.swing.border.CompoundBorder", new String[] {
                "outsideBorder", "insideBorder" });
        registerConstructor(
                "javax.swing.plaf.BorderUIResource$CompoundBorderUIResource",
                new String[] { "outsideBorder", "insideBorder" });
        registerConstructor("javax.swing.border.EmptyBorder", new String[] {
                "top", "left", "bottom", "right" });
        registerConstructor(
                "javax.swing.plaf.BorderUIResource$EmptyBorderUIResource",
                new String[] { "top", "left", "bottom", "right" });
        registerConstructor("javax.swing.border.EtchedBorder", new String[] {
                "etchType", "highlight", "shadow" });
        registerConstructor(
                "javax.swing.plaf.BorderUIResource$EtchedBorderUIResource",
                new String[] { "etchType", "highlight", "shadow" });
        registerConstructor("javax.swing.border.LineBorder", new String[] {
                "lineColor", "thickness" });
        registerConstructor(
                "javax.swing.plaf.BorderUIResource$LineBorderUIResource",
                new String[] { "lineColor", "thickness" });
        // Note this should check to see which of "color" and "tileIcon" is
        // non-null.
        registerConstructor("javax.swing.border.MatteBorder", new String[] {
                "top", "left", "bottom", "right", "tileIcon" });
        registerConstructor(
                "javax.swing.plaf.BorderUIResource$MatteBorderUIResource",
                new String[] { "top", "left", "bottom", "right", "tileIcon" });
        registerConstructor("javax.swing.border.SoftBevelBorder", new String[] {
                "bevelType", "highlightOuter", "highlightInner", "shadowOuter",
                "shadowInner" });
        // registerConstructorWithBadEqual("javax.swing.plaf.BorderUIResource$SoftBevelBorderUIResource",
        // new String[]{"bevelType", "highlightOuter", "highlightInner",
        // "shadowOuter", "shadowInner"});
        registerConstructor("javax.swing.border.TitledBorder", new String[] {
                "border", "title", "titleJustification", "titlePosition",
                "titleFont", "titleColor" });

        registerConstructor(
                "javax.swing.plaf.BorderUIResource$TitledBorderUIResource",
                new String[] { "border", "title", "titleJustification",
                        "titlePosition", "titleFont", "titleColor" });

        registerConstructor("javax.swing.text.html.HTML$Tag",
                new String[] { "name" });

    }

    /* pp */static boolean equals(Object o1, Object o2)
    {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }

    public synchronized static void setConverter(Class<?> type,
            DefaultConverter c)
    {
        converters.put(type.getName(), c);
    }

    public synchronized static DefaultConverter getConverter(Class<?> type)
    {
        if (type == null) return null;

        String name = type.getName();

        DefaultConverter conv = converters.get(name);
        if (conv != null) return conv;
        try
        {
            Class<?> c = Class.forName(PACKAGE_NAME + name.replace('.', '_')
                    + "_PersistenceDelegate");
            conv = (DefaultConverter) c.newInstance();
            converters.put(name, conv);
            return conv;
        }
        catch (ClassNotFoundException e)
        {
        }
        catch (Exception e)
        {
            System.err.println("Internal error: " + e);
            e.printStackTrace();
        }

        conv = new DefaultConverter(type);
        converters.put(name, conv);
        return conv;
    }

    public static BeanInfo getBeanInfo(Class<?> type)
    {
        BeanInfo info = null;
        try
        {
            info = Introspector.getBeanInfo(type, type.getSuperclass());
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        return info;
    }

    public static Collection<String> getTransientProperties(String cls_name)
    {
        return transientProperties.get(cls_name);
    }

    // MetaData registration
    public synchronized static void registerConstructor(String typeName,
            String[] constructor)
    {
        converters.put(typeName, new DefaultConverter(typeName, constructor));
    }

    public synchronized static void registerConstructor(String typeName,
            String[] constructor, Class<?>[] ctr_types)
    {
        converters.put(typeName, new DefaultConverter(typeName, constructor,
                ctr_types));
    }

    public synchronized static void registerFactoryConstructor(Class<?> type,
            Class<?> fcls, String method, String[] ctrParamNames,
            Class<?>[] ctrParamTypes)
    {
        DefaultConverter c = new DefaultConverter(type);
        c.setFactoryCtr(fcls, method, ctrParamNames, ctrParamTypes);
        converters.put(type.getName(), c);
    }

    public static void removeProperty(String typeName, String property)
    {
        Vector<String> tp = transientProperties.get(typeName);
        if (tp == null)
        {
            tp = new Vector<String>();
            transientProperties.put(typeName, tp);
        }
        tp.add(property);
    }
}

/*
 * 
 * class ProxyPersistenceDelegate extends DefaultConverter { protected
 * Expression instantiate(Object oldInstance, Encoder out) { Class type =
 * oldInstance.getClass(); java.lang.reflect.Proxy p =
 * (java.lang.reflect.Proxy)oldInstance; // This unappealing hack is not
 * required but makes the // representation of EventHandlers much more concise.
 * java.lang.reflect.InvocationHandler ih =
 * java.lang.reflect.Proxy.getInvocationHandler(p); if (ih instanceof
 * EventHandler) { EventHandler eh = (EventHandler)ih; Vector args = new
 * Vector(); args.add(type.getInterfaces()[0]); args.add(eh.getTarget());
 * args.add(eh.getAction()); if (eh.getEventPropertyName() != null) {
 * args.add(eh.getEventPropertyName()); } if (eh.getListenerMethodName() !=
 * null) { args.setSize(4); args.add(eh.getListenerMethodName()); } return new
 * Expression(oldInstance, EventHandler.class, "create", args.toArray()); }
 * return new Expression(oldInstance, java.lang.reflect.Proxy.class,
 * "newProxyInstance", new Object[]{type.getClassLoader(), type.getInterfaces(),
 * ih}); } }
 */
// Fields
// class java_lang_reflect_Field_PersistenceDelegate extends DefaultConverter {
// protected static String CLASS = "field_name";
//
// protected static String NAME = "class";
//
// public java_lang_reflect_Field_PersistenceDelegate() {
// super(java.lang.reflect.Field.class);
// }
//
// protected Object instantiate(Class type, Map<String, Object> props) {
// String name = (String) props.get(NAME);
// Class cls = (Class) props.get(CLASS);
// try {
// if (name != null && cls != null)
// return cls.getField(name);
// } catch (Exception ex) {
// ex.printStackTrace();
// }
// return null;
// }
//
// protected static Map<String, Class> map = new HashMap<String, Class>(2);
// static {
// map.put(NAME, String.class);
// map.put(CLASS, Class.class);
// }
//
// protected Map<String, Class> getAuxSlots() {
// return map;
// }
//
// public Map<String, Object> store(Object instance) {
// Map<String, Object> res = new HashMap<String, Object>(2);
// res.put(NAME, ((Field) instance).getName());
// res.put(CLASS, ((Field) instance).getDeclaringClass());
// return res;
// }
// }
// Methods
// class java_lang_reflect_Method_PersistenceDelegate extends DefaultConverter {
// protected static String CLASS = "method_name";
//
// protected static String NAME = "class";
//
// public java_lang_reflect_Method_PersistenceDelegate() {
// super(java.lang.reflect.Method.class);
// }
//
// public Object instantiate(Map<String, Object> props) {
// String name = (String) props.get(NAME);
// Class cls = (Class) props.get(CLASS);
// try {
// if (name != null && cls != null)
// return cls.getMethod(name);
// } catch (Exception ex) {
// ex.printStackTrace();
// }
// return null;
// }
//
// protected static Map<String, Class> map = new HashMap<String, Class>(2);
// static {
// map.put(NAME, String.class);
// map.put(CLASS, Class.class);
// }
//
// protected Map<String, Class> getAuxSlots() {
// return map;
// }
//
// public Map<String, Object> store(Object instance) {
// Map<String, Object> res = new HashMap<String, Object>(2);
// res.put(NAME, ((Method) instance).getName());
// res.put(CLASS, ((Method) instance).getDeclaringClass());
// return res;
// }
// }
// AWT
/*
 * class StaticFieldsPersistenceDelegate extends PersistenceDelegate { protected
 * void installFields(Encoder out, Class<?> cls) { Field fields[] =
 * cls.getFields(); for(int i = 0; i < fields.length; i++) { Field field =
 * fields[i]; // Don't install primitives, their identity will not be preserved
 * // by wrapping. if (Object.class.isAssignableFrom(field.getType())) {
 * out.writeExpression(new Expression(field, "get", new Object[]{null})); } } }
 * 
 * protected Expression instantiate(Object oldInstance, Encoder out) { throw new
 * RuntimeException("Unrecognized instance: " + oldInstance); }
 * 
 * public void writeObject(Object oldInstance, Encoder out) { if
 * (out.getAttribute(this) == null) { out.setAttribute(this, Boolean.TRUE);
 * installFields(out, oldInstance.getClass()); } super.writeObject(oldInstance,
 * out); //SystemColor java.awt.font.TextAttribute } }
 */

// SystemColor
// class java_awt_SystemColor_PersistenceDelegate extends
// StaticFieldsPersistenceDelegate {}
// TextAttribute
// class java_awt_font_TextAttribute_PersistenceDelegate extends
// StaticFieldsPersistenceDelegate {}
// Component
class java_awt_Component_PersistenceDelegate extends DefaultConverter
{
    private static final String SIZE = "size";

    private static final String LOCATION = "location";
    private static final String BOUNDS = "bounds";
    private static final String FONT = "font";
    private static final String FOREGROUND = "foreground";
    private static final String BACKGROUND = "background";

    private static final Map<String, Class<?>> map = new HashMap<String, Class<?>>(
            6);
    static
    {
        map.put(SIZE, Dimension.class);
        map.put(LOCATION, Point.class);
        map.put(BOUNDS, Rectangle.class);
        map.put(FONT, Font.class);
        map.put(FOREGROUND, Color.class);
        map.put(BACKGROUND, Color.class);
    }

    public java_awt_Component_PersistenceDelegate()
    {
        super(java.awt.Component.class);
    }

    @Override
    protected Map<String, Class<?>> getAuxSlots()
    {
        return map;
    }
}

// Container
class java_awt_Container_PersistenceDelegate extends DefaultConverter
{

    public java_awt_Container_PersistenceDelegate()
    {
        super(Container.class);
    }

    private static final Set<AddOnType> addOnFields = new HashSet<AddOnType>();
    static
    {
        addOnFields.add(new DefaultConverter.Add(AddOnFactory.ADD_COMP,
                new String[] { "component" }, new Class[] { Component.class }));
    }

    public Set<AddOnType> getAddOnFields()
    {
        return addOnFields;
    }
}

// Choice
class java_awt_Choice_PersistenceDelegate extends DefaultConverter
{

    public java_awt_Choice_PersistenceDelegate()
    {
        super(Choice.class);
    }

    private static final Set<AddOnType> addOnFields = new HashSet<AddOnType>();
    static
    {
        addOnFields.add(new DefaultConverter.Add(AddOnFactory.ADD_STR,
                new String[] { "name" }, new Class[] { String.class }));
    }

    public Set<AddOnType> getAddOnFields()
    {
        return addOnFields;
    }
}

// Menu
class java_awt_Menu_PersistenceDelegate extends DefaultConverter
{
    public java_awt_Menu_PersistenceDelegate()
    {
        super(Menu.class);
    }

    private static final Set<AddOnType> addOnFields = new HashSet<AddOnType>();
    static
    {
        addOnFields.add(new DefaultConverter.Add(AddOnFactory.ADD_COMP,
                new String[] { "items" }, new Class[] { Component.class }));
    }

    public Set<AddOnType> getAddOnFields()
    {
        return addOnFields;
    }
}

// MenuBar
class java_awt_MenuBar_PersistenceDelegate extends DefaultConverter
{
    public java_awt_MenuBar_PersistenceDelegate()
    {
        super(MenuBar.class);
    }

    private static final Set<AddOnType> addOnFields = new HashSet<AddOnType>();
    static
    {
        addOnFields.add(new DefaultConverter.Add(AddOnFactory.ADD_COMP,
                new String[] { "menus" }, new Class[] { Component.class }));
    }

    public Set<AddOnType> getAddOnFields()
    {
        return addOnFields;
    }
}

// List
class java_awt_List_PersistenceDelegate extends DefaultConverter
{
    public java_awt_List_PersistenceDelegate()
    {
        super(java.awt.List.class);
    }

    private static final Set<AddOnType> addOnFields = new HashSet<AddOnType>();
    static
    {
        addOnFields.add(new DefaultConverter.Add(AddOnFactory.ADD_STR,
                new String[] { "items" }, new Class[] { String.class }));
    }

    public Set<AddOnType> getAddOnFields()
    {
        return addOnFields;
    }
}

// LayoutManagers

// BorderLayout
class java_awt_BorderLayout_PersistenceDelegate extends DefaultConverter
{
    public java_awt_BorderLayout_PersistenceDelegate()
    {
        super(java.awt.BorderLayout.class);
    }

    private static final Set<AddOnType> addOnFields = new HashSet<AddOnType>();
    static
    {
        addOnFields.add(new DefaultConverter.Add(AddOnFactory.BORDER_LAYOUT,
                new String[] { "north", "south", "east", "west", "center" },
                null));
    }

    public Set<AddOnType> getAddOnFields()
    {
        return addOnFields;
    }
}

// CardLayout
class java_awt_CardLayout_PersistenceDelegate extends DefaultConverter
{
    public java_awt_CardLayout_PersistenceDelegate()
    {
        super(java.awt.CardLayout.class);
    }

    private static final Set<AddOnType> addOnFields = new HashSet<AddOnType>();
    static
    {
        addOnFields.add(new DefaultConverter.Add(AddOnFactory.CARD_LAYOUT,
                new String[] { "vector" }, new Class[] { Vector.class }));
    }

    public Set<AddOnType> getAddOnFields()
    {
        return addOnFields;
    }
}

// GridBagLayout
class java_awt_GridBagLayout_PersistenceDelegate extends DefaultConverter
{
    public java_awt_GridBagLayout_PersistenceDelegate()
    {
        super(java.awt.GridBagLayout.class);
    }

    private static final Set<AddOnType> addOnFields = new HashSet<AddOnType>();
    static
    {
        addOnFields.add(new DefaultConverter.Add(AddOnFactory.GRID_BAG_LAYOUT,
                new String[] { "comptable" }, new Class[] { Hashtable.class }));
    }

    public Set<AddOnType> getAddOnFields()
    {
        return addOnFields;
    }
}

// Swing
class javax_swing_AbstractAction_PersistenceDelegate extends DefaultConverter
{
    public javax_swing_AbstractAction_PersistenceDelegate()
    {
        super(AbstractAction.class);
    }

    protected static final Map<String, Class<?>> map = new HashMap<String, Class<?>>(
            1);
    static
    {
        map.put("arrayTable", Object.class);
    }

    protected Map<String, Class<?>> getAuxSlots()
    {
        return map;
    }
}

class javax_swing_ArrayTable_PersistenceDelegate extends DefaultConverter
{
    public javax_swing_ArrayTable_PersistenceDelegate()
    {
        try
        {
            this.type = Class.forName("javax.swing.ArrayTable");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    protected static final Map<String, Class<?>> map = new HashMap<String, Class<?>>(
            1);
    static
    {
        map.put("table", Object.class);
    }

    protected Map<String, Class<?>> getAuxSlots()
    {
        return map;
    }
}

// Models
// DefaultListModel
class javax_swing_DefaultListModel_PersistenceDelegate extends DefaultConverter
{
    public javax_swing_DefaultListModel_PersistenceDelegate()
    {
        super(DefaultListModel.class);
    }

    private static final Set<AddOnType> addOnFields = new HashSet<AddOnType>();
    static
    {
        addOnFields.add(new DefaultConverter.Add(AddOnFactory.ADD_EL,
                new String[] { "delegate" }, null));
    }

    public Set<AddOnType> getAddOnFields()
    {
        return addOnFields;
    }
}

class javax_swing_JComboBox_PersistenceDelegate extends DefaultConverter
{
    public javax_swing_JComboBox_PersistenceDelegate()
    {
        super(JComboBox.class);
    }

    @Override
    public Set<AddOnType> getAllAddOnFields()
    {
        return null;
    }
}

//component add-on not needed, components will be added by setTopXX, setLeftXX
class javax_swing_JSplitPane_PersistenceDelegate extends DefaultConverter
{
    /*
     * This is required because the JSplitPane reveals a private layout
     * class called BasicSplitPaneUI$BasicVerticalLayoutManager which
     * changes with the orientation. To avoid the necessity for
     * instantiating it we cause the orientation attribute to get set before
     * the layout manager - that way the layout manager will be changed as a
     * side effect. Unfortunately, the layout property belongs to the
     * superclass and therefore precedes the orientation property. PENDING -
     * we need to allow this kind of modification. For now, put the property
     * in the constructor.
     */
    public javax_swing_JSplitPane_PersistenceDelegate()
    {
        super(JSplitPane.class, new String[] { "orientation" });
    }

    @Override
    public Set<AddOnType> getAllAddOnFields()
    {
        return null;
    }
}

// DefaultComboBoxModel
class javax_swing_DefaultComboBoxModel_PersistenceDelegate extends
        DefaultConverter
{
    public javax_swing_DefaultComboBoxModel_PersistenceDelegate()
    {
        super(DefaultComboBoxModel.class);
    }

    public Map<String, Class<?>> getSlots()
    {
        Map<String, Class<?>> sup = super.getSlots();
        sup.put("objects", Vector.class);
        return sup;
    }
}

// DefaultMutableTreeNode
class javax_swing_tree_DefaultMutableTreeNode_PersistenceDelegate extends
        DefaultConverter
{

    public javax_swing_tree_DefaultMutableTreeNode_PersistenceDelegate()
    {
        super(DefaultMutableTreeNode.class);
    }

    private static final Set<AddOnType> addOnFields = new HashSet<AddOnType>();
    static
    {
        addOnFields.add(new DefaultConverter.Add(AddOnFactory.ADD_TREE,
                new String[] { "children" }, null));
    }

    public Set<AddOnType> getAddOnFields()
    {
        return addOnFields;
    }
}

// JTabbedPane
class javax_swing_JTabbedPane_PersistenceDelegate extends DefaultConverter
{

    public javax_swing_JTabbedPane_PersistenceDelegate()
    {
        super(JTabbedPane.class);
    }

    private static final Set<AddOnType> addOnFields = new HashSet<AddOnType>();
    static
    {
        addOnFields.add(new DefaultConverter.Add(AddOnFactory.ADD_TAB,
                new String[] { "componentAt", "titleAt", "iconAt" },
                new Class[] { Component.class, String.class, Icon.class }));
    }

    public Set<AddOnType> getAddOnFields()
    {
        return addOnFields;
    }

}

// JMenu
// Note that we do not need to state the initializer for
// JMenuItems since the getComponents() method defined in
// Container will return all of the sub menu items that
// need to be added to the menu item.
// Not so for JMenu apparently.
class javax_swing_JMenu_PersistenceDelegate extends DefaultConverter
{
    public javax_swing_JMenu_PersistenceDelegate()
    {
        super(JMenu.class);
    }

    private static final Set<AddOnType> addOnFields = new HashSet<AddOnType>();
    static
    {
        addOnFields.add(new DefaultConverter.Add(AddOnFactory.ADD_COMP,
                new String[] { "menuComponents" },
                new Class[] { Component.class }));
    }

    public Set<AddOnType> getAddOnFields()
    {
        return addOnFields;
    }
}

class javax_swing_JScrollBar_PersistenceDelegate extends DefaultConverter
{

    public javax_swing_JScrollBar_PersistenceDelegate()
    {
        super(JScrollBar.class);
    }

    public Set<AddOnType> getAllAddOnFields()
    {
        return null;
    }
}

class javax_swing_JScrollPane_PersistenceDelegate extends DefaultConverter
{

    public javax_swing_JScrollPane_PersistenceDelegate()
    {
        super(JScrollPane.class);
    }

    public Set<AddOnType> getAllAddOnFields()
    {
        return null;
    }
}

class javax_swing_JFrame_PersistenceDelegate extends DefaultConverter
{

    public javax_swing_JFrame_PersistenceDelegate()
    {
        super(JFrame.class);
    }

    public Set<AddOnType> getAllAddOnFields()
    {
        return null;
    }
}

//class javax_swing_JPanel_PersistenceDelegate extends DefaultConverter
//{
//
//    public javax_swing_JPanel_PersistenceDelegate()
//    {
//        super(JPanel.class);
//    }
//
//    public Set<AddOnType> getAllAddOnFields()
//    {
//        return null;
//    }
//}
