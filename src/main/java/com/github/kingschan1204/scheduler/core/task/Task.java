package com.github.kingschan1204.scheduler.core.task;

import com.github.kingschan1204.scheduler.core.SchedulerContent;
import com.github.kingschan1204.scheduler.core.cron.CronHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author kingschan
 */
@Slf4j
public abstract class Task implements Delayed, Runnable, Serializable {
    //    protected final long interval;     // 任务执行间隔（毫秒）
    @Getter
    protected long nextRunTime;        // 下一次执行时间（动态更新）
    @Getter
    protected final TaskDataMap taskDataMap;

    public Task(TaskDataMap taskDataMap) {
        this.taskDataMap = taskDataMap;
        Date date = CronHelper.getNextValidTime(taskDataMap.getCron());
        this.nextRunTime = date.getTime();
    }

    public abstract void execute() throws Exception;

    @Override
    public void run() {
        try {
            execute();
            //如果是周期任务，则加入队列中
            if(null != CronHelper.getNextValidTime(taskDataMap.getCron())){
                SchedulerContent.getInstance().addTask(taskDataMap);
            }
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
        if (taskDataMap.getCurrentCount() >= 3) {
            log.error("任务错误次数过多，放弃执行");
            return;
        }
        taskDataMap.setCurrentCount(taskDataMap.getCurrentCount() + 1);
        // 计算退避时间（interval * 2^retryCount），上限设为1小时
        long maxBackoff = 3600_000;  // 1小时
        long backoffTime = (long) Math.min(1000 * 30 * Math.pow(2, taskDataMap.getRetryCount()), maxBackoff);
        // 更新下一次执行时间
        this.nextRunTime = System.currentTimeMillis() + backoffTime;
        // 重新加入队列（需处理可能的队列满异常）
        try {
            log.warn("任务执行失败，将在{}秒后重试", backoffTime / 1000);
            SchedulerContent.getInstance().addTask(taskDataMap);
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
