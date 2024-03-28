package edu.java.bot.retryutils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

@Aspect
@Component
public class RetryAspect {
    @Around("@annotation(edu.java.bot.retryutils.Retry)")
    public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {
        Retry retry = retrieveAnnotation(joinPoint);
        long delay = retry.initialDelay();
        int attempts = retry.attempts();
        Collection<Class<? extends Exception>> handledExceptions = Arrays.stream(retry.handled()).toList();
        BackoffPolicy backoff = retry.backoffPolicy();
        var delayTimeGenerator = new DelayTimeGenerator(backoff, delay);

        for (int i = 0; i < attempts; i++) {
            try {
                Thread.sleep(delayTimeGenerator.next());
                return joinPoint.proceed();
            } catch (Exception e) {
                Class<? extends Exception> caughtExceptionClass = e.getClass();

                if (!handledExceptions.contains(caughtExceptionClass)) {
                    throw e;
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
        DelayTimeGenerator(BackoffPolicy policy, long delay) {
            this.delay = delay;

            delayFunction = switch (policy) {
                case CONSTANT -> ((iter, initialDelay) -> initialDelay);
                case LINEAR -> ((iter, initialDelay) -> initialDelay + Math.round(iter * GROWTH_RATE));
                case EXPONENTIAL -> ((iter, initialDelay) -> initialDelay + Math.round(Math.pow(Math.E, iter)));
            };
        }

        private static final float GROWTH_RATE = 1;

        private final long delay;
        private final DelayFunction<Integer, Long, Long> delayFunction;
        private int curIter = 0;

        long next() {
            return delayFunction.apply(curIter++, delay);
        }
    }

    @FunctionalInterface
    private static interface DelayFunction<I, D, R> {
        public R apply(I iter, D delay);
    }
}
