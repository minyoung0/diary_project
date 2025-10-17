package com.example.coupleDiary.repository;

import com.example.coupleDiary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary,Integer> {
    Diary findByDiaryId(int diaryId);
    List<Diary> findByUserIdIn(List<String> userIds);
}
