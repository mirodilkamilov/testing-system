package dev.mirodil.testing_system.repositories.impls;

import dev.mirodil.testing_system.models.TestEvent;
import dev.mirodil.testing_system.repositories.CustomTestEventRepository;
import dev.mirodil.testing_system.repositories.GenericRepositoryWithPagination;
import dev.mirodil.testing_system.repositories.GenericRowMapper;
import dev.mirodil.testing_system.utils.DataUtil;
import dev.mirodil.testing_system.utils.PageWithFilterRequest;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class CustomTestEventRepositoryImpl implements CustomTestEventRepository {
    private final RowMapper<TestEvent> testEventRowMapper = new GenericRowMapper<>(DataUtil::extractTestEventFromResultSet);
    private final GenericRepositoryWithPagination<TestEvent> paginationRepository;

    public CustomTestEventRepositoryImpl(GenericRepositoryWithPagination<TestEvent> paginationRepository) {
        this.paginationRepository = paginationRepository;
    }

    @Override
    public List<TestEvent> findAndSortTestEventsWithPagination(PageWithFilterRequest pageable) {
        return paginationRepository.findAndSortModelWithPagination(pageable, getBaseQuery(), testEventRowMapper);
    }

    @Override
    public long countFilteredTestEvents(PageWithFilterRequest pageable) {
        return paginationRepository.countFilteredModel(pageable, getCountBaseQuery());
    }

    private StringBuilder getBaseQuery() {
        return new StringBuilder("""
                SELECT te.id AS test_event_id, event_datetime, te.status AS test_event_status,
                      score_points, score_percentage, is_passed, started_at, finished_at, te.created_at AS test_event_created_at,
                      test_taker_id, email, fname, lname,
                      te.test_id AS test_id, title, duration, no_of_questions, passing_percentage
                FROM test_events te
                JOIN users u ON u.id = te.test_taker_id
                JOIN tests ON tests.id = te.test_id
                """);
    }

    private StringBuilder getCountBaseQuery() {
        return new StringBuilder("""
                SELECT count(te.id)
                FROM test_events te
                JOIN users u ON u.id = te.test_taker_id
                JOIN tests ON tests.id = te.test_id
                """);
    }
}
