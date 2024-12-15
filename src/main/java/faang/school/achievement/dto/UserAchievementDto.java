package faang.school.achievement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievementDto {
    @NotNull
    @Positive
    private Long userId;
    @NotNull
    @Positive
    private Long achievementId;
}