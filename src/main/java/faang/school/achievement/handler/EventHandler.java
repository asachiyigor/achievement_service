package faang.school.achievement.handler;

public interface EventHandler<T> {
    void handleEvent(T event);
}