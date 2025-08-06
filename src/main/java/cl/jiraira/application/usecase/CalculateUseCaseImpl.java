package cl.jiraira.application.usecase;

import cl.jiraira.domain.exception.PercentageUnavailableException;
import cl.jiraira.domain.model.Calculation;
import cl.jiraira.domain.port.in.CalculateUseCase;
import cl.jiraira.domain.port.out.CachePort;
import cl.jiraira.domain.port.out.PercentagePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class CalculateUseCaseImpl implements CalculateUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CalculateUseCaseImpl.class);
    private static final String PERCENTAGE_CACHE_KEY = "percentage";

    private final PercentagePort percentagePort;
    private final CachePort cachePort;

    public CalculateUseCaseImpl(PercentagePort percentagePort, CachePort cachePort) {
        this.percentagePort = percentagePort;
        this.cachePort = cachePort;
    }

    @Override
    public Calculation calculate(BigDecimal firstNumber, BigDecimal secondNumber) {
        logger.info("Iniciando cálculo para num1: {}, num2: {}", firstNumber, secondNumber);

        BigDecimal percentage = null;
        String details = null;
        int maxRetries = 3;
        int attempt = 0;
        boolean success = false;
        Exception lastException = null;
        while (attempt < maxRetries && !success) {
            try {

                percentage = percentagePort.getPercentage();
                cachePort.putPercentage(PERCENTAGE_CACHE_KEY, percentage, 30L);
                details = String.format(Locale.US, "(%.1f + %.1f) + %.1f%%", firstNumber, secondNumber, percentage);
                success = true;
            } catch (Exception e) {
                lastException = e;
                logger.warn("Intento {} fallido al obtener porcentaje del servicio externo: {}", attempt + 1, e.getMessage());
                attempt++;
            }
        }
        if (!success) {
            var cached = cachePort.getPercentage(PERCENTAGE_CACHE_KEY);
            if (cached.isPresent()) {
                percentage = cached.get();
                details = String.format(Locale.US, "(%.1f + %.1f) + %.1f%% (usando último valor en caché)", firstNumber, secondNumber, percentage);
            } else {
                logger.error("No hay porcentaje disponible en caché ni en el servicio externo tras {} intentos", maxRetries);
                throw new PercentageUnavailableException("No se pudo obtener el porcentaje dinámico ni existe valor previo en caché", lastException);
            }
        }

        Calculation calculation = new Calculation(firstNumber, secondNumber, percentage, details);
        logger.info("Cálculo completado: {}", calculation.getDetails());
        return calculation;
    }
}
