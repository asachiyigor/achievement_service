package faang.school.achievement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @Min(value = 1, message = "ID must be a positive number")
    private Long id;
    @NotBlank(message = "Title should not be blank")
    private String username;
    @Email(message = "Email must be in right format")
    private String email;
}