/*
 * Copyright (C) 2019, 1C
 */

package ru.zzz.demo.sber.shs.server.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * This class handles exceptions that were not thrown from controllers but from Spring code, e.g. when
 * it cannot find a suitable controller.
 */
@Component("SHS.Infrastructure.HttpServer.ErrorWebExceptionHandler")
@Order(HIGHEST_PRECEDENCE)
class ErrorWebExceptionHandler implements WebExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorWebExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable e) {
        LOGGER.error("Unhandled request error", e);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        HttpHeaders responseHeaders = response.getHeaders();
        responseHeaders.setContentType(MediaType.TEXT_PLAIN);
        Mono<DataBuffer> buf = Mono.fromSupplier(e::getMessage)
                .map(bodyStr -> bodyStr.getBytes(UTF_8))
                .map(bodyBytes -> response.bufferFactory().wrap(bodyBytes));
        return response.writeAndFlushWith(Flux.just(buf));
    }
}
