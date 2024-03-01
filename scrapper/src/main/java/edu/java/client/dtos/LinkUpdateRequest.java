package edu.java.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LinkUpdateRequest {
    int id;
    String url;
    String description;
    List<Integer> tgChatIds;
}
