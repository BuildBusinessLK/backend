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

@Service
public class AuthUserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthUserService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
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
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullName(request.getFullName().trim());
        userProfileRepository.save(profile);

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
        User user = userRepository.findById(userId).orElseThrow();
        return toDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, ProfileUpdateRequest req) {
        User user = userRepository.findById(userId).orElseThrow();
        UserProfile profile = userProfileRepository.findByUser_Id(userId).orElseGet(() -> {
            UserProfile p = new UserProfile();
            p.setUser(user);
            p.setFullName("");
            return p;
        });
        if (req.getFullName() != null) {
            String n = req.getFullName().trim();
            if (!n.isEmpty()) {
                profile.setFullName(n);
            }
        }
        if (req.getPhone() != null) {
            profile.setPhone(trimToNull(req.getPhone()));
        }
        if (req.getExperienceLevel() != null) {
            profile.setExperienceLevel(trimToNull(req.getExperienceLevel()));
        }
        if (req.getPreferredLanguage() != null) {
            profile.setPreferredLanguage(trimToNull(req.getPreferredLanguage()));
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
        if (profile != null) {
            dto.setFullName(profile.getFullName());
            dto.setPhone(profile.getPhone());
            dto.setDistrict(profile.getDistrict());
            dto.setExperienceLevel(profile.getExperienceLevel());
            dto.setPreferredLanguage(profile.getPreferredLanguage());
        }
        return dto;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
