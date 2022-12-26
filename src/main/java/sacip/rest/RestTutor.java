package sacip.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.type.TypeReference;
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

import sacip.sti.components.DBConnection;
import sacip.sti.dataentities.Content;

@RestController
@RequestMapping("/tutor")
public class RestTutor {

	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

	@GetMapping("/requisitaConteudos/{agentPort}")
	public String getConteudosRecomendados(@PathVariable String agentPort) {
		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP" + agentPort, "suggestContent" + agentPort);
			List run = wrapper.run();
			return run.toString();
		} catch (Exception e) {
			LOG.error("Falhou ao sugerir conteudos", e);
			return "Falhou ao sugerir conteudos: \n" + e.getLocalizedMessage();
		}
	}

	@PostMapping("/requisitaConteudos")
	public String getConteudosRecomendados2(@RequestBody JsonNode dados) {
		try {
			String content = buscarConteudos(dados);
			return content;
		} catch (Exception e) {
			LOG.error("Falhou ao sugerir conteudos", e);
			throw new IllegalStateException("falhou ao sugerir", e);
		}
	}

	private String buscarConteudos(JsonNode dados) throws IOException {
		DBConnection dbc = new DBConnection();
		Map<String, Object> attributes = new HashMap<>();

		if (dados.has("topico"))
			attributes.put("topic", dados.get("topico").asText());
		if (dados.has("tags")) {
			ArrayNode tags = ((ArrayNode) dados.get("tags"));
			attributes.put("tags", new ObjectMapper().convertValue(tags, String[].class));
		}
		List<Content> contents = (List<Content>) dbc.getContents(Map.of("topic", dados.get("topico").asText()));
		List<String> tags = new ObjectMapper().readerFor(new TypeReference<List<String>>() {
		}).readValue(dados.get("tags"));
		List<Content> finalContents = new ArrayList<>();
		finalContents.addAll(getRandomNContents(contents, 2));
		finalContents.addAll(getRandomNContents(filtrarConteudosPorTags(contents, tags), 8));

		Content content = getRandomNContents(finalContents, 1).get(0);

		return content.getName();
	}

	private List<Content> getRandomNContents(List<Content> contents, int n) {
		List<Integer> usedIndexes = new ArrayList<>();
		List<Content> returnContents = new ArrayList<>();

		if (contents.size() <= n) {
			return contents;
		}

		for (int i = 0; i < n; i++) {
			Random rand = new Random();
			int index = rand.nextInt(contents.size());
			while (usedIndexes.contains(index)) {
				index = rand.nextInt(contents.size());
			}
			usedIndexes.add(index);
			returnContents.add(contents.get(index));
		}

		return returnContents;
	}

	private List<Content> filtrarConteudosPorTags(List<Content> conteudos, List<String> caracteristicas) {
		List<Content> conteudosRecomendados = new ArrayList<>();

		for (Content content : conteudos) {

			if (!caracteristicas.stream().anyMatch(element -> content.getTags().contains(element.toLowerCase()))) {
				continue;
			}

			conteudosRecomendados.add(content);
		}

		if (conteudosRecomendados.isEmpty()) {
			return conteudos;
		}

		return conteudosRecomendados;
	}

	@GetMapping("/requisitaTrilha/{agentPort}")
	public String getTrilha(@PathVariable String agentPort) {
		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP" + agentPort, "getTrilha" + agentPort);
			List run = wrapper.run();
			return run.toString();
		} catch (Exception e) {
			LOG.error("Falhou ao pegar trilha", e);
			return "Falhou ao pegar trilha: \n" + e.getLocalizedMessage();
		}
	}

	@GetMapping("/getInfo")
	public String getInfo() {
		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP", "getInfo");
			List run = wrapper.run();
			return run.get(0).toString();
		} catch (Exception e) {
			LOG.error("Falhou ao pegar trilha", e);
			return "Falhou ao pegar trilha: \n" + e.getLocalizedMessage();
		}
	}

	@PostMapping("/buscarConteudo")
	public String getConteudos(@RequestBody JsonNode dados) {
		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP", "findContents");
			ArrayNode tags = ((ArrayNode) dados.get("tags"));
			if (dados.has("nome"))
				wrapper.addParameter("~name", dados.get("nome").asText());
			if (dados.has("topico"))
				wrapper.addParameter("topic", dados.get("topico").asText());
			if (dados.has("taxonomia"))
				wrapper.addParameter("taxonomy", dados.get("taxonomia").asText());
			if (dados.has("nivel"))
				wrapper.addParameter("level", dados.get("nivel").asText());
			if (dados.has("exercicio"))
				wrapper.addParameter("exercise", dados.get("exercicio").asBoolean());
			if (dados.has("tags"))
				wrapper.addParameter("tags", new ObjectMapper().convertValue(tags, String[].class));

			List run = wrapper.run();
			if (run.isEmpty())
				return run.toString();
			return new ObjectMapper().valueToTree(run.get(0)).toString();
		} catch (Exception e) {
			LOG.error("Falhou ao pegar trilha", e);
			return "Falhou ao pegar trilha: \n" + e.getLocalizedMessage();
		}
	}

	@PostMapping("/atualizarTrilha/{agentPort}")
	public String atualizarTrilha(@PathVariable String agentPort, @RequestBody JsonNode dados) {
		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP" + agentPort, "atualizarTrilha" + agentPort);
			wrapper.addParameter("nomeConteudo", dados.get("nomeConteudo").asText(""));
			List run = wrapper.run();
			return run.get(0).toString();
		} catch (Exception e) {
			LOG.error("Falhou ao atualizar trilha", e);
			return "Falhou ao atualizar trilha: \n" + e.getLocalizedMessage();
		}
	}

}
