package sacip.sti.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.midas.as.AgentServer;
import org.midas.as.agent.board.Board;
import org.midas.as.agent.board.BoardException;
import org.midas.as.agent.board.Message;
import org.midas.as.agent.board.MessageListener;
import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;
import org.midas.as.manager.execution.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sacip.Launcher;
import sacip.sti.dataentities.Content;
import sacip.sti.dataentities.Student;

public class PedagogicalAgent extends Agent implements MessageListener {

	private String instancia;
	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);
	private Student student;
	List<Content> trilha = new ArrayList<>();

	public PedagogicalAgent() {
		super();
		this.instancia = Launcher.instancia;
		montarAlunoExemplo();
		registrarConteudosDaTrilhaDoAluno();
	}

	private void montarAlunoExemplo() {
		List<String> preferencias = new ArrayList<>();
		preferencias.add("desenhos");
		preferencias.add("dc");
		preferencias.add("animais");
		preferencias.add("marvel");
		List<String> trilha = new ArrayList<String>() {
			{
				add("2dcznTNvej");
				add("Jvp1ma0QEN");
				add("s19ra6yQrd");
				add("tsxlrStu9a");
				add("UlGW3aVSZI");
			}
		};
		this.student = new Student("l4yhU9z2bx", "123456", "link", "masculino", 24, "Graduação", preferencias, trilha);
	}

	private void registrarConteudosDaTrilhaDoAluno() {
		try {
			ServiceWrapper buscarConteudos = require("SACIP", "findContents");
			buscarConteudos.addParameter("name",this.student.getTrilha().toArray(new String[this.student.getTrilha().size()]));
			List resultado = buscarConteudos.run();
			if (resultado.get(0) instanceof List) {
				List<Content> trilhaConteudos = (List<Content>) resultado.get(0);
				for (String nome : this.student.getTrilha()) {
					for (Content content : trilhaConteudos) {
						if (content.getName().equals(nome)) {
							trilha.add(content);
							continue;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Não conseguiu gerar a trilha", e);
		}
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
				throw new ServiceException("Serviço " + service + " não foi implementado no agente pedagógico.");
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {

	}

	@Override
	public void boardChanged(Message msg) {
		// TODO Auto-generated method stub

	}

	private List<Student> getStudentGroup(Student aluno) throws BoardException
	{
		HashMap<Integer, List<Student>> studentGroups = (HashMap<Integer, List<Student>>) Board.getContextAttribute("StudentsGroups");

		for (List<Student> group : studentGroups.values()) {
			for (Student student : group) {
				if(student.getName().equals(aluno.getName()))
				{
					return group;
				}
			}
		}
		
		return new ArrayList<>();
	}

	private String suggestContent()
	{
		try 
		{
			List<Student> studentGroup = getStudentGroup(getAluno());
			

			//Pegar grupo de alunos
			ServiceWrapper servicoGetGroups = require("SACIP", "getStudentGroups");
			servicoGetGroups.addParameter("estudante", getAluno());
			List<Student> grupo = (List<Student>) servicoGetGroups.run().get(0);

			studentGroup.forEach(s -> {
				Student student = grupo.stream().filter(g -> g.getName().equals(s.getName()))
				.findAny().orElseGet(()->null);
				if(student==null)
				grupo.add(s);				
			});

			// for (Student student : studentGroup) {
			// 	boolean has = false;
			// 	for (Student student2 : grupo) {
			// 		if(student.getName().equals(student2.getName()))
			// 		{
			// 			has = true;
			// 			break;
			// 		}
			// 	}
			// 	if(!has)
			// 	grupo.add(student);
			// }

			//Pedir recomendação para o recomendador
			ServiceWrapper servicoGetContent = require("SACIP", "getRecommendedContent");
			servicoGetContent.addParameter("estudante", getAluno());
			servicoGetContent.addParameter("grupo", grupo);
			servicoGetContent.addParameter("trilha", this.trilha);
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

	public List<Content> getTrilha() {
		return this.trilha;
	}

	public void setTrilha(List<Content> trilha) {
		this.trilha = trilha;
	}

}
