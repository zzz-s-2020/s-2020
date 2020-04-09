package ru.zzz.demo.sber.shs.CircuitBreaker.device;

import org.springframework.lang.NonNull;
import ru.zzz.demo.sber.shs.device.DeviceManager;
import ru.zzz.demo.sber.shs.device.Reply;
import ru.zzz.demo.sber.shs.device.Request;

/**
 * This is a circuit breaker which does nothing but delegate calls to the {@link DeviceManager}.
 * Writing proper circuit breaker or integrate something like Hystrix is too much work for the small project.
 */
public interface DeviceCircuitBreaker {
    /**
     * Registers a new Device stub if it didn't exist and issues a comment on it.
     *
     * @param msg a requested commend.
     * @return reply. Errors are indicated as a {@link Reply.Error}.
     *
     * @throws IllegalArgumentException is an msg is null.
     */
    @NonNull
    Reply accept(Request msg);
}
