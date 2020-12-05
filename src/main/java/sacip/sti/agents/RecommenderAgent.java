package sacip.sti.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.midas.as.AgentServer;
import org.midas.as.agent.templates.Agent;
import org.midas.as.agent.templates.LifeCycleException;
import org.midas.as.agent.templates.ServiceException;
import org.midas.as.manager.execution.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sacip.sti.dataentities.Content;
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
		//Board.setContextAttribute("eventState", "checkErrors");
	}

	private String getConteudosRecomendados(List<Student> grupo, Student aluno) {

		List<String> preferenciasAluno = aluno.getPreferencias();
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
				caracteristicas.addAll(preferenciasAluno);
			}
				
			//Fazer chamada ao banco
			ServiceWrapper wrapper = require("SACIP", "getContentByTags");
			wrapper.addParameter("tags", caracteristicas);
			List resultado = wrapper.run();
			if(resultado.get(0)==null || resultado.get(0) instanceof String)
			{
				return "não há conteúdos";
			}
			List<Content> conteudos =  (List<Content>) resultado.get(0);

			List<Content> sortedContent = new ArrayList<Content>(){
				@Override
				public boolean add(Content e) {
					super.add(e);
					Collections.sort(this, new Comparator<Content>(){
						@Override
						public int compare(Content o1, Content o2) {
							return o2.pontos-o1.pontos;
						}
					});
					return true;
				}
			};
			
			for (Content content : conteudos) {
				content.pontos += calculateTagPoints(content.getTags(), preferenciasAluno);
				content.pontos += calculateDistancePoints(aluno.getTrilha(), content);
				
				sortedContent.add(content);
			}

			//retornando conteudos
			String exercicio = sortedContent.toString();
	
			return exercicio;
		} 
		catch (Exception e) 
		{
			LOG.error("ERRO NO PEDAGOGICAL AGENT AO SUGERIR EXERCÍCIOS", e);
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
	}

	private int calculateTagPoints(List<String> tagsConteudo, List<String> preferenciasAluno)
	{
		int pontos = 0;

		for (String tagConteudo : tagsConteudo) {
			if(preferenciasAluno.contains(tagConteudo))
			{
				pontos++;
			}	
		}

		return pontos;
	}

	private int calculateDistancePoints(List<String> trilha, Content conteudo)
	{
		int pontos = 0;
		ListIterator li = trilha.listIterator(trilha.size());
		
		while(li.hasPrevious())
		{
			String contTrilha = (String) li.previous();
		}
		return pontos;
	}
}
