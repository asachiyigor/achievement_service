package faang.school.achievement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
public class UserAchievementDto {
    @NotNull
    @Positive
    private long userId;
    private AchievementDto achievement;
}
