package faang.school.achievement.publisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AchievementMessagePublisherTest {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ChannelTopic channelTopicAchievement;

    @Test
    void publishSuccess() {
        AchievementMessagePublisher achievementMessagePublisher = new AchievementMessagePublisher(redisTemplate, channelTopicAchievement);
        String message = "Test message";
        String topicName = "achievement-topic";

        when(channelTopicAchievement.getTopic()).thenReturn(topicName);

        achievementMessagePublisher.publish(message);

        verify(redisTemplate).setValueSerializer(any(StringRedisSerializer.class));
        verify(redisTemplate).convertAndSend(topicName, message);
    }
}
