package edu.common.ratelimiting;

import edu.common.datatypes.dtos.ApiErrorResponse;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RequestRateSupervisor requestRateSupervisor;

    private static final ResponseEntity<?> REQUEST_RATE_LIMIT_ACHIEVED_RESPONSE = new ResponseEntity<>(
        new ApiErrorResponse(
            "rate limit", "429", null, null, null
        ),
        HttpStatus.TOO_MANY_REQUESTS
    );

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, Object handler)
        throws Exception {
        Bucket bucket = requestRateSupervisor.resolveBucket(request.getRemoteAddr());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            response.getWriter().write(REQUEST_RATE_LIMIT_ACHIEVED_RESPONSE.toString());
            response.setStatus(REQUEST_RATE_LIMIT_ACHIEVED_RESPONSE.getStatusCode().value());
            return false;
        }

        return true;
    }
}
