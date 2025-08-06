package cl.jiraira.infrastructure.adapter.in.web;

import cl.jiraira.domain.model.Calculation;
import cl.jiraira.domain.port.in.ApiCallHistoryUseCase;
import cl.jiraira.domain.port.in.CalculateUseCase;
import cl.jiraira.infrastructure.adapter.in.web.dto.CalculationRequest;
import cl.jiraira.infrastructure.config.RateLimitingConfig;
import cl.jiraira.infrastructure.interceptor.RateLimitInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalculationWebAdapter.class)
@Import({RateLimitInterceptor.class})
class CalculationWebAdapterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CalculateUseCase calculateUseCase;

    @MockBean
    private RateLimitInterceptor rateLimitInterceptor;

    @MockBean
    private RateLimitingConfig rateLimitingConfig;

    @MockBean
    private ApiCallHistoryUseCase apiCallHistoryUseCase;

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();

        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void calculate_WithValidRequest_ShouldReturnOk() throws Exception {
        CalculationRequest request = new CalculationRequest(5.0, 5.0);
        Calculation calculation = new Calculation(
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(10.0)
        );

        when(calculateUseCase.calculate(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(calculation);

        mockMvc.perform(post("/api/v1/calculations/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.percentage").exists())
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void calculate_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        String invalidRequest = "{\"num1\": null, \"num2\": 5.0}";
        mockMvc.perform(post("/api/v1/calculations/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void calculate_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        CalculationRequest request = new CalculationRequest(5.0, 5.0);
        when(calculateUseCase.calculate(any(BigDecimal.class), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/calculations/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void calculate_WhenRateLimitExceeded_ShouldReturn429() throws Exception {
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"error\":\"Rate limit excedido\"}");
            return false;
        }).when(rateLimitInterceptor).preHandle(any(), any(), any());

        CalculationRequest request = new CalculationRequest(5.0, 6.0);
        mockMvc.perform(post("/api/v1/calculations/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void calculate_WithNegativeNumbers_ShouldReturnBadRequest() throws Exception {
        CalculationRequest request = new CalculationRequest(-5.0, -3.0);
        mockMvc.perform(post("/api/v1/calculations/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void calculate_WithNonNumericParameters_ShouldReturnBadRequest() throws Exception {
        String invalidRequest = "{\"num1\": \"abc\", \"num2\": 5.0}";
        mockMvc.perform(post("/api/v1/calculations/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }
}
