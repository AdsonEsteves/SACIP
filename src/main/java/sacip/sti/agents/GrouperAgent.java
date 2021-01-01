package sacip.sti.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.chen0040.data.utils.TupleTwo;
import com.github.chen0040.lda.Doc;
import com.github.chen0040.lda.Lda;
import com.github.chen0040.lda.LdaResult;
import org.midas.as.AgentServer;
import org.midas.as.agent.board.Board;
import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;
import org.midas.as.manager.execution.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sacip.sti.dataentities.Student;

public class GrouperAgent extends Agent {

	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {

		try {
			switch (service) {
				case "getStudentGroups":
					out.add(findStudentSimilars((Student) in.get("estudante")));
					break;

				default:
					break;
			}
		} catch (Exception e) {

		}
		if (service.equals("getStudentGroups")) {
			try {

			} catch (Exception e) {
				LOG.error("ERRO NO AGENT AGRUPADOR", e);
			}
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		
		while(this.alive)
		{
			try 
			{				
				HashMap<Integer, List<Student>> studentGroups = findStudentGroup();
				Board.setContextAttribute("StudentsGroups", studentGroups);
			} 
			catch (Exception e) {
				LOG.error("ERRO NO CICLO DE VIDA DO GROUPER", e);
			}
			Thread.sleep(3000000);
		}

	}

	public static void main(String[] args) {
		GrouperAgent agent = new GrouperAgent();
		agent.findStudentGroup();
	}

	public HashMap<Integer, List<Student>> findStudentGroup()
	{
		List<Student> estudantes = getUsers();
		HashMap<Integer, List<Student>> studentGroups = new HashMap<>();
		double score = 0.0;
		int mean = 10;
		// List<String> docs = Arrays.asList("carros animes youtube História", "comédia animes História Livros", "monstros cultura comédia Tecnologia", "Livros", "mitologia animes", "Livros matemática");
		List<String> docs = new ArrayList<>();

		for (Student estudante : estudantes) 
		{
			docs.add(estudante.getPreferencias().toString().replaceAll("[,\\[\\]]", ""));
		}
		while(score<0.5)
		{
			Lda method = new Lda();
			method.setTopicCount((estudantes.size()/mean)+1);
			method.setMaxVocabularySize(20000);
			method.setProgressListener(null);
	
			LdaResult result = method.fit(docs);
			
			for(Doc doc : result.documents())
			{
				List<TupleTwo<Integer, Double>> topTopics = doc.topTopics(1);
				int key = topTopics.get(0)._1();
				int studentIndex = doc.getDocIndex();
				
				if(studentGroups.containsKey(topTopics.get(0)._1()))
				{
					studentGroups.get(key).add(estudantes.get(studentIndex));
				}
				else
				{
					List<Student> grupo = new ArrayList<>();
					grupo.add(estudantes.get(studentIndex));
					studentGroups.put(key, grupo);
				}
				score+=topTopics.get(0)._2();
				System.out.println("Doc: {"+doc.getDocIndex()+"}"+" TOP TOPIC: {"+topTopics.get(0)._1()+"}"+" SCORE: {"+topTopics.get(0)._2()+"}");
			}
			score=score/docs.size();
			mean++;
		}
		//System.out.println("Finalizou: "+studentGroups);
		return studentGroups;
	}

	private List<Student> findStudentSimilars(Student alunoRequisitado)
	{
		List<Student> estudantes = getUsers();

		if(estudantes.size()<20)
		{
			return new ArrayList<>();
		}

		List<Student> sortedStudents = new ArrayList<Student>(){
			@Override
			public boolean add(Student e) {
				super.add(e);
				Collections.sort(this, new Comparator<Student>(){
					@Override
					public int compare(Student o1, Student o2) {
						return o2.pontos-o1.pontos;
					}
				});
				return true;
			}
		};

		for (Student student : estudantes) 
		{
			List<String> preferencias = student.getPreferencias();
			
			if(!alunoRequisitado.getName().equals(student.getName()))
			{
				for (String preferencia : preferencias) 
				{
					if(alunoRequisitado.getPreferencias().contains(preferencia))
					{
						student.pontos++;
					}
					// else{
					// 	student.pontos--;
					// }	
				}
	
				if(student.getNivelEducacional().equals(alunoRequisitado.getNivelEducacional()))
				{
					student.pontos+=5;
				}
				sortedStudents.add(student);
			}
		}

		List<Student> grupoDe10 = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			grupoDe10.add(sortedStudents.get(i));
		}

		return grupoDe10;
	}

	private List<Student> getUsers()
	{
		try 
		{
			ServiceWrapper pegarEstudantes  = require("SACIP", "findStudents");
			List result = pegarEstudantes.run();
			if(result.get(0) instanceof String)
			{
				return null;
			}
			return (List<Student>) result.get(0);
		} 
		catch (Exception e) 
		{
			LOG.error("ERRO AO REQUISITAR USUARIOS DO SISTEMA", e);
			return null;
		}
	}


}
