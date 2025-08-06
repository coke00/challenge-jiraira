package cl.jiraira.domain.port.in;

import cl.jiraira.domain.model.ApiCall;

import java.util.List;


public interface ApiCallHistoryUseCase {
    void saveApiCall(ApiCall apiCall);
    List<ApiCall> getHistory(int page, int size);
}
