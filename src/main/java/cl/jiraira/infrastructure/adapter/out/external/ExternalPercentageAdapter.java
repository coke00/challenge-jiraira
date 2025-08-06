package cl.jiraira.infrastructure.adapter.out.external;

import cl.jiraira.domain.port.out.PercentagePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * Adaptador para el servicio externo de porcentajes
 */
@Component
public class ExternalPercentageAdapter implements PercentagePort {

    private static final Logger logger = LoggerFactory.getLogger(ExternalPercentageAdapter.class);

    private final RestTemplate restTemplate;
    private final String percentageServiceUrl;

    public ExternalPercentageAdapter(RestTemplate restTemplate,
                                   @Value("${external.percentage-service.url}") String percentageServiceUrl) {
        this.restTemplate = restTemplate;
        this.percentageServiceUrl = percentageServiceUrl;
    }

    @Override
    @Retryable(
        retryFor = {Exception.class},
        maxAttemptsExpression = "${external.percentage-service.retry.max-attempts:3}",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public BigDecimal getPercentage() {
        logger.info("Consultando servicio externo para obtener porcentaje: {}", percentageServiceUrl);

        try {
            Double percentage = restTemplate.getForObject(percentageServiceUrl, Double.class);

            if (percentage == null) {
                throw new RuntimeException("El servicio externo retornó un valor nulo");
            }

            BigDecimal result = BigDecimal.valueOf(percentage);
            logger.info("Porcentaje obtenido del servicio externo: {}%", result);

            return result;

        } catch (Exception e) {
            logger.error("Error al consultar servicio externo de porcentajes: {}", e.getMessage());
            throw new RuntimeException("Error al obtener porcentaje del servicio externo", e);
        }
    }
}
