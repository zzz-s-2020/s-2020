package ru.zzz.demo.sber.shs.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceRegistrationRequestDto {
    private final String addr;

    @JsonCreator
    public DeviceRegistrationRequestDto(@JsonProperty("address") String addr) {
        this.addr = addr;
    }

    public String getAddress() {
        return addr;
    }
}
