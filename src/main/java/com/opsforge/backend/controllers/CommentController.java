package com.opsforge.backend.controllers;

import com.opsforge.backend.models.Comment;
import com.opsforge.backend.models.User;
import com.opsforge.backend.services.CloudinaryService;
import com.opsforge.backend.services.CommentService;
import com.opsforge.backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*") // in production allow fronted url
@Tag(name = "Comments", description = "Endpoints for ticket discussions")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    public CommentController(CommentService commentService, UserService userService, CloudinaryService cloudinaryService) {
        this.commentService = commentService;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    //POST http://localhost:8080/api/comments
    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Add comment", description = "Adds a new comment with an optional attachment")
    public ResponseEntity<Comment> addComment(
            @RequestParam("ticketId") Long ticketId,
            @RequestParam("message") String message,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Principal principal) {
        
        // Get the authenticated user's ID
        User user = userService.getUserByUsername(principal.getName());
        Long authorId = user.getId();

        String attachmentUrl = null;
        if (file != null && !file.isEmpty()) {
            attachmentUrl = cloudinaryService.uploadFile(file);
        }

        // The Chef orchestrates the save
        Comment newComment = commentService.addComment(ticketId, authorId, message, attachmentUrl);
        
        return ResponseEntity.ok(newComment);
    }

    //GET http://localhost:8080/api/comments/ticket/5
    @GetMapping("/ticket/{ticketId}")
    public List<Comment> getCommentsForTicket(@PathVariable Long ticketId) {
        return commentService.getCommentsForTicket(ticketId);
    }
}