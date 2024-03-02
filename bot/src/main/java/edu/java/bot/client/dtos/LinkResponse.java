package edu.java.bot.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.net.URI;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkResponse {
    long id;
    URI url;
}
