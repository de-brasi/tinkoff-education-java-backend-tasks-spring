package edu.java.bot.api.dtos;

import java.util.List;
import lombok.Data;

@Data
public class LinkUpdateRequest {
    int id;
    String url;
    String description;
    List<Integer> tgChatIds;
}
