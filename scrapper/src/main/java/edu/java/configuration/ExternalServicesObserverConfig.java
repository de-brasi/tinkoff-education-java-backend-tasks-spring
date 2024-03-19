package edu.java.configuration;

import edu.java.clients.ExternalServiceClient;
import edu.java.services.ExternalServicesObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class ExternalServicesObserverConfig {
    @Bean("externalServicesObserver")
    ExternalServicesObserver externalServicesObserver(
        @Autowired
        @Qualifier("gitHubClient")
        ExternalServiceClient githubClient,

        @Autowired
        @Qualifier("stackOverflowClient")
        ExternalServiceClient stackoverflowClient
    ) {
        return new ExternalServicesObserver(List.of(githubClient, stackoverflowClient));
    }
}