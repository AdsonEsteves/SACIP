package sacip.sti.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	//String[] taxBloom = {"Lembrar", "Compreeder", "Aplicar", "Analisar", "Avaliar", "Criar"};
	private static final int LEMBRAR = 0;
	private static final int COMPREENDER = 1;
	private static final int APLICAR = 2;
	private static final int ANALISAR = 3;
	private static final int AVALIAR = 4;
	private static final int CRIAR = 5;
	
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
			//Verificar os tópicos e níveis que o aluno utilizou
			HashMap<Integer, List<String>> nivelETopico = descobrirNiveisETopicos(trilha);
			Integer[] niveisFeitos = nivelETopico.keySet().toArray(new Integer[nivelETopico.size()]);
			//Fazer chamada ao banco buscando os conteúdos desses níveis
			//Fazer chamada ao banco
			ServiceWrapper servicoPegarConteudosEmNiveis = require("SACIP", "findContents");
			servicoPegarConteudosEmNiveis.addParameter("level", niveisFeitos);
			List resultado = servicoPegarConteudosEmNiveis.run();
			if(resultado.get(0)==null || resultado.get(0) instanceof String)
			{
				return "não há conteúdos";
			}
			List<Content> conteudos =  (List<Content>) resultado.get(0);
			List<Content> conteudosDoGrupoNaoFeitos = new ArrayList<>();
			if(!grupo.isEmpty())
			{
				//VERIFICAR TRILHAS E PEGAR CONTEUDOS UTILIZADOS NOS SEMELHANTES
				List<Content> conteudosDasTrilhas = filtrarConteudosDasTrilhasDosAlunosDoGrupo(grupo, conteudos);

				for (Content content : conteudosDasTrilhas) {
					if(!aluno.getTrilha().contains(content.getName()))
					{
						conteudos.remove(content);
						conteudosDoGrupoNaoFeitos.add(content);
					}
				}				
			}


			//CONTEUDOS DO TÓPICO EM QUE ELE ESTÁ

			//VER OS ULTIMOS CONTEUDOS POR TÓPICO QUE ELE FEZ: MAPA(TOPICOS, CONTEUDOS) => MAPA(TOPICOS/PROXIMATAXONOMIA)
			Map<String, Integer> taxonomiasPorTopicos = descobrirProximosConteudosPorTaxonomia(trilha);
			List<Content> conteudosPorNovasTaxonomias = new ArrayList<>();
			if(!taxonomiasPorTopicos.isEmpty())
			{
				//BUSCAR NOVOS CONTEUDOS DE NOVA TAXONOMIA PARA CADA TOPICO
				conteudosPorNovasTaxonomias = descobrirConteudosDeTaxonomia(conteudos, taxonomiasPorTopicos);
	
				//FILTRAR POR TAGS
				conteudosPorNovasTaxonomias = filtrarConteudosPorTags(conteudosPorNovasTaxonomias, preferenciasAluno);
			}



			//Verificar se há algum tópico faltante em um dos níveis feitos do aluno (guardar níveis completados?)
			List<String> topicosFaltantes = descobrirTopicosFaltantes(conteudos, nivelETopico);

			//recomendar os tópicos de níveis mais baixos.
			//pegar os conteúdos desse tópico
			if(topicosFaltantes.isEmpty())
			{
				int proximoNivelAluno = 1;
				for (int i = 0; i < niveisFeitos.length; i++) {
					int nivelFeito = niveisFeitos[i];
					if(nivelFeito>proximoNivelAluno)
					{
						proximoNivelAluno=nivelFeito;
					}
				}
				proximoNivelAluno++;

				ServiceWrapper servicoPegarConteudosDoNivel = require("SACIP", "findContents");
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
			conteudos = filtrarConteudosPorTags(conteudos, preferenciasAluno);

			Set<Content> conteudosFiltrados = new HashSet<>();
			conteudosFiltrados.addAll(conteudosPorNovasTaxonomias);
			conteudosFiltrados.addAll(conteudos);
			conteudosFiltrados.addAll(conteudosDoGrupoNaoFeitos);

			//PRIORIZAR POR PONTOS
			for (Content content : conteudosFiltrados) {
				content.pontos += calculateTagPoints(content.getTags(), preferenciasAluno);
				if(conteudosPorNovasTaxonomias.contains(content))
				{
					content.pontos++;
				}				
				sortedContent.add(content);
			}

			List<Content> top10Conteudos = new ArrayList<>();

			for (int i = 0; i < 10; i++) {
				top10Conteudos.add(sortedContent.get(i));
			}

			//retornando conteudos
			String exercicio = top10Conteudos.toString();
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

	private HashMap<Integer, List<String>> descobrirNiveisETopicos(List<Content> trilha)
	{
		HashMap<Integer, List<String>> nivelETopic = new HashMap();

		for (Content content : trilha) {
			Integer nivel = content.getLevel();
			String topico = content.getTopic();
			if(nivelETopic.containsKey(nivel))
			{
				if(!nivelETopic.get(nivel).contains(topico))
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

		for (Content content : conteudos) {
			if(topicos.contains(content.getTopic()))
			{
				conteudosRecomendados.add(content);
			}			
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

		for (Content content : conteudos) {

			if(!caracteristicas.stream().anyMatch(element -> content.getTags().contains(element.toLowerCase())))
			{
				continue;
			}

			conteudosRecomendados.add(content);
		}

		if(conteudosRecomendados.isEmpty())
		{
			return conteudos;
		}
 
		return conteudosRecomendados;
	}

	private Map<String, Integer> descobrirProximosConteudosPorTaxonomia(List<Content> trilha)
	{
		Map<String, Integer> taxonomiaPorTopicos = new HashMap<>();

		for (Content content : trilha) {			
			String topico = content.getTopic();
			int tax = content.getTaxonomy();
			if(taxonomiaPorTopicos.containsKey(topico))
			{
				int setTax = taxonomiaPorTopicos.get(topico);
				boolean foundTax = false;
				for (int i = 0; i < CRIAR; i++) {
					if(i==tax ||i==setTax)
					{
						if(!foundTax)
						{
							foundTax = true;
						}
						else
						{
							taxonomiaPorTopicos.put(topico, i+1);
						}
					}					
				}
			}
			else
			{
				if(tax!=CRIAR)
				taxonomiaPorTopicos.put(topico, tax+1);
			}
		}

		return taxonomiaPorTopicos;
	}

	private List<Content> descobrirConteudosDeTaxonomia(List<Content> conteudos, Map<String, Integer> taxonomiasPorTopicos)
	{
		List<Content> conteudosRecomendados = new ArrayList<>();

		for (Content content : conteudos) {
			if(taxonomiasPorTopicos.containsKey(content.getTopic()))
			{
				int tax = taxonomiasPorTopicos.get(content.getTopic());
				if(content.getTaxonomy()==tax)
				{
					conteudosRecomendados.add(content);
				}
			}
		}

		if(conteudosRecomendados.isEmpty())
		return conteudos;

		return conteudosRecomendados;
	}

	private List<Content> filtrarConteudosDasTrilhasDosAlunosDoGrupo(List<Student> grupo, List<Content> conteudos)
	{
		List<Content> conteudosEmTodos = new ArrayList<>();

		Map<String, Integer> conteudosDoGrupo = new HashMap<>();

		for (Student aluno : grupo) 
		{
			List<String> trilha = aluno.getTrilha();
			for (String conteudo : trilha) {
				if(conteudosDoGrupo.containsKey(conteudo))
				{
					conteudosDoGrupo.put(conteudo, conteudosDoGrupo.get(conteudo)+1);
				}
				else
				{
					conteudosDoGrupo.put(conteudo, 1);
				}
			}			
		}

		Map<String, Integer> conteudosOrdenados = sortByValue(conteudosDoGrupo);
		
		if(conteudosOrdenados.size()>10)
		{
			conteudosOrdenados = conteudosOrdenados.entrySet().stream()
								.limit(10)
								.collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

		}

		for (Entry<String, Integer> entry : conteudosOrdenados.entrySet())
		{
			for (Content content : conteudos) {
				if(content.getName().equals(entry.getKey()))
				{
					content.pontos+=entry.getValue();
					conteudosEmTodos.add(content);
				}
			}
		}

		return conteudosEmTodos;
	}

	public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());
		Collections.reverse(list);
        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
