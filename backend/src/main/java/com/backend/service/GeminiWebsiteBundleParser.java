package com.backend.service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts fenced code blocks from Gemini text (same contract as the legacy marketing website flow).
 */
public final class GeminiWebsiteBundleParser {

    private static final Pattern HTML = Pattern.compile("```(?:html|HTML)([\\s\\S]*?)```");
    private static final Pattern CSS = Pattern.compile("```(?:css|CSS)([\\s\\S]*?)```");
    private static final Pattern JS = Pattern.compile("```(?:javascript|js|JS)([\\s\\S]*?)```");

    private GeminiWebsiteBundleParser() {
    }

    public static Map<String, String> parseBundle(String content) {
        Map<String, String> files = new LinkedHashMap<>();
        if (content == null) {
            files.put("index.html", "");
            files.put("style.css", "");
            files.put("script.js", "");
            return files;
        }

        files.put("index.html", firstGroup(HTML, content));
        files.put("style.css", firstGroup(CSS, content));
        files.put("script.js", firstGroup(JS, content));
        return files;
    }

    public static String extractTextFromGeminiResponse(Map<String, Object> body) {
        if (body == null) {
            return "";
        }
        Object candidatesObj = body.get("candidates");
        if (!(candidatesObj instanceof java.util.List<?> list) || list.isEmpty()) {
            return "";
        }
        Object first = list.get(0);
        if (!(first instanceof Map<?, ?> cand)) {
            return "";
        }
        Object content = cand.get("content");
        if (!(content instanceof Map<?, ?> cMap)) {
            return "";
        }
        Object parts = cMap.get("parts");
        if (!(parts instanceof java.util.List<?> partsList) || partsList.isEmpty()) {
            return "";
        }
        Object part0 = partsList.get(0);
        if (!(part0 instanceof Map<?, ?> pMap)) {
            return "";
        }
        Object text = pMap.get("text");
        return text == null ? "" : text.toString();
    }

    public static boolean bundleLooksUsable(Map<String, String> bundle) {
        if (bundle == null) {
            return false;
        }
        String html = bundle.getOrDefault("index.html", "").trim();
        if (html.length() < 80) {
            return false;
        }
        String lower = html.toLowerCase(Locale.ROOT);
        return lower.contains("<html") || lower.contains("<!doctype");
    }

    private static String firstGroup(Pattern pattern, String content) {
        Matcher m = pattern.matcher(content);
        return m.find() ? m.group(1).trim() : "";
    }
}
