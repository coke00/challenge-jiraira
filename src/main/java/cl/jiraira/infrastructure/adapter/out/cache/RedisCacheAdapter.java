package cl.jiraira.infrastructure.adapter.out.cache;

import cl.jiraira.domain.port.out.CachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Adaptador para el manejo de caché Redis
 */
@Component
public class RedisCacheAdapter implements CachePort {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheAdapter.class);
    private static final String CACHE_NAME = "percentage";

    private final CacheManager cacheManager;

    public RedisCacheAdapter(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void putPercentage(String key, BigDecimal percentage) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                cache.put(key, percentage.doubleValue());
                logger.info("Porcentaje guardado en caché: {}% con key: {}", percentage, key);
            } else {
                logger.warn("Cache '{}' no está disponible", CACHE_NAME);
            }
        } catch (Exception e) {
            logger.error("Error al guardar porcentaje en caché: {}", e.getMessage(), e);
        }
    }

    @Override
    public void putPercentage(String key, BigDecimal percentage, long ttl) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                cache.put(key, percentage.doubleValue());
                logger.info("Porcentaje guardado en caché: {}% con key: {} y ttl: {}", percentage, key, ttl);
                // Nota: Spring Cache no maneja TTL por clave directamente. Si necesitas TTL, considera usar RedisTemplate.
            } else {
                logger.warn("Cache '{}' no está disponible", CACHE_NAME);
            }
        } catch (Exception e) {
            logger.error("Error al guardar porcentaje en caché con TTL: {}", e.getMessage(), e);
        }
    }

    @Override
    public Optional<BigDecimal> getPercentage(String key) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                Cache.ValueWrapper cachedValue = cache.get(key);
                if (cachedValue != null && cachedValue.get() != null) {
                    Double percentage = (Double) cachedValue.get();
                    BigDecimal result = BigDecimal.valueOf(percentage);
                    logger.info("Porcentaje obtenido del caché: {}% con key: {}", result, key);
                    return Optional.of(result);
                }
            }
            logger.info("No se encontró valor en caché para key: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error al obtener porcentaje del caché: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void evictPercentage(String key) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                cache.evict(key);
                logger.info("Porcentaje eliminado del caché con key: {}", key);
            }
        } catch (Exception e) {
            logger.error("Error al eliminar porcentaje del caché: {}", e.getMessage(), e);
        }
    }
}
