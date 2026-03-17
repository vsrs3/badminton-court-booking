package com.bcb.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RegisterGoogleLinkStore {
    private static final long DEFAULT_TTL_MS = 60 * 1000L;
    private static final Map<String, PendingLinkEntry> STORE = new ConcurrentHashMap<>();

    private RegisterGoogleLinkStore() {
    }

    public static void save(String token, String email) {
        save(token, email, DEFAULT_TTL_MS);
    }

    public static void save(String token, String email, long ttlMs) {
        if (token == null || email == null) {
            return;
        }

        STORE.put(token, new PendingLinkEntry(email, System.currentTimeMillis() + ttlMs));
    }

    public static PendingLinkEntry get(String token) {
        if (token == null) {
            return null;
        }

        PendingLinkEntry entry = STORE.get(token);
        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            STORE.remove(token);
            return null;
        }

        return entry;
    }

    public static void remove(String token) {
        if (token == null) {
            return;
        }

        STORE.remove(token);
    }

    public static final class PendingLinkEntry {
        private final String email;
        private final long expiresAt;

        private PendingLinkEntry(String email, long expiresAt) {
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
}
