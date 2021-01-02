package sacip.rest;

import java.util.List;

import org.midas.as.AgentServer;
import org.midas.as.manager.execution.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // @GetMapping("/trilha/{user}")
	// public String getTrilhaUsuario(String name) {
	// 	return test;
	// }

    // @GetMapping("/trilha")
	// public String getTrilhas(String name) {
	// 	return test;
	// }

	// @GetMapping("/dicas")
	// public String getDicas(String name) {
	// 	return test;
	// }

}
	
