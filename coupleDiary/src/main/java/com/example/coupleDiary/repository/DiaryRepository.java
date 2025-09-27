package com.example.coupleDiary.repository;

import com.example.coupleDiary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary,Integer> {
}
