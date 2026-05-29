package at.jku.dke.task_app.hierarchical_clustering.generators;

import java.util.Random;

public interface Generator<T> {

    default T generate(int n) {
        return generate(n, new Random());
    }

    T generate(int n, Random random);

}
