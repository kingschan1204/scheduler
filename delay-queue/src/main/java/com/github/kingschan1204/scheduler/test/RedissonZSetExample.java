package com.github.kingschan1204.scheduler.test;

import org.redisson.Redisson;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.config.Config;

import java.util.Map;

public class RedissonZSetExample {

    public static void main(String[] args) {
        // 创建 Redisson 配置
        Config config = new Config();
        // 这里使用单机模式，根据实际情况修改 Redis 地址
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");

        // 创建 Redisson 客户端
        RedissonClient redisson = Redisson.create(config);

        // 获取 ZSet 对象
        RScoredSortedSet<String> zSet = redisson.getScoredSortedSet("task_delay_queue");

        // 获取当前时间戳（以毫秒为单位）
        long currentTime = System.currentTimeMillis();

        // 查询分数小于等于当前时间的元素
        for (ScoredEntry<String> entry : zSet.entryRange(Double.MIN_VALUE, true, (double) currentTime, true)) {
            double score = entry.getScore();
            String value = entry.getValue();
            System.out.println("Value: " + value + ", Score: " + score);
        }


        // 关闭 Redisson 客户端
        redisson.shutdown();
    }
}