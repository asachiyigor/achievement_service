package faang.school.achievement.filters.achievementFilters;

import faang.school.achievement.dto.AchievementFilterDto;
import faang.school.achievement.model.Achievement;
import faang.school.achievement.model.Rarity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Slf4j
@Component
public class FilterByAchievementRarity implements AchievementFilter {
    @Override
    public boolean isApplicable(AchievementFilterDto achievementFilterDto) {
        return achievementFilterDto.getRarity() != null && !achievementFilterDto.getRarity().isBlank();
    }

    @Override
    public Stream<Achievement> apply(Stream<Achievement> achievements, AchievementFilterDto achievementFilterDto) {
        try {
            Rarity rarity = Rarity.valueOf(achievementFilterDto.getRarity().toUpperCase());
            return achievements.filter(a -> a.getRarity() == rarity);
        } catch (IllegalArgumentException e) {
            log.error("Такого достижения не существует: {}", e.getMessage());
            return Stream.empty();
        }
    }
}
