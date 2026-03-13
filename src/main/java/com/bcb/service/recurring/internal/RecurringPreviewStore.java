package com.bcb.service.recurring.internal;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight in-memory preview store with TTL.
 *
 * @author AnhTN
 */
public final class RecurringPreviewStore {

    private static final long TTL_MINUTES = 15;
    private static final Map<String, RecurringPreviewCacheEntry> STORE = new ConcurrentHashMap<>();

    private RecurringPreviewStore() {
    }

    public static String put(RecurringPreviewCacheEntry entry) {
        cleanupExpired();
        String token = UUID.randomUUID().toString();
        entry.setCreatedAt(LocalDateTime.now());
        STORE.put(token, entry);
        return token;
    }

    public static RecurringPreviewCacheEntry get(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        RecurringPreviewCacheEntry entry = STORE.get(token);
        if (entry == null) {
            return null;
        }
        if (entry.getCreatedAt() == null || entry.getCreatedAt().plusMinutes(TTL_MINUTES).isBefore(LocalDateTime.now())) {
            STORE.remove(token);
            return null;
        }
        return entry;
    }

    private static void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        STORE.entrySet().removeIf(e -> e.getValue() == null
                || e.getValue().getCreatedAt() == null
                || e.getValue().getCreatedAt().plusMinutes(TTL_MINUTES).isBefore(now));
    }
}

