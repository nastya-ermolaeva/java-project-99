package hexlet.code.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {
    private JsonNullable<Integer> index;

    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;

    @Size(min = 1, message = "Title must be at least 1 character long")
    private JsonNullable<String> title;

    private JsonNullable<String> content;

    private JsonNullable<String> status;

    private JsonNullable<Set<Long>> taskLabelIds;
}
