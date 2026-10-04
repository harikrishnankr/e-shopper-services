package com.eshopper.catalog.shared.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}