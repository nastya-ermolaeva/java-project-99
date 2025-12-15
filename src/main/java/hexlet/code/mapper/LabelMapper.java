package hexlet.code.mapper;

import hexlet.code.dto.LabelCreateOrUpdateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)

public abstract class LabelMapper {

    @Autowired
    private LabelRepository labelRepository;

    public abstract Label map(LabelCreateOrUpdateDTO dto);

    public abstract LabelDTO map(Label model);

    public abstract Label map(LabelDTO dto);

    public abstract void update(LabelCreateOrUpdateDTO dto, @MappingTarget Label model);

    @Named("idsToLabels")
    public Set<Label> getLabelsFromIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }

        var labels = labelRepository.findAllById(ids);

        if (labels.size() != ids.size()) {
            throw new ResourceNotFoundException("Some labels were not found");
        }

        return new HashSet<>(labels);
    }

    @Named("labelsToIds")
    public Set<Long> getLabelIds(Set<Label> labels) {
        if (labels == null || labels.isEmpty()) {
            return Collections.emptySet();
        }

        return labels.stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
    }
}

