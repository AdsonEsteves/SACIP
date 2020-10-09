package sacip.sti.agents;

import java.util.List;
import java.util.Map;

import org.midas.as.agent.board.Message;
import org.midas.as.agent.board.MessageListener;
import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;

public class InterfaceAgent extends Agent implements MessageListener{

	@Override
	public void boardChanged(Message msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {

		if(service.equals("getAluno"))
		{
			out.add("Adson");
		}
		
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		// TODO Auto-generated method stub
		
	}

}
