package com.hanyoonsoo.mfa.api;

import com.hanyoonsoo.mfa.api.dto.request.CreatePostRequest;
import com.hanyoonsoo.mfa.api.dto.response.PostResponse;
import com.hanyoonsoo.mfa.common.ApiResponse;
import com.hanyoonsoo.mfa.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("")
    public ApiResponse<Void> createPost(@Valid @RequestBody CreatePostRequest request) {
        postService.createPost(request);

        return ApiResponse.success(HttpStatus.OK);
    }

    @GetMapping("")
    public ApiResponse<List<PostResponse>> getPosts() {
        List<PostResponse> response = postService.getPosts();
        return ApiResponse.success(HttpStatus.OK, response);
    }
}
