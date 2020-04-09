package ru.zzz.demo.sber.shs.model;

import ru.zzz.demo.sber.shs.model.device.Device;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Indicators connects Devices with buttons.
 *
 * TODO Under construction!
 */
public class Indicator {
    private AtomicReference<Device> device;

    public Optional<Device> getDevice() {
        return Optional.ofNullable(device.get());
    }

    /**
     * IF Indicator is not connected it connects to a target Device.
     * @param target some device
     * @throws IllegalArgumentException on null
     * @throws IllegalStateException on already connected
     */
    public void connect(Device target) {
        if (target == null) throw new IllegalArgumentException("Cannot connect to null");
        if (!device.compareAndSet(null, target))
            throw new IllegalStateException("Already connected");
    }
}
