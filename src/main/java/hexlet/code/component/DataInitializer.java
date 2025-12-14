package hexlet.code.component;

import hexlet.code.dto.LabelCreateOrUpdateDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.TaskStatusService;
import hexlet.code.repository.LabelRepository;
import hexlet.code.service.LabelService;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Component
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    private final UserService userService;

    private final TaskStatusRepository tsRepository;

    private final TaskStatusService tsService;

    private final LabelRepository labelRepository;

    private final LabelService labelService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initAdmin();
        initDefaultTaskStatuses();
        initDefaultLabels();
    }

    public void initAdmin() {
        var email = "hexlet@example.com";

        if (userRepository.findByEmail(email).isEmpty()) {

            var admin = new UserCreateDTO();
            admin.setEmail(email);
            admin.setFirstName("Hexlet");
            admin.setLastName("Admin");
            admin.setPassword("qwerty");

            userService.create(admin);
        }
    }

    public void initDefaultTaskStatuses() {
        var statuses = Map.of(
                "Draft", "draft",
                "To review", "to_review",
                "To be fixed", "to_be_fixed",
                "To publish", "to_publish",
                "Published", "published");

        for (var entry : statuses.entrySet()) {
            var slug = entry.getValue();

            if (!tsRepository.existsBySlug(slug)) {
                var name = entry.getKey();
                var taskStatus = new TaskStatusCreateDTO();
                taskStatus.setName(name);
                taskStatus.setSlug(slug);
                tsService.create(taskStatus);
            }
        }
    }

    public void initDefaultLabels() {
        var labels = Set.of("feature", "bug");

        for (var label : labels) {
            if (!labelRepository.existsByName(label)) {
                var defaultLabel = new LabelCreateOrUpdateDTO();
                defaultLabel.setName(label);
                labelService.create(defaultLabel);
            }
        }
    }
}
