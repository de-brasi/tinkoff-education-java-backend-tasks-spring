package edu.java.bot.retryutils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
    long initialDelay() default 0;
    int attempts() default 0;
    Class<? extends Exception>[] handled();
    BackoffPolicy backoffPolicy() default BackoffPolicy.LINEAR;
}
