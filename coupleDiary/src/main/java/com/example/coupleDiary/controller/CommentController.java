package com.example.coupleDiary.controller;

import com.example.coupleDiary.domain.Comment;
import com.example.coupleDiary.repository.CommentRepository;
import com.example.coupleDiary.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {


    private final CommentService commentService;
    private final CommentRepository commentRepository;

    public CommentController(CommentService commentService, CommentRepository commentRepository) {
        this.commentService = commentService;
        this.commentRepository = commentRepository;
    }

    //댓글 작성
    @PostMapping("/add")
    public ResponseEntity<?> addComment(@RequestBody Comment comment, Principal principal){
        try {
            String userId = principal.getName();
            comment.setUserId(userId);
            comment.setCreatedAt(LocalDateTime.now());
            comment.setIsDeleted(0);
            if (comment.getParentId() == null) {
                comment.setParentId(0);
            }

            commentService.saveComment(comment);
            return ResponseEntity.ok("댓글 작성 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("댓글 작성 실패");
        }
    }

    @GetMapping("/list/{diaryId}")
    public ResponseEntity<?> list(@PathVariable int diaryId) {
        return ResponseEntity.ok(commentRepository.getCommentsByDiaryId(diaryId));
    }

    @PostMapping("/delete/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable int commentId, Principal principal) {
        try {
            String userId = principal.getName();
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

            if (!comment.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body("본인 댓글만 삭제할 수 있습니다.");
            }

            comment.setIsDeleted(1);
            commentRepository.save(comment);
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("댓글 삭제 실패");
        }
    }

    // 댓글 수정
    @PostMapping("/update/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable int commentId,
                                           @RequestBody Comment req,
                                           Principal principal) {
        try {
            String userId = principal.getName();
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

            if (!comment.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body("본인 댓글만 수정할 수 있습니다.");
            }

            comment.setComment(req.getComment());
            comment.setUpdatedAt(LocalDateTime.now());
            commentRepository.save(comment);
            return ResponseEntity.ok("수정 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("댓글 수정 실패");
        }
    }
}
