package com.example.coupleDiary.service;

import com.example.coupleDiary.domain.Auth;
import com.example.coupleDiary.domain.MemberEntity;
import com.example.coupleDiary.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음: " + username));
    }

    public MemberEntity register(Auth.SignUp member){
        boolean exist = this.memberRepository.existsByUserId(member.getUserId());
        if(exist) {
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        var resilt= this.memberRepository.save(member.toEntity());
        return resilt;
    }

    public MemberEntity authenticate(Auth.SignIn member){
        var user= this.memberRepository.findByUserId(member.getUserId())
                .orElseThrow(()->new RuntimeException("존재하지 않는 ID 입니다"));
        if(!this.passwordEncoder.matches(member.getPassword(),user.getPassword())){
            new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        return user;
    }
}
