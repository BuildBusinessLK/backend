package com.backend.dto.email;

import com.backend.domain.CampaignGoal;
import com.backend.domain.Sector;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateCampaignRequest {

    @NotBlank(message = "Campaign title is required")
    private String title;

    private CampaignGoal goal = CampaignGoal.GENERAL_ANNOUNCEMENT;

    private Sector targetSector;

    private String targetAudienceSummary;

    @NotBlank(message = "Subject line is required")
    private String subject;

    @NotBlank(message = "Email body is required")
    private String body;

    private List<String> groupKeys;

    private List<Long> groupIds;

    private boolean isMock = true;

    public CreateCampaignRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public CampaignGoal getGoal() { return goal; }
    public void setGoal(CampaignGoal goal) { this.goal = goal; }

    public Sector getTargetSector() { return targetSector; }
    public void setTargetSector(Sector targetSector) { this.targetSector = targetSector; }

    public String getTargetAudienceSummary() { return targetAudienceSummary; }
    public void setTargetAudienceSummary(String targetAudienceSummary) { this.targetAudienceSummary = targetAudienceSummary; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public List<String> getGroupKeys() { return groupKeys; }
    public void setGroupKeys(List<String> groupKeys) { this.groupKeys = groupKeys; }

    public List<Long> getGroupIds() { return groupIds; }
    public void setGroupIds(List<Long> groupIds) { this.groupIds = groupIds; }

    public boolean isMock() { return isMock; }
    public void setMock(boolean mock) { isMock = mock; }
}
