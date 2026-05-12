package at.jku.dke.task_app.hierarchical_clustering.evaluation.solution;

public final class LinkageMethods {

    private LinkageMethods() {}

    public static final LinkageMethod SINGLE = (a, b, dist) -> {
        double min = Double.MAX_VALUE;
        for (int i : a)
            for (int j : b)
                if (dist[i][j] < min) min = dist[i][j];
        return min;
    };

    public static final LinkageMethod COMPLETE = (a, b, dist) -> {
        double max = Double.MIN_VALUE;
        for (int i : a)
            for (int j : b)
                if (dist[i][j] > max) max = dist[i][j];
        return max;
    };
}
