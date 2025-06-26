package com.github.kingschan1204.scheduler.core;

/**
 * @author kingschan
 */

import java.util.concurrent.*;

public class RateLimiter {
    final int limit;
    final Semaphore semaphore;
    final ScheduledExecutorService scheduler;

    public RateLimiter(int limit) {
        this.limit = limit;
        this.semaphore = new Semaphore(limit);
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // 每秒定时重置许可数
        scheduler.scheduleAtFixedRate(() -> {
            int drained = semaphore.drainPermits();
            // 确保补充到limit个许可
            semaphore.release(limit - drained);
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void execute(Runnable task) {
        try {
            semaphore.acquire(); // 获取许可（阻塞直到可用）
            task.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 不需要手动释放，许可由定时任务重置
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }


}
