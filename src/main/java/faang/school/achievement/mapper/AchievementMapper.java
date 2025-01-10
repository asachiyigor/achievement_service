package faang.school.achievement.mapper;

import faang.school.achievement.dto.AchievementDto;
import faang.school.achievement.dto.AchievementProgressDto;
import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.dto.AchievementCacheDto;
import faang.school.achievement.model.Achievement;
import faang.school.achievement.model.AchievementProgress;
import faang.school.achievement.model.UserAchievement;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AchievementMapper {
    AchievementDto toDtoFromEntity(Achievement achievement);
    List<AchievementProgressDto> toAchievementProgress(List<AchievementProgress> achievementProgresses);
    List<UserAchievementDto> toUserAchievementDto(List<UserAchievement> userAchievements);
    AchievementCacheDto fromEntityToCacheDto(Achievement achievement);

    @Mapping(target = "userAchievements", ignore = true)
    @Mapping(target = "progresses", ignore = true)
    Achievement fromCacheDtoToEntity(AchievementCacheDto dto);

    @Named("achievementToDto")
    AchievementDto toDto(Achievement achievement);
    Achievement toEntity(AchievementDto achievementDto);

    AchievementProgressDto toDto(AchievementProgress achievementProgress);
    AchievementProgress toEntity(AchievementProgressDto achievementProgressDto);

    UserAchievement toEntity(UserAchievementDto userAchievementDto);
    UserAchievementDto toDto(UserAchievement userAchievement);
}