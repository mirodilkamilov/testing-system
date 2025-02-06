package dev.mirodil.testing_system.controllers;

import dev.mirodil.testing_system.dtos.TestEventResponseDTO;
import dev.mirodil.testing_system.models.TestEvent;
import dev.mirodil.testing_system.services.TestEventService;
import dev.mirodil.testing_system.utils.PageWithFilterRequest;
import dev.mirodil.testing_system.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class TestEventController {
    private final TestEventService testEventService;

    public TestEventController(TestEventService testEventService) {
        this.testEventService = testEventService;
    }

    @GetMapping("/test-events")
    public Page<TestEventResponseDTO> getAllTestEvents(PageWithFilterRequest pageable) {
        ValidationUtil.forceValidPageable(
                pageable,
                TestEvent.getAllowedSortAttributes(),
                TestEvent.getAllowedFilterAttributes()
        );
        return testEventService.getTestEvents(pageable);
    }

    @GetMapping("/test-events/{id}")
    public WrapResponseWithContentKey<?> getTestEventById(@PathVariable Long id) {
        TestEventResponseDTO testEventDTO = testEventService.getTestEventById(id);
        return new WrapResponseWithContentKey<>(testEventDTO);
    }

    @GetMapping("/test-events/{id}/test-attempt")
    public WrapResponseWithContentKey<?> getTestAttempt(@PathVariable Long id) {
        // TODO: implement
        return new WrapResponseWithContentKey<>(null);
    }
}
