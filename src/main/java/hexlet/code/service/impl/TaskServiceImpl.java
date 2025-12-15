package hexlet.code.service.impl;

import hexlet.code.service.TaskService;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.specification.TaskSpecification;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    private final TaskSpecification specBuilder;

    @Override
    public List<TaskDTO> getAll(TaskParamsDTO params) {
        var spec = specBuilder.build(params);
        var tasks = taskRepository.findAll(spec);
        return tasks.stream()
                .map(taskMapper::map)
                .toList();
    }

    @Override
    public TaskDTO getById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        return taskMapper.map(task);
    }

    @Override
    public TaskDTO create(TaskCreateDTO data) {
        var task = taskMapper.map(data);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    @Override
    public TaskDTO update(TaskUpdateDTO data, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        taskMapper.update(data, task);
        taskRepository.save(task);

        return taskMapper.map(task);
    }

    @Override
    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
