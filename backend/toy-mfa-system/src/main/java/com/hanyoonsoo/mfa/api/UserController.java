package com.hanyoonsoo.mfa.api;

import com.hanyoonsoo.mfa.api.dto.request.CreateUserRequest;
import com.hanyoonsoo.mfa.api.dto.response.GetUserInfoResponse;
import com.hanyoonsoo.mfa.common.ApiResponse;
import com.hanyoonsoo.mfa.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("")
    public ApiResponse<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        userService.createUser(request);
        return ApiResponse.success(HttpStatus.OK);
    }

    @GetMapping("/me")
    public ApiResponse<GetUserInfoResponse> getUserInfo() {
        GetUserInfoResponse response = userService.getUserInfo();
        return ApiResponse.success(HttpStatus.OK, response);
    }
}
