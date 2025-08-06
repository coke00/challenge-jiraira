package cl.jiraira.application.usecase;

import cl.jiraira.domain.model.ApiCall;
import cl.jiraira.domain.port.in.ApiCallHistoryUseCase;
import cl.jiraira.domain.port.out.ApiCallHistoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiCallHistoryUseCaseImpl implements ApiCallHistoryUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ApiCallHistoryUseCaseImpl.class);

    private final ApiCallHistoryPort apiCallHistoryPort;

    public ApiCallHistoryUseCaseImpl(ApiCallHistoryPort apiCallHistoryPort) {
        this.apiCallHistoryPort = apiCallHistoryPort;
    }

    @Override
    @Async
    public void saveApiCall(ApiCall apiCall) {
        logger.debug("Guardando historial de llamada API: {} {}", apiCall.method(), apiCall.endpoint());

        try {
            apiCallHistoryPort.save(apiCall);
            logger.debug("Historial guardado exitosamente para: {} {}", apiCall.method(), apiCall.endpoint());
        } catch (Exception e) {
            logger.error("Error al guardar historial de API call: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<ApiCall> getHistory(int page, int size) {
        logger.info("Obteniendo historial de API calls - página: {}, tamaño: {}", page, size);

        try {
            List<ApiCall> history = apiCallHistoryPort.findHistory(page, size);
            logger.info("Se obtuvieron {} registros del historial", history.size());
            return history;
        } catch (Exception e) {
            logger.error("Error al obtener historial de API calls: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener el historial de llamadas", e);
        }
    }
}
