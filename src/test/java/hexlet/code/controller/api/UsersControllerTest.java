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
import hexlet.code.dto.UserDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
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
class UsersControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    private Model<User> userModel;
    private User testUser;
    private JwtRequestPostProcessor token;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        userModel = modelGenerator.getUserModel();
        testUser = Instancio.of(userModel).create();
        userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
    }

    @Test
    void testIndex() throws Exception {
        User u1 = Instancio.of(userModel).create();
        User u2 = Instancio.of(userModel).create();
        userRepository.saveAll(List.of(u1, u2));

        var response = mockMvc.perform(get("/api/users")
                .with(token))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        var body = response.getContentAsString();

        List<UserDTO> userDTOs = om.readValue(body, new TypeReference<>() {
        });

        var actual = userDTOs.stream().map(userMapper::map).toList();
        var expected = userRepository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        assertThatJson(body).isArray().hasSize(3);
    }

    @Test
    void testIndexWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    void testShow() throws Exception {
        var response = mockMvc.perform(get("/api/users/" + testUser.getId()).with(token))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        var body = response.getContentAsString();

        assertThatJson(body).and(
                a -> a.node("id").isEqualTo(testUser.getId()),
                a -> a.node("email").isEqualTo(testUser.getEmail()),
                a -> a.node("firstName").isEqualTo(testUser.getFirstName()),
                a -> a.node("lastName").isEqualTo(testUser.getLastName())
        );
    }

    @Test
    void testShowWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreate() throws Exception {
        var data = Instancio.of(userModel).create();
        var dto = userMapper.toCreateDTO(data);
        dto.setPassword("qwerTy12345");

        var response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        var json = response.getContentAsString();

        assertThatJson(json).and(
                a -> a.node("id").isNotNull(),
                a -> a.node("email").isEqualTo(data.getEmail()),
                a -> a.node("firstName").isEqualTo(data.getFirstName()),
                a -> a.node("lastName").isEqualTo(data.getLastName())
        );
    }

    @Test
    void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("firstName", "Mike");

        var response = mockMvc.perform(put("/api/users/" + testUser.getId())
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        var json = response.getContentAsString();

        assertThatJson(json).and(
                a -> a.node("firstName").isEqualTo("Mike"),
                a -> a.node("email").isEqualTo(testUser.getEmail())
        );

        var updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getFirstName()).isEqualTo("Mike");
    }

    @Test
    void testUpdateByOtherUser() throws Exception {
        var other = Instancio.of(userModel).create();
        userRepository.save(other);

        var data = new HashMap<>();
        data.put("firstName", "Other");

        mockMvc.perform(put("/api/users/" + other.getId())
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getId())
                        .with(token))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }

    @Test
    void testDeleteOtherUser() throws Exception {
        var other = Instancio.of(userModel).create();
        userRepository.save(other);

        mockMvc.perform(delete("/api/users/" + other.getId())
                        .with(token))
                .andExpect(status().isForbidden());
    }
}

