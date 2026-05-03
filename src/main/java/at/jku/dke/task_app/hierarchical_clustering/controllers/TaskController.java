package at.jku.dke.task_app.hierarchical_clustering.controllers;

import at.jku.dke.etutor.task_app.controllers.BaseTaskController;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.dto.AssignmentTypeDto;
import at.jku.dke.task_app.hierarchical_clustering.dto.HierarchicalClusteringTaskDto;
import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
import at.jku.dke.task_app.hierarchical_clustering.services.HierarchicalClusteringTaskService;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing {@link HierarchicalClusteringTask}s.
 */
@RestController
public class TaskController extends BaseTaskController<HierarchicalClusteringTask, HierarchicalClusteringTaskDto, ModifyHierarchicalClusteringTaskDto> {

    /**
     * Creates a new instance of class {@link TaskController}.
     *
     * @param taskService The task service.
     */
    public TaskController(HierarchicalClusteringTaskService taskService) {
        super(taskService);
    }

    @Override
    protected HierarchicalClusteringTaskDto mapToDto(HierarchicalClusteringTask task) {
        AssignmentTypeDto assignmentType = task.getCoordinateList() == null ? AssignmentTypeDto.MATRIX : AssignmentTypeDto.COORDINATES;
        return new HierarchicalClusteringTaskDto(
            assignmentType,
            task.getDistanceMetric(),
            task.getDistanceMatrix().getLabels().size(), // =nDataPoints
            task.getLinkageMethod(),
            task.getPointsPerCorrectCluster(),
            task.getWrongOrderPenalty(),
            task.getDistanceMatrix());
    }

}
