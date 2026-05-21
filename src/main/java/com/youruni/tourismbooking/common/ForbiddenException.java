package com.youruni.tourismbooking.common;
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}