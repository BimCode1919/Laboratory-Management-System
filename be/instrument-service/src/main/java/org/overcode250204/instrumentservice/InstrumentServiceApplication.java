package org.overcode250204.instrumentservice;

import org.overcode250204.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Import(GlobalExceptionHandler.class)
public class InstrumentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstrumentServiceApplication.class, args);
    }

}
