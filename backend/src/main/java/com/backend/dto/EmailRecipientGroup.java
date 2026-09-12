package com.backend.dto;

/** A dynamic audience segment. Recipient addresses are deliberately not exposed to the client. */
public class EmailRecipientGroup {

    private String id;
    private String label;
    private int recipientCount;

    public EmailRecipientGroup() {
    }

    public EmailRecipientGroup(String id, String label, int recipientCount) {
        this.id = id;
        this.label = label;
        this.recipientCount = recipientCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public int getRecipientCount() { return recipientCount; }
    public void setRecipientCount(int recipientCount) { this.recipientCount = recipientCount; }
}
