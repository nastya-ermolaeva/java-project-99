package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class TaskStatusCreateDTO {
    @NotBlank
    @Size(min = 1, message = "Name must be at least 1 character long")
    private String name;

    @NotBlank
    @Size(min = 1, message = "Slug must be at least 1 character long")
    private String slug;
}
