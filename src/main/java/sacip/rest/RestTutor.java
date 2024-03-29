package sacip.rest;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.midas.as.AgentServer;
import org.midas.as.manager.execution.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tutor")
public class RestTutor {
    
	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

	@GetMapping("/requisitaConteudos/{agentPort}")
	public String getConteudosRecomendados(@PathVariable String agentPort){
		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP"+agentPort, "suggestContent"+agentPort);
			List run = wrapper.run();
			return run.toString();
		} catch (Exception e) {
			LOG.error("Falhou ao sugerir conteudos", e);
			return "Falhou ao sugerir conteudos: \n"+e.getLocalizedMessage();
		}	
	}

	@GetMapping("/requisitaTrilha/{agentPort}")
	public String getTrilha(@PathVariable String agentPort){
		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP"+agentPort, "getTrilha"+agentPort);
			List run = wrapper.run();
			return run.toString();
		} catch (Exception e) {
			LOG.error("Falhou ao pegar trilha", e);
			return "Falhou ao pegar trilha: \n"+e.getLocalizedMessage();
		}	
	}

	@GetMapping("/getInfo")
	public String getInfo(){
		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP", "getInfo");
			List run = wrapper.run();
			return run.get(0).toString();
		} catch (Exception e) {
			LOG.error("Falhou ao pegar trilha", e);
			return "Falhou ao pegar trilha: \n"+e.getLocalizedMessage();
		}	
	}

	@PostMapping("/buscarConteudo")
	public String getConteudos(@RequestBody JsonNode dados){
		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP", "findContents");
			ArrayNode tags = ((ArrayNode)dados.get("tags"));
			if(dados.has("nome"))
			wrapper.addParameter("~name", dados.get("nome").asText());
			if(dados.has("topico"))
			wrapper.addParameter("topic", dados.get("topico").asText());
			if(dados.has("taxonomia"))
			wrapper.addParameter("taxonomy", dados.get("taxonomia").asText());
			if(dados.has("nivel"))
			wrapper.addParameter("level", dados.get("nivel").asText());
			if(dados.has("exercicio"))
			wrapper.addParameter("exercise", dados.get("exercicio").asBoolean());
			if(dados.has("tags"))
			wrapper.addParameter("tags", new ObjectMapper().convertValue(tags, String[].class));
			
			List run = wrapper.run();
			if(run.isEmpty())
			return run.toString();
			return new ObjectMapper().valueToTree(run.get(0)).toString();
		} catch (Exception e) {
			LOG.error("Falhou ao pegar trilha", e);
			return "Falhou ao pegar trilha: \n"+e.getLocalizedMessage();
		}	
	}

	@PostMapping("/atualizarTrilha/{agentPort}")
	public String atualizarTrilha(@PathVariable String agentPort, @RequestBody JsonNode dados){
		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP"+agentPort, "atualizarTrilha"+agentPort);
			wrapper.addParameter("nomeConteudo", dados.get("nomeConteudo").asText(""));
			List run = wrapper.run();
			return run.get(0).toString();
		} catch (Exception e) {
			LOG.error("Falhou ao atualizar trilha", e);
			return "Falhou ao atualizar trilha: \n"+e.getLocalizedMessage();
		}		
	}

}
	
