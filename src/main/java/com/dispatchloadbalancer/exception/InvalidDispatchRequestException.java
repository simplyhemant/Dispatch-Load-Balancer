package com.dispatchloadbalancer.exception;

public class InvalidDispatchRequestException extends RuntimeException {

    public InvalidDispatchRequestException(String message) {
        super(message);
    }
}
