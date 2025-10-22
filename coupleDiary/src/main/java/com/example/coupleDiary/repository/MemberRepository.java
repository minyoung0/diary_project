package com.example.coupleDiary.repository;

import com.example.coupleDiary.domain.MemberEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity,Long> {

    Optional<MemberEntity> findByUserId(String userId);

    boolean existsByUserId(String userId);

    @Modifying //변경쿼리(Insert,Update,Delete)임을 알려줌
    @Query("UPDATE MemberEntity m SET m.coupleId = :coupleId WHERE m.userId = :userId")
    void updateCoupleId(@Param("userId") String userId, @Param("coupleId") Integer coupleId);

    @Query("Select m.userId from MemberEntity m where m.coupleId = :coupleId")
    List<String> findUserIdsByCoupleId(@Param("coupleId")int coupleId);

    @Query("SELECT m FROM MemberEntity m WHERE m.coupleId = :coupleId AND m.userId <> :userId")
    MemberEntity findPartnerByCoupleId(@Param("coupleId") int coupleId, @Param("userId") String userId);

    @Query("Select m From MemberEntity  m WHERE m.coupleId = :coupleId")
    MemberEntity findById(@Param("coupleId")int coupleId);
}

