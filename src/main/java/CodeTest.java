import com.github.kingschan1204.scheduler.core.SchedulerContent;
import com.github.kingschan1204.scheduler.core.Task;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Slf4j
class TestTask extends Task {
    public TestTask(long interval) {
        super(interval);
    }

    @Override
    protected void execute() throws Exception {
        String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("{}", datetime);
//        throw new RuntimeException("error test");
    }


}

/**
 * @author kingschan
 */
@Slf4j
public class CodeTest {


    public static void main(String[] args) {

        log.info("start...");
        SchedulerContent scheduler = SchedulerContent.getInstance();
        scheduler.addTask(new TestTask(1000));
        // 添加一个定时任务，用于定时添加新任务
       /* ScheduledExecutorService taskAdder = Executors.newSingleThreadScheduledExecutor();
        taskAdder.scheduleAtFixedRate(() -> {
            manager.addTask(100);
        }, 0, 10, TimeUnit.MILLISECONDS); // 每秒添加一个新任务

        // 添加一个延迟任务，用于优雅关闭资源
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            manager.shutdown();
            taskAdder.shutdown();
        }));*/
    }
}
