package com.example.coupleDiary.persist;

import com.example.coupleDiary.model.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity,Long> {

    Optional<MemberEntity> findByUsername(String username);

    boolean existsbyUsername(String username);
}
