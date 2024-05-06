package edu.java.bot.configuration;

import edu.common.ratelimiting.RequestRateSupervisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {
    @Bean
    RequestRateSupervisor requestRateSupervisor() {
        return new RequestRateSupervisor();
    }
}
