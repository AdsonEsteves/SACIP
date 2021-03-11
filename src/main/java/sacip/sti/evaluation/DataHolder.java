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

import sacip.sti.components.DBConnection;
import sacip.sti.dataentities.Content;
import sacip.sti.dataentities.Student;

public class DataHolder {

    public static DataHolder dh;

    Map<String, Map<String, Object>> dados = new HashMap<>();
    String mainTopic = "";

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

    public void imprimirDados()
    {
        String[] colunas = {"posicao conhecimento", "interesses do aluno",
                            "10 tags mais comuns nos conteudos", "10 tags mais comuns do grupo","Melhor conteudo do grupo e tags","Melhor conteudo do banco e tags",
                            "% tags semelhantes ao melhor"};
        //Map<String, String> printableData = new LinkedHashMap<>();
        List<String> fileLines = new ArrayList<>();
        fileLines.add("posicao conhecimento;interesses do aluno;10 tags mais comuns nos conteudos;topicos do grupo;10 tags mais comuns do grupo;10 melhores conteudos do grupo;melhor conteudo do banco;% tags semelhantes ao estudante");
        
        File printTxT = new File("C:\\Users\\shina\\Desktop\\data.txt");
        for (Entry<String, Map<String, Object>> entry : dados.entrySet()) {
            String position = entry.getKey();
            Student estudante = (Student) entry.getValue().get("estudante");
            List<Student> grupo = (List<Student>) entry.getValue().get("grupo");
            List<Content> recomendacoes = (List<Content>) entry.getValue().get("recomendacoes");
            List<String> topicosFaltantes = (List<String>) entry.getValue().get("topicosFaltantes");
            Integer proximoNivel = (Integer) entry.getValue().get("proximoNivel");
            

            String interessesDoAluno = String.join(" ", estudante.getPreferencias());
            String tagscomuns = retorneTagsComuns(recomendacoes);
            String tagsComunsGrupo = retorneTagsComunsGrupo(grupo);
            List<Content> melhoresConteudos = retorneMelhoresConteudos(recomendacoes, estudante.getPreferencias(), topicosFaltantes, proximoNivel);
            String melhoresConteudosString = melhoresConteudos.get(0).getName() + ": " + melhoresConteudos.get(0).getTags();
            Content melhorConteudoBanco = retorneMelhorConteudoBanco(estudante.getPreferencias(), topicosFaltantes, proximoNivel);
            String conteuDoBanco = melhorConteudoBanco.getName() + ": " + melhorConteudoBanco.getTags();
            String propSemelhantes = retorneProporcaoSemelhantes(estudante.getPreferencias(), melhoresConteudos.get(0));

            fileLines.add(position+";"+interessesDoAluno+";"+tagscomuns+";"+mainTopic+";"+tagsComunsGrupo+";"+melhoresConteudosString+";"+conteuDoBanco+";"+propSemelhantes);
        }

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
            DBConnection conect = new DBConnection();
            Map<String, Object> data = new HashMap<>();
            data.put("tags", preferencias);
            List out = new ArrayList<>();
            conect.provide("getContentByTags", data, out);
            List<Content> busca = (List<Content>)out.get(0);
    
            return retorneMelhoresConteudos(busca, preferencias, topicosFaltantes, proximoNivel).get(0);
            
        } catch (Exception e) {
            System.out.println("ERRRO DO MELHOR CONTEUDO: "+e);
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

    private String retorneTagsComuns(List<Content> conteudos)
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
            if(i>=10)
            {
                break;
            }
        }

        return String.join(" ", tagsComuns);
    }

    private String retorneTagsComunsGrupo(List<Student> estudante)
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
            if(i>=10)
            {
                break;
            }
        }

        return String.join(" ", tagsComuns);
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
