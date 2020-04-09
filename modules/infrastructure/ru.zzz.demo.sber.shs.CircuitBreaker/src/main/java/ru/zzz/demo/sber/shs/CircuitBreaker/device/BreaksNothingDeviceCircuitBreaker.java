package ru.zzz.demo.sber.shs.CircuitBreaker.device;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.zzz.demo.sber.shs.device.DeviceManager;
import ru.zzz.demo.sber.shs.device.Reply;
import ru.zzz.demo.sber.shs.device.Request;

@Component("SHS.Infrastructure.DeviceCircuitBreaker")
class BreaksNothingDeviceCircuitBreaker implements DeviceManager, DeviceCircuitBreaker {
    private final DeviceManager dm;

    @Autowired
    public BreaksNothingDeviceCircuitBreaker(DeviceManager dm) {
        this.dm = dm;
    }

    @Override
    @NonNull
    public Reply accept(Request msg) {
        return dm.accept(msg);
    }
}
