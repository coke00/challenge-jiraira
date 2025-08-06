package cl.jiraira.domain.port.out;

import cl.jiraira.domain.model.ApiCall;

import java.util.List;

public interface ApiCallHistoryPort {
    void save(ApiCall apiCall);

    List<ApiCall> findHistory(int page, int size);
}
