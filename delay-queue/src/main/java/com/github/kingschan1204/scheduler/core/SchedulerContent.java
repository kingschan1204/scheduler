package com.github.kingschan1204.scheduler.core;

import com.github.kingschan1204.scheduler.core.config.SchedulerConfig;
import com.github.kingschan1204.scheduler.core.task.Task;
import com.github.kingschan1204.scheduler.core.task.TaskDataMap;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kingschan
 */
@Slf4j
public class SchedulerContent {
    private TaskScheduler scheduler;

    private SchedulerContent() {
        try {
            Class<?> clazz = Class.forName(SchedulerConfig.getInstance().getEngine());

            // 创建对象
            Object obj = clazz.getDeclaredConstructor().newInstance();
            scheduler = (TaskScheduler) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void addTask(TaskDataMap taskdata) {
        try{
            scheduler.addTask(taskdata);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
