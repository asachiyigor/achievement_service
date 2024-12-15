package faang.school.achievement.filters.achievementFilters;

import faang.school.achievement.dto.AchievementFilterDto;
import faang.school.achievement.model.Achievement;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class FilterByAchievementDescription implements AchievementFilter {
    @Override
    public boolean isApplicable(AchievementFilterDto achievementFilterDto) {
        return achievementFilterDto.getDescription() != null && !achievementFilterDto.getDescription().isBlank();
    }

    @Override
    public Stream<Achievement> apply(Stream<Achievement> achievements, AchievementFilterDto achievementFilterDto) {
        return achievements.filter(a -> a.getDescription().equalsIgnoreCase(achievementFilterDto.getDescription()));
    }
}
