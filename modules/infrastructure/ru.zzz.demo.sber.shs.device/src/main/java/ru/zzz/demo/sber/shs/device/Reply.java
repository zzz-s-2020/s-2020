package ru.zzz.demo.sber.shs.device;

import org.springframework.lang.NonNull;

/**
 * A base type of a devices' replies. Subtypes describe reply commands.
 */
public class Reply {
    private Reply() {
    }

    @NonNull
    public static Reply ok() {
        return new Ok();
    }

    @NonNull
    public static Reply error(String reason) {
        return new Error(reason);
    }

    @NonNull
    public static Reply val(int val) {
        return new Val(val);
    }

    public static class Ok extends Reply {
        protected Ok() {
        }
    }

    public static class Error extends Reply {
        private final String reason;

        protected Error(String reason) {
            if (reason == null || reason.isEmpty())
                throw new IllegalArgumentException();
            this.reason = reason;
        }

        @NonNull
        public String getReason() {
            return reason;
        }
    }

    public static class Val extends Reply {
        private final int val;

        protected Val(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }
}
