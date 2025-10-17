package com.example.coupleDiary.domain;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

public class Auth {

    @Data
    public static class SignIn {
        private String userId;
        private String password;


    }

    @Data
    public static class SignUp {
        private String userId;
        private String nickname;
        private String password;
        private String email;
        private MultipartFile profileImg;  // 프로필 이미지까지 넣고 싶으면 추가
        private String profileImgPath;
        public MemberEntity toEntity() {
            return MemberEntity.builder()
                    .userId(this.userId)
                    .nickname(this.nickname)
                    .password(this.password)
                    .email(this.email)
                    .profileImg(this.profileImgPath)
                    .build();
        }
    }
}
