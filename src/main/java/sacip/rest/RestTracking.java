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
		// String names[] = {"Componente Clicado", "Modulo", "Timestamp", "Usuario", "IP"};
		// String modulos[] = {"Exercicio", "ConteudoNE", "Exemplos", "OGPor", "Ajuda"};
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
			LOG.error("Ocorreu erro ao enviar ao agente Tracking", e);
			return e.getLocalizedMessage();
		}
	}

	@PostMapping("/exercicioResolvido")
	public String armazenaExercicioResolvido()
	{
		return "";
	}

	@PostMapping("/novoConteudoTrilha")
	public String armazenaNovoConteudoTrilha()
	{
		return "";
	}
}
	
