package com.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@Data
@MappedSuperclass
public class GenericTableConstants {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private String createdBy;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private String updatedBy;

    @PrePersist //JPA lifecycle hook to set default values before saving a new entity
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;

        if (createdBy == null) createdBy = "admin";
        if (updatedBy == null) updatedBy = "admin";
    }

    @PreUpdate //JPA lifecycle hook to update the timestamp and user before updating an existing entity
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (updatedBy == null) updatedBy = "admin";
    }
}

