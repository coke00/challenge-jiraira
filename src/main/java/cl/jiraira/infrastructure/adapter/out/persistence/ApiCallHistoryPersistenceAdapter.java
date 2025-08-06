package cl.jiraira.infrastructure.adapter.out.persistence;

import cl.jiraira.domain.model.ApiCall;
import cl.jiraira.domain.port.out.ApiCallHistoryPort;
import cl.jiraira.infrastructure.adapter.out.persistence.entity.ApiCallHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para el historial de API calls
 */
@Component
public class ApiCallHistoryPersistenceAdapter implements ApiCallHistoryPort {

    private final ApiCallHistoryRepository repository;

    public ApiCallHistoryPersistenceAdapter(ApiCallHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(ApiCall apiCall) {
        ApiCallHistory entity = new ApiCallHistory();
        entity.setEndpoint(apiCall.endpoint());
        entity.setMethod(apiCall.method());
        entity.setRequestBody(apiCall.requestBody());
        entity.setResponseBody(apiCall.responseBody());
        entity.setResponseStatus(apiCall.responseStatus());
        entity.setTimestamp(apiCall.timestamp());
        entity.setExecutionTimeMs(apiCall.executionTimeMs());

        repository.save(entity);
    }

    @Override
    public List<ApiCall> findHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<ApiCallHistory> historyPage = repository.findAll(pageable);

        return historyPage.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ApiCall toDomain(ApiCallHistory entity) {
        return new ApiCall(
                entity.getId(),
                entity.getEndpoint(),
                entity.getMethod(),
                entity.getRequestBody(),
                entity.getResponseBody(),
                entity.getResponseStatus(),
                entity.getTimestamp(),
                entity.getExecutionTimeMs()
        );
    }
}
