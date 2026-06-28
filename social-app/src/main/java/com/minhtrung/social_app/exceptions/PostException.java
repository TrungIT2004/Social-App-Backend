package com.minhtrung.social_app.exceptions;

import com.minhtrung.social_app.enums.PostErrorCode;

public class PostException extends RuntimeException {
    private PostErrorCode errorCode;

    public PostException(PostErrorCode errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public PostErrorCode getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(PostErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
