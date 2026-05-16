package com.backend.security;

import com.backend.entity.User;
import com.backend.entity.UserProfile;
import com.backend.repository.UserRepository;
import com.backend.repository.UserProfileRepository;
import com.backend.user.UserStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public CustomUserDetailsService(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String fullName = userProfileRepository.findByUser_Id(user.getId())
                .map(UserProfile::getFullName)
                .orElse("");
        boolean enabled = user.getStatus() == UserStatus.ACTIVE;
        return new CustomUserDetails(user, fullName, enabled);
    }
}
