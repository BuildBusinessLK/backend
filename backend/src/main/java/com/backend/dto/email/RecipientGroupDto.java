package com.backend.dto.email;

import com.backend.domain.RecipientGroupType;
import com.backend.domain.Sector;

public class RecipientGroupDto {
    private Long id;
    private String groupKey;
    private String name;
    private RecipientGroupType type;
    private Sector sector;
    private String description;
    private boolean isSystem;
    private int recipientCount;

    public RecipientGroupDto() {}

    public RecipientGroupDto(Long id, String groupKey, String name, RecipientGroupType type, Sector sector, String description, boolean isSystem, int recipientCount) {
        this.id = id;
        this.groupKey = groupKey;
        this.name = name;
        this.type = type;
        this.sector = sector;
        this.description = description;
        this.isSystem = isSystem;
        this.recipientCount = recipientCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGroupKey() { return groupKey; }
    public void setGroupKey(String groupKey) { this.groupKey = groupKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RecipientGroupType getType() { return type; }
    public void setType(RecipientGroupType type) { this.type = type; }

    public Sector getSector() { return sector; }
    public void setSector(Sector sector) { this.sector = sector; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isSystem() { return isSystem; }
    public void setSystem(boolean system) { isSystem = system; }

    public int getRecipientCount() { return recipientCount; }
    public void setRecipientCount(int recipientCount) { this.recipientCount = recipientCount; }
}
