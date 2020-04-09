package ru.zzz.demo.sber.shs.service.api;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.zzz.demo.sber.shs.model.device.Device;

/**
 * DTO to transfer Device parameters to clients in read only manner.
 *
 * <p>Instances of this class are thread safe.
 */
public class DeviceDescriptor {
    private final String address;
    private final boolean isOn;
    private final Integer value;

    public DeviceDescriptor(Device device) {
        this.address = device.getAddress().getRawAddress();
        this.isOn = device.isOn();
        this.value = device.getValue().orElse(null);
    }

    @NonNull
    public String getAddress() {
        return address;
    }

    public boolean isOn() {
        return isOn;
    }

    @Nullable
    public Integer getValue() {
        return value;
    }
}
