package seco.boot;

import java.io.BufferedReader;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.JComponent;

import org.hypergraphdb.HGConfiguration;
import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.event.HGListenerAtom;
import org.hypergraphdb.event.HGOpenedEvent;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.hypergraphdb.type.HGAtomType;
import org.hypergraphdb.type.JavaTypeFactory;

import seco.AppConfig;
import seco.ThisNiche;
import seco.U;
import seco.actions.CommonActions;
import seco.gui.GUIHelper;
import seco.gui.visual.CellContainerVisual;
import seco.gui.visual.JComponentVisual;
import seco.gui.visual.NBUIVisual;
import seco.gui.visual.TabbedPaneVisual;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookUI;
import seco.notebook.OutputCellDocument;
import seco.notebook.ScriptletDocument;
import seco.rtenv.RuntimeContext;
import seco.rtenv.SEDescriptor;
import seco.rtenv.SELoader;
import seco.storage.notebook.NotebookDocumentType;
import seco.storage.notebook.NotebookUIType;
import seco.storage.notebook.OutputCellDocumentType;
import seco.storage.notebook.ScriptletDocumentType;
import seco.storage.swing.SwingTypeMapper;
import seco.storage.swing.types.SwingType;
import seco.storage.swing.types.SwingTypeConstructor;
import seco.things.AvailableVisual;
import seco.things.ByAttributeIndexer;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupMember;
import seco.things.CellGroupType;
import seco.things.CellType;
import seco.things.DefaultVisual;
import seco.things.HGClassType;

/**
 * <p>
 * This class deals solely with the creation and initialization of new niches. All predefined, hard-coded
 * data and logic is embedded in this class.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class NicheManager
{
    public static final String NICHELIST = ".secoNiches";
    static File nichesFile = new File(new File(U.findUserHome()), NICHELIST);
    
    public static boolean firstTime = false;
    
    public static Map<String, File> readNiches()
    {
        HashMap<String, File> niches = new HashMap<String, File>();
        try
        {
            niches.clear();
            if (!nichesFile.exists())
            {
            	firstTime = true;
                return niches;
            }
            FileReader reader = new FileReader(nichesFile);
            BufferedReader in = new BufferedReader(reader);
            for (String line = in.readLine(); line != null; line = in.readLine())
            {
                line = line.trim();
                if (line.length() == 0)
                    continue;
                String [] tokens = line.split(",");
                if (tokens.length != 2)
                    continue;
                File location = new File(U.unquote(tokens[1]));
                niches.put(U.unquote(tokens[0]), location);
            }
            in.close();
            reader.close();
            return niches;
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
            throw new RuntimeException(t);
        }
    }

    public static void saveNiches(Map<String, File> niches)
    {
        try
        {
        	FileWriter out = new FileWriter(nichesFile);
            for (Map.Entry<String, File> e : niches.entrySet())
            {
                out.write(U.quote(e.getKey()));
                out.write(",");
                out.write(U.quote(e.getValue().getAbsolutePath()));
                out.write("\n");
            }
            out.close();
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
            throw new RuntimeException(t);
        }
    }

    /**
     * <p>Recursively delete a directory with all its contents.</p>
     * @param dir
     */
    public static void deleteDirectory(File dir)
    {
        try
        {
            for (File f : dir.listFiles())
                if (f.isDirectory())
                    deleteDirectory(f);
                else
                    f.delete();
            dir.delete();
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);            
        }
    }
    
    public static boolean isLocationOk(File location)
    {
        if (!location.exists())
            return true;
        else if (!location.isDirectory())
            return false;
        else if (location.list().length > 0)
            return false;
        else
            return true;
    }
    
    /**
     * <p>Test whether a given location on the file system is a "niche" HyperGraphDB.</p>
     */
    public static boolean isNicheLocation(File location)
    {
        if (!HGEnvironment.exists(location.getAbsolutePath()))
        	return false;
        else
        {
        	HyperGraph hg = null;
        	try
        	{
        		HGConfiguration config = new HGConfiguration();
        		config.setSkipOpenedEvent(true);
        		config.setSkipMaintenance(true);
        		hg = HGEnvironment.get(location.getAbsolutePath(), config);
        		return hg.get(ThisNiche.NICHE_NAME_HANDLE) != null &&
        			   hg.get(ThisNiche.TOP_CONTEXT_HANDLE) != null;
        	}
        	catch (Throwable T)
        	{
        		T.printStackTrace(System.err);
        		return false;
        	}
        	finally
        	{
        		if (hg != null)
        			try { hg.close(); } catch (Throwable t) {}        		
        	}
        }
    }
	
    public static void loadPredefinedTypes(HyperGraph graph)
    {
        HGPersistentHandle handle = graph.getHandleFactory()
                .makeHandle("0b4503c0-dcd5-11dd-acb1-0002a5d5c51b");
        HGAtomType type = new HGClassType();
        type.setHyperGraph(graph);
        graph.getTypeSystem().addPredefinedType(handle, type, Class.class);
        graph.getIndexManager().register(new ByPartIndexer(handle, "name"));
        
        //
        // Handling of swing types.
        //
        SwingTypeMapper stm = new SwingTypeMapper();
        stm.setHyperGraph(graph);
        JavaTypeFactory f =
            (JavaTypeFactory) graph.getTypeSystem().getJavaTypeFactory();
        f.getMappers().add(0, stm);      
        HGPersistentHandle pHandle = graph.getHandleFactory().makeHandle("ae9e93e7-07c9-11da-831d-8d375c1471ff");
        if (graph.get(pHandle) == null)
        {
        	type = new SwingTypeConstructor();
            type.setHyperGraph(graph);
            graph.getTypeSystem().addPredefinedType(pHandle, type, SwingType.class);
        }        
    }

    public static String getNicheName(HyperGraph hg)
    {
        return (String) hg.get(ThisNiche.NICHE_NAME_HANDLE);
    }
    
    static HGHandle assertLanguage(HyperGraph graph, String name, String factory, String [] packageNames, String editSupport)
    {
    	HGHandle h = graph.findOne(hg.and(hg.type(SEDescriptor.class), hg.eq("language", name)));
    	if (h == null)
    		h = graph.add(new SEDescriptor(name, factory, packageNames, editSupport));
    	return h;
    }
    
    static void populateDefaultScriptingLanguages(HyperGraph graph)
    {
    	// To detect which languages are present, we try to load their engine factory class. But
    	// we don't load it in the AppClassLoader because we'll get linkage violations down the lines
    	// 
    	SELoader tryLoader = new SELoader(NicheManager.class.getClassLoader(), new String[]{});     	
    	assertLanguage(graph,
	    			"beanshell", 
	    			"bsh.engine.BshScriptEngineFactoryEx",
	                new String[] { "bsh", "bsh.engine", "bsh.classpath",
	                        "bsh.collection", "bsh.reflect", "bsh.util",
	                        "bsh.commands", "bsh.reflect", "bsh.util" },
	                        "bsh.BshScriptSupportFactory");
    	assertLanguage(graph,
        		"jscheme",
                "jscheme.engine.JSchemeScriptEngineFactory",
                new String[] { "jsint", "jscheme", "jscheme.engine" },
                "seco.langs.jscheme.JSchemeScriptSupportFactory");        
    	assertLanguage(graph,
                "html", 
                null, 
                new String[0], 
                "seco.notebook.html.HTMLScriptSupportFactory");
        // Try other know languages:
    	try
    	{
    		tryLoader.loadHere("symja.engine.SymjaScriptEngineFactoryEx");
    		assertLanguage(graph, "symja", 
        			"symja.engine.SymjaScriptEngineFactoryEx",
                    new String[] { "m", "symja.engine", "org.matheclipse" },
                            "symja.SymjaScriptSupportFactory");    		
    	}
    	catch (/*ClassNotFoundException*/Throwable t) {
//    		t.printStackTrace(System.err);
    	}
        try
        {
        	tryLoader.loadHere("seco.langs.groovy.jsr.GroovyScriptEngineFactory");
        	//Class.forName("org.codehaus.groovy.jsr223.GroovyScriptEngineFactory");
        	assertLanguage(graph,"groovy",  
                                       "seco.langs.groovy.jsr.GroovyScriptEngineFactory",
                                        new String[] {},  
                                        "seco.langs.groovy.GroovyScriptSupportFactory");            
        }
        catch (/*ClassNotFoundException*/Throwable t) { }
        try
        {
        	tryLoader.loadHere("seco.langs.ruby.JRubyScriptEngineFactory");
        	assertLanguage(graph,"jruby",  
                                       "seco.langs.ruby.JRubyScriptEngineFactory",
                                        new String[] {},   
                                        "seco.langs.ruby.RubyScriptSupportFactory");
        }
        catch (/*ClassNotFoundException*/Throwable t) { }
//        try
//        {
//            Class.forName("seco.langs.javafx.jsr.JavaFXScriptEngineFactory");
//            graph.add(new SEDescriptor("javafx",   
//                                       "seco.langs.javafx.jsr.JavaFXScriptEngineFactory",   
//                                        new String[] {},      
//                                        "seco.langs.javafx.JavaFxScriptSupportFactory"));            
//        }
//        catch (/*ClassNotFoundException*/Throwable t) { }        
        try
        {
        	tryLoader.loadHere("alice.tuprologx.TuScriptEngineFactory");
        	assertLanguage(graph,"prolog",   
                                       "alice.tuprologx.TuScriptEngineFactory",   
                                        new String[] {},      
                                        "seco.langs.prolog.PrologScriptSupportFactory");            
        }
        catch (/*ClassNotFoundException*/Throwable t) { }       
        try
        {
        	tryLoader.loadHere("seco.langs.javascript.jsr.RhinoScriptEngineFactory");
        	assertLanguage(graph,"javascript",   
                                       "seco.langs.javascript.jsr.RhinoScriptEngineFactory",   
                                        new String[] {},      
                                        "seco.langs.javascript.JSScriptSupportFactory");            
        }
        catch (/*ClassNotFoundException*/Throwable t) { }
        try
        {
        	tryLoader.loadHere("seco.langs.python.jsr223.PyScriptEngineFactory");
        	assertLanguage(graph,"python",  
                                       "seco.langs.python.jsr223.PyScriptEngineFactory",
                                        new String[] {},   
                                        "seco.langs.python.PythonScriptSupportFactory");
        }
        catch (/*ClassNotFoundException*/Throwable t) { }
        try
        {
        	tryLoader.loadHere("scala.tools.nsc.interpreter.Scripted");
        	assertLanguage(graph,
        				   "scala",  
                           "scala.tools.nsc.interpreter.Scripted$Factory",
                           new String[] {"scala.tools.nsc.interpreter"},
                           "seco.langs.scala.jsr223.ScalaScriptSupportFactory");
        }
        catch (/*ClassNotFoundException*/Throwable t) { 
        	t.printStackTrace(System.err);
        }
        
    }
    
    static void populateDefaultVisuals(HyperGraph graph)
    {
    	graph.define(JComponentVisual.getHandle(), new JComponentVisual());
    	graph.define(CellContainerVisual.getHandle(), new CellContainerVisual());
    	graph.define(TabbedPaneVisual.getHandle(), new TabbedPaneVisual());
    	graph.define(NBUIVisual.getHandle(), new NBUIVisual());
    	HGHandle typeHandle = graph.getTypeSystem().getTypeHandle(JComponent.class);
    	HGHandle visualHandle = JComponentVisual.getHandle();
    	graph.add(new DefaultVisual(typeHandle, visualHandle));
    	graph.add(new AvailableVisual(typeHandle, visualHandle));
    	
        typeHandle = CellGroupType.HGHANDLE;
        visualHandle = NBUIVisual.getHandle();
        graph.add(new DefaultVisual(typeHandle, visualHandle));
        graph.add(new AvailableVisual(typeHandle, visualHandle));
    }
    
    public static void populateSecoTypes(HyperGraph hg)
    {
        HGTypeSystem ts = hg.getTypeSystem();
        if (ts.getType(CellGroupType.HGHANDLE) == null)
        {
            HGHandle baseType = ts.getTypeHandle(CellGroupMember.class);
            HGAtomType type = new CellGroupType();
            type.setHyperGraph(hg);
            ts.addPredefinedType(CellGroupType.HGHANDLE, type, CellGroup.class);
            ts.assertSubtype(baseType, CellGroupType.HGHANDLE);
            type = new CellType();
            type.setHyperGraph(hg);
            ts.addPredefinedType(CellType.HGHANDLE, type, Cell.class);
            ts.assertSubtype(baseType, CellType.HGHANDLE);
            type = new NotebookDocumentType();
            type.setHyperGraph(hg);
            ts.addPredefinedType(NotebookDocumentType.HGHANDLE, type,
                    NotebookDocument.class);
            type = new NotebookUIType();
            type.setHyperGraph(hg);
            ts.addPredefinedType(NotebookUIType.HGHANDLE, type,
                    NotebookUI.class);
            type = new OutputCellDocumentType();
            type.setHyperGraph(hg);
            ts.addPredefinedType(OutputCellDocumentType.HGHANDLE, type,
                    OutputCellDocument.class);
            type = new ScriptletDocumentType();
            type.setHyperGraph(hg);
            ts.addPredefinedType(ScriptletDocumentType.HGHANDLE, type,
                    ScriptletDocument.class);
        }
        
    }
    
    public static void populateDefaultSecoUI(HyperGraph graph)
    {
    	populateSecoTypes(graph);
        if(graph.get(ThisNiche.TOP_CELL_GROUP_HANDLE) == null)
        {
           GUIHelper.makeTopCellGroup();
        }
    }   
        
    static void populateThisNiche()
    {
    	ThisNiche.graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
    	public Object call() {
        	populateDefaultScriptingLanguages(ThisNiche.graph);
        	populateDefaultVisuals(ThisNiche.graph);
        	populateDefaultSecoUI(ThisNiche.graph);
        	return null;
    	}});
    }
    
    public static HyperGraph openNicheDatabase(File path)
    {
    	return HGEnvironment.get(path.getAbsolutePath(), U.dbConfig());
    }
    
    public static void createNiche(String name, File path)
    {
        int levelsToDeleteOnFail = 0;
        for (File existing = path; !existing.exists(); existing = existing.getParentFile())
            levelsToDeleteOnFail++;
        HyperGraph graph = null;
        try
        {
            graph = openNicheDatabase(path);
            // Scriptlet s = new Scriptlet("jscheme", "(load \"jscheme/scribaui.scm\")(install-runtime-menu)");            
          //  hg.add(new HGValueLink("on-load", new HGHandle[] {ThisNiche.TOP_CONTEXT_HANDLE, hg.add(s)}));
            HyperGraph saveHG = ThisNiche.graph; // likely, this is null, but just in case
            try
            {
            	graph.getIndexManager().register(
        				new ByAttributeIndexer<String, HGHandle>(
        						graph.getTypeSystem().getTypeHandle(CellGroupMember.class), 
        						"name", 
        						graph.getTypeSystem().getTypeHandle(String.class)));            	
                graph.add(new HGListenerAtom(HGOpenedEvent.class.getName(), 
        				  seco.boot.NicheBootListener.class.getName()));
                graph.define(ThisNiche.NICHE_NAME_HANDLE, name);
                graph.define(ThisNiche.TOP_CONTEXT_HANDLE, new RuntimeContext("top"));
                ThisNiche.bindNiche(graph);
                populateThisNiche();            	
            }
            finally
            {
            	if (saveHG != null)
            		ThisNiche.bindNiche(saveHG);
            }
            graph.close();
        }
        catch (Throwable t)
        {
            if (graph != null) try { graph.close(); } catch (Throwable ex) { }
            for (int i = 0; i < levelsToDeleteOnFail; i++)
            {
                path.delete();
                path = path.getParentFile();
            }
            if (t instanceof RuntimeException)
                throw (RuntimeException)t;
            else
                throw new RuntimeException(t);
        }
    }	
}