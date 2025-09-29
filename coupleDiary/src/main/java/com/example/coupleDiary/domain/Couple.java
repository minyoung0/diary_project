package com.example.coupleDiary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private int coupleId;

    @Column(name="user1_id")
    private String user1Id;
    @Column(name="user2_id")
    private String user2Id;
    @Column(name="created_at")
    private LocalDateTime createdAt;
}
