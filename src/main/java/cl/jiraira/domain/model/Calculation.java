package cl.jiraira.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Setter
@Getter
public class Calculation {
    private final BigDecimal firstNumber;
    private final BigDecimal secondNumber;
    private final BigDecimal percentage;
    private final BigDecimal result;
    private final String details;

    public Calculation(BigDecimal firstNumber, BigDecimal secondNumber, BigDecimal percentage) {
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
        this.percentage = percentage;
        this.result = calculateResult();
        this.details = buildDetails();
    }

    public Calculation(BigDecimal firstNumber, BigDecimal secondNumber, BigDecimal percentage, String details) {
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
        this.percentage = percentage;
        this.result = calculateResult();
        this.details = details;
    }

    private BigDecimal calculateResult() {
        BigDecimal sum = firstNumber.add(secondNumber);
        BigDecimal percentageAmount = sum.multiply(percentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return sum.add(percentageAmount).setScale(2, RoundingMode.HALF_UP);
    }

    private String buildDetails() {
        return String.format(Locale.US, "(%.1f + %.1f) + %.1f%%", firstNumber, secondNumber, percentage);
    }

}
