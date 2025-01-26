package dev.mirodil.testing_system.dtos;

import dev.mirodil.testing_system.models.TestEvent;
import dev.mirodil.testing_system.models.enums.TestEventStatus;

import java.util.Date;

public record TestEventResponseDTO(
        Long testEventId,
        UserResponseDTO testTaker,
        Long testId,
        Date eventDatetime,
        TestEventStatus status,
        Double score,
        Boolean isPassed,
        Date startedAt,
        Date finishedAt,
        Date createdAt,
        String testAttempt
        // TODO: path
) {
    public TestEventResponseDTO(TestEvent testEvent) {
        this(
                testEvent.getId(),
                new UserResponseDTO(testEvent.getTestTaker()),
                testEvent.getTestId(),
                testEvent.getEventDateTime(),
                testEvent.getStatus(),
                testEvent.getScore(),
                testEvent.getPassed(),
                testEvent.getStartedAt(),
                testEvent.getFinishedAt(),
                testEvent.getCreatedAt(),
                testEvent.getTestAttempt()
        );
    }
}
