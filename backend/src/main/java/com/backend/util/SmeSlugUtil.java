package com.backend.util;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class SmeSlugUtil {

    private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9-]+");
    private static final Pattern COLLAPSE_HYPHENS = Pattern.compile("-{2,}");

    private static final Set<String> RESERVED = Set.of(
            "dashboard", "business", "api", "static", "admin", "sign-in", "sign-up",
            "subscriptions", "about", "contact", "netlify-callback", "marketing");

    private SmeSlugUtil() {
    }

    /**
     * Lowercase kebab-case; strips invalid characters. Empty if nothing usable remains.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String slug = raw.toLowerCase(Locale.ROOT).trim().replace(' ', '-');
        slug = NON_SLUG.matcher(slug).replaceAll("-");
        slug = COLLAPSE_HYPHENS.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("^-|-$", "");
        return slug;
    }

    public static boolean isReserved(String slug) {
        if (slug == null || slug.isBlank()) {
            return true;
        }
        return RESERVED.contains(slug.toLowerCase(Locale.ROOT));
    }
}
