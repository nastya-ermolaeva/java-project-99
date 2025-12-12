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
import hexlet.code.model.Label;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelCreateOrUpdateDTO;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
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
class LabelsControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    private Model<User> userModel;
    private User testUser;
    private JwtRequestPostProcessor token;
    private Model<Label> labelModel;
    private Label testLabel;

    @BeforeEach
    void setUp() {
        labelRepository.deleteAll();
        userRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        userModel = modelGenerator.getUserModel();
        testUser = Instancio.of(userModel).create();
        userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        labelModel = modelGenerator.getLabelModel();
        testLabel = Instancio.of(labelModel).create();
        labelRepository.save(testLabel);
    }

    @Test
    void testIndex() throws Exception {
        Label l1 = Instancio.of(labelModel).create();
        Label l2 = Instancio.of(labelModel).create();

        labelRepository.saveAll(List.of(l1, l2));

        var response = mockMvc.perform(get("/api/labels")
                        .with(token))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        var body = response.getContentAsString();

        List<LabelDTO> labelDTOs = om.readValue(body, new TypeReference<>() {
        });

        var actual = labelDTOs.stream().map(labelMapper::map).toList();
        var expected = labelRepository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        assertThatJson(body).isArray().hasSize(3);
    }

    @Test
    void testIndexWithoutToken() throws Exception {
        mockMvc.perform(get("/api/labels"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testShow() throws Exception {
        var response = mockMvc.perform(get("/api/labels/" + testLabel.getId())
                        .with(token))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        var body = response.getContentAsString();

        assertThatJson(body).and(
                a -> a.node("id").isEqualTo(testLabel.getId()),
                a -> a.node("name").isEqualTo(testLabel.getName())
        );
    }

    @Test
    void testShowWithoutToken() throws Exception {
        mockMvc.perform(get("/api/labels"  + testLabel.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreate() throws Exception {
        var data  = Instancio.of(labelModel).create();

        var dto = new LabelCreateOrUpdateDTO();
        dto.setName(data.getName());

        var response = mockMvc.perform(post("/api/labels")
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        var json = response.getContentAsString();

        assertThatJson(json).and(
                a -> a.node("name").isEqualTo(data.getName())
        );
    }

    @Test
    void testCreateWithoutToken() throws Exception {
        var dto = new LabelCreateOrUpdateDTO();
        dto.setName("Test");

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateValidationFails() throws Exception {
        var data = new HashMap<String, Object>();
        data.put("name", "bo");

        mockMvc.perform(post("/api/labels")
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("name", "New name");

        var response = mockMvc.perform(put("/api/labels/" + testLabel.getId())
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        var json = response.getContentAsString();

        assertThatJson(json).and(
                a -> a.node("name").isEqualTo("New name")
        );

        var updatedLabel = labelRepository.findById(testLabel.getId()).orElseThrow();
        assertThat(updatedLabel.getName()).isEqualTo("New name");
    }

    @Test
    void testUpdateWithoutToken() throws Exception {
        var data = new HashMap<>();
        data.put("name", "New name");

        mockMvc.perform(put("/api/labels/" + testLabel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateValidationFails() throws Exception {
        var data = new HashMap<>();
        data.put("name", "");

        mockMvc.perform(put("/api/labels/" + testLabel.getId())
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/labels/" + testLabel.getId())
                        .with(token))
                .andExpect(status().isNoContent());

        assertThat(labelRepository.findById(testLabel.getId())).isEmpty();
    }

    @Test
    void testDeleteWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/labels/" + testLabel.getId()))
                .andExpect(status().isUnauthorized());
    }
}

