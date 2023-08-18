package com.techelevator.tenmo.exception;

public class InvalidAccountException extends RuntimeException{
    public InvalidAccountException() {
        super();
    }
    public InvalidAccountException(String message) {
        super(message);
    }
    public InvalidAccountException(String message, Exception cause) {
        super(message, cause);
    }
}
