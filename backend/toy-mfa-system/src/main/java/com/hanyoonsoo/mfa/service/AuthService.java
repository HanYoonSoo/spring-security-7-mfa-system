package com.hanyoonsoo.mfa.service;

import com.hanyoonsoo.mfa.api.dto.request.SignInRequest;
import com.hanyoonsoo.mfa.api.dto.response.TokenResponse;
import com.hanyoonsoo.mfa.common.Pair;
import com.hanyoonsoo.mfa.entity.User;
import com.hanyoonsoo.mfa.infra.redis.service.AuthRedisService;
import com.hanyoonsoo.mfa.repository.UserRepository;
import com.hanyoonsoo.mfa.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthRedisService authRedisService;
    private final JwtProvider jwtProvider;

    public TokenResponse signIn(SignInRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("해당 유저는 존재하지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        Pair<String, String> tokens = jwtProvider.createTokens(
                user.getId().toString(),
                List.of(user.getRole())
        );

        String accessToken = tokens.getLeft();
        String refreshToken = tokens.getRight();

        authRedisService.saveRefreshToken(
                user.getId().toString(),
                refreshToken,
                jwtProvider.getRefreshTokenExpirationMillis()
        );

        ResponseCookie refreshCookie = jwtProvider.generateRefreshCookie(refreshToken);

        return new TokenResponse(accessToken, refreshCookie);
    }
}
