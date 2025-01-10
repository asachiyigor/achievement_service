package faang.school.achievement.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.achievement.handler.TaskEventHandler;
import faang.school.achievement.model.TaskCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskEventListener implements MessageListener {
    private final List<TaskEventHandler> handlers;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            TaskCompletedEvent event = objectMapper.readValue(message.getBody(), TaskCompletedEvent.class);
            handlers.forEach(handler -> handler.handleEvent(event));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read message", e);
        }
    }
}