package at.jku.dke.task_app.hierarchical_clustering.clustering;

import java.util.List;

public enum LinkageMethod {
    SINGLE((a, b, dist) -> {
        double min = Double.MAX_VALUE;

        for (int i : a) {
            for (int j : b) {
                if (dist[i][j] < min) {
                    min = dist[i][j];
                }
            }
        }

        return min;
    }),

    COMPLETE((a, b, dist) -> {
        double max = Double.MIN_VALUE;

        for (int i : a) {
            for (int j : b) {
                if (dist[i][j] > max) {
                    max = dist[i][j];
                }
            }
        }

        return max;
    });


    private final Linkage linkage;

    LinkageMethod(Linkage linkage) {
        this.linkage = linkage;
    }

    public double distance(List<Integer> a, List<Integer> b, double[][] dist) {
        return linkage.distance(a, b, dist);
    }


    @FunctionalInterface
    private interface Linkage {
        double distance(List<Integer> a, List<Integer> b, double[][] dist);
    }

}
