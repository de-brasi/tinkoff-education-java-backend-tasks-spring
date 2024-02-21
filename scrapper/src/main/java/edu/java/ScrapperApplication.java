package edu.java;

import edu.java.clients.GitHubClient;
import edu.java.clients.StackOverflowClient;
import edu.java.configuration.ApplicationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import java.lang.reflect.InvocationTargetException;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationConfig.class)
public class ScrapperApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(ScrapperApplication.class, args);

        // бин gitHubClient из контекста
        GitHubClient githubClient =
            context.getBean("gitHubClient", GitHubClient.class);

        // результат
        System.out.println("GitHub client: " + githubClient.fetchUpdate("de-brasi", "tinkoff-education-java-backend-tasks-spring"));

        StackOverflowClient stackoverflowClient =
            context.getBean("stackOverflowClient", StackOverflowClient.class);
        System.out.println("StackoverflowClient: " + stackoverflowClient.fetchUpdate(41808152));

        context.close();
    }
}
