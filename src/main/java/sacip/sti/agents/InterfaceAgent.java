package sacip.sti.agents;

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

public class InterfaceAgent extends Agent implements MessageListener{

	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

	@Override
	public void boardChanged(Message msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {

		if(service.equals("createAccount"))
		{
			try {				
				ServiceWrapper wrapper = require("SACIP", "createStudent");
				wrapper.addParameter("conta", in.get("novaConta"));
				List response = wrapper.run();
				out.add(response.get(0));

			} catch (Exception e) {
				LOG.error("Falhou criar conta", e);
				out.add("FALHOU INTERFACE AGENT"+e);
			}
		}
		
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		// TODO Auto-generated method stub
		
	}

}
