package com.github.kingschan1204.scheduler.core;

import com.github.kingschan1204.scheduler.core.task.Task;

/**
 * @author kingschan
 * 2025-03-09
 */
public interface TaskScheduler {
    void addTask(Task task);
    void shutdown();
}
