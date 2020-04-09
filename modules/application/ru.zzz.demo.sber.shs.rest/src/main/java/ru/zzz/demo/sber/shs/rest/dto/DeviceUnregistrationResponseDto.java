package ru.zzz.demo.sber.shs.rest.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.springframework.lang.NonNull;

public class DeviceUnregistrationResponseDto {
    private final boolean deviceExistedAndWasUnrigistered;

    @NonNull
    public static DeviceUnregistrationResponseDto ofReallyUnregistered(Boolean wasUnregistered) {
        if (wasUnregistered == null) throw new IllegalArgumentException();
        return new DeviceUnregistrationResponseDto(wasUnregistered);
    }

    private DeviceUnregistrationResponseDto(boolean deviceExistedAndWasUnrigistered) {
        this.deviceExistedAndWasUnrigistered = deviceExistedAndWasUnrigistered;
    }

    @JsonGetter("device-existed-and-was-unregistered")
    public String isDeviceExistedAndWasUnrigistered() {
        return deviceExistedAndWasUnrigistered ? "true" : "false";
    }
}
