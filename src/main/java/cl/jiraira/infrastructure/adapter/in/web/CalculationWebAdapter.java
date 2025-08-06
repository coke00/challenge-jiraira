package cl.jiraira.infrastructure.adapter.in.web;

import cl.jiraira.domain.model.Calculation;
import cl.jiraira.domain.port.in.CalculateUseCase;
import cl.jiraira.infrastructure.adapter.in.web.dto.CalculationRequest;
import cl.jiraira.infrastructure.adapter.in.web.dto.CalculationResponse;
import cl.jiraira.infrastructure.adapter.in.web.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/calculations")
@Tag(name = "Calculations", description = "API para realizar cálculos con porcentaje dinámico")
@AllArgsConstructor
public class CalculationWebAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CalculationWebAdapter.class);

    private final CalculateUseCase calculateUseCase;

    @PostMapping("/calculate")
    @Operation(
        summary = "Calcular suma con porcentaje dinámico",
        description = "Recibe dos números, los suma y aplica un porcentaje obtenido de un servicio externo. " +
                     "El porcentaje se cachea por 30 minutos y tiene lógica de retry en caso de fallo del servicio externo."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cálculo realizado exitosamente",
            content = @Content(schema = @Schema(implementation = CalculationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate limit excedido (máximo 3 RPM)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<CalculationResponse> calculate(@Valid @RequestBody CalculationRequest request) {
        logger.info("Recibida solicitud de cálculo: {}", request);
        Calculation calculation = calculateUseCase.calculate(
            BigDecimal.valueOf(request.getNum1()),
            BigDecimal.valueOf(request.getNum2())
        );

        CalculationResponse response = CalculationResponse.builder()
                .details(calculation.getDetails()).percentage(calculation.getPercentage().doubleValue()).result(calculation.getResult().doubleValue()).build();

        logger.info("Respuesta enviada: {}", response);
        return ResponseEntity.ok(response);
    }

    /*@GetMapping("/health")
    @Operation(
        summary = "Health check del servicio de cálculo",
        description = "Verifica que el servicio de cálculo esté funcionando correctamente"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok({status: "UP"});
    }*/
}
