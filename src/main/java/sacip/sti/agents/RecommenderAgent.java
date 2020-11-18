package sacip.sti.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.midas.as.AgentServer;
import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sacip.sti.dataentities.Student;

public class RecommenderAgent extends Agent {

	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {
		switch (service) 
		{
			case "getRecommendedContent":
					out.add(getConteudosRecomendados((List<Student>)in.get("grupo"), (Student)in.get("estudante")));
				break;
		
			default:
				throw new ServiceException("Serviço "+service+" não foi implementado no agente recomendador.");
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		// TODO Auto-generated method stub
		//Board.setContextAttribute("eventState", "checkErrors");
	}

	private String getConteudosRecomendados(List<Student> grupo, Student aluno) {
		try 
		{
			List<String> caracteristicas = new ArrayList<>();
	
			if(!grupo.isEmpty())
			{
				System.out.println("Verificando semelhanças entre alunos");
	
				caracteristicas.add("exemplo");
			}
			else
			{
				caracteristicas.add(aluno.getPreferenciasAsString());
			}
			
			System.out.println("buscando caracteristicas em conteudos");
	
			//Fazer chamada ao banco
			
			System.out.println("retornando conteudos");
	
			String exercicio = "";
	
			return exercicio;			
		} 
		catch (Exception e) 
		{
			LOG.error("ERRO NO PEDAGOGICAL AGENT AO SUGERIR EXERCÍCIOS", e);
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
	}

}
