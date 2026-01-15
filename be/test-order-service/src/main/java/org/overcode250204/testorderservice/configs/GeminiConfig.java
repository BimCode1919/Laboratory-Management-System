package org.overcode250204.testorderservice.configs;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Bean
    public Client geminiClient() {
        return new Client(); // it automatically finds the api key in .env file
    }
}
