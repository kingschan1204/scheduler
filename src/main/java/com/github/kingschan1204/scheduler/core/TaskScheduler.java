package com.github.kingschan1204.scheduler.core;

import com.github.kingschan1204.scheduler.core.task.TaskDataMap;

/**
 * @author kingschan
 * 2025-03-09
 */
public interface TaskScheduler {
    void addTask(TaskDataMap taskDataMap) throws Exception;
    void shutdown();
}
