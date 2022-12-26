package sacip.sti.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import sacip.kmeans.Centroid;
import sacip.kmeans.EuclideanDistance;
import sacip.kmeans.KMeans;
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
				case "resetStudentGroups":
					reset_groups();
					break;
				default:
					break;
			}
		} catch (Exception e) {
			LOG.error("ERRO NO GROUPER AGENT AO AGRUPAR", e);
			e.printStackTrace();
			out.add(e.getLocalizedMessage());
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {

		while (this.alive) {
			try {
				// HashMap<String, List<Student>> studentGroups = findStudentGroup();
				HashMap<String, List<Student>> studentGroups = findStudentGroup2();
				Board.setContextAttribute("StudentsGroups", studentGroups);
			} catch (Exception e) {
				LOG.error("ERRO NO CICLO DE VIDA DO GROUPER", e);
			}
			Thread.sleep(3000000);
		}

	}

	public void reset_groups() {
		Board.setContextAttribute("StudentsGroups", "");
		// HashMap<String, List<Student>> studentGroups = findStudentGroup();
		HashMap<String, List<Student>> studentGroups = findStudentGroup2();
		Board.setContextAttribute("StudentsGroups", studentGroups);
	}

	public static void main(String[] args) {
		GrouperAgent agent = new GrouperAgent();
		agent.findStudentGroup();
	}

	public HashMap<String, List<Student>> findStudentGroup2() {
		HashMap<String, List<Student>> studentGroups = new HashMap<>();
		List<Student> users = getUsers();
		if (users == null) {
			return studentGroups;
		}

		List<Student> filterUsers = users.stream().filter(s -> s.getTrilha().size() > 2).collect(
				Collectors.toList());

		if (filterUsers.size() <= 10) {
			studentGroups.put(users.hashCode() + "", users);
			return studentGroups;
		}
		Map<Centroid, List<Student>> cluster = KMeans
				.fit(filterUsers, (int) Math.ceil(filterUsers.size() / 10.0), new EuclideanDistance(), 500);

		cluster.forEach((t, u) -> {
			studentGroups.put(t.hashCode() + "", u);
		});

		return studentGroups;
	}

	public HashMap<String, List<Student>> findStudentGroup() {
		List<Student> estudantes = getUsers();
		if (estudantes == null) {
			return new HashMap<>();
		}
		HashMap<String, List<Student>> studentGroups = new HashMap<>();
		double score = 0.0;
		int mean = 10;
		// List<String> docs = Arrays.asList("carros animes youtube História", "comédia
		// animes História Livros", "monstros cultura comédia Tecnologia", "Livros",
		// "mitologia animes", "Livros matemática");
		List<String> docs = new ArrayList<>();

		for (Student estudante : estudantes) {
			docs.add(estudante.getPreferencias().toString().replaceAll("[,\\[\\]]", "") + " "
					+ estudante.getNivelEducacional() + " " + grupoIdade(estudante.getIdade()));
			// System.out.println(estudante.getPreferencias().toString().replaceAll("[,\\[\\]]",
			// "")+" "+estudante.getNivelEducacional());
			// docs.add(estudante.getPreferencias().toString().replaceAll("[,\\[\\]]", ""));
		}
		// while(score<0.5)
		{
			studentGroups.clear();
			Lda method = new Lda();
			method.setTopicCount((estudantes.size() / mean) + 1);
			method.setMaxVocabularySize(20000);
			method.setRemoveNumber(false);

			LdaResult result = method.fit(docs);

			for (Doc doc : result.documents()) {
				List<TupleTwo<Integer, Double>> topTopics = doc.topTopics(1);
				String key = result.topicSummary(topTopics.get(0)._1());
				int studentIndex = doc.getDocIndex();

				if (studentGroups.containsKey(key)) {
					studentGroups.get(key).add(estudantes.get(studentIndex));
				} else {
					List<Student> grupo = new ArrayList<>();
					grupo.add(estudantes.get(studentIndex));
					studentGroups.put(key, grupo);
				}
				score += topTopics.get(0)._2();
				// System.out.println("Doc: {"+doc.getDocIndex()+"}"+" TOP TOPIC:
				// {"+result.topicSummary(topTopics.get(0)._1())+"}"+" SCORE:
				// {"+topTopics.get(0)._2()+"}");
			}
			score = score / docs.size();
			mean++;
		}
		System.out.println("Finalizou: " + score + " Media:" + mean + " Grupos: " + studentGroups.size());

		return studentGroups;
	}

	private String grupoIdade(int idade) {
		if (idade < 13) {
			return "menor13";
		} else if (idade < 18) {
			return "13menor18";
		} else if (idade < 24) {
			return "18menor24";
		} else if (idade < 30) {
			return "24menor30";
		}
		return "maior30";
	}

	private List<Student> findStudentSimilars(Student alunoRequisitado) {
		List<Student> estudantes = getUsers();

		if (estudantes.size() < 20) {
			return new ArrayList<>();
		}

		List<Student> sortedStudents = new ArrayList<Student>() {
			@Override
			public boolean add(Student e) {
				super.add(e);
				Collections.sort(this, new Comparator<Student>() {
					@Override
					public int compare(Student o1, Student o2) {
						return o2.pontos - o1.pontos;
					}
				});
				return true;
			}
		};

		for (Student student : estudantes) {
			List<String> preferencias = student.getPreferencias();

			if (!alunoRequisitado.getName().equals(student.getName())) {
				for (String preferencia : preferencias) {
					if (alunoRequisitado.getPreferencias().contains(preferencia)) {
						student.pontos++;
					}
					// else{
					// student.pontos--;
					// }
				}

				if (student.getNivelEducacional() == alunoRequisitado.getNivelEducacional()) {
					student.pontos += 5;
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

	private List<Student> getUsers() {
		try {
			ServiceWrapper pegarEstudantes = require("SACIP", "findStudents");
			List result = pegarEstudantes.run();
			if (result.get(0) instanceof String) {
				return null;
			}
			return (List<Student>) result.get(0);
		} catch (Exception e) {
			LOG.error("ERRO AO REQUISITAR USUARIOS DO SISTEMA", e);
			return null;
		}
	}

}
