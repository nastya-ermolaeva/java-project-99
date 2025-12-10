package hexlet.code.mapper;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.TaskStatus;

import hexlet.code.repository.TaskStatusRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)

public abstract class TaskStatusMapper {

    @Autowired
    private TaskStatusRepository tsRepository;

    public abstract TaskStatus map(TaskStatusCreateDTO dto);

    public abstract TaskStatusDTO map(TaskStatus model);

    public abstract TaskStatus map(TaskStatusDTO dto);

    public abstract void update(TaskStatusUpdateDTO dto, @MappingTarget TaskStatus model);

    @Named("slugToTaskStatus")
    public TaskStatus fromSlug(String slug) {
        return tsRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with slug " + slug + " not found"));
    }
}

