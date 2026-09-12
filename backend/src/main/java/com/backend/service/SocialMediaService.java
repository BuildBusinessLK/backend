package com.backend.service;

import com.backend.dto.SocialAdRequest;
import com.backend.dto.SocialAdResponse;
import com.backend.dto.SocialPostResponse;
import com.backend.dto.SocialPostsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SocialMediaService {

    private static final Logger log = LoggerFactory.getLogger(SocialMediaService.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final AiHordeService aiHordeService;

    public SocialMediaService(
            ObjectMapper objectMapper,
            AiHordeService aiHordeService) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.aiHordeService = aiHordeService;
    }

    public SocialAdResponse generateAd(SocialAdRequest request) {
        String idea = safeTrim(request.getIdea());
        String tone = normalize(request.getTone(), "professional");
        String platform = normalize(request.getPlatform(), "facebook");

        log.info("Generating ad for platform={} tone={}", platform, tone);

        if (hasOpenAiKey()) {
            SocialAdResponse generated = generateAdWithOpenAi(idea, tone, platform);
            if (generated != null) {
                return generated;
            }
        }

        String headline = buildHeadline(idea, tone, platform);
        String description = buildDescription(idea, tone, platform);
        String cta = buildCta(platform, tone);

        return new SocialAdResponse(headline, description, cta);
    }

    public List<SocialPostResponse> generatePosts(SocialPostsRequest request) {
        String idea = safeTrim(request.getIdea());
        List<String> platforms = request.getPlatforms();

        if (platforms == null || platforms.isEmpty()) {
            platforms = Arrays.asList("facebook", "instagram", "linkedin");
        }

        if (hasOpenAiKey()) {
            List<SocialPostResponse> generated = generatePostsWithOpenAi(idea, platforms);
            if (generated != null && !generated.isEmpty()) {
                return generated;
            }
        }

        List<SocialPostResponse> posts = new ArrayList<>();
        for (String platform : platforms) {
            String normalizedPlatform = normalize(platform, "facebook");
            posts.add(buildPost(idea, normalizedPlatform));
        }

        return posts;
    }

    private SocialPostResponse buildPost(String idea, String platform) {
        String hook = buildHook(idea, platform);
        String caption = buildCaption(idea, platform);
        String hashtags = buildHashtags(idea, platform);
        return new SocialPostResponse(platform, hook, caption, hashtags);
    }

    private String buildHeadline(String idea, String tone, String platform) {
        String subject = shorten(idea, 40);
        if (idea.isBlank()) {
            subject = "your next campaign";
        }

        if ("instagram".equals(platform)) {
            return capitalize(tone) + " Instagram Ad for " + subject;
        }
        if ("linkedin".equals(platform)) {
            return "Professional " + capitalize(tone) + " Growth Ad";
        }
        if ("whatsapp".equals(platform)) {
            return "Instant Message Offer: " + subject;
        }
        if ("twitter".equals(platform)) {
            return "Stop the Scroll: " + subject;
        }
        return capitalize(tone) + " Facebook Campaign for " + subject;
    }

    private String buildDescription(String idea, String tone, String platform) {
        if (idea.isBlank()) {
            return "Describe your product, offer, or audience to generate a tailored ad.";
        }

        return switch (platform) {
            case "instagram" -> "Create a visually rich campaign around " + idea + " with a " + tone + " voice.";
            case "linkedin" -> "Position " + idea + " for a professional audience with clear business value.";
            case "twitter" -> "Write a concise, high-impact message that drives clicks for " + idea + ".";
            case "whatsapp" -> "Use a conversational message that feels personal and direct for " + idea + ".";
            default -> "Promote " + idea + " with a " + tone + " message built for social engagement.";
        };
    }

    private String buildCta(String platform, String tone) {
        String base = switch (platform) {
            case "instagram" -> "Shop Now";
            case "linkedin" -> "Learn More";
            case "twitter" -> "See Details";
            case "whatsapp" -> "Message Us";
            default -> "Buy Now";
        };

        if ("friendly".equals(tone)) {
            return base + " Today";
        }
        if ("urgent".equals(tone)) {
            return base + " Now";
        }
        return base;
    }

    private String buildHook(String idea, String platform) {
        if (idea.isBlank()) {
            return "🔥 Don’t miss this!";
        }

        return switch (platform) {
            case "instagram" -> "Scroll-stopping idea: " + shorten(idea, 48);
            case "linkedin" -> "A smarter way to grow with " + shorten(idea, 42);
            case "twitter" -> "Big value, short message: " + shorten(idea, 40);
            case "whatsapp" -> "Quick update for you: " + shorten(idea, 44);
            default -> "Try this today: " + shorten(idea, 50);
        };
    }

    private String buildCaption(String idea, String platform) {
        if (idea.isBlank()) {
            return "Share your goal and we’ll draft a complete social post.";
        }

        return switch (platform) {
            case "instagram" -> "Showcase the value of " + idea + " with a polished visual story and a strong offer.";
            case "linkedin" -> "Highlight the business benefit of " + idea + " and invite your audience to take the next step.";
            case "twitter" -> "Keep it direct, useful, and easy to share. " + idea + " can drive quick engagement with the right angle.";
            case "whatsapp" -> "Send a friendly, conversational update about " + idea + " to keep the message personal.";
            default -> "Turn " + idea + " into an engaging social message that invites action.";
        };
    }

    private String buildHashtags(String idea, String platform) {
        List<String> tags = new ArrayList<>();
        tags.add("#marketing");
        tags.add("#business");
        tags.add("#socialmedia");

        String[] words = idea.toLowerCase(Locale.ROOT).split("[^a-z0-9]+");
        for (String word : words) {
            if (word.length() >= 4) {
                tags.add("#" + word);
            }
            if (tags.size() >= 6) {
                break;
            }
        }

        if ("instagram".equals(platform)) {
            tags.add("#instagood");
        } else if ("linkedin".equals(platform)) {
            tags.add("#leadership");
        } else if ("twitter".equals(platform)) {
            tags.add("#thread");
        } else if ("whatsapp".equals(platform)) {
            tags.add("#community");
        }

        return tags.stream().distinct().collect(Collectors.joining(" "));
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalize(String value, String fallback) {
        String normalized = safeTrim(value).toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? fallback : normalized;
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1).toLowerCase(Locale.ROOT);
    }

    private String shorten(String value, int length) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= length) {
            return trimmed;
        }
        return trimmed.substring(0, length - 1).trim() + "…";
    }

    private boolean hasOpenAiKey() {
        return true;
    }

    private SocialAdResponse generateAdWithOpenAi(String idea, String tone, String platform) {
        String prompt = """
                Return JSON only with keys headline, description, cta.
                Create a social ad.
                Idea: %s
                Tone: %s
                Platform: %s
                """.formatted(idea, tone, platform);

        try {
            JsonNode json = callOpenAi(prompt);
            if (json == null) {
                return null;
            }

            return new SocialAdResponse(
                    textOrFallback(json, "headline", buildHeadline(idea, tone, platform)),
                    textOrFallback(json, "description", buildDescription(idea, tone, platform)),
                    textOrFallback(json, "cta", buildCta(platform, tone))
            );
        } catch (Exception ex) {
            log.warn("OpenAI ad generation failed, using fallback: {}", ex.getMessage());
            return null;
        }
    }

    private List<SocialPostResponse> generatePostsWithOpenAi(String idea, List<String> platforms) {
        String prompt = """
                Return JSON only as an array of objects.
                Each object must have keys platform, hook, caption, hashtags.
                Create one post per platform in this exact order: %s
                Idea: %s
                """.formatted(String.join(",", platforms), idea);

        try {
            JsonNode json = callOpenAi(prompt);
            if (json == null || !json.isArray()) {
                return null;
            }

            List<SocialPostResponse> posts = new ArrayList<>();
            for (JsonNode item : json) {
                posts.add(new SocialPostResponse(
                        textOrFallback(item, "platform", "facebook"),
                        textOrFallback(item, "hook", buildHook(idea, "facebook")),
                        textOrFallback(item, "caption", buildCaption(idea, "facebook")),
                        textOrFallback(item, "hashtags", buildHashtags(idea, "facebook"))
                ));
            }
            return posts;
        } catch (Exception ex) {
            log.warn("OpenAI post generation failed, using fallback: {}", ex.getMessage());
            return null;
        }
    }

    private JsonNode callOpenAi(String prompt) throws IOException, InterruptedException {
        try {
            String text = aiHordeService.generateText(prompt);
            if (text == null || text.isBlank()) {
                throw new IOException("AI Horde returned empty response");
            }
            String content = text.trim();
            if (content.startsWith("```")) {
                content = content.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
            }
            return objectMapper.readTree(content);
        } catch (Exception ex) {
            throw new IOException("AI Horde generation failed: " + ex.getMessage(), ex);
        }
    }

    private String textOrFallback(JsonNode node, String fieldName, String fallback) {
        JsonNode value = node == null ? null : node.get(fieldName);
        return value == null || value.asText().isBlank() ? fallback : value.asText();
    }
}
