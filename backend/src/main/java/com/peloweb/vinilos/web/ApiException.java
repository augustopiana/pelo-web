package com.peloweb.vinilos.web;

import org.springframework.http.HttpStatus;

/** Excepcion de negocio con un status HTTP asociado; la traduce GlobalExceptionHandler. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
