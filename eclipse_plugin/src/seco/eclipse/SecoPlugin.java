package seco.eclipse;

import java.io.File;

import javax.swing.SwingUtilities;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.storage.BDBConfig;
import org.osgi.framework.BundleContext;

import seco.ThisNiche;
import seco.eclipse.SecoView.GoToDeclarationAction;
import seco.gui.GUIHelper;
import seco.actions.ActionManager;
import seco.AppConfig;
import seco.notebook.NotebookUI;
import seco.storage.ClassRepository;
import seco.rtenv.ClassPathEntry;
import seco.rtenv.RuntimeContext;

import java.util.Arrays;

/**
 * The activator class controls the plug-in life cycle
 */
public class SecoPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "seco.eclipse.plugin";
	// The shared instance
	private static SecoPlugin plugin;
	private String nicheLocation;

	/**
	 * The constructor
	 */
	public SecoPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		String osname = System.getProperty("os.name");
		if (osname != null
				&& (osname.indexOf("win") > -1 || osname.indexOf("Win") > -1)) {
			System.loadLibrary("libdb50");
			System.loadLibrary("libdb_java50");
		}
		
		//System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName()); 
		
		GUIHelper.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
		      public void uncaughtException(Thread t, Throwable e) {
		    	  SecoPlugin.getDefault().nicheLocation = null;	
					PluginU.showError(e.toString());
					show_or_update_view();
					throw new RuntimeException(e);
			  }
		    });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		if (ThisNiche.guiController != null)
			ThisNiche.guiController.exit();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SecoPlugin getDefault() {
		if (plugin == null) {
			plugin = (SecoPlugin) Platform.getPlugin(SecoPlugin.PLUGIN_ID);
			try {
				ClassRepository.getInstance();
			} catch (Exception ex) {
				File f = AppConfig.getJarDirectory(BDBConfig.class);
				System.out.println("DB.jar dir: " + f.getAbsolutePath());
				ex.printStackTrace();
			}
		}
		return plugin;
	}

	public String getNicheLocation() {
		return nicheLocation;
	}

	public void setNicheLocation(String _nicheLocation) {
		if (_nicheLocation != null && _nicheLocation.equals(nicheLocation))
			return;
		if (_nicheLocation == null && nicheLocation == null)
			return;
		if (nicheLocation != null)
			closeNiche();
		this.nicheLocation = _nicheLocation;
		show_or_update_view();
	}
	
	private void show_or_update_view()
	{
		SecoView view = PluginU.getSecoView();
		if (view != null)
			view.update();
		else
			PluginU.openSecoView();
	}

	void closeNiche() {
		try {
			if (ThisNiche.graph != null) {
				ThisNiche.guiController.exit();
				ThisNiche.graph.close();
			}
		}
		// catch (Exception ex)
		// {
		// ex.printStackTrace();
		// }
		finally {
			ThisNiche.graph = null;
			ThisNiche.guiController = null;
		}
	}

	static GoToDeclarationAction goToDeclarationAction = new GoToDeclarationAction();

	boolean setupNiche() {
		ThisNiche.guiController = new SecoEclipseGUIController();
		try
		{  
		 	  HGEnvironment.get(plugin.getNicheLocation());
		  }catch (Throwable t)
	      {
			nicheLocation = null;
			t.printStackTrace();
			PluginU.showError(t.toString());
			return false;
	      }    	
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				RuntimeContext rt = ThisNiche.getTopContext().getRuntimeContext(); 
				//eclipse plugins dir
				File f = AppConfig.getJarDirectory(Platform.class);
				rt.getClassPath().add(new ClassPathEntry(f));
				//seco plugin lib dir
				f = AppConfig.getJarDirectory(NotebookUI.class);
				rt.getClassPath().add(new ClassPathEntry(f));
				rt.getBindings().put("plugin", plugin);
				rt.getBindings().put("workspace", ResourcesPlugin.getWorkspace());
				rt.getBindings().put("frame", null);
				if (!Arrays.asList(NotebookUI.getPopupMenu().getComponents())
						.contains(goToDeclarationAction)) {
					NotebookUI.getPopupMenu().add(goToDeclarationAction);
					ActionManager.getInstance().putAction(
							goToDeclarationAction, false);
				}
			}
		});
		// JavaDocManager.getInstance().addJavaDocProvider(new
		// EclipseJavaDocProvider());

		return true;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
//	public static class ExceptionHandler { 
//		public void handle(Throwable ex)
//		{
//			SecoPlugin.getDefault().nicheLocation = null;	
//			PluginU.showError(ex.toString());
//			throw new RuntimeException(ex);
//		}
//	} 
}
