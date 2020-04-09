package ru.zzz.demo.sber.shs.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zzz.demo.sber.shs.CircuitBreaker.device.DeviceCircuitBreaker;
import ru.zzz.demo.sber.shs.CircuitBreaker.storage.DeviceStorageCircuitBreaker;
import ru.zzz.demo.sber.shs.device.Reply;
import ru.zzz.demo.sber.shs.device.Request;
import ru.zzz.demo.sber.shs.model.device.Device;
import ru.zzz.demo.sber.shs.model.device.DeviceAddress;
import ru.zzz.demo.sber.shs.model.device.DeviceIsOffException;
import ru.zzz.demo.sber.shs.service.api.DeviceDescriptor;
import ru.zzz.demo.sber.shs.service.api.DeviceManagementException;
import ru.zzz.demo.sber.shs.service.api.DeviceManager;
import ru.zzz.demo.sber.shs.service.api.UnknownDeviceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service("SHS.Application.DeviceManager")
class InMemoryDeviceManager implements DeviceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryDeviceManager.class);

    // devices list
    private final Map<DeviceAddress, DeviceAccessSerializer> devices = new ConcurrentHashMap<>();
    // An interface of a pseudo circuit breaker to the device
    private final DeviceCircuitBreaker deviceCircuitBreaker;
    // An interface of a pseudo circuit breaker to the devices state store
    private final DeviceStorageCircuitBreaker deviceStorageCircuitBreaker;
    // A monitor to guarantee devices list is loaded from a store once.
    private final Object devicesInitializationLock = new Object();
    // Flag indicating that initial state was read from a DB.
    private boolean devicesInitialized = false;

    @Autowired
    InMemoryDeviceManager(DeviceCircuitBreaker deviceCircuitBreaker,
            DeviceStorageCircuitBreaker deviceStorageCircuitBreaker) {
        this.deviceCircuitBreaker = deviceCircuitBreaker;
        this.deviceStorageCircuitBreaker = deviceStorageCircuitBreaker;
    }

    @Override
    @NonNull
    public List<DeviceAddress> list() {
        return new ArrayList<>(getDevices().keySet());
    }

    @Override
    @NonNull
    public List<DeviceDescriptor> listWithCurrentStatus() {
        return getDevices().values()
                .stream()
                .map(DeviceAccessSerializer::get)
                .map(DeviceDescriptor::new)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DeviceDescriptor> getDevice(DeviceAddress address) {
        DeviceAccessSerializer serializer = getDevices().get(address);
        return (serializer == null) ? Optional.empty() : Optional.of(new DeviceDescriptor(serializer.get()));
    }

    @Override
    public Mono<Void> register(DeviceAddress address) {
        if (address == null) throw new IllegalArgumentException("address is null");
        // Create a device in an off state
        return Mono.fromCallable(() -> getDevices().compute(address, (addr, existing) -> {
            if (existing == null) return new DeviceAccessSerializer(Device.of(addr));
            throw new DeviceManagementException("Already associated");
        })).flatMap(v -> {
            // Update devices store
            return deviceStorageCircuitBreaker.setDeviceIsOff(address.getRawAddress());
        }).then();
    }

    @Override
    public Mono<Boolean> unregister(DeviceAddress address) {
        if (address == null) throw new IllegalArgumentException("address is null");
        // Update model and then the devices store in case the device really existed
        return Mono.fromCallable(() -> getDevices().remove(address) != null).flatMap(removed -> {
            if (removed != null && removed) {
                return deviceStorageCircuitBreaker.removeDevice(address.getRawAddress())
                        .then(Mono.just(true));
            } else {
                return Mono.justOrEmpty(removed);
            }
        });
    }

    @Override
    @NonNull
    public Mono<Void> on(DeviceAddress address) {
        if (address == null) throw new IllegalArgumentException("address is null");
        return Mono.fromCallable(() -> {
            DeviceAccessSerializer serializer = getDevices().get(address);
            if (serializer == null) throw new UnknownDeviceException(address);
            return serializer.call(device -> {
                // Update a device stub
                Reply onReply = deviceCircuitBreaker.accept(Request.on(address.getRawAddress()));
                if (onReply instanceof Reply.Error) throw new DeviceManagementException(
                        "Cannot switch device " + address + " on: " + ((Reply.Error) onReply).getReason());
                // Update model
                device.on();
                // Set value to the device stub
                Integer v = device.getValue().orElseThrow(() -> new IllegalStateException("Device is off"));
                Reply setReply = deviceCircuitBreaker.accept(Request.set(address.getRawAddress(), v));
                if (setReply instanceof Reply.Error) throw new DeviceManagementException(
                        "Cannot set device " + address + " value: " + ((Reply.Error) setReply).getReason());
                return v;
            });
        }).flatMap(v -> {
            // Update the devices store (DB)
            return deviceStorageCircuitBreaker.setDeviceValue(address.getRawAddress(), v);
        }).then();
    }

    @Override
    @NonNull
    public Mono<Void> off(DeviceAddress address) {
        if (address == null) throw new IllegalArgumentException("address is null");
        return Mono.fromCallable(() -> {
            DeviceAccessSerializer serializer = getDevices().get(address);
            if (serializer == null) throw new UnknownDeviceException(address);
            return serializer.call(device -> {
                // Update a device stub
                Reply onReply = deviceCircuitBreaker.accept(Request.off(address.getRawAddress()));
                if (onReply instanceof Reply.Error) throw new DeviceManagementException(
                        "Cannot switch device " + address + " off: " + ((Reply.Error) onReply).getReason());
                // Update model
                device.off();
                return true;
            });
        }).flatMap(v -> {
            // Update the devices store (DB)
            return deviceStorageCircuitBreaker.setDeviceIsOff(address.getRawAddress());
        }).then();
    }

    @Override
    @NonNull
    public Mono<Integer> increment(DeviceAddress address) {
        if (address == null) throw new IllegalArgumentException("address is null");
        return Mono.fromCallable(() -> {
            DeviceAccessSerializer serializer = getDevices().get(address);
            if (serializer == null) throw new UnknownDeviceException(address);
            return serializer.call(device -> {
                // Update a device stub
                if(!device.isOn()) throw new DeviceIsOffException(address);
                int newValue = device.incrementValue();
                Reply onReply = deviceCircuitBreaker.accept(Request.set(address.getRawAddress(), newValue));
                if (onReply instanceof Reply.Error) throw new DeviceManagementException(
                        "Cannot set device " + address + " value to " + newValue + ": " +
                                ((Reply.Error) onReply).getReason());
                return newValue;
            });
        }).flatMap(v -> {
            // Update the devices store (DB)
            return deviceStorageCircuitBreaker.setDeviceValue(address.getRawAddress(), v).map(dummy -> v);
        });
    }

    @Override
    @NonNull
    public Mono<Integer> decrement(DeviceAddress address) {
        if (address == null) throw new IllegalArgumentException("address is null");
        return Mono.fromCallable(() -> {
            DeviceAccessSerializer serializer = getDevices().get(address);
            if (serializer == null) throw new UnknownDeviceException(address);
            return serializer.call(device -> {
                // Update a device stub
                if(!device.isOn()) throw new DeviceIsOffException(address);
                int newValue = device.decrementValue();
                Reply onReply = deviceCircuitBreaker.accept(Request.set(address.getRawAddress(), newValue));
                if (onReply instanceof Reply.Error) throw new DeviceManagementException(
                        "Cannot set device " + address + " value to " + newValue + ": " +
                                ((Reply.Error) onReply).getReason());
                return newValue;
            });
        }).flatMap(v -> {
            // Update the devices store (DB)
            return deviceStorageCircuitBreaker.setDeviceValue(address.getRawAddress(), v).map(dummy -> v);
        });
    }

    /**
     * On the first access this method reads devices state from the store
     *
     * @return device address -> Device
     */
    @NonNull
    private Map<DeviceAddress, DeviceAccessSerializer> getDevices() {
        synchronized (devicesInitializationLock) {
            if (!devicesInitialized) {
                devicesInitialized = true;
                deviceStorageCircuitBreaker.readAll().onErrorResume(t -> {
                    LOGGER.warn("Error reading devices state from a DB", t);
                    return Flux.empty();
                }).toStream().forEach(dto -> {
                    DeviceAddress addr = DeviceAddress.of(dto.getAddr());
                    Device d = Device.of(addr, dto.getValue(), dto.isOn());
                    devices.put(addr, new DeviceAccessSerializer(d));
                });
            }
        }
        return devices;
    }

    /**
     * Guaranties sequential access to the device by allowing only one action at a time.
     *
     * <p>{@link #call(Function)} action must not call these methods recursively directly or indirectly.
     * <p>
     * TODO remember thread inside a lock to validate calling thread is not the same.
     */
    private static class DeviceAccessSerializer {
        private final Device device;
        private final Semaphore parallelAccessLimiter = new Semaphore(1);

        private DeviceAccessSerializer(Device device) {
            this.device = device;
        }

        @NonNull
        Device get() {
            return device;
        }

        <T> T call(Function<Device, T> action) {
            boolean acquired = parallelAccessLimiter.tryAcquire();
            if (!acquired) throw new DeviceManagementException(
                    "Device " + device.getAddress() + " is currently executing another action");
            try {
                return action.apply(device);
            } finally {
                parallelAccessLimiter.release();
            }
        }
    }
}
