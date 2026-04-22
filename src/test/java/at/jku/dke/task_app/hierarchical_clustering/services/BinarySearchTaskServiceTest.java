//package at.jku.dke.task_app.hierarchical_clustering.services;
//
//import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
//import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
//import at.jku.dke.etutor.task_app.dto.TaskStatus;
//import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
//import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
//import org.junit.jupiter.api.Test;
//import org.springframework.context.MessageSource;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.math.BigDecimal;
//import java.util.Locale;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//
//class BinarySearchTaskServiceTest {
//
//    @Test
//    void createTask() {
//        // Arrange
//        ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "hierarchical-clustering", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(33));
//        HierarchicalClusteringTaskService service = new HierarchicalClusteringTaskService(null, null, null);
//
//        // Act
//        HierarchicalClusteringTask task = service.createTask(3, dto);
//
//        // Assert
//        assertEquals(dto.additionalData().solution(), task.getSolution());
//    }
//
//    @Test
//    void createTaskInvalidType() {
//        // Arrange
//        ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "sql", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(33));
//        HierarchicalClusteringTaskService service = new HierarchicalClusteringTaskService(null, null, null);
//
//        // Act & Assert
//        assertThrows(ResponseStatusException.class, () -> service.createTask(3, dto));
//    }
//
//    @Test
//    void updateTask() {
//        // Arrange
//        ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "hierarchical-clustering", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(33));
//        HierarchicalClusteringTaskService service = new HierarchicalClusteringTaskService(null, null, null);
//        HierarchicalClusteringTask task = new HierarchicalClusteringTask(3);
//
//        // Act
//        service.updateTask(task, dto);
//
//        // Assert
//        assertEquals(dto.additionalData().solution(), task.getSolution());
//    }
//
//    @Test
//    void updateTaskInvalidType() {
//        // Arrange
//        ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> dto = new ModifyTaskDto<>(7L, BigDecimal.TEN, "sql", TaskStatus.APPROVED, new ModifyHierarchicalClusteringTaskDto(33));
//        HierarchicalClusteringTaskService service = new HierarchicalClusteringTaskService(null, null, null);
//        HierarchicalClusteringTask task = new HierarchicalClusteringTask(3);
//
//        // Act & Assert
//        assertThrows(ResponseStatusException.class, () -> service.updateTask(task, dto));
//    }
//
//    @Test
//    void mapToReturnData() {
//        // Arrange
//        MessageSource ms = mock(MessageSource.class);
//        HierarchicalClusteringTaskService service = new HierarchicalClusteringTaskService(null, null, ms);
//        HierarchicalClusteringTask task = new HierarchicalClusteringTask(3);
//        task.setSolution(33);
//
//        // Act
//        TaskModificationResponseDto result = service.mapToReturnData(task, true);
//
//        // Assert
//        assertNotNull(result);
//        verify(ms).getMessage("defaultTaskDescription", null, Locale.GERMAN);
//        verify(ms).getMessage("defaultTaskDescription", null, Locale.ENGLISH);
//    }
//
//}
