package com.example.coupleDiary.repository;

import com.example.coupleDiary.model.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity,Long> {

    Optional<MemberEntity> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
