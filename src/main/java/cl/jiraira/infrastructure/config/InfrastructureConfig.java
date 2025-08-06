package cl.jiraira.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;


@Configuration
public class InfrastructureConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                   @Value("${external.percentage-service.timeout:5000ms}") Duration timeout) {
        return builder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }
}
