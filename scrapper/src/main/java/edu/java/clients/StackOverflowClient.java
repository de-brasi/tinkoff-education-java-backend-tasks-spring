package edu.java.clients;

import edu.java.entities.UpdateResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ReactorNettyClientRequestFactory;
import org.springframework.web.client.RestClient;

public class StackOverflowClient  {
    private final RestClient restClient;

    private final static String defaultBaseUrl =
        "https://api.stackexchange.com/2.3/questions/";

    public StackOverflowClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.requestFactory(new ReactorNettyClientRequestFactory()).baseUrl(StackOverflowClient.defaultBaseUrl).build();
    }

    public StackOverflowClient(RestClient.Builder restClientBuilder, String baseUrl) {
        this.restClient = restClientBuilder.requestFactory(new ReactorNettyClientRequestFactory()).baseUrl(baseUrl).build();
    }

    public String getAllInfo(String questionId) {
        return this.restClient
            .get()
            .uri("/%s?site=stackoverflow&filter=withbody".formatted(questionId))
            .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
            .retrieve()
            .body(String.class);
    }

    public String getAllInfo(Integer questionId) {
        return this.getAllInfo(Integer.toString(questionId));
    }

    // TODO
    public UpdateResponse fetchUpdate() {
        return null;
    }
}
