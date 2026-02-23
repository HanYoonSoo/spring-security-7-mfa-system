package com.hanyoonsoo.mfa.service;

import com.hanyoonsoo.mfa.api.dto.request.CreateUserRequest;
import com.hanyoonsoo.mfa.api.dto.response.GetUserInfoResponse;
import com.hanyoonsoo.mfa.entity.User;
import com.hanyoonsoo.mfa.repository.UserRepository;
import com.hanyoonsoo.mfa.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createUser(CreateUserRequest request) {
        User user = User.of(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.email()
        );

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public GetUserInfoResponse getUserInfo() {
        UUID userId = SecurityUtils.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저는 존재하지 않습니다."));

        return new GetUserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
