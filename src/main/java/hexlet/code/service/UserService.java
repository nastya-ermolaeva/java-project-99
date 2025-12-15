package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;

import java.util.List;

public interface UserService {

    List<UserDTO> getAll();

    UserDTO getById(Long id);

    UserDTO create(UserCreateDTO data);

    UserDTO update(UserUpdateDTO data, Long id);

    void delete(Long id);
}
