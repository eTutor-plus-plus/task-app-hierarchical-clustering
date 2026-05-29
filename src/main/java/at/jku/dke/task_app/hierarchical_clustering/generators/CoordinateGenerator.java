package at.jku.dke.task_app.hierarchical_clustering.generators;

import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class CoordinateGenerator implements Generator<List<HierarchicalClusteringTask.CoordinatePoint>> {

    @Override
    public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, Random random) {
        return generate(n, 0, 10, 0, 10, random);
    }

    public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double length) {
        return this.generate(n, length, length);
    }

    public List<HierarchicalClusteringTask.CoordinatePoint> generate(int n, double maxX, double maxY) {
        return this.generate(n, 0, maxX, 0, maxY);
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
