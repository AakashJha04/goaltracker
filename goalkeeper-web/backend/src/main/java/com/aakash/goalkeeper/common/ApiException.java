package com.aakash.goalkeeper.common;

import org.springframework.http.HttpStatus;

/** Thrown for expected business errors carrying a specific HTTP status. */
public class ApiException extends RuntimeException {
    public final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
