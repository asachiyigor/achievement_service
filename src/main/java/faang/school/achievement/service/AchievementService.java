package faang.school.achievement.service;

import faang.school.achievement.dto.AchievementDto;
import faang.school.achievement.dto.AchievementFilterDto;
import faang.school.achievement.dto.AchievementProgressDto;
import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.filters.achievementFilters.AchievementFilter;
import faang.school.achievement.mapper.AchievementMapper;
import faang.school.achievement.model.Achievement;
import faang.school.achievement.repository.AchievementProgressRepository;
import faang.school.achievement.repository.AchievementRepository;
import faang.school.achievement.repository.UserAchievementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Component
@Slf4j
public class AchievementService {
    private final AchievementRepository achievementRepository;
    private final AchievementProgressRepository achievementProgressRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final List<AchievementFilter> achievementFilters;
    private final AchievementMapper achievementMapper;

    public List<AchievementDto> getAllAchievement(AchievementFilterDto achievementFilterDto) {
        log.info("Получаем все достижения");
        Stream<Achievement> achievementStream = StreamSupport.stream(achievementRepository.findAll().spliterator(), false);
        return achievementFilters.stream()
                .filter(filter -> filter.isApplicable(achievementFilterDto))
                .reduce(achievementStream, (stream, filter) -> filter.apply(stream, achievementFilterDto), (s1, s2) -> s1)
                .map(achievementMapper::toDtoFromEntity)
                .toList();

    }

    public List<UserAchievementDto> getAllUserAchievementById(long userId) {
        log.info("Получаем все достижения пользователя");
        return achievementMapper.toUserAchievementDto(userAchievementRepository.findByUserId(userId));
    }

    public AchievementDto getAchievementById(long achievementId) {
        log.info("Получаем достижение по ид");
        return achievementMapper.toDtoFromEntity(achievementRepository.findById(achievementId).orElseThrow(() ->
                new EntityNotFoundException("Достижение не найдено")));

    }

    public List<AchievementProgressDto> getAllNotReceivedUserAchievement(long userId) {
        log.info("Получаем достижения в процессе выполнения, по ид пользователя");
        return achievementMapper.toAchievementProgress(achievementProgressRepository.findByUserId(userId));
    }
}
