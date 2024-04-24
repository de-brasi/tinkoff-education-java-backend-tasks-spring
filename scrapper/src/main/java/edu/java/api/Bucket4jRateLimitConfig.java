package edu.java.api;

import edu.common.ratelimiting.RateLimitInterceptor;
import edu.common.ratelimiting.RequestRateSupervisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Import({RateLimitInterceptor.class, RequestRateSupervisor.class})
public class Bucket4jRateLimitConfig implements WebMvcConfigurer {
    @Autowired
    private RateLimitInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**");
    }
}
