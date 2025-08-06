package cl.jiraira.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCallHistoryResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("endpoint")
    private String endpoint;

    @JsonProperty("method")
    private String method;

    @JsonProperty("parameters")
    private String parameters;

    @JsonProperty("response")
    private String response;

    @JsonProperty("statusCode")
    private Integer statusCode;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("executionTimeMs")
    private Long executionTimeMs;

}
