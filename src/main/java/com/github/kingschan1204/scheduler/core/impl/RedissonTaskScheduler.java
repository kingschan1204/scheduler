package com.github.kingschan1204.scheduler.core.impl;

import com.github.kingschan1204.scheduler.core.Task;
import com.github.kingschan1204.scheduler.core.TaskScheduler;
import com.github.kingschan1204.scheduler.core.ThreadFactoryBuilder;
import org.redisson.Redisson;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.config.Config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 使用 Redisson 实现的任务调度器，借助 Redis 的有序集合来存储任务并实现延迟调度功能
 * @author kingschan
 */
public class RedissonTaskScheduler implements TaskScheduler {
    // Redis 有序集合的键名，用于存储任务调度信息
    private static final String DELAY_QUEUE_KEY = "task_delay_queue";
    // Redisson 客户端实例，用于与 Redis 进行交互
    private final RedissonClient redissonClient;
    // 线程池，用于执行任务调度的检查逻辑
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // 工作线程池，执行实际的爬取任务
    private final ThreadPoolExecutor workerPool;
    // 每秒允许的最大请求次数
    private final int MAX_REQUESTS_PER_SECOND = 1;
    private final Semaphore semaphore = new Semaphore(MAX_REQUESTS_PER_SECOND);

    /**
     * 构造函数，初始化 Redisson 客户端和线程池，并启动任务调度检查线程
     * @param host Redis 服务器的主机名
     * @param port Redis 服务器的端口号
     */
    public RedissonTaskScheduler(String host, int port) {
        // 创建 Redisson 配置对象
        Config config = new Config();

        // 配置 Redisson 连接到指定的 Redis 单节点服务器
        config.useSingleServer().setAddress("redis://" + host + ":" + port)
                .setConnectionPoolSize(20) // 设置最大连接数
                .setConnectionMinimumIdleSize(10); // 设置最小空闲连接数;
        // 根据配置创建 Redisson 客户端实例
        this.redissonClient = Redisson.create(config);
        int core = Runtime.getRuntime().availableProcessors();
        workerPool = new ThreadPoolExecutor(
                core,
                2*core,
                60L, TimeUnit.SECONDS,  // 非核心线程空闲60秒后回收
                new LinkedBlockingQueue<>(1000),  // 任务缓冲队列（防止OOM）
                new ThreadFactoryBuilder("redis pool")  // 线程命名（便于监控）
        );
        // 启动任务调度检查线程
        startScheduler();
    }

    /**
     * 实现 TaskScheduler 接口的方法，将任务添加到 Redis 的有序集合中
     * @param task 要添加的任务
     */
    @Override
    public void addTask(Task task) {
        // 从 Redisson 客户端获取有序集合对象
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(DELAY_QUEUE_KEY);
        // 将任务的哈希码作为成员，任务的下一次运行时间作为分数添加到有序集合中
        sortedSet.add(task.getNextRunTime(), String.valueOf(task.hashCode()));
    }

    /**
     * 实现 TaskScheduler 接口的方法，关闭线程池和 Redisson 客户端
     */
    @Override
    public void shutdown() {
        scheduler.shutdown();  // 停止定时调度器
        workerPool.shutdown(); // 停止线程池（不再接受新任务）
        try {
            // 等待现有任务完成，最多60秒
            if (!workerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                workerPool.shutdownNow(); // 强制终止未完成的任务
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
        }
        // 关闭 Redisson 客户端，释放与 Redis 的连接
        redissonClient.shutdown();
    }

    /**
     * 启动任务调度检查线程，定期检查有序集合中是否有到期的任务
     */
    private void startScheduler() {

        scheduler.scheduleAtFixedRate(() -> {
            try {
                // 从 Redisson 客户端获取有序集合对象
                RScoredSortedSet<String> zSet = redissonClient.getScoredSortedSet(DELAY_QUEUE_KEY);

                // 获取当前时间
                long currentTime = System.currentTimeMillis();
//                ScoredEntry<String> sets = (ScoredEntry<String>) sortedSet.entryRange(Double.MIN_VALUE, true, (double) currentTime, true);
//                fromScore：指定分数范围的起始分数
//                fromInclusive：一个布尔值，用于指定是否包含起始分数。如果为 true，则包含起始分数；如果为 false，则不包含起始分数
//                toScore：指定分数范围的结束分数
//                toInclusive：一个布尔值，用于指定是否包含结束分数
//                Iterable<Map.Entry<Double, V>> entryRange(double fromScore, boolean fromInclusive, double toScore, boolean toInclusive);


                // 获取分数在 0 到 现在时间戳 之间（包含边界）的元素及其分数  获取过期或等于当前时间的任务
                Collection<ScoredEntry<String>> entries = zSet.entryRange(0, true, currentTime, true);

                for (ScoredEntry<String> entry :  entries) {
                    double score = entry.getScore();
                    String value = entry.getValue();


                    workerPool.execute(() -> {
                        try {
                            // 获取信号量许可 （控制请求频率）
                            semaphore.acquire();
                            System.out.println("Value: " + value + ", Score: " + score);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            // 释放信号量许可
                            semaphore.release();
                        }
                    });

                    zSet.remove(value);
                }

//                workerPool.submit()
            } catch (Exception e) {
                e.printStackTrace();
                // 正确处理中断（例如关闭应用时）
                Thread.currentThread().interrupt();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);  // 初始延迟0，周期100ms


       /* // 向线程池提交一个任务调度检查任务
        executorService.submit(() -> {
            while (true) {
                // 从 Redisson 客户端获取有序集合对象
                RScoredSortedSet<String> zSet = redissonClient.getScoredSortedSet(DELAY_QUEUE_KEY);

                // 获取当前时间
                long currentTime = System.currentTimeMillis();
//                ScoredEntry<String> sets = (ScoredEntry<String>) sortedSet.entryRange(Double.MIN_VALUE, true, (double) currentTime, true);
//                fromScore：指定分数范围的起始分数
//                fromInclusive：一个布尔值，用于指定是否包含起始分数。如果为 true，则包含起始分数；如果为 false，则不包含起始分数
//                toScore：指定分数范围的结束分数
//                toInclusive：一个布尔值，用于指定是否包含结束分数
//                Iterable<Map.Entry<Double, V>> entryRange(double fromScore, boolean fromInclusive, double toScore, boolean toInclusive);


                // 获取分数在 0 到 现在时间戳 之间（包含边界）的元素及其分数  获取过期或等于当前时间的任务
                Collection<ScoredEntry<String>> entries = zSet.entryRange(0, true, currentTime, true);

                for (ScoredEntry<String> entry :  entries) {
                    double score = entry.getScore();
                    String value = entry.getValue();
                    System.out.println("Value: " + value + ", Score: " + score);
                    zSet.remove(value);
                }
                try {
                    // 线程休眠 100 毫秒，避免过于频繁地检查
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // 若线程休眠过程中被中断，恢复中断状态
                    Thread.currentThread().interrupt();
                }
            }
        });*/
    }
}