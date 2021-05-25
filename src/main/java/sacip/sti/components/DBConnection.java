package sacip.sti.components;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

import org.midas.as.AgentServer;
import org.midas.as.agent.templates.Component;
import org.midas.as.agent.templates.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sacip.sti.dataentities.Content;
import sacip.sti.dataentities.Student;
import sacip.sti.utils.BoltCypherExecutor;
import sacip.sti.utils.CypherExecutor;

@SuppressWarnings("unchecked")
public class DBConnection extends Component {

    private static final CypherExecutor cypher = new BoltCypherExecutor("bolt://localhost:7687", "neo4j", "123456", null);;
    private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

    public DBConnection() {
        super();
    }

    @Override
    public void provide(String service, Map in, List out) throws ServiceException {

        try {            
            switch (service) 
            {
                case "createStudent":
                    out.add(createUser((Student)in.get("conta")));
                    break;
    
                case "findStudents":
                    out.add(getUsers(in));
                    break;
                
                case "editStudent":
                    out.add(editUser((String)in.get("name"), (String)in.get("attrName"), (String)in.get("newValue")));
                    break;
    
                case "editStudentListAttr":
                    out.add(editUserListAttr((String)in.get("name"), (String)in.get("attrName"), (JsonNode)in.get("newValue")));
                    break;
    
                case "getLogsDoAluno":
                    out.add(getUserLogInformation((String)in.get("name"), (String)in.get("type")));
                    break;
                
                case "deleteStudent":
                    out.add(deleteUser((String) in.get("name")));
                    break;
                
                case "storeStudentUseData":
                    out.add(addClickInformation((String)in.get("name"), (Map)in.get("data")));
                    break;
    
                case "storeStudentContentUse":
                    out.add(addContentUseInformation((String)in.get("name"), (JsonNode)in.get("content")));
                    break;
                
                case "getStudentsContentUse":
                    out.add(getUserLogInformation((String)in.get("name"), (String)in.get("type")));
                    break;
    
                case "createContent":
                    out.add(createContent((Content)in.get("conteudo"), (String)in.get("conteudoRelacionado"), (int)in.get("valorRelacao")));
                    break;
                
                case "findContents":
                    out.add(getContents(in));
                    break;
                
                case "getContentByTags":
                    out.add(getContentsByTags((List<String>) in.get("tags")));
                    break;
                
                case "editContent":
                    out.add(editContent((String)in.get("name"), (String)in.get("attrName"), (String)in.get("newValue")));
                    break;
                
                case "deleteContent":
                    out.add(deleteContent((String) in.get("name")));
                    break;
                
                case "getInfo":
                    out.add(getContentInfo());
                    break;
                
            }
        }
        catch (Exception e) {
            out.add(e.getMessage());
            LOG.error("ERRO NOS SERVICOS DE CONEXAO DO BANCO DE DADOS", e);
        }
        finally{
            //cypher.close_connection();
        }

    }

    private Student instanceStudent(Map in)
    {    
        try {
            return new Student((String)in.get("name"),
                                (String)in.get("password"),
                                (String)in.get("avatar"),
                                (String)in.get("genero"),
                                ((Long)in.get("idade")).intValue(),
                                (String)in.get("nivelEdu"),
                                (List<String>) in.get("preferencias"),
                                (List<String>) in.get("trilha"));
        } catch (Exception e) {            
            LOG.error("Não foi possível instanciar o estudante", e);
            return null;
        }
    }

    private Content instanceContent(Map in)
    {
        try {
            return new Content((String)in.get("name"),
                                (String)in.get("descricao"),
                                ((Long)in.get("level")).intValue(),
                                (String)in.get("topic"),
                                (String)in.get("complexity"),
                                (boolean)in.get("exercise"),
                                ((Long)in.get("taxonomy")).intValue(),
                                (List<String>) in.get("tags"),
                                (String)in.get("link"),
                                (String)in.get("imageLink"));    
        } catch (Exception e) {
            LOG.error("Não foi possível instanciar o conteúdo", e);
            return null;
        }
    }

    // private void createConstraints() {
    //     var result = cypher.writequery("CREATE CONSTRAINT uniqueuser ON (u:USER) ASSERT u.name IS UNIQUE", Map.of());
    //     System.out.println(result);
    //     result = cypher.writequery("CREATE CONSTRAINT uniquecontent ON (c:CONTENT) ASSERT c.name IS UNIQUE", Map.of());
    //     System.out.println(result);
    // }

    public String createUser(Student student) {
        //TODO Mapear os dados de uso
        try 
        {
            List<Map<String, Object>> result = cypher.writequery("CREATE (u:USER {" 
                                        + "name: $name," 
                                        + "password: $password," 
                                        + "avatar: $avatar,"
                                        + "genero: $genero," 
                                        + "nivelEdu: $nivelEdu," 
                                        + "idade: $idade,"
                                        + "preferencias: $preferencias" 
                                        + "})",
                        Map.of("name", student.getName(),
                                "password", student.getPassword(),
                                "avatar", student.getAvatar(),
                                "genero", student.getGenero(),
                                "nivelEdu", student.getNivelEducacional(),
                                "idade", student.getIdade(),
                                "preferencias", student.getPreferencias()));
            return result.toString();            
        }
        catch (Exception e)
        {
            LOG.error("Não foi possível criar o estudante no banco", e);
            return "FALHOU criação de estudante "+e.getLocalizedMessage();
        }
    }

    private Object addContentUseInformation(String name, JsonNode contentdata)
    {
        try {
            String query =  "MATCH (n:USER { name : $name})"+
                            "MERGE (n)-[:USES]-(k:CONTENTUSE {name:$name})"+
                            "SET k.log = coalesce(k.log, []) + $klog";
            var result =  cypher.writequery(query, Map.of("name", name, "klog", contentdata.toString()));
            return result.toString();
        } catch (Exception e) {
            LOG.error("FALHOU AO ADICIONAR DADOS DE CONTEUDO USADO", e);
            return "FALHOU AO ADICIONAR DADOS DE CONTEUDO USADO"+ e;
        }
    }

    private Object addClickInformation(String name, Map<String, Object> cliqueReg){
        try 
        {
            //Cria query necessaria
            StringBuilder query = new StringBuilder("MATCH 	(n:USER {name: $name }) \n");
            Map<String, Object> updates = new HashMap<String, Object>();
            updates.put("name", name);         
            for (Map.Entry<String, Object> entry : cliqueReg.entrySet()) {
                switch(entry.getKey())
                {
                    case "Conteudo":
                        query.append("\nMERGE (n)-[:CLICKS]-(c:CONTENTLOG{name: $name})");
                    break;
                    
                    case "Exemplos":
                        query.append("\nMERGE (n)-[:CLICKS]-(e:EXEMPLOLOG{name: $name})");
                    break;
                    
                    case "OGPor":
                        query.append("\nMERGE (n)-[:CLICKS]-(o:OGPORLOG{name: $name})");
                    break;
                    
                    case "Ajuda":
                        query.append("\nMERGE (n)-[:CLICKS]-(a:AJUDALOG{name: $name})");
                    break;
                }
            }
            for (Map.Entry<String, Object> entry : cliqueReg.entrySet()) {
                switch(entry.getKey())
                {
                    case "Conteudo":
                        query.append("\nSET c.log = coalesce(c.log, []) + $clog");
                        updates.put("clog", cliqueReg.get(entry.getKey())); 
                    break;
                    
                    case "Exemplos":
                        query.append("\nSET e.log = coalesce(e.log, []) + $elog");
                        updates.put("elog", cliqueReg.get(entry.getKey())); 
                    break;
                    
                    case "OGPor":
                        query.append("\nSET o.log = coalesce(o.log, []) + $olog");
                        updates.put("olog", cliqueReg.get(entry.getKey())); 
                    break;
                    
                    case "Ajuda":
                        query.append("\nSET a.log = coalesce(a.log, []) + $alog");
                        updates.put("alog", cliqueReg.get(entry.getKey())); 
                    break;
                }
            }
            query.append("\nRETURN n");
            //realisa o set
            var result = cypher.writequery(query.toString(), updates);
            if(result.isEmpty())
            {
                return "SUCESSO";
            }

            return result.toString();
        } 
        catch (Exception e) 
        {    
            LOG.error("Não foi possível registrar os cliques do estudante no banco", e);
            return "FALHOU registro de cliques do estudante "+e.getLocalizedMessage();        
        }
    }

    private Object getUserLogInformation(String name, String type)
    {
        try {
            if(type.equals("CLICK"))
            {
                var result = cypher.readquery("MATCH [:CLICKS]-(n {name:$name})"+
                                              "RETURN n.log", Map.of("name", name));
                
                return result.toString();
            }
            else if(type.equals("USE"))
            {
                var result = cypher.readquery("MATCH (k:CONTENTUSE {name:$name}"+
                                              "RETURN k.log", Map.of("name", name));
                
                return result.toString();
            }

            throw new ServiceException("Usuario não possui esse tipo de informação");
            
        } catch (Exception e) {
            LOG.error("Não foi possível registrar os cliques do estudante no banco", e);
            return "FALHOU registro de cliques do estudante "+e.getLocalizedMessage();    
        }
    }

    private Object getUsers(Map<String, Object> attributes) {
        try 
        {
            //Cria query necessária
            StringBuilder query = new StringBuilder("MATCH (n:USER)");
            if(!attributes.isEmpty())
            {
                query.append("\nWHERE");
                for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                    query.append(" n.");
                    query.append(entry.getKey());
                    query.append(" IN [");
                    if(entry.getValue() instanceof String[])
                    {
                        String[] attrs = (String[]) entry.getValue();
                        for (String attr : attrs) {
                            query.append("'"+attr+"',");
                        }
                        query.deleteCharAt(query.length()-1);
                    }
                    else if(entry.getValue() instanceof Integer[])
                    {
                        Integer[] attrs = (Integer[]) entry.getValue();
                        for (Integer attr : attrs) {
                            query.append(""+attr+",");
                        }
                        query.deleteCharAt(query.length()-1);
                    }
                    else
                    {
                        query.append("'"+entry.getValue()+"'");
                    }
                    query.append("] AND");
                }
                query.replace(query.length()-3, query.length(), "");
            }
            query.append("\nRETURN n");
            
            //realisa a busca
            var result = cypher.readquery(query.toString(), attributes);
            if(result.isEmpty())
            {
                return null;
            }
    
            //Faz uma lista de estudantes encontrados
            List<Student> students = new ArrayList<>();
            for (Map<String,Object> map : result) {
                map = (Map<String, Object>) map.get("n");
                students.add(instanceStudent(map));
            }        
            return students;            
        } 
        catch (Exception e) 
        {
            LOG.error("FALHOU busca de estudante", e);
            return "FALHOU busca de estudante "+e.getLocalizedMessage();
        }
    }    

    private String editUser(String name, String atributeName, String newValue)
    {
        try 
        {
            var result = cypher.writequery("MATCH (n:USER { name: $name })"+
                                            "SET n."+atributeName+" = $newValue"+
                                            "RETURN n.name, n."+atributeName+""
                        , Map.of("name", name, "atribute", atributeName, "newValue", newValue));
            return result.toString();            
        } 
        catch (Exception e) 
        {
            LOG.error("FALHOU edicao de estudante", e);
            return "FALHOU edicao de estudante "+e.getLocalizedMessage();
        }
    }

    private String editUserListAttr(String name, String atributeName, JsonNode newValue)
    {
        try 
        {
            if(newValue instanceof TextNode)
            {
                var result = cypher.writequery("MATCH (n:USER { name: $name })"+
                                                "\nSET n."+atributeName+" = coalesce(n."+atributeName+", []) + $newValue"+
                                                "\nRETURN n.name, n."+atributeName
                            , Map.of("name", name, "atribute", atributeName, "newValue", newValue.asText()));
                return result.toString();
            }
            else{
                var result = cypher.writequery("MATCH (n:USER { name: $name })"+
                                                "\nSET n."+atributeName+" = coalesce(n."+atributeName+", []) + $newValue"+
                                                "\nRETURN n.name, n."+atributeName
                            , Map.of("name", name, "atribute", atributeName, "newValue", newValue.toString()));
                return result.toString();
            }
        } 
        catch (Exception e) 
        {
            LOG.error("FALHOU edicao de lista de estudante ", e);
            return "FALHOU edicao de lista de estudante "+e.getLocalizedMessage();
        }
    }

    private String deleteUser(String name)
    {
        try 
        {
            var result = cypher.writequery("MATCH (n:USER { name: $name })"+
                                           "DELETE n"
                        , Map.of("name", name));
            return result.toString();    
        }
        catch (Exception e) 
        {
            LOG.error("FALHOU remoção de estudante", e);
            return "FALHOU remoção de estudante "+e.getLocalizedMessage();
        }
    }

    private String createContent(Content content, String relatedName, int relationValue)
    {
        try 
        {
            StringBuilder query = new StringBuilder();
            
            if(relatedName!=null)
            query.append("MATCH (n:CONTENT {name: $relatedName})\n");

            query.append("CREATE (c:CONTENT "
                        +"{"
                        +"name: $name,"
                        +"level: $level,"
                        +"topic: $topic,"
                        +"complexity: $complexity,"
                        +"exercise: $exercise,"
                        +"taxonomy: $taxonomy,"
                        +"tags: $tags,"
                        +"link: $link"
                        +"})");
            

            HashMap<String, Object> map = new HashMap<>();
            map.putAll(Map.of("name", content.getName(),
                                "level", content.getLevel(),
                                "topic", content.getTopic(),
                                "complexity", content.getComplexity(),
                                "exercise", content.getExercise(),
                                "taxonomy", content.getTaxonomy(),
                                "tags", content.getTags(),
                                "link", content.getLink()
            ));

            if(relatedName!=null)
            {
                if(relationValue>0)
                {
                    query.append("-[:RELATED {dPoints: $relationValue}]->(n)");
                }
                else
                {
                    query.append("<-[:RELATED {dPoints: $relationValue}]-(n)");
                }
                map.put("relatedName", relatedName);
                map.put("relationValue", relationValue);
            }

            var result = cypher.writequery(query.toString(),map);
            return result.toString();    
        } 
        catch (Exception e) 
        {
            LOG.error("FALHOU criação de conteudo", e);
            return "FALHOU criação de conteudo "+ e.getLocalizedMessage();
        }
    }

    private Object getContents(Map<String, Object> attributes)
    {
        try {
            //Cria query necessária
            StringBuilder query = new StringBuilder("MATCH (n:CONTENT)");
            if(!attributes.isEmpty())
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                query.append("\nWITH*");
                query.append("\nWHERE ");
                if(entry.getKey().equals("tags"))
                {
                    query.append("all(tags IN ");
                    query.append("[ ");
                    String[] tags = (String[]) entry.getValue();
                    for (String tag : tags) {
                        query.append("'"+tag+"',");
                    }
                    query.deleteCharAt(query.length()-1);
                    query.append("] ");
                    query.append("WHERE tags IN n.tags) ");
                }
                else if(entry.getKey().equals("~name"))
                {
                    query.append("n.name =~ ");
                    query.append("'(?i).*"+entry.getValue()+".*'");
                }
                else
                {
                    query.append("n.");
                    query.append(entry.getKey());
                    query.append(" IN [");
                    if(entry.getValue() instanceof String[])
                    {
                        String[] attrs = (String[]) entry.getValue();
                        if(attrs.length!=0)
                        for (String attr : attrs) {
                            query.append("'"+attr+"',");
                        }
                        if(attrs.length!=0)
                        query.deleteCharAt(query.length()-1);
                    }
                    else if(entry.getValue() instanceof Integer[])
                    {
                        Integer[] attrs = (Integer[]) entry.getValue();
                        for (Integer attr : attrs) {
                            query.append(attr+",");
                        }
                        if(attrs.length!=0)
                        query.deleteCharAt(query.length()-1);
                    }
                    else if(entry.getValue() instanceof Boolean || entry.getValue() instanceof Integer)
                    {
                        query.append(entry.getValue());
                    }
                    else
                    {
                        query.append("'"+entry.getValue()+"'");
                    }
                    query.append("]");
                } 
            }
            query.append("\nRETURN n");
            //realisa a busca
            var result = cypher.readquery(query.toString(), attributes);
            if(result.isEmpty())
            {
                return new ArrayList<Content>();
            }
    
            //Faz uma lista de conteudos encontrados
            List<Content> content = new ArrayList<>();
            for (Map<String,Object> map : result) {
                map = (Map<String, Object>) map.get("n");
                content.add(instanceContent(map));
            }        
            return content;            
        } catch (Exception e) {
            LOG.error("FALHOU busca de conteudo", e);
            return "FALHOU busca de conteudo "+ e.getLocalizedMessage();
        }
    }
    
    private Object getContentsByTags(List<String> tags)
    {
        try {
            //Cria query necessária
            StringBuilder query = new StringBuilder("MATCH (n:CONTENT)");
            if(!tags.isEmpty())
            {
                query.append("\nWHERE ");
                query.append("any(tags IN n.tags WHERE tags IN ");
                query.append("[ ");
                for (String tag : tags) {
                    query.append("'"+tag+"',");
                }
                query.deleteCharAt(query.length()-1);
                query.append("] )");          
            }
            query.append("\nRETURN n");
            
            System.out.println(query.toString());
            //realisa a busca
            var result = cypher.readquery(query.toString(), Map.of());
            if(result.isEmpty())
            {
                return null;
            }
    
            //Faz uma lista de conteudos encontrados
            List<Content> content = new ArrayList<>();
            for (Map<String,Object> map : result) {
                map = (Map<String, Object>) map.get("n");
                content.add(instanceContent(map));
            }        
            return content;            
        } catch (Exception e) {
            LOG.error("FALHOU busca de conteudo", e);
            return "FALHOU busca de conteudo "+ e.getLocalizedMessage();
        }
    }

    private String editContent(String name, String atributeName, String newValue)
    {
        try 
        {
            var result = cypher.writequery("MATCH (n:CONTENT { name: $name })"+
                                            "SET n.$atribute = $newValue"+
                                            "RETURN n.name, n.$atribute"
                , Map.of("name", name, "atribute", atributeName, "newValue", newValue));
            return result.toString();            
        }
        catch (Exception e)
        {
            LOG.error("FALHOU edicao de conteudo", e);
            return "FALHOU edicao de conteudo "+ e.getLocalizedMessage();
        }
    }

    private String deleteContent(String name)
    {
        try 
        {
            var result = cypher.writequery("MATCH (n:CONTENT { name: $name })"+
                                           "DELETE n"
                        , Map.of("name", name));
            return result.toString();            
        } 
        catch (Exception e) 
        {
            LOG.error("FALHOU remoção de conteudo", e);
            return "FALHOU remoção de conteudo " + e.getLocalizedMessage();
        }
    }

    private String getContentInfo()
    {
        try 
        {
            var result = cypher.writequery("MATCH (n:INFO)"+
                                           "RETURN n"
                        , Map.of());
            
            return new ObjectMapper().writeValueAsString(result.get(0).get("n")).toString();            
        } 
        catch (Exception e) 
        {
            LOG.error("FALHOU pegar Informacao Conteudos", e);
            return "FALHOU pegar Informacao Conteudos" + e.getLocalizedMessage();
        }
    }

    private void showNodes()
    {
        var result = cypher.readquery("MATCH (n) RETURN n", Map.of());
        for (Map<String,Object> map : result) {
            System.out.println(map.toString());
        }
    }

    private void showNodeRelationships(String name)
    {
        var result = cypher.readquery("MATCH (n:USER {name: $name})--(r) RETURN n,r", Map.of("name", name));
        for (Map<String,Object> map : result) {
            System.out.println(map.toString());
        }
    }

    private void resetDB()
    {
        cypher.writequery("MATCH (n) DETACH DELETE n", Map.of());
    }

    private void doQuery(String query, Map mapa)
    {
        cypher.writequery(query, mapa);
    }

    public static void main(String[] args) {

        DBConnection conect = new DBConnection();
        //conect.showNodes();
        conect.resetDB();
        conect.dummyData();
        System.exit(0);
    }

    private static List<Content> contentNames = new ArrayList<>();

    private void dummyData()
    {
        String[] tags = {
                        "carros", "musica", "animes", "desenhos", "animacao", "jogos", "geografia",
                        "matematica", "linguas", "biologia", "animais", "pets", "imagens", "memes",
                        "mitologia", "marvel", "dc", "monstros", "youtube", "comedia", "cultura",
                        "filmes", "superherois", "historia", "esportes", "ciencia", "brinquedos",
                        "internacional", "tecnologia", "comidas", "livros"
                        }
                         ;
        String[][] topicos = {
                            {"t1", "t2", "t3"},
                            {"t4", "t5", "t6"},
                            {"t7", "t8", "t9"},
                            {"t10", "t11", "t12"},
                            {"t13", "t14", "t15"}                            
                            };
        String[] topicos1d = {
            "t1", "t2", "t3",
            "t4", "t5", "t6",
            "t7", "t8", "t9",
            "t10", "t11", "t12",
            "t13", "t14", "t15",                         
            };

        int niveis = 5;

        String[] complexidade = {"Matemática", "Cognitiva", "Algoritmo", "Codigo"};

        String[] generos = {"Masculino", "Feminino", "Transsexual", "Outro"};

        String[] niveisEdu = {"Fundamental", "EnsinoMedio", "Graduacao"};

        final int LIMITE = 500;

        final int LIMALU = 250;

        List<Map<String, Object>> mapas = new ArrayList<>();

        List<Map<String, Object>> dummyStudents = new ArrayList<>();

        

        for (int i = 0; i < LIMITE; i++) {
            mapas.add(mapaCriacaoConteudo(returnRandomTags(tags), topicos, niveis, complexidade));
        }

        for (int i = 0; i < LIMALU; i++) {
            //dummyStudents.add(mapaCriacaoUsuario(returnRandomTags(tags), generos, niveisEdu));
        }

        //System.out.println(queryBuilt.toString());

        doQuery("UNWIND $props AS map CREATE (n:CONTENT) SET n = map", Map.of("props", mapas.toArray()));

        //doQuery("UNWIND $props AS map CREATE (n:USER) SET n = map", Map.of("props", dummyStudents.toArray()));

        doQuery("CREATE (n:INFO {"+
                "MAXlevel: $MAXlevel,"+
                "tags: $tags,"+
                "topics: $topics"+
                "})", Map.of("MAXlevel", niveis, "tags", tags, "topics", topicos1d));

    }

    private static Map<String, Object> mapaCriacaoConteudo(String[] tags, String[][] topicos, int niveis, String[] complexidades)
    {
        Map<String, Object> conteudo = new HashMap<>();

        String name = randomString();
        String descricao = randomString();
        Random generator = new Random();
        int level = generator.nextInt(5) + 1;
        String topico = topicos[level-1][generator.nextInt(3)];
        String complexidade = complexidades[generator.nextInt(4)];
        int taxonomia = generator.nextInt(6);
        boolean exercicio = generator.nextBoolean();
        String[] links = {"https://cdn.discordapp.com/attachments/571157550956019741/808827449109512282/8340.jpg",
                        "https://cdn.discordapp.com/attachments/571157550956019741/808827457775599616/d00428efa0bf27b9edd37eac32dfd2c1.png",
                        "https://cdn.discordapp.com/attachments/571157550956019741/808827462914015242/blog-10.png"};
        String imageLink = links[generator.nextInt(3)];

        contentNames.add(new Content(name, descricao, level, topico, complexidade, exercicio, taxonomia, Arrays.asList(tags), name, imageLink));

        conteudo.put("name", name);
        conteudo.put("descricao", descricao);
        conteudo.put("topic", topico);
        conteudo.put("complexity", complexidade);
        conteudo.put("exercise", exercicio);
        conteudo.put("taxonomy", taxonomia);
        conteudo.put("tags", tags);
        conteudo.put("link", name);
        conteudo.put("level", level);
        conteudo.put("imageLink", imageLink);

        return conteudo;
    }

    private static Map<String, Object> mapaCriacaoUsuario(String[] tags, String[] generos, String[] niveisEdu)
    {
        Map<String, Object> conteudo = new HashMap<>();
        Random generator = new Random();

        String name = randomString();
        String password = randomString();
        String genero = generos[generator.nextInt(generos.length)];
        String nivelEdu = niveisEdu[generator.nextInt(niveisEdu.length)];
        int idade = generator.nextInt(30)+12;
        String[] links = {"https://cdn.discordapp.com/attachments/571157550956019741/800619655366574091/12243585_1694508097447198_1004266710788666891_n.jpg",
                        "https://cdn.discordapp.com/attachments/571157550956019741/800619703089365002/21077295_1119616784841346_734019202998452151_n.jpg",
                        "https://cdn.discordapp.com/attachments/571157550956019741/800619727889629264/1521285067403.jpg"};
        String avatar = links[generator.nextInt(3)];
        

        conteudo.put("name", name);
        conteudo.put("password", password);
        conteudo.put("avatar", avatar);
        conteudo.put("genero", genero);
        conteudo.put("nivelEdu", nivelEdu);
        conteudo.put("idade", idade);
        conteudo.put("preferencias", tags);
        conteudo.put("trilha", returnRandomContents(contentNames.toArray(new Content[contentNames.size()]), Arrays.asList(tags)));

        return conteudo;
    }

    public static String randomString() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
        
        return generatedString;
    }

    private static String[] returnRandomTags(String[] tags)
    {
        List<String> selectedTags = new ArrayList<>();

        int randtags = (int)(Math.random() * 3) + 1; 

        for (int i = 0; i < randtags; i++) {
            String chosenTag = tags[new Random().nextInt(tags.length)];
            if(selectedTags.contains(chosenTag))
            {
                i--;
            }
            else{
                selectedTags.add(chosenTag);
            }
        }
        return selectedTags.toArray(new String[selectedTags.size()]);
    }

    private static String[] returnRandomContents(Content[] contents, List<String> usertags)
    {
        List<String> selectedContent = new ArrayList<>();
        List<Content> filteredContent = new ArrayList<>();
        List<String> topicsDone = new ArrayList<>();

        for (Content content : contents) {
            if(!Collections.disjoint(usertags, content.getTags()))
            filteredContent.add(content);
        }

        int randtags = new Random().nextInt(30); 

        for (int i = 0; i < randtags; i++) {
            Content chosenContent = filteredContent.get(new Random().nextInt(filteredContent.size()));
            if(selectedContent.contains(chosenContent.getName()))
            {
                i--;                
            }
            else {
                if(!topicsDone.contains(chosenContent.getTopic()))
                selectedContent.add(chosenContent.getName());
                topicsDone.add(chosenContent.getTopic());
            }
        }
        return selectedContent.toArray(new String[selectedContent.size()]);
    }
}
