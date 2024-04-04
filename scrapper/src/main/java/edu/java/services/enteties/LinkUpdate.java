package edu.java.services.enteties;

import java.util.List;

public record LinkUpdate(
    int linkId,
    String url,
    String updateDescription,
    List<Long> subscribers
) { }
