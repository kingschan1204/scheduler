package com.github.kingschan1204.scheduler.core;

import com.github.kingschan1204.scheduler.core.config.SchedulerConfig;
import com.github.kingschan1204.scheduler.core.task.TaskDataMap;

/**
 * @author kingschan
 * 2025-03-09
 */
public interface TaskScheduler {
    default SchedulerConfig getSchedulerConfig(){
        return null;
    }
    void addTask(TaskDataMap taskDataMap) throws Exception;
    void shutdown();
}
