package seco.eclipse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

public class EclipseActions
{
    public static final String OPEN_NICHE_ACTION = "Open Niche";
    public static final String CLOSE_NICHE_ACTION = "Close Niche";
    public static final String ABOUT_ACTION = "About";

    private static Map<String, Action> actions = new HashMap<String, Action>();
    static
    {
        actions.put(OPEN_NICHE_ACTION, new OpenNicheAction());
        actions.put(CLOSE_NICHE_ACTION, new CloseNicheAction());
        actions.put(ABOUT_ACTION, new AboutAction());
    }

    static class CloseNicheAction extends Action
    {
        public CloseNicheAction()
        {
            super();
            setText(CLOSE_NICHE_ACTION);
            setToolTipText("Close Current Niche");
        }

        public void run()
        {
            SecoPlugin.getDefault().setNicheLocation(null);
        }
    }

    static class OpenNicheAction extends Action
    {
        public OpenNicheAction()
        {
            super();
            setText(OPEN_NICHE_ACTION);
            setToolTipText("Open Another Niche");
        }

        public void run()
        {
            PluginU.openNichesDlg(PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow());
        }
    };

    static class AboutAction extends Action
    {
        public AboutAction()
        {
            super();
            setText(ABOUT_ACTION);
            setToolTipText("About Seco Plug-In");
        }

        public void run()
        {
            PluginU.showMessage("Seco Plugin 1.0");
        }
    };

    public static Action getAction(String name)
    {
        return actions.get(name);
    }

}
