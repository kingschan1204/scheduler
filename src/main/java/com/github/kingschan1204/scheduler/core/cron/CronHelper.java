package com.github.kingschan1204.scheduler.core.cron;

import java.util.Date;

public class CronHelper {
    public static Date getNextValidTime(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            Date date = cron.getNextValidTimeAfter(new Date());
            return date;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
