package seco.eclipse;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.albireo.core.AwtEnvironment;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import seco.ThisNiche;
import seco.boot.NicheManager;
import seco.boot.NicheSelectDialog;
import seco.rtenv.ClassPath;
import seco.rtenv.ClassPathEntry;

public class PluginU {
	private static SecoView view;

	public static SecoView getSecoView() {
		if (view != null)
			return view;

		IWorkbench win = PlatformUI.getWorkbench();
		if (win.getActiveWorkbenchWindow() == null)
			return null;
		IWorkbenchPage activePage = win.getActiveWorkbenchWindow()
				.getActivePage();
		IViewPart part = (activePage != null) ? activePage
				.findView(SecoView.ID) : null;
		return part instanceof SecoView ? view = (SecoView) part : null;
	}

	public static void showMessage(String message) {
		if (getSecoView() != null)
			MessageDialog.openInformation(getSecoView().getSite().getShell(),
					"Seco", message);
	}

	public static void showError(final String message) {
		runInEclipseGUIThread(new Runnable() {
			public void run() {
				Shell shell = getSecoView() != null ? getSecoView().getSite()
						.getShell() : null;
				MessageDialog.openError(shell, "Seco", message);
			}
		});
	}

	public static void openNichesDlg(IWorkbenchWindow window) {
		AwtEnvironment env = AwtEnvironment.getInstance(window.getShell()
				.getDisplay());
		final Map<String, File> niches = NicheManager.readNiches();
		RunnableWithResult run = new RunnableWithResult() {
			NicheSelectDialog dlg;

			public void run() {
				dlg = new NicheSelectDialog();
				dlg.setNiches(niches);
				dlg.setVisible(true);
			}

			public Object getResult() {
				return dlg.getSucceeded() ? niches.get(dlg.getSelectedNiche())
						.getAbsolutePath() : null;
			}

		};
		env.invokeAndBlockSwt(run);
		if (run.getResult() != null) {
			String nicheLocation = (String) run.getResult();
			try {
				SecoPlugin.getDefault().setNicheLocation(nicheLocation);
			} catch (Throwable t) {
				showError(t.toString());
			}
		}
	}

	public static void openSecoView() {
		IWorkbench win = PlatformUI.getWorkbench();
		IWorkbenchPage activePage = win.getActiveWorkbenchWindow()
				.getActivePage();
		try {
			if (activePage != null)
				view = (SecoView) activePage.showView(SecoView.ID);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static void hideSecoView(){
		SecoView view = getSecoView();
	    view.getSite().getPage().hideView(view);
	}

	interface RunnableWithResult extends Runnable {
		Object getResult();
	}

	public static List<TypeNameMatch> findClass(String name) {
		final ArrayList<TypeNameMatch> list = new ArrayList<TypeNameMatch>();
		TypeNameMatchRequestor requestor = new TypeNameMatchRequestor() {
			public void acceptTypeNameMatch(TypeNameMatch match) {
				list.add(match);
			}
		};
		SearchEngine engine = new SearchEngine((WorkingCopyOwner) null);
		int flags = SearchPattern.R_EXACT_MATCH;
		// | SearchPattern.R_PREFIX_MATCH
		// | SearchPattern.R_PATTERN_MATCH
		// | SearchPattern.R_CAMELCASE_MATCH
		// | SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
		String pck = name.indexOf(".") > 0 ? name.substring(0,
				name.lastIndexOf('.')) : null;
		if (pck != null)
			name = name.substring(name.lastIndexOf('.') + 1);
		try {
			engine.searchAllTypeNames(pck != null ? pck.toCharArray() : null,
					SearchPattern.R_EXACT_MATCH, name.toCharArray(), flags,
					IJavaSearchConstants.TYPE,
					SearchEngine.createWorkspaceScope(), requestor,
					IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return list;
	}

	public static void searchAndOpen(String name) {
		List<TypeNameMatch> list = findClass(name);
		if (!list.isEmpty()) {
			// trying to find .java file to open
			for (TypeNameMatch m : list)
				if (m.getType().getClass().getName().indexOf("SourceType") > 0) {
					openInEditor(m.getType());
					return;
				}
			// just open the first .class file
			openInEditor(list.get(0).getType());
		}
	}

	public static void openInEditor(final IJavaElement el) {
		runInEclipseGUIThread(new Runnable() {
			public void run() {
				try {
					JavaUI.openInEditor(el, true, true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	public static void runInEclipseGUIThread(Runnable r) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(r);
	}

	public static void setStatusLineMsg(final String message) {
		final Display display = Display.getDefault();

		new Thread() {

			public void run() {

				display.syncExec(new Runnable() {

					public void run() {

						IWorkbench wb = PlatformUI.getWorkbench();
						IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

						IWorkbenchPage page = win.getActivePage();

						IWorkbenchPart part = page.getActivePart();
						IWorkbenchPartSite site = part.getSite();

						IViewSite vSite = (IViewSite) site;

						IActionBars actionBars = vSite.getActionBars();

						if (actionBars == null)
							return;

						IStatusLineManager statusLineManager = actionBars
								.getStatusLineManager();

						if (statusLineManager == null)
							return;

						statusLineManager.setMessage(message);
					}
				});
			}
		}.start();
	}

	/**
	 * makes a window request a users attention
	 * 
	 * @param tempMessage
	 *            a message for the user to know why attention is needed
	 */
	static void requestUserAttention(String tempMessage) {
		if (getSecoView() == null)
			return;
		IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getSecoView()
				.getSite().getService(IWorkbenchSiteProgressService.class);
		// notify the user by turning the workbench part's title bold
		service.warnOfContentChange();

		// rate at which the title will change in milliseconds
		int rateOfChange = 1000;

		final Shell window = getSecoView().getSite().getShell();
		// flash n times and thats it
		int n = 5;
		final String orgText = window.getText();
		final String message = tempMessage;

		window.setData("requestUserAttention", true);
		window.addShellListener(new ShellAdapter() {
			@Override
			public void shellActivated(ShellEvent e) {
				window.setData("requestUserAttention", false);
			}
		});
		for (int x = 0; x < n; x++) {
			window.getDisplay().timerExec(2 * rateOfChange * x - rateOfChange,
					new Runnable() {
						@Override
						public void run() {
							if (((Boolean) window
									.getData("requestUserAttention"))) {
								window.setText(message);
							}
						}
					});
			window.getDisplay().timerExec(2 * rateOfChange * x, new Runnable() {
				@Override
				public void run() {
					if (((Boolean) window.getData("requestUserAttention"))
							|| window.getText().equals(message)) {
						window.setText(orgText);
					}
				}
			});
		}
	}

	public static IType getClassIType(Class<?> cls) {
		// final ArrayList<SearchMatch> list = new ArrayList<SearchMatch>();
		// SearchRequestor requestor = new SearchRequestor() {
		// public void acceptSearchMatch(SearchMatch match)
		// {
		// list.add(match);
		// }
		// };
		// SearchPattern pat = SearchPattern.createPattern(cls.getName(),
		// IJavaSearchConstants.CLASS_AND_INTERFACE,
		// IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		// SearchEngine engine = new SearchEngine((WorkingCopyOwner) null);
		// try{
		// engine.search(pat, new SearchParticipant[] {
		// SearchEngine.getDefaultSearchParticipant()},
		// SearchEngine.createWorkspaceScope(), requestor, null);
		// }catch(Exception ex)
		// {
		//
		// }
		// return (IJavaElement) list.get(0).getElement();
		List<TypeNameMatch> list = findClass(cls.getName());
		if (list.isEmpty())
			return null;
		// trying to find .java file to open
		for (TypeNameMatch m : list)
			if (m.getType().getClass().getName().indexOf("SourceType") > 0) {
				return m.getType();
			}
		return list.get(0).getType();
	}

	public static IMethod getIMethod(Method m) {
		IType cl = getClassIType(m.getDeclaringClass());
		if (cl == null)
			return null;
		String[] params = new String[m.getParameterTypes().length];
		for (int i = 0; i < params.length; i++) {
			params[i] = PluginU.getClassIType(m.getParameterTypes()[i])
					.getElementName();
		}

		return cl.getMethod(m.getName(), params);
	}

	@SuppressWarnings("restriction")
	public static boolean addEclipseProjectToSecoRuntimeContext(String projectName) {
		IJavaProject jp = JavaModelManager.getJavaModelManager().getJavaModel()
				.getJavaProject(projectName);
		if (jp == null)
			return true;
		try {
			IResource pr = jp.getCorrespondingResource();
			IClasspathEntry[] cp = jp.getRawClasspath();
			ClassPath seco_cp = ThisNiche.getTopContext().getRuntimeContext()
					.getClassPath();
			IFileStore store = ((Workspace) ResourcesPlugin.getWorkspace())
					.getFileSystemManager().getStore(pr);
			for (int i = 0; i < cp.length; i++) {
				if (cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY
						&& cp[i].getContentKind() == IPackageFragmentRoot.K_BINARY) {
					File f = new File("" + store.getFileStore(cp[i].getPath()));
					seco_cp.add(new ClassPathEntry(f));
				} else if (cp[i].getEntryKind() == IClasspathEntry.CPE_SOURCE
						&& cp[i].getContentKind() == IPackageFragmentRoot.K_SOURCE) {
					// when cp's outputLocation is null, the project's one is
					// used
					File f = (cp[i].getOutputLocation() != null) ? new File(""
							+ store.getFileStore(cp[i].getOutputLocation()))
							: new File(
									"" + store.getFileStore(jp
													.getOutputLocation()));
					seco_cp.add(new ClassPathEntry(f));
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
		return true;
	}

	public static class ResourceChangeLIstener implements
			IResourceChangeListener {
		public void resourceChanged(IResourceChangeEvent event) {
			//if (IResourceDelta.NO_CHANGE != event.getDelta().getKind())
			//	System.out.println("resourceChanged123: " + event + " delta: "
			//			+ event.getDelta());
			// ThisNiche.getTopContext().getRuntimeContext().getBindings().put("event",
			// event);
			try{
				Visitor vis = new Visitor();
			    event.getDelta().accept(vis);
			    //TODO:
			    if(!vis.added.isEmpty())
			    	;
			}catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	public static  class Visitor implements IResourceDeltaVisitor 
	{
		Set<IResourceDelta> added = new HashSet<IResourceDelta>();
	     public boolean visit(IResourceDelta delta) {
	         switch (delta.getKind()) {
	         case IResourceDelta.ADDED :{
	             if("class".equals(delta.getResource().getFileExtension()))
	                added.add(delta);
	             break;}
	         case IResourceDelta.REMOVED :
	             // handle removed resource
	             break;
	         case IResourceDelta.CHANGED :
	             // handle changed resource
	             break;
	         }
	     return true;
	     }
	 }
	
}
