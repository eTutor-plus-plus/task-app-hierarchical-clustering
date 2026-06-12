package at.jku.dke.task_app.hierarchical_clustering.data.repositories;

import at.jku.dke.etutor.task_app.data.repositories.TaskRepository;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * Repository for entity {@link HierarchicalClusteringTask}.
 */
public interface HierarchicalClusteringTaskRepository extends TaskRepository<HierarchicalClusteringTask> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"solutionMergeHistory", "solutionMergeHistory.result"})
    Optional<HierarchicalClusteringTask> findById(@NonNull Long id);

}
