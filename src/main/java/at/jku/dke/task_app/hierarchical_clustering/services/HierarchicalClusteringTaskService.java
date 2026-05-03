package at.jku.dke.task_app.hierarchical_clustering.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.services.BaseTaskService;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringTaskRepository;
import at.jku.dke.task_app.hierarchical_clustering.dto.AssignmentTypeDto;
import at.jku.dke.task_app.hierarchical_clustering.dto.DistanceMetricDto;
import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
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

        validateMaxPoints(modifyTaskDto);

        HierarchicalClusteringTask task = new HierarchicalClusteringTask();

        generateTaskData(task, modifyTaskDto);

        task.setLinkageMethod(modifyTaskDto.additionalData().linkageMethod());
        task.setPointsPerCorrectCluster(modifyTaskDto.additionalData().pointsPerCorrectCluster());
        task.setWrongOrderPenalty(modifyTaskDto.additionalData().wrongOrderPenalty());

        return task;
    }

    @Override
    protected void updateTask(HierarchicalClusteringTask task, ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> modifyTaskDto) {
        if (!modifyTaskDto.taskType().equals("hierarchical-clustering"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task type.");

        validateMaxPoints(modifyTaskDto);

        task.setLinkageMethod(modifyTaskDto.additionalData().linkageMethod());
        task.setPointsPerCorrectCluster(modifyTaskDto.additionalData().pointsPerCorrectCluster());
        task.setWrongOrderPenalty(modifyTaskDto.additionalData().wrongOrderPenalty());

        if (modifyTaskDto.additionalData().nDataPoints() != task.getDistanceMatrix().getDistances().length ||
            (task.getCoordinateList() == null && modifyTaskDto.additionalData().assignmentType() == AssignmentTypeDto.COORDINATES) ||
            (task.getCoordinateList() != null && modifyTaskDto.additionalData().assignmentType() == AssignmentTypeDto.MATRIX)) {
            generateTaskData(task, modifyTaskDto);
        } else if (modifyTaskDto.additionalData().assignmentType() == AssignmentTypeDto.MATRIX) {
            task.setDistanceMatrix(modifyTaskDto.additionalData().distanceMatrix());
        } else {
            task.setDistanceMetric(modifyTaskDto.additionalData().distanceMetric());
        }
    }

    @Override
    protected TaskModificationResponseDto mapToReturnData(HierarchicalClusteringTask task, boolean create) {
        String algorithm = this.messageSource.getMessage("description.agglomerative", null, Locale.ENGLISH);
        String linkageMethod = this.messageSource.getMessage(task.getLinkageMethod().getTranslationKey(), null, Locale.ENGLISH);
        String taskType;

        String matrixImg = DistanceMatrixGenerator.getAsImg(task.getDistanceMatrix());

        if (task.getCoordinateList() != null) {
            String coordinatesTableHtml = getCoordinatesAsHtmlTable(task.getCoordinateList());
            taskType = this.messageSource.getMessage(
                "description.coordinates",
                new Object[]{
                    this.messageSource.getMessage(task.getDistanceMetric().getTranslationKey(), null, Locale.ENGLISH),
                    coordinatesTableHtml},
                Locale.ENGLISH);
        } else {
            taskType = this.messageSource.getMessage("description.matrix", new Object[]{matrixImg}, Locale.ENGLISH);
        }

        Object[] args = { algorithm, linkageMethod, taskType };

        return new TaskModificationResponseDto(
            this.messageSource.getMessage("defaultTaskDescription", null, Locale.GERMAN),
            this.messageSource.getMessage("description.general", args, Locale.ENGLISH)
        );
    }

    private void generateTaskData(HierarchicalClusteringTask task, ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> modifyTaskDto) {
        if (modifyTaskDto.additionalData().assignmentType() == AssignmentTypeDto.COORDINATES) {
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
            task.setCoordinateList(null);
            task.setDistanceMetric(null);
            task.setDistanceMatrix(DistanceMatrixGenerator.getRandomMatrix(modifyTaskDto.additionalData().nDataPoints()));
        }
    }

    private void validateMaxPoints(ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> modifyTaskDto) {
        BigDecimal expectedMaxPoints = modifyTaskDto.additionalData().pointsPerCorrectCluster().multiply(
            BigDecimal.valueOf(modifyTaskDto.additionalData().nDataPoints() - 1));

        if (expectedMaxPoints.compareTo(modifyTaskDto.maxPoints()) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Max. points (currently " + modifyTaskDto.maxPoints().doubleValue() +
                    ") do not correspond to expected value (" + expectedMaxPoints.doubleValue() + ".");
        }
    }


    private String getCoordinatesAsHtmlTable(List<HierarchicalClusteringTask.CoordinatePoint> points) {
        StringBuilder html = new StringBuilder();

        html.append("<table>");

        html.append("<tr>");
        html.append("<th></th>");
        html.append("<th>x</th>");
        html.append("<th>y</th>");
        html.append("</tr>");

        for (HierarchicalClusteringTask.CoordinatePoint p : points) {
            html.append("<tr>");
            html.append("<td>").append(p.getLabel()).append("</td>");
            html.append("<td>").append(p.getX()).append("</td>");
            html.append("<td>").append(p.getY()).append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");

        return html.toString();
    }
}
