package cl.jiraira.infrastructure.adapter.out.persistence;

import cl.jiraira.infrastructure.adapter.out.persistence.entity.ApiCallHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository para el manejo de persistencia del historial de API calls
 */
@Repository
public interface ApiCallHistoryRepository extends JpaRepository<ApiCallHistory, Long> {

    /**
     * Encuentra el historial paginado ordenado por timestamp
     */
    Page<ApiCallHistory> findAll(Pageable pageable);
}
