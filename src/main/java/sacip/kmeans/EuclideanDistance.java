package sacip.kmeans;

import java.util.List;
import java.util.Map;

import sacip.sti.dataentities.Student;

/**
 * Calculates the distance between two items using the Euclidean formula.
 */
public class EuclideanDistance implements Distance {

    @Override
    public double calculate(Student f1, Centroid f2) {
        if (f1 == null || f2 == null) {
            throw new IllegalArgumentException("Feature vectors can't be null");
        }

        int idadev1 = f1.getIdade();
        int idadev2 = f2.getIdade();
        int NEv1 = f1.getNivelEducacional();
        int NEv2 = f2.getNivelEducacional();

        double sum = 0;
        sum += Math.pow(idadev1 - idadev2, 2);
        sum += Math.pow(NEv1 - NEv2, 2);

        List<String> trilhav1 = f1.getTrilha();
        List<String> trilhav2 = f2.getTrilha();
        double semelhancaSum = 0;
        for (int i = 0; i < trilhav1.size(); i++) {
            int j = 0;
            String content = trilhav1.get(i);
            for (j = 0; j < trilhav2.size(); j++) {
                if (trilhav2.get(j).equals(content)) {
                    break;
                }
            }
            int diff = Math.abs(i - j);
            double chance = (100 / trilhav2.size()) * diff;

            semelhancaSum = semelhancaSum + (100 - chance);
        }

        sum += Math.pow(semelhancaSum / trilhav1.size(), 2);

        List<String> prefsv1 = f1.getPreferencias();
        List<String> prefsv2 = f2.getPreferencias();

        int samePref = 0;

        for (String string : prefsv2) {
            if (prefsv1.contains(string)) {
                samePref++;
            }
        }

        sum += Math.pow((samePref * (prefsv1.size() / 100)), 2);

        return Math.sqrt(sum);
    }
}
