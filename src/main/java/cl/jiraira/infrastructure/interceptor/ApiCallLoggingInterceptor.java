package cl.jiraira.infrastructure.interceptor;

import cl.jiraira.domain.model.ApiCall;
import cl.jiraira.domain.port.in.ApiCallHistoryUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;

@Component
public class ApiCallLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApiCallLoggingInterceptor.class);

    private final ApiCallHistoryUseCase apiCallHistoryUseCase;

    public ApiCallLoggingInterceptor(ApiCallHistoryUseCase apiCallHistoryUseCase, ObjectMapper objectMapper) {
        this.apiCallHistoryUseCase = apiCallHistoryUseCase;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // TODO: podriamos manejar una accion despues del request
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            Long startTime = (Long) request.getAttribute("startTime");
            Long executionTime = startTime != null ? System.currentTimeMillis() - startTime : null;

            String endpoint = request.getRequestURI();
            String method = request.getMethod();
            String parameters = extractParameters(request);
            String responseBody = extractResponseBody(response);
            //String errorMessage = ex != null ? ex.getMessage() : null;
            Integer statusCode = response.getStatus();

            ApiCall apiCall = ApiCall.builder()
                    .timestamp(LocalDateTime.now())
                    .endpoint(endpoint)
                    .method(method)
                    .executionTimeMs(executionTime)
                    .responseStatus(statusCode)
                    .requestBody(parameters)
                    .responseBody(responseBody).build();

            apiCallHistoryUseCase.saveApiCall(apiCall);

        } catch (Exception e) {
            logger.error("Error al registrar la llamada API en el historial: {}", e.getMessage());
        }
    }

    private String extractParameters(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    return new String(content, StandardCharsets.UTF_8);
                }
            }

            String queryString = request.getQueryString();
            if (queryString != null && !queryString.isEmpty()) {
                return queryString;
            }

            return null;
        } catch (Exception e) {
            logger.warn("Error al extraer parámetros de la request: {}", e.getMessage());
            return null;
        }
    }

    private String extractResponseBody(HttpServletResponse response) {
        try {
            if (response instanceof ContentCachingResponseWrapper wrapper) {
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    return new String(content, StandardCharsets.UTF_8);
                }
            }
            return null;
        } catch (Exception e) {
            logger.warn("Error al extraer el body de la response: {}", e.getMessage());
            return null;
        }
    }
}
