package faang.school.achievement.service;

import faang.school.achievement.dto.AchievementDto;
import faang.school.achievement.dto.AchievementFilterDto;
import faang.school.achievement.dto.AchievementProgressDto;
import faang.school.achievement.dto.UserAchievementDto;
import faang.school.achievement.exception.DataValidationException;
import faang.school.achievement.filters.achievementFilters.AchievementFilter;
import faang.school.achievement.mapper.AchievementMapper;
import faang.school.achievement.model.Achievement;
import faang.school.achievement.model.AchievementProgress;
import faang.school.achievement.model.UserAchievement;
import faang.school.achievement.repository.AchievementProgressRepository;
import faang.school.achievement.repository.AchievementRepository;
import faang.school.achievement.repository.UserAchievementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Transactional(readOnly = true)
    public boolean hasAchievement(long userId, long achievementId) {
        return userAchievementRepository.existsByUserIdAndAchievementId(userId, achievementId);
    }

    @Transactional
    public void createProgressIfNecessary(long userId, long achievementId) {
        if (!achievementProgressRepository.existsByUserIdAndAchievementId(userId, achievementId)) {
            Achievement achievement = achievementRepository.findById(achievementId)
                    .orElseThrow(() -> new DataValidationException("Achievement not found"));

            AchievementProgress progress = AchievementProgress.builder()
                    .userId(userId)
                    .achievement(achievement)
                    .currentPoints(0L)
                    .version(0L)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            achievementProgressRepository.save(progress);
            log.debug("Created new progress for user {} and achievement {}", userId, achievementId);
        }
    }

    @Transactional(readOnly = true)
    public AchievementProgressDto getProgress(long userId, long achievementId) {
        AchievementProgress progress = achievementProgressRepository.findByUserIdAndAchievementId(userId, achievementId)
                .orElseThrow(() -> new DataValidationException(
                        String.format("Achievement progress not found for userId: %d and achievementId: %d",
                                userId, achievementId)));
        return achievementMapper.toDto(progress);
    }

    @Transactional
    public AchievementProgressDto saveProgress(AchievementProgress progress) {
        progress.setUpdatedAt(LocalDateTime.now());
        progress = achievementProgressRepository.save(progress);
        return achievementMapper.toDto(progress);
    }
    @Transactional
    public UserAchievementDto giveAchievement(AchievementDto achievementDto, long userId) {
        if (!hasAchievement(userId, achievementDto.getId())) {
            Achievement achievement = achievementRepository.findById(achievementDto.getId())
                    .orElseThrow(() -> new DataValidationException("Achievement not found"));

            UserAchievement userAchievement = UserAchievement.builder()
                    .achievement(achievement)
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userAchievement = userAchievementRepository.save(userAchievement);
            log.info("User with ID {} received achievement {}", userId, achievement.getTitle());
            return achievementMapper.toDto(userAchievement);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<UserAchievementDto> getUserAchievements(long userId) {
        List<UserAchievement> userAchievements = userAchievementRepository.findAllByUserId(userId);
        return achievementMapper.toUserAchievementDto(userAchievements);
    }

    @Transactional(readOnly = true)
    public List<AchievementProgressDto> getUserProgress(long userId) {
        List<AchievementProgress> progressList = achievementProgressRepository.findAllByUserId(userId);
        return achievementMapper.toAchievementProgress(progressList);
    }
}