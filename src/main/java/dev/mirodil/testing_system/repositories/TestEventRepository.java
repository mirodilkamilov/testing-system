package dev.mirodil.testing_system.repositories;

import dev.mirodil.testing_system.models.TestEvent;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TestEventRepository extends CrudRepository<TestEvent, Long>, GenericRepositoryWithPagination<TestEvent> {
    String GET_DETAILED_BASE_QUERY = """
            SELECT te.id AS test_event_id, test_taker_id, te.test_id AS test_id, event_datetime, te.status AS test_event_status,
                  score, is_passed, started_at, finished_at, te.created_at AS test_event_created_at, test_attempt,
                  email, password, role_id, roles.name AS role_name, fname, lname, gender, u.status AS user_status, u.created_at AS user_created_at,
                  title, description, duration, no_of_questions, passing_percentage, should_shuffle, should_randomly_pick, deleted_at
            FROM test_events te
            JOIN users u ON u.id = te.test_taker_id
            JOIN roles ON roles.id = u.role_id
            JOIN tests ON tests.id = te.test_id
            """;

    static StringBuilder getPaginationBaseQuery() {
        return new StringBuilder("""
                SELECT te.id AS test_event_id, event_datetime, te.status AS test_event_status,
                      score, is_passed, started_at, finished_at, te.created_at AS test_event_created_at,
                      test_taker_id, email, fname, lname,
                      te.test_id AS test_id, title, duration, no_of_questions, passing_percentage
                FROM test_events te
                JOIN users u ON u.id = te.test_taker_id
                JOIN tests ON tests.id = te.test_id
                """
        );
    }

    static StringBuilder getPaginationCountBaseQuery() {
        return new StringBuilder("SELECT count(*) FROM test_events");
    }

    @Query(value = GET_DETAILED_BASE_QUERY + " WHERE te.id = :id",
            rowMapperRef = "testEventRowMapper")
    Optional<TestEvent> getTestEventsById(Long id); // TODO: add more data: testTaker
}
