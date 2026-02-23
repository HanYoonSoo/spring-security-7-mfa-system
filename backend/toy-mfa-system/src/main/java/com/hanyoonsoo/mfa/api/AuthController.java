package com.hanyoonsoo.mfa.api;

import com.hanyoonsoo.mfa.api.dto.request.SignInRequest;
import com.hanyoonsoo.mfa.api.dto.response.TokenResponse;
import com.hanyoonsoo.mfa.common.ApiResponse;
import com.hanyoonsoo.mfa.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("sign-in")
    public ApiResponse<Void> signIn(
            @Valid @RequestBody SignInRequest request,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = authService.signIn(request);
        setTokenToResponse(response, tokenResponse);
        return ApiResponse.success(HttpStatus.OK);
    }

    private void setTokenToResponse(HttpServletResponse response, TokenResponse tokenResponse) {
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponse.accessToken());
        response.addHeader(HttpHeaders.SET_COOKIE, tokenResponse.refreshCookie().toString());
    }
}
