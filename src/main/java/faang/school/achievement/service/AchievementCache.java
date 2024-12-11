package faang.school.achievement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.achievement.model.Achievement;
import faang.school.achievement.repository.AchievementRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementCache {
    private final AchievementRepository achievementRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public Achievement get(String title) {
        Object value = redisTemplate.opsForHash().get("achievements", title);
        if (value == null) {
            log.info("AchievementCache: miss: {}", title);
            Achievement achievement = achievementRepository.findByTitle(title);
            if (achievement != null) {
                log.info("AchievementCache: put: {}", title);
                redisTemplate.opsForHash().put("achievements", title, achievement);
            }
        }
        log.info("AchievementCache: get: {} -> {}", title, value);
        return objectMapper.convertValue(value, Achievement.class);
    }

    @PostConstruct
    public void load() {
        log.info("AchievementCache: load...");
        List<Achievement> achievements = IterableUtils.toList(achievementRepository.findAll());
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            achievements.forEach(achievement ->
                    redisTemplate.opsForHash().put("achievements", achievement.getTitle(), achievement));
            return null;
        });
        log.info("AchievementCache: load done {} items", achievements.size());
    }
}
