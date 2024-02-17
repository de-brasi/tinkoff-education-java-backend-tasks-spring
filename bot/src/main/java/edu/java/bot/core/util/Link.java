package edu.java.bot.core.util;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class Link {
    private final static int BAD_HTTP_RESPONSE_UPPER_BOUND = 500;
    private final static int BAD_HTTP_RESPONSE_LOWER_BOUND = 400;

    protected Link() {}

    public static boolean validate(String urlString) {
        try {
            URL url = URI.create(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            return !(BAD_HTTP_RESPONSE_LOWER_BOUND <= responseCode
                && responseCode < BAD_HTTP_RESPONSE_UPPER_BOUND);
        } catch (Exception e) {
            return false;
        }
    }
}
