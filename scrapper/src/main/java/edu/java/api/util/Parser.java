package edu.java.api.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public class Parser {

    private final Map<String, Pattern> supportedPatterns;

    public Parser() {
        supportedPatterns = new HashMap<>();
    }

    public static ParserBuilder builder() {
        return new ParserBuilder();
    }

    public void addField(String field, String searchRegexp) {
        supportedPatterns.put(field, Pattern.compile(searchRegexp));
    }

    public @Nullable String retrieveValueOfField(String field, String toParse) {
        Pattern pattern = supportedPatterns.get(field);

        if (pattern == null) {
            return null;
        }

        Matcher matcher = pattern.matcher(toParse);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }

}
