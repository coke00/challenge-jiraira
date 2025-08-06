package cl.jiraira.domain.port.in;

import cl.jiraira.domain.model.Calculation;

import java.math.BigDecimal;

public interface CalculateUseCase {
    Calculation calculate(BigDecimal firstNumber, BigDecimal secondNumber);
}
