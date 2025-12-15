package hexlet.code.service;

import hexlet.code.dto.LabelCreateOrUpdateDTO;
import hexlet.code.dto.LabelDTO;

import java.util.List;

public interface LabelService {

    List<LabelDTO> getAll();

    LabelDTO getById(Long id);

    LabelDTO create(LabelCreateOrUpdateDTO data);

    LabelDTO update(LabelCreateOrUpdateDTO data, Long id);

    void delete(Long id);
}
