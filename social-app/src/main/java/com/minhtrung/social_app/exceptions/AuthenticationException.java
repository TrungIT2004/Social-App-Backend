package com.minhtrung.social_app.exceptions;

import com.minhtrung.social_app.enums.AuthErrorCode;

public class AuthenticationException extends RuntimeException {
    private final AuthErrorCode code;
    
    public AuthenticationException(AuthErrorCode code, String message) {
        super(message);
        this.code = code;
    }
    
    public AuthErrorCode getCode() { return code; }
}
