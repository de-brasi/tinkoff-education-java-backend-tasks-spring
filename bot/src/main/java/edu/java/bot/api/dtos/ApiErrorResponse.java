package edu.java.bot.api.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
    String description;
    String code;
    String exceptionName;
    String exceptionMessage;
    List<String> stacktrace;
}
