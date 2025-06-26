package com.github.kingschan1204.scheduler.core.context.impl;

import com.github.kingschan1204.scheduler.core.TaskScheduler;
import com.github.kingschan1204.scheduler.core.config.SchedulerConfig;
import com.github.kingschan1204.scheduler.core.context.SchedulerContext;
import com.github.kingschan1204.scheduler.core.task.TaskDataMap;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kingschan
 */
@Slf4j
public class DefaultSchedulerContext implements SchedulerContext {
    private TaskScheduler scheduler;
    final SchedulerConfig schedulerConfig;

    public DefaultSchedulerContext(SchedulerConfig schedulerConfig) {
        this.schedulerConfig = schedulerConfig;
        try {
            Class<?> clazz = Class.forName(schedulerConfig.engine());
            // 指定构造函数的参数类型
            Class<?>[] parameterTypes = {SchedulerConfig.class};
            // 获取带参数的构造函数
            java.lang.reflect.Constructor<?> constructor = clazz.getConstructor(parameterTypes);
            // 指定构造函数的参数值
            Object[] initArgs = {schedulerConfig};
            // 创建对象
            Object obj = constructor.newInstance(initArgs);
            scheduler = (TaskScheduler) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("Scheduler is shutting down...");
            scheduler.shutdown();
        }));
    }

//
//    private static class Holder {
//        private static SingletonSchedulerContext instance = new SingletonSchedulerContext(schedulerConfig);
//
//    }
//
//    public static SingletonSchedulerContext getInstance() {
//        return Holder.instance;
//    }

    @Override
    public void addTask(TaskDataMap taskdata) {
        try{
            scheduler.addTask(taskdata);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void shutdown() {
        scheduler.shutdown();
    }
}
