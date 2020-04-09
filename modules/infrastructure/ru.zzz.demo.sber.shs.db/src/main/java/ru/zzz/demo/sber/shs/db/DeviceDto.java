package ru.zzz.demo.sber.shs.db;

import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

/**
 * DTO to transfer device state records from {@link DeviceStorageRepository} to the outer world.
 */
public class DeviceDto {
    private final String addr;
    private final boolean isOn;
    private final int value;
    private final LocalDateTime lastSaveDateUtc;

    private DeviceDto(String addr, boolean isOn, int value, LocalDateTime lastSaveDateUtc) {
        this.addr = addr;
        this.isOn = isOn;
        this.value = value;
        this.lastSaveDateUtc = lastSaveDateUtc;
    }

    public static DeviceDto of(String addr, boolean isOn, int value, LocalDateTime lastSaveDateUtc) {
        return new DeviceDto(addr, isOn, value, lastSaveDateUtc);
    }

    /**
     * @return an address as it came from an underlying storage. This DTO does not check it is not null.
     */
    @Nullable
    public String getAddr() {
        return addr;
    }

    public boolean isOn() {
        return isOn;
    }

    public int getValue() {
        return value;
    }

    @Nullable
    public LocalDateTime getLastSaveDateUtc() {
        return lastSaveDateUtc;
    }
}
