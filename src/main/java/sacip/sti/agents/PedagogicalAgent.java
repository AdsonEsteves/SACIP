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
	}

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {
		// TODO Auto-generated method stub
		
		if(service.equals("getAluno"))
		{
			out.add(getAluno());			
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

	private Student getAluno()
	{
		return this.student;
	}

}
