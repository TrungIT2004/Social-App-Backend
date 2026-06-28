package com.minhtrung.social_app.exceptions;

import com.minhtrung.social_app.enums.ShareErrorCode;

public class ShareException extends RuntimeException {
    ShareErrorCode errorCode;

    public ShareException(ShareErrorCode errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public ShareErrorCode getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(ShareErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
