package com.techelevator.tenmo.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super();
    }
    public InsufficientFundsException(String message) {
        super(message);
    }
    public InsufficientFundsException(String message, Exception cause) {
        super(message, cause);
    }
}