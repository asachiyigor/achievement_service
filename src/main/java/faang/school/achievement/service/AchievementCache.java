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
        log.debug("AchievementCache: Attempting to get achievement with title: {}", title);

        Object value = redisTemplate.opsForHash().get(KEY_HASH, title);
        if (value == null) {
            log.info("AchievementCache: Cache miss for title: {}", title);

            Achievement achievement = achievementRepository.findByTitle(title);
            if (achievement == null) {
                log.warn("AchievementCache: Achievement not found in database for title: {}", title);
                return null;
            }

            log.info("AchievementCache: Adding achievement with title '{}' to cache", title);
            AchievementCacheDto dto = achievementMapper.fromEntityToCacheDto(achievement);
            redisTemplate.opsForHash().put(KEY_HASH, title, dto);
        }
        log.debug("AchievementCache: Cache hit for title: {}", title);

        log.debug("AchievementCache: Converting cache data for title: {}", title);
        AchievementCacheDto dto = objectMapper.convertValue(value, AchievementCacheDto.class);
        Achievement achievement = achievementMapper.fromCacheDtoToEntity(dto);
        log.info("AchievementCache: Returning achievement for title: {}", title);

        return achievement;
    }

    @PostConstruct
    public void load() {
        log.info("AchievementCache: Starting cache initialization...");

        List<Achievement> achievements = IterableUtils.toList(achievementRepository.findAll());
        if (achievements.isEmpty()) {
            log.warn("AchievementCache: No achievements found in the database. Cache will not be populated.");
            return;
        }
        log.info("AchievementCache: Found {} achievements. Loading into Redis cache...", achievements.size());

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            achievements.forEach(achievement -> {
                AchievementCacheDto dto = achievementMapper.fromEntityToCacheDto(achievement);
                redisTemplate.opsForHash().put(KEY_HASH, dto.getTitle(), dto);
                log.debug("AchievementCache: Added achievement '{}' to cache", dto.getTitle());
            });
            return null;
        });
        redisTemplate.expire(KEY_HASH, Duration.ofDays(TTL));
        log.info("AchievementCache: Cache initialization completed. Total {} achievements cached.", achievements.size());
    }
}