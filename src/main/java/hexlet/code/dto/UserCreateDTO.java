package hexlet.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDTO {
    private String firstName;
    private String lastName;

    @Email(message = "Email must be in the valid format")
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 3, message = "Password must be at least 3 characters long")
    private String password;
}
