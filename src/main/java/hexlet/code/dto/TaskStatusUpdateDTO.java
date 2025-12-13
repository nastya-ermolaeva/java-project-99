package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskStatusUpdateDTO {
    @Size(min = 1, message = "Name must be at least 1 character long")
    private JsonNullable<String> name;

    @Size(min = 1, message = "Slug must be at least 1 character long")
    private JsonNullable<String> slug;
}
