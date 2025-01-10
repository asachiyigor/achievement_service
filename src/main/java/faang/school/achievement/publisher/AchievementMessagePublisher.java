package faang.school.achievement.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementMessagePublisher implements MessagePublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.data.redis.channel.achievement}")
    private String achievementTopic;

    private final ChannelTopic channelTopicAchievement;

    @Override
    public void publish(String message) {
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.convertAndSend(channelTopicAchievement.getTopic(), message);
        log.info("Published message: {}", message);
    }
}