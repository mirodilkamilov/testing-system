package dev.mirodil.testing_system.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.mirodil.testing_system.models.Test;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TestResponseDTO(
        Long testId,
        String title,
        String description,
        int duration,
        int noOfQuestions,
        int passingPercentage,
        Boolean shouldShuffle,
        Boolean shouldRandomlyPick,
        Date deletedAt
) {
    public TestResponseDTO(Test test) {
        this(
                test.getId(),
                test.getTitle(),
                test.getDescription(),
                test.getDuration(),
                test.getNoOfQuestions(),
                test.getPassingPercentage(),
                test.shouldShuffle(),
                test.shouldRandomlyPick(),
                test.getDeletedAt()
        );
    }
}
