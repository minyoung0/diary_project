package com.example.coupleDiary.repository;

import com.example.coupleDiary.domain.MemberEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity,Long> {

    Optional<MemberEntity> findByUserId(String userId);

    boolean existsByUserId(String userId);

    @Modifying //변경쿼리(Insert,Update,Delete)임을 알려줌
    @Query("UPDATE MemberEntity m SET m.coupleId = :coupleId WHERE m.userId = :userId")
    void updateCoupleId(@Param("userId") String userId, @Param("coupleId") Integer coupleId);
}

