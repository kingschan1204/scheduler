package com.github.kingschan1204;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SchedulerApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(SchedulerApplication.class);
        app.run(args);

    }

}
