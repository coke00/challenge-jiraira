package cl.jiraira.infrastructure.config;

import cl.jiraira.domain.port.out.PercentagePort;
import cl.jiraira.infrastructure.adapter.out.MockPercentagePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PercentagePortConfig {
    @Bean
    public PercentagePort percentagePort() {
        return new MockPercentagePort();
    }
}

