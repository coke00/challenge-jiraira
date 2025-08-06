package cl.jiraira.infrastructure.adapter.out;

import cl.jiraira.domain.port.out.PercentagePort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mock que retorna un porcentaje fijo para pruebas
 */
@Component
public class MockPercentagePort implements PercentagePort {
    @Override
    public BigDecimal getPercentage() {
        return BigDecimal.valueOf(10); // Retorna siempre 10%
    }
}

