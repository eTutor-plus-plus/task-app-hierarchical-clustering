package at.jku.dke.task_app.hierarchical_clustering.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringClusterRepository;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringMergeRepository;
import at.jku.dke.task_app.hierarchical_clustering.dto.AssignmentTypeDto;
import at.jku.dke.task_app.hierarchical_clustering.generators.matrix.DistanceMetric;
import at.jku.dke.task_app.hierarchical_clustering.clustering.LinkageMethod;
import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
import at.jku.dke.task_app.hierarchical_clustering.dendrogram.DendrogramModel;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HierarchicalClusteringTaskServiceTest {

    @Test
    void createTask() {
        // Arrange
        ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> dto = new ModifyTaskDto<>(7L, new BigDecimal(4), "hierarchical-clustering", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(
            AssignmentTypeDto.MATRIX,
            null,
            5,
            LinkageMethod.COMPLETE,
            new BigDecimal(1),
            new BigDecimal(2),
            null,
            new HierarchicalClusteringTask.DistanceMatrix(List.of("1", "2", "3", "4", "5"), toBigDecimalMatrix(new double[][]{
                { 0,  1,  2,  3,  4},
                { 1,  0,  5,  6,  7},
                { 2,  5,  0,  8,  9},
                { 3,  6,  8,  0, 10},
                { 4,  7,  9, 10,  0}
            })
        )));
        HierarchicalClusteringTaskService service = new HierarchicalClusteringTaskService(null, null, mock(HierarchicalClusteringMergeRepository.class), mock(HierarchicalClusteringClusterRepository.class));

        // Act
        HierarchicalClusteringTask task = service.createTask(3, dto);

        // Assert
        assertEquals(dto.additionalData().nDataPoints(), task.getDistanceMatrix().getLabels().size());
        assertEquals(dto.additionalData().linkageMethod(), task.getLinkageMethod());
        assertEquals(dto.additionalData().distanceMetric(), task.getDistanceMetric());
        assertEquals(dto.additionalData().pointsPerCorrectCluster(), task.getPointsPerCorrectCluster());
        assertEquals(dto.additionalData().wrongOrderPenalty(), task.getWrongOrderPenalty());
        assertNull(task.getCoordinateSystem());
    }

    @Test
    void createTaskInvalidType() {
        // Arrange
        ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "sql", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(
            AssignmentTypeDto.MATRIX,
            null,
            5,
            LinkageMethod.COMPLETE,
            new BigDecimal(1),
            new BigDecimal(2),
            null,
            new HierarchicalClusteringTask.DistanceMatrix(List.of("1", "2", "3", "4", "5"), toBigDecimalMatrix(new double[][]{
                { 0,  1,  2,  3,  4},
                { 1,  0,  5,  6,  7},
                { 2,  5,  0,  8,  9},
                { 3,  6,  8,  0, 10},
                { 4,  7,  9, 10,  0}
            })
        )));
        HierarchicalClusteringTaskService service = new HierarchicalClusteringTaskService(null, null, null, null);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.createTask(3, dto));
    }

    @Test
    void updateTask() {
        // Arrange
        ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> dto = new ModifyTaskDto<>(7L, new BigDecimal(4), "hierarchical-clustering", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(
            AssignmentTypeDto.MATRIX,
            null,
            5,
            LinkageMethod.COMPLETE,
            new BigDecimal(1),
            new BigDecimal(2),
            null,
            new HierarchicalClusteringTask.DistanceMatrix(List.of("1", "2", "3", "4", "5"), toBigDecimalMatrix(new double[][]{
                { 0,  1,  2,  3,  4},
                { 1,  0,  5,  6,  7},
                { 2,  5,  0,  8,  9},
                { 3,  6,  8,  0, 10},
                { 4,  7,  9, 10,  0}
            })
        )));
        HierarchicalClusteringTaskService service = new HierarchicalClusteringTaskService(null, null, mock(HierarchicalClusteringMergeRepository.class), mock(HierarchicalClusteringClusterRepository.class));
        HierarchicalClusteringTask task = getNewTask();

        // Act
        service.updateTask(task, dto);

        // Assert
        assertEquals(dto.additionalData().nDataPoints(), task.getDistanceMatrix().getLabels().size());
        assertEquals(dto.additionalData().linkageMethod(), task.getLinkageMethod());
        assertEquals(dto.additionalData().distanceMetric(), task.getDistanceMetric());
        assertEquals(dto.additionalData().pointsPerCorrectCluster(), task.getPointsPerCorrectCluster());
        assertEquals(dto.additionalData().wrongOrderPenalty(), task.getWrongOrderPenalty());
    }

    @Test
    void updateTaskInvalidType() {
        // Arrange
        ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> dto = new ModifyTaskDto<>(7L, new BigDecimal(4), "sql", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(
            AssignmentTypeDto.MATRIX,
            null,
            5,
            LinkageMethod.COMPLETE,
            new BigDecimal(1),
            new BigDecimal(2),
            null,
            new HierarchicalClusteringTask.DistanceMatrix(List.of("1", "2", "3", "4", "5"), toBigDecimalMatrix(new double[][]{
                { 0,  1,  2,  3,  4},
                { 1,  0,  5,  6,  7},
                { 2,  5,  0,  8,  9},
                { 3,  6,  8,  0, 10},
                { 4,  7,  9, 10,  0}
            })
        )));
        HierarchicalClusteringTaskService service = new HierarchicalClusteringTaskService(null, null, null, null);
        HierarchicalClusteringTask task = getNewTask();

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> service.updateTask(task, dto));
    }

    @Test
    void mapToReturnData() {
        // Arrange
        MessageSource ms = mock(MessageSource.class);
        HierarchicalClusteringTaskService service = new HierarchicalClusteringTaskService(null, ms, null, null);
        HierarchicalClusteringTask task = getNewTask();

        // Act
        TaskModificationResponseDto result = service.mapToReturnData(task, true);

        // Assert
        assertNotNull(result);
        verify(ms).getMessage(eq("description.general"), argThat(args -> args != null && args.length == 4), eq(Locale.ENGLISH));
        verify(ms).getMessage(eq("description.agglomerative"), argThat(Objects::isNull), eq(Locale.ENGLISH));
        verify(ms).getMessage(eq("description.complete"), argThat(Objects::isNull), eq(Locale.ENGLISH));
        verify(ms).getMessage(eq("description.matrix"), argThat(args -> args != null && args.length == 1), eq(Locale.ENGLISH));
        verify(ms).getMessage(eq("description.ordering"), argThat(Objects::isNull), eq(Locale.ENGLISH));
    }

    private HierarchicalClusteringTask getNewTask() {
        // initialize task with empty values to avoid NullPointerExceptions
        var task = new HierarchicalClusteringTask(1L, BigDecimal.TWO, TaskStatus.APPROVED, new HierarchicalClusteringTask.DistanceMatrix(
            List.of("1", "2", "3"),
            toBigDecimalMatrix(new double[][]{
                {0, 1, 2},
                {1, 0, 3},
                {2, 3, 0}
            })), new BigDecimal(2));
        task.setDistanceMetric(DistanceMetric.MANHATTAN);
        task.setWrongOrderPenalty(BigDecimal.ONE);
        task.setLinkageMethod(LinkageMethod.COMPLETE);
        task.setDendrogramModel(new DendrogramModel(List.of(), new DendrogramModel.Node()));

        return task;
    }

    private BigDecimal[][] toBigDecimalMatrix(double[][] values) {
        BigDecimal[][] result = new BigDecimal[values.length][];
        for (int i = 0; i < values.length; i++) {
            result[i] = new BigDecimal[values[i].length];
            for (int j = 0; j < values[i].length; j++) {
                result[i][j] = BigDecimal.valueOf(values[i][j]);
            }
        }
        return result;
    }

}
