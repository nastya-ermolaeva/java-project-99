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
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.util.ModelGenerator;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import org.instancio.Instancio;
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
class TaskStatusesControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskStatusRepository tsRepository;

    @Autowired
    private TaskStatusMapper tsMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    private Model<User> userModel;
    private User testUser;
    private JwtRequestPostProcessor token;
    private Model<TaskStatus> tsModel;
    private TaskStatus testTS;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
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
    }

    @Test
    void testIndex() throws Exception {
        TaskStatus ts1 = Instancio.of(tsModel).create();
        TaskStatus ts2 = Instancio.of(tsModel).create();
        tsRepository.saveAll(List.of(ts1, ts2));

        var response = mockMvc.perform(get("/api/task_statuses")
                        .with(token))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        var body = response.getContentAsString();

        List<TaskStatusDTO> tsDTOs = om.readValue(body, new TypeReference<>() {
        });

        var actual = tsDTOs.stream().map(tsMapper::map).toList();
        var expected = tsRepository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        assertThatJson(body).isArray().hasSize(3);
    }

    @Test
    void testIndexWithoutToken() throws Exception {
        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testShow() throws Exception {
        var response = mockMvc.perform(get("/api/task_statuses/" + testTS.getId())
                .with(token))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        var body = response.getContentAsString();

        assertThatJson(body).and(
                a -> a.node("id").isEqualTo(testTS.getId()),
                a -> a.node("name").isEqualTo(testTS.getName()),
                a -> a.node("slug").isEqualTo(testTS.getSlug())
        );
    }

    @Test
    void testCreate() throws Exception {
        var data  = Instancio.of(tsModel).create();

        var dto = new TaskStatusCreateDTO();
        dto.setName(data.getName());
        dto.setSlug(data.getSlug());

        var response = mockMvc.perform(post("/api/task_statuses")
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        var json = response.getContentAsString();

        assertThatJson(json).and(
                a -> a.node("name").isEqualTo(data.getName()),
                a -> a.node("slug").isEqualTo(data.getSlug())
        );
    }

    @Test
    void testCreateWithoutToken() throws Exception {
        var data  = Instancio.of(tsModel).create();

        var dto = new TaskStatusCreateDTO();
        dto.setName(data.getName());
        dto.setSlug(data.getSlug());

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateValidationFails() throws Exception {
        var data = new HashMap<String, Object>();
        data.put("name", "");
        data.put("slug", "ab");

        mockMvc.perform(post("/api/task_statuses")
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("name", "newName");

        var response = mockMvc.perform(put("/api/task_statuses/" + testTS.getId())
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        var json = response.getContentAsString();

        assertThatJson(json).and(
                a -> a.node("name").isEqualTo("newName"),
                a -> a.node("slug").isEqualTo(testTS.getSlug())
        );

        var updatedTS = tsRepository.findById(testTS.getId()).orElseThrow();
        assertThat(updatedTS.getName()).isEqualTo("newName");
    }

    @Test
    void testUpdateWithoutToken() throws Exception {
        var data = new HashMap<>();
        data.put("name", "newName");

        mockMvc.perform(put("/api/task_statuses/" + testTS.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateValidationFails() throws Exception {
        var data = new HashMap<>();
        data.put("slug", "");

        mockMvc.perform(put("/api/task_statuses/" + testUser.getId())
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/" + testTS.getId())
                        .with(token))
                .andExpect(status().isNoContent());

        assertThat(tsRepository.findById(testTS.getId())).isEmpty();
    }

    @Test
    void testDeleteWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/" + testTS.getId()))
                .andExpect(status().isUnauthorized());
    }
}

