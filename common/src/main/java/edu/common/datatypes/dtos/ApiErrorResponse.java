package edu.common.datatypes.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorResponse {
    String description;
    String code;
    String exceptionName;
    String exceptionMessage;
    List<String> stacktrace;
}
