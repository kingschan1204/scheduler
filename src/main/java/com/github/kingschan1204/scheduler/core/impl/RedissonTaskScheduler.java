package com.github.kingschan1204.scheduler.core.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.kingschan1204.scheduler.core.RateLimiter;
import com.github.kingschan1204.scheduler.core.TaskScheduler;
import com.github.kingschan1204.scheduler.core.ThreadFactoryBuilder;
import com.github.kingschan1204.scheduler.core.config.SchedulerConfig;
import com.github.kingschan1204.scheduler.core.cron.CronExpression;
import com.github.kingschan1204.scheduler.core.task.Task;
import com.github.kingschan1204.scheduler.core.task.TaskDataMap;
import com.github.kingschan1204.scheduler.core.task.TaskHelper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

import java.util.Date;
import java.util.concurrent.*;

/**
 * 使用 Redisson 延迟队列实现的任务调度器
 *
 * @author kingschan
 */
@Slf4j
public class RedissonTaskScheduler implements TaskScheduler {

    // 目标队列和延迟队列的键名
    private static final String TARGET_QUEUE_KEY = SchedulerConfig.getInstance().getQueueName();

    // Redisson 客户端实例
    private final RedissonClient redissonClient;
    // 延迟队列和目标队列
    private final RDelayedQueue<TaskDataMap> delayedQueue;
    private final RBlockingQueue<TaskDataMap> targetQueue;
    // 工作线程池
    private final ThreadPoolExecutor workerPool;
    // 频率控制信号量
    private final int MAX_REQUESTS_PER_SECOND = SchedulerConfig.getInstance().getRateLimiter();
    private final RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_SECOND);
    // 队列监听线程池
    private final ExecutorService queueListener = Executors.newSingleThreadExecutor();

    public RedissonTaskScheduler() {
        ObjectMapper mapper = new ObjectMapper();
        // 禁用所有类型信息（关键！）
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        // 禁用类型推断
        mapper.deactivateDefaultTyping();
        JsonJacksonCodec codec = new JsonJacksonCodec(mapper);
        // Redisson 配置
        Config config = new Config();
        // 设置序列化方式为 JSON
        config.setCodec(codec);
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
    public void addTask(TaskDataMap taskDataMap) throws Exception {
        CronExpression cronExpression = new CronExpression(taskDataMap.getCron());
        Date date = cronExpression.getNextValidTimeAfter(new Date());
        // 计算延迟时间
        long delay = date.getTime() - System.currentTimeMillis();
        // 添加到延迟队列（时间单位转换为秒）
        delayedQueue.offer(taskDataMap, Math.max(0, delay), TimeUnit.MILLISECONDS);
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
            // 检查Redisson客户端是否已经关闭
           /* if (redissonClient != null && !redissonClient.isShutdown()) {
                // 关闭Redisson客户端
                redissonClient.shutdown();
            }*/
        } catch (Exception e) {
            workerPool.shutdownNow();
        }
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
                    TaskDataMap taskDataMap = targetQueue.take();
                    Task task = TaskHelper.of(taskDataMap);
                    workerPool.execute(() -> rateLimiter.execute(task));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
}