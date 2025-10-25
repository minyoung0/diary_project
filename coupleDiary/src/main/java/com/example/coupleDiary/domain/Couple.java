package com.example.coupleDiary.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name="tb_couple")
public class Couple {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "couple_id")
    private int coupleId;

    @Column(name="user1_id")
    private String user1Id;
    @Column(name="user2_id")
    private String user2Id;
    @Column(name="created_at")
    private LocalDateTime createdAt;
    @Column(name="anniversary")
    private LocalDateTime anniversary;
}
