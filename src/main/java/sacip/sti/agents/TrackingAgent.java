package sacip.sti.agents;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.midas.as.agent.board.Board;
import org.midas.as.agent.board.Message;
import org.midas.as.agent.board.MessageListener;
import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;

public class TrackingAgent extends Agent implements MessageListener{

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {
		
		if(service.equals("storeData"))
		{
			JsonNode dados = (JsonNode) in.get("dados");
			Iterator<Entry<String, JsonNode>> fields = dados.fields();
			if(dados.isObject())
			{
				ObjectNode jsonobject = (ObjectNode) dados;
				Iterator<Entry<String, JsonNode>> nodes = jsonobject.get("cliques").fields();

				while (nodes.hasNext()) {
					Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodes.next();

					//logger.info("key --> " + entry.getKey() + " value-->" + entry.getValue());
				}

			}
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		// TODO Auto-generated method stub
		Board.addMessageListener("SACIP", this);
		
		// while(alive)
		// {
		// 	Thread.sleep(2000);
		// }
		
	}

	@Override
	public void boardChanged(Message msg) {
		// TODO Auto-generated method stub
		
	}

}
