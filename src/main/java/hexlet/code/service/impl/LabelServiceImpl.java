package hexlet.code.service.impl;

import hexlet.code.service.LabelService;
import hexlet.code.dto.LabelCreateOrUpdateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.exception.BadRequestException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;

    private final LabelMapper labelMapper;

    private final TaskRepository taskRepository;

    @Override
    public List<LabelDTO> getAll() {
        var labels = labelRepository.findAll();
        return labels.stream()
                .map(labelMapper::map)
                .toList();
    }

    @Override
    public LabelDTO getById(Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
        return labelMapper.map(label);
    }

    @Override
    public LabelDTO create(LabelCreateOrUpdateDTO data) {
        var label = labelMapper.map(data);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    @Override
    public LabelDTO update(LabelCreateOrUpdateDTO data, Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));

        labelMapper.update(data, label);
        labelRepository.save(label);

        return labelMapper.map(label);
    }

    @Override
    public void delete(Long id) {
        if (taskRepository.existsByLabelsId(id)) {
            throw new BadRequestException("Cannot delete the label as it is used in tasks");
        }

        labelRepository.deleteById(id);
    }
}
