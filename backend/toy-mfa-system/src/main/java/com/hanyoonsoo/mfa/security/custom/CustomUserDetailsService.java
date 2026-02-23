package com.hanyoonsoo.mfa.security.custom;

import com.hanyoonsoo.mfa.entity.User;
import com.hanyoonsoo.mfa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findUser(username);
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getId().toString())
                .password(user.getPassword())
                .authorities(user.getRole().toSpringRole())
                .build();
    }

    private User findUser(String username) {
        try {
            return userRepository.findById(UUID.fromString(username))
                    .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다."));
        } catch (IllegalArgumentException ignored) {
            throw new UsernameNotFoundException("해당 유저를 찾을 수 없습니다.");
        }
    }
}
