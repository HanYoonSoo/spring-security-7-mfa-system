package com.hanyoonsoo.mfa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected Post(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
    }

    public static Post of(
            String title,
            String content,
            User user
    ) {
        return new Post(title, content, user);
    }
}
