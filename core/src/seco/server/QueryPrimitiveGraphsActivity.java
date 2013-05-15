package seco.server;

import java.util.ArrayList;
import java.util.List;

import mjson.Json;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.WorkflowState;
import org.hypergraphdb.query.And;

/**
 * <p>
 * This activity allows querying previously stored primitive graphs (via the
 * PrimitiveGraphIndexAction) by the tagging used to store them.
 * </p>
 * 
 * @author borislav
 *
 */
public class QueryPrimitiveGraphsActivity extends FSMActivity
{
    public static final String TYPENAME = "query-primitive-graphs";
    
	private HGPeerIdentity target;
	private Json query;
	private Json result;
	
	public QueryPrimitiveGraphsActivity() { }
	public QueryPrimitiveGraphsActivity(HGPeerIdentity target, Json query) 
	{
		this.target = target;
		this.query = query; 
	}
	
	@Override
	public void initiate()
	{
		send(target, createMessage(Performative.QueryRef, query));
	}
	
	@FromState("Started")
	@OnMessage(performative="QueryRef")
	public WorkflowState queryGraphs(Json msg)
	{
		HyperGraph graph = getThisPeer().getGraph();
		query = msg.at(Messages.CONTENT);
		And and = hg.and();
		if (query.has("tags"))
		{
			for (Json j : query.at("tags").asJsonList())
			{
				HGHandle tag = hg.findOne(graph, hg.eq(j.asString()));
				if (tag != null)
					tag = hg.findOne(graph, hg.and(hg.type(HGTag.class), hg.eq("tag", tag)));
				if (tag != null)
					and.add(hg.memberOf(tag));
			}
		}
		List<HGHandle> atoms = new ArrayList<HGHandle>();
		if (and.size() > 0) 
			atoms = hg.findAll(graph, and); 
		Json A = Json.array();
		for (HGHandle a : atoms)
		{
			List<HGTag> atomTags = hg.getAll(graph, hg.and(hg.type(HGTag.class), hg.contains(a)));
			Json tagList = Json.array();
			for (HGTag t : atomTags)
				tagList.add(graph.get(t.getTag()));
			System.out.println(hg.getAll(graph, hg.type(GUpdate.class)));
			GUpdate lastUpdate = hg.getOne(graph, 
					hg.and(hg.type(GUpdate.class), 
						   hg.incident(a),
						   hg.lt("timestamp", Long.MAX_VALUE)));
			A.add(Json.object("handle", a.getPersistent().toString(),
							  "tags", tagList,
							  "lastUpdate", lastUpdate == null ? null : lastUpdate.toJson()));
		}
		reply(msg, Performative.InformRef, A);
		return WorkflowState.Completed;
	}
	
	@FromState("Started")
	@OnMessage(performative="InformRef")
	public WorkflowState receiveQueryAnswer(Json msg)
	{
		result = msg.at(Messages.CONTENT);
		return WorkflowState.Completed;
	}
	
	public Json getResult()
	{
		return result;
	}
	
    @Override
    public String getType()
    {
        return TYPENAME;
    }	
}