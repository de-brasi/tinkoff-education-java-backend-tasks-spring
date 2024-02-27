package edu.java.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ListLinksResponse {
    List<LinkResponse> links;
    int size;
}
