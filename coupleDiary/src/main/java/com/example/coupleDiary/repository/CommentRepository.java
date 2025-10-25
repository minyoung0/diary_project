package com.example.coupleDiary.repository;

import com.example.coupleDiary.domain.Comment;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @Query(value = """
                SELECT 
                    c.comment_id AS commentId,
                    c.comment,
                    c.created_at AS createdAt,
                    c.updated_at AS updatedAt,
                    c.is_deleted AS isDeleted,
                    c.parent_id AS parentId,
                    c.user_id AS userId,
                    c.diary_id AS diaryId,
                    u.nickname AS nickname
                FROM tb_comment c
                JOIN tb_user u ON u.user_id = c.user_id
                WHERE c.diary_id = :diaryId
                  AND c.is_deleted = 0
                ORDER BY c.created_at ASC
            """, nativeQuery = true)
    List<Map<String, Object>> getCommentsByDiaryId(@Param("diaryId") int diaryId);
}
