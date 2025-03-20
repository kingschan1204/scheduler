package com.github.kingschan1204.scheduler.test;

import com.github.kingschan1204.scheduler.core.task.Task;
import com.github.kingschan1204.scheduler.core.task.TaskDataMap;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public
class TestTask extends Task {


    public TestTask(TaskDataMap taskDataMap) {
        super(taskDataMap);
    }

    @Override
    public void execute() throws Exception {
//        if (null == taskDataMap.getData()) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("count", 0);
//            taskDataMap.setData(map);
//        } else {
//            Map<String, Object> map = taskDataMap.getData();
//            int count = (int) map.get("count");
//            count++;
//            map.put("count", count);
//            taskDataMap.setData(map);
//            if (count == 3) {
//                jobdone = true;
//            }
//        }
        log.info("{}", taskDataMap);
    }
//        throw new RuntimeException("error test");


}
