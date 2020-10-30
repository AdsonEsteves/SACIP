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

public class PedagogicalAgent extends Agent implements MessageListener{

	List<String> alunosOnline = new ArrayList<>();
	private int instancia;
	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

	public PedagogicalAgent() {
		super();
		this.instancia = Launcher.instancia;
	}

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {
		// TODO Auto-generated method stub
		
		if(service.equals("suggestExercise"))
		{
			try
			{
				System.out.println("verificando informações do aluno");				
				ServiceWrapper serviceWrapper = require("LocalAgents", "getAluno");
				List alunoInfo = serviceWrapper.run();
				
				System.out.println("requisitando exercício");				
				ServiceWrapper serviceWrapper2 = require("PublicAgents", "getRecommendedExercises");
				serviceWrapper2.addParameter("aluno", alunoInfo.get(0));
				List exercicio = serviceWrapper2.run();
				
				System.out.println("apresentando exercício");
			}
			catch(Exception e)
			{
				throw new ServiceException("Não foi possível pegar o exercício recomendado - PAgent",e);
			}
			
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {

		// Board.addMessageListener("PortugolSTI", this);
		// for (String aluno : alunosOnline) {
			try 
			{
		// 		while(Board.getContextAttribute(aluno+"eventState").equals("Programando"))
		// 		{
					// ServiceWrapper serviceWrapper = require("SACIP", "storeStudentErrors"+this.instancia);
					
					// List data = serviceWrapper.run();
		// 			List dicas = checkDicas(data);
		// 			if(dicas.size()>0)
		// 			{
		// 				sendDicas(dicas);
		// 			}					
		// 			Thread.sleep(2000);
		// 		}
				
			}
			catch(Exception e)
			{
				LOG.error("Não foi possível analisar o aluno", e);
			}
		// }		

	}

	@Override
	public void boardChanged(Message msg) {
		// TODO Auto-generated method stub
		
	}

	public List<String> checkDicas(Object resposta)
	{
		return new ArrayList<>();
	}

	public void sendDicas(Object dicas)
	{
		
	}

}
