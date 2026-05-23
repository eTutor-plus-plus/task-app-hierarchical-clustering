package at.jku.dke.task_app.hierarchical_clustering.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.services.BaseTaskService;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringCluster;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringClusterRepository;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringMergeRepository;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringTaskRepository;
import at.jku.dke.task_app.hierarchical_clustering.dto.AssignmentTypeDto;
import at.jku.dke.task_app.hierarchical_clustering.dto.DistanceMetricDto;
import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
import at.jku.dke.task_app.hierarchical_clustering.evaluation.solution.LinkageMethod;
import at.jku.dke.task_app.hierarchical_clustering.evaluation.solution.LinkageMethods;
import at.jku.dke.task_app.hierarchical_clustering.evaluation.solution.NaiveAgglomerativeClusteringAlgorithm;
import at.jku.dke.task_app.hierarchical_clustering.generators.*;
import at.jku.dke.task_app.hierarchical_clustering.generators.dendrogram.DendrogramModelBuilder;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

/**
 * This class provides methods for managing {@link HierarchicalClusteringTask}s.
 */
@Service
public class HierarchicalClusteringTaskService extends BaseTaskService<HierarchicalClusteringTask, ModifyHierarchicalClusteringTaskDto> {

    private final MessageSource messageSource;

    private final HierarchicalClusteringMergeRepository mergeRepository;
    private final HierarchicalClusteringClusterRepository clusterRepository;

    /**
     * Creates a new instance of class {@link HierarchicalClusteringTaskService}.
     *
     * @param repository          The task repository.
     * @param messageSource       The message source.
     */
    public HierarchicalClusteringTaskService(HierarchicalClusteringTaskRepository repository, MessageSource messageSource, HierarchicalClusteringMergeRepository mergeRepository, HierarchicalClusteringClusterRepository clusterRepository) {
        super(repository);
        this.messageSource = messageSource;
        this.mergeRepository = mergeRepository;
        this.clusterRepository = clusterRepository;
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

        createSolution(task);

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
            (task.getCoordinateList() != null && modifyTaskDto.additionalData().assignmentType() == AssignmentTypeDto.MATRIX) ||
            task.getDistanceMetric() != modifyTaskDto.additionalData().distanceMetric() ||
            (task.getCoordinateList() != null && (modifyTaskDto.additionalData().lengthX() != task.getCoordinateList().getLengthX() ||
            modifyTaskDto.additionalData().lengthY() != task.getCoordinateList().getLengthY()))) {
            generateTaskData(task, modifyTaskDto);
        } else if (modifyTaskDto.additionalData().assignmentType() == AssignmentTypeDto.MATRIX) {
            task.setDistanceMatrix(modifyTaskDto.additionalData().distanceMatrix());
        } else {
            DistanceMetric metric = switch (modifyTaskDto.additionalData().distanceMetric()) {
                case EUCLIDEAN -> new EuclideanDistance();
                case MANHATTAN -> new ManhattanDistance();
            };
            task.setDistanceMetric(modifyTaskDto.additionalData().distanceMetric());
            task.setCoordinateList(new HierarchicalClusteringTask.CoordinateList(
                modifyTaskDto.additionalData().lengthX(),
                modifyTaskDto.additionalData().lengthY(),
                modifyTaskDto.additionalData().coordinatePoints()));
            task.setDistanceMatrix(DistanceMatrixGenerator.getMatrixFromCoordinates(modifyTaskDto.additionalData().coordinatePoints(), metric));
        }

        // delete all old clusters and merges for this task (as they are not needed anymore) and compute and persist the new solution
        List<HierarchicalClusteringMerge> oldSolution = task.getSolutionMergeHistory();
        Map<List<String>, HierarchicalClusteringCluster> clusterLookup = new HashMap<>();

        for (HierarchicalClusteringMerge merge : oldSolution) {
            HierarchicalClusteringCluster clusterLeft = merge.getClusterLeft();
            clusterLookup.putIfAbsent(clusterLeft.getDataPoints(), clusterLeft);

            HierarchicalClusteringCluster clusterRight = merge.getClusterRight();
            clusterLookup.putIfAbsent(clusterRight.getDataPoints(), clusterRight);

            HierarchicalClusteringCluster result = merge.getResult();
            clusterLookup.putIfAbsent(result.getDataPoints(), result);
        }

        task.getSolutionMergeHistory().clear();
        mergeRepository.flush();
        clusterRepository.deleteAll(clusterLookup.values());
        createSolution(task);
    }

    @Override
    protected TaskModificationResponseDto mapToReturnData(HierarchicalClusteringTask task, boolean create) {
        String algorithm = this.messageSource.getMessage("description.agglomerative", null, Locale.ENGLISH);
        String linkageMethod = this.messageSource.getMessage(task.getLinkageMethod().getTranslationKey(), null, Locale.ENGLISH);
        String taskType;
        String ordering;

        if (task.getCoordinateList() != null) {
            String coordinatesTableHtml = getCoordinatesAsHtmlTable(task.getCoordinateList().getCoordinateList());
            taskType = this.messageSource.getMessage(
                "description.coordinates",
                new Object[]{
                    this.messageSource.getMessage(task.getDistanceMetric().getTranslationKey(), null, Locale.ENGLISH),
                    coordinatesTableHtml},
                Locale.ENGLISH);
        } else {
            String matrixHtml = getMatrixAsHtmlTable(task.getDistanceMatrix());
            taskType = this.messageSource.getMessage("description.matrix", new Object[]{matrixHtml}, Locale.ENGLISH);
        }

        ordering = task.getWrongOrderPenalty() != null && task.getWrongOrderPenalty().compareTo(BigDecimal.ZERO) > 0 ?
            this.messageSource.getMessage("description.ordering", null, Locale.ENGLISH) :
            "";

        Object[] args = { algorithm, linkageMethod, taskType, ordering };

        return new TaskModificationResponseDto(
            this.messageSource.getMessage("defaultTaskDescription", null, Locale.GERMAN),
            this.messageSource.getMessage("description.general", args, Locale.ENGLISH)
        );
    }

    private void validateMaxPoints(ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> modifyTaskDto) {
        int nSolutionSteps = modifyTaskDto.additionalData().nDataPoints() - 1;
        BigDecimal expectedMaxPoints = modifyTaskDto.additionalData().pointsPerCorrectCluster().multiply(BigDecimal.valueOf(nSolutionSteps));
        BigDecimal actualMaxPoints = modifyTaskDto.maxPoints();

        if (expectedMaxPoints.compareTo(actualMaxPoints) != 0) {
            throw new IllegalArgumentException("Invalid max. points: need to be set to {0} (currently set value: {1}).");
        }
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
                int dimensionX = modifyTaskDto.additionalData().lengthX();
                int dimensionY = modifyTaskDto.additionalData().lengthY();

                List<HierarchicalClusteringTask.CoordinatePoint> coordinatePoints = coordinateGenerator.generate(
                    modifyTaskDto.additionalData().nDataPoints(), dimensionX, dimensionY);
                task.setCoordinateList(new HierarchicalClusteringTask.CoordinateList(dimensionX, dimensionY, coordinatePoints));
                task.setDistanceMatrix(DistanceMatrixGenerator.getMatrixFromCoordinates(coordinatePoints, distanceMetric));
            }
        } else {
            task.setCoordinateList(null);
            task.setDistanceMetric(null);
            task.setDistanceMatrix(DistanceMatrixGenerator.getRandomMatrix(modifyTaskDto.additionalData().nDataPoints()));
        }
    }

    private void createSolution(HierarchicalClusteringTask task) {
        LinkageMethod linkageMethod = switch (task.getLinkageMethod()) {
            case SINGLE -> LinkageMethods.SINGLE;
            case COMPLETE -> LinkageMethods.COMPLETE;
        };

        List<HierarchicalClusteringMerge> solutionMergeHistory = new NaiveAgglomerativeClusteringAlgorithm(linkageMethod)
            .cluster(task.getDistanceMatrix());

        // persist clusters and merges
        for (HierarchicalClusteringMerge merge : solutionMergeHistory) {
            HierarchicalClusteringCluster clusterLeft = merge.getClusterLeft();
            if (clusterLeft != null && clusterLeft.getDataPoints().size() == 1) {
                clusterRepository.save(clusterLeft);
            }

            HierarchicalClusteringCluster clusterRight = merge.getClusterRight();
            if (clusterRight != null && clusterRight.getDataPoints().size() == 1) {
                clusterRepository.save(clusterRight);
            }

            clusterRepository.save(merge.getResult());

            task.getSolutionMergeHistory().add(merge);
            merge.setTask(task);
        }

        task.setDendrogramModel(new DendrogramModelBuilder().build(solutionMergeHistory));
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
            html.append("<th>").append(p.getLabel()).append("</th>");
            html.append("<td>").append(p.getX()).append("</td>");
            html.append("<td>").append(p.getY()).append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");

        return html.toString();
    }

    private String getMatrixAsHtmlTable(HierarchicalClusteringTask.DistanceMatrix matrix) {
        StringBuilder html = new StringBuilder();
        List<String> labels = matrix.getLabels();
        double[][] distances = matrix.getDistances();

        html.append("<table>");

        html.append("<tr>");
        html.append("<th></th>");

        for (String label : labels) {
            html.append("<th>").append(label).append("</th>");
        }

        html.append("</tr>");

        for (int i = 0; i < distances.length; i++) {
            html.append("<tr>");
            html.append("<th>").append(labels.get(i)).append("</th>");

            for (int j = 0; j < distances[i].length && i >= j; j++) {
                html.append("<td>").append(distances[i][j]).append("</td>");
            }

            html.append("</tr>");
        }

        html.append("</table>");

        return html.toString();
    }
}
