package dev.mirodil.testing_system.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Table("tests")
public class Test {
    @Id
    private Long id;
    private String title;
    private String description;
    private int duration;
    private int noOfQuestions;
    private int passingPercentage;
    private boolean shouldShuffle;
    private boolean shouldRandomlyPick;
    private Date deleted_at;

    public Test() {
    }

    public Test(String title, String description, int duration, int noOfQuestions, int passingPercentage, boolean shouldShuffle, boolean shouldRandomlyPick) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.noOfQuestions = noOfQuestions;
        this.passingPercentage = passingPercentage;
        this.shouldShuffle = shouldShuffle;
        this.shouldRandomlyPick = shouldRandomlyPick;
    }

    public Test(Long id, String title, String description, int duration, int noOfQuestions, int passingPercentage, boolean shouldShuffle, boolean shouldRandomlyPick, Date deleted_at) {
        this(
                title,
                description,
                duration,
                noOfQuestions,
                passingPercentage,
                shouldShuffle,
                shouldRandomlyPick
        );
        this.id = id;
        this.deleted_at = deleted_at;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getDuration() {
        return duration;
    }

    public int getNoOfQuestions() {
        return noOfQuestions;
    }

    public double getPassingPercentage() {
        return passingPercentage;
    }

    public boolean shouldShuffle() {
        return shouldShuffle;
    }

    public boolean shouldRandomlyPick() {
        return shouldRandomlyPick;
    }

    public Date getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(Date deleted_at) {
        this.deleted_at = deleted_at;
    }
}
