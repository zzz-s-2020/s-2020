package ru.zzz.demo.sber.shs.device.stub;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.zzz.demo.sber.shs.device.DeviceException;
import ru.zzz.demo.sber.shs.device.DeviceManager;
import ru.zzz.demo.sber.shs.device.Reply;
import ru.zzz.demo.sber.shs.device.Request;

import java.util.concurrent.ConcurrentHashMap;

@Component("SHS.Infrastructure.DeviceStubsManager")
class DeviceStubsManager implements DeviceManager {
    private final ConcurrentHashMap<String, DeviceStub> stubs = new ConcurrentHashMap<>();

    @NonNull
    @Override
    public Reply accept(Request msg) {
        if (msg == null)
            throw new IllegalArgumentException();
        DeviceStub s = stubs.computeIfAbsent(msg.getAddress(), addr -> new DeviceStub());
        try {
            if (msg instanceof Request.On) {
                s.on();
                return Reply.ok();
            } else if (msg instanceof Request.Off) {
                s.off();
                return Reply.ok();
            } else if (msg instanceof Request.Set) {
                s.setVal(((Request.Set) msg).getVal());
                return Reply.ok();
            } else if (msg instanceof Request.Get) {
                return Reply.val(s.getVal());
            } else {
                throw new UnsupportedOperationException("Request of unknown type " + msg);
            }
        } catch (DeviceException e) {
            return Reply.error(e.getMessage());
        }
    }
}
