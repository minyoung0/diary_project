package com.example.coupleDiary.service;

import com.example.coupleDiary.domain.Comment;
import com.example.coupleDiary.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public void saveComment(Comment comment){
        commentRepository.save(comment);
    }
}
