package hexlet.code.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.exception.BadRequestException;

import java.util.List;

@AllArgsConstructor
@Service
public class TaskStatusService {

    private final TaskStatusRepository repository;

    private final TaskStatusMapper mapper;

    private final TaskRepository taskRepository;

    public List<TaskStatusDTO> getAll() {
        var taskStatuses = repository.findAll();
        return taskStatuses.stream()
                .map(mapper::map)
                .toList();
    }

    public TaskStatusDTO getById(Long id) {
        var taskStatus = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));
        return mapper.map(taskStatus);
    }

    public TaskStatusDTO create(TaskStatusCreateDTO data) {
        var taskStatus = mapper.map(data);
        repository.save(taskStatus);
        return mapper.map(taskStatus);
    }

    public TaskStatusDTO update(TaskStatusUpdateDTO data, Long id) {
        var taskStatus = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));

        mapper.update(data, taskStatus);
        repository.save(taskStatus);

        return mapper.map(taskStatus);
    }

    public void delete(Long id) {
        if (taskRepository.existsByTaskStatusId(id)) {
            throw new BadRequestException("Cannot delete the task status as it is used in tasks");
        }

        repository.deleteById(id);
    }
}
