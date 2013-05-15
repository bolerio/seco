package seco.server;

import static org.hypergraphdb.peer.Messages.CONTENT;

import java.util.Collection;
import java.util.HashSet;
import mjson.Json;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.annotation.HGTransact;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.SubgraphManager;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.WorkflowState;
import org.hypergraphdb.storage.StorageGraph;

/**
 * This activity deals with saving and indexing primitive graphs in a database
 * that's used purely as a storage back-end/backup for Seco clients. Because
 * each client may have their own HGDB types that don't have to be present everywhere,
 * the server is not going to fully instantiate the primitive graph it is receiving.
 * It doesn't need to instantiate it at all. It needs to index it somehow so that it
 * is searchable. But since it can't be instantiated, the indexing data must be provided
 * by the client. In addition, any types auto-generated from Java classes at the client
 * will have to be mapped to the corresponding types to whatever other client retrieves 
 * that primitive graph. For that to work, the server needs to store the class <-> HGDB handle
 * mappings "contextualized" by each of its clients. 
 * 
 * 
 * @author borislav
 *
 */
public class PrimitiveGraphIndexActivity extends FSMActivity
{
    public static final String TYPENAME = "primitive-graph-index";
    private HGPeerIdentity target;
    private HGHandle atom;
    private String [] tags;
    
    public PrimitiveGraphIndexActivity() { }
    
    public PrimitiveGraphIndexActivity(HyperGraphPeer thisPeer, HGPeerIdentity target, HGHandle atom, String...tags)
    {
        super(thisPeer);
        this.target = target;
        this.atom = atom;
        this.tags = tags;
    }
    
    @Override
    public void initiate()
    {
        HyperGraph graph = getThisPeer().getGraph();
        Json msg = createMessage(Performative.Request, this)
	        		.set(CONTENT, SubgraphManager.getTransferAtomRepresentation(graph, atom)
	        		.set("tags", Json.array(tags)));
        send(target, msg);
    }
    
    @HGTransact("write")
	@FromState("Started")
	@OnMessage(performative="Request")
	public WorkflowState saveStorageGraph(Json msg)
	{
		Json tagstrings = msg.at("content").at("tags");
		StorageGraph sgraph = SubgraphManager.decodeSubgraph(msg.at("content").at("storage-graph").asString());
		NicheUpdateAction uaction = new NicheUpdateAction(getThisPeer().getGraph(), sgraph);
		GUpdate update = uaction.makeUpdate();
		if (update != null)
		{
			HGPeerIdentity peerId = getThisPeer().getIdentity(Messages.getSender(msg));
			update.setPeerId(peerId.getId());
			//System.out.println(hg.getAll(getThisPeer().getGraph(), hg.type(GUpdate.class)));			
			Collection<HGHandle> tags = new HashSet<HGHandle>();
			for (Json j : tagstrings.asJsonList())
				tags.add(hg.assertAtom(getThisPeer().getGraph(), j.asString()));
			for (HGHandle root : sgraph.getRoots())
			{
				Collection<HGHandle> C = Tagger.tag(getThisPeer().getGraph(), root, tags);
				if (!C.isEmpty())
					update.getOldTags().put(root, C);
			}
			HGHandle uhandle = getThisPeer().getGraph().add(update);			
			reply(msg, Performative.Agree, Json.object("updateId", uhandle.getPersistent().toString()));
		}
		return WorkflowState.Completed;
	}

	@FromState("Started")
	@OnMessage(performative="Agree")
	public WorkflowState saveDone(Json msg)
    {
    	return WorkflowState.Completed;
    }
    
    @Override
    public String getType()
    {
        return TYPENAME;
    }	
}