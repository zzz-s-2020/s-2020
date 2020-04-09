package ru.zzz.demo.sber.shs.service.api;

import ru.zzz.demo.sber.shs.model.device.DeviceAddress;

/**
 * Indicates that device cannot be found.
 */
public class UnknownDeviceException extends DeviceManagementException {
    public UnknownDeviceException(DeviceAddress address) {
        super("Unknown device "+address);
    }
}
