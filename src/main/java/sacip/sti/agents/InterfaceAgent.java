package sacip.sti.agents;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.midas.as.agent.board.Message;
import org.midas.as.agent.board.MessageListener;
import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;
import org.midas.as.manager.execution.ServiceWrapper;

public class InterfaceAgent extends Agent implements MessageListener{

	@Override
	public void boardChanged(Message msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {

		if(service.equals("createAccount"))
		{
			try {
				System.out.println("CRIANDO CONTA");
				JSONObject conta = (JSONObject) in.get("novaConta");
				System.out.println("GERANDO JSON" + conta.toString());
				ServiceWrapper wrapper = require("SACIP", "createStudent");
				// wrapper.setParameters(in);
				wrapper.addParameter("name", conta.get("name"));			
				wrapper.addParameter("password", conta.get("password"));			
				wrapper.addParameter("avatar", conta.get("avatar"));			
				wrapper.addParameter("genero", conta.get("genero"));			
				wrapper.addParameter("idade", conta.get("idade"));			
				wrapper.addParameter("nivelEdu", conta.get("nivelEdu"));			
				wrapper.addParameter("preferencias", conta.get("preferencias"));
				System.out.println("GERANDO PARAMETROS");
				out = wrapper.run();

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("FALHOU INTERFACE AGENT"+e);
				out.add("FALHOU INTERFACE AGENT"+e);
			}
		}
		
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		// TODO Auto-generated method stub
		
	}

}
