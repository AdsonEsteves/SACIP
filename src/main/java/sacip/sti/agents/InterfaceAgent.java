package sacip.sti.agents;

import java.util.List;
import java.util.Map;

import org.midas.as.AgentServer;
import org.midas.as.agent.board.Message;
import org.midas.as.agent.board.MessageListener;
import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;
import org.midas.as.manager.execution.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import sacip.sti.dataentities.Student;

@RestController
@RequestMapping("/interface")
public class InterfaceAgent extends Agent implements MessageListener{

	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

	@Override
	public void boardChanged(Message msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {
		
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@PostMapping("/contas")
	@ResponseBody
	public String criaNovaConta(@RequestBody Student conta) {

		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP", "createStudent");
			wrapper.addParameter("novaConta", conta);
			List run = wrapper.run();
			return run.toString();
		} catch (Exception e) {
			LOG.error("Falhou criar conta em REST Interface", e);
			return "Falhou criar conta: \n"+e.getLocalizedMessage();
		}
	}

	@GetMapping("/contas")
	public String fazLogin() {
		return "";
	}

    @PutMapping("/contas")
	public String editarConta(String name) {
		return "test";
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
