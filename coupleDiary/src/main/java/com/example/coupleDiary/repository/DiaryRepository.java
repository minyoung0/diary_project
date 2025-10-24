package com.example.coupleDiary.repository;

import com.example.coupleDiary.domain.Diary;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DiaryRepository extends JpaRepository<Diary,Integer> {
    Diary findByDiaryId(int diaryId);

    List<Diary> findByUserIdIn(List<String> userIds);

    @Query(value = """
        SELECT d.diary_id,
               d.content,
               d.created_at,
               d.updated_at,
               d.is_deleted,
               d.mood,
               d.weather,
               d.temperature,
               d.user_id,
               u.nickname
        FROM tb_diary d
        JOIN tb_user u ON d.user_id = u.user_id
        WHERE d.user_id IN (:userIds) 
        AND d.is_deleted=0
    """, nativeQuery = true)
    List<Map<String, Object>> findDiariesWithNickname(@Param("userIds") List<String> userIds);

}
