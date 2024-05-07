package edu.java.bot.api;

import edu.common.datatypes.dtos.ApiErrorResponse;
import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.common.ratelimiting.RequestRateSupervisor;
import edu.java.bot.api.util.UpdateHandler;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/bot/api")
@Slf4j
@RequiredArgsConstructor
public class UpdateController {
    private final UpdateHandler updateHandler;
    private final RequestRateSupervisor requestRateSupervisor;
    private static final ResponseEntity<?> REQUEST_RATE_LIMIT_ACHIEVED_RESPONSE = new ResponseEntity<>(
        new ApiErrorResponse(
            "rate limit", "429", null, null, null
        ),
        HttpStatus.TOO_MANY_REQUESTS
    );

    @PostMapping(value = "/updates", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @SuppressWarnings("RegexpSinglelineJava")
    public ResponseEntity<?> handleUpdateRequest(
        @RequestBody LinkUpdateRequest requestBody,
        HttpServletRequest request
    ) {
        Bucket bucket = requestRateSupervisor.resolveBucket(request.getRemoteAddr());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            return REQUEST_RATE_LIMIT_ACHIEVED_RESPONSE;
        }
        log.info("Got request by http:" + requestBody.toString());
        updateHandler.handleUpdate(requestBody);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
