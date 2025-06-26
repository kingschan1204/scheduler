package com.github.kingschan1204.scheduler.core.context;

import com.github.kingschan1204.scheduler.core.task.TaskDataMap;

public interface SchedulerContext {
    void addTask(TaskDataMap taskdata);

    void shutdown();
}
