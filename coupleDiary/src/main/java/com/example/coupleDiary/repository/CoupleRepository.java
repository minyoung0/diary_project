package com.example.coupleDiary.repository;

import com.example.coupleDiary.domain.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoupleRepository extends JpaRepository<Couple,Integer> {


}
