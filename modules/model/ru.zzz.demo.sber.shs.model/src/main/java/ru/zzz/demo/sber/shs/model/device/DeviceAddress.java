package ru.zzz.demo.sber.shs.model.device;

import org.springframework.lang.NonNull;

import java.util.Objects;

/**
 * An immutable device address.
 * Invariant for all commands:
 * /\ rawAddress!=null
 * /\ rawAddress is not empty
 */
public final class DeviceAddress {
    private final String rawAddress;

    private DeviceAddress(String rawAddress) {
        this.rawAddress = rawAddress;
    }

    @NonNull
    public static DeviceAddress of(String address) throws InvalidAddressException {
        if (address == null || address.isEmpty()) throw new InvalidAddressException(address);
        return new DeviceAddress(address);
    }

    @NonNull
    public String getRawAddress() {
        return rawAddress;
    }

    @NonNull
    @Override
    public String toString() {
        return rawAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceAddress that = (DeviceAddress) o;
        return rawAddress.equals(that.rawAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawAddress);
    }
}
