package dev.mirodil.testing_system.models;

import dev.mirodil.testing_system.models.enums.TestEventStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Table("test_events")
public class TestEvent {
    @Id
    private Long id;
    private Long testTakerId;
    private Long testId;
    @Column("event_datetime")
    private Date eventDateTime;
    private TestEventStatus status;
    private Double score = null;
    private Boolean isPassed = null;
    private Date startedAt;
    private Date finishedAt;
    private Date createdAt;
    private String testAttempt;
    @Transient
    private User testTaker;
//    @Transient
//    private Test test;
    //TODO: allowedFilters and allowedSorts


    public TestEvent() {
    }

    public TestEvent(Long testTakerId, Long testId, Date eventDateTime, TestEventStatus status) {
        this.testTakerId = testTakerId;
        this.testId = testId;
        this.eventDateTime = eventDateTime;
        this.status = status;
    }

    public TestEvent(Long id, Long testTakerId, Long testId, Date eventDateTime, TestEventStatus status, Double score, Boolean isPassed, Date startedAt, Date finishedAt, Date createdAt) {
        this(testTakerId, testId, eventDateTime, status);
        this.id = id;
        this.score = score;
        this.isPassed = isPassed;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTestTakerId() {
        return testTakerId;
    }

    public Long getTestId() {
        return testId;
    }

    public Date getEventDateTime() {
        return eventDateTime;
    }

    public TestEventStatus getStatus() {
        return status;
    }

    public void setStatus(TestEventStatus status) {
        this.status = status;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Boolean getPassed() {
        return isPassed;
    }

    public void setPassed(Boolean passed) {
        isPassed = passed;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getTestAttempt() {
        return testAttempt;
    }

    public void setTestAttempt(String testAttempt) {
        this.testAttempt = testAttempt;
    }

    public User getTestTaker() {
        return testTaker;
    }

    public void setTestTaker(User testTaker) {
        this.testTaker = testTaker;
    }
}
