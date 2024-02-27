package edu.java.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LinkResponse {
    long id;
    String url;
}
