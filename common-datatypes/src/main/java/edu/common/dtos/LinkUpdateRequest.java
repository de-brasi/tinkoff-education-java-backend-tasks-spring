package edu.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkUpdateRequest {
    int id;
    String url;
    String description;
    List<Integer> tgChatIds;
}
