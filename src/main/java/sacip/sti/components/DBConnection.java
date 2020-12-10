package sacip.sti.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.util.ElementScanner14;

import com.fasterxml.jackson.databind.JsonNode;

import org.midas.as.AgentServer;
import org.midas.as.agent.templates.Component;
import org.midas.as.agent.templates.ServiceException;

import sacip.sti.dataentities.Content;
import sacip.sti.dataentities.Student;
import sacip.sti.utils.BoltCypherExecutor;
import sacip.sti.utils.CypherExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class DBConnection extends Component {

    private final CypherExecutor cypher;
    private static Logger LOG = LoggerFactory.getLogger(AgentServer.class);

    public DBConnection() {
        super();
        cypher = new BoltCypherExecutor("bolt://localhost:7687", "neo4j", "123456", null);
    }

    @Override
    public void provide(String service, Map in, List out) throws ServiceException {

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

        }
    }

    private Student instanceStudent(Map in)
    {    
        try {
            return new Student((String)in.get("name"),
                                (String)in.get("password"),
                                (String)in.get("avatar"),
                                (String)in.get("genero"),
                                (Integer)in.get("idade"),
                                (String)in.get("nivelEdu"),
                                (List<String>) in.get("preferencias"));
        } catch (Exception e) {            
            LOG.error("Não foi possível instanciar o estudante", e);
            return null;
        }
    }

    private Content instanceContent(Map in)
    {
        try {
            return new Content((String)in.get("name"),
                                ((Long)in.get("level")).intValue(),
                                (String)in.get("topic"),
                                ((Long)in.get("difficulty")).intValue(),
                                (String)in.get("complexity"),
                                (boolean)in.get("exercise"),
                                (String)in.get("taxonomy"),
                                (List<String>) in.get("tags"),
                                (String)in.get("link"));    
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
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                query.append("\nWHERE n.");
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
                else
                {
                    query.append("'"+entry.getValue()+"'");
                }
                query.append("]");
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
            var result = cypher.writequery("MATCH (n:USER { name: $name })"+
                                            "\nSET n."+atributeName+" = coalesce(n."+atributeName+", []) + $newValue"+
                                            "\nRETURN n.name, n."+atributeName
                        , Map.of("name", name, "atribute", atributeName, "newValue", newValue.toString()));
            return result.toString();            
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
                        +"difficulty: $difficulty,"
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
                                "difficulty", content.getDifficulty(),
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
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                query.append("\nWHERE n.");
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
                else
                {
                    query.append("'"+entry.getValue()+"'");
                }
                query.append("]");
            }
            query.append("\nRETURN n");
    
            //realisa a busca
            var result = cypher.readquery(query.toString(), attributes);
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

    public static void main(String[] args) {

        DBConnection conect = new DBConnection();
        // String dado1 = "{'componente':'DEbug', 'timestamp':'1603315704', 'IP':'177.132.153.244'}";
        // String dado2 = "{'componente':'DESU', 'timestamp':'1603315704', 'IP':'177.132.153.244'}";
        // List<String> dadosL = new ArrayList<>();
        // dadosL.add(dado1);
        // dadosL.add(dado2);
        // Map<String, Object> dados = Map.of("Conteudo", dadosL);
        // System.out.println(conect.addClickInformation("Andre", dados));
        conect.showNodes();
        // conect.showNodeRelationships("Andre");
        System.exit(0);
    }

}
