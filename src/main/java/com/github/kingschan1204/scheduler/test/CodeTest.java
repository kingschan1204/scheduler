package com.github.kingschan1204.scheduler.test;

import com.github.kingschan1204.scheduler.core.SchedulerContent;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



/**
 * @author kingschan
 */
@Slf4j
public class CodeTest {

    static void set() {
//        System.setProperty("scheduler.engine", "com.github.kingschan1204.scheduler.core.impl.MemoryTaskScheduler");
        System.setProperty("scheduler.engine", "com.github.kingschan1204.scheduler.core.impl.RedissonTaskScheduler");
        System.setProperty("scheduler.poolName", "redisTask");
        System.setProperty("scheduler.rateLimiter", "2");
        System.setProperty("scheduler.redisPort", "localhost");
        System.setProperty("scheduler.port", "6379");
        System.setProperty("scheduler.redisPassword", "");
        System.setProperty("scheduler.queueName", "task-queue");
    }

    public static void main(String[] args) {
        set();
        log.info("start...");
        SchedulerContent scheduler = SchedulerContent.getInstance();
        scheduler.addTask(new TestTask(1000));
        // 添加一个定时任务，用于定时添加新任务
        ScheduledExecutorService taskAdder = Executors.newSingleThreadScheduledExecutor();
        taskAdder.scheduleAtFixedRate(() -> {
            scheduler.addTask(new TestTask(1000));
        }, 0, 100, TimeUnit.MILLISECONDS); // 每秒添加一个新任务*/
        // 添加一个延迟任务，用于优雅关闭资源
        /*
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            manager.shutdown();
            taskAdder.shutdown();
        }));*/
    }
}
