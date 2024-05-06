package edu.common.datatypes.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkUpdateRequest {
    long id;
    String url;
    String description;
    List<Long> tgChatIds;
}
