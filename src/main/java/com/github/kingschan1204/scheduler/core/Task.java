package com.github.kingschan1204.scheduler.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author kingschan
 */
@Slf4j
public abstract class Task implements Delayed, Runnable, Serializable {

    protected final long interval;     // 任务执行间隔（毫秒）
    @Getter
    protected long nextRunTime;        // 下一次执行时间（动态更新）
    @Getter
    protected int retryCount = 0;      // 失败重试次数（用于指数退避）

    public Task(long interval) {
        this.interval = interval;
        this.nextRunTime = System.currentTimeMillis() + interval;
    }

    public abstract void execute() throws Exception;

    @Override
    public void run() {
        try {
            execute();
        } catch (Exception e) {
            e.printStackTrace();
            handleFailure(e);
        }
    }

    /**
     * 任务失败处理：指数退避重试
     *
     * @param e 异常信息
     */
    private void handleFailure(Exception e) {
        if (retryCount >= 3) {
            log.error("任务错误次数过多，放弃执行");
            return;
        }
        retryCount++;
        // 计算退避时间（interval * 2^retryCount），上限设为1小时
        long maxBackoff = 3600_000;  // 1小时
        long backoffTime = (long) Math.min(interval * Math.pow(2, retryCount), maxBackoff);
        // 更新下一次执行时间
        this.nextRunTime = System.currentTimeMillis() + backoffTime;
        // 重新加入队列（需处理可能的队列满异常）
        try {
            log.warn("任务执行失败，将在{}秒后重试", backoffTime / 1000);
            SchedulerContent.getInstance().addTask(this);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            System.out.println("任务队列已满，丢弃任务");
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long remaining = nextRunTime - System.currentTimeMillis();
        return unit.convert(remaining, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.nextRunTime, ((Task) o).nextRunTime);
    }
}
