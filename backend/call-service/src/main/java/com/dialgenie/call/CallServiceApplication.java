package com.dialgenie.call;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"com.dialgenie"})
@EnableScheduling
@EnableAsync
public class CallServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CallServiceApplication.class, args);
    }
}
