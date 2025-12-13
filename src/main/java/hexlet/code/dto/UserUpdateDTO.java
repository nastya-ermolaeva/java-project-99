package hexlet.code.dto;

import org.openapitools.jackson.nullable.JsonNullable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserUpdateDTO {
    private JsonNullable<String> firstName;
    private JsonNullable<String> lastName;

    @Email
    private JsonNullable<String> email;

    @Size(min = 3, message = "Password must be at least 3 characters long")
    private JsonNullable<String> password;
}
