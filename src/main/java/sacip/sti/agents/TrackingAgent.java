package sacip.sti.agents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

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

import sacip.Launcher;
import sacip.sti.dataentities.Student;

public class TrackingAgent extends Agent implements MessageListener {

	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {

		JsonNode dados = (JsonNode) in.get("dados");
		String nome = dados.get("nome").asText();
		
		switch (service.replace(super.getPort(), "")) 
		{
			case "storeData":
				if (dados.has("cliques")) {
					out.add(storeClicks(dados, nome));
				}
				if (dados.has("conteudo")) {
					out.add(storeUsedContent(dados, nome));
				}
				break;
			
			case "storeSolvedExercise":
				out.add(addStudentData(dados.get("conteudo"), nome, "exerciciosResolvidos"));
				break;
			
			case "storeContentOnPath":
				out.add(addStudentData(dados.get("conteudo"), nome, "trilha"));
				break;
			
			case "storeStudentErrors":
				out.add(addStudentData(dados.get("conteudo"), nome, "errosDoEstudante"));
				break;
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {

		Board.addMessageListener("SACIP", this);

		while (alive) {
			LOG.info("INSTANCIA " + super.getPort() + " viva");
			
			try {
				ServiceWrapper wrapper = require("SACIP"+super.getPort(), "getAluno"+super.getPort());
				Student estudante = (Student) wrapper.run().get(0);
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
			Thread.sleep(30000);
		}

	}

	@Override
	public void boardChanged(Message msg) {
		// TODO Auto-generated method stub

	}

	private Object storeClicks(JsonNode dados, String nome)
	{
		List<String> dadosC = new ArrayList<>();
		List<String> dadosO = new ArrayList<>();
		List<String> dadosA = new ArrayList<>();
		List<String> dadosE = new ArrayList<>();
		
		if (dados.get("cliques").isArray()) {
			ArrayNode cliqueArray = (ArrayNode) dados.get("cliques");
			for (JsonNode jsonNode : cliqueArray) {
				String modulo = jsonNode.get("modulo").asText();
				switch (modulo) {
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

		Map<String, Object> dadoss = Map.of("Conteudo", dadosC, "OGPor", dadosO, "Ajuda", dadosA,
				"Exemplos", dadosE);

		try {
			ServiceWrapper wrapper = require("SACIP", "storeStudentUseData");
			wrapper.addParameter("name", nome);
			wrapper.addParameter("data", dadoss);
			return wrapper.run().get(0);
		} catch (Exception e) {
			LOG.error("ERRO NO TRACKING AGENT AO ENVIAR DADOS", e);
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
	}
	
	private Object storeUsedContent(JsonNode dados, String nome)
	{
		try {
			ServiceWrapper wrapper = require("SACIP", "storeStudentContentUse");
			wrapper.addParameter("name", nome);
			wrapper.addParameter("content", dados.get("conteudo"));
			return wrapper.run().get(0);
		} catch (Exception e) {
			LOG.error("ERRO NO TRACKING AGENT AO ENVIAR DADOS", e);
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
	}

	private Object addStudentData(JsonNode conteudo, String nome, String attrName)
	{
		try {
			ServiceWrapper wrapper = require("SACIP", "editStudentListAttr");
			wrapper.addParameter("name", nome);
			wrapper.addParameter("attrName", attrName);
			wrapper.addParameter("newValue", conteudo);
			return wrapper.run().get(0);
		} catch (Exception e) {
			LOG.error("ERRO NO TRACKING AGENT AO ENVIAR DADOS de "+attrName, e);
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
	}

	private void descobrirTempoGastoPorTag(Student estudante) throws ServiceWrapperException, InterruptedException,
			ExecutionException, JsonMappingException, JsonProcessingException 
	{

		//Fazer busca dos conteudos usados
		ServiceWrapper wrapper = require("SACIP", "getLogsDoAluno");
		wrapper.addParameter("name", estudante.getName());
		wrapper.addParameter("type", "USE");
		List resposta = wrapper.run();
		JsonNode conteudosUsados = (JsonNode) resposta.get(0);

		//Passar pelos conteudos, descobrir suas tags e atribuir os tempos a cada uma
		Map<String, Long> tagTime = new LinkedHashMap<>();
		if(conteudosUsados.isArray())
		{
			for (JsonNode contentNode : conteudosUsados) {
				JsonNode tags = contentNode.get("tags");
				Long timespent = contentNode.get("timespent").asLong(0);
				if(tags.isArray())
				{
					for (JsonNode tagNode : tags) {
						String tag = tagNode.asText();
						tagTime.merge(tag, timespent, Long::sum);
					}
				}
			}
		}
		
		//ordenar por tempo
		Map<String, Long> sortedtags = tagTime.entrySet().stream()
                         .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
						 .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
						 
		ServiceWrapper wrapper2 = require("SACIP", "editStudent");
		wrapper2.addParameter("name", estudante.getName());
		wrapper2.addParameter("attrName", "tempoTag");
		wrapper2.addParameter("newValue", sortedtags);
		wrapper2.run();
	}

	private void descobrirTempoGastoPorModulo(Student estudante)  throws ServiceWrapperException, InterruptedException, ExecutionException {
		//Analisar os conteudos usados, horarios de entrada e saida
	}

	private void descobrirTempoGastoPorTopico(Student estudante)  throws ServiceWrapperException, InterruptedException, ExecutionException {

		//Analisar os conteudos usados, horarios de entrada e saida

	}

	private void descobrirTiposDeExercicioQueMelhorEPiorSaiu(Student estudante)  throws ServiceWrapperException, InterruptedException, ExecutionException {

		//Analisar trilhas, exercicios resolvidos e erros

	}

	private void verificarFrequenciaDoAluno(Student estudante)  throws ServiceWrapperException, InterruptedException, ExecutionException {

		//buscar as datas de entrada e saida do sistema do aluno

	}

	private void descobrirTopicosMaisUtilizados(Student estudante)  throws ServiceWrapperException, InterruptedException, ExecutionException {

		//buscar na trilha os Topicos de cada conteudo
	}

	private void descobrirTagsMaisUtilizadas(Student estudante)  throws ServiceWrapperException, InterruptedException, ExecutionException {

		//buscar na trilha as tags de cada conteudo

	}

	private void descobrirModulosMaisUtilizados(Student estudante)  throws ServiceWrapperException, InterruptedException, ExecutionException {
		
		//buscar cada um dos cliques do alunos e verificar o tempo total

	}

}
