package sacip.rest;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.midas.as.AgentServer;
import org.midas.as.manager.execution.ServiceWrapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interface")
public class RestInterface {

	private String test = "HELLO WORLD";

	@GetMapping("/contas")
	public String fazLogin() {
		return "";
	}

	@PostMapping("/contas")
	@ResponseBody
	public String criaNovaConta(@RequestBody Map<String, Object> conta) {

		try {
			//TODO Tentar fazer pegar JSON
			
			System.out.println("REQUEST JSON" + conta.toString());
			ServiceWrapper wrapper = AgentServer.require("SACIP", "createAccount");
			wrapper.addParameter("novaConta", new JSONObject(conta));
			List run = wrapper.run();
			return run.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return "Falhou criar conta: \n"+e;
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
	
