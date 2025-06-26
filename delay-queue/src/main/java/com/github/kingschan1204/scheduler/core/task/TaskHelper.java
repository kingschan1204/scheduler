package com.github.kingschan1204.scheduler.core.task;

import com.github.kingschan1204.scheduler.core.TaskScheduler;

public class TaskHelper {
    public static Task of(TaskDataMap taskDataMap, TaskScheduler taskScheduler) {
        try{
            String className = taskDataMap.getTaskClass();
            Class<?> clazz = Class.forName(className);
            // 指定构造函数的参数类型
            Class<?>[] parameterTypes = {TaskDataMap.class,TaskScheduler.class};
            // 获取带参数的构造函数
            java.lang.reflect.Constructor<?> constructor = clazz.getConstructor(parameterTypes);
            // 指定构造函数的参数值
            Object[] initArgs = {taskDataMap,taskScheduler};
            // 使用带参数的构造函数实例化对象
            Object instance = constructor.newInstance(initArgs);
            return (Task) instance;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
