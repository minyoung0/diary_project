package com.example.coupleDiary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Diary {

    @Id
    @Column(name="diary_id")
    private int diaryId;
    private String content;

    @CreationTimestamp
    @Column(name="created_at")
    private LocalDate createdAt;

    @UpdateTimestamp
    @Column(name="updated_at")
    private LocalDate updatedAt;

    @Column(name="is_deleted")
    private int isDeleted;
    private String mood;
    private String weather;
    private int temperature;

    @Column(name="user_id")
    private String userId;

}
