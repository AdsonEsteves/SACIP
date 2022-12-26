package sacip.kmeans;

import java.util.Map;

import sacip.sti.dataentities.Student;

/**
 * Defines a contract to calculate distance between two feature vectors. The
 * less the
 * calculated distance, the more two items are similar to each other.
 */
public interface Distance {

    /**
     * Calculates the distance between two feature vectors.
     *
     * @param f1 The first set of features.
     * @param f2 The second set of features.
     * @return Calculated distance.
     * @throws IllegalArgumentException If the given feature vectors are invalid.
     */
    double calculate(Student f1, Centroid f2);
}
