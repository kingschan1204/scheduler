package com.github.kingschan1204.scheduler.core.impl;

import com.github.kingschan1204.scheduler.core.RateLimiter;
import com.github.kingschan1204.scheduler.core.config.SchedulerConfig;
import com.github.kingschan1204.scheduler.core.task.Task;
import com.github.kingschan1204.scheduler.core.TaskScheduler;
import com.github.kingschan1204.scheduler.core.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.*;

/**
 * 使用 Redisson 延迟队列实现的任务调度器
 * @author kingschan
 */
@Slf4j
public class RedissonTaskScheduler implements TaskScheduler {

    // 目标队列和延迟队列的键名
    private static final String TARGET_QUEUE_KEY = SchedulerConfig.getInstance().getQueueName();

    // Redisson 客户端实例
    private final RedissonClient redissonClient;
    // 延迟队列和目标队列
    private final RDelayedQueue<String> delayedQueue;
    private final RBlockingQueue<String> targetQueue;
    // 工作线程池
    private final ThreadPoolExecutor workerPool;
    // 频率控制信号量
    private final int MAX_REQUESTS_PER_SECOND = SchedulerConfig.getInstance().getRateLimiter();
    private final RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_SECOND);
    // 队列监听线程池
    private final ExecutorService queueListener = Executors.newSingleThreadExecutor();

    public RedissonTaskScheduler() {
        // Redisson 配置
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + SchedulerConfig.getInstance().getRedisHost() + ":" + SchedulerConfig.getInstance().getRedisPort())
                .setConnectionPoolSize(20)
                .setConnectionMinimumIdleSize(10);

        this.redissonClient = Redisson.create(config);

        // 初始化队列
        this.targetQueue = redissonClient.getBlockingQueue(TARGET_QUEUE_KEY);
        this.delayedQueue = redissonClient.getDelayedQueue(targetQueue);

        // 初始化工作线程池
        int core = Runtime.getRuntime().availableProcessors();
        workerPool = new ThreadPoolExecutor(
                core,
                2 * core,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadFactoryBuilder(SchedulerConfig.getInstance().getPoolName())
        );

        // 启动队列监听
        startQueueListener();
    }

    @Override
    public void addTask(Task task) {
        // 计算延迟时间
        long delay = task.getNextRunTime() - System.currentTimeMillis();
        // 添加到延迟队列（时间单位转换为秒）
        delayedQueue.offer(task.getId(), Math.max(0, delay), TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        // 停止队列监听
        queueListener.shutdown();
        // 关闭延迟队列
        delayedQueue.destroy();
        // 关闭工作线程池
        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
        }
        // 关闭Redisson客户端
        redissonClient.shutdown();
        //关闭限流器
        rateLimiter.shutdown();

    }

    /**
     * 启动队列监听线程
     */
    private void startQueueListener() {
        queueListener.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 阻塞获取队列任务
                    String taskId = targetQueue.take();

                    workerPool.execute(() -> {
                        rateLimiter.execute(()->{
                            log.info("{}",taskId);

                        });
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
}