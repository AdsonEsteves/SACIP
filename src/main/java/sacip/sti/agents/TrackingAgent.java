package sacip.sti.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.midas.as.AgentServer;
import org.midas.as.agent.board.Board;
import org.midas.as.agent.board.Message;
import org.midas.as.agent.board.MessageListener;
import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;
import org.midas.as.manager.execution.ServiceWrapper;
import org.midas.as.manager.execution.ServiceWrapperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackingAgent extends Agent implements MessageListener {

	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {

		if (service.equals("storeData")) {
			JsonNode dados = (JsonNode) in.get("dados");
			if (dados.isObject()) {
				ObjectNode jsonobject = (ObjectNode) dados;
				String nome = jsonobject.get("nome").asText();
				
				List<String> dadosC = new ArrayList<>();
				List<String> dadosO = new ArrayList<>();
				List<String> dadosA = new ArrayList<>();
				List<String> dadosE = new ArrayList<>();
				if(jsonobject.has("cliques"))
				{
					if(jsonobject.get("cliques").isArray())
					{
						ArrayNode cliqueArray = (ArrayNode) jsonobject.get("cliques");
						for (JsonNode jsonNode : cliqueArray) {
							String modulo = jsonNode.get("modulo").asText();
							switch(modulo)
							{
								case "Exemplo":
									dadosE.add(jsonNode.toString());
								break;

								case "Conteudo":
									dadosC.add(jsonNode.toString());
								break;

								case "Ajuda":
									dadosA.add(jsonNode.toString());
								break;

								case "OGPor":
									dadosO.add(jsonNode.toString());
								break;
							}
						}
					}
					
					Map<String, Object> dadoss = Map.of("Conteudo", dadosC, "OGPor", dadosO, "Ajuda", dadosA, "Exemplos", dadosE);

					try {
						ServiceWrapper wrapper = require("SACIP", "storeStudentUseData");
						wrapper.addParameter("name", nome);
						wrapper.addParameter("data", dadoss);
						out.add(wrapper.run().get(0));
					} catch (Exception e) {
						out.add(e.getLocalizedMessage());
						LOG.error("ERRO NO TRACKING AGENT AO ENVIAR DADOS", e);
						e.printStackTrace();
					}
				}
				if(jsonobject.has("conteudo"))
				{
					try {
						ServiceWrapper wrapper = require("SACIP", "storeStudentContentUse");
						wrapper.addParameter("name", nome);
						wrapper.addParameter("content", jsonobject.get("conteudo"));
						out.add(wrapper.run().get(0));
					} catch (Exception e) {
						out.add(e.getLocalizedMessage());
						LOG.error("ERRO NO TRACKING AGENT AO ENVIAR DADOS", e);
						e.printStackTrace();
					}
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
