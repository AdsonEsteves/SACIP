package sacip.kmeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.Set;

import sacip.sti.dataentities.Student;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Encapsulates an implementation of KMeans clustering algorithm.
 *
 * @author Ali Dehghani
 */
public class KMeans {

    private KMeans() {
        throw new IllegalAccessError("You shouldn't call this constructor");
    }

    /**
     * Will be used to generate random numbers.
     */
    private static final Random random = new Random();

    /**
     * Performs the K-Means clustering algorithm on the given dataset.
     *
     * @param students      The dataset.
     * @param k             Number of Clusters.
     * @param distance      To calculate the distance between two items.
     * @param maxIterations Upper bound for the number of iterations.
     * @return K clusters along with their features.
     */
    public static Map<Centroid, List<Student>> fit(List<Student> students, int k, Distance distance,
            int maxIterations) {
        applyPreconditions(students, k, distance, maxIterations);

        List<Centroid> centroids = randomCentroids(students, k);
        Map<Centroid, List<Student>> clusters = new HashMap<>();
        Map<Centroid, List<Student>> lastState = new HashMap<>();

        // iterate for a pre-defined number of times
        for (int i = 0; i < maxIterations; i++) {
            boolean isLastIteration = i == maxIterations - 1;

            // in each iteration we should find the nearest centroid for each record
            for (Student student : students) {
                Centroid centroid = nearestCentroid(student, centroids, distance);
                assignToCluster(clusters, student, centroid);
            }

            // if the assignment does not change, then the algorithm terminates
            boolean shouldTerminate = isLastIteration || clusters.equals(lastState);
            lastState = clusters;
            if (shouldTerminate) {
                break;
            }

            // at the end of each iteration we should relocate the centroids
            centroids = relocateCentroids(clusters);
            clusters = new HashMap<>();
        }

        return lastState;
    }

    /**
     * Move all cluster centroids to the average of all assigned features.
     *
     * @param clusters The current cluster configuration.
     * @return Collection of new and relocated centroids.
     */
    private static List<Centroid> relocateCentroids(Map<Centroid, List<Student>> clusters) {
        return clusters
                .entrySet()
                .stream()
                .map(e -> average(e.getKey(), e.getValue()))
                .collect(toList());
    }

    /**
     * Moves the given centroid to the average position of all assigned features. If
     * the centroid has no feature in its cluster, then there would be no need for a
     * relocation. Otherwise, for each entry we calculate the average of all records
     * first by summing all the entries and then dividing the final summation value
     * by
     * the number of records.
     *
     * @param centroid The centroid to move.
     * @param records  The assigned features.
     * @return The moved centroid.
     */
    private static Centroid average(Centroid centroid, List<Student> records) {
        // if this cluster is empty, then we shouldn't move the centroid
        if (records == null || records.isEmpty()) {
            return centroid;
        }

        // Since some records don't have all possible attributes, we initialize
        // average coordinates equal to current centroid coordinates
        // Map<String, Double> average = centroid.getCoordinates();
        int averageIdade = centroid.getIdade();
        int averageNivelEducacional = centroid.getNivelEducacional();
        List<String> averagePreferencias = centroid.getPreferencias();
        List<String> averageTrilha = centroid.getTrilha();

        // The average function works correctly if we clear all coordinates
        // corresponding
        // to present record attributes
        // records
        // .stream()
        // .flatMap(e -> e.getIdade()
        // .keySet()
        // .stream())
        // .forEach(k -> average.put(k, 0.0));

        for (Student record : records) {
            int idade = record.getIdade();
            int nivelEducacional = record.getNivelEducacional();

            averageIdade = (averageIdade + idade);
            averageNivelEducacional = (averageNivelEducacional + nivelEducacional);
        }

        EuclideanDistance distance = new EuclideanDistance();
        Student middleStudent = null;
        Double medium = Double.MAX_VALUE;
        for (Student student : records) {
            if (middleStudent == null) {
                middleStudent = student;
                continue;
            }
            List<Double> distances = new ArrayList<>();
            for (Student student2 : records) {
                distances.add(
                        distance.calculate(student, new Centroid(student2.getIdade(), student2.getNivelEducacional(),
                                student2.getPreferencias(), student2.getTrilha())));
            }
            Double[] array = distances.toArray(new Double[distances.size()]);
            Arrays.sort(array);
            int middle = array.length / 2;
            OptionalDouble average = distances.stream().mapToDouble((x) -> x).average();
            if (Math.abs(average.getAsDouble() - array[middle]) < medium) {
                middleStudent = student;
                medium = Math.abs(average.getAsDouble() - array[middle]);
                continue;
            }
        }

        averagePreferencias = middleStudent.getPreferencias();
        averageTrilha = middleStudent.getTrilha();

        return new Centroid(averageIdade / records.size(), averageNivelEducacional / records.size(),
                averagePreferencias, averageTrilha);
    }

    /**
     * Assigns a feature vector to the given centroid. If this is the first
     * assignment for this centroid,
     * first we should create the list.
     *
     * @param clusters The current cluster configuration.
     * @param student  The feature vector.
     * @param centroid The centroid.
     */
    private static void assignToCluster(Map<Centroid, List<Student>> clusters, Student student, Centroid centroid) {
        clusters.compute(centroid, (key, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }

            list.add(student);
            return list;
        });
    }

    /**
     * With the help of the given distance calculator, iterates through centroids
     * and finds the
     * nearest one to the given record.
     *
     * @param student   The feature vector to find a centroid for.
     * @param centroids Collection of all centroids.
     * @param distance  To calculate the distance between two items.
     * @return The nearest centroid to the given feature vector.
     */
    private static Centroid nearestCentroid(Student student, List<Centroid> centroids, Distance distance) {
        double minimumDistance = Double.MAX_VALUE;
        Centroid nearest = null;

        for (Centroid centroid : centroids) {
            double currentDistance = distance.calculate(student, centroid);

            if (currentDistance < minimumDistance) {
                minimumDistance = currentDistance;
                nearest = centroid;
            }
        }

        return nearest;
    }

    /**
     * Generates k random centroids. Before kicking-off the centroid generation
     * process,
     * first we calculate the possible value range for each attribute. Then when
     * we're going to generate the centroids, we generate random coordinates in
     * the [min, max] range for each attribute.
     *
     * @param students The dataset which helps to calculate the [min, max] range for
     *                 each attribute.
     * @param k        Number of clusters.
     * @return Collections of randomly generated centroids.
     */
    private static List<Centroid> randomCentroids(List<Student> students, int k) {
        List<Centroid> centroids = new ArrayList<>();
        Map<String, Double> maxs = new HashMap<>();
        Map<String, Double> mins = new HashMap<>();

        for (Student student : students) {
            int idade = student.getIdade();
            int nivelEducacional = student.getNivelEducacional();
            maxs.put("idade",
                    maxs.containsKey("idade") ? (idade > maxs.get("idade") ? idade : maxs.get("idade")) : idade);
            mins.put("idade",
                    mins.containsKey("idade") ? (idade < mins.get("idade") ? idade : mins.get("idade")) : idade);

            maxs.put("nivelEducacional",
                    maxs.containsKey("nivelEducacional")
                            ? (nivelEducacional > maxs.get("nivelEducacional") ? nivelEducacional
                                    : maxs.get("nivelEducacional"))
                            : nivelEducacional);
            mins.put("nivelEducacional",
                    mins.containsKey("nivelEducacional")
                            ? (nivelEducacional < mins.get("nivelEducacional") ? nivelEducacional
                                    : mins.get("nivelEducacional"))
                            : nivelEducacional);
        }

        Set<String> attributes = maxs.keySet();
        for (int i = 0; i < k; i++) {
            int idade = 0;
            int nivelEducacional = 0;
            for (String attribute : attributes) {
                double max = maxs.get(attribute);
                double min = mins.get(attribute);
                if (attribute.equals("idade"))
                    idade = (int) (random.nextDouble() * (max - min) + min);
                if (attribute.equals("nivelEducacional"))
                    nivelEducacional = (int) (random.nextDouble() * (max - min) + min);
            }
            Student randomStudent = students.get(random.nextInt(students.size()));

            centroids.add(
                    new Centroid(idade, nivelEducacional, randomStudent.getPreferencias(), randomStudent.getTrilha()));
        }

        return centroids;
    }

    private static void applyPreconditions(List<Student> student, int k, Distance distance, int maxIterations) {
        if (student == null || student.isEmpty()) {
            throw new IllegalArgumentException("The dataset can't be empty");
        }

        if (k <= 1) {
            throw new IllegalArgumentException("It doesn't make sense to have less than or equal to 1 cluster");
        }

        if (distance == null) {
            throw new IllegalArgumentException("The distance calculator is required");
        }

        if (maxIterations <= 0) {
            throw new IllegalArgumentException("Max iterations should be a positive number");
        }
    }
}
