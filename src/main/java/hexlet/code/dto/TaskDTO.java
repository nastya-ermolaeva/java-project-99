package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private Integer index;
    private LocalDate createdAt;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    private String title;
    private String content;
    private String status;
    private Set<Long> taskLabelIds;
}
