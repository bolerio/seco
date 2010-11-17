package seco.storage.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.text.StyledEditorKit;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.AtomTypeCondition;
import org.hypergraphdb.type.javaprimitive.IntType;


public class MasterTest
{
	public static final String IMAGE_BASE = "seco/notebook/images/";
	public static final String PATH = "F:\\kosta\\xpackTest27";
	public static boolean ADD = !true;

	public static ImageIcon makeIcon()
	{
		URL url = null;
		//javax.swing.DefaultBoundedRangeModel
		try
		{
			url = new URL(
					"file:/F:/kosta/ticl/seco/src/seco/notebook/images/Undo16.gif");
		}
		catch (Exception ex)
		{
		}
		if (url != null)
		{
			// System.out.println("Adding Icon");
			ImageIcon icon = new ImageIcon(url);
			icon = new ImageIcon(url);
			return icon;
		}
		return null;
	}

	public static JMenuBar makeBar()
	{
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic('f');
		JMenuItem mi = new JMenuItem("New");
		mi.addActionListener(new MyActionListener());
//		mi.setIcon(makeIcon());
		mi.setToolTipText("New Tooltip");
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));
		menu.add(mi);
		menu.addSeparator();
		Action a = getAction();
		if(a!=null){
		a.putValue(Action.NAME, "Very Long Name Here");
//		a.putValue(Action.SMALL_ICON, makeIcon());
		a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_N, ActionEvent.CTRL_MASK));}
		// mi = new JMenuItem(a);
		mi = new JMenuItem("Very Long Name Here");
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK));
		// mi.addActionListener(new MyActionListener());
		// mi.setIcon(makeIcon());
		menu.add(mi);
		// mi.addActionListener((ActionListener)EventHandler.create(
		// ActionListener.class, new MyActionListener(), "openCellTree"));
		bar.add(menu);
		/*
		 * menu = new JMenu("Edit"); a = getAction(); a.putValue(Action.NAME,
		 * "cut"); a.putValue(Action.SMALL_ICON, makeIcon());
		 * a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		 * KeyEvent.VK_X, ActionEvent.CTRL_MASK)); mi = new JMenuItem(a);
		 * //mi.addActionListener(new MyActionListener()); menu.add(mi);
		 */
		// bar.add(menu);
		return bar;
	}

	public static final Action getAction()
	{
		StyledEditorKit kit = new StyledEditorKit();
		Action[] actions = kit.getActions();
		for (Action a : actions)
			if (a.getValue(Action.NAME).equals(StyledEditorKit.cutAction))
				return a;
		return null;
	}

	public static JButton makeButton()
	{
		JButton button = new JButton("test");
		button.setIcon(makeIcon());
		button.addActionListener(new MyActionListener());
		button.setDisplayedMnemonicIndex(0);
		return button;
	}

	public static Component makePanel()
	{
		JPanel panel = new JPanel();//new AddRemoveListPanel();//new JPanel();
		//panel.add(makeButton());
		List l = new LinkedList();
		l.add("First"); l.add("Second");
		//((AddRemoveListPanel)panel).setData(l);
		//JPanel outer = new JPanel();
		//outer.add(panel);
		//JTabbedPane pane = new JTabbedPane();
	   // pane.addTab("First Pane", panel);	
	   // pane.addTab("Second Pane", new JTable());
		return panel;
		
		//return makeButton();
	}

	public static Object find(HyperGraph hg, Class cls)
	{
		System.out.println("Find: " + cls);
		HGSearchResult res = hg.find(new AtomTypeCondition(cls));
		try
		{
			while (res.hasNext())
			{
				HGHandle hh = (HGHandle) res.next();
				System.out.println("Find-hh: " + hh);
				System.out.println("Find-m: " + hg.get(hh));
				if (hh == null) continue;
				return hg.get(hh);
			}
		}
		finally
		{
			res.close();
		}
		return null;
	}

	public static void main(String[] args)
	{
		//int [] aa = new int[]{};
//		Class t = AddRemoveListPanel.RemoveAction.class;//aa.getClass();//Insets.class;
//		if (t.isPrimitive())
//		{
//			String typeName = BonesOfBeans.wrapperEquivalentOf(t).getName()
//					.replace('.', '/');
//			System.out.println(typeName);
//			
//		} else
//		{
//			System.out.println("NOT: " + t.getName().replace('.', '/'));
//			System.out.println(
//					Type.getType(t).getDescriptor());
//			try{
//			System.out.println(Type
//					.getMethodDescriptor(t.getMethod("setPanel", AddRemoveListPanel.class)));
//			System.out.println(Type
//					.getMethodDescriptor(t.getMethod("getPanel")));
//			
//			}catch(Exception e){
//				
//			}
//			}
//		 if(true) return;
//		
	// Class cl =
	//	JPanel.class;//Container.class;//java.awt.geom.Rectangle2D.class;
		//DefaultConverter c = (DefaultConverter) MetaData.getConverter(cl);
		//System.out.println("Slots: " + c.getSlots() +
	//		":" + c.getSlots().size() + ((DefaultConverter)c).getType());
//		for(Converter.AddOnType a : c.getAllAddOnFields())
//		  System.out.println("All_Addon: " + a.getArgs()[0] + ":" +
//				  a.getTypes()[0]);
//		c = (DefaultConverter) MetaData.getConverter(JRootPane.class);
//		 for(Converter.AddOnType a : c.getAllAddOnFields())
//			  System.out.println("Addon: " + a.getArgs()[0] + ":" +
//					  a.getTypes()[0]);
//		JPanel in = (JPanel)makePanel();
//		Map<String, Object> res1 = c.store(in);
//		System.out.println("Rs:" + in.getBounds() + ": " + res1);
//		LayoutManager mgr = in.getLayout();
//		System.out.println("Rs2:" + in.getLayout());
//		 JFrame ff = new JFrame();
//		 JPanel o = (JPanel) c.make(res1);
//		 ff.getContentPane().add((JPanel) o);
//		 o.setLayout(mgr);
//		 mgr.layoutContainer(o);
//		 
//		 ff.setVisible(true);
//		 Component[] pp = (Component[]) res1.get("component");
//		 System.out.println("Rs:" + o + ":" + pp.length );
		 //if(true) return;
		
		HyperGraph hg = null;
		try
		{
			// listSlots(java.util.Date.class);
			hg = new HyperGraph(PATH);
			// hg.add(new Object[]{"Mist", null, "Most"});
			// hg.add(new Font("Default", 0, 12));
			// hg.add(new Font("Default", 0, 14));
			HGHandle h = null;
			if (ADD)
			{
				
				//h = hg.add(makeBar());
				Component p = makePanel();
				hg.add(p);
				 JFrame frame = new JFrame();
				// frame.setJMenuBar(makeBar());
				 frame.getContentPane().add(p);
				 
				// hg.add(frame);
				
			}
			if (true)
			{
				JFrame f = null;//(JFrame) find(hg, JFrame.class);
				//((JFrame) f).setJMenuBar(makeBar());
				if(f != null)
				{
				
				System.out.println("Frame: " + f.getContentPane().getComponentCount());
				for(Component cc: f.getContentPane().getComponents())
					System.out.println(cc);
				//		+ ":" + ((JRootPane) f.getContentPane().getComponent(0)).getComponentCount());
//				for(Component c: ((JRootPane) f.getContentPane().getComponent(0)).getComponents())
//				{
//					System.out.println("RootPane children: " + c);
//					for(Component inner: ((Container)c).getComponents())
//					{
//						System.out.println("Inner children: " + inner);
//						for(Component inner1: ((Container)inner).getComponents())
//						   System.out.println("Inner1 children: " + inner1);
//						
//					}
//				}
				//((JFrame) f).getContentPane().add(new JButton("Test"), BorderLayout.SOUTH);
				((JFrame) f).validate();
				((JFrame) f).setVisible(true);
				((JFrame) f).validate();
					return;
				}
				Object m = find(hg, JMenuBar.class);
				 JFrame frame = new JFrame();
				 if(m != null)
					frame.setJMenuBar((JMenuBar) m);
				 //else
				//	 frame.setJMenuBar(makeBar());
				 m = find(hg, JPanel.class); //AddRemoveListPanel.class);
				System.out.println("Panel: " + ((JPanel)m).getComponentCount());
				 if(m!= null)
				   frame.getContentPane().add((Component) m);
				 frame.setVisible(true);
				 if (ADD) hg.add(frame);
//				 XMLEncoder enc = new XMLEncoder(new FileOutputStream(
//						 "E:/temp/Frame.xml"));
//				 enc.writeObject(frame);
//				 enc.close();
//				 enc = new XMLEncoder(new FileOutputStream(
//				 "E:/temp/FBar.xml"));
//		          enc.writeObject(frame.getJMenuBar());
//		          enc.close();
				// find(hg, Font.class);
				// find(hg, MasterTest.TestBean.class);
				// find(hg, ActionListener[].class);
			} else
			{
				Object res = hg.get(hg.getPersistentHandle(h));
				System.out.println("Res: " + res);
				System.out.println("Button: " + ((JPanel) res).getComponent(0));
				// System.out.println("Icon: " + ((JButton)res).getIcon());
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if (hg != null) hg.close();
		}
		// System.exit(0);
	}

	static void test(int number)
	{
		IntType t = new IntType();
		Integer i = new Integer(number);
		byte[] b = t.toByteArray(i);
		int out = (Integer) t.fromByteArray(b);
		if (number != out)
			System.out.println("In: " + number + " Out: " + out);
	}


	public static class MyActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			System.out.println("Button pressed");
		}
	}

	public static class TestBean
	{
		List<ActionListener> listeners = new ArrayList<ActionListener>();

		public ActionListener[] getListeners()
		{
			return listeners.toArray(new ActionListener[listeners.size()]);
		}

		// public void setListeners(ActionListener[] listeners) {
		// this.listeners = listeners;
		// }
		public void addListener(ActionListener l)
		{
			listeners.add(l);
		}

		public String toString()
		{
			return "" + listeners.size();
		}

		public void make(TestBean test)
		{
			test.addListener(null);
		}
	}

	public static class TestBeanConverter extends DefaultConverter
	{
		public TestBeanConverter(Class<?> type)
		{
			super(TestBean.class);
		}

		public final Map<String, Class<?>> map = new HashMap<String, Class<?>>();

		protected Map<String, Class<?>> getAuxSlots()
		{
			if (map != null && map.size() == 0)
			{
				map.put("components", ActionListener[].class);
			}
			// System.out.println("Container - getAuxSlots");
			return map;
		}
	}

}
