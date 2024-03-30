package edu.java.api;

import edu.common.datatypes.dtos.ApiErrorResponse;
import edu.common.ratelimiting.RequestRateSupervisor;
import edu.java.services.interfaces.TgChatService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/scrapper/api/tg-chat", produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings({"MultipleStringLiterals"})
public class ChatController {
    private final TgChatService tgChatService;
    private final RequestRateSupervisor requestRateSupervisor;
    private static final ResponseEntity<?> REQUEST_RATE_LIMIT_ACHIEVED_RESPONSE = new ResponseEntity<>(
        new ApiErrorResponse(
            "rate limit", "429", null, null, null
        ),
        HttpStatus.TOO_MANY_REQUESTS
    );

    public ChatController(@Autowired TgChatService tgChatService, @Autowired RequestRateSupervisor supervisor) {
        this.tgChatService = tgChatService;
        this.requestRateSupervisor = supervisor;
    }

    @PostMapping(value = "/{id}")
    public ResponseEntity<?> handleRegistryChat(@PathVariable Long id, HttpServletRequest request) {
        Bucket bucket = requestRateSupervisor.resolveBucket(request.getRemoteAddr());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            return REQUEST_RATE_LIMIT_ACHIEVED_RESPONSE;
        }

        tgChatService.register(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> handleDeleteChat(@PathVariable Long id, HttpServletRequest request) {
        Bucket bucket = requestRateSupervisor.resolveBucket(request.getRemoteAddr());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            return REQUEST_RATE_LIMIT_ACHIEVED_RESPONSE;
        }

        tgChatService.unregister(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
