package ru.zzz.demo.sber.shs.model.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceTest {
    @Test
    public void operationsOnDeviceThatIsOff() {
        Device d = Device.of("addr");
        assertThrows(DeviceIsOffException.class, d::incrementValue);
        assertThrows(DeviceIsOffException.class, d::decrementValue);
        assertFalse(d.getValue().isPresent());
    }

    @Test
    void defaultVolumeTest() {
        Device d = Device.of("addr", 42);
        assertFalse(d.getValue().isPresent());
        d.on();
        assertTrue(d.getValue().isPresent());
        assertEquals(42, d.getValue().orElseThrow(IllegalStateException::new));
    }

    @Test
    void undoOnDecrement() {
        Device d = Device.of("addr", 1);
        d.on();
        assertEquals(1, d.getValue().orElseThrow(IllegalStateException::new));
        d.decrementValue();
        assertEquals(0, d.getValue().orElseThrow(IllegalStateException::new));
        d.decrementValue();
        assertEquals(0, d.getValue().orElseThrow(IllegalStateException::new));
        d.undoValueChange();
        assertEquals(1, d.getValue().orElseThrow(IllegalStateException::new));
    }

    @Test
    void decrementOfZeroDoesNothing() {
        Device d = Device.of("addr", 1);
        d.on();
        assertEquals(1, d.getValue().orElseThrow(IllegalStateException::new));
        d.decrementValue();
        assertEquals(0, d.getValue().orElseThrow(IllegalStateException::new));
        d.decrementValue();
        assertEquals(0, d.getValue().orElseThrow(IllegalStateException::new));
    }

    @Test
    void undoOnIncrement() {
        Device d = Device.of("addr", 1);
        d.on();
        assertEquals(1, d.getValue().orElseThrow(IllegalStateException::new));
        d.incrementValue();
        assertEquals(2, d.getValue().orElseThrow(IllegalStateException::new));
        d.undoValueChange();
        assertEquals(1, d.getValue().orElseThrow(IllegalStateException::new));
        d.undoValueChange();
        assertEquals(1, d.getValue().orElseThrow(IllegalStateException::new));
    }
}