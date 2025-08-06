package cl.jiraira.infrastructure.adapter.in.web;

import cl.jiraira.domain.model.ApiCall;
import cl.jiraira.domain.port.in.ApiCallHistoryUseCase;
import cl.jiraira.infrastructure.adapter.in.web.dto.ApiCallHistoryResponse;
import cl.jiraira.infrastructure.adapter.in.web.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/history")
@Tag(name = "API Call History", description = "API para consultar el historial de llamadas")
public class HistoryWebAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HistoryWebAdapter.class);

    private final ApiCallHistoryUseCase apiCallHistoryUseCase;

    public HistoryWebAdapter(ApiCallHistoryUseCase apiCallHistoryUseCase) {
        this.apiCallHistoryUseCase = apiCallHistoryUseCase;
    }

    @GetMapping
    @Operation(
        summary = "Obtener historial de llamadas API",
        description = "Retorna el historial paginado de todas las llamadas realizadas a los endpoints de la API. " +
                     "Incluye detalles como fecha/hora, endpoint, parámetros, respuesta y errores."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Historial obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = ApiCallHistoryResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros de paginación inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate limit excedido (máximo 3 RPM)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<ApiCallHistoryResponse>> getHistory(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Consultando historial - página: {}, tamaño: {}", page, size);

        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos. Page debe ser >= 0 y size debe estar entre 1 y 100");
        }

        List<ApiCall> history = apiCallHistoryUseCase.getHistory(page, size);
        List<ApiCallHistoryResponse> response = history.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        logger.info("Historial obtenido: {} elementos en página {}", response.size(), page);

        return ResponseEntity.ok(response);
    }

    private ApiCallHistoryResponse toResponse(ApiCall apiCall) {
        ApiCallHistoryResponse response = new ApiCallHistoryResponse();
        response.setId(apiCall.id());
        response.setEndpoint(apiCall.endpoint());
        response.setMethod(apiCall.method());
        response.setParameters(apiCall.requestBody());
        response.setResponse(apiCall.responseBody());
        response.setStatusCode(apiCall.responseStatus());
        response.setTimestamp(apiCall.timestamp());
        response.setExecutionTimeMs(apiCall.executionTimeMs());
        return response;
    }
}
