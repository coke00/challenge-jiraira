package cl.jiraira.infrastructure.adapter.in.web;

import cl.jiraira.domain.model.ApiCall;
import cl.jiraira.domain.port.in.ApiCallHistoryUseCase;
import cl.jiraira.infrastructure.adapter.in.web.dto.ApiCallHistoryResponse;
import cl.jiraira.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HistoryWebAdapter.class)
class HistoryWebAdapterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiCallHistoryUseCase apiCallHistoryUseCase;

    @MockBean
    private cl.jiraira.infrastructure.config.RateLimitingConfig rateLimitingConfig;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.mockito.Mockito.when(rateLimitingConfig.isAllowed(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
    }

    @Test
    void getHistory_ShouldReturnPagedHistory() throws Exception {
        ApiCall call = new ApiCall(1L, "/api/v1/calculations/calculate", "POST", "{\"num1\":5,\"num2\":5}", "{\"result\":11}", 200, LocalDateTime.now(), 50L);
        when(apiCallHistoryUseCase.getHistory(anyInt(), anyInt())).thenReturn(Collections.singletonList(call));

        mockMvc.perform(get("/api/v1/history?page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].endpoint").value("/api/v1/calculations/calculate"))
                .andExpect(jsonPath("$[0].method").value("POST"))
                .andExpect(jsonPath("$[0].parameters").exists())
                .andExpect(jsonPath("$[0].response").exists())
                .andExpect(jsonPath("$[0].statusCode").value(200));
    }

    @Test
    void getHistory_InvalidPagination_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/history?page=-1&size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getHistory_ShouldReturnPaginatedApiCallHistory() throws Exception {
        ApiCall call1 = ApiCall.builder()
                .timestamp(LocalDateTime.of(2025,8,6,10,0))
                .endpoint("/api/v1/calculations/calculate")
                .requestBody("{num1:5,num2:5}")
                .responseBody("{result:11}")
                .build();
        ApiCall call2 = ApiCall.builder()
                .timestamp(LocalDateTime.of(2025,8,6,10,1))
                .endpoint("/api/v1/calculations/calculate")
                .requestBody("{num1:2,num2:3}")
                .responseBody(null)
                .build();
        when(apiCallHistoryUseCase.getHistory(0, 2)).thenReturn(Arrays.asList(call1, call2));

        mockMvc.perform(get("/api/v1/history?page=0&size=2")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].endpoint").value("/api/v1/calculations/calculate"))
            .andExpect(jsonPath("$[0].parameters").value("{num1:5,num2:5}"))
            .andExpect(jsonPath("$[0].response").value("{result:11}"))
            .andExpect(jsonPath("$[0].error").doesNotExist())
            .andExpect(jsonPath("$[1].endpoint").value("/api/v1/calculations/calculate"))
            .andExpect(jsonPath("$[1].parameters").value("{num1:2,num2:3}"))
            .andExpect(jsonPath("$[1].response").doesNotExist());
    }
}
