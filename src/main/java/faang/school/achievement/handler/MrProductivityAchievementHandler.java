package faang.school.achievement.handler;

import faang.school.achievement.dto.AchievementDto;
import faang.school.achievement.dto.AchievementProgressDto;
import faang.school.achievement.exception.AchievementNotFoundException;
import faang.school.achievement.mapper.AchievementMapper;
import faang.school.achievement.model.Achievement;
import faang.school.achievement.model.AchievementProgress;
import faang.school.achievement.model.TaskCompletedEvent;
import faang.school.achievement.service.AchievementCache;
import faang.school.achievement.service.AchievementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MrProductivityAchievementHandler extends TaskEventHandler {

    private static final String ACHIEVEMENT_TITLE = "MR PRODUCTIVITY";
    private static final long REQUIRED_TASKS = 1000L;

    private final AchievementMapper achievementMapper;
    private final AchievementCache achievementCache;
    private final AchievementService achievementService;

    @Override
    @Async("taskExecutor")
    @Transactional(
    propagation = Propagation.REQUIRES_NEW,
    isolation = Isolation.REPEATABLE_READ,
    rollbackFor = Exception.class)
    @Retryable(
            value = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )

    public void handleEvent(TaskCompletedEvent event) {
        try {
            log.info("Processing task completion event for user: {}", event.getUserId());

            AchievementDto achievement = getAndValidateAchievement();

            if (achievementService.hasAchievement(event.getUserId(), achievement.getId())) {
                log.debug("User {} already has the productivity achievement", event.getUserId());
                return;
            }

            achievementService.createProgressIfNecessary(event.getUserId(), achievement.getId());

            AchievementProgressDto progressDto = achievementService.getProgress(event.getUserId(), achievement.getId());

            AchievementProgress progress = AchievementProgress.builder()
                    .id(progressDto.getId())
                    .userId(progressDto.getUserId())
                    .achievement(achievementMapper.toEntity(progressDto.getAchievement()))
                    .currentPoints(progressDto.getCurrentPoints() + 1) // Увеличиваем на 1
                    .version(progressDto.getVersion())
                    .build();

            progressDto = achievementService.saveProgress(progress);

            log.debug("User {} has completed {} tasks out of {}",
                    event.getUserId(),
                    progressDto.getCurrentPoints(),
                    REQUIRED_TASKS);

            if (progressDto.getCurrentPoints() >= REQUIRED_TASKS) {
                log.info("User {} has completed {} tasks and earned the productivity achievement!",
                        event.getUserId(),
                        REQUIRED_TASKS);
                achievementService.giveAchievement(achievement, event.getUserId());
            }
        } catch (Exception e) {
            log.error("Error processing task completion event for user {}: {}",
                    event.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    private AchievementDto getAndValidateAchievement() {
        Achievement achievement = achievementCache.get(ACHIEVEMENT_TITLE);
        if (achievement == null) {
            log.error("Failed to get {} achievement from cache", ACHIEVEMENT_TITLE);
            throw new AchievementNotFoundException(
                    String.format("Achievement %s not found in cache", ACHIEVEMENT_TITLE));
        }
        return achievementMapper.toDtoFromEntity(achievement);
    }
}