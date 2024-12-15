package faang.school.achievement.mapper;

import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.dto.UserAchievementRequestDto;
import faang.school.achievement.model.UserAchievement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserAchievementRequestMapper {
    @Mapping(source = "achievement.id", target = "achievementId")
    UserAchievementRequestDto toDto(UserAchievement userAchievement);

    @Mapping(source = "achievementId", target = "achievement.id")
    UserAchievement toEntity(UserAchievementRequestDto userAchievementRequestDto);
}
