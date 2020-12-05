package sacip.sti.agents;

import java.util.List;
import java.util.Map;

import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;

import sacip.sti.dataentities.Student;

public class GrouperAgent extends Agent{

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {
		// TODO Auto-generated method stub
		if(service.equals("getStudentGroups"))
		{
			System.out.println("Buscando alunos no banco");
			System.out.println("Classificando alunos");
			
			Student aluno = (Student) in.get("estudante");
			
			System.out.println("Selecionando cluster de aluno "+aluno.getName()+" requisitado");
			System.out.println("retornando grupo");
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		// TODO Auto-generated method stub
		
	}

}
