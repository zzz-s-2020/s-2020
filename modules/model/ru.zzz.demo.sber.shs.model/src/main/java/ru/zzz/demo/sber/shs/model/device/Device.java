package ru.zzz.demo.sber.shs.model.device;

import org.springframework.lang.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a Device and its parameters in memory. Keeps history to revert value changes.
 * Invariants:
 * <pre>
 * /\ defaultValue >= 0
 * /\ value >= 0
 * /\ address is not empty
 * </pre>
 *
 * <p>Instances of this class are thread safe.
 */
public class Device {
    private final int defaultValue;
    private final DeviceAddress addr;
    private final Deque<Integer> history = new ArrayDeque<>();
    private final Object lock = new Object();
    private int value;
    private boolean isOn;

    private Device(int defaultValue, DeviceAddress address) {
        this.defaultValue = defaultValue;
        this.addr = address;
        value = defaultValue;
    }

    private Device(DeviceAddress address, int value, boolean isOn) {
        this.addr = address;
        this.value = value;
        this.isOn = isOn;
        this.defaultValue = 0;
    }

    /**
     * Factory
     *
     * @param address       nonempty device address
     * @param defaultValue  value
     * @return new Device
     * @throws IllegalArgumentException on empty address or negative defaultVolume
     */
    @NonNull
    public static Device of(String address, int defaultValue) {
        if (address == null || address.isEmpty()) throw new IllegalArgumentException("Empty address");
        if (defaultValue < 0) throw new IllegalArgumentException("Negative defaultValue");
        return new Device(defaultValue, DeviceAddress.of(address));
    }

    /**
     * Factory
     *
     * @param address nonempty device address
     * @return new Device
     * @throws IllegalArgumentException on empty address or negative defaultVolume
     */
    @NonNull
    public static Device of(String address) {
        if (address == null || address.isEmpty()) throw new IllegalArgumentException("Empty address");
        return new Device(0, DeviceAddress.of(address));
    }

    /**
     * Factory
     *
     * @param address nonempty device address
     * @return new Device
     * @throws IllegalArgumentException on empty address or negative defaultVolume
     */
    @NonNull
    public static Device of(DeviceAddress address) {
        if (address == null) throw new IllegalArgumentException("Empty address");
        return new Device(0, address);
    }

    /**
     * Factory
     *
     * @param address nonempty device address
     * @param value current value
     * @param isOn is on
     * @return new Device
     * @throws IllegalArgumentException on empty address or negative defaultVolume
     */
    @NonNull
    public static Device of(DeviceAddress address, int value, boolean isOn) {
        if (address == null) throw new IllegalArgumentException("Empty address");
        if (value < 0) throw new IllegalArgumentException("Negative value");
        return new Device(address, value, isOn);
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    @NonNull
    public DeviceAddress getAddress() {
        return addr;
    }

    public void on() {
        synchronized (lock) {
            isOn = true;
        }
    }

    public void off() {
        synchronized (lock) {
            isOn = false;
        }
    }

    public boolean isOn() {
        synchronized (lock) {
            return isOn;
        }
    }

    public Optional<Integer> getValue() {
        synchronized (lock) {
            return isOn ? Optional.of(value) : Optional.empty();
        }
    }

    public int incrementValue() {
        synchronized (lock) {
            checkIsOn();
            history.push(value);
            value++;
            return value;
        }
    }

    public int decrementValue() {
        synchronized (lock) {
            checkIsOn();
            if (value == 0)
                return value;
            history.push(value);
            value--;
            return value;
        }
    }

    public int undoValueChange() {
        synchronized (lock) {
            checkIsOn();
            if (history.isEmpty())
                return value;
            value = history.pop();
            return value;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(addr, device.addr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addr);
    }

    @Override
    public String toString() {
        boolean localIsOn;
        int localVolume;
        synchronized (lock) {
            localIsOn = isOn;
            localVolume = value;
        }
        return "Device{" +
                "defaultVolume=" + defaultValue +
                ", addr=" + addr +
                ", state=" + (localIsOn ? "ON" : "OFF") +
                ", volume=" + localVolume +
                '}';
    }

    private void checkIsOn() {
        if (!isOn) throw new DeviceIsOffException(addr);
    }
}
