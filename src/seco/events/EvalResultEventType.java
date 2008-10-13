package seco.events;

import javax.swing.undo.AbstractUndoableEdit;

import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;

public final class EvalResultEventType extends AbstractUndoableEdit
{
	public static final HGPersistentHandle HANDLE = HGHandleFactory.makeHandle("533c6383-932d-11dc-8bd1-0019b91e4d7d");

}