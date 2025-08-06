package cl.jiraira.domain.port.out;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Puerto de salida para el manejo de caché
 */
public interface CachePort {
    void putPercentage(String key, BigDecimal percentage);
    void putPercentage(String key, BigDecimal percentage, long expirationMinutes);
    Optional<BigDecimal> getPercentage(String key);
    void evictPercentage(String key);
}
