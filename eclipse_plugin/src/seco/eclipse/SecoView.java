package seco.eclipse;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;

import org.eclipse.albireo.core.SwingControl;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import seco.ThisNiche;
import seco.gui.PiccoloCanvas;
import seco.notebook.Acceptor;
import seco.notebook.FinderFactory;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.Utilities;
import seco.notebook.syntax.ScriptSupport;
import bsh.BshAst;
import bsh.ClassIdentifier;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 *  Main Seco view.
 * <p>
 */

public class SecoView extends ViewPart
{
    public static final String ID = "seco.eclipse.SecoView";
    private SwingControl swingControl;
    private IMemento memento;
    private NoNicheGUI noNicheGUI;

    Composite parent;

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException
    {
        super.init(site, memento);
        this.memento = memento;
        //PluginU.view = this;
        makeActions();
    }

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
        SecoPlugin plugin = SecoPlugin.getDefault();
        if (plugin.getNicheLocation() == null)
            plugin.setNicheLocation(restoreState());
        clean();
        if (plugin.getNicheLocation() == null)
        {
            noNicheGUI = new NoNicheGUI(parent, SWT.None);
        }
        else
        {
            boolean success = plugin.setupNiche();
            if (!success)
            {
                plugin.closeNiche();
                noNicheGUI = new NoNicheGUI(parent, SWT.None);
            }
            else
                swingControl = new SwingControl(parent, SWT.NONE) {
                    protected JComponent createSwingComponent()
                    {

                        PiccoloCanvas canvas = ThisNiche.guiController
                                .getCanvas();
                        PScrollPane scroll = new PScrollPane(canvas);
                        return scroll;
                    }

                    public Composite getLayoutAncestor()
                    {
                        return parent;
                    }
                };
        }
        
        parent.layout();
    }

    

   
    @Override
    public void saveState(IMemento memento)
    {
        if (ThisNiche.graph != null)
            memento = memento.createChild("niche", ThisNiche.graph
                    .getLocation());
    }

    private String restoreState()
    {
        if (memento == null) return null;
        String res = (memento.getChild("niche") != null) ? memento.getChild(
                "niche").getID() : null;
        memento = null;
        return res;
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus()
    {
        if (swingControl != null) swingControl.setFocus();
    }

    void setWinTitle(String title)
    {
        final Shell window = getSite().getShell();
        window.setText(title);
    }

    private void makeActions()
    {
        IActionBars bars = getViewSite().getActionBars();
        IMenuManager manager = bars.getMenuManager();
        manager.add(EclipseActions.getAction(EclipseActions.OPEN_NICHE_ACTION));
        manager.add(EclipseActions.getAction(EclipseActions.CLOSE_NICHE_ACTION));
        manager.add(new Separator());
        manager.add(EclipseActions.getAction(EclipseActions.ABOUT_ACTION));
    }

    private class NoNicheGUI extends Composite
    {
        private Button noNicheButton;
        private Label text;

        public NoNicheGUI(final Composite parent, int style)
        {
            super(parent, style);
            setLayout(new FormLayout());
            setBackground(new org.eclipse.swt.graphics.Color(getDisplay(), 255,
                    255, 255));
            text = new Label(this, SWT.WRAP);
            text.setText("No niche specified.\n " + "Please select one.");
            text.setBackground(new org.eclipse.swt.graphics.Color(getDisplay(),
                    255, 255, 255));
            org.eclipse.swt.graphics.Font font = new org.eclipse.swt.graphics.Font(
                    getDisplay(), new FontData("Times New Roman", 16, 0));
            text.setFont(font);
            FormData formData = new FormData();
            formData.top = new FormAttachment(5, 5);
            formData.left = new FormAttachment(5, 5);
            text.setLayoutData(formData);

            noNicheButton = new Button(this, SWT.PUSH);
            formData = new FormData();
            formData.top = new FormAttachment(5, 5);
            formData.left = new FormAttachment(text, 5);
            noNicheButton.setLayoutData(formData);

            SelectionAdapter adapter = new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event)
                {
                	 	
                    IWorkbench win = PlatformUI.getWorkbench();
                    PluginU.openNichesDlg(win.getActiveWorkbenchWindow());
                }
            };
            noNicheButton.addSelectionListener(adapter);
            noNicheButton.setText("Select");
            noNicheButton.setFont(font);
        }

    }

    static class GoToDeclarationAction extends AbstractAction
    {

        public GoToDeclarationAction()
        {
            super();
            putValue(AbstractAction.NAME, "Go to Declaration");
            putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_F3, 0));
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
            try
            {
                if (ui.getSelectionStart() != ui.getSelectionEnd())
                {
                    resolveAndOpen(sup, doc.getText(ui.getSelectionStart(),
                            ui.getSelectionEnd() - ui.getSelectionStart()), pos);
                    return;
                }
                //TODO: some NB bug to fix 
                int start = getWordStart(ui.getDoc(), pos) + 1;
                int end = getWordEnd(ui.getDoc(), pos);
                resolveAndOpen(sup, doc.getText(start, end - start), pos);
            }
            catch (Exception ex)
            {

            }
        }
        
        private void resolveAndOpen(ScriptSupport sup, String s, int pos)
        {
            if (sup.getParser() == null)
            {
                PluginU.searchAndOpen(s);
                return;
            }
            // int offset = pos - start;
            int dot = s.lastIndexOf(".");
            if (dot > pos) s = s.substring(0, dot);
            BshAst bsh = (BshAst) sup.getParser();
            if (s.indexOf("(") > 0)
            {
                Method m = bsh.resolveMethod(s, pos);
                if (m != null)
                {
                    IMethod im = PluginU.getIMethod(m);
                    if (im != null) PluginU.openInEditor(im);
                }
            }
            else
            {
                Class<?> cls = bsh.resolveVarAsClass(s, pos);
                if (cls != null)
                    PluginU.searchAndOpen(cls.getName());
                else
                    PluginU.searchAndOpen(s);
            }
        }
    };

    public static int getWordStart(NotebookDocument doc, int offset)
            throws BadLocationException
    {
        return Utilities.find(doc, new FinderFactory.AcceptorBwdFinder(
                new FwdAcceptor()
                ), offset, 0);
    }

    public static int getWordEnd(NotebookDocument doc, int offset)
            throws BadLocationException
    {
        int ret = Utilities.find(doc,
                new SmartFwdFinder(), offset,
                -1);
        return (ret > 0) ? ret : doc.getLength();
    }
    
    static class SmartFwdFinder extends FinderFactory.AcceptorFwdFinder
    {
        public SmartFwdFinder()
        {
            super(new FwdAcceptor());
        }
    }
    
    static class FwdAcceptor implements Acceptor
    {
        int numParanthes = 0;
        @Override
        public boolean accept(char ch)
        {
            if(ch == '(')
                numParanthes++;
            if(ch == ')')
                numParanthes--;
            if(numParanthes == 0 && (ch == '.'|| ch == ' ' || ch == ','))
                return false;
            return ch != ';' && ch != '\n' && ch != '=';
        }
    }
    
//    static class SmartBwdAcceptor implements Acceptor
//    {
//        @Override
//        public boolean accept(char ch)
//        {
//            return ch != ';' && ch != '\n' && ch != '=';
//        }
//    }

}