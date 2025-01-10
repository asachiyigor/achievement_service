package faang.school.achievement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AchievementProgressDto {
    private long id;
    private AchievementDto achievement;
    private long userId;
    private long currentPoints;
    private long version;
}