package sacip.sti.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.midas.as.AgentServer;
import org.midas.as.manager.execution.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sacip.sti.components.DBConnection;
import sacip.sti.dataentities.Content;
import sacip.sti.dataentities.Student;

public class DataHolder {

    private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);
    public static DataHolder dh;

    Map<String, Map<String, Object>> dados = new HashMap<>();
    Map<String, Map<String, Object>> dadosMainipulados = new HashMap<>();
    String mainTopic = "";
    
    float valor_relevante_Recomendacoes = 0f;
    float valor_relevante_NoGrupo = 0f;
    float valor_relevante_recomendado = 0f;
    float valor_relevante_banco = 0f;

    public DataHolder() {
        super();
    }

    public static DataHolder getInstance()
    {
        if(dh==null)
        {
            dh = new DataHolder();
        }
        return dh;
    }

    public Map<String, Map<String, Object>> getDados()
    {
        return dados;
    }

    public void adicionarDados(String posicaoAluno, String tipodado, Object dado)
    {
        if(!dados.containsKey(posicaoAluno))
        {
            Map<String, Object> mapadedados = new HashMap<>();
            //List<Object> listaDados = new ArrayList<>();
            
            //listaDados.add(dado);
            mapadedados.put(tipodado, dado);
            dados.put(posicaoAluno, mapadedados);
        }
        else
        {
            Map<String, Object> mapadedados = dados.get(posicaoAluno);
            if(!mapadedados.containsKey(tipodado))
            {
                //List<Object> listaDados = new ArrayList<>();
                //listaDados.add(dado);
                mapadedados.put(tipodado, dado);
            }
            else
            {
                //List<Object> listaDados = mapadedados.get(tipodado);
                //listaDados.add(dado);
                mapadedados.put(tipodado, dado);
            }
            dados.put(posicaoAluno, mapadedados);
        }
    }

    public void setTopic(String topic)
    {
        this.mainTopic = topic;
    }

    public void imprimirDadosManipulados()
    {
        imprimirDadosMediaPorcentagemDeInteressesDoAlunoNoGrupo();
        imprimirDadosMediaPorcentagemDeInteressesDoAlunoNasRecomendacoes();
        imprimirDadosMediaPorcentagemDeComparacaoMelhoresConteudos();
        imprimirDadosdeRelevância();
    }

    public void resetar_dados()
    {
        dados.clear();
    }

    public void imprimirDados()
    {
        String[] colunas = {"posicao conhecimento", "interesses do aluno",
                            "10 tags mais comuns nos conteudos", "10 tags mais comuns do grupo","Melhor conteudo do grupo e tags","Melhor conteudo do banco e tags",
                            "% tags semelhantes ao melhor"};
        //Map<String, String> printableData = new LinkedHashMap<>();
        //List<String> fileLines = new ArrayList<>();
        //fileLines.add("posicao conhecimento;interesses do aluno;10 tags mais comuns nos conteudos;topicos do grupo;10 tags mais comuns do grupo;10 melhores conteudos do grupo;melhor conteudo do banco;% tags semelhantes ao estudante");
        
        
        for (Entry<String, Map<String, Object>> entry : dados.entrySet()) {
            String position = entry.getKey();
            Student estudante = (Student) entry.getValue().get("estudante");
            List<Student> grupo = (List<Student>) entry.getValue().get("grupo");
            List<Content> recomendacoes = (List<Content>) entry.getValue().get("recomendacoes");
            List<String> topicosFaltantes = (List<String>) entry.getValue().get("topicosFaltantes");
            Integer proximoNivel = (Integer) entry.getValue().get("proximoNivel");
            

            // List<String> interessesDoAluno = String.join(" ", estudante.getPreferencias());
            List<String> tagscomuns = retorneTagsComuns(recomendacoes);
            List<String> tagsComunsGrupo = retorneTagsComunsGrupo(grupo);
            List<Content> melhoresConteudos = retorneMelhoresConteudos(recomendacoes, estudante.getPreferencias(), topicosFaltantes, proximoNivel);
            //String melhoresConteudosString = melhoresConteudos.get(0).getName() + ": " + melhoresConteudos.get(0).getTags();
            Content melhorConteudoBanco = retorneMelhorConteudoBanco(estudante.getPreferencias(), topicosFaltantes, proximoNivel);
            //String conteuDoBanco = melhorConteudoBanco.getName() + ": " + melhorConteudoBanco.getTags();
            //String propSemelhantes = retorneProporcaoSemelhantes(estudante.getPreferencias(), melhoresConteudos.get(0));

            salvarDadosManipulados(estudante.getName(), position, estudante.getPreferencias(), tagsComunsGrupo, tagscomuns, melhoresConteudos.get(0).getTags(), melhorConteudoBanco.getTags());
        }        
             
    }

    public void salvarDadosManipulados(String nome, String topico,  List<String> interessesAluno, List<String> interessesGrupo, List<String> tagsComunsConteudos,
                                        List<String> tagsMelhorConteudoRecomendado, List<String> tagsMelhorConteudoBanco){
        if(!dadosMainipulados.containsKey(nome))
        {
            Map<String, Object> mapadedados = new HashMap<>();
            mapadedados.put("interessesAluno", interessesAluno);
            mapadedados.put("interessesGrupo", interessesGrupo);
            float relevancia = calcularRelevanciaDoGrupo(interessesAluno, interessesGrupo);
            mapadedados.put("relevanciaDoGrupo", relevancia);
            dadosMainipulados.put(nome, mapadedados);
        }

        Map<String, Object> mapadedados = dadosMainipulados.get(nome);

        if(!mapadedados.containsKey(topico))
        {
            Map<String, Object> mapadedadosTopico = new HashMap<>();

            mapadedados.put(topico, mapadedadosTopico);
        }

        Map<String, Object> mapadedadosTopico = (Map<String, Object>) mapadedados.get(topico);

        mapadedadosTopico.put("tagsComunsConteudos", tagsComunsConteudos);
        float relevancia = calcularRelevanciaDoGrupo(interessesAluno, tagsComunsConteudos);
        mapadedadosTopico.put("relevanciaConteudos", relevancia);

        mapadedadosTopico.put("tagsMelhorConteudoRecomendado", tagsMelhorConteudoRecomendado);
        relevancia = calcularRelevanciaDoGrupo(interessesAluno, tagsMelhorConteudoRecomendado);
        mapadedadosTopico.put("relevanciaMelhorConteudoRecomendado", relevancia);
        
        mapadedadosTopico.put("tagsMelhorConteudoBanco", tagsMelhorConteudoBanco);
        relevancia = calcularRelevanciaDoGrupo(interessesAluno, tagsMelhorConteudoBanco);
        mapadedadosTopico.put("relevanciaMelhorConteudoBanco", relevancia);

        mapadedados.put(topico, mapadedadosTopico);
        dadosMainipulados.put(nome, mapadedados);

    }

    public float calcularRelevanciaDoGrupo(List<String> tagsInteresse, List<String> tagsInteressadas)
    {
        float contem = 0f;

        for (String interesse : tagsInteresse)
        {
            if(tagsInteressadas.contains(interesse))
            {
                contem++;
            }
        }

        float porcentagem = contem / tagsInteresse.size();

        return porcentagem;
    }

    public void imprimirDadosMediaPorcentagemDeInteressesDoAlunoNoGrupo()
    {
        File printTxT = new File("C:\\Users\\shina\\Desktop\\dataInteressesPorGrupo.txt");
        File printTxT2 = new File("C:\\Users\\shina\\Desktop\\Final - dataInteressesPorGrupo.txt");
        List<String> fileLines = new ArrayList<>();
        List<String> fileLines2 = new ArrayList<>();

        fileLines.add("nome;interesses do aluno;10 interesses mais comuns do grupo;Porcentagem de relevância");
        fileLines2.add("Relevancia Média do grupo entre alunos");

        for (Entry<String, Map<String, Object>> entry : dadosMainipulados.entrySet()) {

            Map<String, Object> mapadedados = entry.getValue();

            String estudante = entry.getKey();
            List<String> interessesAluno = (List<String>) mapadedados.get("interessesAluno");
            List<String> interessesGrupo = (List<String>) mapadedados.get("interessesGrupo");
            float relevancia = (float) mapadedados.get("relevanciaDoGrupo");

            valor_relevante_NoGrupo = (valor_relevante_NoGrupo+relevancia);

            fileLines.add(estudante+";"+interessesAluno+";"+interessesGrupo+";"+relevancia);
        }

        valor_relevante_NoGrupo = (valor_relevante_NoGrupo/dadosMainipulados.size());

        try {
            givenWritingStringToFile_whenUsingPrintWriter_thenCorrect(printTxT, fileLines);
        } catch (Exception e) {
            System.out.println("ERRO DE PRINT" + e);
        }
        try {
            givenWritingStringToFile_whenUsingPrintWriter_thenCorrect(printTxT2, fileLines2);
        } catch (Exception e) {
            System.out.println("ERRO DE PRINT" + e);
        }
    }

    public void imprimirDadosMediaPorcentagemDeInteressesDoAlunoNasRecomendacoes()
    {
        File printTxT = new File("C:\\Users\\shina\\Desktop\\dataInteressesPorRecomendacoes.txt");
        File printTxT2 = new File("C:\\Users\\shina\\Desktop\\Final - dataInteressesPorRecomendacoes.txt");
        List<String> fileLines = new ArrayList<>();
        List<String> fileLines2 = new ArrayList<>();

        fileLines.add("nome;Tópico;10 tags mais comuns nos conteudos;Porcentagem de relevância");
        fileLines2.add("Relevância média das recomendacoes");
        for (Entry<String, Map<String, Object>> entry : dadosMainipulados.entrySet()) {

            Map<String, Object> mapadedados = entry.getValue();
            String estudante = entry.getKey();

            for (Entry<String, Object> entry2 : mapadedados.entrySet()) {
                String topico = entry2.getKey();
                if(entry2.getValue() instanceof Map)
                {
                    Map<String, Object> mapadedadosTopico = (Map<String, Object>) entry2.getValue();
                    List<String> tagsComunsConteudo = (List<String>) mapadedadosTopico.get("tagsComunsConteudos");
                    float relevancia = (float) mapadedadosTopico.get("relevanciaConteudos");
                    fileLines.add(estudante+";"+topico+";"+tagsComunsConteudo+";"+relevancia);
                    valor_relevante_Recomendacoes = (valor_relevante_Recomendacoes+relevancia);
                }
            }
        }
        System.out.println("("+valor_relevante_Recomendacoes+") / ("+dadosMainipulados.size()*15+")");
        valor_relevante_Recomendacoes = (valor_relevante_Recomendacoes/(dadosMainipulados.size()*15));

        try {
            givenWritingStringToFile_whenUsingPrintWriter_thenCorrect(printTxT, fileLines);
        } catch (Exception e) {
            System.out.println("ERRO DE PRINT" + e);
        }
        try {
            givenWritingStringToFile_whenUsingPrintWriter_thenCorrect(printTxT2, fileLines2);
        } catch (Exception e) {
            System.out.println("ERRO DE PRINT" + e);
        }
    }

    public void imprimirDadosMediaPorcentagemDeComparacaoMelhoresConteudos()
    {
        File printTxT = new File("C:\\Users\\shina\\Desktop\\dataInteressesPorMelhoresConteudos.txt");
        List<String> fileLines = new ArrayList<>();
        fileLines.add("nome;Tópico;Tags Melhor Conteudo Recomendados;Tags Melhor Conteudo Banco");
        
        for (Entry<String, Map<String, Object>> entry : dadosMainipulados.entrySet()) {

            Map<String, Object> mapadedados = entry.getValue();
            String estudante = entry.getKey();

            for (Entry<String, Object> entry2 : mapadedados.entrySet()) {
                String topico = entry2.getKey();
                if(entry2.getValue() instanceof Map)
                {
                    Map<String, Object> mapadedadosTopico = (Map<String, Object>) entry2.getValue();
                    
                    List<String> tagsMelhorConteudoRecomendado = (List<String>) mapadedadosTopico.get("tagsMelhorConteudoRecomendado");
                    float relevanciaMelhorConteudoRecomendado = (float) mapadedadosTopico.get("relevanciaMelhorConteudoRecomendado");
    
                    List<String> tagsMelhorConteudoBanco = (List<String>) mapadedadosTopico.get("tagsMelhorConteudoBanco");
                    float relevanciaMelhorConteudoBanco = (float) mapadedadosTopico.get("relevanciaMelhorConteudoBanco");
                    
                    
                    fileLines.add(estudante+";"+topico+";"+tagsMelhorConteudoRecomendado+";"+tagsMelhorConteudoBanco);
    
                    valor_relevante_recomendado = (valor_relevante_recomendado+relevanciaMelhorConteudoRecomendado);
                    valor_relevante_banco = (valor_relevante_banco+relevanciaMelhorConteudoBanco);
                }
                
            }
        }
        valor_relevante_recomendado = valor_relevante_recomendado/(dadosMainipulados.size()*(15));
        valor_relevante_banco = valor_relevante_banco/(dadosMainipulados.size()*(15));

        try {
            givenWritingStringToFile_whenUsingPrintWriter_thenCorrect(printTxT, fileLines);
        } catch (Exception e) {
            System.out.println("ERRO DE PRINT" + e);
        }
    }

    public void imprimirDadosdeRelevância()
    {
        File printTxT = new File("C:\\Users\\shina\\Desktop\\dataInteressesPorMelhoresConteudos.txt");
        List<String> fileLines = new ArrayList<>();

        fileLines.add("Relevancia do Grupo;Relevância das Recomendacoes;Aderencia do Conteudo Recomendado;Aderência do Conteúdo do Banco");
        fileLines.add(valor_relevante_NoGrupo+";"+valor_relevante_Recomendacoes+";"+valor_relevante_recomendado+";"+valor_relevante_banco);


        try {
            givenWritingStringToFile_whenUsingPrintWriter_thenCorrect(printTxT, fileLines);
        } catch (Exception e) {
            System.out.println("ERRO DE PRINT" + e);
        }
    }



    public void givenWritingStringToFile_whenUsingPrintWriter_thenCorrect(File fileName, List<String> lines)
    throws IOException {
        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        for (String string : lines) {
            printWriter.println(string);
        }
        printWriter.close();
    }

    private String retorneMelhoresConteudosEmString(List<Content> conteudos)
    {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Content content : conteudos) {
            builder.append(content.getName());
            builder.append(" ");
            i++;
            if(i>=10)
            {
                break;
            }
        }
        return builder.toString();
    }

    private String retorneProporcaoSemelhantes(List<String> preferencias, Content melhorConteudoRecom)
    {
        double found = 0;
        for (String tag  : preferencias) {
            
            if(melhorConteudoRecom.getTags().contains(tag))
            {
                found++;
            }
        }

        return (found/preferencias.size())*100+"%";
    }

    private Content retorneMelhorConteudoBanco(List<String> preferencias, List<String> topicosFaltantes, int proximoNivel)
    {
        try {
            // DBConnection conect = new DBConnection();
            // Map<String, Object> data = new HashMap<>();
            // data.put("tags", preferencias);
            // List out = new ArrayList<>();
            // conect.provide("getContentByTags", data, out);
            // conect.close_conection();
            ServiceWrapper wrapper = AgentServer.require("SACIP", "getContentByTags");
			wrapper.addParameter("tags", preferencias);
			List run = wrapper.run();
            List<Content> busca = (List<Content>)run.get(0);
            return retorneMelhoresConteudos(busca, preferencias, topicosFaltantes, proximoNivel).get(0);
            
        } catch (Exception e) {
            LOG.error("ERRO MELHOR CONTEUDO", e);
            e.printStackTrace();
            return null;
        }
    }

    private List<Content> retorneMelhoresConteudos(List<Content> conteudos, List<String> preferenciasAluno, List<String> topicosFaltantes, int proximoNivel)
    {
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
            content.pontos = 0;
            content.pontos += calculateTagPoints(content.getTags(), preferenciasAluno);
            if(topicosFaltantes.contains(content.getTopic()))
            {
                content.pontos += 10;
            }
            else if(proximoNivel == content.getLevel())
            {
                content.pontos += 10;
            }
            sortedContent.add(content);
        }

        return sortedContent;
    }

    private List<String> retorneTagsComuns(List<Content> conteudos)
    {
        List<String> tagsComuns = new ArrayList<>();
        Map<String, Integer> pontuacaoTags = new LinkedHashMap<>();
        
        for (Content conteudo : conteudos) {
            List<String> tags = conteudo.getTags();
            for (String tag : tags) {
                if(pontuacaoTags.containsKey(tag))
                {
                    pontuacaoTags.put(tag, pontuacaoTags.get(tag)+1);
                }
                else
                {
                    pontuacaoTags.put(tag, 1);
                }                
            }            
        }
        pontuacaoTags = sortByValue(pontuacaoTags);
        int i = 0;
        for (String string : pontuacaoTags.keySet()) {
            i++;
            tagsComuns.add(string);
            // if(i>=10)
            // {
            //     break;
            // }
        }

        return tagsComuns;
    }

    private List<String> retorneTagsComunsGrupo(List<Student> estudante)
    {
        List<String> tagsComuns = new ArrayList<>();
        Map<String, Integer> pontuacaoTags = new LinkedHashMap<>();
        
        for (Student conteudo : estudante) {
            List<String> tags = conteudo.getPreferencias();
            for (String tag : tags) {
                if(pontuacaoTags.containsKey(tag))
                {
                    pontuacaoTags.put(tag, pontuacaoTags.get(tag)+1);
                }
                else
                {
                    pontuacaoTags.put(tag, 1);
                }                
            }            
        }
        pontuacaoTags = sortByValue(pontuacaoTags);
        int i = 0;
        for (String string : pontuacaoTags.keySet()) {
            i++;
            tagsComuns.add(string);
            // if(i>=10)
            // {
            //     break;
            // }
        }

        return tagsComuns;
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

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
