package com.bcb.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EmailActionSyncStore {
    public static final String PURPOSE_REGISTER = "register";
    public static final String PURPOSE_FORGOT_PASSWORD = "forgot-password";
    private static final long DEFAULT_TTL_MS = 30 * 60 * 1000L;
    private static final Map<String, SyncEntry> STORE = new ConcurrentHashMap<>();

    private EmailActionSyncStore() {
    }

    public static void markConfirmed(String purpose, String token, String email) {
        markConfirmed(purpose, token, email, DEFAULT_TTL_MS);
    }

    public static void markConfirmed(String purpose, String token, String email, long ttlMs) {
        STORE.put(buildKey(purpose, token), new SyncEntry(email, System.currentTimeMillis() + ttlMs));
    }

    public static SyncEntry getConfirmed(String purpose, String token) {
        if (purpose == null || token == null) {
            return null;
        }

        String key = buildKey(purpose, token);
        SyncEntry entry = STORE.get(key);
        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            STORE.remove(key);
            return null;
        }

        return entry;
    }

    public static void remove(String purpose, String token) {
        if (purpose == null || token == null) {
            return;
        }

        STORE.remove(buildKey(purpose, token));
    }

    public static ConfirmedAction findConfirmedByEmail(String purpose, String email) {
        if (purpose == null || email == null) {
            return null;
        }

        for (Map.Entry<String, SyncEntry> item : STORE.entrySet()) {
            String key = item.getKey();
            SyncEntry entry = item.getValue();

            if (!key.startsWith(purpose + ":")) {
                continue;
            }

            if (entry == null || entry.isExpired()) {
                STORE.remove(key);
                continue;
            }

            if (email.equalsIgnoreCase(entry.getEmail())) {
                String token = key.substring((purpose + ":").length());
                return new ConfirmedAction(token, entry.getEmail(), entry.getExpiresAt());
            }
        }

        return null;
    }

    private static String buildKey(String purpose, String token) {
        return purpose + ":" + token;
    }

    public static final class SyncEntry {
        private final String email;
        private final long expiresAt;

        private SyncEntry(String email, long expiresAt) {
            this.email = email;
            this.expiresAt = expiresAt;
        }

        public String getEmail() {
            return email;
        }

        public long getExpiresAt() {
            return expiresAt;
        }

        private boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    public static final class ConfirmedAction {
        private final String token;
        private final String email;
        private final long expiresAt;

        private ConfirmedAction(String token, String email, long expiresAt) {
            this.token = token;
            this.email = email;
            this.expiresAt = expiresAt;
        }

        public String getToken() {
            return token;
        }

        public String getEmail() {
            return email;
        }

        public long getExpiresAt() {
            return expiresAt;
        }
    }
}
