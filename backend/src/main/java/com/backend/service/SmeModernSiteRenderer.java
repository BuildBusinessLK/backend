package com.backend.service;

import com.backend.dto.SmeContactDto;
import com.backend.dto.SmeProductItem;
import com.backend.dto.SmeSocialDto;
import com.backend.dto.SmeWebsiteGenerateRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Deterministic, lightweight SME landing page — always works without an LLM.
 * Single index.html with embedded CSS/JS for simple hosting and preview.
 */
public final class SmeModernSiteRenderer {

    private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9]+");

    private SmeModernSiteRenderer() {
    }

    public static Map<String, String> renderBundle(SmeWebsiteGenerateRequest req) {
        String html = buildSinglePageHtml(req);
        Map<String, String> bundle = new LinkedHashMap<>();
        bundle.put("index.html", html);
        bundle.put("style.css", "/* Inlined in index.html for SME static deploy */\n");
        bundle.put("script.js", "/* Inlined in index.html for SME static deploy */\n");
        return bundle;
    }

    public static String suggestedSubdomain(SmeWebsiteGenerateRequest req) {
        String raw = req != null ? req.getBusinessName() : null;
        if (raw == null || raw.isBlank()) {
            return "your-business";
        }
        String slug = raw.toLowerCase(Locale.ROOT).trim();
        slug = NON_SLUG.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("^-|-$", "");
        return slug.isEmpty() ? "your-business" : slug;
    }

    public static String composePreviewDocument(Map<String, String> bundle) {
        if (bundle == null) {
            return "";
        }
        String html = bundle.getOrDefault("index.html", "").trim();
        if (!html.isEmpty()) {
            return bundle.getOrDefault("index.html", "");
        }
        String css = bundle.getOrDefault("style.css", "");
        String js = bundle.getOrDefault("script.js", "");
        return "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/><style>"
                + css + "</style></head><body><p>Website bundle did not include HTML.</p><script>" + js + "</script></body></html>";
    }

    private static String buildSinglePageHtml(SmeWebsiteGenerateRequest req) {
        Theme t = Theme.from(req != null ? req.getTheme() : null);
        String name = esc(req != null ? req.getBusinessName() : "");
        String tag = esc(req != null ? req.getTagline() : "");
        String industry = esc(req != null ? req.getIndustry() : "");
        String about = esc(req != null ? req.getAbout() : "");

        SmeContactDto c = req != null ? req.getContact() : null;
        String email = esc(c != null ? c.getEmail() : "");
        String phone = esc(c != null ? c.getPhone() : "");
        String address = esc(c != null ? c.getAddress() : "");

        List<ProductRow> products = new ArrayList<>();
        if (req != null && req.getProducts() != null) {
            int i = 0;
            for (SmeProductItem p : req.getProducts()) {
                if (p == null) {
                    continue;
                }
                String pn = esc(p.getName());
                String pd = esc(p.getDescription());
                if (pn.isBlank() && pd.isBlank()) {
                    continue;
                }
                products.add(new ProductRow(++i, pn, pd.isBlank() ? "Crafted with care for wholesale and retail buyers." : pd));
            }
        }
        if (products.isEmpty()) {
            products.add(new ProductRow(1, "Signature offering", "Tell your story — add products from the studio."));
            products.add(new ProductRow(2, "Bulk supply", "Describe grades, packaging, and lead times."));
        }

        List<String> gallery = new ArrayList<>();
        if (req != null && req.getGalleryImageUrls() != null) {
            for (String u : req.getGalleryImageUrls()) {
                if (u != null && !u.isBlank()) {
                    gallery.add(u.trim());
                }
            }
        }
        if (gallery.isEmpty()) {
            gallery.add("https://images.unsplash.com/photo-1580910534554-576d6f6b2f9f?auto=format&fit=crop&w=1200&q=70");
            gallery.add("https://images.unsplash.com/photo-1501004318641-b39e6451bec6?auto=format&fit=crop&w=1200&q=70");
        }

        String socialHtml = socialLinks(req != null ? req.getSocial() : null);

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1" />
                  <title>__TITLE__</title>
                  <meta name="description" content="__DESC__" />
                  <style>
                  :root {
                    --bg0: __BG0__;
                    --bg1: __BG1__;
                    --ink: __INK__;
                    --muted: __MUTED__;
                    --card: __CARD__;
                    --accent: __ACCENT__;
                    --accent2: __ACCENT2__;
                  }
                  * { box-sizing: border-box; margin: 0; padding: 0; }
                  body {
                    font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, "Apple Color Emoji", "Segoe UI Emoji";
                    color: var(--ink);
                    background: radial-gradient(1200px 600px at 10% -10%, rgba(255,255,255,.25), transparent 55%),
                                linear-gradient(180deg, var(--bg0), var(--bg1));
                    line-height: 1.55;
                  }
                  a { color: inherit; }
                  .shell { max-width: 1120px; margin: 0 auto; padding: 0 20px; }
                  header.hero {
                    padding: 48px 0 28px;
                  }
                  .brand {
                    display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap;
                  }
                  .logo {
                    width: 48px; height: 48px; border-radius: 14px;
                    background: linear-gradient(135deg, var(--accent), var(--accent2));
                    box-shadow: 0 12px 40px rgba(0,0,0,.12);
                  }
                  .title {
                    font-size: clamp(1.6rem, 3vw, 2.25rem);
                    font-weight: 800;
                    letter-spacing: -0.03em;
                  }
                  .pill {
                    display: inline-flex; align-items: center; gap: 8px;
                    padding: 8px 12px; border-radius: 999px;
                    background: var(--card);
                    border: 1px solid rgba(0,0,0,.08);
                    font-size: 0.85rem; color: var(--muted); font-weight: 600;
                  }
                  .hero-grid {
                    margin-top: 28px;
                    display: grid;
                    grid-template-columns: 1.15fr 0.85fr;
                    gap: 24px;
                    align-items: stretch;
                  }
                  @media (max-width: 900px) { .hero-grid { grid-template-columns: 1fr; } }
                  .hero-card {
                    background: var(--card);
                    border: 1px solid rgba(0,0,0,.08);
                    border-radius: 20px;
                    padding: 28px;
                    box-shadow: 0 20px 60px rgba(0,0,0,.08);
                  }
                  .hero h1 {
                    font-size: clamp(2rem, 4vw, 3rem);
                    font-weight: 850;
                    letter-spacing: -0.04em;
                    line-height: 1.05;
                    margin-bottom: 12px;
                  }
                  .hero p.lead {
                    color: var(--muted);
                    font-size: 1.05rem;
                    margin-bottom: 20px;
                    max-width: 52ch;
                  }
                  .cta-row { display: flex; flex-wrap: wrap; gap: 10px; }
                  .btn {
                    display: inline-flex; align-items: center; justify-content: center;
                    padding: 12px 18px;
                    border-radius: 12px;
                    border: none;
                    cursor: pointer;
                    font-weight: 700;
                    text-decoration: none;
                    transition: transform .12s ease, box-shadow .12s ease;
                  }
                  .btn-primary {
                    background: linear-gradient(135deg, var(--accent), var(--accent2));
                    color: #fff;
                    box-shadow: 0 14px 40px rgba(0,0,0,.18);
                  }
                  .btn-ghost {
                    background: transparent;
                    border: 1px solid rgba(0,0,0,.12);
                    color: var(--ink);
                  }
                  .btn:hover { transform: translateY(-1px); }
                  .banner {
                    border-radius: 20px;
                    min-height: 220px;
                    background:
                      linear-gradient(120deg, rgba(255,255,255,.15), transparent),
                      url('__HERO_IMG__') center/cover no-repeat;
                    border: 1px solid rgba(0,0,0,.08);
                  }
                  section { padding: 44px 0; }
                  section h2 {
                    font-size: 1.35rem;
                    font-weight: 800;
                    letter-spacing: -0.02em;
                    margin-bottom: 14px;
                  }
                  .grid-3 {
                    display: grid;
                    grid-template-columns: repeat(3, 1fr);
                    gap: 14px;
                  }
                  @media (max-width: 900px) { .grid-3 { grid-template-columns: 1fr; } }
                  .card {
                    background: var(--card);
                    border: 1px solid rgba(0,0,0,.08);
                    border-radius: 16px;
                    padding: 18px;
                  }
                  .card h3 { font-size: 1.05rem; margin-bottom: 6px; }
                  .muted { color: var(--muted); font-size: 0.95rem; }
                  .gallery {
                    display: grid;
                    grid-template-columns: repeat(3, 1fr);
                    gap: 10px;
                  }
                  @media (max-width: 900px) { .gallery { grid-template-columns: 1fr 1fr; } }
                  .gallery img {
                    width: 100%;
                    height: 160px;
                    object-fit: cover;
                    border-radius: 14px;
                    border: 1px solid rgba(0,0,0,.08);
                  }
                  form .row { display: grid; gap: 10px; margin-bottom: 12px; }
                  label { font-size: 0.85rem; font-weight: 700; color: var(--muted); }
                  input, textarea {
                    width: 100%;
                    padding: 12px 12px;
                    border-radius: 12px;
                    border: 1px solid rgba(0,0,0,.12);
                    background: rgba(255,255,255,.75);
                    font: inherit;
                  }
                  footer {
                    padding: 28px 0 40px;
                    color: var(--muted);
                    font-size: 0.9rem;
                  }
                  .foot-row { display: flex; flex-wrap: wrap; gap: 12px; justify-content: space-between; align-items: center; }
                  .social a {
                    display: inline-flex;
                    margin-right: 10px;
                    font-weight: 700;
                    color: var(--ink);
                    text-decoration: none;
                    border-bottom: 1px solid rgba(0,0,0,.15);
                  }
                  </style>
                </head>
                <body>
                  <header class="hero">
                    <div class="shell">
                      <div class="brand">
                        <div style="display:flex; gap:12px; align-items:center;">
                          <div class="logo" aria-hidden="true"></div>
                          <div>
                            <div class="title">__NAME__</div>
                            <div class="pill"><span>__INDUSTRY__</span></div>
                          </div>
                        </div>
                        <a class="btn btn-ghost" href="#contact">Inquire</a>
                      </div>
                      <div class="hero-grid">
                        <div class="hero-card">
                          <h1>__TAG__</h1>
                          <p class="lead">__ABOUT__</p>
                          <div class="cta-row">
                            <a class="btn btn-primary" href="#products">View offerings</a>
                            <a class="btn btn-ghost" href="#gallery">Gallery</a>
                          </div>
                        </div>
                        <div class="banner" role="img" aria-label="Business banner"></div>
                      </div>
                    </div>
                  </header>

                  <main class="shell">
                    <section id="products">
                      <h2>Products &amp; services</h2>
                      <div class="grid-3">
                        __PRODUCTS__
                      </div>
                    </section>

                    <section id="gallery">
                      <h2>Gallery</h2>
                      <div class="gallery">
                        __GALLERY__
                      </div>
                    </section>

                    <section id="contact">
                      <h2>Inquiry</h2>
                      <div class="card">
                        <p class="muted" style="margin-bottom:14px;">
                          <strong>Contact:</strong>
                          __EMAIL__ · __PHONE__<br/>
                          __ADDRESS__
                        </p>
                        <form id="inq">
                          <div class="row">
                            <label for="n">Name</label>
                            <input id="n" name="name" required autocomplete="name" />
                          </div>
                          <div class="row">
                            <label for="e">Email</label>
                            <input id="e" name="email" type="email" required autocomplete="email" />
                          </div>
                          <div class="row">
                            <label for="m">Message</label>
                            <textarea id="m" name="message" rows="4" required></textarea>
                          </div>
                          <button class="btn btn-primary" type="submit">Send inquiry</button>
                        </form>
                      </div>
                    </section>
                  </main>

                  <footer>
                    <div class="shell foot-row">
                      <div>&copy; __YEAR__ __NAME__. Crafted via BuildBusinessLK.</div>
                      <div class="social">__SOCIAL__</div>
                    </div>
                  </footer>

                  <script>
                  (function(){
                    var f = document.getElementById('inq');
                    if (!f) return;
                    f.addEventListener('submit', function(e){
                      e.preventDefault();
                      alert('Thanks! This demo form is static — wire it to your platform email API next.');
                      f.reset();
                    });
                    document.querySelectorAll('a[href^="#"]').forEach(function(a){
                      a.addEventListener('click', function(ev){
                        var id = this.getAttribute('href').slice(1);
                        var el = document.getElementById(id);
                        if (el) { ev.preventDefault(); el.scrollIntoView({behavior:'smooth', block:'start'}); }
                      });
                    });
                  })();
                  </script>
                </body>
                </html>
                """
                .replace("__TITLE__", name.isEmpty() ? "SME Website" : name + " · " + industry)
                .replace("__DESC__", (tag.isEmpty() ? industry : tag + " — " + industry))
                .replace("__BG0__", t.bg0)
                .replace("__BG1__", t.bg1)
                .replace("__INK__", t.ink)
                .replace("__MUTED__", t.muted)
                .replace("__CARD__", t.card)
                .replace("__ACCENT__", t.accent)
                .replace("__ACCENT2__", t.accent2)
                .replace("__HERO_IMG__", gallery.isEmpty() ? "" : escUrl(gallery.get(0)))
                .replace("__NAME__", name.isEmpty() ? "Your business" : name)
                .replace("__INDUSTRY__", industry.isEmpty() ? "Sri Lankan SME" : industry)
                .replace("__TAG__", tag.isEmpty() ? "Premium quality for domestic and export markets" : tag)
                .replace("__ABOUT__", about.isEmpty()
                        ? "We serve buyers who value transparent sourcing, consistent quality, and dependable delivery across Coconut, Kithul, and Palmyrah value chains."
                        : about)
                .replace("__PRODUCTS__", productCards(products))
                .replace("__GALLERY__", galleryCells(gallery))
                .replace("__EMAIL__", email.isEmpty() ? "hello@yourbusiness.lk" : email)
                .replace("__PHONE__", phone.isEmpty() ? "+94 · · · · · · · ·" : phone)
                .replace("__ADDRESS__", address.isEmpty() ? "Sri Lanka" : address)
                .replace("__SOCIAL__", socialHtml.isEmpty() ? "<span class='muted'>Add social links later.</span>" : socialHtml)
                .replace("__YEAR__", String.valueOf(java.time.Year.now().getValue()));
    }

    private static String productCards(List<ProductRow> rows) {
        StringBuilder sb = new StringBuilder();
        for (ProductRow r : rows) {
            sb.append("""
                    <div class="card">
                      <h3>__N__</h3>
                      <p class="muted">__D__</p>
                    </div>
                    """.replace("__N__", r.name).replace("__D__", r.desc));
        }
        return sb.toString();
    }

    private static String galleryCells(List<String> urls) {
        StringBuilder sb = new StringBuilder();
        int cap = Math.min(urls.size(), 6);
        for (int i = 0; i < cap; i++) {
            String u = escUrl(urls.get(i));
            sb.append("<img src=\"").append(u).append("\" alt=\"Gallery image ").append(i + 1).append("\" loading=\"lazy\" />");
        }
        return sb.toString();
    }

    private static String socialLinks(SmeSocialDto s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        link(sb, "Facebook", s.getFacebook());
        link(sb, "Instagram", s.getInstagram());
        link(sb, "YouTube", s.getYoutube());
        link(sb, "LinkedIn", s.getLinkedin());
        link(sb, "TikTok", s.getTiktok());
        return sb.toString();
    }

    private static void link(StringBuilder sb, String label, String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String safe = escUrl(url.trim());
        sb.append("<a href=\"").append(safe).append("\" target=\"_blank\" rel=\"noopener noreferrer\">")
                .append(label).append("</a>");
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /** Basic URL validation: allow http(s) only for gallery/social inserts */
    private static String escUrl(String u) {
        String t = u.trim();
        String lower = t.toLowerCase(Locale.ROOT);
        if (lower.startsWith("https://") || lower.startsWith("http://")) {
            return esc(t);
        }
        return esc("https://" + t.replaceFirst("^[a-zA-Z]+://", ""));
    }

    private record ProductRow(int idx, String name, String desc) {
    }

    private enum Theme {
        FOREST("#ecfdf5", "#d1fae5", "#0f172a", "#475569", "rgba(255,255,255,.86)", "#15803d", "#0d9488"),
        OCEAN("#eff6ff", "#dbeafe", "#0f172a", "#475569", "rgba(255,255,255,.9)", "#0369a1", "#7c3aed"),
        AMBER("#fffbeb", "#ffedd5", "#0f172a", "#57534e", "rgba(255,255,255,.9)", "#c2410c", "#ca8a04");

        final String bg0;
        final String bg1;
        final String ink;
        final String muted;
        final String card;
        final String accent;
        final String accent2;

        Theme(String bg0, String bg1, String ink, String muted, String card, String accent, String accent2) {
            this.bg0 = bg0;
            this.bg1 = bg1;
            this.ink = ink;
            this.muted = muted;
            this.card = card;
            this.accent = accent;
            this.accent2 = accent2;
        }

        static Theme from(String id) {
            if (id == null) {
                return FOREST;
            }
            return switch (id.toLowerCase(Locale.ROOT)) {
                case "ocean" -> OCEAN;
                case "amber" -> AMBER;
                default -> FOREST;
            };
        }
    }
}
