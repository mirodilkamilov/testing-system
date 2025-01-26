package dev.mirodil.testing_system.services;

import dev.mirodil.testing_system.dtos.TestEventResponseDTO;
import dev.mirodil.testing_system.models.TestEvent;
import dev.mirodil.testing_system.repositories.GenericRowMapper;
import dev.mirodil.testing_system.repositories.TestEventRepository;
import dev.mirodil.testing_system.utils.DataUtil;
import dev.mirodil.testing_system.utils.PageWithFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestEventService {
    private final TestEventRepository testEventRepository;

    public TestEventService(TestEventRepository testEventRepository) {
        this.testEventRepository = testEventRepository;
    }

    public Page<TestEventResponseDTO> getTestEvents(PageWithFilterRequest pageable) {
        StringBuilder queryBuilder = new StringBuilder("""
                SELECT te.id AS test_event_id, test_taker_id, te.test_id AS test_id, event_datetime, te.status AS test_event_status,
                      score, is_passed, started_at, finished_at, te.created_at AS test_event_created_at, test_attempt,
                      email, password, role_id, fname, lname, gender, u.status AS user_status, u.created_at AS user_created_at
                FROM test_events te
                JOIN users u ON u.id = te.test_taker_id
                """
        );

        RowMapper<TestEvent> testEventRowMapper = new GenericRowMapper<>(DataUtil::extractTestEventFromResultSet);
        List<TestEvent> testEvents = testEventRepository.findAndSortModelWithPagination(
                pageable, queryBuilder, testEventRowMapper);
        long totalElements = testEventRepository.countFilteredModel(pageable, new StringBuilder("SELECT count(*) FROM test_events"));

        List<TestEventResponseDTO> testEventsDTO = testEvents.stream()
                .map(TestEventResponseDTO::new)
                .toList();

        return new PageImpl<>(testEventsDTO, pageable, totalElements);
    }

}
