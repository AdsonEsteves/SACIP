package sacip.sti.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.midas.as.agent.templates.Component;
import org.midas.as.agent.templates.ServiceException;

import sacip.sti.dataentities.Content;
import sacip.sti.dataentities.Student;
import sacip.sti.utils.BoltCypherExecutor;
import sacip.sti.utils.CypherExecutor;

@SuppressWarnings("unchecked")
public class DBConnection extends Component {

    private final CypherExecutor cypher;

    public DBConnection() {
        super();
        cypher = new BoltCypherExecutor("bolt://localhost:7687", "neo4j", "123456", null);
    }

    @Override
    public void provide(String service, Map in, List out) throws ServiceException {
        //TODO
        //FINALIZAR A CRIAÇÃO DOS SERVIÇOS AQUI E NO XML

        if (service.equals("createStudent"))
        {
            try
            {
                String result = createUser(instanceStudent(in));
                out.add(result);
            }
            catch(Exception e)
            {
                out.add("FALHOU criação de estudante"+e);
                System.out.println(e);
            }
        }
        if (service.equals("findStudents")) {
            try
            {
                List<Student> users = getUsers(in);
                out.add(users);
            }
            catch(Exception e)
            {
                out.add("FALHOU busca de estudante"+e);
                System.out.println(e);
            }
        }
        if (service.equals("editStudent")) {
            try
            {
                editUser((String)in.get("name"), (String)in.get("attrName"), (String)in.get("newValue"));
            }
            catch(Exception e)
            {
                out.add("FALHOU edicao de estudante"+e);
                System.out.println(e);
            }
        }
        if (service.equals("deleteStudent")) {
            try 
            {
                deleteUser((String) in.get("name"));                
            }
            catch (Exception e)
            {
                out.add("FALHOU remoção de estudante"+e);
                System.out.println(e);
            }
        }
        if (service.equals("createContent"))
        {
            try
            {
                String result = createContent(instanceContent(in));
                out.add(result);
            }
            catch(Exception e)
            {
                out.add("FALHOU criação de conteudo"+e);
                System.out.println(e);
            }
        }
        if (service.equals("findContents")) {
            try
            {
                List<Content> Contents = getContents(in);
                out.add(Contents);
            }
            catch(Exception e)
            {
                out.add("FALHOU busca de conteudo"+e);
                System.out.println(e);
            }
        }
        if (service.equals("editContent")) {
            try
            {
                editContent((String)in.get("name"), (String)in.get("attrName"), (String)in.get("newValue"));
            }
            catch(Exception e)
            {
                out.add("FALHOU edicao de conteudo"+e);
                System.out.println(e);
            }
        }
        if (service.equals("deleteContent")) {
            try 
            {
                deleteContent((String) in.get("name"));                
            }
            catch (Exception e)
            {
                out.add("FALHOU remoção de conteudo"+e);
                System.out.println(e);
            }
        }
    }

    private Student instanceStudent(Map in)
    {
        return new Student((String)in.get("name"),
                            (String)in.get("password"),
                            (String)in.get("avatar"),
                            (String)in.get("genero"),
                            (int)in.get("idade"),
                            (String)in.get("nivelEdu"),
                            (List<String>) in.get("preferencias"));
    }

    private Content instanceContent(Map in)
    {
        return new Content((String)in.get("name"),
                            (String)in.get("topic"),
                            ((Long)in.get("difficulty")).intValue(),
                            (String)in.get("complexity"),
                            (boolean)in.get("exercise"),
                            (String)in.get("taxonomy"),
                            (List<String>) in.get("tags"),
                            (String)in.get("link"));
    }

    // private void createConstraints() {
    //     var result = cypher.writequery("CREATE CONSTRAINT uniqueuser ON (u:USER) ASSERT u.name IS UNIQUE", Map.of());
    //     System.out.println(result);
    //     result = cypher.writequery("CREATE CONSTRAINT uniquecontent ON (c:CONTENT) ASSERT c.name IS UNIQUE", Map.of());
    //     System.out.println(result);
    // }

    public String createUser(Student student) {
        System.out.println("FAZENDO QUERY");
        var result = cypher.writequery("CREATE (u:USER {" 
                                    + "name: $name," 
                                    + "password: $password," 
                                    + "avatar: $avatar,"
                                    + "genero: $genero," 
                                    + "nivelEdu: $nivelEdu," 
                                    + "idade: $idade,"
                                    + "preferencias: [$preferencias]" 
                                    + "})",
                    Map.of("name", student.getName(),
                            "password", student.getPassword(),
                            "avatar", student.getAvatar(),
                            "genero", student.getGenero(),
                            "nivelEdu", student.getNivelEducacional(),
                            "idade", student.getIdade(),
                            "preferencias", student.getPreferenciasAsString()));
                            System.out.println("TERMINOU QUERY");
        return result.toString();
    }

    private List<Student> getUsers(Map<String, Object> attributes) {
        //Cria query necessária
        StringBuilder query = new StringBuilder("MATCH (n:USER {");
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            query.append(entry.getKey()+": $"+entry.getKey());
            query.append(", ");
        }
        query.delete(query.length()-2, query.length()-1);
        query.append("}) RETURN n");

        //realisa a busca
        var result = cypher.writequery(query.toString(), attributes);
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

    private String editUser(String name, String atributeName, String newValue)
    {
        var result = cypher.writequery("MATCH (n:USER { name: $name })"+
                                        "SET n.$atribute = $newValue"+
                                        "RETURN n.name, n.$atribute"
                    , Map.of("name", name, "atribute", atributeName, "newValue", newValue));
        return result.toString();
    }

    private String deleteUser(String name)
    {
        var result = cypher.writequery("MATCH (n:USER { name: $name })"+
                                       "DELETE n"
                    , Map.of("name", name));
        return result.toString();
    }

    private String createContent(Content content)
    {
        var result = cypher.writequery("CREATE (c:CONTENT "
                                    +"{"
                                    +"name: $name,"
                                    +"topic: $topic,"
                                    +"difficulty: $difficulty,"
                                    +"complexity: $complexity,"
                                    +"exercise: $exercise,"
                                    +"taxonomy: $taxonomy,"
                                    +"tags: [$tags],"
                                    +"link: $link"
                                    +"})",
                        Map.of("name", content.getName(),
                                "topic", content.getTopic(),
                                "difficulty", content.getDifficulty(),
                                "complexity", content.getComplexity(),
                                "exercise", content.getExercise(),
                                "taxonomy", content.getTaxonomy(),
                                "tags", content.getTagsAsString(),
                                "link", content.getLink()
                        ));
        return result.toString();
    }

    private List<Content> getContents(Map<String, Object> attributes)
    {
        //Cria query necessária
        StringBuilder query = new StringBuilder("MATCH (n:CONTENT {");
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            query.append(entry.getKey()+": $"+entry.getKey());
            query.append(", ");
        }
        query.delete(query.length()-2, query.length()-1);
        query.append("}) RETURN n");

        //realisa a busca
        var result = cypher.writequery(query.toString(), attributes);
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
    } 

    private String editContent(String name, String atributeName, String newValue)
    {
        var result = cypher.writequery("MATCH (n:CONTENT { name: $name })"+
                                        "SET n.$atribute = $newValue"+
                                        "RETURN n.name, n.$atribute"
            , Map.of("name", name, "atribute", atributeName, "newValue", newValue));
        return result.toString();
    }

    private String deleteContent(String name)
    {
        var result = cypher.writequery("MATCH (n:CONTENT { name: $name })"+
                                       "DELETE n"
                    , Map.of("name", name));
        return result.toString();
    }

    private void showNodes()
    {
        var result = cypher.readquery("MATCH (n) RETURN n", Map.of());
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
        // Student student1 = new Student("Adson", "", "animegirl", "homem", 24, "graduação", new ArrayList<>());
        // Student student2 = new Student("Saber", "", "bruhh", "homem", 24, "graduação", new ArrayList<>());
        // Student student3 = new Student("Aluizio", "", "xx", "homem", 24, "graduação", new ArrayList<>());
        // Student student4 = new Student("Rodrigo", "", "aa", "homem", 24, "graduação", new ArrayList<>());
        // Student student5 = new Student("Andre", "", "dd", "homem", 24, "graduação", new ArrayList<>());
        // Student student6 = new Student("Alisson", "", "ff", "homem", 24, "graduação", new ArrayList<>());
        // List<String> preferencias = new ArrayList<>();
        // preferencias.add("filmes");
        // preferencias.add("musica");
        // Student student7 = new Student("Yukino", "3333", "oregairu", "mulher", 24, "mestrado", preferencias);

        // conect.createUser(student1);
        // conect.createUser(student2);
        // conect.createUser(student3);
        // conect.createUser(student4);
        // conect.createUser(student5);
        // conect.createUser(student6);
        // conect.createUser(student7);

        conect.showNodes();
        // List<Student> found = conect.getUsers(Map.of("idade", 24, "nivelEdu", "graduação"));
        // if(found!=null)
        // System.out.println("encontrou "+found.toString());
        // else
        // System.out.println("naoencontrou");
        // conect.resetDB();

        // conect.printPeople("A");
        System.exit(0);
    }

}
