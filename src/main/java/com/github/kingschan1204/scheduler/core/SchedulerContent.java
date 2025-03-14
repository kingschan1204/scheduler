package com.github.kingschan1204.scheduler.core;

import com.github.kingschan1204.scheduler.core.impl.MemoryTaskScheduler;
import com.github.kingschan1204.scheduler.core.impl.RedissonTaskScheduler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kingschan
 */
@Slf4j
public class SchedulerContent {
    private final int core = Runtime.getRuntime().availableProcessors();
//    private TaskScheduler scheduler = new MemoryTaskScheduler(core, 2 * core);
    private TaskScheduler scheduler = new RedissonTaskScheduler("localhost", 6379);

    private SchedulerContent() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("Scheduler is shutting down...");
            scheduler.shutdown();
        }));
    }


    private static class Holder {
        private static SchedulerContent instance = new SchedulerContent();
    }

    public static SchedulerContent getInstance() {
        return Holder.instance;
    }

    public void addTask(Task task) {
        scheduler.addTask(task);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
