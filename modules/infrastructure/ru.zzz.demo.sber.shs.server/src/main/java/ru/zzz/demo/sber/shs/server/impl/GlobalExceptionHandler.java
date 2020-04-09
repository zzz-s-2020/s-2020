/*
 * Copyright (C) 2019, 1C
 */

package ru.zzz.demo.sber.shs.server.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Catches exceptions from controllers.
 * Add {@link org.springframework.web.bind.annotation.ControllerAdvice} to use.
 */
@Component("SHS.Infrastructure.HttpServer.GlobalExceptionHandler")
@RestControllerAdvice
class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<String>> onCategorizedError(ServerWebExchange exchange,
            ResponseStatusException e) {
        return Mono.fromCallable(() -> new ResponseEntity<>(e.getMessage(), e.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> onUnknownError(ServerWebExchange exchange, Exception e) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("{}Request processing failed: HTTP {}", exchange.getLogPrefix(),
                    formatRequest(exchange.getRequest()), e);
        }
        return Mono.fromCallable(
                () -> new ResponseEntity<>("Error: " + e.getMessage(), INTERNAL_SERVER_ERROR));
    }

    private static String formatRequest(ServerHttpRequest request) {
        String rawQuery = request.getURI().getRawQuery();
        String query = StringUtils.hasText(rawQuery) ? "?" + rawQuery : "";
        return request.getMethod() + " \"" + request.getPath() + query + "\"";
    }
}
