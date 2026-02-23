package com.hanyoonsoo.mfa.service;

import com.hanyoonsoo.mfa.api.dto.request.CreatePostRequest;
import com.hanyoonsoo.mfa.api.dto.response.PostResponse;
import com.hanyoonsoo.mfa.entity.Post;
import com.hanyoonsoo.mfa.entity.User;
import com.hanyoonsoo.mfa.repository.PostRepository;
import com.hanyoonsoo.mfa.repository.UserRepository;
import com.hanyoonsoo.mfa.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createPost(CreatePostRequest request) {
        UUID userId = SecurityUtils.getUserId();

        if (userId != null) {
            User user = userRepository.getReferenceById(userId);

            Post post = Post.of(
                    request.title(),
                    request.content(),
                    user
            );

            postRepository.save(post);
        } else {
            throw new RuntimeException("로그인이 필요합니다.");
        }

    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPosts() {
        return postRepository.findAll().stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getUser().getId()
                ))
                .toList();
    }
}
