package edu.common.customretry;

import edu.common.datatypes.exceptions.httpresponse.BadHttpResponseException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RetryAspect {
    @Around("@annotation(edu.common.customretry.Retry)")
    public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {
        Retry retry = retrieveAnnotation(joinPoint);
        long delay = retry.initialDelay();
        int attempts = retry.attempts();
        Collection<HttpStatus> handledStatusCodes = Arrays.stream(retry.handled()).toList();
        BackoffPolicy backoff = retry.backoffPolicy();
        var delayTimeGenerator = new DelayTimeGenerator(backoff, delay);

        for (int i = 0; i < attempts; i++) {
            try {
                var time = delayTimeGenerator.next();
                Thread.sleep(time);
                return joinPoint.proceed();
            } catch (BadHttpResponseException e) {
                // information message about the error being caught
                log.warn("Error BadHttpResponseException caught in aspect. Status: " + e.getHttpCode());

                HttpStatus responseStatus = e.getHttpCode();
                if (!handledStatusCodes.contains(responseStatus)) {
                    throw e;
                } else {
                    var time = delayTimeGenerator.next();
                    log.info("In aspect retry delay " + time + "ms");
                    Thread.sleep(time);
                }
            }
        }

        return joinPoint.proceed();
    }

    private Retry retrieveAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        return method.getAnnotation(Retry.class);
    }

    private static class DelayTimeGenerator {
        private static final float GROWTH_RATE = 100;
        private final long delay;
        private final DelayValueGeneratingFunction<Integer, Long, Long> delayValueGeneratingFunction;
        private int currentRetryNumber = 0;

        DelayTimeGenerator(BackoffPolicy policy, long delay) {
            this.delay = delay;

            delayValueGeneratingFunction = switch (policy) {
                case CONSTANT -> ((retryNumber, initialDelayValue) -> initialDelayValue);
                case LINEAR ->
                    ((retryNumber, initialDelayValue) -> initialDelayValue + Math.round(retryNumber * GROWTH_RATE));
                case EXPONENTIAL ->
                    ((retryNumber, initialDelayValue) -> initialDelayValue + Math.round(Math.pow(Math.E, retryNumber)));
            };
        }

        long next() {
            return delayValueGeneratingFunction.apply(currentRetryNumber++, delay);
        }
    }

    @FunctionalInterface
    private interface DelayValueGeneratingFunction<I, D, R> {
        R apply(I iter, D delay);
    }
}
