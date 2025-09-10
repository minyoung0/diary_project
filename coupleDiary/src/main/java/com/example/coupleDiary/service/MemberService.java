package com.example.coupleDiary.service;

import com.example.coupleDiary.model.Auth;
import com.example.coupleDiary.model.MemberEntity;
import com.example.coupleDiary.persist.MemberRepository;
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
        this.memberRepository.findByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("couldn't find user ->"+username));
        return null;
    }

    public MemberEntity register(Auth.SignUp member){
        boolean exist = this.memberRepository.existsbyUsername(member.getUsername());
        if(exist) {
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        var resilt= this.memberRepository.save(member.toEntity());
        return resilt;
    }

    public MemberEntity authenticate(Auth.SignIn member){
        return null;
    }
}
