package at.jku.dke.task_app.hierarchical_clustering.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.services.BaseTaskService;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringTaskRepository;
import at.jku.dke.task_app.hierarchical_clustering.dto.DistanceMetricDto;
import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
import at.jku.dke.task_app.hierarchical_clustering.dto.GenerationStrategyDto;
import at.jku.dke.task_app.hierarchical_clustering.generators.*;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
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

        generateTaskData(task, modifyTaskDto);

        task.setLinkageMethod(modifyTaskDto.additionalData().linkageMethod());
        task.setPointsPerCorrectCluster(BigDecimal.ONE);

        return task;
    }

    @Override
    protected void updateTask(HierarchicalClusteringTask task, ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> modifyTaskDto) {
        if (!modifyTaskDto.taskType().equals("hierarchical-clustering"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task type.");

        task.setLinkageMethod(modifyTaskDto.additionalData().linkageMethod());
        task.setPointsPerCorrectCluster(BigDecimal.ONE);

        if (modifyTaskDto.additionalData().nDataPoints() != task.getDistanceMatrix().getDistances().length) {
            generateTaskData(task, modifyTaskDto);
        } else if (modifyTaskDto.additionalData().generationStrategy() == GenerationStrategyDto.MATRIX) {
            task.setDistanceMatrix(modifyTaskDto.additionalData().distanceMatrix());
        } else {
            task.setDistanceMetric(modifyTaskDto.additionalData().distanceMetric());
        }
    }

    @Override
    protected TaskModificationResponseDto mapToReturnData(HierarchicalClusteringTask task, boolean create) {
        String algorithm = this.messageSource.getMessage("description.agglomerative", null, Locale.ENGLISH);
        String linkageMethod = this.messageSource.getMessage(task.getLinkageMethod().getTranslationKey(), null, Locale.ENGLISH);
        String taskType = null;

        String matrixImg = DistanceMatrixGenerator.getAsImg(task.getDistanceMatrix());
        if (!task.getCoordinateList().isEmpty()) {
            taskType = this.messageSource.getMessage("description.coordinates",
                new Object[]{this.messageSource.getMessage(task.getDistanceMetric().getTranslationKey(), null, Locale.ENGLISH), "coordinate list placeholder"},
                Locale.ENGLISH);
        } else {
            taskType = this.messageSource.getMessage("description.matrix", new Object[]{matrixImg}, Locale.ENGLISH);
        }
        Object[] args = { algorithm, linkageMethod, taskType};

        return new TaskModificationResponseDto(
            this.messageSource.getMessage("defaultTaskDescription", null, Locale.GERMAN),
            this.messageSource.getMessage("description.general", args, Locale.ENGLISH)
        );
    }

    private void generateTaskData(HierarchicalClusteringTask task, ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> modifyTaskDto) {
        if (modifyTaskDto.additionalData().generationStrategy() == GenerationStrategyDto.COORDINATES) {
            CoordinateGenerator coordinateGenerator;
            DistanceMetric distanceMetric;

            switch (modifyTaskDto.additionalData().distanceMetric()) {
                case DistanceMetricDto.EUCLIDEAN:
                    task.setDistanceMetric(DistanceMetricDto.EUCLIDEAN);
                    coordinateGenerator = new EuclideanCoordinateGenerator();
                    distanceMetric = new EuclideanDistance();
                    break;
                case DistanceMetricDto.MANHATTAN:
                    task.setDistanceMetric(DistanceMetricDto.MANHATTAN);
                    coordinateGenerator = new ManhattanCoordinateGenerator();
                    distanceMetric = new ManhattanDistance();
                    break;
                default:
                    coordinateGenerator = null;
                    distanceMetric = null;
                    break;
            }

            if (coordinateGenerator == null) {
                throw new IllegalArgumentException("Chosen distance metric does not exist or is not supported.");
            } else {
                List<HierarchicalClusteringTask.CoordinatePoint> coordinatePoints = coordinateGenerator.generate(modifyTaskDto.additionalData().nDataPoints(), 10);
                task.setCoordinateList(coordinatePoints);
                task.setDistanceMatrix(DistanceMatrixGenerator.getMatrixFromCoordinates(coordinatePoints, distanceMetric));
            }
        } else {
            task.setDistanceMatrix(DistanceMatrixGenerator.getRandomMatrix(modifyTaskDto.additionalData().nDataPoints()));
        }
    }
}
