package edu.java.bot.api;

import edu.java.bot.api.dtos.LinkUpdateRequest;
import edu.java.bot.api.dtos.ApiErrorResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/bot/api")
public class Controllers {
    @PostMapping(value = "/updates", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiErrorResponse> handleUpdateRequest(@RequestBody LinkUpdateRequest request) {
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }

}
