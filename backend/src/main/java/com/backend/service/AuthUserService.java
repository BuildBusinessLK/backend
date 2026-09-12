package com.backend.service;

import com.backend.dto.auth.AuthResponse;
import com.backend.dto.auth.LoginRequest;
import com.backend.dto.auth.ProfileUpdateRequest;
import com.backend.dto.auth.RegisterRequest;
import com.backend.dto.auth.UserProfileDto;
import com.backend.entity.User;
import com.backend.entity.UserProfile;
import com.backend.repository.UserProfileRepository;
import com.backend.repository.UserRepository;
import com.backend.security.CustomUserDetails;
import com.backend.security.JwtService;
import com.backend.user.UserRole;
import com.backend.user.UserStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthUserService {

    private static final Logger log = LoggerFactory.getLogger(AuthUserService.class);

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    @Value("${google.client.id:558034937251-f6ii8p6fb0iar9gmkm9rqdic27mesas1.apps.googleusercontent.com}")
    private String googleClientId;

    @Value("${app.mail.from:havindufonseka@gmail.com}")
    private String mailFrom;

    @Value("${app.name:BuildBusinessLK}")
    private String appName;

    @Value("${app.client.url:http://localhost:3000}")
    private String clientUrl;

    public AuthUserService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            ObjectProvider<JavaMailSender> mailSender,
            RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.mailSender = mailSender.getIfAvailable();
        this.restTemplate = restTemplate;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("An account with this email already exists.");
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(1000000));

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setVerificationOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(15));
        user = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullName(request.getFullName().trim());
        userProfileRepository.save(profile);

        sendOtpEmail(email, otp);

        String token = jwtService.generateToken(user);
        AuthResponse res = new AuthResponse();
        res.setToken(token);
        res.setUser(toDto(user));
        return res;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        try {
            Authentication auth =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
            CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
            User user = userRepository.findById(details.getId()).orElseThrow();
            String token = jwtService.generateToken(user);
            AuthResponse res = new AuthResponse();
            res.setToken(token);
            res.setUser(toDto(user));
            return res;
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password.");
        }
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long userId) {
        if (userId == null) {
            throw new IllegalStateException("Authentication required");
        }
        User user = userRepository.findById(userId).orElseThrow();
        return toDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, ProfileUpdateRequest req) {
        if (userId == null) {
            throw new IllegalStateException("Authentication required");
        }
        ProfileUpdateRequest safeReq = req != null ? req : new ProfileUpdateRequest();
        User user = userRepository.findById(userId).orElseThrow();
        UserProfile profile = userProfileRepository.findByUser_Id(userId).orElseGet(() -> {
            UserProfile p = new UserProfile();
            p.setUser(user);
            p.setFullName("");
            return p;
        });
        if (safeReq.getFullName() != null) {
            String n = safeReq.getFullName().trim();
            if (!n.isEmpty()) {
                profile.setFullName(n);
            }
        }
        if (safeReq.getPhone() != null) {
            profile.setPhone(trimToNull(safeReq.getPhone()));
        }
        if (safeReq.getExperienceLevel() != null) {
            profile.setExperienceLevel(trimToNull(safeReq.getExperienceLevel()));
        }
        if (safeReq.getPreferredLanguage() != null) {
            profile.setPreferredLanguage(trimToNull(safeReq.getPreferredLanguage()));
        }
        userProfileRepository.save(profile);
        return toDto(user);
    }

    public UserProfileDto toDto(User user) {
        UserProfile profile = userProfileRepository.findByUser_Id(user.getId()).orElse(null);
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setEmailVerified(user.getEmailVerified());
        if (profile != null) {
            dto.setFullName(profile.getFullName());
            dto.setPhone(profile.getPhone());
            dto.setDistrict(profile.getDistrict());
            dto.setExperienceLevel(profile.getExperienceLevel());
            dto.setPreferredLanguage(profile.getPreferredLanguage());
        }
        return dto;
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        if (email == null || otp == null) {
            throw new IllegalArgumentException("Email and verification code are required.");
        }
        String cleanEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account found for this email address."));
        if (user.getVerificationOtp() == null || !user.getVerificationOtp().equals(otp.trim())) {
            throw new IllegalArgumentException("Invalid verification code. Please check and try again.");
        }
        if (user.getOtpExpiry() != null && LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new IllegalArgumentException("Verification code has expired. Please request a new code.");
        }
        user.setEmailVerified(true);
        user.setVerificationOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public void resendOtp(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        String cleanEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account found for this email address."));
        String otp = String.format("%06d", new java.util.Random().nextInt(1000000));
        user.setVerificationOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        sendOtpEmail(cleanEmail, otp);
    }

    @Transactional
    public AuthResponse googleLogin(String credential) {
        if (credential == null || credential.isBlank()) {
            throw new IllegalArgumentException("Missing Google credential.");
        }
        try {
            Map<?, ?> payload = null;
            String cleanToken = credential.trim();

            // 1. Try id_token verification
            try {
                String tokenUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + cleanToken;
                payload = restTemplate.getForObject(tokenUrl, Map.class);
            } catch (Exception ex) {
                log.debug("id_token verification skipped or failed: {}", ex.getMessage());
            }

            // 2. Try userinfo endpoint with access_token
            if (payload == null || !payload.containsKey("email")) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(cleanToken);
                    HttpEntity<Void> entity = new HttpEntity<>(headers);
                    ResponseEntity<Map> userinfoRes = restTemplate.exchange(
                            "https://www.googleapis.com/oauth2/v3/userinfo",
                            HttpMethod.GET,
                            entity,
                            Map.class
                    );
                    payload = userinfoRes.getBody();
                } catch (Exception ex) {
                    log.debug("userinfo endpoint lookup failed: {}", ex.getMessage());
                }
            }

            // 3. Fallback: try tokeninfo?access_token=
            if (payload == null || !payload.containsKey("email")) {
                try {
                    String tokenUrl = "https://oauth2.googleapis.com/tokeninfo?access_token=" + cleanToken;
                    payload = restTemplate.getForObject(tokenUrl, Map.class);
                } catch (Exception ex) {
                    log.debug("access_token tokeninfo lookup failed: {}", ex.getMessage());
                }
            }

            if (payload == null || !payload.containsKey("email")) {
                throw new IllegalStateException("Google authentication failed. Could not retrieve verified email.");
            }

            String email = String.valueOf(payload.get("email")).trim().toLowerCase();
            String name = payload.containsKey("name") ? String.valueOf(payload.get("name")).trim()
                    : (payload.containsKey("given_name") ? String.valueOf(payload.get("given_name")).trim() : "Google User");

            User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setRole(UserRole.USER);
                user.setStatus(UserStatus.ACTIVE);
                user.setEmailVerified(true);
                user = userRepository.save(user);

                UserProfile profile = new UserProfile();
                profile.setUser(user);
                profile.setFullName(name);
                userProfileRepository.save(profile);
            } else {
                user.setEmailVerified(true);
                userRepository.save(user);
            }

            String token = jwtService.generateToken(user);
            AuthResponse res = new AuthResponse();
            res.setToken(token);
            res.setUser(toDto(user));
            return res;
        } catch (Exception ex) {
            log.error("Google authentication failed: {}", ex.getMessage());
            throw new IllegalStateException("Failed to verify Google account: " + ex.getMessage());
        }
    }

    private void sendOtpEmail(String email, String otp) {
        log.info("[EMAIL VERIFICATION] Verification OTP for {}: {}", email, otp);
        if (mailSender != null) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(mailFrom);
                msg.setTo(email);
                msg.setSubject(appName + ": Your Email Verification Code");
                msg.setText("Welcome to " + appName + "!\n\n"
                        + "Your 6-digit verification code is: " + otp + "\n\n"
                        + "This code is valid for 15 minutes.\n\n"
                        + "Sign in to your account at: " + clientUrl + "\n\n"
                        + "Thank you,\n" + appName + " Team");
                mailSender.send(msg);
            } catch (Exception ex) {
                log.warn("Could not send verification email via SMTP: {}. Verification code is logged above for dev testing.", ex.getMessage());
            }
        }
    }

    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        String cleanEmail = email.trim().toLowerCase();
        userRepository.findByEmailIgnoreCase(cleanEmail).ifPresent(user -> {
            String otp = String.format("%06d", new java.util.Random().nextInt(1000000));
            user.setVerificationOtp(otp);
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(30));
            userRepository.save(user);
            log.info("[PASSWORD RESET] Reset code for {}: {}", cleanEmail, otp);
            if (mailSender != null) {
                try {
                    SimpleMailMessage msg = new SimpleMailMessage();
                    msg.setFrom(mailFrom);
                    msg.setTo(cleanEmail);
                    msg.setSubject(appName + ": Password Reset Code");
                    msg.setText("Hello,\n\n"
                            + "You requested a password reset for your " + appName + " account.\n\n"
                            + "Your reset code is: " + otp + "\n\n"
                            + "Access your account at: " + clientUrl + "\n\n"
                            + "If you did not request this, please ignore this email.\n\n"
                            + "Best regards,\n" + appName + " Team");
                    mailSender.send(msg);
                } catch (Exception ex) {
                    log.warn("Could not send password reset email: {}", ex.getMessage());
                }
            }
        });
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
