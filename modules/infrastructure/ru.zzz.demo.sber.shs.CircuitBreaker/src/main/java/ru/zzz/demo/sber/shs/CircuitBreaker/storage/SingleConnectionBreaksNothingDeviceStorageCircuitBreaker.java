package ru.zzz.demo.sber.shs.CircuitBreaker.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zzz.demo.sber.shs.db.DeviceDto;
import ru.zzz.demo.sber.shs.db.DeviceStorageRepository;

@Component("SHS.Infrastructure.DeviceStorageCircuitBreaker")
class SingleConnectionBreaksNothingDeviceStorageCircuitBreaker implements DeviceStorageCircuitBreaker {
    private final DeviceStorageRepository repository;

    @Autowired
    public SingleConnectionBreaksNothingDeviceStorageCircuitBreaker(DeviceStorageRepository repository) {
        this.repository = repository;
    }

    @Override
    @NonNull
    public Mono<Boolean> setDeviceValue(String address, int value) {
        return repository.setDeviceValue(address, value);
    }

    @Override
    @NonNull
    public Mono<Boolean> setDeviceIsOff(String address) {
        return repository.setDeviceIsOff(address);
    }

    @Override
    @NonNull
    public Mono<Boolean> removeDevice(String address) {
        return repository.removeDevice(address);
    }

    @Override
    @NonNull
    public Flux<DeviceDto> readAll() {
        return repository.readAll();
    }
}
