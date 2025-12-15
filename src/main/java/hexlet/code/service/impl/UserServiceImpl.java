package hexlet.code.service.impl;

import hexlet.code.service.UserService;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.exception.BadRequestException;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final TaskRepository taskRepository;

    @Override
    public List<UserDTO> getAll() {
        var users = userRepository.findAll();
        return users.stream()
                .map(userMapper::map)
                .toList();
    }

    @Override
    public UserDTO getById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        return userMapper.map(user);
    }

    @Override
    public UserDTO create(UserCreateDTO data) {
        var user = userMapper.map(data);
        var hashedPassword = passwordEncoder.encode(data.getPassword());
        user.setPasswordDigest(hashedPassword);
        userRepository.save(user);
        return userMapper.map(user);
    }

    @Override
    public UserDTO update(UserUpdateDTO data, Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        if (data.getPassword() != null && data.getPassword().isPresent()) {
            var hashed = passwordEncoder.encode(data.getPassword().get());
            user.setPasswordDigest(hashed);
        }

        userMapper.update(data, user);
        userRepository.save(user);

        return userMapper.map(user);
    }

    @Override
    public void delete(Long id) {
        if (taskRepository.existsByAssigneeId(id)) {
            throw new BadRequestException("Cannot delete the user as they are assigned to tasks");
        }
        userRepository.deleteById(id);
    }
}
