package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.dto.TaskParamsDTO;

import java.util.List;

public interface TaskService {

    List<TaskDTO> getAll(TaskParamsDTO params);

    TaskDTO getById(Long id);

    TaskDTO create(TaskCreateDTO data);

    TaskDTO update(TaskUpdateDTO data, Long id);

    void delete(Long id);
}
