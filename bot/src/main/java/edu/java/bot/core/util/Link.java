package edu.java.bot.core.util;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class Link {
    protected Link() {}

    public static boolean validate(String urlString) {
        try {
            URL url = URI.create(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            System.out.println(responseCode);
            return !(400 <= responseCode && responseCode < 500);
        } catch (Exception e) {
            return false;
        }
    }
}
