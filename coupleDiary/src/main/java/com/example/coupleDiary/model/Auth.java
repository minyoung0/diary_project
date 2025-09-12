package com.example.coupleDiary.model;

import lombok.Data;

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
        private String profileImg;  // 프로필 이미지까지 넣고 싶으면 추가

        public MemberEntity toEntity() {
            return MemberEntity.builder()
                    .userId(this.userId)
                    .nickname(this.nickname)
                    .password(this.password)
                    .email(this.email)
                    .profileImg(this.profileImg)
                    .build();
        }
    }
}
