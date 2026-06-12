package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.generators.coordinates.CoordinateGenerator;
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMatrixGenerator;
import java.util.Random;

/**
 * Represents a component capable of generating a number of arbitrary data points.
 * <p>
 * Implementations may generate data points in a format containing different information,
 * like their coordinates or interdistances to other data points.
 *
 * @param <T> The return type/format of generated data points.
 * @see CoordinateGenerator
 * @see DistanceMatrixGenerator
 */
public interface DataGenerator<T> {

    /**
     * Generates a number of arbitrary data points without a seed.
     *
     * @param n The number of data points to be generated.
     * @return The generated data points
     */
    default T generate(int n) {
        return generate(n, new Random());
    }

    /**
     * Generates a number of arbitrary data points. A specific
     * {@link Random} instance can be given for seeded generation.
     * <p>
     * Implementations should generate data points using the specified
     * {@link Random} instance for reproducibility of results.
     *
     * @param n      The number of data points to be generated.
     * @param random An instance of class {@link Random} for seeded generation.
     * @return The generated data points.
     */
    T generate(int n, Random random);

}
