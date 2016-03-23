package org.messic.android.smartphone.rxevents;

import java.util.HashMap;

/**
 * Created by lgvalle on 22/07/15.
 */
public class RxAction {
    private final String type;
    private final HashMap<String, Object> data;

    RxAction(String type, HashMap<String, Object> data) {
        this.type = type;
        this.data = data;
    }

    public static Builder create(String type) {
        return new Builder().with(type);
    }

    public boolean isType(String type) {
        return this.type.equals(type);
    }

    public String getType() {
        return type;
    }

    public Object getSimpleData() {
        return data.get(type);
    }

    public HashMap getData() {
        return data;
    }

    public static class Builder {

        private String type;
        private HashMap<String, Object> data;

        Builder with(String type) {
            if (type == null) {
                throw new IllegalArgumentException("Type may not be null.");
            }
            this.type = type;
            this.data = new HashMap<>();
            return this;
        }

        public Builder bundle(String key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("Key may not be null.");
            }

            if (value == null) {
                throw new IllegalArgumentException("Value may not be null.");
            }
            data.put(key, value);
            return this;
        }

        public RxAction build() {
            if (type == null || type.isEmpty()) {
                throw new IllegalArgumentException("At least one key is required.");
            }
            return new RxAction(type, data);
        }
    }
}