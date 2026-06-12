package at.jku.dke.task_app.hierarchical_clustering.generators.coordinates;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.generators.DataGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class CoordinateGenerator implements DataGenerator<List<HierarchicalClusteringTask.CoordinatePoint>> {

    @Override
    public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, Random random) {
        return generate(n, 0, 10, 0, 10, random);
    }

    public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double max) {
        return this.generate(n, 0, max, 0, max);
    }

    public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double minX, double maxX, double minY, double maxY) {
        return this.generate(n, minX, maxX, minY, maxY, new Random());
    }

    public abstract List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double minX, double maxX, double minY, double maxY, Random random);

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
