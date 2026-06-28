package com.minhtrung.social_app.exceptions;

import com.minhtrung.social_app.enums.CommentErrorCode;

public class CommentException extends RuntimeException {
    private CommentErrorCode errorCode;

    public CommentException(CommentErrorCode errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public CommentErrorCode getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(CommentErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
