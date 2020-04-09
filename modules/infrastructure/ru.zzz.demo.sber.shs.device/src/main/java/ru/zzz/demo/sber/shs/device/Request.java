package ru.zzz.demo.sber.shs.device;

import org.springframework.lang.NonNull;

/**
 * A base type of a request to a device. Subtypes describe commands.
 * Invariant for all commands:
 * /\ address!=null
 * /\ address is not empty
 */
public class Request {
    private final String address;

    private Request(String address) {
        if (address == null || address.isEmpty())
            throw new IllegalArgumentException("address");
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    @NonNull
    public static Request on(String address) {
        return new On(address);
    }

    @NonNull
    public static Request off(String address) {
        return new Off(address);
    }

    @NonNull
    public static Request set(String address, int val) {
        return new Set(address, val);
    }

    @NonNull
    public static Request get(String address) {
        return new Get(address);
    }

    public static class On extends Request {
        protected On(String address) {
            super(address);
        }
    }

    public static class Off extends Request {
        protected Off(String address) {
            super(address);
        }
    }

    public static class Set extends Request {
        private final int val;

        protected Set(String address, int val) {
            super(address);
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }

    public static class Get extends Request {
        protected Get(String address) {
            super(address);
        }
    }
}
