package at.jku.dke.task_app.hierarchical_clustering.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.services.BaseTaskService;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringTaskRepository;
import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
import at.jku.dke.task_app.hierarchical_clustering.generators.DistanceMatrixGenerator;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * This class provides methods for managing {@link HierarchicalClusteringTask}s.
 */
@Service
public class HierarchicalClusteringTaskService extends BaseTaskService<HierarchicalClusteringTask, ModifyHierarchicalClusteringTaskDto> {

    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link HierarchicalClusteringTaskService}.
     *
     * @param repository          The task repository.
     * @param messageSource       The message source.
     */
    public HierarchicalClusteringTaskService(HierarchicalClusteringTaskRepository repository, MessageSource messageSource) {
        super(repository);
        this.messageSource = messageSource;
    }

    @Override
    protected HierarchicalClusteringTask createTask(long id, ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> modifyTaskDto) {
        if (!modifyTaskDto.taskType().equals("hierarchical-clustering"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task type.");

        HierarchicalClusteringTask task = new HierarchicalClusteringTask();
        task.setPointsPerCorrectCluster(BigDecimal.ONE);
        task.setDistanceMatrix(DistanceMatrixGenerator.getRandomMatrix(modifyTaskDto.additionalData().nDataPoints()));

        return task;
    }

    @Override
    protected void updateTask(HierarchicalClusteringTask task, ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> modifyTaskDto) {
        if (!modifyTaskDto.taskType().equals("hierarchical-clustering"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task type.");
        task.setPointsPerCorrectCluster(BigDecimal.ONE);
        if (modifyTaskDto.additionalData().nDataPoints() != task.getDistanceMatrix().getDistances().length) {
            task.setDistanceMatrix(DistanceMatrixGenerator.getRandomMatrix(modifyTaskDto.additionalData().nDataPoints()));
        } else {
            task.setDistanceMatrix(modifyTaskDto.additionalData().distanceMatrix());
        }
    }

    @Override
    protected TaskModificationResponseDto mapToReturnData(HierarchicalClusteringTask task, boolean create) {
        String matrixSvg = DistanceMatrixGenerator.getAsSvg(task.getDistanceMatrix());
        Object[] args = {"agglomerative clustering", "minimum distance (single linkage)", matrixSvg};

        return new TaskModificationResponseDto(
            this.messageSource.getMessage("defaultTaskDescription", null, Locale.GERMAN),
            this.messageSource.getMessage("defaultTaskDescription", args, Locale.ENGLISH)
        );
    }
}
