package ru.zzz.demo.sber.shs.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;

public class ValueDto {
    private final int value;

    private ValueDto(int value) {
        this.value = value;
    }

    public static ValueDto of(Integer v) {
        if (v == null) throw new ResponseStatusException(CONFLICT, "Cannot get value");
        return new ValueDto(v);
    }

    @JsonProperty("value")
    public int getValue() {
        return value;
    }
}
