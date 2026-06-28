package com.minhtrung.social_app.exceptions;

import com.minhtrung.social_app.enums.PostReactionErrorCode;

public class PostReactionException extends RuntimeException {
    private PostReactionErrorCode errorCode;

    public PostReactionException(PostReactionErrorCode errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public PostReactionErrorCode getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(PostReactionErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
