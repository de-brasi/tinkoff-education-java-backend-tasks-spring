package edu.java.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WebClientProperties {
    int timeoutInMilliseconds;
    String baseUrl;
}
