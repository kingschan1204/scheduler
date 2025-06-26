package com.github.kingschan1204.scheduler.core.config;


public interface SchedulerConfig {
    String engine ();
    String poolName ();
    int rateLimiter ();

    String redisHost ();
    int redisPort ();
    String redisPassword ();
    String queueName ();
}
