package sacip.sti.dataentities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.midas.util.MidasBean;

public class Student extends MidasBean{
    
    //Dados pessoais
    private String name;
    private String password;
    private String avatar;
    private String genero;
    private int idade;
    private String nivelEducacional;
    private List<String> preferencias;
    
    //dados de Uso
    private List<String> trilha;
    private List<String> exerciciosResolvidos;
    private int tempoResolucao;
    private List<String> errosDoEstudante;
    private Map<String, Long> tempoTag;

    public Student() {
        super();
    }

    public Student(String name, String password, String avatar, String genero, int idade, String nivelEducacional, List<String> preferencias) {
        this.name = name;
        this.password = password;
        this.avatar = avatar;
        this.genero = genero;
        this.idade = idade;
        this.nivelEducacional = nivelEducacional;
        this.preferencias = preferencias;
        this.trilha = new ArrayList<>();
        this.exerciciosResolvidos = new ArrayList<>();
        this.tempoResolucao = 0;
        this.errosDoEstudante = new ArrayList<>();
        this.tempoTag = new LinkedHashMap<>();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }     

    public String getGenero() {
        return this.genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getIdade() {
        return this.idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public String getNivelEducacional() {
        return this.nivelEducacional;
    }

    public void setNivelEducacional(String nivelEducacional) {
        this.nivelEducacional = nivelEducacional;
    }

    public List<String> getPreferencias() {
        return this.preferencias;
    }

    public String getPreferenciasAsString() {

        StringBuilder builder = new StringBuilder();

        for (String preferencia : preferencias) {
            builder.append("'"+preferencia+"',");
        }
        
        if(!preferencias.isEmpty())
        builder.deleteCharAt(builder.lastIndexOf(","));
        
        return builder.toString();
    }

    public void setPreferencias(List<String> preferencias) {
        this.preferencias = preferencias;
    }

    public List<String> getTrilha() {
        return this.trilha;
    }

    public void setTrilha(List<String> trilha) {
        this.trilha = trilha;
    }

    public List<String> getExerciciosResolvidos() {
        return this.exerciciosResolvidos;
    }

    public void setExerciciosResolvidos(List<String> exerciciosResolvidos) {
        this.exerciciosResolvidos = exerciciosResolvidos;
    }

    public int getTempoResolucao() {
        return this.tempoResolucao;
    }

    public void setTempoResolucao(int tempoResolucao) {
        this.tempoResolucao = tempoResolucao;
    }

    public List<String> getErrosDoEstudante() {
        return this.errosDoEstudante;
    }

    public void setErrosDoEstudante(List<String> errosDoEstudante) {
        this.errosDoEstudante = errosDoEstudante;
    }

    @Override
    public String toString() {
        return "{" +
            " name:'" + name + "'" +
            ", password:'" + password + "'" +
            ", avatar:'" + avatar + "'" +
            ", genero:'" + genero + "'" +
            ", idade:" + idade + "" +
            ", nivelEducacional:'" + nivelEducacional + "'" +
            ", preferencias:[" + getPreferenciasAsString() + "]" +
            ", trilha:" + trilha + "" +
            ", exerciciosResolvidos:" + exerciciosResolvidos + "" +
            ", tempoResolucao:" + tempoResolucao + "" +
            ", errosDoEstudante:" + errosDoEstudante + "" +
            "}";
    }

}