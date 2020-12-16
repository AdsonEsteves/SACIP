package sacip.sti.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

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
					out.add(getConteudosRecomendados((List<Student>)in.get("grupo"), (Student)in.get("estudante"), (List<Content>)in.get("trilha")));
				break;
		
			default:
				throw new ServiceException("Serviço "+service+" não foi implementado no agente recomendador.");
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		//Board.setContextAttribute("eventState", "checkErrors");
	}

	private String getConteudosRecomendados(List<Student> grupo, Student aluno, List<Content> trilha) {

		List<String> preferenciasAluno = aluno.getPreferencias();

		//lista filtrada por pontos
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

			//Verificar os tópicos e níveis que o aluno utilizou
			HashMap<Integer, List<String>> nivelETopico = descobrirNiveisETopicos(trilha);
			String[] niveisFeitos = nivelETopico.keySet().toArray(new String[nivelETopico.size()]);
			//Fazer chamada ao banco buscando os conteúdos desses níveis
			//Fazer chamada ao banco
			ServiceWrapper servicoPegarConteudosEmNiveis = require("SACIP", "getContents");
			servicoPegarConteudosEmNiveis.addParameter("level", niveisFeitos);
			List resultado = servicoPegarConteudosEmNiveis.run();
			if(resultado.get(0)==null || resultado.get(0) instanceof String)
			{
				return "não há conteúdos";
			}
			List<Content> conteudos =  (List<Content>) resultado.get(0);

			//CONTEUDOS DO TÓPICO EM QUE ELE ESTÁ

			//VER OS ULTIMOS CONTEUDOS POR TÓPICO QUE ELE FEZ: MAPA(TOPICOS, CONTEUDOS) => MAPA(TOPICOS/PROXIMATAXONOMIA)
			Map<String, String> taxonomiasPorTopicos = descobrirProximosConteudosPorTaxonomia(trilha);
			List<Content> conteudosPorNovasTaxonomias = new ArrayList<>();
			if(!taxonomiasPorTopicos.isEmpty())
			{
				//BUSCAR NOVOS CONTEUDOS DE NOVA TAXONOMIA PARA CADA TOPICO
				conteudosPorNovasTaxonomias = descobrirConteudosDeTaxonomia(conteudos, taxonomiasPorTopicos);
	
				//FILTRAR POR TAGS
				conteudosPorNovasTaxonomias = filtrarConteudosPorTags(conteudosPorNovasTaxonomias, caracteristicas);
			}

			//Verificar se há algum tópico faltante em um dos níveis feitos do aluno (guardar níveis completados?)
			List<String> topicosFaltantes = descobrirTopicosFaltantes(conteudos, nivelETopico);


			//recomendar os tópicos de níveis mais baixos.
			//pegar os conteúdos desse tópico
			if(topicosFaltantes.isEmpty())
			{
				int proximoNivelAluno = 1;
				for (int i = 0; i < niveisFeitos.length; i++) {
					int nivelFeito = Integer.parseInt(niveisFeitos[i]);
					if(nivelFeito>proximoNivelAluno)
					{
						proximoNivelAluno=nivelFeito;
					}
				}
				proximoNivelAluno++;

				ServiceWrapper servicoPegarConteudosDoNivel = require("SACIP", "getContents");
				servicoPegarConteudosDoNivel.addParameter("level", proximoNivelAluno);
				resultado = servicoPegarConteudosDoNivel.run();
				if(resultado.get(0)==null || resultado.get(0) instanceof String)
				{
					return "não há conteúdos";
				}
				conteudos =  (List<Content>) resultado.get(0);
			}
			else
			{
				conteudos = filtrarConteudosPorTopicos(conteudos, topicosFaltantes);						
			}			
			conteudos = filtrarConteudosPorTags(conteudos, caracteristicas);



			//PRIORIZAR POR PONTOS
			for (Content content : conteudos) {
				// if(!content.getDifficulty().equals(aluno.getNivelEducacional()))
				// {
				// 	content.pontos-=100;
				// }
				content.pontos += calculateTagPoints(content.getTags(), preferenciasAluno);				
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

	private int dificuldadeMedia(List<Content> trilha)
	{
		int media = 0;

		for (Content content : trilha) {
			media+=content.getDifficulty();
		}

		return media/trilha.size();
	}

	private HashMap<Integer, List<String>> descobrirNiveisETopicos(List<Content> trilha)
	{
		HashMap<Integer, List<String>> nivelETopic = new HashMap();

		for (Content content : trilha) {
			int nivel = content.getLevel();
			String topico = content.getTopic();
			if(nivelETopic.containsKey(nivel))
			{
				nivelETopic.get(nivel).add(topico);
			}
			else
			{
				List<String> topicos = new ArrayList<>();
				topicos.add(topico);
				nivelETopic.put(nivel, topicos);
			}
		}
		return nivelETopic;
	}

	private List<String> descobrirTopicosFaltantes(List<Content> conteudosBuscados, HashMap<Integer, List<String>> niveisTopicosDoAluno)
	{
		HashMap<Integer, List<String>> niveisETopicosBuscados = descobrirNiveisETopicos(conteudosBuscados);
		List<String> topicosFaltantes = new ArrayList<>();

		for (Entry<Integer, List<String>> entry : niveisETopicosBuscados.entrySet()) {
			Integer key = entry.getKey();
			List<String> listaTopicos = entry.getValue();
			for (String topico : listaTopicos) {
				if(!niveisTopicosDoAluno.get(key).contains(topico))
				{
					topicosFaltantes.add(topico);
				}
			}
		}

		return topicosFaltantes;
	}

	private List<Content> filtrarConteudosPorTopicos(List<Content> conteudos, List<String> topicos)
	{
		List<Content> conteudosRecomendados = new ArrayList<>();

		for (Content content : conteudosRecomendados) {
			if(!topicos.contains(content.getTopic()))
			{
				continue;
			}
			conteudos.add(content);
		}

		if(conteudosRecomendados.isEmpty())
		{
			return conteudos;
		}
 
		return conteudosRecomendados;
	}

	private List<Content> filtrarConteudosPorTags(List<Content> conteudos, List<String> caracteristicas)
	{
		List<Content> conteudosRecomendados = new ArrayList<>();

		for (Content content : conteudosRecomendados) {

			if(!caracteristicas.stream().anyMatch(element -> content.getTags().contains(element)))
			{
				continue;
			}

			conteudos.add(content);
		}

		if(conteudosRecomendados.isEmpty())
		{
			return conteudos;
		}
 
		return conteudosRecomendados;
	}

	private Map<String, String> descobrirProximosConteudosPorTaxonomia(List<Content> trilha)
	{
		Map<String, String> taxonomiaPorTopicos = new HashMap<>();
		String[] taxBloom = {"Lembrar", "Compreeder", "Aplicar", "Analisar", "Avaliar", "Criar"};

		for (Content content : trilha) {			
			String topico = content.getTopic();
			String tax = content.getTaxonomy();
			if(taxonomiaPorTopicos.containsKey(topico))
			{
				String setTax = taxonomiaPorTopicos.get(topico);
				boolean foundTax = false;
				for (int i = 0; i < taxBloom.length-1; i++) {
					if(taxBloom[i].equals(tax) ||taxBloom[i].equals(setTax))
					{
						if(!foundTax)
						{
							foundTax = true;
						}
						else
						{
							taxonomiaPorTopicos.put(topico, taxBloom[i+1]);
						}
					}					
				}
			}
			else
			{
				if(!tax.equals("Criar"))
				taxonomiaPorTopicos.put(topico, tax);
			}
		}

		return taxonomiaPorTopicos;
	}

	private List<Content> descobrirConteudosDeTaxonomia(List<Content> conteudos, Map<String, String> taxonomiasPorTopicos)
	{
		List<Content> conteudosRecomendados = new ArrayList<>();

		for (Content content : conteudosRecomendados) {
			if(taxonomiasPorTopicos.containsKey(content.getTopic()))
			{
				String tax = taxonomiasPorTopicos.get(content.getTopic());
				if(content.getTaxonomy().equals(tax))
				{
					conteudosRecomendados.add(content);
				}
			}
		}

		if(conteudosRecomendados.isEmpty())
		return conteudos;

		return conteudosRecomendados;
	}
}
