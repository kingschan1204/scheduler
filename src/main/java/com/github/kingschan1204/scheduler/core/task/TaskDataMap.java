package com.github.kingschan1204.scheduler.core.task;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * @author kingschan
 */
@ToString
public class TaskDataMap implements Serializable {
    // 任务类
    @Getter
    private final String taskClass;
    // cron表达式
    @Getter
    private final String cron;
    // 重试次数
    @Getter
    private Integer retryCount = 3;
    // 当前重试次数
    @Getter
    @Setter
    private Integer currentCount = 0;
    // 任务参数
    @Getter
    @Setter
    private Map<String, Object> data;

    public TaskDataMap(String taskClass, String cron) {
        this.taskClass = taskClass;
        this.cron = cron;
    }
    public TaskDataMap(String taskClass, String cron, Map<String, Object> data) {
        this.taskClass = taskClass;
        this.cron = cron;
        this.data = data;
    }
    public TaskDataMap(String taskClass, String cron, Map<String, Object> data, Integer retryCount) {
        this.taskClass = taskClass;
        this.cron = cron;
        this.data = data;
        this.retryCount = retryCount;
    }


}
