package com.github.kingschan1204.scheduler.test;

import com.github.kingschan1204.scheduler.core.task.Task;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
class TestTask extends Task {
    public TestTask(long interval) {
        super(interval);
    }

    @Override
    public void execute() throws Exception {
        String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("{}", datetime);
//        throw new RuntimeException("error test");
    }


}
