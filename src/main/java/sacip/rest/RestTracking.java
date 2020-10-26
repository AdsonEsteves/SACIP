package sacip.rest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

import org.midas.as.AgentServer;
import org.midas.as.manager.execution.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rastreador")
public class RestTracking {
    
    private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

	@PostMapping("/dadosUso")
	public String armazenaDadosUso(@RequestBody JsonNode dados){
		// String informacoesInteressantes[] = {"Modulos mais utilizados", "Tags de Conteudo mais utilizadas", "Tópicos mais utilizados", "Frequencia de entrada no sistema", "Tempo gasto por tópico",
		// 									 "Tempo gasto por tag", "Tempo gasto por Modulo", "Exercicios que foi bem?"};
		try 
		{
			ServiceWrapper wrapper = AgentServer.require("SACIP", "storeData");
			wrapper.addParameter("dados", dados);
			List out = wrapper.run();
			return (String) out.get(0);
		} 
		catch (Exception e) 
		{
			LOG.error("Ocorreu erro ao enviar dados de uso ao agente Tracking", e);
			return e.getLocalizedMessage();
		}
	}

	@PostMapping("/exercicioResolvido")
	public String armazenaExercicioResolvido(@RequestBody JsonNode dados)
	{
		try 
		{
			ServiceWrapper wrapper = AgentServer.require("SACIP", "storeSolvedExercise");
			wrapper.addParameter("dados", dados);
			List out = wrapper.run();
			return (String) out.get(0);
		} 
		catch (Exception e) 
		{
			LOG.error("Ocorreu erro ao enviar exercicio resolvido ao agente Tracking", e);
			return e.getLocalizedMessage();
		}
	}

	@PostMapping("/novoConteudoTrilha")
	public String armazenaNovoConteudoTrilha(@RequestBody JsonNode dados)
	{
		try 
		{
			ServiceWrapper wrapper = AgentServer.require("SACIP", "storeContentOnPath");
			wrapper.addParameter("dados", dados);
			List out = wrapper.run();
			return (String) out.get(0);
		} 
		catch (Exception e) 
		{
			LOG.error("Ocorreu erro ao enviar exercicio resolvido ao agente Tracking", e);
			return e.getLocalizedMessage();
		}
	}
}
	
