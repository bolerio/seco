package seco.eclipse;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.text.Utilities;

import org.eclipse.albireo.core.SwingControl;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.hypergraphdb.HGEnvironment;

import seco.ThisNiche;
import seco.gui.PiccoloCanvas;
import seco.notebook.AppConfig;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.syntax.ScriptSupport;
import seco.notebook.syntax.completion.JavaDocManager;
import seco.rtenv.ClassPathEntry;
import edu.emory.mathcs.backport.java.util.Arrays;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class SecoView extends ViewPart
{
    public static final String ID = "seco.eclipse.SecoView";
    private IMemento memento;
    private NoNicheGUI noNicheGUI;
   
    Composite parent;

     /**
     * The constructor.
     */
    public SecoView()
    {
    }

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException
    {
        super.init(site, memento);
        this.memento = memento;
        makeActions();
    }

    private SwingControl swingControl;

    public void createPartControl(final Composite par)
    {
        this.parent = par;
        update();
    }

    private void clean()
    {
        if (swingControl != null) 
        {
            swingControl.dispose();
            swingControl = null;
        }
        if (noNicheGUI != null)
        {
            noNicheGUI.dispose();
            noNicheGUI = null;
        }
    }
    
    void update()
    {
        SecoPlugin plugin = PluginU.getSecoPlugin();
        if (plugin.getNicheLocation() == null)
            plugin.setNicheLocation(restoreState());
        clean();
        if (plugin.getNicheLocation() == null)
        {
           
            noNicheGUI = new NoNicheGUI(parent, SWT.None);
        }
        else
        {
            boolean success = setupNiche();
            if(!success)
            {
                PluginU.getSecoPlugin().closeNiche();
                noNicheGUI = new NoNicheGUI(parent, SWT.None);
            }
            else
               swingControl = new SwingControl(parent, SWT.NONE) {
                protected JComponent createSwingComponent()
                {
                    
                    PiccoloCanvas canvas = ThisNiche.guiController.getCanvas();
                    PScrollPane scroll = new PScrollPane(canvas);
                    return scroll;
                }

                public Composite getLayoutAncestor()
                {
                    return parent;
                }
            };
        }
       // parent.setLayout(new FillLayout());
        parent.layout();
    }

    static GoToDeclarationAction goToDeclarationAction = new GoToDeclarationAction();
    boolean setupNiche()
    {
        SecoPlugin plugin = PluginU.getSecoPlugin();
        ThisNiche.guiController = new SecoEclipseGUIController();
        try{
           HGEnvironment.get(plugin.getNicheLocation());
        }catch(Throwable t)
        {
            return false;
        }
        //something went wrong
        if(ThisNiche.getTopContext() == null)
            return false;
        File f = AppConfig.getJarDirectory(Platform.class);
        ThisNiche.getTopContext().getRuntimeContext().getClassPath().add(
                new ClassPathEntry(f));
        ThisNiche.getTopContext().getRuntimeContext().getBindings().put(
                "plugin", plugin);
        ThisNiche.getTopContext().getRuntimeContext().getBindings().put(
                "workspace", ResourcesPlugin.getWorkspace());
        ThisNiche.getTopContext().getRuntimeContext().getBindings().put(
                "frame", null);
//        try{
//          BshScriptEngineEx eng = (BshScriptEngineEx)
//            ThisNiche.getEvaluationContext(
//                    ThisNiche.TOP_CONTEXT_HANDLE).getEngine("beanshell");
//          eng.importPackage("seco.eclipse");
//        }catch(Exception ex)
//        {
//            ex.printStackTrace();
//        }
       if(!Arrays.asList(NotebookUI.getPopupMenu().getComponents()).contains(goToDeclarationAction))  
         NotebookUI.getPopupMenu().add(goToDeclarationAction);
       //JavaDocManager.getInstance().addJavaDocProvider(new EclipseJavaDocProvider());
       
       return true;
    }

    @Override
    public void saveState(IMemento memento)
    {
        if(ThisNiche.graph != null)
          memento = memento.createChild("niche", ThisNiche.graph.getLocation());
    }

    private String restoreState()
    {
        if (memento == null) 
            return null;
        String res = (memento.getChild("niche") != null) ?
                memento.getChild("niche")
               .getID() : null;
        memento = null;
        return res;
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus()
    {
        if(swingControl != null)
           swingControl.setFocus();
    }
    
    void setTitle0(String title)
    {
       // setTitle(title);
       // this.setPartName(title);
       // this.setContentDescription(title);
    }


    private void makeActions()
    {
        Action nicheAction = new Action() {
            public void run()
            {
                PluginU.openNichesDlg(PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow());
            }
        };
        nicheAction.setText("Open Niche");
        nicheAction.setToolTipText("Open Another Niche");
        Action closeNicheAction = new Action() {
            public void run()
            {
                SecoPlugin plugin = PluginU.getSecoPlugin();
                plugin.setNicheLocation(null);
            }
        };
        closeNicheAction.setText("Close Niche");
        closeNicheAction.setToolTipText("Close Current Niche");
        
        Action aboutAction = new Action() {
            public void run()
            {
               showMessage("Seco Plugin 1.0");
            }
        };
        aboutAction.setText("About");
        aboutAction.setToolTipText("About");
        
        IActionBars bars = getViewSite().getActionBars();
        IMenuManager manager = bars.getMenuManager();
        manager.add(nicheAction);
        manager.add(closeNicheAction);
        manager.add(new Separator());
        manager.add(aboutAction);
    }

    private void showMessage(String message)
    {
        MessageDialog.openInformation(getSite().getShell(), "Seco", message);
    }
    
//    private void hookDoubleClickAction()
//    {
//        // viewer.addDoubleClickListener(new IDoubleClickListener() {
//        // public void doubleClick(DoubleClickEvent event)
//        // {
//        // doubleClickAction.run();
//        // }
//        // });
//    }
    
//  private void hookContextMenu()
//  {
//      MenuManager menuMgr = new MenuManager("#PopupMenu");
//      menuMgr.setRemoveAllWhenShown(true);
//      menuMgr.addMenuListener(new IMenuListener() {
//          public void menuAboutToShow(IMenuManager manager)
//          {
//              SecoView.this.fillContextMenu(manager);
//          }
//      });
//      // Menu menu = menuMgr.createContextMenu(viewer.getControl());
//      // viewer.getControl().setMenu(menu);
//      // getSite().registerContextMenu(menuMgr, viewer);
//  }
//
//  private void fillLocalPullDown(IMenuManager manager)
//  {
//      manager.add(nicheAction);
//      manager.add(closeNicheAction);
//      manager.add(new Separator());
//      manager.add(aboutAction);
//  }
//
//  private void fillContextMenu(IMenuManager manager)
//  {
//      manager.add(nicheAction);
//      manager.add(closeNicheAction);
//      manager.add(aboutAction);
//      // Other plug-ins can contribute there actions here
//      manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
//  }
//
//  private void fillLocalToolBar(IToolBarManager manager)
//  {
//      manager.add(nicheAction);
//      manager.add(closeNicheAction);
//      manager.add(aboutAction);
//  }
    
    private class NoNicheGUI extends Composite
    {
        private Button noNicheButton;
        private Label text;
        
        public NoNicheGUI(final Composite parent, int style)
        {
            super(parent, style);
            setLayout(new FormLayout());
            setBackground(new org.eclipse.swt.graphics.Color(
                    getDisplay(), 255, 255, 255));
            text = new Label(this, SWT.WRAP);
            text.setText("No niche specified.\n " +
            		"Please select one.");
            text.setBackground(new org.eclipse.swt.graphics.Color(
                    getDisplay(), 255, 255, 255));
            org.eclipse.swt.graphics.Font font = 
                new org.eclipse.swt.graphics.Font(
                    getDisplay(), new FontData("Times New Roman", 16, 0));
            text.setFont(font);
            FormData formData = new FormData();
            formData.top = new FormAttachment(5,5);
            formData.left = new FormAttachment(5,5);
            text.setLayoutData(formData);
           
            noNicheButton = new Button(this, SWT.PUSH);
            formData = new FormData();
            formData.top = new FormAttachment(5,5);
            formData.left = new FormAttachment(text, 5);
            noNicheButton.setLayoutData(formData);
            
            SelectionAdapter adapter = new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event)
                {
                    // SecoAction a = new SecoAction();
                    IWorkbench win = PlatformUI.getWorkbench();
                    // a.init(win.getActiveWorkbenchWindow());
                    // a.run(null);
                    PluginU.openNichesDlg(win.getActiveWorkbenchWindow());
                    //createPartControl(parent);
                }
            };
            noNicheButton.addSelectionListener(adapter);
            noNicheButton.setText("Select");
            noNicheButton.setFont(font);
            }
        
    }
    
    static class GoToDeclarationAction  extends AbstractAction
    {

        public GoToDeclarationAction()
        {
            super();
            putValue(AbstractAction.NAME, "Go to Declaration");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            NotebookUI ui = NotebookUI.getFocusedNotebookUI();
            if (ui == null) return;
            NotebookDocument doc = ui.getDoc();
            int pos = ui.getCaretPosition();
            ScriptSupport sup = doc.getScriptSupport(pos);
            if (sup == null) return;
            try{
                String cls = (ui.getSelectionStart() != ui.getSelectionEnd()) ?
                        doc.getText(ui.getSelectionStart(), 
                                ui.getSelectionEnd() - ui.getSelectionStart()):
                        doc.getText(Utilities.getWordEnd(ui, pos), 
                                Utilities.getWordStart(ui, pos));    
              PluginU.searchAndOpen(cls);
            }catch(Exception ex)
            {
         
            }
        }
    };
    
}