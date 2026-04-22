package at.jku.dke.task_app.hierarchical_clustering.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskModificationResponseDto;
import at.jku.dke.etutor.task_app.services.BaseTaskService;
import at.jku.dke.task_app.hierarchical_clustering.data.entities.HierarchicalClusteringTask;
import at.jku.dke.task_app.hierarchical_clustering.data.repositories.HierarchicalClusteringTaskRepository;
import at.jku.dke.task_app.hierarchical_clustering.dto.ModifyHierarchicalClusteringTaskDto;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        return new HierarchicalClusteringTask();
    }

    @Override
    protected void updateTask(HierarchicalClusteringTask task, ModifyTaskDto<ModifyHierarchicalClusteringTaskDto> modifyTaskDto) {
        if (!modifyTaskDto.taskType().equals("hierarchical-clustering"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task type.");
        // task.setSolution(modifyTaskDto.additionalData().solution());
    }

    @Override
    protected TaskModificationResponseDto mapToReturnData(HierarchicalClusteringTask task, boolean create) {
        return new TaskModificationResponseDto(
            this.messageSource.getMessage("defaultTaskDescription", null, Locale.GERMAN),
            this.messageSource.getMessage("defaultTaskDescription", null, Locale.ENGLISH)
        );
    }
}
