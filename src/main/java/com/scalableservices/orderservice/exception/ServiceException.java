package com.scalableservices.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ServiceException extends ResponseStatusException {
    public ServiceException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
