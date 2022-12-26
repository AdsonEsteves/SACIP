package sacip.kmeans;

import java.util.List;
import java.util.Map;

import sacip.sti.dataentities.Student;

/**
 * Encapsulates methods to calculates errors between centroid and the cluster
 * members.
 */
public class Errors {

    public static double sse(Map<Centroid, List<Student>> clustered, Distance distance) {
        double sum = 0;
        for (Map.Entry<Centroid, List<Student>> entry : clustered.entrySet()) {
            Centroid centroid = entry.getKey();
            for (Student student : entry.getValue()) {
                double d = distance.calculate(student, centroid);
                sum += Math.pow(d, 2);
            }
        }

        return sum;
    }
}
