/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.event.HGEvent;
import org.hypergraphdb.event.HGListener;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.hypergraphdb.type.HGAtomType;

import seco.notebook.AppForm;
import seco.notebook.CellDocument;
import seco.notebook.CellDocumentType;
import seco.notebook.GUIHelper;
import seco.notebook.NotebookDocument;
import seco.notebook.NotebookDocumentType;
import seco.notebook.NotebookUI;
import seco.notebook.NotebookUIType;
import seco.notebook.PiccoloFrame;
import seco.notebook.storage.swing.SwingTypeMapper;
import seco.notebook.storage.swing.types.SwingType;
import seco.notebook.storage.swing.types.SwingTypeConstructor;
import seco.rtenv.RuntimeContext;
import seco.things.Cell;
import seco.things.CellGroup;
import seco.things.CellGroupType;
import seco.things.CellType;
import seco.things.CellVisual;
import seco.things.HGClassType;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class NicheBootListener implements HGListener
{
    private void loadPredefinedTypes(HyperGraph graph)
    {
        HGPersistentHandle handle = HGHandleFactory
                .makeHandle("0b4503c0-dcd5-11dd-acb1-0002a5d5c51b");
        HGAtomType type = new HGClassType();
        type.setHyperGraph(graph);
        graph.getTypeSystem().addPredefinedType(handle, type, Class.class);
        graph.getIndexManager().register(new ByPartIndexer(handle, "name"));
    }

    private String getNicheName(HyperGraph hg)
    {
        return (String) hg.get(ThisNiche.NICHE_NAME_HANDLE);
    }

    private JFrame loadTopFrame(HyperGraph hg)
    {
        JFrame result = seco.notebook.AppForm.getInstance().loadFrame();

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        result.setLocationRelativeTo(null);
        result.setLocation(0, 0);
        result.setSize(3 * (dim.width / 4), 3 * (dim.height) / 4);
        return result;
    }

    public Result handle(HyperGraph hg, HGEvent event)
    {
        // Initialize the niche core objects.
        ThisNiche.hg = hg;
        loadPredefinedTypes(hg);
        ThisNiche.name = getNicheName(hg);
        RuntimeContext topRuntime = (RuntimeContext) ThisNiche.hg
                .freeze(ThisNiche.TOP_CONTEXT_HANDLE);

        initSwingStuff(hg);
        initSecoStuff(hg);

        if (AppForm.PICCOLO)
        {
            PiccoloFrame s = PiccoloFrame.getInstance();
            topRuntime.getBindings().put("desktop", AppForm.getInstance());
            ThisNiche.hg.update(topRuntime);
//            s.loadComponents();
//            AppForm.getInstance().openBooks();
//            ThisNiche.topContext = ThisNiche
//                    .getEvaluationContext(ThisNiche.TOP_CONTEXT_HANDLE);
//            s.getCanvas().loadDims();
            CellGroup group = (CellGroup) hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE);
            CellVisual v = (CellVisual) hg.get(group.getVisual());
            v.bind(group, null);
            s.setVisible(true);
            return Result.ok;
        }

        // Create the top-level window.
        final JFrame topFrame = loadTopFrame(hg);
        topFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        topRuntime.getBindings().put("desktop", topFrame);
        ThisNiche.topContext = ThisNiche
                .getEvaluationContext(ThisNiche.TOP_CONTEXT_HANDLE);
        //((seco.notebook.AppForm) topFrame).openBooks();

        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                topFrame.setVisible(true);
            }
        });

        return Result.ok;
    }

    private static final String pHandleStr = "ae9e93e7-07c9-11da-831d-8d375c1471ff";

    private void initSwingStuff(HyperGraph hg)
    {
        SwingTypeMapper stm = new SwingTypeMapper();
        stm.setHyperGraph(hg);
        hg.getTypeSystem().getJavaTypeFactory().getMappers().add(0, stm);
        HGPersistentHandle pHandle = HGHandleFactory.makeHandle(pHandleStr);
        if (hg.get(pHandle) == null)
        {
            SwingTypeConstructor type = new SwingTypeConstructor();
            type.setHyperGraph(hg);
            hg.getTypeSystem()
                    .addPredefinedType(pHandle, type, SwingType.class);
        }
    }

    private void initSecoStuff(HyperGraph hg)
    {
        HGTypeSystem ts = hg.getTypeSystem();
        if (ts.getType(CellGroupType.HGHANDLE) == null)
        {
            HGAtomType type = new CellGroupType();
            type.setHyperGraph(hg);
            ts.addPredefinedType(CellGroupType.HGHANDLE, type, CellGroup.class);
            type = new CellType();
            type.setHyperGraph(hg);
            ts.addPredefinedType(CellType.HGHANDLE, type, Cell.class);
            type = new NotebookDocumentType();
            type.setHyperGraph(hg);
            ts.addPredefinedType(NotebookDocumentType.HGHANDLE, type,
                    NotebookDocument.class);
            type = new NotebookUIType();
            type.setHyperGraph(hg);
            ts.addPredefinedType(NotebookUIType.HGHANDLE, type,
                    NotebookUI.class);
            type = new CellDocumentType();
            type.setHyperGraph(hg);
            ts.addPredefinedType(CellDocumentType.HGHANDLE, type,
                    CellDocument.class);
        }
        if(hg.get(ThisNiche.TOP_CELL_GROUP_HANDLE) == null)
           GUIHelper.makeTopCellGroup(hg);
    }
}
