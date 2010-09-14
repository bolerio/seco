package seco.eclipse;

import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import seco.ThisNiche;

/**
 * The activator class controls the plug-in life cycle
 */
public class SecoPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "seco.eclipse.plugin";

	// The shared instance
	private static SecoPlugin plugin;
	
	private String nicheLocation;
	
	public String getNicheLocation()
    {
        return nicheLocation;
    }

    public void setNicheLocation(String _nicheLocation)
    {
        if(_nicheLocation != null && _nicheLocation.equals(nicheLocation))
            return;
        if(_nicheLocation == null && nicheLocation == null) return;
        if(nicheLocation != null)
            closeNiche();
        this.nicheLocation = _nicheLocation;
        SecoView view = PluginU.getSecoView();
        if(view != null)
           view.update();
        else
           PluginU.openSecoView();
    }

    void closeNiche()
    {
        if(ThisNiche.graph != null){
           ThisNiche.guiController.exit();
           ThisNiche.graph.close();
        }
        ThisNiche.graph = null;
        ThisNiche.guiController = null;
    }
    
    /**
	 * The constructor
	 */
	public SecoPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception 
	{
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
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
