package com.tiffin.common.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 40)
    private String id;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Each entity must define its own prefix.
     * User entity returns "usr_"
     * Order entity returns "ord_"
     * Payment entity returns "pay_"
     * This method is called by @PrePersist to build the full ID.
     */
    protected abstract String getIdPrefix();

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            // Builds: "usr_" + "550e8400-e29b-41d4-a716-446655440000"
            // Remove hyphens to keep it cleaner: "usr_550e8400e29b41d4a716446655440000"
            this.id = getIdPrefix() + UUID.randomUUID().toString().replace("-", "");
        }
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}