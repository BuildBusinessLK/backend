package com.backend.service;

import com.backend.dto.auth.AuthResponse;
import com.backend.dto.auth.LoginRequest;
import com.backend.dto.auth.RegisterRequest;
import com.backend.dto.auth.UserProfileDto;
import com.backend.dto.auth.ProfileUpdateRequest;
import com.backend.entity.User;
import com.backend.repository.UserRepository;
import com.backend.security.CustomUserDetails;
import com.backend.security.JwtService;
import com.backend.user.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthUserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("An account with this email already exists.");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setRole(UserRole.USER);
        user = userRepository.save(user);

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
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
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
        User user = userRepository.findById(userId).orElseThrow();
        return toDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, ProfileUpdateRequest req) {
        User user = userRepository.findById(userId).orElseThrow();
        if (req.getFullName() != null) {
            String n = req.getFullName().trim();
            if (!n.isEmpty()) {
                user.setFullName(n);
            }
        }
        if (req.getPhone() != null) {
            user.setPhone(trimToNull(req.getPhone()));
        }
        if (req.getBusinessName() != null) {
            user.setBusinessName(trimToNull(req.getBusinessName()));
        }
        if (req.getIndustry() != null) {
            user.setIndustry(trimToNull(req.getIndustry()));
        }
        if (req.getDistrict() != null) {
            user.setDistrict(trimToNull(req.getDistrict()));
        }
        if (req.getAiNotes() != null) {
            user.setAiNotes(trimToNull(req.getAiNotes()));
        }
        user = userRepository.save(user);
        return toDto(user);
    }

    public static UserProfileDto toDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setPhone(user.getPhone());
        dto.setBusinessName(user.getBusinessName());
        dto.setIndustry(user.getIndustry());
        dto.setDistrict(user.getDistrict());
        dto.setAiNotes(user.getAiNotes());
        return dto;
    }

    public static String buildUserContextForAi(User user) {
        if (user == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(user.getFullName()).append('\n');
        sb.append("Email: ").append(user.getEmail()).append('\n');
        appendLine(sb, "Phone", user.getPhone());
        appendLine(sb, "Business", user.getBusinessName());
        appendLine(sb, "Industry focus", user.getIndustry());
        appendLine(sb, "District", user.getDistrict());
        if (user.getAiNotes() != null && !user.getAiNotes().isBlank()) {
            sb.append("Owner notes / goals:\n").append(user.getAiNotes().trim()).append('\n');
        }
        return sb.toString().trim();
    }

    private static void appendLine(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(label).append(": ").append(value.trim()).append('\n');
        }
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
