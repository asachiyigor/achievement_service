package faang.school.achievement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.achievement.dto.AchievementCacheDto;
import faang.school.achievement.mapper.AchievementMapper;
import faang.school.achievement.model.Achievement;
import faang.school.achievement.repository.AchievementRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementCache {
    private final static int TTL = 1;
    private final static String KEY_HASH = "achievements";

    private final AchievementRepository achievementRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AchievementMapper achievementMapper;

    public Achievement get(String title) {
        Object value = redisTemplate.opsForHash().get(KEY_HASH, title);
        if (value == null) {
            log.info("AchievementCache: miss: {}", title);
            Achievement achievement = achievementRepository.findByTitle(title);
            if (achievement != null) {
                log.info("AchievementCache: put: {}", title);
                AchievementCacheDto dto = achievementMapper.fromEntityToCacheDto(achievement);
                redisTemplate.opsForHash().put(KEY_HASH, title, dto);
            }
        }
        log.info("AchievementCache: get: {} -> {}", title, value);
        AchievementCacheDto dto = objectMapper.convertValue(value, AchievementCacheDto.class);
        return achievementMapper.fromCacheDtoToEntity(dto);
    }

    @PostConstruct
    public void load() {
        log.info("AchievementCache: load...");
        List<Achievement> achievements = IterableUtils.toList(achievementRepository.findAll());
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            achievements.forEach(achievement -> {
                AchievementCacheDto dto = achievementMapper.fromEntityToCacheDto(achievement);
                redisTemplate.opsForHash().put(KEY_HASH, dto.getTitle(), dto);
            });
            redisTemplate.expire(KEY_HASH, Duration.ofDays(TTL));
            return null;
        });
        log.info("AchievementCache: load done {} items", achievements.size());
    }
}
