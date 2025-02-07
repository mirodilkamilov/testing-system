package dev.mirodil.testing_system.models;

import dev.mirodil.testing_system.models.enums.TestEventStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Table("test_events")
public class TestEvent {
    private static final Set<String> ALLOWED_SORT_ATTRIBUTES = Set.of(
            "testEventId", "testTakerId", "testId", "eventDatetime", "testEventStatus", "isPassed", "testEventCreatedAt",
            "email", "fname", "lname", "title"
    );
    private static final Map<String, Class<?>> ALLOWED_FILTER_ATTRIBUTES = Map.of(
            "testEventStatus", String.class,
            "isPassed", Boolean.class,
            "email", String.class,
            "fname", String.class,
            "lname", String.class,
            "title", String.class
    );
    // TODO: add custom eventBetweenDatetime, enums - testEventStatus (re-think) filter

    @Id
    private Long id;
    private Long testTakerId;
    private Long testId;
    @Column("event_datetime")
    private Date eventDateTime;
    private TestEventStatus status;
    private Float scorePoints = null;
    private Integer scorePercentage = null;
    private Boolean isPassed = null;
    private Date startedAt;
    private Date finishedAt;
    private Date createdAt;
    private List<TestAttempt> testAttempt;
    @Transient
    private User testTaker;
    @Transient
    private Test test;

    public TestEvent() {
    }

    public TestEvent(Long testTakerId, Long testId, Date eventDateTime, TestEventStatus status) {
        this.testTakerId = testTakerId;
        this.testId = testId;
        this.eventDateTime = eventDateTime;
        this.status = status;
    }

    public TestEvent(Long id, Long testTakerId, Long testId, Date eventDateTime, TestEventStatus status, Float scorePoints, Integer scorePercentage, Boolean isPassed, Date startedAt, Date finishedAt, Date createdAt) {
        this(testTakerId, testId, eventDateTime, status);
        this.id = id;
        this.scorePoints = scorePoints;
        this.scorePercentage = scorePercentage;
        this.isPassed = isPassed;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.createdAt = createdAt;
    }

    public static Set<String> getAllowedSortAttributes() {
        return ALLOWED_SORT_ATTRIBUTES;
    }

    public static Map<String, Class<?>> getAllowedFilterAttributes() {
        return ALLOWED_FILTER_ATTRIBUTES;
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

    public Float getScorePoints() {
        return scorePoints;
    }

    public void setScorePoints(Float scorePoints) {
        this.scorePoints = scorePoints;
    }

    public Integer getScorePercentage() {
        return scorePercentage;
    }

    public void setScorePercentage(Integer scorePercentage) {
        this.scorePercentage = scorePercentage;
    }

    public Boolean getIsPassed() {
        return isPassed;
    }

    public void setIsPassed(Boolean isPassed) {
        this.isPassed = isPassed;
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

    public List<TestAttempt> getTestAttempt() {
        return testAttempt;
    }

    public void setTestAttempt(List<TestAttempt> testAttempt) {
        this.testAttempt = testAttempt;
    }

    public User getTestTaker() {
        return testTaker;
    }

    public void setTestTaker(User testTaker) {
        this.testTaker = testTaker;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }
}
