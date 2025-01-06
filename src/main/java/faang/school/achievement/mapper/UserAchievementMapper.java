package faang.school.achievement.mapper;

import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.model.UserAchievement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserAchievementMapper {

    @Mapping(source = "achievement", target = "achievement")
    UserAchievementDto toDto(UserAchievement userAchievement);

    @Mapping(source = "achievement", target = "achievement")
    UserAchievement toEntity(UserAchievementDto userAchievementDto);
}