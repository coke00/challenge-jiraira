package cl.jiraira.application.usecase;

import cl.jiraira.domain.model.Calculation;
import cl.jiraira.domain.port.out.CachePort;
import cl.jiraira.domain.port.out.PercentagePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateUseCaseImplTest {

    @Mock
    private PercentagePort percentagePort;

    @Mock
    private CachePort cachePort;

    private CalculateUseCaseImpl calculateUseCase;

    @BeforeEach
    void setUp() {
        calculateUseCase = new CalculateUseCaseImpl(percentagePort, cachePort);
    }

    @Test
    void calculate_WithValidInputAndExternalService_ShouldReturnCorrectResult() {
        // Given
        BigDecimal num1 = BigDecimal.valueOf(5.0);
        BigDecimal num2 = BigDecimal.valueOf(5.0);
        BigDecimal percentage = BigDecimal.valueOf(10.0);

        when(percentagePort.getPercentage()).thenReturn(percentage);

        // When
        Calculation result = calculateUseCase.calculate(num1, num2);

        // Then
        assertEquals(0, BigDecimal.valueOf(11.00).compareTo(result.getResult()));
        assertEquals(percentage, result.getPercentage());
        System.out.println("DEBUG details: " + result.getDetails());
        assertTrue(result.getDetails().contains("(5.0 + 5.0) + 10.0%"));

        verify(cachePort).putPercentage(anyString(), eq(percentage), any(Long.class));
    }

    @Test
    void calculate_WithCachedPercentage_ShouldUseCachedValue() {
        // Given
        BigDecimal num1 = BigDecimal.valueOf(3.0);
        BigDecimal num2 = BigDecimal.valueOf(7.0);
        BigDecimal cachedPercentage = BigDecimal.valueOf(15.0);

        // Configurar el comportamiento del mock para el primer y segundo intento
        when(percentagePort.getPercentage()).thenReturn(cachedPercentage);

        // When
        Calculation result = calculateUseCase.calculate(num1, num2);

        // Then
        assertEquals(0, BigDecimal.valueOf(11.50).compareTo(result.getResult()));
        assertEquals(cachedPercentage, result.getPercentage());

        // La implementación siempre intenta obtener del servicio externo primero
        verify(percentagePort).getPercentage();
        verify(cachePort).putPercentage(anyString(), eq(cachedPercentage), any(Long.class));
    }

    @Test
    void calculate_WithExternalServiceFailure_ShouldUseCachedValue() {
        // Given
        BigDecimal num1 = BigDecimal.valueOf(4.0);
        BigDecimal num2 = BigDecimal.valueOf(6.0);
        BigDecimal cachedPercentage = BigDecimal.valueOf(15.0);

        when(percentagePort.getPercentage()).thenThrow(new RuntimeException("External service error"));
        when(cachePort.getPercentage(anyString())).thenReturn(Optional.of(cachedPercentage));

        // When
        Calculation result = calculateUseCase.calculate(num1, num2);

        // Then
        assertEquals(0, BigDecimal.valueOf(11.50).compareTo(result.getResult())); // (4+6) + 15% = 11.5
        assertEquals(cachedPercentage, result.getPercentage());
        assertTrue(result.getDetails().contains("usando último valor en caché"));
    }

    @Test
    void calculate_WhenExternalServiceFails_ShouldUseCachedValue() {
        BigDecimal num1 = BigDecimal.valueOf(2.0);
        BigDecimal num2 = BigDecimal.valueOf(3.0);
        BigDecimal cachedPercentage = BigDecimal.valueOf(15.0);

        when(percentagePort.getPercentage()).thenThrow(new RuntimeException("Servicio externo caído"));
        when(cachePort.getPercentage(anyString())).thenReturn(Optional.of(cachedPercentage));

        Calculation result = calculateUseCase.calculate(num1, num2);

        assertEquals(0, BigDecimal.valueOf(5.75).compareTo(result.getResult()));
        assertEquals(cachedPercentage, result.getPercentage());
        assertTrue(result.getDetails().contains("usando último valor en caché"));
        
        verify(percentagePort, times(3)).getPercentage();
    }

    @Test
    void calculate_WhenExternalServiceFailsAndNoCache_ShouldThrowException() {
        BigDecimal num1 = BigDecimal.valueOf(2.0);
        BigDecimal num2 = BigDecimal.valueOf(3.0);

        when(percentagePort.getPercentage()).thenThrow(new RuntimeException("Servicio externo caído"));
        when(cachePort.getPercentage(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> calculateUseCase.calculate(num1, num2));
        assertTrue(exception.getMessage().contains("No se pudo obtener el porcentaje dinámico ni existe valor previo en caché"));
    }

    @Test
    void calculate_ShouldRetryUpToThreeTimesOnExternalServiceFailure() {
        BigDecimal num1 = BigDecimal.valueOf(1.0);
        BigDecimal num2 = BigDecimal.valueOf(1.0);
        BigDecimal percentage = BigDecimal.valueOf(10.0);

        // Falla dos veces, éxito en el tercer intento
        when(percentagePort.getPercentage())
            .thenThrow(new RuntimeException("Fallo 1"))
            .thenThrow(new RuntimeException("Fallo 2"))
            .thenReturn(percentage);

        Calculation result = calculateUseCase.calculate(num1, num2);
        assertEquals(0, BigDecimal.valueOf(2.2).compareTo(result.getResult()));
        assertEquals(percentage, result.getPercentage());
        verify(percentagePort, times(3)).getPercentage();
        verify(cachePort).putPercentage(anyString(), eq(percentage), any(Long.class));
    }

    @Test
    void calculate_ShouldRetryUpToThreeTimesOnExternalServiceFailureAndThenUseCache() {
        BigDecimal num1 = BigDecimal.valueOf(1.0);
        BigDecimal num2 = BigDecimal.valueOf(2.0);
        BigDecimal cachedPercentage = BigDecimal.valueOf(20.0);

        // Simula que el servicio externo falla 3 veces
        when(percentagePort.getPercentage())
            .thenThrow(new RuntimeException("Fallo 1"))
            .thenThrow(new RuntimeException("Fallo 2"))
            .thenThrow(new RuntimeException("Fallo 3"));
        when(cachePort.getPercentage(anyString())).thenReturn(Optional.of(cachedPercentage));

        Calculation result = calculateUseCase.calculate(num1, num2);
        assertEquals(cachedPercentage, result.getPercentage());
        verify(percentagePort, times(3)).getPercentage();
        assertTrue(result.getDetails().contains("usando último valor en caché"));
    }

    @Test
    void calculate_WhenNoCacheAndExternalFails_ShouldThrowPercentageUnavailableException() {
        BigDecimal num1 = BigDecimal.valueOf(2.0);
        BigDecimal num2 = BigDecimal.valueOf(8.0);

        when(cachePort.getPercentage(anyString())).thenReturn(Optional.empty());
        when(percentagePort.getPercentage()).thenThrow(new RuntimeException("Servicio externo caído"));

        assertThrows(cl.jiraira.domain.exception.PercentageUnavailableException.class, () -> {
            calculateUseCase.calculate(num1, num2);
        });
        
        verify(percentagePort, times(3)).getPercentage();
    }
}
