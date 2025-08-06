package cl.jiraira.infrastructure.config;

import cl.jiraira.infrastructure.interceptor.ApiCallLoggingInterceptor;
import cl.jiraira.infrastructure.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final ApiCallLoggingInterceptor apiCallLoggingInterceptor;

    public WebConfig(RateLimitInterceptor rateLimitInterceptor,
                    ApiCallLoggingInterceptor apiCallLoggingInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.apiCallLoggingInterceptor = apiCallLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**");

        registry.addInterceptor(apiCallLoggingInterceptor)
                .addPathPatterns("/api/**");
    }
}
