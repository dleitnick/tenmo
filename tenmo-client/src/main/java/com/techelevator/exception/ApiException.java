package com.techelevator.exception;

public class ApiException extends RuntimeException {
    public ApiException() {
        super();
    }
    public ApiException(String message) {
        super(message);
    }
    public ApiException(String message, Exception cause) {
        super(message, cause);
    }
}
