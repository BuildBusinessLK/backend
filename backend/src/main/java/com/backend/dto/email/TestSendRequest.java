package com.backend.dto.email;

public class TestSendRequest {
    private String testEmail;

    public TestSendRequest() {}

    public TestSendRequest(String testEmail) {
        this.testEmail = testEmail;
    }

    public String getTestEmail() { return testEmail; }
    public void setTestEmail(String testEmail) { this.testEmail = testEmail; }
}
