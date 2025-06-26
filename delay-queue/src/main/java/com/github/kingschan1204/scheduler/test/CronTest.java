package com.github.kingschan1204.scheduler.test;


import com.github.kingschan1204.scheduler.core.cron.CronExpression;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CronTest {
    public static void main(String[] args) throws Exception {
        CronExpression cron = new CronExpression("0 30 9 ? * 2-6");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        for (int i = 0; i < 10; i++) {
            date = cron.getNextValidTimeAfter(date);
            if (null != date) {
                System.out.println(sdf.format(date));
            } else {
                System.out.println("不会执行了！");
                break;
            }
        }
    }
}
