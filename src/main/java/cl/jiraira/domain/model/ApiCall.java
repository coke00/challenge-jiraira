package cl.jiraira.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ApiCall(Long id, String endpoint, String method, String requestBody, String responseBody,
                      Integer responseStatus, LocalDateTime timestamp, Long executionTimeMs) {
}
