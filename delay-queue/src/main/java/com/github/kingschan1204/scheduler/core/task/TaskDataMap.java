package com.github.kingschan1204.scheduler.core.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author kingschan
 */
@ToString
public class TaskDataMap {
    // 任务类
    @JsonProperty("taskClass")
    @Getter
    private final String taskClass;
    // cron表达式
    @JsonProperty("cron")
    @Getter
    private final String cron;
    // 重试次数
    @JsonProperty("retryCount")
    @Getter
    private Integer retryCount = 3;
    // 当前重试次数
    @JsonProperty("currentCount")
    @Getter
    @Setter
    private Integer currentCount = 0;
    // 任务参数
    @JsonProperty("data")
    @Getter
    @Setter
    private Map<String, Object> data;


    public TaskDataMap( String taskClass, String cron) {
        this.taskClass = taskClass;
        this.cron = cron;
    }
    @JsonCreator
    public TaskDataMap(@JsonProperty("taskClass") String taskClass,@JsonProperty("cron") String cron,@JsonProperty("data") Map<String, Object> data) {
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
