package sacip.sti.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sacip.Launcher;
import sacip.sti.dataentities.Student;

public class TrackingAgent extends Agent implements MessageListener {

	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);
	private int instancia;

	public TrackingAgent() {
		super();
		this.instancia = Launcher.instancia;
	}

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {

		if (service.equals("storeData")) 
		{
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
		else if(service.equals("storeSolvedExercise"))
		{
			JsonNode dados = (JsonNode) in.get("dados");
			String nome =  dados.get("nome").asText();

			try {
				ServiceWrapper wrapper = require("SACIP", "editStudentListAttr");
				wrapper.addParameter("name", nome);
				wrapper.addParameter("attrName", "exerciciosResolvidos");
				wrapper.addParameter("newValue", dados.get("conteudo"));
				out.add(wrapper.run().get(0));
			} catch (Exception e) {
				out.add(e.getLocalizedMessage());
				LOG.error("ERRO NO TRACKING AGENT AO ENVIAR DADOS", e);
				e.printStackTrace();
			}
		}
		else if(service.equals("storeContentOnPath"))
		{
			JsonNode dados = (JsonNode) in.get("dados");
			String nome =  dados.get("nome").asText();
			
			try {
				ServiceWrapper wrapper = require("SACIP", "editStudentListAttr");
				wrapper.addParameter("name", nome);
				wrapper.addParameter("attrName", "trilha");
				wrapper.addParameter("newValue", dados.get("conteudo"));
				out.add(wrapper.run().get(0));
			} catch (Exception e) {
				out.add(e.getLocalizedMessage());
				LOG.error("ERRO NO TRACKING AGENT AO ENVIAR DADOS", e);
				e.printStackTrace();
			}
		}
		else if(service.equals("storeStudentErrors"+this.instancia))
		{
			JsonNode dados = (JsonNode) in.get("dados");
			String nome =  dados.get("nome").asText();
			
			try {
				ServiceWrapper wrapper = require("SACIP", "editStudentListAttr");
				wrapper.addParameter("name", nome);
				wrapper.addParameter("attrName", "errosDoEstudante");
				wrapper.addParameter("newValue", dados.get("conteudo"));
				out.add(wrapper.run().get(0));
			} catch (Exception e) {
				out.add(e.getLocalizedMessage());
				LOG.error("ERRO NO TRACKING AGENT AO ENVIAR DADOS", e);
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {

		Board.addMessageListener("SACIP", this);
		
		while(alive)
		{
			LOG.info("INSTANCIA "+this.instancia+" viva");
			Thread.sleep(1000);
			try 
			{
				// ServiceWrapper wrapper = require("SACIP", "getAluno");
				// Student estudante = (Student) wrapper.run();
				// descobrirModulosMaisUtilizados(estudante);
				// descobrirTagsMaisUtilizadas(estudante);
				// descobrirTopicosMaisUtilizados(estudante);
				// verificarFrequenciaDoAluno(estudante);
				// descobrirTempoGastoPorTopico(estudante);
				// descobrirTempoGastoPorTag(estudante);
				// descobrirTempoGastoPorModulo(estudante);
				// descobrirTiposDeExercicioQueMelhorEPiorSaiu(estudante);
				
			} catch (Exception e) {
				
				LOG.error("Ocorreu um erro no ciclo de vida do Agente Rastreador", e);
			}			
		}
		
	}
	
	@Override
	public void boardChanged(Message msg) {
		// TODO Auto-generated method stub
		
	}

	private void descobrirTempoGastoPorTag(Student estudante) {
		//Analisar os conteudos usados, horarios de entrada e saida
	}

	private void descobrirTempoGastoPorModulo(Student estudante) {
		//Analisar os conteudos usados, horarios de entrada e saida
	}

	private void descobrirTempoGastoPorTopico(Student estudante) {

		//Analisar os conteudos usados, horarios de entrada e saida

	}

	private void descobrirTiposDeExercicioQueMelhorEPiorSaiu(Student estudante) {

		//Analisar trilhas, exercicios resolvidos e erros

	}

	private void verificarFrequenciaDoAluno(Student estudante) {

		//buscar as datas de entrada e saida do sistema do aluno

	}

	private void descobrirTopicosMaisUtilizados(Student estudante) {

		//buscar na trilha os Topicos de cada conteudo
	}

	private void descobrirTagsMaisUtilizadas(Student estudante) {

		//buscar na trilha as tags de cada conteudo

	}

	private void descobrirModulosMaisUtilizados(Student estudante) {
		
		//buscar cada um dos cliques do alunos e verificar o tempo total

	}

}
