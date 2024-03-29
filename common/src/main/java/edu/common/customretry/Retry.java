package edu.common.customretry;

import org.springframework.http.HttpStatus;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
    long initialDelay() default 0;
    int attempts() default 0;
    HttpStatus[] handled();
    BackoffPolicy backoffPolicy() default BackoffPolicy.LINEAR;
}
