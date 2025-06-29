package com.github.kingschan1204.scheduler.test;

import com.github.kingschan1204.scheduler.core.config.SchedulerConfig;
import com.github.kingschan1204.scheduler.core.config.impl.SysEnvSchedulerConfig;
import com.github.kingschan1204.scheduler.core.context.SchedulerContext;
import com.github.kingschan1204.scheduler.core.context.impl.DefaultSchedulerContext;
import com.github.kingschan1204.scheduler.core.task.TaskDataMap;
import lombok.extern.slf4j.Slf4j;


/**
 * @author kingschan
 */
@Slf4j
public class CodeTest {

    static void set() {
//        System.setProperty("scheduler.engine", "com.github.kingschan1204.scheduler.core.impl.MemoryTaskScheduler");
        System.setProperty("scheduler.engine", "com.github.kingschan1204.scheduler.core.impl.RedissonTaskScheduler");
        System.setProperty("scheduler.poolName", "redisTask");
        System.setProperty("scheduler.rateLimiter", "10");
        System.setProperty("scheduler.redisPort", "localhost");
        System.setProperty("scheduler.port", "6379");
        System.setProperty("scheduler.redisPassword", "");
        System.setProperty("scheduler.queueName", "task-queue");
    }

    public static void main(String[] args) {
        set();
        log.info("start...");
        SchedulerConfig schedulerConfig = new SysEnvSchedulerConfig();
        SchedulerContext context = new DefaultSchedulerContext(schedulerConfig);
        context.addTask(new TaskDataMap("com.github.kingschan1204.scheduler.test.TestTask", "0/3 * * * * ?", null));
    }
}
