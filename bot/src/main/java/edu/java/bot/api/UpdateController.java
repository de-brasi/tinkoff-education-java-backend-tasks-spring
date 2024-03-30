package edu.java.bot.api;

import edu.common.datatypes.dtos.ApiErrorResponse;
import edu.common.datatypes.dtos.LinkUpdateRequest;
import edu.common.ratelimiting.RequestRateSupervisor;
import edu.java.bot.services.TelegramBotWrapper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final TelegramBotWrapper telegramBot;
    private final RequestRateSupervisor requestRateSupervisor;
    private static final ResponseEntity<?> REQUEST_RATE_LIMIT_ACHIEVED_RESPONSE = new ResponseEntity<>(
        new ApiErrorResponse(
            "rate limit", "429", null, null, null
        ),
        HttpStatus.TOO_MANY_REQUESTS
    );

    public UpdateController(
        @Autowired TelegramBotWrapper telegramBotWrapper,
        @Autowired RequestRateSupervisor supervisor
    ) {
        this.telegramBot = telegramBotWrapper;
        this.requestRateSupervisor = supervisor;
    }

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

        final String messageToClient = "Update in link %s with description: '%s'"
            .formatted(requestBody.getUrl(), requestBody.getDescription());

        for (Long chatId : requestBody.getTgChatIds()) {
            telegramBot.sendPlainTextMessage(chatId, messageToClient);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
