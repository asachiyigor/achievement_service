package faang.school.achievement.config.async;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${async.executor.core-pool-size}")
    private int corePoolSize;

    @Value("${async.executor.max-pool-size}")
    private int maxPoolSize;

    @Value("${async.executor.queue-capacity}")
    private int queueCapacity;

    @Value("${async.executor.thread-name-prefix}")
    private String threadNamePrefix;

    @Value("${async.executor.await-termination-seconds}")
    private int awaitTerminationSeconds;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAllowCoreThreadTimeOut(true);

        executor.setRejectedExecutionHandler((r, e) -> {
            throw new RuntimeException("Executor queue is full, cannot accept more tasks");
        });

        executor.initialize();

        return executor;
    }
}