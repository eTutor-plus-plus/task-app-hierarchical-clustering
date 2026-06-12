package at.jku.dke.task_app.hierarchical_clustering.clustering;

import java.math.BigDecimal;
import java.util.List;

public enum LinkageMethod {
    SINGLE((a, b, dist) -> {
        BigDecimal min = BigDecimal.valueOf(Double.MAX_VALUE);

        for (int i : a) {
            for (int j : b) {
                if (dist[i][j].compareTo(min) < 0) {
                    min = dist[i][j];
                }
            }
        }

        return min;
    }),

    COMPLETE((a, b, dist) -> {
        BigDecimal max = BigDecimal.valueOf(Double.MIN_VALUE);

        for (int i : a) {
            for (int j : b) {
                if (dist[i][j].compareTo(max) > 0) {
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

    public BigDecimal linkage(List<Integer> a, List<Integer> b, BigDecimal[][] dist) {
        return linkage.linkage(a, b, dist);
    }


    @FunctionalInterface
    private interface Linkage {
        BigDecimal linkage(List<Integer> a, List<Integer> b, BigDecimal[][] dist);
    }

}
