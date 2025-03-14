package com.github.kingschan1204.scheduler.core.impl;

import com.github.kingschan1204.scheduler.core.Task;
import com.github.kingschan1204.scheduler.core.TaskScheduler;
import com.github.kingschan1204.scheduler.core.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author kingschan
 */
public class MemoryTaskScheduler implements TaskScheduler {
    // 定时任务调度器，用于周期性检查任务队列
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // 工作线程池，执行实际的爬取任务
    private final ThreadPoolExecutor workerPool;
    // 延时任务队列，存放待执行的爬取任务（按执行时间排序）
    private final DelayQueue<Task> taskQueue = new DelayQueue<>();

    // 每秒允许的最大请求次数
    private final int MAX_REQUESTS_PER_SECOND = 10;
    private final Semaphore semaphore = new Semaphore(MAX_REQUESTS_PER_SECOND);

    /**
     * 初始化调度管理器
     *
     * @param corePoolSize 核心线程数（保持活跃的最小线程数）
     * @param maxPoolSize  最大线程数（突发流量时允许扩展到的上限）
     */
    public MemoryTaskScheduler(int corePoolSize, int maxPoolSize) {
        // 配置线程池参数
        workerPool = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,  // 非核心线程空闲60秒后回收
                new LinkedBlockingQueue<>(1000),  // 任务缓冲队列（防止OOM）
                new ThreadFactoryBuilder("my pool")  // 线程命名（便于监控）
        );
        initScheduler();
    }


    /**
     * 初始化定时调度任务：每隔100毫秒从队列中提取一个任务并提交到线程池
     */
    private void initScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // 从队列中取出一个到期的任务（阻塞直到有可用任务）
                Task task = taskQueue.take();
                workerPool.execute(() -> {
                    try {
                        // 获取信号量许可 （控制请求频率）
                        semaphore.acquire();
                        task.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        // 释放信号量许可
                        semaphore.release();
                    }
                });
//                workerPool.submit()
            } catch (InterruptedException e) {
                e.printStackTrace();
                // 正确处理中断（例如关闭应用时）
                Thread.currentThread().interrupt();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);  // 初始延迟0，周期100ms
    }

    @Override
    public void addTask(Task task) {
        taskQueue.put(task);
    }


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
    }
}
