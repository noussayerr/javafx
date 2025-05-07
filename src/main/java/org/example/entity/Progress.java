package org.example.entity;

import java.time.LocalDateTime;

public class Progress {
    private int id;
    private int userId;
    private int coursId;
    private int fichierId;
    private LocalDateTime completedAt;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCoursId() {
        return coursId;
    }

    public void setCoursId(int coursId) {
        this.coursId = coursId;
    }

    public int getFichierId() {
        return fichierId;
    }

    public void setFichierId(int fichierId) {
        this.fichierId = fichierId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}