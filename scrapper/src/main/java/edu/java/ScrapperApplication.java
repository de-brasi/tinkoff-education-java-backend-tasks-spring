package edu.java;

import edu.java.configuration.ApplicationConfig;
import edu.java.configuration.GitHubClientSettings;
import edu.java.configuration.StackOverflowClientSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    ApplicationConfig.class,
    GitHubClientSettings.class,
    StackOverflowClientSettings.class
})
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
