package com.backend.domain;

/**
 * Extensible constants for actions the AI assistant can suggest to the user.
 * Only GENERATE_WEBSITE is active now; others are scaffolded for future use.
 *
 * Frontend: AIChatPage.js reads the `action` field on AI messages and renders
 * an appropriate CTA button.
 */
public final class AssistantActions {
    private AssistantActions() {}

    /** Active — triggers "Create website" button in the chat UI */
    public static final String GENERATE_WEBSITE = "GENERATE_WEBSITE";

    /** Future — social media marketing flow (not yet implemented) */
    public static final String SOCIAL_MARKETING = "SOCIAL_MARKETING";

    /** Future — email campaign builder (not yet implemented) */
    public static final String EMAIL_CAMPAIGN = "EMAIL_CAMPAIGN";

    /** Future — navigate to business profile editor */
    public static final String VIEW_PROFILE = "VIEW_PROFILE";

    /** Fallback when no specific action applies */
    public static final String UNKNOWN = "UNKNOWN";
}
