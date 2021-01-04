package sacip.sti.agents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.midas.as.AgentServer;
import org.midas.as.agent.board.Board;
import org.midas.as.agent.board.Message;
import org.midas.as.agent.board.MessageListener;
import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;
import org.midas.as.catalog.Catalog;
import org.midas.as.manager.execution.ServiceWrapper;
import org.midas.as.manager.manager.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	public int localport = 7102;
	public final String serverport = "7100";
	public final String serverAddress = "127.0.0.1";
	private static Map<String, Student> usuariosConectados;

	public InterfaceAgent() {
		super();			
		usuariosConectados = new HashMap<>();
	}

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

	@PostMapping("/logout/{agentPort}")
	@ResponseBody
	public String deslogar(@PathVariable String agentPort) {

		try {
			Manager.getInstance().disconnect(agentPort, true);
			return "SUCESSO";
		} catch (Exception e) {
			LOG.error("Falhou criar conta em REST Interface", e);
			return "Falhou criar conta: \n"+e.getLocalizedMessage();
		}
	}

	@PostMapping("/login")
	@ResponseBody
	public String fazLogin(@RequestBody JsonNode credenciais) {
		try {
			String usuario = credenciais.get("usuario").asText();
			String senha = credenciais.get("senha").asText();
	
			ServiceWrapper wrapper = AgentServer.require("SACIP", "findStudents");
			wrapper.addParameter("name", usuario);
			wrapper.addParameter("password", senha);
			List run = wrapper.run();

			if(run.get(0)==null)
			{
				return "Login incorreto";
			}

			while(usuariosConectados.containsKey(localport+""))
			{
				localport++;
			}
			int setLocal = localport;
			List<Student> estudantes = (List<Student>) run.get(0);
			usuariosConectados.put(setLocal+"", estudantes.get(0));

			Board.setContextAttribute("conectedUsers", usuariosConectados);

			iniciandoAgentesUsuario(setLocal+"");
			do {
				try
				{
					Catalog.getEntityByName(setLocal+"", "SACIP"+setLocal, "PedagogicalAgent"+setLocal);				
					break;
				}
				catch (Exception e) {
					LOG.error("Entidade não encontrada", e);
					Thread.sleep(1000);
				}				
			} while (true);

			require("SACIP"+setLocal, "registerStudent"+setLocal).run();

			return setLocal+"";
			
		} catch (Exception e) {
			LOG.error("ERRO NO LOGIN", e);
			return e.getMessage();
		}

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

	public void iniciandoAgentesUsuario(String instancia) throws IOException
	{
		//Inicializando Agentes do Usuário
		String UserAgentsStructureXML = readFile("UserAgentsStructure.xml");
		String UserAgentsServicesXML = readFile("UserAgentsServices.xml");	
		UserAgentsStructureXML = UserAgentsStructureXML.replace("$localport", instancia+"").replace("$serverport", serverport).replace("$serverAddress", serverAddress).replace("</name>", instancia+"</name>");
		UserAgentsServicesXML = UserAgentsServicesXML.replace("</name>", instancia+"</name>").replace("</entity>", instancia+"</entity>").replace("</organization>", instancia+"</organization>");
		AgentServer.initialize(true, true, UserAgentsStructureXML, UserAgentsServicesXML);
	}

	public String readFile(String file) throws IOException
	{
		Path fileName = Path.of(file);         
        String actual = Files.readString(fileName);
		return actual;
	}

}
