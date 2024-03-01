package edu.java.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
    String description;
    String code;
    String exceptionName;
    String exceptionMessage;
    List<String> stacktrace;
}
