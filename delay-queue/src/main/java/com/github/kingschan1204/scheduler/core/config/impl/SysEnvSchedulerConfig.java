package com.github.kingschan1204.scheduler.core.config.impl;

import com.github.kingschan1204.scheduler.core.config.SchedulerConfig;
import lombok.Getter;

/**
 * @author kingschan
 */

public class SysEnvSchedulerConfig implements SchedulerConfig {
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


    public SysEnvSchedulerConfig() {
        engine = System.getProperty("scheduler.engine", "com.github.kingschan1204.scheduler.core.impl.MemoryTaskScheduler");
        poolName = System.getProperty("scheduler.poolName", "my pool");
        rateLimiter = Integer.parseInt(System.getProperty("scheduler.rateLimiter", "6"));
        redisHost = System.getProperty("scheduler.redisPort", "localhost");
        redisPort = Integer.parseInt(System.getProperty("scheduler.port", "6379"));
        redisPassword = System.getProperty("scheduler.redisPassword", "");
        queueName = System.getProperty("scheduler.queueName", "task-queue");
    }

    @Override
    public String engine() {
        return this.engine;
    }

    @Override
    public String poolName() {
        return this.poolName;
    }

    @Override
    public int rateLimiter() {
        return this.rateLimiter;
    }

    @Override
    public String redisHost() {
        return this.redisHost;
    }

    @Override
    public int redisPort() {
        return this.redisPort;
    }

    @Override
    public String redisPassword() {
        return this.redisPassword;
    }

    @Override
    public String queueName() {
        return this.queueName;
    }

    /*private static class Holder {
        private static SingletonSchedulerConfig instance = new SingletonSchedulerConfig();
    }

    public static SingletonSchedulerConfig getInstance() {
        return Holder.instance;
    }*/
}
