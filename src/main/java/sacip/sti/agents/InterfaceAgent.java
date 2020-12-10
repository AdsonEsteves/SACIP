package sacip.sti.agents;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

import sacip.sti.dataentities.Content;
import sacip.sti.dataentities.Student;

@RestController
@RequestMapping("/interface")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
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
			wrapper.addParameter("conta", conta);
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

	@PostMapping("/conteudos")
	@ResponseBody
	public String criaNovoConteudo(@RequestBody JsonNode conteudo) {

		try {
			ServiceWrapper wrapper = AgentServer.require("SACIP", "createContent");
			String contxt= conteudo.get("conteudo").toString();
			Content content = new ObjectMapper().readValue(contxt, Content.class);
			wrapper.addParameter("conteudo", content);
			wrapper.addParameter("conteudoRelacionado", conteudo.get("conteudoRelacionado").asText(null));
			wrapper.addParameter("valorRelacao", conteudo.get("valorRelacao").asInt(0));
			List run = wrapper.run();
			return run.toString();
		} catch (Exception e) {
			LOG.error("Falhou criar conteudo em REST Interface", e);
			return "Falhou criar conteudo: \n"+e.getLocalizedMessage();
		}
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
