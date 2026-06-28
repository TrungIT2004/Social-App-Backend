package com.minhtrung.social_app.exceptions;

import com.minhtrung.social_app.enums.CommentReactionErrorCode;

public class CommentReactionException extends RuntimeException {
    private CommentReactionErrorCode errorCode;

    public CommentReactionException(CommentReactionErrorCode errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public CommentReactionErrorCode getErrorCode() {
        return this.errorCode;
    }
}
