package ru.zzz.demo.sber.shs.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import ru.zzz.demo.sber.shs.model.device.DeviceAddress;
import ru.zzz.demo.sber.shs.rest.dto.DeviceDescriptorDto;
import ru.zzz.demo.sber.shs.rest.dto.DeviceRegistrationRequestDto;
import ru.zzz.demo.sber.shs.rest.dto.DeviceUnregistrationResponseDto;
import ru.zzz.demo.sber.shs.rest.dto.ValueDto;
import ru.zzz.demo.sber.shs.service.api.AlreadyAssociatedException;
import ru.zzz.demo.sber.shs.service.api.DeviceDescriptor;
import ru.zzz.demo.sber.shs.service.api.DeviceManagementException;
import ru.zzz.demo.sber.shs.service.api.DeviceManager;
import ru.zzz.demo.sber.shs.service.api.UnknownDeviceException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FAILED_DEPENDENCY;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller("SHS.Application.Rest.Controller")
public class DeviceManagementController {
    private final DeviceManager deviceManager;
    private final Scheduler scheduler;

    @Autowired
    public DeviceManagementController(DeviceManager deviceManager, Scheduler scheduler) {
        this.deviceManager = deviceManager;
        this.scheduler = scheduler;
    }

    @RequestMapping(value = "/api/device", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Flux<DeviceAddress> deviceList(ServerWebExchange exchange) {
        List<DeviceAddress> list = deviceManager.list();
        return Flux.fromIterable(list);
    }

    @RequestMapping(value = "/api/device/status", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Flux<DeviceDescriptorDto> deviceListStatus(ServerWebExchange exchange) {
        List<DeviceDescriptor> list = deviceManager.listWithCurrentStatus();
        return Flux.fromIterable(list).map(DeviceDescriptorDto::of);
    }

    @RequestMapping(value = "/api/device/{addr}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<DeviceDescriptorDto> deviceStatus(ServerWebExchange exchange,
            @PathVariable("addr") String addr) {
        return deviceManager.getDevice(DeviceAddress.of(addr))
                .map(deviceDescriptor -> Mono.just(DeviceDescriptorDto.of(deviceDescriptor)))
                .orElseGet(Mono::empty);
    }

    @RequestMapping(value = "/api/device", method = POST, consumes = APPLICATION_JSON_VALUE)
    public Mono<Void> deviceRegister(ServerWebExchange exchange,
            @RequestBody Mono<DeviceRegistrationRequestDto> requestDtoMono) {
        return requestDtoMono.map(DeviceRegistrationRequestDto::getAddress)
                .map(DeviceAddress::of)
                .subscribeOn(scheduler)
                .flatMap(deviceManager::register)
                .onErrorMap(AlreadyAssociatedException.class,
                        e -> new ResponseStatusException(CONFLICT, e.getMessage()))
                .onErrorMap(DeviceManagementException.class,
                        e -> new ResponseStatusException(FAILED_DEPENDENCY, e.getMessage()));
    }

    @RequestMapping(value = "/api/device/{addr}", method = DELETE, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<DeviceUnregistrationResponseDto> deviceUnregister(ServerWebExchange exchange,
            @PathVariable("addr") String addr) {
        return deviceManager.unregister(DeviceAddress.of(addr))
                .publishOn(scheduler)
                .map(DeviceUnregistrationResponseDto::ofReallyUnregistered);
    }

    @RequestMapping(value = "/api/device/{addr}/on", method = POST)
    public Mono<Void> deviceOn(ServerWebExchange exchange, @PathVariable("addr") String addr) {
        return deviceManager.on(DeviceAddress.of(addr))
                .publishOn(scheduler)
                .onErrorMap(UnknownDeviceException.class, e -> new ResponseStatusException(NOT_FOUND))
                .onErrorMap(DeviceManagementException.class,
                        e -> new ResponseStatusException(FAILED_DEPENDENCY, e.getMessage()));
    }

    @RequestMapping(value = "/api/device/{addr}/off", method = POST)
    public Mono<Void> deviceOff(ServerWebExchange exchange, @PathVariable("addr") String addr) {
        return deviceManager.off(DeviceAddress.of(addr))
                .publishOn(scheduler)
                .onErrorMap(UnknownDeviceException.class, e -> new ResponseStatusException(NOT_FOUND))
                .onErrorMap(DeviceManagementException.class,
                        e -> new ResponseStatusException(FAILED_DEPENDENCY, e.getMessage()));
    }

    @RequestMapping(value = "/api/device/{addr}/increment", method = POST)
    @ResponseBody
    public Mono<ValueDto> deviceIncrement(ServerWebExchange exchange, @PathVariable("addr") String addr) {
        return deviceManager.increment(DeviceAddress.of(addr))
                .publishOn(scheduler)
                .map(ValueDto::of)
                .onErrorMap(UnknownDeviceException.class, e -> new ResponseStatusException(NOT_FOUND))
                .onErrorMap(DeviceManagementException.class,
                        e -> new ResponseStatusException(FAILED_DEPENDENCY, e.getMessage()));
    }

    @RequestMapping(value = "/api/device/{addr}/decrement", method = POST)
    @ResponseBody
    public Mono<ValueDto> deviceDecrement(ServerWebExchange exchange, @PathVariable("addr") String addr) {
        return deviceManager.decrement(DeviceAddress.of(addr))
                .publishOn(scheduler)
                .map(ValueDto::of)
                .onErrorMap(UnknownDeviceException.class, e -> new ResponseStatusException(NOT_FOUND))
                .onErrorMap(DeviceManagementException.class,
                        e -> new ResponseStatusException(FAILED_DEPENDENCY, e.getMessage()));
    }
}
