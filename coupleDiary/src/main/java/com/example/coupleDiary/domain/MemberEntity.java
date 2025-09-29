package com.example.coupleDiary.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name="tb_user")
public class MemberEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // DB PK
    private Long id;   // 내부 식별자 (AUTO_INCREMENT)

    @Column(name="user_id",nullable = false)
    private String userId;
    private String nickname;
    private String email;
    private String password;

    @CreationTimestamp
    @Column(name="created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @Column(name="profile_img")
    private String profileImg;

    @Column(name="couple_id")
    private String coupleId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public String getUsername() {
        return this.userId;  // username 대신 userId 사용
    }
}
