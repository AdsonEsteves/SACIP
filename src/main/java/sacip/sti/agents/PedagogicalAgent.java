package sacip.sti.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

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
import sacip.sti.evaluation.DataHolder;

public class PedagogicalAgent extends Agent implements MessageListener {

	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);
	private Student student;
	List<Content> trilha = new ArrayList<>();

	public PedagogicalAgent() {
		super();
	}

	private void registrarAluno()
	{
		try {
			Map<String, Student> usuariosConectados = (Map<String, Student>) Board.getContextAttribute("conectedUsers");
			this.student = usuariosConectados.get(super.getPort());			
		} catch (Exception e) {
			LOG.error("Problema no registro do usuario", e);
		}
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
		if(student.getTrilha()!=null)
		{
			List<String> modifiedList = new ArrayList<>();
			try {
				ServiceWrapper buscarConteudos = require("SACIP", "findContents");
				buscarConteudos.addParameter("name",this.student.getTrilha().toArray(new String[this.student.getTrilha().size()]));
				List resultado = buscarConteudos.run();
				if (resultado.get(0) instanceof List) {
					List<Content> trilhaConteudos = (List<Content>) resultado.get(0);
					trilha.clear();
					for (String nome : this.student.getTrilha()) {
						for (Content content : trilhaConteudos) {
							if (content.getName().equals(nome)) {
								trilha.add(content);
								modifiedList.add(nome);
								continue;
							}
						}
					}
				}
				student.setTrilha(modifiedList);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("Não conseguiu gerar a trilha", e);
			}
		}
		else{
			student.setTrilha(new ArrayList<String>());
		}
	}

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {
		switch (service.replace(super.getPort(), "")) {
			case "getAluno":
				out.add(getAluno());
				break;

			case "getTrilha":
				out.add(new ObjectMapper().valueToTree(trilha));
				break;

			case "atualizarTrilha":
				out.add(atualizarTrilha((String)in.get("nomeConteudo")));
				break;

			case "suggestContent":
				out.add(suggestContent());
				break;

			case "registerStudent":
				registrarAluno();
				//montarAlunoExemplo();
				registrarConteudosDaTrilhaDoAluno();
			break;

			default:
				throw new ServiceException("Serviço " + service + " não foi implementado no agente pedagógico.");
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		// while(alive)
		// {
		// 	try {
		// 		if(super.getPort()!=null && this.student == null)
		// 		{					
					
		// 			break;
		// 		}
		// 	} catch (Exception e) {
		// 		//TODO: handle exception
		// 	}
		// }
	}

	@Override
	public void boardChanged(Message msg) {
		// TODO Auto-generated method stub

	}

	private Object atualizarTrilha(String nomeConteudo)
	{
		if(nomeConteudo.equals(""))
		{
			return new ServiceException("nome de conteudo invalido");
		}

		getAluno().addNovoPassoTrilha(nomeConteudo);
		registrarConteudosDaTrilhaDoAluno();

		try {
			ServiceWrapper wrapper = require("SACIP", "editStudentListAttr");
			wrapper.addParameter("name", getAluno().getName());
			wrapper.addParameter("attrName", "trilha");
			wrapper.addParameter("newValue", new TextNode(nomeConteudo));
			return wrapper.run().get(0);
		} catch (Exception e) {
			LOG.error("ERRO NO TRACKING AGENT AO ENVIAR DADOS de "+trilha, e);
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
	}

	private List<Student> getStudentGroup(Student aluno) throws BoardException, InterruptedException
	{
		HashMap<String, List<Student>> studentGroups = (HashMap<String, List<Student>>) Board.getContextAttribute("StudentsGroups");

		for (Entry<String, List<Student>> group : studentGroups.entrySet()) {
			for (Student student : group.getValue()) {
				if(student.getName().equals(aluno.getName()))
				{
					System.out.println("TOPICOS: "+group.getKey());
					DataHolder.getInstance().setTopic(group.getKey());
					return group.getValue();
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
			// List<Student> grupo = (List<Student>) servicoGetGroups.run().get(0);
			List<Student> grupo = new ArrayList<>();

			// studentGroup.forEach(s -> {
			// 	Student student = grupo.stream().filter(g -> g.getName().equals(s.getName()))
			// 	.findAny().orElseGet(()->null);
			// 	if(student==null && !s.getName().equals(getAluno().getName()))
			// 	grupo.add(s);				
			// });
			studentGroup.forEach(s -> {
				if(!s.getName().equals(getAluno().getName()))
				grupo.add(s);				
			});
			
			
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
			String recomendacoes = resultado.get(0).toString();

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
