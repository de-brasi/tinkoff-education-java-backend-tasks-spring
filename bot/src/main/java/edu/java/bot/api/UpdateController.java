package edu.java.bot.api;

import edu.common.dtos.ApiErrorResponse;
import edu.common.dtos.LinkUpdateRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/bot/api")
public class UpdateController {
    @PostMapping(value = "/updates", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @SuppressWarnings("RegexpSinglelineJava")
    public ResponseEntity<ApiErrorResponse> handleUpdateRequest(@RequestBody LinkUpdateRequest request) {
        LOGGER.info(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private final static Logger LOGGER = LogManager.getLogger();
}
