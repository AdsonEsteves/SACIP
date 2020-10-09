package sacip.sti.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.midas.as.agent.board.Board;
import org.midas.as.agent.board.Message;
import org.midas.as.agent.board.MessageListener;
import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;
import org.midas.as.manager.execution.ServiceWrapper;

public class TrackingAgent extends Agent implements MessageListener{

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		// TODO Auto-generated method stub
		Board.addMessageListener("SACIP", this);
		
		// try
		// {
		// 	System.out.println("TESTANDO TRACK");
		// 	List<String> preferencias = Arrays.asList("futebol", "memes", "animais");
		// 	ServiceWrapper wrapper = require("SACIP", "createStudent");
		// 	wrapper.addParameter("name", "Joe");
		// 	wrapper.addParameter("idade", 35);
		// 	wrapper.addParameter("password", "1235456");
		// 	wrapper.addParameter("avatar", "bigimage");
		// 	wrapper.addParameter("genero", "homem");
		// 	wrapper.addParameter("nivelEdu", "ensino médio");
		// 	wrapper.addParameter("preferencias", preferencias);
		// 	System.out.println("VAI MANDAR");
		// 	List respostas = wrapper.run();
		// 	System.out.println("MANDOU");;
		// 	System.out.println(respostas.get(0));
		// }
		// catch(Exception e)
		// {
		// 	System.out.println(e.getMessage());
		// }
		// while(alive)
		// {
		// 	Thread.sleep(2000);
		// }
		
	}

	@Override
	public void boardChanged(Message msg) {
		// TODO Auto-generated method stub
		
	}
	
	public void portugolListener()
	{
		
	}

}
