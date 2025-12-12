package hexlet.code.controller.api;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.model.Task;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.util.ModelGenerator;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.instancio.Instancio;
import org.instancio.Select;
import org.instancio.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
class TasksControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskStatusRepository tsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private TaskStatusMapper tsMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    private Model<User> userModel;
    private User testUser;
    private JwtRequestPostProcessor token;
    private Model<Task> taskModel;
    private Task testTask;
    private Model<Label> labelModel;
    private Label testLabel;
    private Model<TaskStatus> tsModel;
    private TaskStatus testTS;
    private Set<Label> labels = new HashSet<>();

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        tsRepository.deleteAll();
        userRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        userModel = modelGenerator.getUserModel();
        testUser = Instancio.of(userModel).create();
        userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        tsModel = modelGenerator.getTaskStatusModel();
        testTS = Instancio.of(tsModel).create();
        tsRepository.save(testTS);

        labelModel = modelGenerator.getLabelModel();
        testLabel = Instancio.of(labelModel).create();
        labelRepository.save(testLabel);

        labels.add(testLabel);

        taskModel = modelGenerator.getTaskModel();
        testTask = Instancio.of(taskModel)
                .set(Select.field(Task::getTaskStatus), testTS)
                .set(Select.field(Task::getAssignee), testUser)
                .set(Select.field(Task::getLabels), labels)
                .create();
        taskRepository.save(testTask);
    }

    @Test
    void testIndex() throws Exception {
        Task t1 = Instancio.of(taskModel)
                .set(Select.field(Task::getTaskStatus), testTS)
                .set(Select.field(Task::getAssignee), testUser)
                .set(Select.field(Task::getLabels), labels)
                .create();

        Task t2 = Instancio.of(taskModel)
                .set(Select.field(Task::getTaskStatus), testTS)
                .set(Select.field(Task::getAssignee), testUser)
                .set(Select.field(Task::getLabels), labels)
                .create();

        taskRepository.saveAll(List.of(t1, t2));

        var response = mockMvc.perform(get("/api/tasks")
                        .with(token))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        var body = response.getContentAsString();

        List<TaskDTO> taskDTOs = om.readValue(body, new TypeReference<>() {
        });

        var actual = taskDTOs.stream().map(taskMapper::map).toList();
        var expected = taskRepository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        assertThatJson(body).isArray().hasSize(3);
    }

    @Test
    void testIndexWithoutToken() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testIndexWithParams() throws Exception {
        testTask.setName("Perfect match");
        taskRepository.save(testTask);

        var otherUser = Instancio.of(userModel).create();
        userRepository.save(otherUser);

        Task t1 = Instancio.of(taskModel)
                .set(Select.field(Task::getTaskStatus), testTS)
                .set(Select.field(Task::getAssignee), otherUser)
                .set(Select.field(Task::getLabels), labels)
                .create();
        t1.setName("Perfect marsh");
        taskRepository.save(t1);

        var otherStatus = Instancio.of(tsModel).create();
        tsRepository.save(otherStatus);

        var otherLabel = Instancio.of(labelModel).create();
        labelRepository.save(otherLabel);

        Task t2 = Instancio.of(taskModel)
                .set(Select.field(Task::getTaskStatus), otherStatus)
                .set(Select.field(Task::getAssignee), testUser)
                .set(Select.field(Task::getLabels), Set.of(otherLabel))
                .create();
        t2.setName("Perfect mango");
        taskRepository.save(t2);

        var response = mockMvc.perform(
                        get("/api/tasks")
                                .param("titleCont", "perfect")
                                .param("assigneeId", testUser.getId().toString())
                                .param("status", testTS.getSlug())
                                .param("labelId", testLabel.getId().toString())
                                .with(token)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var body = response.getContentAsString();
        List<TaskDTO> results = om.readValue(body, new TypeReference<>() { });

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(testTask.getId());
    }

    @Test
    void testIndexWithParamsAndNoResult() throws Exception {
        var response = mockMvc.perform(
                        get("/api/tasks")
                                .param("labelId", String.valueOf(testLabel.getId() + 1))
                                .with(token)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var body = response.getContentAsString();
        List<TaskDTO> results = om.readValue(body, new TypeReference<>() { });

        assertThat(results).isEmpty();
    }

    @Test
    void testShow() throws Exception {
        var response = mockMvc.perform(get("/api/tasks/" + testTask.getId())
                        .with(token))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        var body = response.getContentAsString();

        assertThatJson(body).and(
                a -> a.node("id").isEqualTo(testTask.getId()),
                a -> a.node("title").isEqualTo(testTask.getName()),
                a -> a.node("status").isEqualTo(testTS.getSlug()),
                a -> a.node("assignee_id").isEqualTo(testUser.getId()),
                a -> a.node("taskLabelIds").isArray().containsExactlyInAnyOrder(testLabel.getId())
        );
    }

    @Test
    void testCreate() throws Exception {
        var data  = Instancio.of(taskModel)
                .set(Select.field(Task::getTaskStatus), testTS)
                .set(Select.field(Task::getAssignee), testUser)
                .set(Select.field(Task::getLabels), labels)
                .create();

        var dto = new TaskCreateDTO();
        dto.setTitle(data.getName());
        dto.setContent(data.getDescription());
        dto.setAssigneeId(testUser.getId());
        dto.setStatus(testTS.getSlug());
        dto.setTaskLabelIds(Set.of(testLabel.getId()));

        var response = mockMvc.perform(post("/api/tasks")
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        var json = response.getContentAsString();

        assertThatJson(json).and(
                a -> a.node("title").isEqualTo(data.getName()),
                a -> a.node("status").isEqualTo(testTS.getSlug()),
                a -> a.node("assignee_id").isEqualTo(testUser.getId()),
                a -> a.node("taskLabelIds").isArray().containsExactlyInAnyOrder(testLabel.getId())
        );
    }

    @Test
    void testCreateWithoutToken() throws Exception {
        var dto = new TaskCreateDTO();
        dto.setTitle("Test");
        dto.setStatus(testTS.getSlug());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateValidationFails() throws Exception {
        var data = new HashMap<String, Object>();
        data.put("title", "");
        data.put("status", null);
        data.put("content", "test");

        mockMvc.perform(post("/api/tasks")
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("title", "New title");

        var response = mockMvc.perform(put("/api/tasks/" + testTask.getId())
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        var json = response.getContentAsString();

        assertThatJson(json).and(
                a -> a.node("title").isEqualTo("New title"),
                a -> a.node("status").isEqualTo(testTS.getSlug())
        );

        var updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();
        assertThat(updatedTask.getName()).isEqualTo("New title");
    }

    @Test
    void testUpdateWithoutToken() throws Exception {
        var data = new HashMap<>();
        data.put("title", "New title");

        mockMvc.perform(put("/api/tasks/" + testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateValidationFails() throws Exception {
        var data = new HashMap<>();
        data.put("title", "");

        mockMvc.perform(put("/api/tasks/" + testTask.getId())
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/tasks/" + testTask.getId())
                        .with(token))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(testTask.getId())).isEmpty();
    }

    @Test
    void testDeleteWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/tasks/" + testTask.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteUserWithTask() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getId())
                .with(token))
                .andExpect(status().isBadRequest());
        assertThat(userRepository.findById(testUser.getId())).isPresent();
    }

    @Test
    void testDeleteTaskStatusWithTask() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/" + testTS.getId())
                .with(token))
                .andExpect(status().isBadRequest());

        assertThat(tsRepository.findById(testTS.getId())).isPresent();
    }

    @Test
    void testDeleteLabelWithTask() throws Exception {
        mockMvc.perform(delete("/api/labels/" + testLabel.getId())
                        .with(token))
                .andExpect(status().isBadRequest());

        assertThat(labelRepository.findById(testLabel.getId())).isPresent();
    }
}

