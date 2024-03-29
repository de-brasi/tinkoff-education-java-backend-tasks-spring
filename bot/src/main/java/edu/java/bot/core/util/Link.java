package edu.java.bot.core.util;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.springframework.http.HttpStatusCode;

public class Link {
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
