package org.overcode250204.warehouseservice;

import org.overcode250204.exception.GlobalExceptionHandler;
import org.overcode250204.warehouseservice.model.entities.Configuration;
import org.overcode250204.warehouseservice.model.entities.Instrument;
import org.overcode250204.warehouseservice.model.entities.Reagent;
import org.overcode250204.warehouseservice.model.enums.Status;
import org.overcode250204.warehouseservice.repositories.ConfigurationRepository;
import org.overcode250204.warehouseservice.repositories.InstrumentsRepository;
import org.overcode250204.warehouseservice.repositories.ReagentsRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableScheduling
@Import(GlobalExceptionHandler.class)
public class WarehouseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseServiceApplication.class, args);
    }
}
