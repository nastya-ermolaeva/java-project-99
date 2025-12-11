package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Task;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingTarget;

@Mapper(
        uses = {JsonNullableMapper.class,
                ReferenceMapper.class,
                TaskStatusMapper.class,
                LabelMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)

public abstract class TaskMapper {
    @Mapping(target = "name", source = "title")
    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "taskStatus", source = "status", qualifiedByName = "slugToTaskStatus")
    @Mapping(target = "labels", source = "taskLabelIds", qualifiedByName = "idsToLabels")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "title", source = "name")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "content", source = "description")
    @Mapping(target = "status", source = "taskStatus.slug")
    @Mapping(target = "taskLabelIds", source = "labels", qualifiedByName = "labelsToIds")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "name", source = "title")
    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "taskStatus", source = "status", qualifiedByName = "slugToTaskStatus")
    @Mapping(target = "labels", source = "taskLabelIds", qualifiedByName = "idsToLabels")
    public abstract Task map(TaskDTO dto);

    @Mapping(target = "name", source = "title")
    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "taskStatus", source = "status", qualifiedByName = "slugToTaskStatus")
    @Mapping(target = "labels", source = "taskLabelIds", qualifiedByName = "idsToLabels")
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);
}
