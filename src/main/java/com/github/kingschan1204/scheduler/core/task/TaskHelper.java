package com.github.kingschan1204.scheduler.core.task;

public class TaskHelper {
    public static Task of(TaskDataMap taskDataMap) {
        try{
            String className = taskDataMap.getTaskClass();
            Class<?> clazz = Class.forName(className);
            // 指定构造函数的参数类型
            Class<?>[] parameterTypes = {TaskDataMap.class};
            // 获取带参数的构造函数
            java.lang.reflect.Constructor<?> constructor = clazz.getConstructor(parameterTypes);
            // 指定构造函数的参数值
            Object[] initArgs = {taskDataMap};
            // 使用带参数的构造函数实例化对象
            Object instance = constructor.newInstance(initArgs);
            return (Task) instance;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
