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
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMetric;
import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
import at.jku.dke.task_app.hierarchical_clustering.clustering.NaiveAgglomerativeClusteringAlgorithm;
import at.jku.dke.task_app.hierarchical_clustering.dendrogram.DendrogramModelBuilder;
import at.jku.dke.task_app.hierarchical_clustering.generators.coordinates.CoordinateGenerator;
import at.jku.dke.task_app.hierarchical_clustering.generators.coordinates.EuclideanCoordinateGenerator;
import at.jku.dke.task_app.hierarchical_clustering.generators.coordinates.ManhattanCoordinateGenerator;
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMatrixGenerator;
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

        validateAdditional(modifyTaskDto);

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

        validateAdditional(modifyTaskDto);

        task.setLinkageMethod(modifyTaskDto.additionalData().linkageMethod());
        task.setPointsPerCorrectCluster(modifyTaskDto.additionalData().pointsPerCorrectCluster());
        task.setWrongOrderPenalty(modifyTaskDto.additionalData().wrongOrderPenalty());

        if (needsRegeneration(task, modifyTaskDto.additionalData())) {
            generateTaskData(task, modifyTaskDto);
        } else if (modifyTaskDto.additionalData().assignmentType() == AssignmentTypeDto.COORDINATES) {
            DistanceMetric metric = modifyTaskDto.additionalData().distanceMetric();

            HierarchicalClusteringTask.DistanceMatrix distanceMatrix = new DistanceMatrixGenerator().calculateMatrixFromCoordinates(modifyTaskDto.additionalData().coordinateSystem().getCoordinateList(), metric);
            task.setCoordinateSystem(modifyTaskDto.additionalData().coordinateSystem());
            task.setDistanceMetric(modifyTaskDto.additionalData().distanceMetric());
            task.setDistanceMatrix(distanceMatrix);
        } else if (modifyTaskDto.additionalData().assignmentType() == AssignmentTypeDto.MATRIX) {
            task.setDistanceMatrix(modifyTaskDto.additionalData().distanceMatrix());
        }

        // delete all old clusters and merges for this task (as they are not needed anymore) and compute and persist the new clustering
        List<HierarchicalClusteringMerge> oldSolution = task.getSolutionMergeHistory();
        Map<List<String>, HierarchicalClusteringCluster> clusterLookup = new HashMap<>();

        for (HierarchicalClusteringMerge merge : oldSolution) {
            HierarchicalClusteringCluster sourceCluster1 = merge.getSourceCluster1();
            clusterLookup.putIfAbsent(sourceCluster1.getDataPoints(), sourceCluster1);

            HierarchicalClusteringCluster sourceCluster2 = merge.getSourceCluster2();
            clusterLookup.putIfAbsent(sourceCluster2.getDataPoints(), sourceCluster2);

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
        String descriptionDe = getTaskDescription(task, Locale.GERMAN);
        String descriptionEn = getTaskDescription(task, Locale.ENGLISH);

        return new TaskModificationResponseDto(descriptionDe, descriptionEn);
    }

    /**
     * Helper method to determine whether a task needs to be regenerated on update.
     * <p>
     * Returns {@code true} in any of the following cases:
     *
     * <ul>
     *     <li>Assignment type has changed
     *     <li>Distance metric has changed (because coordinates are generated
     *         differently depending on the metric to fulfill constraints and therefore need to ge regenerated)
     *     <li>Number of data points has changed
     *     <li>The axes of the coordinate system have changed
     * </ul>
     *
     * If this method returns {@code true}, the coordinate list and/or distance matrix
     * should be regenerated to reflect the changes made by users.
     *
     * @param task The task which should be updated.
     * @param data The data to update the task with.
     * @return {@code true} if any changes have been made that warrant regeneration of coordinates/distance matrix.
     */
    private boolean needsRegeneration(HierarchicalClusteringTask task, ModifyHierarchicalClusteringTaskDto data) {
        boolean isDifferentN = data.nDataPoints() != task.getDistanceMatrix().getDistances().length;
        boolean hasAssignmentTypeChangedToCoordinates = task.getCoordinateSystem() == null && data.assignmentType() == AssignmentTypeDto.COORDINATES;
        boolean hasAssignmentTypeChangedToMatrix = task.getCoordinateSystem() != null && data.assignmentType() == AssignmentTypeDto.MATRIX;
        boolean hasDistanceMetricChanged = data.assignmentType() == AssignmentTypeDto.COORDINATES && task.getDistanceMetric() != data.distanceMetric();
        boolean haveAxisLengthsChanged = data.assignmentType() == AssignmentTypeDto.COORDINATES && task.getCoordinateSystem() != null &&
            (!Objects.equals(data.coordinateSystem().getMinX(), task.getCoordinateSystem().getMinX()) ||
                !Objects.equals(data.coordinateSystem().getMaxX(), task.getCoordinateSystem().getMaxX()) ||
                !Objects.equals(data.coordinateSystem().getMinY(), task.getCoordinateSystem().getMinY()) ||
                !Objects.equals(data.coordinateSystem().getMaxY(), task.getCoordinateSystem().getMaxY()));

        return isDifferentN || hasAssignmentTypeChangedToCoordinates || hasAssignmentTypeChangedToMatrix || hasDistanceMetricChanged || haveAxisLengthsChanged;
    }

    /**
     * Helper method to build the default task description for the given locale.
     *
     * @param task   The task for which to build the description.
     * @param locale The locale/language for the description.
     * @return The default task description in the specified locale/language.
     */
    private String getTaskDescription(HierarchicalClusteringTask task, Locale locale) {
        String algorithm = this.messageSource.getMessage("description.agglomerative", null, locale);
        String linkageMethod = this.messageSource.getMessage("description." + task.getLinkageMethod().toString().toLowerCase(), null, locale);
        String taskType;
        String ordering;

        if (task.getCoordinateSystem() != null) {
            String coordinatesTableHtml = getCoordinatesAsHtmlTable(task.getCoordinateSystem().getCoordinateList());
            taskType = this.messageSource.getMessage(
                "description.coordinates",
                new Object[]{
                    this.messageSource.getMessage("description." + task.getDistanceMetric().toString().toLowerCase(), null, locale),
                    coordinatesTableHtml},
                locale);
        } else {
            String matrixHtml = getMatrixAsHtmlTable(task.getDistanceMatrix());
            taskType = this.messageSource.getMessage("description.matrix", new Object[]{matrixHtml}, locale);
        }

        ordering = task.getWrongOrderPenalty() != null && task.getWrongOrderPenalty().compareTo(BigDecimal.ZERO) > 0 ?
            this.messageSource.getMessage("description.ordering", null, locale) :
            "";

        Object[] args = { algorithm, linkageMethod, taskType, ordering };

        return this.messageSource.getMessage("description.general", args, locale);
    }

    /**
     * Additional validation on top of custom validators for validating certain inputs.
     * <p>
     * Checks whether the number of data points is at least 2 (because tasks are pointless
     * otherwise), as well as whether the set max points correspond to the expected value
     * (evaluation would break otherwise).
     *
     * @param modifyTaskDto The new task data to be validated.
     */
    private void validateAdditional(ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> modifyTaskDto) {
        int n = modifyTaskDto.additionalData().nDataPoints();

        if (n <= 1) {
            // language can be changed (e.g. to Locale.getDefault()) if desired
            throw new IllegalArgumentException(this.messageSource.getMessage("validation.nDataPoints", null, Locale.ENGLISH));
        }

        int nSolutionSteps = n - 1;
        BigDecimal expectedMaxPoints = modifyTaskDto.additionalData().pointsPerCorrectCluster().multiply(BigDecimal.valueOf(nSolutionSteps));
        BigDecimal actualMaxPoints = modifyTaskDto.maxPoints();

        if (expectedMaxPoints.compareTo(actualMaxPoints) != 0) {
            // language can be changed (e.g. to Locale.getDefault()) if desired
            throw new IllegalArgumentException(this.messageSource.getMessage("validation.maxPoints", new Object[]{expectedMaxPoints, actualMaxPoints}, Locale.ENGLISH));
        }
    }

    /**
     * Helper method to generate task data when creating or updating tasks.
     * <p>
     * Generates either a list of coordinates or a distance matrix, depending on
     * the assignment type. If coordinates are generated, a corresponding distance
     * matrix will be calculated from those coordinates as well.
     *
     * @param task          The task for which to generate data for.
     * @param modifyTaskDto The new task data used to determine
     *                      what needs to be generated/calculated and how.
     */
    private void generateTaskData(HierarchicalClusteringTask task, ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> modifyTaskDto) {
        if (modifyTaskDto.additionalData().assignmentType() == AssignmentTypeDto.COORDINATES) {
            CoordinateGenerator coordinateGenerator;
            DistanceMetric distanceMetric = modifyTaskDto.additionalData().distanceMetric();
            task.setDistanceMetric(distanceMetric);

            coordinateGenerator = switch (distanceMetric) {
                case DistanceMetric.EUCLIDEAN -> new EuclideanCoordinateGenerator();
                case DistanceMetric.MANHATTAN -> new ManhattanCoordinateGenerator();
                default -> null;
            };

            if (coordinateGenerator == null) {
                throw new IllegalArgumentException("Chosen distance metric does not exist or is not supported.");
            } else {
                HierarchicalClusteringTask.CoordinateSystem coordinateSystem = modifyTaskDto.additionalData().coordinateSystem();
                int minX = coordinateSystem.getMinX();
                int maxX = coordinateSystem.getMaxX();
                int minY = coordinateSystem.getMinY();
                int maxY = coordinateSystem.getMaxY();

                List<HierarchicalClusteringTask.CoordinatePoint> coordinateList =
                    coordinateGenerator.generate(modifyTaskDto.additionalData().nDataPoints(), minX, maxX, minY, maxY);
                coordinateSystem.setCoordinateList(coordinateList);
                task.setCoordinateSystem(coordinateSystem);
                task.setDistanceMatrix(new DistanceMatrixGenerator().calculateMatrixFromCoordinates(coordinateList, distanceMetric));
            }
        } else {
            task.setCoordinateSystem(null);
            task.setDistanceMetric(null);
            task.setDistanceMatrix(new DistanceMatrixGenerator().generate(modifyTaskDto.additionalData().nDataPoints()));
        }
    }

    /**
     * Helper method to compute and persist a solution and the corresponding dendrogram
     * for the given task.
     *
     * @param task The task for which to compute a solution for.
     */
    private void createSolution(HierarchicalClusteringTask task) {
        List<HierarchicalClusteringMerge> solutionMergeHistory = new NaiveAgglomerativeClusteringAlgorithm(task.getLinkageMethod()).cluster(task.getDistanceMatrix());

        // persist clusters and merges
        for (HierarchicalClusteringMerge merge : solutionMergeHistory) {
            HierarchicalClusteringCluster sourceCluster1 = merge.getSourceCluster1();
            if (sourceCluster1 != null && sourceCluster1.getDataPoints().size() == 1) {
                clusterRepository.save(sourceCluster1);
            }

            HierarchicalClusteringCluster sourceCluster2 = merge.getSourceCluster2();
            if (sourceCluster2 != null && sourceCluster2.getDataPoints().size() == 1) {
                clusterRepository.save(sourceCluster2);
            }

            clusterRepository.save(merge.getResult());

            task.getSolutionMergeHistory().add(merge);
            merge.setTask(task);
        }


        task.setDendrogramModel(new DendrogramModelBuilder().build(solutionMergeHistory));
    }


    /**
     * Helper method to format the list of coordinates into an HTML table for
     * displaying in the task description.
     *
     * @param points The list of coordinates.
     * @return The coordinate list formatted as an HTML table.
     */
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

    /**
     * Helper method to formate the distance matrix into an HTML table for
     * displaying in the task description.
     *
     * @param matrix The distance matrix.
     * @return The matrix formatted as an HTML table.
     */
    private String getMatrixAsHtmlTable(HierarchicalClusteringTask.DistanceMatrix matrix) {
        StringBuilder html = new StringBuilder();
        List<String> labels = matrix.getLabels();
        BigDecimal[][] distances = matrix.getDistances();

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
