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

import com.fasterxml.jackson.databind.ObjectMapper;

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
import sacip.sti.evaluation.DataHolder;

public class RecommenderAgent extends Agent {

	private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);
	// String[] taxBloom = {"Lembrar", "Compreeder", "Aplicar", "Analisar",
	// "Avaliar", "Criar"};
	private static final int LEMBRAR = 0;
	private static final int COMPREENDER = 1;
	private static final int APLICAR = 2;
	private static final int ANALISAR = 3;
	private static final int AVALIAR = 4;
	private static final int CRIAR = 5;

	@Override
	public void provide(String service, Map in, List out) throws ServiceException {
		switch (service) {
			case "getRecommendedContent":
				out.add(getConteudosRecomendados((List<Student>) in.get("grupo"), (Student) in.get("estudante"),
						(List<Content>) in.get("trilha")));
				break;

			default:
				throw new ServiceException("Serviço " + service + " não foi implementado no agente recomendador.");
		}
	}

	@Override
	protected void lifeCycle() throws LifeCycleException, InterruptedException {
		// Board.setContextAttribute("eventState", "checkErrors");
	}

	private Object getConteudosRecomendados(List<Student> grupo, Student aluno, List<Content> trilha) {

		List<String> preferenciasAluno = aluno.getPreferencias();

		// lista filtrada por pontos
		List<Content> sortedContent = new ArrayList<Content>() {
			@Override
			public boolean add(Content e) {
				super.add(e);
				Collections.sort(this, new Comparator<Content>() {
					@Override
					public int compare(Content o1, Content o2) {
						return Double.compare(o2.pontos, o1.pontos);
					}
				});
				return true;
			}
		};

		try {
			List<Content> conteudos = new ArrayList<>();
			// Verificar os tópicos e níveis que o aluno utilizou
			HashMap<Integer, List<String>> nivelETopico = descobrirNiveisETopicos(trilha);
			Integer[] niveisFeitos = nivelETopico.keySet().toArray(new Integer[nivelETopico.size()]);
			// Fazer chamada ao banco buscando os conteúdos desses níveis
			// Fazer chamada ao banco
			ServiceWrapper servicoPegarConteudosEmNiveis = require("SACIP", "findContents");

			if (!trilha.isEmpty()) {
				servicoPegarConteudosEmNiveis.addParameter("level", niveisFeitos);
				List resultado = servicoPegarConteudosEmNiveis.run();
				if (resultado.get(0) == null || resultado.get(0) instanceof String) {
					return "não há conteúdos";
				}
				conteudos = (List<Content>) resultado.get(0);
			}

			// Verificar se há algum tópico faltante em um dos níveis feitos do aluno
			// (guardar níveis completados?)
			List<String> topicosFaltantes = descobrirTopicosFaltantes(conteudos, nivelETopico);
			List<Content> conteudosPorTopicosFaltantes = new ArrayList<>();

			// recomendar os tópicos de níveis mais baixos.
			// pegar os conteúdos desse tópico
			int proximoNivelAluno = 0;
			if (topicosFaltantes.isEmpty()) {
				for (int i = 0; i < niveisFeitos.length; i++) {
					int nivelFeito = niveisFeitos[i];
					if (nivelFeito > proximoNivelAluno) {
						proximoNivelAluno = nivelFeito;
					}
				}
				proximoNivelAluno++;

				ServiceWrapper servicoPegarConteudosDoNivel = require("SACIP", "findContents");
				servicoPegarConteudosDoNivel.addParameter("level", proximoNivelAluno);
				List resultado = servicoPegarConteudosDoNivel.run();
				if (resultado.get(0) == null || resultado.get(0) instanceof String) {
					return "não há conteúdos";
				}
				conteudos.addAll((List<Content>) resultado.get(0));
				// conteudosPorTopicosFaltantes.addAll((List<Content>) resultado.get(0));
				conteudosPorTopicosFaltantes = filtrarConteudosPorNivel(conteudos, proximoNivelAluno);
			} else {
				conteudosPorTopicosFaltantes = filtrarConteudosPorTopicos(conteudos, topicosFaltantes);
			}
			// conteudosPorTopicosFaltantes =
			// filtrarConteudosPorTags(conteudosPorTopicosFaltantes, preferenciasAluno);

			List<Content> conteudosDasTrilhas = new ArrayList<>();
			if (!grupo.isEmpty()) {
				// VERIFICAR TRILHAS E PEGAR CONTEUDOS UTILIZADOS NOS SEMELHANTES
				conteudosDasTrilhas = filtrarConteudosDasTrilhasDosAlunosDoGrupo(grupo, conteudos, trilha);
			}

			// CONTEUDOS DO TÓPICO EM QUE ELE ESTÁ

			// VER OS ULTIMOS CONTEUDOS POR TÓPICO QUE ELE FEZ: MAPA(TOPICOS, CONTEUDOS) =>
			// MAPA(TOPICOS/PROXIMATAXONOMIA)
			Map<String, Integer> taxonomiasPorTopicos = descobrirProximosConteudosPorTaxonomia(trilha);
			List<Content> conteudosPorNovasTaxonomias = new ArrayList<>();
			if (!taxonomiasPorTopicos.isEmpty()) {
				// BUSCAR NOVOS CONTEUDOS DE NOVA TAXONOMIA PARA CADA TOPICO
				conteudosPorNovasTaxonomias = descobrirConteudosDeTaxonomia(conteudos, taxonomiasPorTopicos);

				// FILTRAR POR TAGS
				// conteudosPorNovasTaxonomias =
				// filtrarConteudosPorTags(conteudosPorNovasTaxonomias, preferenciasAluno);
			}

			Set<Content> conteudosFiltrados = new HashSet<>();
			conteudosFiltrados.addAll(conteudosPorTopicosFaltantes);
			conteudosPorNovasTaxonomias.forEach(t -> {
				if (conteudosFiltrados.stream().noneMatch(c -> c.getName().equalsIgnoreCase(t.getName()))) {
					conteudosFiltrados.add(t);
				}
			});
			conteudosDasTrilhas.forEach(t -> {
				if (conteudosFiltrados.stream().noneMatch(c -> c.getName().equalsIgnoreCase(t.getName()))) {
					conteudosFiltrados.add(t);
				}
			});

			// PRIORIZAR POR PONTOS
			for (Content content : conteudosFiltrados) {
				String motivoDeEscolhar = "";
				double escolha = 0;
				// content.pontos += calculateTagPoints(content.getTags(), preferenciasAluno);

				if (!aluno.getTrilha().contains(content.getName())) {

					Content groupContent = conteudosDasTrilhas.stream().filter(c -> c.getName() == content.getName())
							.findFirst().orElse(null);
					if (groupContent != null) {
						if (escolha < content.pontos) {
							motivoDeEscolhar = "GRUPO";
							escolha = content.pontos;
						}
					} else {
						content.pontos = 0.0;
					}

					if (conteudosPorNovasTaxonomias.contains(content)) {
						content.pontos += (grupo.size() + 1.0) / 2.0;
						if (escolha < (grupo.size() + 1.0) / 2.0) {
							motivoDeEscolhar = "TAXONOMIA";
							escolha = (grupo.size() + 1.0) / 2.0;
						}
					}

					if (preferenciasAluno.contains(content.getType())) {
						content.pontos += (grupo.size() + 1.0) / 4.0;
						if (escolha < (grupo.size() + 1.0) / 4.0) {
							escolha = (grupo.size() + 1.0) / 4.0;
							motivoDeEscolhar = "PREFERENCIAS";
						}
					}
					content.motivo = motivoDeEscolhar;
					sortedContent.add(content);
				}
			}

			List<Content> top10Conteudos = new ArrayList<>();

			if (sortedContent.size() >= 10) {
				for (int i = 0; i < 10; i++) {
					top10Conteudos.add(sortedContent.get(i));
				}
			} else {
				top10Conteudos.addAll(sortedContent);
			}
			int nivelMaximo = niveisFeitos.length;
			String topicoMaximo = "0";
			if (!nivelETopico.isEmpty()) {
				List<String> list = nivelETopico.get(nivelMaximo);
				if (list != null && !list.isEmpty())
					topicoMaximo = list.get(list.size() - 1);
			}

			if (!grupo.isEmpty()) {
				DataHolder.getInstance().adicionarDados("nv" + nivelMaximo + "-tpc" + topicoMaximo, "grupo", grupo);
				DataHolder.getInstance().adicionarDados("nv" + nivelMaximo + "-tpc" + topicoMaximo, "estudante", aluno);
				DataHolder.getInstance().adicionarDados("nv" + nivelMaximo + "-tpc" + topicoMaximo, "recomendacoes",
						conteudosDasTrilhas);
				DataHolder.getInstance().adicionarDados("nv" + nivelMaximo + "-tpc" + topicoMaximo, "topicosFaltantes",
						topicosFaltantes);
				DataHolder.getInstance().adicionarDados("nv" + nivelMaximo + "-tpc" + topicoMaximo, "proximoNivel",
						proximoNivelAluno);
				DataHolder.getInstance().adicionarDados("nv" + nivelMaximo + "-tpc" + topicoMaximo,
						"conteudosPorNovasTaxonomias",
						conteudosPorNovasTaxonomias);
				DataHolder.getInstance().adicionarDados("nv" + nivelMaximo + "-tpc" + topicoMaximo,
						"motivoDeEscolha",
						sortedContent.get(0).motivo);
				DataHolder.getInstance().imprimirDados();
			}

			// retornando conteudos
			String exercicio = top10Conteudos.toString();
			return new ObjectMapper().valueToTree(top10Conteudos);
		} catch (Exception e) {
			LOG.error("ERRO NO PEDAGOGICAL AGENT AO SUGERIR EXERCÍCIOS", e);
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
	}

	private int calculateTagPoints(List<String> tagsConteudo, List<String> preferenciasAluno) {
		int pontos = 0;

		for (String tagConteudo : tagsConteudo) {
			if (preferenciasAluno.contains(tagConteudo)) {
				pontos++;
			}
		}

		return pontos;
	}

	private HashMap<Integer, List<String>> descobrirNiveisETopicos(List<Content> trilha) {
		HashMap<Integer, List<String>> nivelETopic = new HashMap();

		for (Content content : trilha) {
			Integer nivel = content.getLevel();
			String topico = content.getTopic();
			if (nivelETopic.containsKey(nivel)) {
				if (!nivelETopic.get(nivel).contains(topico))
					nivelETopic.get(nivel).add(topico);
			} else {
				List<String> topicos = new ArrayList<>();
				topicos.add(topico);
				nivelETopic.put(nivel, topicos);
			}
		}
		return nivelETopic;
	}

	private List<String> descobrirTopicosFaltantes(List<Content> conteudosBuscados,
			HashMap<Integer, List<String>> niveisTopicosDoAluno) {
		HashMap<Integer, List<String>> niveisETopicosBuscados = descobrirNiveisETopicos(conteudosBuscados);
		List<String> topicosFaltantes = new ArrayList<>();

		if (!niveisTopicosDoAluno.isEmpty())
			for (Entry<Integer, List<String>> entry : niveisETopicosBuscados.entrySet()) {
				Integer nivel = entry.getKey();
				List<String> listaTopicos = entry.getValue();
				for (String topico : listaTopicos) {
					if (!niveisTopicosDoAluno.get(nivel).contains(topico)) {
						topicosFaltantes.add(topico);
					}
				}
			}

		return topicosFaltantes;
	}

	private List<Content> filtrarConteudosPorTopicos(List<Content> conteudos, List<String> topicos) {
		List<Content> conteudosRecomendados = new ArrayList<>();

		for (Content content : conteudos) {
			if (topicos.contains(content.getTopic())) {
				conteudosRecomendados.add(content);
			}
		}

		if (conteudosRecomendados.isEmpty()) {
			return conteudos;
		}

		return conteudosRecomendados;
	}

	private List<Content> filtrarConteudosPorNivel(List<Content> conteudos, int nivel) {
		List<Content> conteudosRecomendados = new ArrayList<>();

		for (Content content : conteudos) {
			if (nivel == content.getLevel()) {
				conteudosRecomendados.add(content);
			}
		}

		if (conteudosRecomendados.isEmpty()) {
			return conteudos;
		}

		return conteudosRecomendados;
	}

	private List<Content> filtrarConteudosPorTags(List<Content> conteudos, List<String> caracteristicas) {
		List<Content> conteudosRecomendados = new ArrayList<>();

		for (Content content : conteudos) {

			if (!caracteristicas.stream().anyMatch(element -> content.getTags().contains(element.toLowerCase()))) {
				continue;
			}

			conteudosRecomendados.add(content);
		}

		if (conteudosRecomendados.isEmpty()) {
			return conteudos;
		}

		return conteudosRecomendados;
	}

	private Map<String, Integer> descobrirProximosConteudosPorTaxonomia(List<Content> trilha) {
		Map<String, Integer> taxonomiaPorTopicos = new HashMap<>();
		if (trilha.isEmpty()) {
			taxonomiaPorTopicos.put("t1-1", LEMBRAR);
		}

		for (Content content : trilha) {
			String topico = content.getTopic();
			int tax = content.getTaxonomy();
			if (taxonomiaPorTopicos.containsKey(topico)) {
				int setTax = taxonomiaPorTopicos.get(topico);
				boolean foundTax = false;
				for (int i = 0; i < CRIAR; i++) {
					if (i == tax || i == setTax) {
						if (!foundTax) {
							foundTax = true;
						} else {
							taxonomiaPorTopicos.put(topico, i + 1);
						}
					}
				}
			} else {
				if (tax != CRIAR)
					taxonomiaPorTopicos.put(topico, tax + 1);
			}
		}

		return taxonomiaPorTopicos;
	}

	private List<Content> descobrirConteudosDeTaxonomia(List<Content> conteudos,
			Map<String, Integer> taxonomiasPorTopicos) {
		List<Content> conteudosRecomendados = new ArrayList<>();
		List<Content> conteudosTopico = new ArrayList<>();

		for (Content content : conteudos) {
			if (taxonomiasPorTopicos.containsKey(content.getTopic())) {
				conteudosTopico.add(content);
				int tax = taxonomiasPorTopicos.get(content.getTopic());
				if (content.getTaxonomy() == tax) {
					conteudosRecomendados.add(content);
				}
			}
		}
		int count = 1;
		while (conteudosRecomendados.isEmpty()) {
			for (Content content : conteudosTopico) {
				int tax = taxonomiasPorTopicos.get(content.getTopic());
				if (content.getTaxonomy() == tax + count) {
					conteudosRecomendados.add(content);
				}
			}
			if (count > CRIAR || !conteudosRecomendados.isEmpty()) {
				break;
			}
			count++;
		}

		if (conteudosRecomendados.isEmpty())
			return conteudos;

		return conteudosRecomendados;
	}

	private List<Content> filtrarConteudosDasTrilhasDosAlunosDoGrupo(List<Student> grupo, List<Content> conteudos,
			List<Content> reqtrilha) {
		List<Content> conteudosEmTodos = new ArrayList<>();

		Map<String, Double> conteudosDoGrupo = new HashMap<>();

		for (Student aluno : grupo) {
			List<String> trilha = aluno.getTrilha();
			for (String conteudo : trilha) {
				double mult = aluno.getNotaFinal() >= 8.0 ? 1.5 : aluno.getAssiduidade() > 0.94 ? 1.2 : 1.0;
				double points = 1 * mult / 2;
				if (conteudosDoGrupo.containsKey(conteudo)) {
					conteudosDoGrupo.put(conteudo, conteudosDoGrupo.get(conteudo) + points);
				} else {
					if (!reqtrilha.stream().anyMatch(s -> s.getName().equals(conteudo)))
						conteudosDoGrupo.put(conteudo, points);
				}
			}
		}

		Map<String, Double> conteudosOrdenados = sortByValue(conteudosDoGrupo);

		// if(conteudosOrdenados.size()>10)
		// {
		// conteudosOrdenados = conteudosOrdenados.entrySet().stream()
		// .limit(10)
		// .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()),
		// Map::putAll);

		// }

		for (Entry<String, Double> entry : conteudosOrdenados.entrySet()) {
			for (Content content : conteudos) {
				if (content.getName().equals(entry.getKey())) {
					content.pontos += entry.getValue();
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
