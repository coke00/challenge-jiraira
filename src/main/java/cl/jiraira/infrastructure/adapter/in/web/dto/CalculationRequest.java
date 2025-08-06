package cl.jiraira.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CalculationRequest {

    @NotNull(message = "num1 es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "num1 debe ser mayor que 0")
    @JsonProperty("num1")
    private Double num1;

    @NotNull(message = "num2 es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "num2 debe ser mayor que 0")
    @JsonProperty("num2")
    private Double num2;

}
