package at.jku.dke.task_app.hierarchical_clustering.generators.coordinates;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.generators.DataGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a component able to generate a list of data points with their
 * coordinates.
 *
 * @see EuclideanCoordinateGenerator
 * @see ManhattanCoordinateGenerator
 */
public abstract class CoordinateGenerator implements DataGenerator<List<HierarchicalClusteringTask.CoordinatePoint>> {

    @Override
    public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, Random random) {
        return generate(n, 0, 10, 0, 10, random);
    }

    /**
     * Generates a number of arbitrary data points in form of a list of coordinates within
     * bounds 0-{@code max}.
     *
     * @param n   The number of data points to be generated.
     * @param max The maximum bound of both axes.
     * @return The list of generated coordinates.
     */
    public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double max) {
        return this.generate(n, 0, max, 0, max);
    }

    /**
     * Generates a number of arbitrary data points in form of a list of coordinates. Coordinates are
     * generated within the given bounds/axis limits.
     *
     * @param n    The number of data points to be generated.
     * @param minX The minimum bound of the x-axis.
     * @param maxX The maximum bound of the x-axis.
     * @param minY The minimum bound of the y-axis.
     * @param maxY The maximum bound of the y-axis.
     * @return The list of generated coordinates.
     */
    public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double minX, double maxX, double minY, double maxY) {
        return this.generate(n, minX, maxX, minY, maxY, new Random());
    }

    /**
     * Generates a number of arbitrary data points in form of a list of coordinates. Coordinates are
     * generated within the given bounds/axis limits. A specific Random instance can be given for
     * seeded generation.
     *
     * @param n      The number of data points to be generated.
     * @param minX   The minimum bound of the x-axis.
     * @param maxX   The maximum bound of the x-axis.
     * @param minY   The minimum bound of the y-axis.
     * @param maxY   The maximum bound of the y-axis.
     * @param random An instance of class Random for seeded generation.
     * @return The list of generated coordinates.
     */
    public abstract List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double minX, double maxX, double minY, double maxY, Random random);

    /**
     * Helper method for coordinate generators that builds a pool of potential coordinate pairs
     * within the axis limits.
     * <p>
     * This method adds candidates as integers to ensure a certain number of decimal places
     * dictated by the step values given as parameters. If for example the step values are
     * actual limits * 10, then the resulting coordinates will have one decimal place, if steps
     * are limits * 100, then two decimal places, etc.
     *
     * @param xMinStep The minimum bound of the x-axis as integer step.
     * @param xMaxStep The maximum bound of the x-axis as integer step.
     * @param yMinStep The minimum bound of the y-axis as integer step.
     * @param yMaxStep The maximum bound of the x-axis as integer step.
     * @return The list of coordinate pair candidates for generation.
     */
    protected static List<int[]> buildGenericCandidatePool(int xMinStep, int xMaxStep, int yMinStep, int yMaxStep) {
        List<int[]> pool = new ArrayList<>();

        for (int xi = xMinStep; xi <= xMaxStep; xi++) {
            for (int yi = yMinStep; yi <= yMaxStep; yi++) {
                pool.add(new int[]{ xi, yi });
            }
        }

        return pool;
    }

}
