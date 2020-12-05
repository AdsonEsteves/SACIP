package sacip.sti.agents;

import java.util.ArrayList;
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

import sacip.Launcher;
import sacip.sti.dataentities.Student;

public class PedagogicalAgent extends Agent implements MessageListener{

	List<String> alunosOnline = new ArrayList<>();
	private String instancia;
	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);
	private Student student;

	public PedagogicalAgent() {
		super();
		this.instancia = Launcher.instancia;
		List<String> preferencias = new ArrayList<>();
		preferencias.add("Animação");
		preferencias.add("Filmes");
		this.student = new Student("Adson", "123456", "link", "masculino", 24, "bacharelado", preferencias);
	}

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {
		switch (service) {
			case "getAluno":
				out.add(getAluno());
				break;

			case "suggestContent":
				out.add(suggestContent());
				break;
		
			default:
				throw new ServiceException("Serviço "+service+" não foi implementado no agente pedagógico.");
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {

	}

	@Override
	public void boardChanged(Message msg) {
		// TODO Auto-generated method stub
		
	}

	private String suggestContent()
	{
		try 
		{
			//Pegar grupo de alunos
			ServiceWrapper servicoGetGroups = require("SACIP", "getStudentGroups");
			servicoGetGroups.addParameter("estudante", getAluno());
			List grupo = servicoGetGroups.run();

			//Pedir recomendação para o recomendador
			ServiceWrapper servicoGetContent = require("SACIP", "getRecommendedContent");
			servicoGetContent.addParameter("estudante", getAluno());
			servicoGetContent.addParameter("grupo", grupo);
			List resultado = servicoGetContent.run();
			if(resultado.isEmpty())
			{
				return "vazio";
			}
			String recomendacoes = (String) resultado.get(0);

			return recomendacoes;
						
		} 
		catch (Exception e) 
		{
			LOG.error("ERRO NO PEDAGOGICAL AGENT AO SUGERIR EXERCÍCIOS", e);
			e.printStackTrace();
			return e.getLocalizedMessage();
		}

	}

	private Student getAluno()
	{
		return this.student;
	}

}
