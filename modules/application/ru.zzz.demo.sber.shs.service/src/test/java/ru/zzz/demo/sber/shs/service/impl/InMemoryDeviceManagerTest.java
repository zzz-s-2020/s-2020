package ru.zzz.demo.sber.shs.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.zzz.demo.sber.shs.CircuitBreaker.device.DeviceCircuitBreaker;
import ru.zzz.demo.sber.shs.CircuitBreaker.storage.DeviceStorageCircuitBreaker;
import ru.zzz.demo.sber.shs.db.DbActionException;
import ru.zzz.demo.sber.shs.db.DeviceDto;
import ru.zzz.demo.sber.shs.device.Reply;
import ru.zzz.demo.sber.shs.device.Request;
import ru.zzz.demo.sber.shs.model.device.DeviceAddress;
import ru.zzz.demo.sber.shs.service.api.DeviceDescriptor;
import ru.zzz.demo.sber.shs.service.api.DeviceManagementException;
import ru.zzz.demo.sber.shs.service.api.UnknownDeviceException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InMemoryDeviceManagerTest {
    private DeviceCircuitBreaker dcb = mock(DeviceCircuitBreaker.class);
    private DeviceStorageCircuitBreaker dscb = mock(DeviceStorageCircuitBreaker.class);

    @BeforeEach
    void beforeEach() {
        when(dscb.readAll()).thenReturn(Flux.empty());
    }

    @Test
    void cannotRegisterDevicesWithSameAddresses() {
        //setup
        when(dscb.readAll()).thenReturn(Flux.empty());
        when(dscb.setDeviceIsOff(eq("a"))).thenReturn(Mono.just(TRUE));
        //test and assert
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        StepVerifier.create(dm.register(DeviceAddress.of("a"))).expectComplete().log().verify();
        verify(dscb, times(1)).setDeviceIsOff(eq("a"));
        StepVerifier.create(dm.register(DeviceAddress.of("a"))).expectError().log().verify();
    }

    @Test
    void registrationAfterUnregistrationDevicesWithSameAddresses() {
        //setup
        when(dscb.readAll()).thenReturn(Flux.empty());
        when(dscb.setDeviceIsOff(eq("a"))).thenReturn(Mono.just(TRUE));
        when(dscb.removeDevice(eq("a"))).thenReturn(Mono.just(TRUE));
        //test and assert
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        // register a
        StepVerifier.create(dm.register(DeviceAddress.of("a"))).expectComplete().log().verify();
        verify(dscb, times(1)).setDeviceIsOff(eq("a"));
        // unregister a
        StepVerifier.create(dm.unregister(DeviceAddress.of("a")))
                .expectNext(TRUE)
                .expectComplete()
                .log()
                .verify();
        verify(dscb, times(1)).removeDevice(eq("a"));
        // register a
        StepVerifier.create(dm.register(DeviceAddress.of("a"))).expectComplete().log().verify();
        verify(dscb, times(2)).setDeviceIsOff(eq("a"));
    }

    @Test
    void unregisterAbsentDevice() {
        // setup
        when(dscb.readAll()).thenReturn(Flux.empty());
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        // unregister a
        StepVerifier.create(dm.unregister(DeviceAddress.of("a")))
                .expectNext(FALSE)
                .expectComplete()
                .log()
                .verify();
    }

    @Test
    void switchOnAnUnknownDevice() {
        // setup
        when(dscb.readAll()).thenReturn(Flux.empty());
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        StepVerifier.create(dm.on(DeviceAddress.of("a")))
                .expectError(UnknownDeviceException.class)
                .log()
                .verify();
    }

    @Test
    void switchDeviceOn_Successfully() {
        //setup
        DeviceDto a = DeviceDto.of("a", true, 0, LocalDateTime.now());
        when(dscb.readAll()).thenReturn(Flux.fromArray(new DeviceDto[]{a}));
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        when(dscb.setDeviceValue(eq("a"), eq(0))).thenReturn(Mono.just(TRUE));
        when(dcb.accept(any(Request.class))).thenReturn(Reply.ok());
        // set a on
        StepVerifier.create(dm.on(DeviceAddress.of("a"))).expectComplete().verify();
        verify(dcb, times(2)).accept(any(Request.class));
        verify(dscb, times(1)).setDeviceValue(eq("a"), eq(0));
    }

    @Test
    void switchDeviceOn_DoNotUpdateDbIfDeviceCommunicationFails() {
        //setup
        DeviceDto a = DeviceDto.of("a", true, 0, LocalDateTime.now());
        when(dscb.readAll()).thenReturn(Flux.fromArray(new DeviceDto[]{a}));
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        when(dcb.accept(any(Request.class))).thenReturn(Reply.error("fail"));
        when(dscb.setDeviceIsOff(eq("a"))).thenReturn(Mono.just(TRUE));// for registration
        when(dscb.setDeviceValue(anyString(), anyInt())).thenThrow(
                new RuntimeException("Must not be called"));
        // set a on
        StepVerifier.create(dm.on(DeviceAddress.of("a")))
                .expectError(DeviceManagementException.class)
                .log()
                .verify();
        verify(dcb, times(1)).accept(any(Request.class));
    }

    @Test
    void switchDeviceOn_IfDbUpdateFailsTheDeviceIsStillConsideredToBeOn() {
        //setup
        DeviceDto a = DeviceDto.of("a", false, 0, LocalDateTime.now());
        when(dscb.readAll()).thenReturn(Flux.fromArray(new DeviceDto[]{a}));
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        when(dcb.accept(any(Request.class))).thenReturn(Reply.ok());
        when(dscb.setDeviceValue(anyString(), anyInt())).thenReturn(
                Mono.error(new DbActionException("DB fail", new SQLException("sql"))));
        // set a on
        StepVerifier.create(dm.on(DeviceAddress.of("a"))).expectError(DbActionException.class).log().verify();
        // accept called twice to set device on and to set ins value
        verify(dcb, times(2)).accept(any(Request.class));
        // verify a is considered to be on. Verification uses an in memory list.
        List<DeviceDescriptor> list = dm.listWithCurrentStatus();
        assertEquals(1, list.size());
        DeviceDescriptor dev = list.get(0);
        assertEquals("a", dev.getAddress());
        assertTrue(dev.isOn());
    }

    @Test
    void switchOffAnUnknownDevice() {
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        StepVerifier.create(dm.off(DeviceAddress.of("a")))
                .expectError(UnknownDeviceException.class)
                .log()
                .verify();
    }

    @Test
    void switchDeviceOff_Successfully() {
        //setup: a exists and is on
        DeviceDto a = DeviceDto.of("a", true, 0, LocalDateTime.now());
        when(dscb.readAll()).thenReturn(Flux.fromArray(new DeviceDto[]{a}));
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        when(dscb.setDeviceIsOff(eq("a"))).thenReturn(Mono.just(TRUE));
        when(dcb.accept(any(Request.class))).thenReturn(Reply.ok());
        // set a off
        StepVerifier.create(dm.off(DeviceAddress.of("a"))).expectComplete().verify();
        // verify
        verify(dcb, times(1)).accept(any(Request.class));
        verify(dscb, times(1)).setDeviceIsOff(eq("a"));
    }

    @Test
    void switchDeviceOff_DoNotUpdateDbIfDeviceCommunicationFails() {
        //setup: a exists and is on
        DeviceDto a = DeviceDto.of("a", true, 0, LocalDateTime.now());
        when(dscb.readAll()).thenReturn(Flux.fromArray(new DeviceDto[]{a}));
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        when(dcb.accept(any(Request.class))).thenReturn(Reply.error("fail"));
        when(dscb.setDeviceValue(anyString(), anyInt())).thenThrow(
                new RuntimeException("Must not be called"));
        // set a off
        StepVerifier.create(dm.off(DeviceAddress.of("a")))
                .expectError(DeviceManagementException.class)
                .log()
                .verify();
        // verify
        verify(dcb, times(1)).accept(any(Request.class));
    }

    @Test
    void switchDeviceOn_IfDbUpdateFailsTheDeviceIsStillConsideredToBeOff() {
        //setup
        DeviceDto a = DeviceDto.of("a", true, 0, LocalDateTime.now());
        when(dscb.readAll()).thenReturn(Flux.fromArray(new DeviceDto[]{a}));
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        when(dcb.accept(any(Request.class))).thenReturn(Reply.ok());
        when(dscb.setDeviceIsOff(eq("a"))).thenReturn(
                Mono.error(new DbActionException("DB fail", new SQLException("sql"))));
        // set a on
        StepVerifier.create(dm.off(DeviceAddress.of("a")))
                .expectError(DbActionException.class)
                .log()
                .verify();
        verify(dcb, times(1)).accept(any(Request.class));
        // verify a is considered to be on. Verification uses an in memory list.
        List<DeviceDescriptor> list = dm.listWithCurrentStatus();
        assertEquals(1, list.size());
        DeviceDescriptor dev = list.get(0);
        assertEquals("a", dev.getAddress());
        assertFalse(dev.isOn());
    }

    @Test
    void incrementOfAnUnknownDevice() {
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        StepVerifier.create(dm.increment(DeviceAddress.of("a")))
                .expectError(UnknownDeviceException.class)
                .log()
                .verify();
    }

    @Test
    void increment_Successfully() {
        //setup: a exists and is on
        DeviceDto a = DeviceDto.of("a", true, 42, LocalDateTime.now());
        when(dscb.readAll()).thenReturn(Flux.fromArray(new DeviceDto[]{a}));
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        when(dscb.setDeviceValue(eq("a"), eq(43))).thenReturn(Mono.just(TRUE));
        when(dcb.accept(any(Request.Set.class))).thenReturn(Reply.ok());
        // set a off
        StepVerifier.create(dm.increment(DeviceAddress.of("a"))).expectNext(43).expectComplete().verify();
        // verify
        verify(dcb, times(1)).accept(any(Request.class));
        verify(dscb, times(1)).setDeviceValue(eq("a"), eq(43));
    }

    @Test
    void concurrentOperationsOnTheSameDeviceAreNotAllowed() throws InterruptedException {
        //setup: a exists and is on
        DeviceDto a = DeviceDto.of("a", true, 42, LocalDateTime.now());
        when(dscb.readAll()).thenReturn(Flux.fromArray(new DeviceDto[]{a}));
        InMemoryDeviceManager dm = new InMemoryDeviceManager(dcb, dscb);
        when(dscb.setDeviceValue(eq("a"), anyInt())).thenReturn(Mono.just(TRUE));
        CountDownLatch parallelThreadStartedLatch = new CountDownLatch(1);
        CountDownLatch mainThreadFinishedLatch = new CountDownLatch(1);
        when(dcb.accept(any(Request.Set.class))).thenAnswer(invocation -> {
            parallelThreadStartedLatch.countDown();
            if (!mainThreadFinishedLatch.await(1, SECONDS)) fail("Latch wait failed.");
            return Reply.ok();
        });
        Thread t = new Thread(() -> {
            StepVerifier.create(dm.increment(DeviceAddress.of("a"))).expectNext(43).expectComplete().verify();
        }, "TestParallelCall-concurrentOperationsOnTheSameDeviceAreNotAllowed");
        t.setDaemon(true);
        t.start();
        parallelThreadStartedLatch.await(1, SECONDS);
        StepVerifier.create(dm.increment(DeviceAddress.of("a")))
                .expectError(DeviceManagementException.class)
                .verify();
        mainThreadFinishedLatch.countDown();
        // verify
        t.join(100);
        verify(dcb, times(1)).accept(any(Request.class));
        verify(dscb, times(1)).setDeviceValue(eq("a"), eq(43));
    }
}