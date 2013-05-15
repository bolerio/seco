package seco.server;

import java.util.concurrent.Future;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.peer.HyperGraphPeer;

import seco.U;
import seco.storage.swing.types.SwingType;
import seco.storage.swing.types.SwingTypeConstructor;
import mjson.Json;

public class SecoServer
{
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out
					.println("Usage seco.server.SecoServer <json-config-file>");
			System.exit(-1);
		}
		try
		{
			Json config = Json.read(U.getFileContentAsString(args[0]));
			// HyperGraph graph = HGEnvironment.get(config.at("db").asString());
			HyperGraphPeer peer = new HyperGraphPeer(config);

			Future<Boolean> startupResult = peer.start();
			if (startupResult.get())
			{
	            
	            HGPersistentHandle pHandle = peer.getGraph().getHandleFactory().makeHandle("ae9e93e7-07c9-11da-831d-8d375c1471ff");
	            SwingTypeConstructor type = new SwingTypeConstructor();
	                type.setHyperGraph(peer.getGraph());
	                peer.getGraph().getTypeSystem().addPredefinedType(pHandle, type, SwingType.class);
	                seco.boot.NicheManager.populateSecoTypes(peer.getGraph());
				System.out.println("Peer started successfully. Hit Ctrl-C to stop...");
				while (true);
			}
			else
			{
				System.out.println("Peer failed to start.");
				peer.getStartupFailedException().printStackTrace(System.err);
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
		}
	}
}