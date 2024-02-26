package edu.java.bot.api.dtos;

import lombok.Data;
import java.util.List;

@Data
public class LinkUpdateRequest {
    int id;
    String url;
    String description;
    List<Integer> tgChatIds;
}
