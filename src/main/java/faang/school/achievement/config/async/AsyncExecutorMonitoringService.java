package faang.school.achievement.config.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class AsyncExecutorMonitoringService {

    @Qualifier("taskExecutor")
    private final Executor taskExecutor;

    @Value("${monitoring.thread-pool.thresholds.pool-utilization}")
    private double poolUtilizationThreshold;

    @Value("${monitoring.thread-pool.thresholds.queue-utilization}")
    private double queueUtilizationThreshold;

    @Scheduled(fixedRateString = "${monitoring.thread-pool.rate}")
    public void reportThreadPoolStatus() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) taskExecutor;

        int activeCount = executor.getActiveCount();
        int corePoolSize = executor.getCorePoolSize();
        int maxPoolSize = executor.getMaxPoolSize();
        int queueSize = executor.getThreadPoolExecutor().getQueue().size();
        long completedTaskCount = executor.getThreadPoolExecutor().getCompletedTaskCount();
        long taskCount = executor.getThreadPoolExecutor().getTaskCount();

        log.info("Thread Pool Metrics:\n" +
                        "Active threads: {}\n" +
                        "Core pool size: {}\n" +
                        "Max pool size: {}\n" +
                        "Queue size: {}\n" +
                        "Completed tasks: {}\n" +
                        "Total tasks: {}\n" +
                        "Queue remaining capacity: {}",
                activeCount,
                corePoolSize,
                maxPoolSize,
                queueSize,
                completedTaskCount,
                taskCount,
                executor.getThreadPoolExecutor().getQueue().remainingCapacity());

        double utilizationRate = (double) activeCount / maxPoolSize * 100;
        if (utilizationRate > poolUtilizationThreshold) {
            log.warn("High thread pool utilization: {}%", String.format("%.2f", utilizationRate));
        }

        double queueUtilization = (double) queueSize /
                (queueSize + executor.getThreadPoolExecutor().getQueue().remainingCapacity()) * 100;
        if (queueUtilization > queueUtilizationThreshold) {
            log.warn("High queue utilization: {}%", String.format("%.2f", queueUtilization));
        }
    }
}