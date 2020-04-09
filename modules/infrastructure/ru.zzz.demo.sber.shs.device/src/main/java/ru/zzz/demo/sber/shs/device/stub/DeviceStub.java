package ru.zzz.demo.sber.shs.device.stub;

import ru.zzz.demo.sber.shs.device.DeviceException;

/**
 * A stub supporting On/off flag and raw int value.
 * Invariants:
 * /\ val >= 0
 */
class DeviceStub {
    private final Object lock = new Object();
    private boolean isOn = false;
    private int val;

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

    /**
     * Sets a value.
     *
     * @param val value, must be >= 0.
     * @throws DeviceException if val < 0 or the device is off.
     */
    public void setVal(int val) {
        if (val < 0) throw new DeviceException("Cannot set");
        synchronized (lock) {
            if (!isOn) throw new DeviceException("Device is off");
            this.val = val;
        }
    }

    /**
     * Gets a value.
     *
     * @throws DeviceException if the device is off.
     */
    public int getVal() {
        synchronized (lock) {
            if (!isOn) throw new DeviceException("Device is off");
            return val;
        }
    }
}
