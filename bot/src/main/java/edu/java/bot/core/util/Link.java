package edu.java.bot.core.util;

import org.springframework.http.HttpStatusCode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class Link {
    private static final int BAD_HTTP_RESPONSE_UPPER_BOUND = 500;
    private static final int BAD_HTTP_RESPONSE_LOWER_BOUND = 400;

    protected Link() {}

    public static boolean validate(String urlString) {
        try {
            URL url = URI.create(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            return HttpStatusCode.valueOf(responseCode).is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
