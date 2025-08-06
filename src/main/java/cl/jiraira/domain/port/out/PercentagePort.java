package cl.jiraira.domain.port.out;

import java.math.BigDecimal;

/**
 * Puerto de salida para obtener porcentajes de servicios externos
 */
public interface PercentagePort {
    BigDecimal getPercentage();
}
