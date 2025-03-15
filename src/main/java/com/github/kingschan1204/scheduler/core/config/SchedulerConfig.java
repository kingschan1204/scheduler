package com.github.kingschan1204.scheduler.core.config;

import lombok.Getter;

/**
 * @author kingschan
 */

public class SchedulerConfig {
    @Getter
    private String engine;
    @Getter
    private String poolName;
    @Getter
    private int rateLimiter;

    @Getter
    private String redisHost;
    @Getter
    private int redisPort;
    @Getter
    private String redisPassword;
    @Getter
    private String queueName;


    private SchedulerConfig() {
        engine = System.getProperty("scheduler.engine", "com.github.kingschan1204.scheduler.core.impl.MemoryTaskScheduler");
        poolName = System.getProperty("scheduler.poolName", "my pool");
        rateLimiter = Integer.parseInt(System.getProperty("scheduler.rateLimiter", "6"));
        redisHost = System.getProperty("scheduler.redisPort", "localhost");
        redisPort = Integer.parseInt(System.getProperty("scheduler.port", "6379"));
        redisPassword = System.getProperty("scheduler.redisPassword", "");
        queueName = System.getProperty("scheduler.queueName", "task-queue");
    }

    private static class Holder {
        private static SchedulerConfig instance = new SchedulerConfig();
    }

    public static SchedulerConfig getInstance() {
        return Holder.instance;
    }
}
