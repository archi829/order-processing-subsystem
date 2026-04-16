package com.erp.integration.adapter;

import java.util.List;
import java.util.Map;

/**
 * PATTERN: Adapter (Structural)
 *
 * Bridges loosely-typed backend payloads (typically {@code Map<String,Object>}
 * from JSON) to the UI's strongly-typed DTOs. Used inside each controller's
 * success callback when the raw response shape differs from the DTO.
 *
 * In the mock flow, {@link com.erp.integration.MockUIService} already returns
 * typed DTOs so the adapters pass through unchanged. Backend teams wiring a
 * real {@code IUIService} implementation populate the map-based overloads to
 * hook their wire format without touching controller logic.
 */
public final class DTOAdapter {

    private DTOAdapter() {}

    @SuppressWarnings("unchecked")
    public static <T> T passthrough(Object raw, Class<T> type) {
        if (raw == null) return null;
        if (type.isInstance(raw)) return (T) raw;
        throw new ClassCastException("Cannot adapt " + raw.getClass().getName()
                + " to " + type.getName() + " — real backend wire-format adapter not yet implemented.");
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> passthroughList(Object raw) {
        return raw == null ? java.util.Collections.emptyList() : (List<T>) raw;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Integer> toIntMap(Object raw) {
        return raw == null ? java.util.Collections.emptyMap() : (Map<String, Integer>) raw;
    }
}
