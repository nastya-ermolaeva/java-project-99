package hexlet.code.util;

import hexlet.code.model.User;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.Task;
import hexlet.code.model.Label;
import net.datafaker.Faker;
import org.instancio.Model;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ModelGenerator {

    @Autowired
    private Faker faker;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Model<User> getUserModel() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPasswordDigest), () ->
                        passwordEncoder.encode(faker.internet().password(8, 20)))
                .toModel();
    }

    public Model<TaskStatus> getTaskStatusModel() {
        return Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .supply(Select.field(TaskStatus::getName), () -> faker.lorem().sentence(2))
                .supply(Select.field(TaskStatus::getSlug), () -> faker.internet().slug())
                .toModel();
    }

    public Model<Task> getTaskModel() {
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .supply(Select.field(Task::getName), () -> faker.lorem().sentence(2))
                .supply(Select.field(Task::getDescription), () -> faker.lorem().sentence(15))
                .supply(Select.field(Task::getIndex), () -> faker.number().numberBetween(1, 5000))
                .toModel();
    }

    public Model<Label> getLabelModel() {
        return Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getCreatedAt))
                .supply(Select.field(Label::getName), () -> faker.lorem().characters(3, 1000))
                .toModel();
    }
}
