package edu.java.api.util;

public class ParserBuilder {
    private final Parser parser;

    public ParserBuilder() {
        parser = new Parser();
    }

    public ParserBuilder field(String fieldName, String searchRegexp) {
        parser.addField(fieldName, searchRegexp);
        return this;
    }

    public Parser build() {
        return parser;
    }

}
