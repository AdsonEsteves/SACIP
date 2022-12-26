package sacip.kmeans;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Encapsulates all coordinates for a particular cluster centroid.
 */
public class Centroid {

    /**
     * The centroid coordinates.
     */

    private int idade;
    private int nivelEducacional;
    private List<String> preferencias;
    private List<String> trilha;

    public Centroid() {
    }

    public Centroid(int idade, int nivelEducacional, List<String> preferencias, List<String> trilha) {
        this.idade = idade;
        this.nivelEducacional = nivelEducacional;
        this.preferencias = preferencias;
        this.trilha = trilha;
    }

    public int getIdade() {
        return this.idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public int getNivelEducacional() {
        return this.nivelEducacional;
    }

    public void setNivelEducacional(int nivelEducacional) {
        this.nivelEducacional = nivelEducacional;
    }

    public List<String> getPreferencias() {
        return this.preferencias;
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

    public Centroid idade(int idade) {
        setIdade(idade);
        return this;
    }

    public Centroid nivelEducacional(int nivelEducacional) {
        setNivelEducacional(nivelEducacional);
        return this;
    }

    public Centroid preferencias(List<String> preferencias) {
        setPreferencias(preferencias);
        return this;
    }

    public Centroid trilha(List<String> trilha) {
        setTrilha(trilha);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Centroid)) {
            return false;
        }
        Centroid centroid = (Centroid) o;
        return idade == centroid.idade && nivelEducacional == centroid.nivelEducacional
                && Objects.equals(preferencias, centroid.preferencias) && Objects.equals(trilha, centroid.trilha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idade, nivelEducacional, preferencias, trilha);
    }

    @Override
    public String toString() {
        return "{" +
                " idade='" + getIdade() + "'" +
                ", nivelEducacional='" + getNivelEducacional() + "'" +
                ", preferencias='" + getPreferencias() + "'" +
                ", trilha='" + getTrilha() + "'" +
                "}";
    }

}
