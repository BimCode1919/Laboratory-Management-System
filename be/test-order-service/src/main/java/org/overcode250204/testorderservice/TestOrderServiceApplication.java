package org.overcode250204.testorderservice;

import org.overcode250204.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@Import(GlobalExceptionHandler.class)
public class TestOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestOrderServiceApplication.class, args);
        System.out.println("Test Order Service Started");
    }

}
