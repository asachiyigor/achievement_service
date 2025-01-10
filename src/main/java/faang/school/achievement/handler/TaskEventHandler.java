package faang.school.achievement.handler;

import faang.school.achievement.model.TaskCompletedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public abstract class TaskEventHandler implements EventHandler<TaskCompletedEvent> {

    @Async("taskExecutor")
    public abstract void handleEvent(TaskCompletedEvent event);
}