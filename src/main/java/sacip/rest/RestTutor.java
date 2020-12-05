package sacip.rest;

import java.util.List;

import org.midas.as.AgentServer;
import org.midas.as.manager.execution.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tutor")
public class RestTutor {
    
	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

	@GetMapping("/requisitaConteudos")
	public String getConteudosRecomendados(){
		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP", "suggestContent");
			List run = wrapper.run();
			return run.toString();
		} catch (Exception e) {
			LOG.error("Falhou criar conta em REST Interface", e);
			return "Falhou criar conta: \n"+e.getLocalizedMessage();
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
	
