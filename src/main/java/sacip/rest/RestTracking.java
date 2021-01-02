package sacip.rest;

import java.util.List;

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

	@PostMapping("/dadosUso/{agentPort}")
	public String armazenaDadosUso(@PathVariable String agentPort, @RequestBody JsonNode dados){		
		try 
		{
			ServiceWrapper wrapper = AgentServer.require("SACIP"+agentPort, "storeData"+agentPort);
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

	@PostMapping("/exercicioResolvido/{agentPort}")
	public String armazenaExercicioResolvido(@PathVariable String agentPort, @RequestBody JsonNode dados)
	{
		try 
		{
			ServiceWrapper wrapper = AgentServer.require("SACIP"+agentPort, "storeSolvedExercise"+agentPort);
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

	@PostMapping("/novoConteudoTrilha/{agentPort}")
	public String armazenaNovoConteudoTrilha(@PathVariable String agentPort, @RequestBody JsonNode dados)
	{
		try 
		{
			ServiceWrapper wrapper = AgentServer.require("SACIP"+agentPort, "storeContentOnPath"+agentPort);
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

	@PostMapping("/ErrosCometidos/{agentPort}")
	public String armazenaNovosErrosCometidos(@PathVariable String agentPort, @RequestBody JsonNode dados)
	{
		try 
		{
			ServiceWrapper wrapper = AgentServer.require("SACIP"+agentPort, "storeStudentErrors"+agentPort);
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
	
