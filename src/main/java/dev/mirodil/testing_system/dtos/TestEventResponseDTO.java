package dev.mirodil.testing_system.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.mirodil.testing_system.controllers.TestEventController;
import dev.mirodil.testing_system.models.TestAttempt;
import dev.mirodil.testing_system.models.TestEvent;
import dev.mirodil.testing_system.models.enums.TestEventStatus;

import java.net.URI;
import java.util.Date;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public record TestEventResponseDTO(
        Long testEventId,
        UserResponseDTO testTaker,
        TestResponseDTO test,
        Date eventDatetime,
        TestEventStatus status,
        Double score,
        Boolean isPassed,
        Date startedAt,
        Date finishedAt,
        Date createdAt,
        // TODO: create separate TestAttemptDTO (with detailed questions and their options)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<TestAttempt> testAttempt,
        URI path
) {
    public TestEventResponseDTO(TestEvent testEvent) {
        this(
                testEvent.getId(),
                new UserResponseDTO(testEvent.getTestTaker()),
                new TestResponseDTO(testEvent.getTest()),
                testEvent.getEventDateTime(),
                testEvent.getStatus(),
                testEvent.getScore(),
                testEvent.getPassed(),
                testEvent.getStartedAt(),
                testEvent.getFinishedAt(),
                testEvent.getCreatedAt(),
                testEvent.getTestAttempt(),
                linkTo(methodOn(TestEventController.class).getTestEventById(testEvent.getId())).toUri()
        );
    }
}
