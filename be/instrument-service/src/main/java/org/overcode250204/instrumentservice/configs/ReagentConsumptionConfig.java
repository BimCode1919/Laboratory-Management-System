package org.overcode250204.instrumentservice.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "reagent")
public class ReagentConsumptionConfig {
    private Map<String, Map<String, Double>> consumption;
}