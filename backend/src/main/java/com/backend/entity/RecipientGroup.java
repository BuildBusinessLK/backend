package com.backend.entity;

import com.backend.domain.RecipientGroupType;
import com.backend.domain.Sector;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recipient_groups")
public class RecipientGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_key", nullable = false, unique = true, length = 100)
    private String groupKey;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false, length = 50)
    private RecipientGroupType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Sector sector;

    @Column(length = 500)
    private String description;

    @Column(name = "is_system", nullable = false)
    private boolean isSystem = true;

    @Column(name = "recipient_count", nullable = false)
    private int recipientCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public RecipientGroup() {}

    public RecipientGroup(String groupKey, String name, RecipientGroupType type, Sector sector, String description, boolean isSystem) {
        this.groupKey = groupKey;
        this.name = name;
        this.type = type;
        this.sector = sector;
        this.description = description;
        this.isSystem = isSystem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RecipientGroupType getType() {
        return type;
    }

    public void setType(RecipientGroupType type) {
        this.type = type;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public int getRecipientCount() {
        return recipientCount;
    }

    public void setRecipientCount(int recipientCount) {
        this.recipientCount = recipientCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
