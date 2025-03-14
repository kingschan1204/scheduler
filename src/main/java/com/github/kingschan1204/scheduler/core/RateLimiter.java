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
            semaphore.release(limit - drained); // 确保补充到limit个许可
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void execute(Runnable task) {
        try {
            semaphore.acquire(); // 获取许可（阻塞直到可用）
            task.run();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 不需要手动释放，许可由定时任务重置
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter(2);
        ExecutorService executor = Executors.newCachedThreadPool();

        // 模拟20个请求
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            executor.execute(() -> {
                try {
                    limiter.execute(() -> {
                        System.out.println("任务 " + taskId + " 执行于: " + System.currentTimeMillis() / 1000);
                    });
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        limiter.shutdown();
    }
}
