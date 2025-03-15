package com.github.kingschan1204.scheduler.core.task;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author kingschan
 */
@Data
public class TaskDataMap implements Serializable {
    private String taskClass;
    private Map<String, Objects> data;
}
