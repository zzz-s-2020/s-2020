package ru.zzz.demo.sber.shs.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.zzz.demo.sber.shs.service.api.DeviceDescriptor;

public class DeviceDescriptorDto {
    private final String address;
    private final boolean isOn;
    private final Integer value;

    private DeviceDescriptorDto(String address, boolean isOn, Integer value) {
        this.address = address;
        this.isOn = isOn;
        this.value = value;
    }

    @NonNull
    public static DeviceDescriptorDto of(DeviceDescriptor d) {
        return new DeviceDescriptorDto(d.getAddress(), d.isOn(), d.getValue());
    }

    @JsonProperty("address")
    @NonNull
    public String getAddress() {
        return address;
    }

    @JsonProperty("is-on")
    public boolean isOn() {
        return isOn;
    }

    @JsonProperty("value")
    @JsonInclude(content = JsonInclude.Include.NON_NULL)
    @Nullable
    public Integer getValue() {
        return value;
    }
}
