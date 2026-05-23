package at.jku.dke.task_app.hierarchical_clustering.controllers;

import at.jku.dke.etutor.task_app.controllers.BaseTaskController;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringMerge;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.dto.AssignmentTypeDto;
import at.jku.dke.task_app.hierarchical_clustering.dto.HierarchicalClusteringTaskDto;
import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
import at.jku.dke.task_app.hierarchical_clustering.generators.dendrogram.DendrogramImageExporter;
import at.jku.dke.task_app.hierarchical_clustering.generators.dendrogram.DendrogramSvgRenderer;
import at.jku.dke.task_app.hierarchical_clustering.services.HierarchicalClusteringTaskService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        HierarchicalClusteringTask.CoordinateList coordinateListSystem = task.getCoordinateList();
        AssignmentTypeDto assignmentType = coordinateListSystem == null ? AssignmentTypeDto.MATRIX : AssignmentTypeDto.COORDINATES;
        int lengthX = coordinateListSystem != null ? coordinateListSystem.getLengthX() : 10;
        int lengthY = coordinateListSystem != null ? coordinateListSystem.getLengthY() : 10;
        List<HierarchicalClusteringTask.CoordinatePoint> coordinateList = coordinateListSystem != null ? coordinateListSystem.getCoordinateList() : null;

        List<HierarchicalClusteringMerge> solutionMergeHistory = task.getSolutionMergeHistory();
        StringBuilder solutionBuilder = new StringBuilder();

        for (HierarchicalClusteringMerge merge : solutionMergeHistory) {
            solutionBuilder.append(merge.toString()).append("\n");
        }

        String dendrogramSvg = new DendrogramSvgRenderer().render(task.getDendrogramModel());
        byte[] dendrogramPng;
        try {
            dendrogramPng = new DendrogramImageExporter().export("png", dendrogramSvg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new HierarchicalClusteringTaskDto(
            assignmentType,
            task.getDistanceMetric(),
            task.getDistanceMatrix().getLabels().size(), // =nDataPoints
            task.getLinkageMethod(),
            task.getPointsPerCorrectCluster(),
            task.getWrongOrderPenalty(),
            lengthX,
            lengthY,
            coordinateList,
            task.getDistanceMatrix(),
            solutionBuilder.toString(),
            dendrogramPng);
    }

}
