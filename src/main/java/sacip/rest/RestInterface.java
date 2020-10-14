package sacip.rest;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.midas.as.AgentServer;
import org.midas.as.manager.execution.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import sacip.sti.dataentities.Student;

@RestController
@RequestMapping("/interface")
public class RestInterface {

	private String test = "HELLO WORLD";
	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

	public RestInterface() {
		super();
	}

	@GetMapping("/contas")
	public String fazLogin() {
		return "";
	}

	@PostMapping("/contas")
	@ResponseBody
	public String criaNovaConta(@RequestBody Student conta) {

		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP", "createAccount");
			wrapper.addParameter("novaConta", conta);
			List run = wrapper.run();
			return run.toString();
		} catch (Exception e) {
			LOG.error("Falhou criar conta em REST Interface", e);
			return "Falhou criar conta: \n"+e.getLocalizedMessage();
		}
	}

    @PutMapping("/contas")
	public String editarConta(String name) {
		return test;
	}
	
	@GetMapping("/pergunta")
	public String buscaPergunta()
	{
		return "";
	}

	@PostMapping("/pergunta")
	public String registraPergunta()
	{
		return "";
	}
}
	
