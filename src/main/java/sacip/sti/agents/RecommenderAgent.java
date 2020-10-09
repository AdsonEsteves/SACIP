package sacip.sti.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.midas.as.agent.board.Board;
import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;
import org.midas.as.manager.execution.ServiceWrapper;
import org.midas.as.manager.execution.ServiceWrapperException;

public class RecommenderAgent extends Agent {

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {
		// TODO Auto-generated method stub
		if (service.equals("getRecommendedExercises")) {
			try {
				String aluno = (String) in.get("aluno");
				List grupo = buscarGrupos(aluno);
				String exercicio = encontrarExercicio(grupo, aluno);
				out.add(exercicio);
			} catch (Exception e) {
				throw new ServiceException("Não foi possível pegar o exercício recomendado", e);
			}

		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		// TODO Auto-generated method stub
		Board.setContextAttribute("eventState", "checkErrors");
	}

	private String encontrarExercicio(List<String> grupo, String aluno) {

		List<String> caracteristicas = new ArrayList<>();

		if(!grupo.isEmpty())
		{
			System.out.println("Verificando semelhanças entre alunos");

			caracteristicas.add("exemplo");
		}
		else
		{
			caracteristicas.add(aluno);
		}
		
		System.out.println("buscando caracteristicas em conteudos");

		//Fazer chamada ao banco
		
		System.out.println("retornando exercício");

		String exercicio = "";

		return exercicio;
	}

	private List<String> buscarGrupos(String aluno)throws InterruptedException, ExecutionException, ServiceWrapperException
	{
		
		System.out.println("Pedindo grupo para o classificador");
		ServiceWrapper serviceWrapper = require("PublicAgents", "classifyStudents");
		serviceWrapper.addParameter("aluno", aluno);
		List resposta = serviceWrapper.run();

		return resposta;
	}

}
