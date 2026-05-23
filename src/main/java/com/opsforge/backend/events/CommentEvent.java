package com.opsforge.backend.events;

import com.opsforge.backend.models.Comment;

public class CommentEvent {
    private final Comment comment;

    public CommentEvent(Comment comment) {
        this.comment = comment;
    }

    public Comment getComment() { return comment; }
}
